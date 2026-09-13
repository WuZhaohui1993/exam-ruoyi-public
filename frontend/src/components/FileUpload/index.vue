<template>
  <div class="upload-file">
    <el-upload
      multiple
      :action="uploadFileUrl"
      :before-upload="handleBeforeUpload"
      :file-list="fileList"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-progress="handleUploadProgress"
      :on-success="handleUploadSuccess"
      :show-file-list="false"
      :headers="headers"
      :disabled="isUploading"
      class="upload-file-uploader"
      ref="fileUpload"
      v-if="!disabled"
    >
      <!-- 上传按钮 -->
      <el-button size="mini" type="primary" :loading="isUploading" :disabled="isUploading">选取文件</el-button>
      <!-- 上传提示 -->
      <div class="el-upload__tip" slot="tip" v-if="showTip">
        请上传
        <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
        <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
        的文件
      </div>
    </el-upload>

    <div class="upload-progress" v-if="uploadProgress.visible && !disabled">
      <div class="upload-progress__meta">
        <span class="upload-progress__name">{{ uploadProgress.fileName || '文件上传中' }}</span>
        <span class="upload-progress__size">{{ uploadProgress.loadedText }}</span>
      </div>
      <el-progress :percentage="uploadProgress.percentage" :status="uploadProgress.status"></el-progress>
      <div class="upload-progress__hint">{{ uploadProgress.statusText }}</div>
    </div>

    <!-- 文件列表 -->
    <transition-group ref="uploadFileList" class="upload-file-list el-upload-list el-upload-list--text" name="el-fade-in-linear" tag="ul">
      <li :key="file.url" class="el-upload-list__item ele-upload-list__item-content" v-for="(file, index) in fileList">
        <el-link :href="`${baseUrl}${file.url}`" :underline="false" target="_blank">
          <span class="el-icon-document"> {{ getFileName(file.name) }} </span>
        </el-link>
        <div class="ele-upload-list__item-content-action">
          <el-link :underline="false" @click="handleDelete(index)" type="danger" v-if="!disabled">删除</el-link>
        </div>
      </li>
    </transition-group>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth"
import Sortable from 'sortablejs'

export default {
  name: "FileUpload",
  props: {
    // 值
    value: [String, Object, Array],
    // 上传接口地址
    action: {
      type: String,
      default: "/common/upload"
    },
    // 上传携带的参数
    data: {
      type: Object
    },
    // 数量限制
    limit: {
      type: Number,
      default: 5
    },
    // 大小限制(MB)
    fileSize: {
      type: Number,
      default: 5
    },
    // 文件类型, 例如['png', 'jpg', 'jpeg']
    fileType: {
      type: Array,
      default: () => ["doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "pdf"]
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    // 禁用组件（仅查看文件）
    disabled: {
      type: Boolean,
      default: false
    },
    // 拖动排序
    drag: {
      type: Boolean,
      default: true
    }
  },
  data() {
    return {
      number: 0,
      uploadList: [],
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadFileUrl: process.env.VUE_APP_BASE_API + this.action, // 上传文件服务器地址
      headers: {
        Authorization: "Bearer " + getToken(),
      },
      fileList: [],
      isUploading: false,
      uploadProgressTimer: null,
      uploadProgress: {
        visible: false,
        fileName: "",
        percentage: 0,
        loadedText: "",
        statusText: "",
        status: null
      }
    }
  },
  beforeDestroy() {
    clearTimeout(this.uploadProgressTimer)
  },
  mounted() {
    if (this.drag && !this.disabled) {
      this.$nextTick(() => {
        const element = this.$refs.uploadFileList?.$el || this.$refs.uploadFileList
        Sortable.create(element, {
          ghostClass: 'file-upload-darg',
          onEnd: (evt) => {
            const movedItem = this.fileList.splice(evt.oldIndex, 1)[0]
            this.fileList.splice(evt.newIndex, 0, movedItem)
            this.$emit("input", this.listToString(this.fileList))
          }
        })
      })
    }
  },
  watch: {
    value: {
      handler(val) {
        if (val) {
          let temp = 1
          // 首先将值转为数组
          const list = Array.isArray(val) ? val : this.value.split(',')
          // 然后将数组转为对象数组
          this.fileList = list.map(item => {
            if (typeof item === "string") {
              item = { name: item, url: item }
            }
            item.uid = item.uid || new Date().getTime() + temp++
            return item
          })
        } else {
          this.fileList = []
          return []
        }
      },
      deep: true,
      immediate: true
    }
  },
  computed: {
    // 是否显示提示
    showTip() {
      return this.isShowTip && (this.fileType || this.fileSize)
    },
  },
  methods: {
    // 上传前校检格式和大小
    handleBeforeUpload(file) {
      // 校检文件类型
      if (this.fileType) {
        const fileName = file.name.split('.')
        const fileExt = fileName[fileName.length - 1].toLowerCase()
        const isTypeOk = this.fileType.indexOf(fileExt) >= 0
        if (!isTypeOk) {
          this.$modal.msgError(`文件格式不正确，请上传${this.fileType.join("/")}格式文件!`)
          return false
        }
      }
      // 校检文件名是否包含特殊字符
      if (file.name.includes(',')) {
        this.$modal.msgError('文件名不正确，不能包含英文逗号!')
        return false
      }
      // 校检文件大小
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 <= this.fileSize
        if (!isLt) {
          this.$modal.msgError(`上传文件大小不能超过 ${this.fileSize} MB!`)
          return false
        }
      }
      this.number++
      this.startUploadProgress(file)
      return true
    },
    // 文件个数超出
    handleExceed() {
      this.$modal.msgError(`上传文件数量不能超过 ${this.limit} 个!`)
    },
    // 上传失败
    handleUploadError(err, file) {
      this.number = Math.max(this.number - 1, 0)
      this.$modal.msgError("上传文件失败，请重试")
      if (file && this.$refs.fileUpload) {
        this.$refs.fileUpload.handleRemove(file)
      }
      this.finishUploadProgress(false, "上传失败，请重试")
    },
    // 上传进度
    handleUploadProgress(event, file) {
      this.updateUploadProgress(event, file)
    },
    // 上传成功回调
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        this.uploadList.push({ name: res.fileName, url: res.fileName })
        this.uploadedSuccessfully()
      } else {
        this.number--
        this.$modal.msgError(res.msg)
        this.$refs.fileUpload.handleRemove(file)
        this.finishUploadProgress(false, res.msg || "上传失败，请重试")
        this.uploadedSuccessfully()
      }
    },
    // 删除文件
    handleDelete(index) {
      this.fileList.splice(index, 1)
      this.$emit("input", this.listToString(this.fileList))
    },
    // 上传结束处理
    uploadedSuccessfully() {
      if (this.number > 0 && this.uploadList.length === this.number) {
        this.fileList = this.fileList.concat(this.uploadList)
        this.uploadList = []
        this.number = 0
        this.$emit("input", this.listToString(this.fileList))
        this.finishUploadProgress(true, "上传完成，文件已加入列表")
      }
    },
    startUploadProgress(file) {
      clearTimeout(this.uploadProgressTimer)
      this.isUploading = true
      this.uploadProgress.visible = true
      this.uploadProgress.fileName = file.name
      this.uploadProgress.percentage = 0
      this.uploadProgress.loadedText = `0 B / ${this.formatFileSize(file.size)}`
      this.uploadProgress.statusText = "准备上传，请勿关闭页面"
      this.uploadProgress.status = null
    },
    updateUploadProgress(event, file) {
      const total = event.total || file.size || 0
      const loaded = event.loaded || Math.round(total * (event.percent || 0) / 100)
      const rawPercent = event.percent || (total ? loaded / total * 100 : 0)
      const percentage = Math.min(99, Math.max(1, Math.floor(rawPercent)))
      this.isUploading = true
      this.uploadProgress.visible = true
      this.uploadProgress.fileName = file.name
      this.uploadProgress.percentage = percentage
      this.uploadProgress.loadedText = total
        ? `${this.formatFileSize(loaded)} / ${this.formatFileSize(total)}`
        : this.formatFileSize(loaded)
      this.uploadProgress.statusText = percentage >= 99 ? "文件已上传，正在等待服务器处理" : "正在上传，请勿关闭页面"
      this.uploadProgress.status = null
    },
    finishUploadProgress(success, statusText) {
      clearTimeout(this.uploadProgressTimer)
      this.isUploading = false
      this.uploadProgress.visible = true
      this.uploadProgress.percentage = success ? 100 : this.uploadProgress.percentage
      this.uploadProgress.status = success ? "success" : "exception"
      this.uploadProgress.statusText = statusText
      this.uploadProgressTimer = setTimeout(() => {
        this.uploadProgress.visible = false
        this.uploadProgress.fileName = ""
        this.uploadProgress.percentage = 0
        this.uploadProgress.loadedText = ""
        this.uploadProgress.statusText = ""
        this.uploadProgress.status = null
      }, success ? 1200 : 2500)
    },
    formatFileSize(size) {
      if (!size) {
        return "0 B"
      }
      if (size < 1024) {
        return `${size} B`
      }
      if (size < 1024 * 1024) {
        return `${(size / 1024).toFixed(1)} KB`
      }
      return `${(size / 1024 / 1024).toFixed(1)} MB`
    },
    // 获取文件名称
    getFileName(name) {
      // 如果是url那么取最后的名字 如果不是直接返回
      if (name.lastIndexOf("/") > -1) {
        return name.slice(name.lastIndexOf("/") + 1)
      } else {
        return name
      }
    },
    // 对象转成指定字符串分隔
    listToString(list, separator) {
      let strs = ""
      separator = separator || ","
      for (let i in list) {
        strs += list[i].url + separator
      }
      return strs != '' ? strs.substr(0, strs.length - 1) : ''
    }
  }
}
</script>

<style scoped lang="scss">
.file-upload-darg {
  opacity: 0.5;
  background: #c8ebfb;
}
.upload-file-uploader {
  margin-bottom: 5px;
}
.upload-progress {
  max-width: 420px;
  margin: 8px 0 10px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f8fafc;
}
.upload-progress__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  color: #606266;
  font-size: 12px;
}
.upload-progress__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.upload-progress__size {
  flex: none;
  color: #909399;
}
.upload-progress__hint {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}
.upload-file-list .el-upload-list__item {
  border: 1px solid #e4e7ed;
  line-height: 2;
  margin-bottom: 10px;
  position: relative;
}
.upload-file-list .ele-upload-list__item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: inherit;
}
.ele-upload-list__item-content-action .el-link {
  margin-right: 10px;
}
</style>
