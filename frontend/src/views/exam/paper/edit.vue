<template>
  <div class="app-container">
    <el-card>
      <div slot="header" class="clearfix">
        <span>编辑试卷</span>
        <el-button style="float: right; padding: 3px 0" type="text" icon="el-icon-back" @click="goBack">返回</el-button>
      </div>


      <el-form ref="form" :model="form" :rules="rules" label-width="120px" v-loading="loading">
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
            <el-form-item label="试卷类型">
              <el-tag :type="getTypeColor(form.paperType)">{{ getTypeName(form.paperType) }}</el-tag>
            </el-form-item>
          </el-col>
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
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio label="0">正常</el-radio>
                <el-radio label="1">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
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
          <el-col :span="8">
            <el-form-item label="总分">
              <span>{{ form.totalScore || 0 }} 分</span>
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

        <!-- 固定试卷题目编辑 -->
        <div v-if="form.paperType === 'fixed' || form.paperType === 'mixed'">
          <el-divider content-position="left">题目配置</el-divider>
          <el-button type="primary" icon="el-icon-plus" @click="showQuestionDialog">添加题目</el-button>


          <el-table :data="selectedQuestions" style="margin-top: 20px;">
            <el-table-column label="序号" type="index" width="50"></el-table-column>
            <el-table-column label="题目标题" prop="questionTitle" show-overflow-tooltip min-width="300">
              <template slot-scope="scope">
                <span>{{ scope.row.questionTitle || scope.row.questionContent }}</span>
              </template>
            </el-table-column>
            <el-table-column label="题目类型" prop="questionType" width="100">
              <template slot-scope="scope">
                <dict-tag :options="dict.type.exam_question_type" :value="scope.row.questionType"/>
              </template>
            </el-table-column>
            <el-table-column label="分值" prop="questionScore" width="120">
              <template slot-scope="scope">
                <el-input-number
                  v-model="scope.row.questionScore"
                  :min="1"
                  :max="100"
                  size="mini"
                  @change="calculateTotalScore"
                />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template slot-scope="scope">
                <el-button size="mini" type="text" icon="el-icon-delete" @click="removeQuestion(scope.$index)">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 随机试卷配置编辑 -->
        <div v-if="form.paperType === 'random' || form.paperType === 'mixed'">
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
                    <el-input-number v-model="rule.questionCount" :min="1" :max="100" @change="calculateTotalScore" />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="单题分值">
                    <el-input-number v-model="rule.questionScore" :min="1" :max="100" @change="calculateTotalScore" />
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

        <el-form-item style="margin-top: 30px; text-align: center;">
          <el-button type="primary" @click="submitForm" :loading="submitLoading">保存修改</el-button>
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
import { getPaper, updatePaper, updatePaperFull, getPaperQuestions } from "@/api/exam/paper";
import QuestionSelector from './components/QuestionSelector';
import PaperCategoryTreeSelect from "@/components/PaperCategoryTreeSelect";
import CategoryTreeSelect from "@/components/CategoryTreeSelect";

export default {
  name: "EditPaper",
  dicts: ['exam_question_type'],
  components: {
    QuestionSelector,
    PaperCategoryTreeSelect,
    CategoryTreeSelect
  },
  data() {
    return {
      loading: false,
      submitLoading: false,
      questionDialogVisible: false,
      selectedQuestions: [],
      randomRules: [],
      form: {
        id: null,
        paperName: '',
        categoryId: null,
        paperType: '',
        paperDescription: '',
        totalScore: 0,
        passScore: 60,
        duration: 120,
        difficultyLevel: 1,
        shuffleQuestions: '0',
        shuffleOptions: '0',
        status: '0'
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
    const paperId = this.$route.params.id;
    this.getPaperInfo(paperId);
  },
  methods: {

    /** 获取试卷信息 */
    getPaperInfo(id) {
      this.loading = true;
      getPaper(id).then(response => {
        this.form = response.data;


        // 解析随机配置
        if (this.form.questionConfig) {
          try {
            const config = JSON.parse(this.form.questionConfig);
            this.randomRules = config.randomConfig ? Object.values(config.randomConfig) : [];
          } catch (e) {
            this.randomRules = [];
          }
        }


        // 获取固定题目
        if (this.form.paperType === 'fixed' || this.form.paperType === 'mixed') {
          this.getPaperQuestions(id);
        } else {
          this.loading = false;
        }
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 获取试卷题目 */
    getPaperQuestions(paperId) {
      getPaperQuestions(paperId).then(response => {
        this.selectedQuestions = response.data.map(item => ({
          ...item.question,
          questionScore: item.score || item.questionScore || 2
        }));
        this.loading = false;
      }).catch(() => {
        this.loading = false;
      });
    },
    /** 获取试卷类型名称 */
    getTypeName(type) {
      const typeMap = {
        'fixed': '固定试卷',
        'random': '随机试卷',
        'mixed': '混合试卷'
      };
      return typeMap[type] || type;
    },
    /** 获取试卷类型颜色 */
    getTypeColor(type) {
      const colorMap = {
        'fixed': '',
        'random': 'success',
        'mixed': 'warning'
      };
      return colorMap[type] || '';
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
      this.calculateTotalScore();
    },
    /** 移除题目 */
    removeQuestion(index) {
      this.selectedQuestions.splice(index, 1);
      this.calculateTotalScore();
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
      this.calculateTotalScore();
    },
    /** 计算总分 */
    calculateTotalScore() {
      let totalScore = 0;


      // 固定题目分数
      this.selectedQuestions.forEach(question => {
        totalScore += question.questionScore || 0;
      });


      // 随机题目分数
      this.randomRules.forEach(rule => {
        totalScore += (rule.questionCount || 0) * (rule.questionScore || 0);
      });


      this.form.totalScore = totalScore;
    },
    /** 提交表单 */
    submitForm() {
      this.$refs["form"].validate(valid => {
        if (valid) {
          this.submitLoading = true;


          const paperData = {
            ...this.form
          };

          // 检查是否需要更新题目
          const needUpdateQuestions = (this.form.paperType === 'fixed' || this.form.paperType === 'mixed') && this.selectedQuestions.length > 0;
          const needUpdateRandomConfig = (this.form.paperType === 'random' || this.form.paperType === 'mixed') && this.randomRules.length > 0;

          if (needUpdateQuestions || needUpdateRandomConfig) {
            // 使用完整更新API
            if (needUpdateQuestions) {
              paperData.questions = this.selectedQuestions.map(q => ({
                questionId: q.id,
                questionScore: q.questionScore
              }));
            }


            if (needUpdateRandomConfig) {
              paperData.randomConfig = JSON.stringify(this.randomRules);
            }

            updatePaperFull(paperData).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.$router.push('/paper-manage/exam/paper');
            }).finally(() => {
              this.submitLoading = false;
            });
          } else {
            // 使用基础更新API
            updatePaper(paperData).then(response => {
              this.$modal.msgSuccess("修改成功");
              this.$router.push('/paper-manage/exam/paper');
            }).finally(() => {
              this.submitLoading = false;
            });
          }
        }
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
