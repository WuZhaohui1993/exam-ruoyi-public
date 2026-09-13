<template>
  <div class="component-upload-image">
    <el-upload
      multiple
      :disabled="disabled || isUploading"
      :action="uploadImgUrl"
      list-type="picture-card"
      :on-success="handleUploadSuccess"
      :before-upload="handleBeforeUpload"
      :data="data"
      :limit="limit"
      :on-error="handleUploadError"
      :on-exceed="handleExceed"
      :on-progress="handleUploadProgress"
      ref="imageUpload"
      :on-remove="handleDelete"
      :show-file-list="true"
      :headers="headers"
      :file-list="fileList"
      :on-preview="handlePictureCardPreview"
      :class="{hide: this.fileList.length >= this.limit}"
    >
      <i class="el-icon-plus"></i>
    </el-upload>

    <!-- 上传提示 -->
    <div class="el-upload__tip" slot="tip" v-if="showTip && !disabled">
      请上传
      <template v-if="fileSize"> 大小不超过 <b style="color: #f56c6c">{{ fileSize }}MB</b> </template>
      <template v-if="fileType"> 格式为 <b style="color: #f56c6c">{{ fileType.join("/") }}</b> </template>
      的文件
    </div>

    <div class="image-upload-progress" v-if="uploadProgress.visible && !disabled">
      <div class="image-upload-progress__meta">
        <span class="image-upload-progress__name">{{ uploadProgress.fileName || '图片上传中' }}</span>
        <span class="image-upload-progress__size">{{ uploadProgress.loadedText }}</span>
      </div>
      <el-progress :percentage="uploadProgress.percentage" :status="uploadProgress.status"></el-progress>
      <div class="image-upload-progress__hint">{{ uploadProgress.statusText }}</div>
    </div>

    <el-dialog
      :visible.sync="dialogVisible"
      title="预览"
      width="800"
      append-to-body
    >
      <img
        :src="dialogImageUrl"
        style="display: block; max-width: 100%; margin: 0 auto"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getToken } from "@/utils/auth"
import { isExternal } from "@/utils/validate"
import Sortable from 'sortablejs'

export default {
  props: {
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
    // 图片数量限制
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
      default: () => ["png", "jpg", "jpeg"]
    },
    // 是否显示提示
    isShowTip: {
      type: Boolean,
      default: true
    },
    // 禁用组件（仅查看图片）
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
      dialogImageUrl: "",
      dialogVisible: false,
      hideUpload: false,
      baseUrl: process.env.VUE_APP_BASE_API,
      uploadImgUrl: process.env.VUE_APP_BASE_API + this.action, // 上传的图片服务器地址
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
        const element = this.$refs.imageUpload?.$el?.querySelector('.el-upload-list')
        Sortable.create(element, {
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
          // 首先将值转为数组
          const list = Array.isArray(val) ? val : this.value.split(',')
          // 然后将数组转为对象数组
          this.fileList = list.map(item => {
            if (typeof item === "string") {
              if (item.indexOf(this.baseUrl) === -1 && !isExternal(item)) {
                  item = { name: this.baseUrl + item, url: this.baseUrl + item }
              } else {
                  item = { name: item, url: item }
              }
            }
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
    // 上传前loading加载
    handleBeforeUpload(file) {
      let isImg = false
      if (this.fileType.length) {
        let fileExtension = ""
        if (file.name.lastIndexOf(".") > -1) {
          fileExtension = file.name.slice(file.name.lastIndexOf(".") + 1)
        }
        isImg = this.fileType.some(type => {
          if (file.type.indexOf(type) > -1) return true
          if (fileExtension && fileExtension.indexOf(type) > -1) return true
          return false
        })
      } else {
        isImg = file.type.indexOf("image") > -1
      }

      if (!isImg) {
        this.$modal.msgError(`文件格式不正确，请上传${this.fileType.join("/")}图片格式文件!`)
        return false
      }
      if (file.name.includes(',')) {
        this.$modal.msgError('文件名不正确，不能包含英文逗号!')
        return false
      }
      if (this.fileSize) {
        const isLt = file.size / 1024 / 1024 <= this.fileSize
        if (!isLt) {
          this.$modal.msgError(`上传头像图片大小不能超过 ${this.fileSize} MB!`)
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
    // 上传成功回调
    handleUploadSuccess(res, file) {
      if (res.code === 200) {
        this.uploadList.push({ name: res.fileName, url: res.fileName })
        this.uploadedSuccessfully()
      } else {
        this.number--
        this.$modal.msgError(res.msg)
        this.$refs.imageUpload.handleRemove(file)
        this.finishUploadProgress(false, res.msg || "上传失败，请重试")
        this.uploadedSuccessfully()
      }
    },
    // 删除图片
    handleDelete(file) {
      const findex = this.fileList.map(f => f.name).indexOf(file.name)
      if (findex > -1) {
        this.fileList.splice(findex, 1)
        this.$emit("input", this.listToString(this.fileList))
      }
    },
    // 上传失败
    handleUploadError(err, file) {
      this.number = Math.max(this.number - 1, 0)
      this.$modal.msgError("上传图片失败，请重试")
      if (file && this.$refs.imageUpload) {
        this.$refs.imageUpload.handleRemove(file)
      }
      this.finishUploadProgress(false, "上传失败，请重试")
    },
    // 上传进度
    handleUploadProgress(event, file) {
      this.updateUploadProgress(event, file)
    },
    // 上传结束处理
    uploadedSuccessfully() {
      if (this.number > 0 && this.uploadList.length === this.number) {
        this.fileList = this.fileList.concat(this.uploadList)
        this.uploadList = []
        this.number = 0
        this.$emit("input", this.listToString(this.fileList))
        this.finishUploadProgress(true, "上传完成，图片已加入列表")
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
      this.uploadProgress.statusText = percentage >= 99 ? "图片已上传，正在等待服务器处理" : "正在上传，请勿关闭页面"
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
    // 预览
    handlePictureCardPreview(file) {
      this.dialogImageUrl = file.url
      this.dialogVisible = true
    },
    // 对象转成指定字符串分隔
    listToString(list, separator) {
      let strs = ""
      separator = separator || ","
      for (let i in list) {
        if (list[i].url) {
          strs += list[i].url.replace(this.baseUrl, "") + separator
        }
      }
      return strs != '' ? strs.substr(0, strs.length - 1) : ''
    }
  }
}
</script>
<style scoped lang="scss">
// .el-upload--picture-card 控制加号部分
::v-deep.hide .el-upload--picture-card {
  display: none;
}

::v-deep .el-upload-list--picture-card.is-disabled + .el-upload--picture-card {
  display: none !important;
}

.image-upload-progress {
  max-width: 420px;
  margin: 8px 0 10px;
  padding: 10px 12px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  background: #f8fafc;
}

.image-upload-progress__meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
  color: #606266;
  font-size: 12px;
}

.image-upload-progress__name {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.image-upload-progress__size {
  flex: none;
  color: #909399;
}

.image-upload-progress__hint {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

// 去掉动画效果
::v-deep .el-list-enter-active,
::v-deep .el-list-leave-active {
  transition: all 0s;
}

::v-deep .el-list-enter, .el-list-leave-active {
  opacity: 0;
  transform: translateY(0);
}
</style>

