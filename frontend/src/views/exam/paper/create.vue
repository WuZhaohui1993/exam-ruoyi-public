<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>{{ getTitle() }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-back" @click="goBack">返回</el-button>
      </div>


      <el-form ref="form" :model="form" :rules="rules" label-width="120px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="试卷名称" prop="paperName">
              <el-input v-model="form.paperName" placeholder="请输入试卷名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="试卷分类" prop="categoryId">
              <paper-category-tree-select v-model="form.categoryId" placeholder="请选择试卷分类" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="难度等级" prop="difficultyLevel">
              <el-select v-model="form.difficultyLevel" placeholder="请选择难度等级">
                <el-option label="简单" :value="1" />
                <el-option label="中等" :value="2" />
                <el-option label="困难" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="及格分数" prop="passScore">
              <el-input-number v-model="form.passScore" :min="0" :max="1000" controls-position="right" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="考试时长" prop="duration">
              <el-input-number v-model="form.duration" :min="1" :max="1000" controls-position="right" />
              <span style="margin-left: 10px;">分钟</span>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="题目乱序">
              <el-switch v-model="form.shuffleQuestions" active-value="1" inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="选项乱序">
              <el-switch v-model="form.shuffleOptions" active-value="1" inactive-value="0"></el-switch>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="试卷描述">
          <el-input v-model="form.paperDescription" type="textarea" :rows="3" placeholder="请输入试卷描述" />
        </el-form-item>

        <!-- 固定试卷题目选择 -->
        <div v-if="paperType === 'fixed'">
          <el-divider content-position="left">题目配置</el-divider>
          <el-button type="primary" icon="el-icon-plus" @click="showQuestionDialog">添加题目</el-button>


          <el-table :data="selectedQuestions" style="margin-top: 20px;" show-summary :summary-method="getSummaries">
            <el-table-column label="题目标题" prop="questionTitle" show-overflow-tooltip>
              <template slot-scope="scope">
                <span>{{ scope.row.questionTitle || scope.row.questionContent }}</span>
              </template>
            </el-table-column>
            <el-table-column label="题目类型" prop="questionType" width="100">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.exam_question_type" :value="scope.row.questionType"/>
              </template>
            </el-table-column>
            <el-table-column label="分值" prop="questionScore" width="80"></el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="removeQuestion(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 随机试卷配置 -->
        <div v-if="paperType === 'random'">
          <el-divider content-position="left">随机规则配置</el-divider>
          <el-button type="primary" icon="el-icon-plus" @click="addRandomRule">添加规则</el-button>


          <div v-for="(rule, index) in randomRules" :key="index" class="random-rule">
            <el-card style="margin-top: 10px;">
              <div slot="header" class="clearfix">
                <span>规则 {{ index + 1 }}</span>
                <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-delete" @click="removeRandomRule(index)">删除</el-button>
              </div>
              <el-row :gutter="20">
                <el-col :span="6">
                  <el-form-item label="题目类型">
                    <el-select v-model="rule.questionType" placeholder="选择题目类型">
                      <el-option
                        v-for="dict in dict.type.exam_question_type"
                        :key="dict.value"
                        :label="dict.label"
                        :value="dict.value"
                      />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="题目数量">
                    <el-input-number v-model="rule.questionCount" :min="1" :max="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="单题分值">
                    <el-input-number v-model="rule.questionScore" :min="1" :max="100" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="难度等级">
                    <el-select v-model="rule.difficultyLevel" placeholder="选择难度" clearable>
                      <el-option label="简单" :value="1" />
                      <el-option label="中等" :value="2" />
                      <el-option label="困难" :value="3" />
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="题目分类">
                    <category-tree-select v-model="rule.categoryId" placeholder="选择题目分类，不选择表示不限制分类" />
                  </el-form-item>
                </el-col>
              </el-row>
            </el-card>
          </div>
        </div>

        <!-- 混合试卷配置 -->
        <div v-if="paperType === 'mixed'">
          <el-divider content-position="left">混合配置</el-divider>
          <el-tabs v-model="activeTab">
            <el-tab-pane label="固定题目" name="fixed">
              <el-button type="primary" icon="el-icon-plus" @click="showQuestionDialog">添加题目</el-button>
              <el-table :data="selectedQuestions" style="margin-top: 20px;" show-summary :summary-method="getSummaries">
                <el-table-column label="题目标题" prop="questionTitle" show-overflow-tooltip>
                  <template slot-scope="scope">
                    <span>{{ scope.row.questionTitle || scope.row.questionContent }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="题目类型" prop="questionType" width="100">
                  <template slot-scope="scope">
                    <dict-tag :options="dict.type.exam_question_type" :value="scope.row.questionType"/>
                  </template>
                </el-table-column>
                <el-table-column label="分值" prop="questionScore" width="80"></el-table-column>
                <el-table-column label="操作" width="100">
                  <template slot-scope="scope">
                    <el-button size="mini" type="text" icon="el-icon-delete" @click="removeQuestion(scope.$index)">移除</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </el-tab-pane>
            <el-tab-pane label="随机规则" name="random">
              <el-button type="primary" icon="el-icon-plus" @click="addRandomRule">添加规则</el-button>
              <div v-for="(rule, index) in randomRules" :key="index" class="random-rule">
                <el-card style="margin-top: 10px;">
                  <div slot="header" class="clearfix">
                    <span>规则 {{ index + 1 }}</span>
                    <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-delete" @click="removeRandomRule(index)">删除</el-button>
                  </div>
                  <el-row :gutter="20">
                    <el-col :span="6">
                      <el-form-item label="题目类型">
                        <el-select v-model="rule.questionType" placeholder="选择题目类型">
                          <el-option
                            v-for="dict in dict.type.exam_question_type"
                            :key="dict.value"
                            :label="dict.label"
                            :value="dict.value"
                          />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="6">
                      <el-form-item label="题目数量">
                        <el-input-number v-model="rule.questionCount" :min="1" :max="100" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="6">
                      <el-form-item label="单题分值">
                        <el-input-number v-model="rule.questionScore" :min="1" :max="100" />
                      </el-form-item>
                    </el-col>
                    <el-col :span="6">
                      <el-form-item label="难度等级">
                        <el-select v-model="rule.difficultyLevel" placeholder="选择难度" clearable>
                          <el-option label="简单" :value="1" />
                          <el-option label="中等" :value="2" />
                          <el-option label="困难" :value="3" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-row :gutter="20">
                    <el-col :span="12">
                      <el-form-item label="题目分类">
                        <category-tree-select v-model="rule.categoryId" placeholder="选择题目分类，不选择表示不限制分类" />
                      </el-form-item>
                    </el-col>
                  </el-row>
                </el-card>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <el-form-item style="margin-top: 30px; text-align: center;">
          <el-button type="primary" @click="submitForm" :loading="submitLoading">保存试卷</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 题目选择对话框 -->
    <el-dialog title="选择题目" :visible.sync="questionDialogVisible" width="80%" top="5vh">
      <question-selector

        v-if="questionDialogVisible"

        @confirm="handleQuestionSelect"

        @cancel="questionDialogVisible = false"
        :selected-questions="selectedQuestions"
      />
    </el-dialog>
  </div>
</template>

<script>
import { createFixedPaper, createRandomPaper, createMixedPaper } from "@/api/exam/paper";
import QuestionSelector from './components/QuestionSelector';
import PaperCategoryTreeSelect from "@/components/PaperCategoryTreeSelect";
import CategoryTreeSelect from "@/components/CategoryTreeSelect";

export default {
  name: "CreatePaper",
  dicts: ['exam_question_type'],
  components: {
    QuestionSelector,
    PaperCategoryTreeSelect,
    CategoryTreeSelect
  },
  data() {
    return {
      paperType: '',
      activeTab: 'fixed',
      submitLoading: false,
      questionDialogVisible: false,
      selectedQuestions: [],
      randomRules: [],
      form: {
        paperName: '',
        categoryId: null,
        paperDescription: '',
        passScore: 60,
        duration: 120,
        difficultyLevel: 1,
        shuffleQuestions: '0',
        shuffleOptions: '0'
      },
      rules: {
        paperName: [
          { required: true, message: "试卷名称不能为空", trigger: "blur" }
        ],
        categoryId: [
          { required: true, message: "试卷分类不能为空", trigger: "change" }
        ],
        passScore: [
          { required: true, message: "及格分数不能为空", trigger: "blur" }
        ],
        duration: [
          { required: true, message: "考试时长不能为空", trigger: "blur" }
        ]
      }
    };
  },
  created() {
    this.paperType = this.$route.params.type || 'fixed';
  },
  methods: {

    /** 获取页面标题 */
    getTitle() {
      const typeMap = {
        'fixed': '创建固定试卷',
        'random': '创建随机试卷',
        'mixed': '创建混合试卷'
      };
      return typeMap[this.paperType] || '创建试卷';
    },
    /** 返回 */
    goBack() {
      this.$tab.closePage();
    },
    /** 显示题目选择对话框 */
    showQuestionDialog() {
      this.questionDialogVisible = true;
    },
    /** 处理题目选择 */
    handleQuestionSelect(questions) {
      this.selectedQuestions = questions;
      this.questionDialogVisible = false;
    },
    /** 移除题目 */
    removeQuestion(index) {
      this.selectedQuestions.splice(index, 1);
    },
    /** 计算表格汇总信息 */
    getSummaries(param) {
      const { columns, data } = param;
      const sums = [];
      columns.forEach((column, index) => {
        if (index === 0) {
          sums[index] = '总计';
          return;
        }
        if (index === 1) {
          sums[index] = `${data.length} 道题目`;
          return;
        }
        if (index === 2) {
          const values = data.map(item => Number(item.questionScore));
          if (!values.every(value => isNaN(value))) {
            const total = values.reduce((prev, curr) => {
              const value = Number(curr);
              if (!isNaN(value)) {
                return prev + curr;
              } else {
                return prev;
              }
            }, 0);
            sums[index] = `${total} 分`;
          } else {
            sums[index] = '';
          }
          return;
        }
        sums[index] = '';
      });
      return sums;
    },
    /** 添加随机规则 */
    addRandomRule() {
      this.randomRules.push({
        questionType: '',
        questionCount: 5,
        questionScore: 2,
        difficultyLevel: null,
        categoryId: null
      });
    },
    /** 移除随机规则 */
    removeRandomRule(index) {
      this.randomRules.splice(index, 1);
    },
    /** 提交表单 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitLoading = true;


          const paperData = {
            ...this.form,
            paperType: this.paperType
          };

          if (this.paperType === 'fixed') {
            if (this.selectedQuestions.length === 0) {
              this.$message.error('请选择至少一道题目');
              this.submitLoading = false;
              return;
            }
            paperData.questions = this.selectedQuestions.map(q => ({
              questionId: q.id,
              questionScore: q.questionScore
            }));
            this.createPaper(createFixedPaper, paperData);
          } else if (this.paperType === 'random') {
            if (this.randomRules.length === 0) {
              this.$message.error('请配置至少一条随机规则');
              this.submitLoading = false;
              return;
            }
            paperData.randomConfig = JSON.stringify(this.randomRules);
            this.createPaper(createRandomPaper, paperData);
          } else if (this.paperType === 'mixed') {
            if (this.selectedQuestions.length === 0 && this.randomRules.length === 0) {
              this.$message.error('请选择题目或配置随机规则');
              this.submitLoading = false;
              return;
            }
            paperData.questions = this.selectedQuestions.map(q => ({
              questionId: q.id,
              questionScore: q.questionScore
            }));
            paperData.randomConfig = JSON.stringify(this.randomRules);
            this.createPaper(createMixedPaper, paperData);
          }
        }
      });
    },
    /** 创建试卷 */
    createPaper(apiMethod, data) {
      apiMethod(data).then(response => {
        this.$modal.msgSuccess("创建成功");


        // 从API响应中获取新创建的试卷ID
        const paperId = response.data?.id || response.data;


        if (paperId) {
          // 跳转到试卷预览页面
          this.$router.push(`/paper-manage/exam/paper/fullscreen-preview/${paperId}`);
        } else {
          // 如果没有返回ID，跳转到试卷管理页面
          this.$router.push('/paper-manage/exam/paper');
        }
      }).catch(error => {
        console.error('创建试卷失败:', error);
      }).finally(() => {
        this.submitLoading = false;
      });
    }
  }
};
</script>

<style scoped>
.random-rule {
  margin-bottom: 10px;
}

/* 使用appendToBody后，下拉菜单渲染到body中，不再需要复杂的z-index设置 */
</style>
