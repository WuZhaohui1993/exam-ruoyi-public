<template>
  <div v-if="visibleMedia.length" class="question-media">
    <div v-if="imageMedia.length" class="media-images">
      <el-image
        v-for="media in imageMedia"
        :key="mediaKey(media)"
        class="media-image"
        :src="resolveUrl(media.fileUrl)"
        :preview-src-list="imagePreviewList"
        fit="cover"
      >
        <div slot="error" class="media-error">
          <i class="el-icon-picture-outline"></i>
        </div>
      </el-image>
    </div>

    <div v-if="videoMedia.length" class="media-videos">
      <video
        v-for="media in videoMedia"
        :key="mediaKey(media)"
        class="media-video"
        :src="resolveUrl(media.fileUrl)"
        controls
        preload="metadata"
      ></video>
    </div>

    <div v-if="fileMedia.length" class="media-files">
      <el-link
        v-for="media in fileMedia"
        :key="mediaKey(media)"
        :href="resolveUrl(media.fileUrl)"
        target="_blank"
        icon="el-icon-document"
        :underline="false"
      >
        {{ media.fileName || getFileName(media.fileUrl) }}
      </el-link>
    </div>
  </div>
</template>

<script>
import { isExternal } from "@/utils/validate"

export default {
  name: "QuestionMedia",
  props: {
    mediaList: {
      type: Array,
      default: () => []
    },
    position: {
      type: String,
      default: "stem"
    }
  },
  computed: {
    visibleMedia() {
      return (this.mediaList || []).filter(media => {
        if (!media || !media.fileUrl) return false
        const position = media.displayPosition || "stem"
        return position === this.position
      }).sort((a, b) => {
        return (a.sortOrder || 0) - (b.sortOrder || 0)
      })
    },
    imageMedia() {
      return this.visibleMedia.filter(media => media.mediaType === "image")
    },
    videoMedia() {
      return this.visibleMedia.filter(media => media.mediaType === "video")
    },
    fileMedia() {
      return this.visibleMedia.filter(media => {
        return media.mediaType !== "image" && media.mediaType !== "video"
      })
    },
    imagePreviewList() {
      return this.imageMedia.map(media => this.resolveUrl(media.fileUrl))
    }
  },
  methods: {
    resolveUrl(url) {
      if (!url) return ""
      if (isExternal(url)) return url
      const baseApi = process.env.VUE_APP_BASE_API || ""
      if (baseApi && url.indexOf(baseApi + "/") === 0) return url
      if (url.indexOf("/") === 0) return baseApi + url
      return baseApi + "/" + url
    },
    getFileName(url) {
      if (!url) return "附件"
      const cleanUrl = url.split("?")[0]
      return cleanUrl.substring(cleanUrl.lastIndexOf("/") + 1) || "附件"
    },
    mediaKey(media) {
      return media.id || `${media.mediaType}-${media.fileUrl}-${media.sortOrder || 0}`
    }
  }
}
</script>

<style scoped>
.question-media {
  margin: 12px 0 18px;
}

.media-images {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 10px;
  max-width: 760px;
}

.media-image {
  width: 100%;
  height: 120px;
  border-radius: 6px;
  border: 1px solid #e4e7ed;
  background: #f5f7fa;
}

.media-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  color: #909399;
  font-size: 24px;
}

.media-videos {
  margin-top: 12px;
}

.media-video {
  display: block;
  width: 100%;
  max-width: 760px;
  max-height: 420px;
  border-radius: 6px;
  background: #000;
}

.media-video + .media-video {
  margin-top: 12px;
}

.media-files {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 12px;
}
</style>
