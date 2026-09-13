<template>
  <div class="h5-exam-taking" v-loading="loading">
    <!-- 顶部状态栏 -->
    <div class="exam-status-bar">
      <div class="exam-title">{{ examInfo.examName }}</div>
      <div class="exam-timer" :class="{ 'warning': remainingMinutes < 5 }">
        <i class="el-icon-time"></i>
        {{ timeRemaining }}
      </div>
    </div>

    <!-- 题目内容 -->
    <div class="question-container" v-if="currentQuestion">
      <div class="question-header">
        <span class="question-index">第 {{ currentQuestionIndex + 1 }} / {{ questions.length }} 题</span>
        <span class="question-score">（{{ currentQuestion.score }}分）</span>
        <span class="question-type">{{ getQuestionTypeName(currentQuestion.question.questionType) }}</span>
      </div>

      <div class="question-content">
        <template v-if="isFillQuestion(currentQuestion.question.questionType)">
          <div
            v-if="hasFillBlanks(currentQuestion.question.questionTitle)"
            class="question-title fill-question-title"
          >
            <div class="fill-content">
              <template v-for="(part, index) in fillTitleParts">
                <span
                  v-if="part.type === 'text'"
                  :key="'title-text-' + index"
                  class="fill-text"
                  v-html="sanitizeContent(part.content)"
                ></span>
                <span v-else-if="part.type === 'blank' && fillBlanks[part.index]" :key="'title-blank-' + index" class="fill-blank-wrapper">
                  <input
                    type="text"
                    v-model="fillBlanks[part.index].answer"
                    :placeholder="'第' + (part.index + 1) + '空'"
                    class="fill-blank-input"
                    @blur="saveFillAnswer"
                  />
                </span>
              </template>
            </div>
          </div>
          <div v-else class="question-title">{{ currentQuestion.question.questionTitle }}</div>
          <div
            v-if="currentQuestion.question.questionContent && !hasFillBlanks(currentQuestion.question.questionContent)"
            class="question-detail"
            v-html="formatQuestionContent(currentQuestion.question.questionContent)"
          ></div>
        </template>
        <template v-else>
          <div class="question-title">{{ currentQuestion.question.questionTitle }}</div>
          <div
            v-if="currentQuestion.question.questionContent"
            class="question-detail"
            v-html="formatQuestionContent(currentQuestion.question.questionContent)"
          ></div>
        </template>
        <question-media :media-list="currentQuestion.question.mediaList" />
      </div>

      <!-- 选择题选项 -->
      <div class="options-list" v-if="isChoiceQuestion">
        <div
          v-for="option in currentQuestion.question.optionList"
          :key="option.key"
          class="option-item"
          :class="{
            'selected': isOptionSelected(option.key),
            'multiple': currentQuestion.question.questionType === 'multiple'
          }"
          @click="selectOption(option.key)"
        >
          <span class="option-key">{{ option.key }}</span>
          <span class="option-value">{{ option.value }}</span>
          <i v-if="isOptionSelected(option.key)" class="el-icon-check"></i>
        </div>
      </div>

      <!-- 判断题选项 -->
      <div class="options-list" v-if="currentQuestion.question.questionType === 'judge'">
        <div
          class="option-item"
          :class="{ 'selected': currentAnswer === '1' }"
          @click="selectJudge('1')"
        >
          <span class="option-key">✓</span>
          <span class="option-value">正确</span>
          <i v-if="currentAnswer === '1'" class="el-icon-check"></i>
        </div>
        <div
          class="option-item"
          :class="{ 'selected': currentAnswer === '0' }"
          @click="selectJudge('0')"
        >
          <span class="option-key">✗</span>
          <span class="option-value">错误</span>
          <i v-if="currentAnswer === '0'" class="el-icon-check"></i>
        </div>
      </div>

      <!-- 填空题：支持多个填空 -->
      <div class="fill-question" v-if="isFillQuestion(currentQuestion.question.questionType)">
        <div class="fill-content" v-if="hasFillBlanks(currentQuestion.question.questionContent)">
          <template v-for="(part, index) in fillContentParts">
            <!-- 文本部分 -->
            <span
              v-if="part.type === 'text'"
              :key="'content-text-' + index"
              class="fill-text"
              v-html="sanitizeContent(part.content)"
            ></span>
            <!-- 填空部分 -->
            <span v-else-if="part.type === 'blank' && fillBlanks[part.index]" :key="'content-blank-' + index" class="fill-blank-wrapper">
              <input
                type="text"
                v-model="fillBlanks[part.index].answer"
                :placeholder="'第' + (part.index + 1) + '空'"
                class="fill-blank-input"
                @blur="saveFillAnswer"
              />
            </span>
          </template>
        </div>
        <!-- 如果没有解析到填空，降级为单个文本框 -->
        <div v-if="!hasAnyFillBlanks" class="fill-fallback">
          <textarea
            v-model="currentAnswer"
            placeholder="请输入答案，多个空用英文逗号分隔"
            rows="3"
            @blur="saveCurrentAnswer"
          ></textarea>
        </div>
      </div>

      <!-- 简答题 -->
      <div class="essay-input" v-if="currentQuestion.question.questionType === 'essay'">
        <textarea
          v-model="currentAnswer"
          placeholder="请输入答案"
          rows="6"
          @blur="saveCurrentAnswer"
        ></textarea>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="bottom-nav">
      <button class="nav-btn prev" :disabled="currentQuestionIndex === 0" @click="prevQuestion">
        <i class="el-icon-arrow-left"></i> 上一题
      </button>
      <button class="nav-btn card" @click="showAnswerCard = true">
        <i class="el-icon-menu"></i> 答题卡
      </button>
      <button
        class="nav-btn next"
        v-if="currentQuestionIndex < questions.length - 1"
        @click="nextQuestion"
      >
        下一题 <i class="el-icon-arrow-right"></i>
      </button>
      <button
        class="nav-btn submit"
        v-else
        @click="showSubmitDialog = true"
      >
        交卷 <i class="el-icon-upload2"></i>
      </button>
    </div>

    <!-- 答题卡弹窗 -->
    <div class="answer-card-modal" v-if="showAnswerCard" @click.self="showAnswerCard = false">
      <div class="answer-card-content">
        <div class="card-header">
          <span>答题卡</span>
          <i class="el-icon-close" @click="showAnswerCard = false"></i>
        </div>
        <div class="card-stats">
          <span>已答: <b class="answered">{{ answeredCount }}</b></span>
          <span>未答: <b class="unanswered">{{ questions.length - answeredCount }}</b></span>
        </div>
        <div class="card-grid">
          <div
            v-for="(q, index) in questions"
            :key="q.id"
            class="card-item"
            :class="{
              'answered': isAnswered(index),
              'current': index === currentQuestionIndex
            }"
            @click="jumpToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
        <button class="submit-btn" @click="showSubmitDialog = true; showAnswerCard = false">
          提交试卷
        </button>
      </div>
    </div>

    <!-- 提交确认弹窗 -->
    <div class="submit-modal" v-if="showSubmitDialog" @click.self="showSubmitDialog = false">
      <div class="submit-content">
        <div class="submit-icon">
          <i class="el-icon-question"></i>
        </div>
        <h3>确认提交？</h3>
        <p>已答 <b>{{ answeredCount }}</b> 题，未答 <b>{{ questions.length - answeredCount }}</b> 题</p>
        <p class="warning" v-if="questions.length - answeredCount > 0">
          <i class="el-icon-warning"></i> 还有未答题目，确认提交吗？
        </p>
        <div class="submit-actions">
          <button class="cancel-btn" @click="showSubmitDialog = false">继续答题</button>
          <button class="confirm-btn" @click="handleSubmit" :disabled="submitting">
            {{ submitting ? '提交中...' : '确认交卷' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getExamPaper, saveAnswer, submitExam } from '@/api/mobile/exam'
import QuestionMedia from '@/components/QuestionMedia'
import { sanitizeRichText } from '@/utils/sanitize'

export default {
  name: 'H5ExamTaking',
  components: {
    QuestionMedia
  },
  data() {
    return {
      loading: false,
      examId: null,
      examUserId: null,
      examInfo: {},
      questions: [],
      answers: {},
      currentQuestionIndex: 0,
      currentAnswer: '',

      // 时间相关
      remainingMinutes: 0,
      remainingSeconds: 0,
      startTime: null,
      examDuration: 0,
      timer: null,

      // 弹窗
      showAnswerCard: false,
      showSubmitDialog: false,
      submitting: false,

      // 题型映射
      questionTypeMap: {
        'single': '单选题',
        'single_choice': '单选题',
        'multiple': '多选题',
        'multiple_choice': '多选题',
        'judge': '判断题',
        'true_false': '判断题',
        'fill': '填空题',
        'fill_blank': '填空题',
        'blank': '填空题',
        '3': '填空题',
        'essay': '简答题'
      },

      // 填空题答案数组
      fillBlanks: []
    }
  },
  computed: {
    currentQuestion() {
      return this.questions[this.currentQuestionIndex]
    },
    timeRemaining() {
      const hours = Math.floor(this.remainingMinutes / 60)
      const minutes = this.remainingMinutes % 60
      const seconds = this.remainingSeconds
      if (hours > 0) {
        return `${hours}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
      }
      return `${minutes}:${String(seconds).padStart(2, '0')}`
    },
    answeredCount() {
      return Object.keys(this.answers).filter(key => {
        const answer = this.answers[key]
        return answer !== undefined && answer !== null && answer !== ''
      }).length
    },
    isChoiceQuestion() {
      if (!this.currentQuestion) return false
      return ['single', 'single_choice', 'multiple', 'multiple_choice'].includes(this.currentQuestion.question.questionType)
    },
    fillTitleParts() {
      const question = this.currentQuestion && this.currentQuestion.question ? this.currentQuestion.question : {}
      return this.getFillParts(question.questionTitle || '', 0)
    },
    fillContentParts() {
      const question = this.currentQuestion && this.currentQuestion.question ? this.currentQuestion.question : {}
      const titleBlankCount = this.countFillBlanks(question.questionTitle || '')
      return this.getFillParts(question.questionContent || '', titleBlankCount)
    },
    hasAnyFillBlanks() {
      if (!this.currentQuestion || !this.isFillQuestion(this.currentQuestion.question.questionType)) {
        return false
      }
      const question = this.currentQuestion.question
      return this.countFillBlanks(question.questionTitle) + this.countFillBlanks(question.questionContent) > 0
    },
    /** 解析填空题内容，分离文本和空格 */
    fillQuestionParts() {
      if (!this.currentQuestion ||
          !this.isFillQuestion(this.currentQuestion.question.questionType)) {
        return []
      }
      return [
        ...this.fillTitleParts,
        ...this.fillContentParts
      ]
    }
  },
  created() {
    this.examId = this.$route.params.examId
    this.examUserId = this.$route.query.examUserId
    this.initExam()
  },
  beforeDestroy() {
    this.clearTimer()
    this.saveCurrentAnswer()
  },
  methods: {
    async initExam() {
      this.loading = true
      try {
        const response = await getExamPaper(this.examId, this.examUserId)
        const data = response.data || response

        if (!data || !data.examination) {
          throw new Error('考试数据格式错误')
        }

        this.examInfo = data.examination
        this.questions = data.questions || []
        this.examDuration = data.examination.duration

        if (data.examUser && data.examUser.id) {
          this.examUserId = data.examUser.id
        }

        if (data.examUser && data.examUser.startTime) {
          this.startTime = new Date(data.examUser.startTime)
        } else {
          this.startTime = new Date()
        }

        // 初始化答案
        if (data.answers && data.answers.length > 0) {
          data.answers.forEach(answer => {
            const questionId = answer.questionId || answer.id
            if (questionId && answer.userAnswer) {
              this.$set(this.answers, questionId, answer.userAnswer)
            }
          })
        }

        // 处理题目选项
        this.questions.forEach(question => {
          if (question.question.questionOptions) {
            try {
              question.question.optionList = JSON.parse(question.question.questionOptions)
            } catch (e) {
              question.question.optionList = []
            }
          }
        })

        this.setCurrentAnswer()
        this.startTimer()
      } catch (error) {
        console.error('获取试卷失败', error)
        this.$message.error('获取试卷失败')
        this.$router.back()
      } finally {
        this.loading = false
      }
    },

    startTimer() {
      this.updateRemainingTime()
      this.timer = setInterval(() => {
        this.updateRemainingTime()
      }, 1000)
    },

    updateRemainingTime() {
      const now = new Date()
      const elapsedMs = now.getTime() - this.startTime.getTime()
      const totalSeconds = this.examDuration * 60
      const usedSeconds = Math.floor(elapsedMs / 1000)
      const remainingTotalSeconds = Math.max(0, totalSeconds - usedSeconds)

      this.remainingMinutes = Math.floor(remainingTotalSeconds / 60)
      this.remainingSeconds = remainingTotalSeconds % 60

      if (remainingTotalSeconds <= 0) {
        this.clearTimer()
        this.$message.warning('考试时间已到，系统将自动提交')
        this.handleSubmit()
      }
    },

    clearTimer() {
      if (this.timer) {
        clearInterval(this.timer)
        this.timer = null
      }
    },

    getQuestionTypeName(type) {
      return this.questionTypeMap[type] || '未知'
    },

    isFillQuestion(type) {
      return ['fill', 'fill_blank', 'blank', '3'].includes(String(type))
    },

    getBlankMatches(content) {
      if (!content) {
        return []
      }
      return String(content).match(/\[空\]|\{\{空\}\}|_{3,}/g) || []
    },

    countFillBlanks(content) {
      return this.getBlankMatches(content).length
    },

    hasFillBlanks(content) {
      return this.countFillBlanks(content) > 0
    },

    getFillParts(content, startIndex = 0) {
      if (!content) {
        return []
      }

      const text = String(content)
      const parts = []
      const blankPattern = /\[空\]|\{\{空\}\}|_{3,}/g
      let lastIndex = 0
      let blankIndex = startIndex
      let match

      while ((match = blankPattern.exec(text)) !== null) {
        if (match.index > lastIndex) {
          const textContent = text.substring(lastIndex, match.index)
          if (textContent) {
            parts.push({ type: 'text', content: textContent })
          }
        }

        parts.push({ type: 'blank', index: blankIndex })
        blankIndex++
        lastIndex = match.index + match[0].length
      }

      if (lastIndex < text.length) {
        const textContent = text.substring(lastIndex)
        if (textContent) {
          parts.push({ type: 'text', content: textContent })
        }
      }

      return parts
    },

    formatQuestionContent(content) {
      if (!content) return ''
      return sanitizeRichText(content.replace(/\[空\]|\{\{空\}\}|_{3,}/g, '___'))
    },

    sanitizeContent(content) {
      return sanitizeRichText(content)
    },

    setCurrentAnswer() {
      if (!this.currentQuestion) return
      const questionId = this.currentQuestion.questionId || this.currentQuestion.question.id
      const answer = this.answers[questionId]
      const questionType = this.currentQuestion.question.questionType

      if (answer !== undefined && answer !== null && answer !== '') {
        if (questionType === 'multiple' || questionType === 'multiple_choice') {
          this.currentAnswer = answer.split(',')
        } else if (this.isFillQuestion(questionType)) {
          this.initializeFillBlanks(answer)
          this.currentAnswer = answer
        } else {
          this.currentAnswer = answer
        }
      } else {
        if (questionType === 'multiple' || questionType === 'multiple_choice') {
          this.currentAnswer = []
        } else if (this.isFillQuestion(questionType)) {
          this.initializeFillBlanks('')
          this.currentAnswer = ''
        } else {
          this.currentAnswer = ''
        }
      }
    },

    parseFillAnswer(savedAnswer, blankCount) {
      if (!savedAnswer) return []
      try {
        const parsed = JSON.parse(savedAnswer)
        if (Array.isArray(parsed)) {
          return parsed.map(item => item == null ? '' : String(item))
        }
      } catch (e) {
        // 兼容旧版本逗号分隔答案
      }
      return blankCount > 1 ? String(savedAnswer).split(',') : [String(savedAnswer)]
    },

    initializeFillBlanks(savedAnswer) {
      if (!this.currentQuestion || !this.isFillQuestion(this.currentQuestion.question.questionType)) return
      const question = this.currentQuestion.question
      const blankCount = this.countFillBlanks(question.questionTitle) + this.countFillBlanks(question.questionContent)
      const answerParts = this.parseFillAnswer(savedAnswer, blankCount)
      const blanks = []
      for (let i = 0; i < blankCount; i++) {
        blanks.push({
          index: i,
          answer: answerParts[i] || ''
        })
      }
      this.$set(this, 'fillBlanks', blanks)
    },

    /** 保存填空题答案 */
    async saveFillAnswer() {
      if (!this.currentQuestion || !this.isFillQuestion(this.currentQuestion.question.questionType)) return
      if (!this.hasAnyFillBlanks) {
        await this.saveCurrentAnswer()
        return
      }

      const questionId = this.currentQuestion.questionId || this.currentQuestion.question.id

      const answerStr = JSON.stringify(this.fillBlanks.map(blank => blank.answer || ''))

      this.$set(this.answers, questionId, answerStr)

      try {
        await saveAnswer({
          examId: this.examId,
          examUserId: this.examUserId,
          questionId: questionId,
          userAnswer: answerStr
        })
      } catch (error) {
        console.error('保存填空题答案失败', error)
      }
    },

    isOptionSelected(optionKey) {
      if (Array.isArray(this.currentAnswer)) {
        return this.currentAnswer.includes(optionKey)
      }
      return this.currentAnswer === optionKey
    },

    selectOption(optionKey) {
      const questionType = this.currentQuestion.question.questionType

      if (questionType === 'multiple' || questionType === 'multiple_choice') {
        if (!Array.isArray(this.currentAnswer)) {
          this.currentAnswer = []
        }
        const index = this.currentAnswer.indexOf(optionKey)
        if (index > -1) {
          this.currentAnswer.splice(index, 1)
        } else {
          this.currentAnswer.push(optionKey)
        }
        this.currentAnswer.sort()
      } else {
        this.currentAnswer = optionKey
      }

      this.saveCurrentAnswer()
    },

    selectJudge(value) {
      this.currentAnswer = value
      this.saveCurrentAnswer()
    },

    async saveCurrentAnswer() {
      if (!this.currentQuestion) return

      const questionId = this.currentQuestion.questionId || this.currentQuestion.question.id
      let answerStr = ''

      if (this.isFillQuestion(this.currentQuestion.question.questionType) && this.hasAnyFillBlanks) {
        answerStr = JSON.stringify(this.fillBlanks.map(blank => blank.answer || ''))
        this.currentAnswer = answerStr
      } else if (Array.isArray(this.currentAnswer)) {
        answerStr = this.currentAnswer.join(',')
      } else {
        answerStr = this.currentAnswer === undefined || this.currentAnswer === null ? '' : String(this.currentAnswer)
      }

      if (answerStr === undefined || answerStr === null || answerStr === '') return

      this.$set(this.answers, questionId, answerStr)

      try {
        await saveAnswer({
          examId: this.examId,
          examUserId: this.examUserId,
          questionId: questionId,
          userAnswer: answerStr
        })
      } catch (error) {
        console.error('保存答案失败', error)
      }
    },

    prevQuestion() {
      if (this.currentQuestionIndex > 0) {
        this.saveCurrentAnswer()
        this.currentQuestionIndex--
        this.setCurrentAnswer()
      }
    },

    nextQuestion() {
      if (this.currentQuestionIndex < this.questions.length - 1) {
        this.saveCurrentAnswer()
        this.currentQuestionIndex++
        this.setCurrentAnswer()
      }
    },

    jumpToQuestion(index) {
      this.saveCurrentAnswer()
      this.currentQuestionIndex = index
      this.setCurrentAnswer()
      this.showAnswerCard = false
    },

    isAnswered(index) {
      const question = this.questions[index]
      const questionId = question.questionId || question.question.id
      const answer = this.answers[questionId]
      return answer !== undefined && answer !== null && answer !== ''
    },

    async handleSubmit() {
      this.submitting = true
      this.saveCurrentAnswer()

      try {
        const res = await submitExam(this.examId, { examUserId: this.examUserId })
        if (res.code === 200) {
          this.$message.success('提交成功')
          this.clearTimer()
          this.$router.replace({
            path: '/h5/exam/' + this.examId + '/result',
            query: { examUserId: this.examUserId }
          })
        } else {
          this.$message.error(res.msg || '提交失败')
        }
      } catch (error) {
        console.error('提交失败', error)
        this.$message.error('提交失败')
      } finally {
        this.submitting = false
        this.showSubmitDialog = false
      }
    }
  }
}
</script>

<style scoped>
.h5-exam-taking {
  min-height: 100vh;
  background: #f5f5f5;
  display: flex;
  flex-direction: column;
}

.exam-status-bar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background: linear-gradient(135deg, #409eff 0%, #2593fc 100%);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 15px;
  z-index: 100;
}

.exam-title {
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}

.exam-timer {
  background: rgba(255, 255, 255, 0.2);
  padding: 6px 12px;
  border-radius: 16px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
}

.exam-timer.warning {
  background: #f56c6c;
  animation: pulse 1s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.question-container {
  flex: 1;
  padding: 60px 15px 80px;
}

.question-header {
  background: #fff;
  padding: 12px 15px;
  border-radius: 10px;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 10px;
}

.question-index {
  font-size: 14px;
  color: #606266;
}

.question-score {
  font-size: 14px;
  color: #409eff;
}

.question-type {
  background: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.question-content {
  background: #fff;
  padding: 15px;
  border-radius: 10px;
  margin-bottom: 12px;
}

.question-title {
  font-size: 16px;
  color: #303133;
  line-height: 1.6;
}

.question-detail {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.options-list {
  background: #fff;
  border-radius: 10px;
  overflow: hidden;
}

.option-item {
  display: flex;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.2s;
}

.option-item:last-child {
  border-bottom: none;
}

.option-item:active {
  background: #f5f7fa;
}

.option-item.selected {
  background: #ecf5ff;
}

.option-key {
  width: 28px;
  height: 28px;
  border: 2px solid #dcdfe6;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #606266;
  margin-right: 12px;
  flex-shrink: 0;
}

.option-item.selected .option-key {
  background: #409eff;
  border-color: #409eff;
  color: #fff;
}

.option-item.multiple .option-key {
  border-radius: 4px;
}

.option-value {
  flex: 1;
  font-size: 15px;
  color: #303133;
}

.option-item .el-icon-check {
  color: #409eff;
  font-size: 18px;
}

/* 填空题样式 */
.fill-question {
  background: #fff;
  border-radius: 10px;
  padding: 15px;
}

.fill-content {
  font-size: 15px;
  color: #303133;
  line-height: 2.2;
  word-wrap: break-word;
}

.fill-text {
  vertical-align: middle;
}

.fill-blank-wrapper {
  display: inline-block;
  vertical-align: middle;
  margin: 2px 4px;
}

.fill-blank-input {
  width: 90px;
  height: 32px;
  padding: 0 8px;
  border: 1px solid #dcdfe6;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
  background: #f5f7fa;
  transition: all 0.2s;
}

.fill-blank-input:focus {
  border-color: #409eff;
  background: #fff;
  outline: none;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.fill-blank-input::placeholder {
  color: #c0c4cc;
  font-size: 12px;
}

.fill-fallback textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 15px;
  resize: none;
  box-sizing: border-box;
}

.fill-fallback textarea:focus {
  border-color: #409eff;
  outline: none;
}

.essay-input textarea {
  width: 100%;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  font-size: 15px;
  resize: none;
  box-sizing: border-box;
}

.essay-input textarea:focus {
  border-color: #409eff;
  outline: none;
}

.bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #fff;
  display: flex;
  align-items: center;
  padding: 0 10px;
  gap: 10px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.08);
}

.nav-btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.nav-btn.prev,
.nav-btn.next {
  background: #f5f7fa;
  color: #606266;
}

.nav-btn.card {
  background: #fdf6ec;
  color: #e6a23c;
}

.nav-btn.submit {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.nav-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.nav-btn i {
  margin: 0 5px;
}

/* 答题卡弹窗 */
.answer-card-modal,
.submit-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: flex-end;
  z-index: 200;
}

.answer-card-content {
  width: 100%;
  background: #fff;
  border-radius: 16px 16px 0 0;
  padding: 20px;
  max-height: 70vh;
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.card-header span {
  font-size: 17px;
  font-weight: 600;
}

.card-header i {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
}

.card-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  font-size: 14px;
  color: #606266;
}

.card-stats .answered {
  color: #67c23a;
}

.card-stats .unanswered {
  color: #f56c6c;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 10px;
  margin-bottom: 20px;
}

.card-item {
  width: 100%;
  aspect-ratio: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  border-radius: 8px;
  font-size: 14px;
  color: #606266;
  cursor: pointer;
}

.card-item.answered {
  background: #e6f7e6;
  color: #67c23a;
}

.card-item.current {
  background: #409eff;
  color: #fff;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
}

/* 提交确认弹窗 */
.submit-content {
  width: 85%;
  max-width: 320px;
  background: #fff;
  border-radius: 16px;
  padding: 30px 20px;
  text-align: center;
  margin: auto;
}

.submit-icon {
  width: 60px;
  height: 60px;
  background: #fdf6ec;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
}

.submit-icon i {
  font-size: 30px;
  color: #e6a23c;
}

.submit-content h3 {
  font-size: 18px;
  color: #303133;
  margin-bottom: 10px;
}

.submit-content p {
  font-size: 14px;
  color: #606266;
}

.submit-content p.warning {
  color: #e6a23c;
  margin-top: 10px;
}

.submit-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.cancel-btn,
.confirm-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  cursor: pointer;
}

.cancel-btn {
  background: #f5f7fa;
  color: #606266;
}

.confirm-btn {
  background: linear-gradient(135deg, #67c23a 0%, #85ce61 100%);
  color: #fff;
}

.confirm-btn:disabled {
  opacity: 0.7;
}
</style>
