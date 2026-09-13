<template>
  <div class="exam-taking-container">
    <!-- 顶部工具栏 -->
    <div class="exam-header">
      <div class="exam-info">
        <h2>{{ examInfo.examName }}</h2>
        <span>试卷：{{ examInfo.paperName }}</span>
      </div>
      <div class="exam-timer">
        <el-alert
          :title="timeRemaining"
          type="warning"
          :closable="false"
          show-icon
        />
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="exam-content" v-loading="loading">
      <el-row :gutter="20">
        <!-- 左侧题目区域 -->
        <el-col :span="18">
          <div class="question-area">
            <!-- 题目导航 -->
            <div class="question-nav">
              <el-button-group>
                <el-button
                  v-for="(question, index) in questions"
                  :key="question.id"
                  :type="getQuestionButtonType(index)"
                  size="small"
                  @click="jumpToQuestion(index)"
                  class="question-nav-btn"
                >
                  {{ index + 1 }}
                </el-button>
              </el-button-group>
            </div>

            <!-- 当前题目 -->
            <div class="current-question" v-if="currentQuestion">
              <div class="question-header">
                <h3>第 {{ currentQuestionIndex + 1 }} 题 ({{ currentQuestion.score }}分)</h3>
                <el-tag :type="getQuestionTypeTag(currentQuestion.question.questionType)">
                  {{ getQuestionTypeText(currentQuestion.question.questionType) }}
                </el-tag>
              </div>

              <div class="question-content">
                <div
                  v-if="isFillQuestion(currentQuestion.question.questionType) && hasFillBlanks(currentQuestion.question.questionTitle)"
                  class="question-title fill-question-title"
                >
                  <div class="fill-content">
                    <template v-for="(part, index) in fillTitleParts">
                      <span v-if="part.type === 'text'" :key="`title-text-${index}`" v-html="sanitizeContent(part.content)"></span>
                      <span v-else-if="part.type === 'blank' && fillBlanks[part.index]" :key="`title-blank-${index}`" class="fill-blank-wrapper">
                        <el-input
                          v-model="fillBlanks[part.index].answer"
                          :placeholder="`请输入第${part.index + 1}空答案`"
                          size="small"
                          style="width: 130px; margin: 0 4px;"
                          @blur="saveFillAnswer"
                        />
                      </span>
                    </template>
                  </div>
                </div>
                <div v-else class="question-title">
                  {{ currentQuestion.question.questionTitle }}
                </div>

                <template v-if="isFillQuestion(currentQuestion.question.questionType)">
                  <!-- 填空题特殊处理：题目内容与答案框融合 -->
                  <div
                    v-if="currentQuestion.question.questionContent && hasFillBlanks(currentQuestion.question.questionContent)"
                    class="question-detail fill-question"
                  >
                    <div class="fill-content">
                      <template v-for="(part, index) in fillContentParts">
                        <!-- 文本部分 -->
                        <span v-if="part.type === 'text'" :key="`content-text-${index}`" v-html="sanitizeContent(part.content)"></span>
                        <!-- 填空部分 -->
                        <span v-else-if="part.type === 'blank' && fillBlanks[part.index]" :key="`content-blank-${index}`" class="fill-blank-wrapper">
                          <el-input
                            v-model="fillBlanks[part.index].answer"
                            :placeholder="`请输入第${part.index + 1}空答案`"
                            size="small"
                            style="width: 130px; margin: 0 4px;"
                            @blur="saveFillAnswer"
                          />
                        </span>
                      </template>
                    </div>
                  </div>
                  <div v-else-if="currentQuestion.question.questionContent" class="question-detail">
                    <div v-html="formatQuestionContent(currentQuestion.question.questionContent)"></div>
                  </div>
                  <div v-if="!hasAnyFillBlanks" class="question-input fill-fallback">
                    <el-input
                      v-model="currentAnswer"
                      type="textarea"
                      :rows="3"
                      placeholder="请输入答案，多个空用英文逗号分隔"
                      @blur="saveCurrentAnswer"
                    />
                  </div>
                </template>

                <!-- 非填空题的题目内容 -->
                <div v-else-if="currentQuestion.question.questionContent" class="question-detail">
                  <div v-html="formatQuestionContent(currentQuestion.question.questionContent)"></div>
                </div>

                <question-media :media-list="currentQuestion.question.mediaList" />

                <!-- 选择题选项 -->
                <div v-if="isChoiceQuestion(currentQuestion.question.questionType)" class="question-options">
                  <el-radio-group 
                    v-if="currentQuestion.question.questionType === 'single'"
                    v-model="currentAnswer"
                    @change="saveCurrentAnswer"
                  >
                    <el-radio
                      v-for="option in currentQuestion.question.optionList"
                      :key="option.key"
                      :label="option.key"
                      class="option-item"
                    >
                      {{ option.key }}. {{ option.value }}
                    </el-radio>
                  </el-radio-group>

                  <el-checkbox-group 
                    v-if="currentQuestion.question.questionType === 'multiple'"
                    v-model="currentAnswer"
                    @change="saveCurrentAnswer"
                  >
                    <el-checkbox
                      v-for="option in currentQuestion.question.optionList"
                      :key="option.key"
                      :label="option.key"
                      class="option-item"
                    >
                      {{ option.key }}. {{ option.value }}
                    </el-checkbox>
                  </el-checkbox-group>
                </div>

                <!-- 判断题 -->
                <div v-if="currentQuestion.question.questionType === 'judge'" class="question-options">
                  <el-radio-group v-model="currentAnswer" @change="saveCurrentAnswer">
                    <el-radio label="1" class="option-item">正确</el-radio>
                    <el-radio label="0" class="option-item">错误</el-radio>
                  </el-radio-group>
                </div>

                <!-- 简答题 -->
                <div v-if="currentQuestion.question.questionType === 'essay'" class="question-input">
                  <el-input
                    v-model="currentAnswer"
                    type="textarea"
                    :rows="6"
                    placeholder="请输入答案"
                    @blur="saveCurrentAnswer"
                  />
                </div>
              </div>

              <!-- 题目操作 -->
              <div class="question-actions">
                <el-button @click="prevQuestion" :disabled="currentQuestionIndex === 0">上一题</el-button>
                <el-button @click="nextQuestion" :disabled="currentQuestionIndex === questions.length - 1">下一题</el-button>
                <el-button type="warning" @click="markQuestion">
                  {{ isMarked(currentQuestionIndex) ? '取消标记' : '标记题目' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-col>

        <!-- 右侧答题卡 -->
        <el-col :span="6">
          <div class="answer-card">
            <el-card>
              <div slot="header">
                <span>答题卡</span>
              </div>
              
              <div class="answer-overview">
                <div class="stats">
                  <p>总题数：{{ questions.length }}</p>
                  <p>已答题：<span class="answered">{{ answeredCount }}</span></p>
                  <p>未答题：<span class="unanswered">{{ questions.length - answeredCount }}</span></p>
                  <p>已标记：<span class="marked">{{ markedQuestions.size }}</span></p>
                </div>

                <div class="legend">
                  <div class="legend-item">
                    <span class="legend-color answered-color"></span>
                    <span>已答</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-color current-color"></span>
                    <span>当前</span>
                  </div>
                  <div class="legend-item">
                    <span class="legend-color marked-color"></span>
                    <span>标记</span>
                  </div>
                </div>

                <div class="question-grid">
                  <div
                    v-for="(question, index) in questions"
                    :key="question.id"
                    class="grid-item"
                    :class="{
                      'current': index === currentQuestionIndex,
                      'answered': isAnswered(index),
                      'marked': isMarked(index)
                    }"
                    @click="jumpToQuestion(index)"
                  >
                    {{ index + 1 }}
                  </div>
                </div>
              </div>

              <div class="submit-section">
                <el-button 
                  type="primary" 
                  @click="showSubmitDialog" 
                  size="medium" 
                  style="width: 100%;"
                  :loading="submitting"
                  :disabled="submitting"
                >
                  {{ submitting ? '提交中...' : '提交试卷' }}
                </el-button>
              </div>
            </el-card>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 提交确认对话框 -->
    <el-dialog title="提交确认" :visible.sync="submitDialogVisible" width="500px" append-to-body>
      <div class="submit-summary">
        <p>您确认要提交试卷吗？提交后将无法再次修改答案。</p>
        <div class="submit-stats">
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-number">{{ questions.length }}</div>
                <div class="stat-label">总题数</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-number answered-number">{{ answeredCount }}</div>
                <div class="stat-label">已答题</div>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-number unanswered-number">{{ questions.length - answeredCount }}</div>
                <div class="stat-label">未答题</div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="stat-item">
                <div class="stat-number marked-number">{{ markedQuestions.size }}</div>
                <div class="stat-label">已标记</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>
      
      <div slot="footer" class="dialog-footer">
        <el-button @click="submitDialogVisible = false" :disabled="submitting">取消</el-button>
        <el-button 
          type="primary" 
          @click="submitExam"
          :loading="submitting"
          :disabled="submitting"
        >
          {{ submitting ? '提交中...' : '确认提交' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 时间到自动提交提示 -->
    <el-dialog title="考试时间已到" :visible.sync="timeoutDialogVisible" width="400px" :close-on-click-modal="false" :close-on-press-escape="false">
      <p>考试时间已到，系统将自动提交您的答案。</p>
      <div slot="footer" class="dialog-footer">
        <el-button 
          type="primary" 
          @click="forceSubmit"
          :loading="submitting"
          :disabled="submitting"
        >
          {{ submitting ? '提交中...' : '确定' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { getExamPaper, saveAnswer, submitExam, getRemainingTime } from "@/api/student/exam"
import QuestionMedia from "@/components/QuestionMedia"
import { sanitizeRichText } from "@/utils/sanitize"

export default {
  name: "StudentExamTaking",
  components: {
    QuestionMedia
  },
  data() {
    return {
      loading: false,
      examId: null,
      examUserId: null, // 新增：用于标识本次考试尝试的唯一ID
      examInfo: {},
      questions: [],
      answers: {},
      currentQuestionIndex: 0,
      currentAnswer: '',
      fillBlanks: [], // 存储填空题的多个空格答案
      markedQuestions: new Set(),
      
      // 时间相关
      remainingMinutes: 0,
      remainingSeconds: 0,
      examDuration: 0, // 考试总时长（分钟）
      startTime: null, // 考试开始时间
      timer: null,
      
      // 对话框
      submitDialogVisible: false,
      timeoutDialogVisible: false,
      
      // 题型映射
      questionTypeMap: {
        'single': '单选题',
        'multiple': '多选题',
        'judge': '判断题',
        'fill': '填空题',
        'fill_blank': '填空题',
        'blank': '填空题',
        '3': '填空题',
        'essay': '简答题'
      },
      
      // 答案保存相关
      saveTimer: null,
      saving: false,
      retryQueue: [],
      
      // 提交相关
      submitting: false,
      
      // 考试状态标志
      examEnded: false
    };
  },
  computed: {
    currentQuestion() {
      return this.questions[this.currentQuestionIndex];
    },
    timeRemaining() {
      const hours = Math.floor(this.remainingMinutes / 60);
      const minutes = this.remainingMinutes % 60;
      const seconds = this.remainingSeconds;
      return `剩余时间：${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    },
    answeredCount() {
      // 修复：正确计算已答题数量，排除Vue响应式对象的干扰
      const count = Object.keys(this.answers).filter(key => {
        const answer = this.answers[key];
        return answer !== undefined && answer !== null && answer !== '';
      }).length;
      
      return count;
    },
    fillTitleParts() {
      const question = this.currentQuestion && this.currentQuestion.question ? this.currentQuestion.question : {};
      return this.getFillParts(question.questionTitle || '', 0);
    },
    fillContentParts() {
      const question = this.currentQuestion && this.currentQuestion.question ? this.currentQuestion.question : {};
      const titleBlankCount = this.countFillBlanks(question.questionTitle || '');
      return this.getFillParts(question.questionContent || '', titleBlankCount);
    },
    hasAnyFillBlanks() {
      if (!this.currentQuestion || !this.isFillQuestion(this.currentQuestion.question.questionType)) {
        return false;
      }
      const question = this.currentQuestion.question;
      return this.countFillBlanks(question.questionTitle) + this.countFillBlanks(question.questionContent) > 0;
    },
    /** 解析填空题内容，分离文本和空格 */
    fillQuestionParts() {
      if (!this.currentQuestion || 
          !this.isFillQuestion(this.currentQuestion.question.questionType)) {
        return [];
      }
      return [
        ...this.fillTitleParts,
        ...this.fillContentParts
      ];
    }
  },
  created() {
    this.examId = this.$route.params.examId;
    this.examUserId = this.$route.query.examUserId;
    
    if (!this.examId || this.examId === 'undefined') {
      this.$modal.msgError("考试ID无效，请重新进入考试");
      this.$router.push('/student/exam');
      return;
    }
    
    this.initExam();
  },
  beforeDestroy() {
    // 清理定时器
    this.clearTimer();
    
    // 组件销毁前尝试保存当前答案，但忽略考试状态相关错误
    this.saveCurrentAnswerImmediately().catch(error => {
      // 如果是考试状态相关错误，静默处理
      if (error.message && (
          error.message.includes('您当前不在考试状态') || 
          error.message.includes('考试已结束') ||
          error.message.includes('考试状态')
        )) {
      } else {
        console.error('组件销毁时保存答案失败:', error);
      }
    });
  },
  methods: {
    /** 初始化考试 */
    async initExam() {
      this.loading = true;
      try {
        const response = await getExamPaper(this.examId, this.examUserId);
        
        // 兼容不同的响应格式
        const data = response.data || response;
        
        if (!data || !data.examination) {
          throw new Error('考试数据格式错误');
        }
        
        this.examInfo = data.examination;
        this.questions = data.questions;
        
        // 设置考试时长和开始时间
        this.examDuration = data.examination.duration; // 考试时长（分钟）
        
        // 调试：打印examUser信息
        
        // 设置examUserId用于后续API调用
        if (data.examUser && data.examUser.id) {
          this.examUserId = data.examUser.id;
          // 不再需要存储到localStorage
        } else {
          console.warn('未找到examUser信息，某些功能可能受限');
        }
        
        // 检查考试用户状态
        if (!data.examUser) {
          throw new Error('用户考试信息缺失，请重新开始考试');
        }
        
        // 检查考试状态
        if (data.examUser.examStatus !== 'in_progress') {
          throw new Error('考试状态异常：' + data.examUser.examStatus + '，请重新开始考试');
        }
        
        // 处理开始时间
        if (data.examUser.startTime) {
          this.startTime = new Date(data.examUser.startTime);
          // 验证时间有效性
          if (isNaN(this.startTime.getTime())) {
            console.warn('开始时间格式异常，使用当前时间');
            this.startTime = new Date();
          }
        } else {
          console.warn('开始时间为空，使用当前时间');
          this.startTime = new Date();
        }
        
        // 初始化已答题记录
        if (data.answers && data.answers.length > 0) {
          data.answers.forEach(answer => {
            // 修复：确保字段名一致性，支持多种可能的字段名
            const questionId = answer.questionId || answer.id;
            const userAnswer = answer.userAnswer || answer.answer;
            if (questionId && userAnswer !== undefined && userAnswer !== null && userAnswer !== '') {
              // 使用Vue的响应式方法确保计算属性能正确更新
              this.$set(this.answers, questionId, userAnswer);
            }
          });
        }
        
        // 处理题目选项
        this.questions.forEach(question => {
          if (question.question.questionOptions) {
            try {
              question.question.optionList = JSON.parse(question.question.questionOptions);
            } catch (e) {
              question.question.optionList = [];
            }
          }
        });
        
        // 设置当前答案
        this.setCurrentAnswer();
        
        // 启动定时器
        this.startTimer();
        
      } catch (error) {
        console.error('获取考试数据失败:', error);
        this.$modal.msgError("无法进入考试：" + (error.message || error.msg || "当前考试状态不可答题"));
        this.$router.replace('/student/exam/list');
      } finally {
        this.loading = false;
      }
    },
    
    /** 启动计时器 */
    startTimer() {
      this.updateRemainingTime();
      this.timer = setInterval(() => {
        this.updateRemainingTime();
      }, 1000); // 每秒更新一次
    },
    
    /** 更新剩余时间 */
    updateRemainingTime() {
      try {
        const now = new Date();
        const elapsedMs = now.getTime() - this.startTime.getTime();
        
        // 防止负数时间（系统时间异常情况）
        if (elapsedMs < 0) {
          console.warn('检测到系统时间异常，使用服务器时间同步');
          this.syncTimeWithServer();
          return;
        }
        
        const elapsedMinutes = Math.floor(elapsedMs / (1000 * 60));
        const elapsedSeconds = Math.floor((elapsedMs % (1000 * 60)) / 1000);
        
        // 计算剩余时间
        const totalSeconds = this.examDuration * 60; // 总秒数
        const usedSeconds = elapsedMinutes * 60 + elapsedSeconds; // 已用秒数
        const remainingTotalSeconds = Math.max(0, totalSeconds - usedSeconds); // 剩余总秒数
        
        this.remainingMinutes = Math.floor(remainingTotalSeconds / 60);
        this.remainingSeconds = remainingTotalSeconds % 60;
        
        // 检查是否超时
        if (remainingTotalSeconds <= 0) {
          this.clearTimer();
          this.timeoutDialogVisible = true;
          return;
        }
        
        // 倒计时最后5分钟时增加同步频率
        if (remainingTotalSeconds <= 300 && remainingTotalSeconds % 60 === 0) {
          this.syncTimeWithServer();
        }
        // 其他时间每5分钟同步一次
        else if (usedSeconds > 0 && usedSeconds % 300 === 0) {
          this.syncTimeWithServer();
        }
      } catch (error) {
        console.error('更新剩余时间失败:', error);
        // 时间计算出错时，使用服务器时间
        this.syncTimeWithServer();
      }
    },
    
    /** 与服务器同步时间 */
    async syncTimeWithServer() {
      try {
        const response = await getRemainingTime(this.examId, this.examUserId);
        const responseData = response && response.data ? response.data : response;
        const serverRemainingMinutes = responseData.remainingMinutes;
        if (serverRemainingMinutes === undefined || serverRemainingMinutes === null) {
          return;
        }
        
        // 计算本地剩余时间（分钟）
        const now = new Date();
        const elapsedMs = now.getTime() - this.startTime.getTime();
        const totalSeconds = this.examDuration * 60;
        const usedSeconds = Math.floor(elapsedMs / 1000);
        const localRemainingSeconds = Math.max(0, totalSeconds - usedSeconds);
        const localRemainingMinutes = Math.floor(localRemainingSeconds / 60);
        
        // 比较服务器时间和本地时间的差异
        const timeDifference = Math.abs(serverRemainingMinutes - localRemainingMinutes);
        
        // 如果差异超过1分钟，使用服务器时间进行校正
        if (timeDifference > 1) {
          
          // 根据服务器剩余时间重新计算开始时间
          const newElapsedMinutes = this.examDuration - serverRemainingMinutes;
          this.startTime = new Date(now.getTime() - newElapsedMinutes * 60 * 1000);
          
          // 立即更新显示
          this.remainingMinutes = serverRemainingMinutes;
          this.remainingSeconds = 0;
        }
        
        // 检查服务器超时状态
        if (response.data.isTimeout) {
          this.timeoutDialogVisible = true;
          this.clearTimer();
        }
      } catch (error) {
      }
    },
    
    /** 清理定时器 */
    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer);
        this.timer = null;
      }
      if (this.saveTimer) {
        clearTimeout(this.saveTimer);
        this.saveTimer = null;
      }
    },
    
    /** 设置当前答案 */
    setCurrentAnswer() {
      // 修复：确保字段名一致性
      const questionId = this.currentQuestion?.questionId || this.currentQuestion?.id || this.currentQuestion?.question?.id;
      const answer = this.answers[questionId];
      
      
      if (answer !== undefined && answer !== null && answer !== '') {
        if (this.currentQuestion.question.questionType === 'multiple') {
          // 多选题答案是数组
          this.currentAnswer = answer.split(',');
        } else if (this.isFillQuestion(this.currentQuestion.question.questionType)) {
          // 填空题需要特殊处理
          this.currentAnswer = answer;
          this.initializeFillBlanks(answer);
        } else {
          this.currentAnswer = answer;
        }
      } else {
        // 重置答案
        if (this.currentQuestion.question.questionType === 'multiple') {
          this.currentAnswer = [];
        } else if (this.isFillQuestion(this.currentQuestion.question.questionType)) {
          this.currentAnswer = '';
          this.initializeFillBlanks('');
        } else {
          this.currentAnswer = '';
        }
      }
    },
    
    /** 初始化填空题答案 */
    initializeFillBlanks(savedAnswer) {
      if (!this.currentQuestion || !this.isFillQuestion(this.currentQuestion.question.questionType)) {
        return;
      }

      const question = this.currentQuestion.question;
      const blankCount = this.countFillBlanks(question.questionTitle) + this.countFillBlanks(question.questionContent);
      
      if (blankCount === 0) {
        this.$set(this, 'fillBlanks', []);
        return;
      }
      
      // 解析保存的答案
      let answersArray = [];
      if (savedAnswer) {
        try {
          // 尝试解析JSON数组格式的答案
          const answers = JSON.parse(savedAnswer);
          if (Array.isArray(answers)) {
            answersArray = answers;
          }
        } catch (error) {
          answersArray = savedAnswer.split(',');
        }
      }
      
      // 直接创建完整的fillBlanks数组，包含正确的答案
      const newFillBlanks = [];
      for (let i = 0; i < blankCount; i++) {
        newFillBlanks.push({
          index: i,
          answer: answersArray[i] || ''
        });
      }
      
      // 使用Vue.set一次性设置整个数组，避免触发多次计算属性更新
      this.$set(this, 'fillBlanks', newFillBlanks);
      
      // 强制触发模板更新
      this.$nextTick(() => {
        this.$forceUpdate();
      });
    },
    
    /** 解析填空题中的空格标记 */
    parseFillBlanks(questionContent) {
      if (!questionContent) return [];
      
      const matches = this.getBlankMatches(questionContent);
      
      if (!matches || matches.length === 0) {
        return [];
      }
      
      // 为每个空格创建一个答案对象
      return matches.map((_, index) => ({
        index: index,
        answer: ''
      }));
    },
    
    /** 保存当前答案（带防抖优化） */
    saveCurrentAnswer() {
      // 如果考试已结束，跳过保存
      if (this.examEnded) {
        return;
      }
      
      // 清除之前的定时器
      if (this.saveTimer) {
        clearTimeout(this.saveTimer);
      }
      
      // 立即更新本地状态
      this.updateLocalAnswer();
      
      // 防抖保存到服务器（1000ms后执行，减少频繁保存）
      this.saveTimer = setTimeout(() => {
        this.saveToServer();
      }, 1000);
    },
    
    /** 更新本地答案状态 */
    updateLocalAnswer() {
      // 修复：确保字段名一致性
      const questionId = this.currentQuestion?.questionId || this.currentQuestion?.id || this.currentQuestion?.question?.id;
      let answerValue = this.currentAnswer;
      
      if (this.isFillQuestion(this.currentQuestion.question.questionType) && this.hasAnyFillBlanks) {
        this.updateFillCurrentAnswer();
        answerValue = this.currentAnswer;
      }

      
      // 处理多选题答案
      if (this.currentQuestion.question.questionType === 'multiple' && Array.isArray(this.currentAnswer)) {
        answerValue = this.currentAnswer.join(',');
      }
      
      // 更新本地答案记录 - 修复：判断题选择"错误"（0）也是有效答案
      if (answerValue !== '' && answerValue !== null && answerValue !== undefined) {
        // 对于多选题，如果是空数组也认为是无效答案
        if (this.currentQuestion.question.questionType === 'multiple' && Array.isArray(this.currentAnswer) && this.currentAnswer.length === 0) {
          this.$delete(this.answers, questionId);
        } else {
          this.$set(this.answers, questionId, answerValue);
        }
      } else {
        this.$delete(this.answers, questionId);
      }
      
      // 强制触发响应式更新
      this.$forceUpdate();
    },
    
    /** 保存到服务器 */
    async saveToServer() {
      // 如果考试已结束，跳过保存
      if (this.examEnded) {
        return;
      }
      
      // 如果已经在保存中，跳过此次保存
      if (this.saving) {
        return;
      }
      
      const questionId = this.currentQuestion?.questionId || this.currentQuestion?.id || this.currentQuestion?.question?.id;
      let answerValue = this.currentAnswer;
      
      if (this.isFillQuestion(this.currentQuestion.question.questionType) && this.hasAnyFillBlanks) {
        this.updateFillCurrentAnswer();
        answerValue = this.currentAnswer;
      }

      // 处理多选题答案
      if (this.currentQuestion.question.questionType === 'multiple' && Array.isArray(this.currentAnswer)) {
        answerValue = this.currentAnswer.join(',');
      }
      
      // 添加到待保存队列
      const saveData = {
        examId: this.examId,
        examUserId: this.examUserId, // 添加examUserId参数
        questionId: questionId,
        userAnswer: answerValue
      };
      
      this.saving = true;
      try {
        await saveAnswer(saveData);
        // 保存成功后处理队列中的其他请求
        await this.processSaveQueue();
      } catch (error) {
        console.error('保存答案失败:', error);
        
        // 检查是否是重复提交错误
        if (error.message && error.message.includes('数据正在处理')) {
          console.warn('重复提交被阻止，忽略此错误');
        } else if (error.message && (
            error.message.includes('您当前不在考试状态') || 
            error.message.includes('考试已结束') ||
            error.message.includes('考试状态')
          )) {
          console.warn('检测到考试状态错误，设置考试结束标志:', error.message);
          this.examEnded = true;
        } else {
          // 其他错误才显示给用户
          this.$modal.msgError("保存答案失败，将在下次切题时重试");
          // 添加到重试队列
          this.retryQueue.push(saveData);
        }
      } finally {
        this.saving = false;
      }
    },
    
    /** 处理保存队列 */
    async processSaveQueue() {
      // 处理重试队列中的请求
      while (this.retryQueue.length > 0) {
        const saveData = this.retryQueue.shift();
        try {
          await saveAnswer(saveData);
        } catch (error) {
          console.error('重试保存失败:', error);
          // 如果重试失败，重新加入队列（最多重试3次）
          if (!saveData.retryCount) {
            saveData.retryCount = 1;
          } else {
            saveData.retryCount++;
          }
          
          if (saveData.retryCount < 3) {
            this.retryQueue.push(saveData);
          } else {
            console.error('答案保存失败，已达最大重试次数:', saveData);
          }
          break; // 一旦失败，停止处理队列
        }
      }
    },
    
    /** 立即保存当前答案（用于切题时） */
    async saveCurrentAnswerImmediately() {
      // 如果考试已结束，跳过保存
      if (this.examEnded) {
        return;
      }
      
      if (this.saveTimer) {
        clearTimeout(this.saveTimer);
        this.saveTimer = null;
      }
      
      this.updateLocalAnswer();
      
      try {
        await this.saveToServer();
      } catch (error) {
        // 如果是考试状态相关错误，不要抛出异常，避免阻断后续流程
        if (error.message && (
            error.message.includes('您当前不在考试状态') || 
            error.message.includes('考试已结束') ||
            error.message.includes('考试状态')
          )) {
          console.warn('考试状态已变化，无法保存答案:', error.message);
          return; // 静默处理，不抛出异常
        }
        // 其他错误继续抛出
        throw error;
      }
    },
    
    /** 跳转到指定题目 */
    async jumpToQuestion(index) {
      if (index >= 0 && index < this.questions.length) {
        // 切题前立即保存当前答案
        await this.saveCurrentAnswerImmediately();
        
        this.currentQuestionIndex = index;
        this.setCurrentAnswer();
      }
    },
    
    /** 上一题 */
    async prevQuestion() {
      if (this.currentQuestionIndex > 0) {
        await this.jumpToQuestion(this.currentQuestionIndex - 1);
      }
    },
    
    /** 下一题 */
    async nextQuestion() {
      if (this.currentQuestionIndex < this.questions.length - 1) {
        await this.jumpToQuestion(this.currentQuestionIndex + 1);
      }
    },
    
    /** 标记题目 */
    markQuestion() {
      const index = this.currentQuestionIndex;
      if (this.markedQuestions.has(index)) {
        this.markedQuestions.delete(index);
      } else {
        this.markedQuestions.add(index);
      }
    },
    
    /** 显示提交对话框 */
    showSubmitDialog() {
      this.submitDialogVisible = true;
    },
    
    /** 提交考试 */
    async submitExam() {
      // 防止重复提交
      if (this.submitting) {
        return;
      }
      
      this.submitDialogVisible = false;
      this.submitting = true;
      
      try {
        // 提交前尝试保存当前答案，如果失败也继续提交
        try {
          await this.saveCurrentAnswerImmediately();
        } catch (saveError) {
          console.warn('提交前保存答案失败:', saveError);
          // 如果是考试状态相关的错误，忽略并继续提交
          if (saveError.message && (
              saveError.message.includes('您当前不在考试状态') || 
              saveError.message.includes('考试已结束') ||
              saveError.message.includes('考试状态')
            )) {
          } else {
            // 其他错误也警告但不阻止提交
            console.warn('保存答案时出现其他错误，但继续提交:', saveError);
          }
        }
        
        const submitResponse = await submitExam(this.examId, { examUserId: this.examUserId });
        
        // 设置考试结束标志，避免后续保存操作
        this.examEnded = true;
        
        this.clearTimer();
        this.$modal.msgSuccess("提交成功！正在生成成绩...");
        
        // 延时2秒后跳转，给服务器时间处理成绩
        setTimeout(() => {
          // 修复：使用examUserId（考试记录ID）而不是examId（考试ID）跳转到结果页面
          const resultId = this.examUserId || this.examId;
          
          // 使用对象形式的路由跳转，确保参数正确传递
          this.$router.push({ 
            name: 'StudentExamResult',
            params: { id: String(resultId) }
          }).catch(err => {
            // 如果命名路由失败，使用路径跳转
            console.warn('命名路由跳转失败，使用路径跳转:', err);
            this.$router.push({ path: `/student/exam-detail/result/${resultId}` });
          });
        }, 2000);
        
      } catch (error) {
        console.error('提交失败:', error);
        // 检查是否是重复提交错误
        if (error.message && error.message.includes('数据正在处理')) {
          this.$modal.msgWarning("正在处理提交请求，请稍候...");
        } else if (error.message && error.message.includes('您当前不在考试状态')) {
          // 考试状态异常，可能已经提交成功，直接跳转到成绩页面
          this.$modal.msgWarning("考试状态发生变化，正在跳转到成绩页面...");
          this.clearTimer();
          setTimeout(() => {
            // 修复：使用examUserId（考试记录ID）而不是examId（考试ID）跳转到结果页面
            const resultId = this.examUserId || this.examId;
            
            // 使用对象形式的路由跳转，确保参数正确传递
            this.$router.push({ 
              name: 'StudentExamResult',
              params: { id: String(resultId) }
            }).catch(err => {
              // 如果命名路由失败，使用路径跳转
              console.warn('异常情况命名路由跳转失败，使用路径跳转:', err);
              this.$router.push({ path: `/student/exam-detail/result/${resultId}` });
            });
          }, 1000);
        } else {
          this.$modal.msgError("提交失败：" + (error.msg || error.message || "请重试"));
        }
      } finally {
        this.submitting = false;
      }
    },
    
    /** 强制提交（时间到） */
    async forceSubmit() {
      // 防止重复提交
      if (this.submitting) {
        return;
      }
      
      this.timeoutDialogVisible = false;
      this.submitting = true;
      
      try {
        // 提交前尝试保存当前答案，如果失败也继续提交
        try {
          await this.saveCurrentAnswerImmediately();
        } catch (saveError) {
          console.warn('强制提交前保存答案失败:', saveError);
          // 如果是考试状态相关的错误，忽略并继续提交
          if (saveError.message && (
              saveError.message.includes('您当前不在考试状态') || 
              saveError.message.includes('考试已结束') ||
              saveError.message.includes('考试状态')
            )) {
          } else {
            // 其他错误也警告但不阻止提交
            console.warn('保存答案时出现其他错误，但继续提交:', saveError);
          }
        }
        
        const submitResponse = await submitExam(this.examId, { examUserId: this.examUserId });
        
        // 设置考试结束标志，避免后续保存操作
        this.examEnded = true;
        
        this.clearTimer();
        this.$modal.msgSuccess("提交成功！正在生成成绩...");
        
        // 延时2秒后跳转，给服务器时间处理成绩
        setTimeout(() => {
          // 修复：使用examUserId（考试记录ID）而不是examId（考试ID）跳转到结果页面
          const resultId = this.examUserId || this.examId;
          this.$router.push({ path: '/student/exam-detail/result/' + resultId });
        }, 2000);
        
      } catch (error) {
        console.error('强制提交失败:', error);
        // 检查是否是重复提交错误
        if (error.message && error.message.includes('数据正在处理')) {
          this.$modal.msgWarning("正在处理提交请求，请稍候...");
        } else if (error.message && error.message.includes('您当前不在考试状态')) {
          // 考试状态异常，可能已经提交成功，直接跳转到成绩页面
          this.$modal.msgWarning("考试时间已到，正在跳转到成绩页面...");
          this.clearTimer();
          setTimeout(() => {
            // 修复：使用examUserId（考试记录ID）而不是examId（考试ID）跳转到结果页面
            const resultId = this.examUserId || this.examId;
            
            // 使用对象形式的路由跳转，确保参数正确传递
            this.$router.push({ 
              name: 'StudentExamResult',
              params: { id: String(resultId) }
            }).catch(err => {
              // 如果命名路由失败，使用路径跳转
              console.warn('异常情况命名路由跳转失败，使用路径跳转:', err);
              this.$router.push({ path: `/student/exam-detail/result/${resultId}` });
            });
          }, 1000);
        } else {
          this.$modal.msgError("提交失败：" + (error.msg || error.message || "请重试"));
        }
      } finally {
        this.submitting = false;
      }
    },
    
    /** 解析填空题中的空格标记 */
    parseFillBlanks(questionContent) {
      if (!questionContent) return [];
      
      const matches = this.getBlankMatches(questionContent);
      
      if (!matches || matches.length === 0) {
        return [];
      }
      
      // 为每个空格创建一个答案对象
      return matches.map((_, index) => ({
        index: index,
        answer: ''
      }));
    },
    
    /** 保存填空题答案 */
    saveFillAnswer() {
      // 更新currentAnswer
      if (this.hasAnyFillBlanks) {
        this.updateFillCurrentAnswer();
      }

      // 调用通用的保存方法
      this.saveCurrentAnswer();
    },
    
    /** 更新填空题的currentAnswer */
    updateFillCurrentAnswer() {
      if (this.fillBlanks.length === 0) {
        this.currentAnswer = '';
        return;
      }
      
      // 将所有空格的答案组合成JSON数组字符串
      const answers = this.fillBlanks.map(blank => blank.answer || '');
      this.currentAnswer = JSON.stringify(answers);
    },
    
    /** 获取题目按钮类型 */
    getQuestionButtonType(index) {
      if (index === this.currentQuestionIndex) return 'primary';
      if (this.isAnswered(index)) return 'success';
      if (this.isMarked(index)) return 'warning';
      return 'default';
    },
    
    /** 获取题型标签类型 */
    getQuestionTypeTag(type) {
      const tagMap = {
        'single': 'primary',
        'multiple': 'success', 
        'judge': 'warning',
        'fill': 'info',
        'essay': 'danger'
      };
      return tagMap[type] || 'default';
    },
    
    /** 获取题型文本 */
    getQuestionTypeText(type) {
      return this.questionTypeMap[type] || type;
    },
    
    /** 判断是否为选择题 */
    isChoiceQuestion(type) {
      return ['single', 'multiple'].includes(type);
    },

    /** 判断是否为填空题 */
    isFillQuestion(type) {
      return ['fill', 'fill_blank', 'blank', '3'].includes(String(type));
    },
    
    /** 判断是否为文本题 */
    isTextQuestion(type) {
      return ['fill', 'fill_blank', 'blank', '3', 'essay'].includes(String(type));
    },

    getBlankMatches(content) {
      if (!content) {
        return [];
      }
      return String(content).match(/\[空\]|\{\{空\}\}|_{3,}/g) || [];
    },

    countFillBlanks(content) {
      return this.getBlankMatches(content).length;
    },

    hasFillBlanks(content) {
      return this.countFillBlanks(content) > 0;
    },

    getFillParts(content, startIndex = 0) {
      if (!content) {
        return [];
      }

      const text = String(content);
      const parts = [];
      const blankPattern = /\[空\]|\{\{空\}\}|_{3,}/g;
      let lastIndex = 0;
      let blankIndex = startIndex;
      let match;

      while ((match = blankPattern.exec(text)) !== null) {
        if (match.index > lastIndex) {
          const textContent = text.substring(lastIndex, match.index);
          if (textContent) {
            parts.push({
              type: 'text',
              content: textContent
            });
          }
        }

        parts.push({
          type: 'blank',
          index: blankIndex
        });

        blankIndex++;
        lastIndex = match.index + match[0].length;
      }

      if (lastIndex < text.length) {
        const textContent = text.substring(lastIndex);
        if (textContent) {
          parts.push({
            type: 'text',
            content: textContent
          });
        }
      }

      return parts;
    },
    
    /** 判断题目是否已答 */
    isAnswered(index) {
      // 修复：确保字段名一致性
      const questionId = this.questions[index]?.questionId || this.questions[index]?.id || this.questions[index]?.question?.id;
      const answer = this.answers[questionId];
      // 修复：确保"0"也被认为是有效答案
      const result = answer !== undefined && answer !== null && answer !== '';
      
      if (index <= 2) { // 只打印前3个题目避免日志过多
      }
      
      return result;
    },
    
    /** 判断题目是否已标记 */
    isMarked(index) {
      return this.markedQuestions.has(index);
    },
    
    /** 格式化题目内容显示 */
    formatQuestionContent(content) {
      if (!content) return '';
      return sanitizeRichText(content.replace(/\[空\]|\{\{空\}\}|_{3,}/g, '___'));
    },

    sanitizeContent(content) {
      return sanitizeRichText(content);
    }
  },
  
  beforeRouteLeave(to, _, next) {
    if (to.path.includes('/student/exam-detail/result/')) {
      // 允许跳转到成绩页面，直接放行
      next();
    } else {
      // 其他情况需要确认
      this.$modal.confirm('确认离开考试页面吗？离开后考试状态将保持不变。').then(() => {
        next();
      }).catch(() => {
        next(false);
      });
    }
  }
};
</script>

<style scoped>
.exam-taking-container {
  min-height: 100vh;
  background-color: #f5f7fa;
}

.exam-header {
  background: white;
  padding: 20px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: sticky;
  top: 0;
  z-index: 100;
}

.exam-info h2 {
  margin: 0 0 5px 0;
  color: #303133;
}

.exam-content {
  padding: 20px;
}

.question-area {
  background: white;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
}

.question-nav {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #e4e7ed;
}

.question-nav-btn {
  margin: 2px;
  width: 40px;
}

.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.question-header h3 {
  margin: 0;
  color: #303133;
}

.question-content {
  margin-bottom: 30px;
}

.question-title {
  font-size: 16px;
  line-height: 1.6;
  margin-bottom: 15px;
  color: #303133;
}

.question-detail {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 4px;
}

.question-options, .question-input {
  margin: 20px 0;
}

.option-item {
  display: block;
  margin: 10px 0;
  line-height: 1.6;
}

.question-actions {
  display: flex;
  gap: 10px;
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.answer-card {
  position: sticky;
  top: 120px;
}

.answer-overview .stats {
  margin-bottom: 20px;
}

.answer-overview .stats p {
  margin: 8px 0;
  display: flex;
  justify-content: space-between;
}

.answered { color: #67c23a; }
.unanswered { color: #f56c6c; }
.marked { color: #e6a23c; }

.legend {
  display: flex;
  gap: 15px;
  margin-bottom: 20px;
  font-size: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 5px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
}

.answered-color { background-color: #67c23a; }
.current-color { background-color: #409eff; }
.marked-color { background-color: #e6a23c; }

.question-grid {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 8px;
  margin-bottom: 20px;
}

.grid-item {
  width: 35px;
  height: 35px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  background: white;
}

.grid-item:hover {
  border-color: #409eff;
}

.grid-item.current {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.grid-item.answered {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.grid-item.marked {
  background-color: #e6a23c;
  color: white;
  border-color: #e6a23c;
}

.submit-section {
  padding-top: 20px;
  border-top: 1px solid #e4e7ed;
}

.submit-summary {
  text-align: center;
}

.submit-stats {
  margin: 20px 0;
}

.stat-item {
  text-align: center;
  padding: 15px;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 5px;
}

.answered-number { color: #67c23a; }
.unanswered-number { color: #f56c6c; }
.marked-number { color: #e6a23c; }

.stat-label {
  font-size: 14px;
  color: #909399;
}

/* 填空题多空样式 */
.fill-blanks-container {
  margin: 20px 0;
}

.fill-blanks {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.fill-blank-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.blank-label {
  min-width: 80px;
  font-weight: 500;
  color: #606266;
  font-size: 14px;
}

.blank-input {
  flex: 1;
  max-width: 300px;
}

.single-fill {
  margin: 10px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .fill-blank-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 5px;
  }
  
  .blank-label {
    min-width: auto;
  }
  
  .blank-input {
    width: 100%;
    max-width: none;
  }
}
</style>
