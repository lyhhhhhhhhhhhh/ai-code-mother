<template>
  <a-modal
    :open="open"
    title="版本差异对比"
    width="80%"
    :footer="null"
    :destroyOnClose="true"
    @update:open="emit('update:open', $event)"
  >
    <div class="version-diff-container">
      <!-- 差异概览 -->
      <div class="diff-overview" v-if="diffData">
        <a-alert
          :message="`发现 ${diffData.diffFileCount} 个文件有差异`"
          type="info"
          show-icon
          style="margin-bottom: 16px"
        />
      </div>

      <!-- 文件列表 -->
      <div class="file-list" v-if="diffData && diffData.diffContentMap">
        <a-tabs v-model:activeKey="activeFile" type="card">
          <a-tab-pane
            v-for="(diffContent, fileName) in diffData.diffContentMap"
            :key="fileName"
            :tab="fileName"
          >
            <div class="diff-content">
              <div class="diff-text">
                <div
                  v-for="(line, index) in parseDiff(diffContent)"
                  :key="index"
                  :class="['diff-line', line.type]"
                >
                  <code>{{ line.text }}</code>
                </div>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <!-- 无差异提示 -->
      <div v-if="diffData && !diffData.hasDiff" class="no-diff">
        <a-empty description="当前版本与上一版本无差异" />
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading">
        <a-spin size="large" />
        <p>正在获取版本差异...</p>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { message } from 'ant-design-vue'
import { getVersionDiff } from '@/api/appController'
import { CodeGenTypeEnum } from '@/utils/codeGenTypes'

interface Props {
  open: boolean
  appId?: number
  codeGenType?: string
}

interface Emits {
  (e: 'update:open', value: boolean): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const loading = ref(false)
const diffData = ref<API.DiffResultVO>()
const activeFile = ref<string>('')

// 监听open变化，当打开时获取差异数据
watch(
  () => props.open,
  async (newOpen) => {
    if (newOpen && props.appId && props.codeGenType) {
      await fetchVersionDiff()
    }
  }
)

// 获取版本差异
const fetchVersionDiff = async () => {
  if (!props.appId || !props.codeGenType) {
    message.error('缺少必要参数')
    return
  }

  loading.value = true
  try {
    const res = await getVersionDiff({
      appId: props.appId,
      codeGenTypeEnum: props.codeGenType,
    })

    if (res.data.code === 0 && res.data.data) {
      diffData.value = res.data.data

      // 设置第一个文件为默认选中
      if (diffData.value.diffContentMap && Object.keys(diffData.value.diffContentMap).length > 0) {
        activeFile.value = Object.keys(diffData.value.diffContentMap)[0]
      }
    } else {
      message.error('获取版本差异失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取版本差异失败：', error)
    message.error('获取版本差异失败')
  } finally {
    loading.value = false
  }
}

// 关闭模态框
const closeModal = () => {
  emit('update:open', false)
  diffData.value = undefined
  activeFile.value = ''
}

const parseDiff = (diffContent: string) => {
  if (!diffContent) return []

  const lines = diffContent.split('\n')
  return lines.map((line) => {
    let type = ''
    if (line.startsWith('+') && !line.startsWith('+++')) {
      type = 'added'
    } else if (line.startsWith('-') && !line.startsWith('---')) {
      type = 'removed'
    } else if (line.startsWith('@@')) {
      type = 'meta'
    } else if (line.startsWith('+++') || line.startsWith('---')) {
      type = 'meta'
    } else {
      type = 'normal'
    }
    return {
      text: line,
      type,
    }
  })
}
</script>

<style scoped>
.version-diff-container {
  max-height: 70vh;
  overflow: hidden;
}

.diff-overview {
  margin-bottom: 16px;
}

.file-list {
  height: 60vh;
  overflow: hidden;
}

.diff-content {
  height: 50vh;
  overflow: auto;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  background: #fafafa;
}

.diff-text {
  margin: 0;
  padding: 16px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-wrap: break-word;
  color: #333;
}

.no-diff {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.loading {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 200px;
}

.loading p {
  margin-top: 16px;
  color: #666;
}

/* 自定义滚动条样式 */
.diff-content::-webkit-scrollbar {
  width: 8px;
}

.diff-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

.diff-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}

.diff-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.diff-line {
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 12px;
  line-height: 1.5;
  white-space: pre-wrap;
  padding: 2px 8px;
  border-left: 4px solid transparent;
}

.diff-line.added {
  background-color: #e6ffed;
  color: #22863a;
  border-left-color: #34d058;
}

.diff-line.removed {
  background-color: #ffeef0;
  color: #b31d28;
  border-left-color: #f97583;
}

.diff-line.meta {
  background-color: #f6f8fa;
  color: #6a737d;
  font-style: italic;
}

.diff-line.normal {
  color: #24292e;
}
</style>
