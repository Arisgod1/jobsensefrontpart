<template>
  <div class="resume-assistant" :class="{ 'dark': isDark }">
    <div class="header">
      <h1>简历智能评估</h1>
      <div class="header-buttons">
        <button class="btn secondary" @click="toggleHistory">
          <i class="fas fa-history mr-2"></i> 历史记录
        </button>
        <button class="btn primary" @click="startNewChat">
          <i class="fas fa-plus mr-2"></i> 新对话
        </button>
      </div>
    </div>

    <div class="main-container">
      <!-- 历史记录侧边栏 -->
      <div class="history-sidebar" :class="{ 'active': showHistory }">
        <div class="history-header">
          <h3>历史记录</h3>
          <button class="close-btn" @click="toggleHistory">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="history-list">
          <div 
            v-for="chat in chatHistory" 
            :key="chat.id"
            class="history-item"
            :class="{ 'active': currentChatId === chat.id }"
            @click="loadChat(chat.id)"
          >
            <i class="fas fa-file-alt mr-2"></i>
            <span class="title">{{ chat.title || '简历对话' }}</span>
          </div>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="content-area">
        <!-- 上传区域 -->
        <div v-if="!currentPdfName" class="card upload-card">
          <div class="upload-area"
              @dragover.prevent="handleDragOver"
              @dragleave.prevent="handleDragLeave"
              @drop.prevent="handleDrop"
              @click="triggerFileInput"
              :class="{ 
                'dragging': isDragging,
                'uploading': isUploading 
              }">
            <div class="upload-content">
              <div v-if="isUploading" class="upload-status">
                <div class="spinner"></div>
                <div class="upload-progress">
                  <p class="status-text">正在上传文件...</p>
                  <p class="filename">{{ uploadingFileName }}</p>
                </div>
              </div>
              <template v-else>
                <div class="upload-icon">
                  <i class="fas fa-cloud-upload-alt"></i>
                </div>
                <h3>上传您的简历</h3>
                <p>将简历文件拖放到此处，或点击浏览</p>
                <input 
                  type="file" 
                  ref="fileInput" 
                  style="display: none" 
                  @change="handleFileChange" 
                  accept=".pdf,.doc,.docx"
                >
                <button class="btn primary">选择文件</button>
                <p class="file-info">支持 PDF, DOC, DOCX (最大5MB)</p>
              </template>
            </div>
            <div v-if="errorMessage" class="error-message">
              {{ errorMessage }}
            </div>
          </div>
        </div>

        <!-- 简历评估和聊天区域 -->
        <div v-else>
          <!-- 简历评估结果 -->
          <div class="card assessment-card">
            <div class="section-header">
              <h2>简历分析</h2>
              <div class="status-badge">
                <i class="fas fa-check-circle mr-1"></i> 分析完成
              </div>
            </div>

            <div class="metrics-grid">
              <div class="metric-card">
                <div class="metric-header">
                  <h4>综合评分</h4>
                  <span class="score">78/100</span>
                </div>
                <div class="score-bar">
                  <div class="score-fill" style="width: 78%"></div>
                </div>
                <p class="metric-description">您的简历良好，但在结构和影响力方面还有改进空间。</p>
              </div>

              <div class="metric-card">
                <h4 class="metric-title">关键指标</h4>
                <div class="metrics-list">
                  <div class="metric-item">
                    <div class="metric-label">
                      <span>清晰度</span>
                      <span>65/100</span>
                    </div>
                    <div class="score-bar">
                      <div class="score-fill yellow" style="width: 65%"></div>
                    </div>
                  </div>
                  <div class="metric-item">
                    <div class="metric-label">
                      <span>相关性</span>
                      <span>82/100</span>
                    </div>
                    <div class="score-bar">
                      <div class="score-fill green" style="width: 82%"></div>
                    </div>
                  </div>
                  <div class="metric-item">
                    <div class="metric-label">
                      <span>影响力</span>
                      <span>71/100</span>
                    </div>
                    <div class="score-bar">
                      <div class="score-fill purple" style="width: 71%"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <h2 class="section-title">改进建议</h2>
            <div class="suggestions-grid">
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg blue">
                    <i class="fas fa-bullseye"></i>
                  </div>
                  <h4>量化成就</h4>
                </div>
                <p>为工作经历添加具体指标（例如"销售额提升30%"）</p>
              </div>
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg purple">
                    <i class="fas fa-align-left"></i>
                  </div>
                  <h4>简化语言</h4>
                </div>
                <p>用更清晰、直接的语言替换复杂的表达</p>
              </div>
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg green">
                    <i class="fas fa-sitemap"></i>
                  </div>
                  <h4>优化结构</h4>
                </div>
                <p>重新组织段落，优先展示最相关的经验</p>
              </div>
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg yellow">
                    <i class="fas fa-magic"></i>
                  </div>
                  <h4>ATS优化</h4>
                </div>
                <p>添加更多职位描述中的关键词以通过自动筛选系统</p>
              </div>
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg red">
                    <i class="fas fa-broom"></i>
                  </div>
                  <h4>移除无关信息</h4>
                </div>
                <p>删除与职业目标无关的爱好和过时经历</p>
              </div>
              <div class="suggestion-card">
                <div class="suggestion-header">
                  <div class="icon-bg indigo">
                    <i class="fas fa-graduation-cap"></i>
                  </div>
                  <h4>教育部分</h4>
                </div>
                <p>除非是应届毕业生，否则将教育经历放在较后位置</p>
              </div>
            </div>
          </div>

          <!-- 聊天区域 -->
          <div class="card chat-card">
            <div class="chat-header">
              <h2>简历问答</h2>
            </div>
            
            <div class="messages" ref="messagesRef">
              <div v-for="(message, index) in currentMessages" :key="index" class="message-bubble">
                <div class="message-avatar" :class="message.role">
                  <i :class="message.role === 'user' ? 'fas fa-user' : 'fas fa-robot'"></i>
                </div>
                <div class="message-content">
                  <div v-if="message.isMarkdown" v-html="DOMPurify.sanitize(marked.parse(message.content))"></div>
                  <template v-else>
                    {{ message.content }}
                  </template>
                  <div class="message-time">
                    {{ formatTime(message.timestamp) }}
                  </div>
                </div>
              </div>
            </div>
            
            <div class="input-area">
              <textarea
                v-model="userInput"
                @keydown.enter.prevent="sendMessage()"
                placeholder="请输入您的问题..."
                rows="1"
                ref="inputRef"
              ></textarea>
              <button 
                class="send-button" 
                @click="sendMessage()"
                :disabled="isStreaming || !userInput.trim()"
              >
                <i class="fas fa-paper-plane"></i>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import { useDark } from '@vueuse/core'
import { marked } from 'marked'
import DOMPurify from 'dompurify'
import { chatAPI } from '../services/api'
import { useRouter } from 'vue-router'

const isDark = useDark()
const router = useRouter()
const messagesRef = ref(null)
const inputRef = ref(null)
const fileInput = ref(null)
const userInput = ref('')
const isStreaming = ref(false)
const isUploading = ref(false)
const currentChatId = ref(null)
const currentMessages = ref([])
const chatHistory = ref([])
const currentPdfName = ref('')
const isDragging = ref(false)
const uploadingFileName = ref('')
const errorMessage = ref('')
const showHistory = ref(false)
const BASE_URL = 'http://localhost:8080'

// 配置 marked
marked.setOptions({
  breaks: true,
  gfm: true,
  sanitize: false
})

// 格式时间
const formatTime = (date) => {
  return new Date(date).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
}

// 调整输入框高度
const adjustTextareaHeight = () => {
  const textarea = inputRef.value
  if (textarea) {
    textarea.style.height = 'auto'
    textarea.style.height = textarea.scrollHeight + 'px'
  }
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

// 清理资源
const cleanupResources = () => {
  currentPdfName.value = ''
  currentMessages.value = []
  currentChatId.value = null
  isUploading.value = false
  uploadingFileName.value = ''
  userInput.value = ''
  isStreaming.value = false
  errorMessage.value = ''
  
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
}

// 切换历史记录侧边栏
const toggleHistory = () => {
  showHistory.value = !showHistory.value
}

// 开始新对话
const startNewChat = () => {
  cleanupResources()
  toggleHistory()
}

// 加载历史对话
const loadChat = async (chatId) => {
  if (!chatId) return
  
  cleanupResources()
  currentChatId.value = chatId
  
  try {
    // 加载消息历史
    const messages = await chatAPI.getChatMessages(chatId, 'pdf')
    currentMessages.value = messages.map(msg => ({
      ...msg,
      isMarkdown: msg.role === 'assistant'
    }))

    // 从服务器获取文件名
    const response = await fetch(`${BASE_URL}/ai/pdf/file/${chatId}`)
    if (!response.ok) throw new Error('获取简历信息失败')
    
    const contentDisposition = response.headers.get('content-disposition')
    let filename = 'resume.pdf'
    if (contentDisposition) {
      const matches = contentDisposition.match(/filename=["']?([^"']+)["']?/)
      if (matches && matches[1]) {
        filename = decodeURIComponent(matches[1])
      }
    }
    
    currentPdfName.value = filename
    const chatIndex = chatHistory.value.findIndex(c => c.id === chatId)
    if (chatIndex !== -1) {
      chatHistory.value[chatIndex].title = filename
    }
    
  } catch (error) {
    console.error('加载失败:', error)
    currentMessages.value.push({
      role: 'assistant',
      content: '加载失败，请重试。',
      timestamp: new Date(),
      isMarkdown: true
    })
  }
}

// 加载聊天历史
const loadChatHistory = async () => {
  try {
    const history = await chatAPI.getChatHistory('pdf')
    chatHistory.value = history || []
    if (history && history.length > 0) {
      await loadChat(history[0].id)
    }
  } catch (error) {
    console.error('加载聊天历史失败:', error)
    chatHistory.value = []
  }
}

// 处理拖拽
const handleDragOver = (event) => {
  event.preventDefault()
  isDragging.value = true
}

const handleDragLeave = (event) => {
  event.preventDefault()
  isDragging.value = false
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value.click()
}

// 验证文件
const validateFile = (file) => {
  const validTypes = [
    'application/pdf', 
    'application/msword', 
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ]
  
  const validExtensions = ['.pdf', '.doc', '.docx']
  const maxSize = 5 * 1024 * 1024 // 5MB
  
  const extension = '.' + file.name.split('.').pop().toLowerCase()
  if (!validExtensions.includes(extension)) {
    errorMessage.value = '不支持的文件格式。请上传PDF、DOC或DOCX文件。'
    return false
  }
  
  if (file.size > maxSize) {
    errorMessage.value = '文件太大。请上传小于5MB的文件。'
    return false
  }
  
  return true
}

// 上传文件
const uploadFile = async (file) => {
  if (!validateFile(file)) return
  
  isUploading.value = true
  errorMessage.value = ''
  uploadingFileName.value = file.name
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const uploadChatId = currentChatId.value || `pdf_${Date.now()}`
    
    // 上传简历文件
    const response = await fetch(`${BASE_URL}/ai/pdf/upload/${uploadChatId}`, {
      method: 'POST',
      body: formData
    })
    
    if (!response.ok) {
      throw new Error(`上传失败: ${response.status}`)
    }
    
    const data = await response.json()
    
    currentChatId.value = data.chatId || uploadChatId
    currentPdfName.value = file.name
    
    const newChat = {
      id: currentChatId.value,
      title: `简历对话: ${file.name.slice(0, 15)}${file.name.length > 15 ? '...' : ''}`
    }
    
    if (!chatHistory.value.some(chat => chat.id === currentChatId.value)) {
      chatHistory.value = [newChat, ...chatHistory.value]
    }
    
    currentMessages.value = []
    
    currentMessages.value.push({
      role: 'assistant',
      content: `已上传简历文件: ${file.name}。您可以开始提问了。`,
      timestamp: new Date(),
      isMarkdown: true
    })
    
  } catch (error) {
    console.error('上传失败:', error)
    errorMessage.value = error.message || '文件上传失败，请重试'
  } finally {
    isUploading.value = false
    uploadingFileName.value = ''
  }
}

// 处理文件选择
const handleFileChange = (event) => {
  const file = event.target.files[0]
  if (file) uploadFile(file)
}

// 处理拖放
const handleDrop = async (event) => {
  event.preventDefault()
  isDragging.value = false
  
  const files = event.dataTransfer.files
  if (files.length === 0) return
  
  const file = files[0]
  uploadFile(file)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isStreaming.value) return
  
  const userMessage = {
    role: 'user',
    content: userInput.value,
    timestamp: new Date()
  }
  currentMessages.value.push(userMessage)
  
  const input = userInput.value
  userInput.value = ''
  if (inputRef.value) {
    inputRef.value.style.height = 'auto'
  }
  
  await scrollToBottom()
  
  const assistantMessageIndex = currentMessages.value.length
  currentMessages.value.push({
    role: 'assistant',
    content: '',
    timestamp: new Date(),
    isMarkdown: true
  })
  
  try {
    isStreaming.value = true
    
    // 发送请求到服务器
    const reader = await chatAPI.sendPdfMessage(input, currentChatId.value)
    const decoder = new TextDecoder()
    let result = ''
    
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      
      const chunk = decoder.decode(value, { stream: true })
      result += chunk
      
      currentMessages.value[assistantMessageIndex] = {
        role: 'assistant',
        content: result,
        timestamp: new Date(),
        isMarkdown: true
      }
      
      await nextTick()
      await scrollToBottom()
    }
    
  } catch (error) {
    console.error('发送消息失败:', error)
    currentMessages.value[assistantMessageIndex] = {
      role: 'assistant',
      content: '发送消息失败，请重试。',
      timestamp: new Date(),
      isMarkdown: true
    }
  } finally {
    isStreaming.value = false
    await scrollToBottom()
  }
}

// 监听输入变化
watch(userInput, () => {
  adjustTextareaHeight()
})

// 初始化
onMounted(() => {
  loadChatHistory()
  adjustTextareaHeight()
})
</script>

<style scoped lang="scss">
.resume-assistant {
  position: relative;
  min-height: 100vh;
  background-color: #f9fafb;
  font-family: 'Inter', 'Segoe UI', system-ui, sans-serif;
  color: #333;
  padding-bottom: 2rem;
  
  .header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1.5rem 2rem;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    
    h1 {
      font-size: 1.75rem;
      font-weight: 600;
      color: #1f2937;
      margin: 0;
    }
    
    .header-buttons {
      display: flex;
      gap: 1rem;
    }
  }
  
  .main-container {
    display: flex;
    max-width: 1400px;
    margin: 0 auto;
    padding: 0 1.5rem;
    position: relative;
  }
  
  .history-sidebar {
    position: fixed;
    top: 0;
    left: 0;
    height: 100vh;
    width: 300px;
    background: white;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
    transform: translateX(-100%);
    transition: transform 0.3s ease;
    z-index: 1000;
    overflow-y: auto;
    
    &.active {
      transform: translateX(0);
    }
    
    .history-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 1.5rem;
      border-bottom: 1px solid #e5e7eb;
      
      h3 {
        font-size: 1.25rem;
        font-weight: 600;
        margin: 0;
      }
      
      .close-btn {
        background: none;
        border: none;
        font-size: 1.25rem;
        color: #6b7280;
        cursor: pointer;
        padding: 0.5rem;
        
        &:hover {
          color: #3b82f6;
        }
      }
    }
    
    .history-list {
      padding: 1rem 0;
    }
    
    .history-item {
      display: flex;
      align-items: center;
      padding: 0.75rem 1.5rem;
      cursor: pointer;
      transition: background-color 0.2s;
      border-left: 3px solid transparent;
      
      &:hover {
        background-color: #f9fafb;
      }
      
      &.active {
        background-color: #f0f7ff;
        border-left-color: #3b82f6;
        
        .title {
          color: #3b82f6;
          font-weight: 500;
        }
      }
      
      i {
        color: #6b7280;
        width: 1.25rem;
        margin-right: 0.75rem;
      }
      
      .title {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        color: #4b5563;
      }
    }
  }
  
  .content-area {
    flex: 1;
    padding: 2rem 0;
  }
  
  .card {
    background-color: white;
    border-radius: 0.75rem;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
    margin-bottom: 2rem;
    overflow: hidden;
  }
  
  .upload-card {
    max-width: 800px;
    margin: 0 auto;
  }
  
  .upload-area {
    padding: 3rem 2rem;
    text-align: center;
    border: 2px dashed #d1d5db;
    border-radius: 0.75rem;
    transition: all 0.3s;
    cursor: pointer;
    position: relative;
    
    &.dragging {
      border-color: #3b82f6;
      background-color: #f0f7ff;
    }
    
    &.uploading {
      border-color: #007CF0;
      background-color: #f0f9ff;
    }
    
    .upload-content {
      pointer-events: none;
    }
    
    .upload-icon {
      width: 5rem;
      height: 5rem;
      background-color: #dbeafe;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 1.5rem;
      
      i {
        color: #3b82f6;
        font-size: 2.5rem;
      }
    }
    
    h3 {
      font-size: 1.5rem;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 0.5rem;
    }
    
    p {
      color: #6b7280;
      margin-bottom: 1.5rem;
      font-size: 1.1rem;
    }
    
    .btn {
      font-size: 1rem;
      padding: 0.75rem 1.75rem;
    }
    
    .file-info {
      font-size: 0.875rem;
      color: #9ca3af;
      margin-top: 1rem;
    }
    
    .upload-status {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 1.5rem;
      
      .spinner {
        width: 48px;
        height: 48px;
        border: 4px solid rgba(0, 124, 240, 0.1);
        border-left-color: #007CF0;
        border-radius: 50%;
        animation: spin 1s linear infinite;
      }
      
      .upload-progress {
        text-align: center;
        
        .status-text {
          font-size: 1.25rem;
          color: #007CF0;
          margin-bottom: 0.5rem;
          font-weight: 500;
        }
        
        .filename {
          font-size: 1rem;
          color: #666;
          max-width: 300px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
          margin: 0 auto;
        }
      }
    }
    
    .error-message {
      margin-top: 15px;
      color: #ef4444;
      font-size: 1rem;
      font-weight: 500;
    }
  }
  
  .assessment-card {
    padding: 2rem;
    
    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 1.5rem;
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #e5e7eb;
      
      h2 {
        font-size: 1.5rem;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
      }
      
      .status-badge {
        background-color: #dcfce7;
        color: #166534;
        padding: 0.5rem 1rem;
        border-radius: 9999px;
        font-size: 0.95rem;
        font-weight: 500;
        display: flex;
        align-items: center;
        
        i {
          margin-right: 0.25rem;
        }
      }
    }
    
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 1.5rem;
      margin-bottom: 2rem;
    }
    
    .metric-card {
      background-color: #f9fafb;
      border-radius: 0.75rem;
      padding: 1.5rem;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateY(-3px);
      }
      
      .metric-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 1rem;
        
        h4 {
          font-size: 1.1rem;
          font-weight: 500;
          color: #4b5563;
          margin: 0;
        }
        
        .score {
          font-size: 1.5rem;
          font-weight: 700;
          color: #3b82f6;
        }
      }
      
      .score-bar {
        height: 0.75rem;
        border-radius: 0.5rem;
        background-color: #e5e7eb;
        overflow: hidden;
        margin-bottom: 1rem;
      }
      
      .score-fill {
        height: 100%;
        background-color: #3b82f6;
        border-radius: 0.5rem;
        transition: width 0.5s ease;
        
        &.yellow {
          background-color: #eab308;
        }
        
        &.green {
          background-color: #22c55e;
        }
        
        &.purple {
          background-color: #8b5cf6;
        }
      }
      
      .metric-description {
        color: #6b7280;
        font-size: 1rem;
        line-height: 1.5;
      }
      
      .metric-title {
        font-weight: 500;
        color: #4b5563;
        margin-bottom: 1rem;
        font-size: 1.1rem;
      }
      
      .metrics-list {
        display: grid;
        gap: 1rem;
      }
      
      .metric-item {
        .metric-label {
          display: flex;
          justify-content: space-between;
          font-size: 0.95rem;
          margin-bottom: 0.5rem;
          color: #4b5563;
        }
      }
    }
    
    .section-title {
      font-size: 1.5rem;
      font-weight: 600;
      color: #1f2937;
      margin-bottom: 1.5rem;
      padding-top: 1rem;
      border-top: 1px solid #e5e7eb;
    }
    
    .suggestions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.5rem;
    }
    
    .suggestion-card {
      background-color: #f9fafb;
      border-radius: 0.75rem;
      padding: 1.5rem;
      transition: transform 0.2s;
      
      &:hover {
        transform: translateY(-3px);
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
      }
      
      .suggestion-header {
        display: flex;
        align-items: center;
        margin-bottom: 1rem;
      }
      
      .icon-bg {
        width: 2.5rem;
        height: 2.5rem;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: 1rem;
        
        i {
          font-size: 1.1rem;
        }
        
        &.blue {
          background-color: #dbeafe;
          color: #3b82f6;
        }
        
        &.purple {
          background-color: #ede9fe;
          color: #8b5cf6;
        }
        
        &.green {
          background-color: #dcfce7;
          color: #22c55e;
        }
        
        &.yellow {
          background-color: #fef9c3;
          color: #eab308;
        }
        
        &.red {
          background-color: #fee2e2;
          color: #ef4444;
        }
        
        &.indigo {
          background-color: #e0e7ff;
          color: #6366f1;
        }
      }
      
      h4 {
        font-size: 1.1rem;
        font-weight: 500;
        color: #1f2937;
        margin: 0;
      }
      
      p {
        font-size: 1rem;
        color: #6b7280;
        line-height: 1.5;
        padding-left: 3.5rem;
        margin: 0;
      }
    }
  }
  
  .chat-card {
    padding: 1.5rem;
    
    .chat-header {
      padding-bottom: 1.5rem;
      border-bottom: 1px solid #e5e7eb;
      margin-bottom: 1rem;
      
      h2 {
        font-size: 1.5rem;
        font-weight: 600;
        color: #1f2937;
        margin: 0;
      }
    }
    
    .messages {
      min-height: 400px;
      max-height: 500px;
      overflow-y: auto;
      padding: 0.5rem;
      margin-bottom: 1rem;
    }
    
    .message-bubble {
      display: flex;
      margin-bottom: 1.5rem;
      
      .message-avatar {
        width: 2.5rem;
        height: 2.5rem;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-shrink: 0;
        margin-right: 1rem;
        
        &.user {
          background-color: #dbeafe;
          color: #3b82f6;
        }
        
        &.assistant {
          background-color: #ede9fe;
          color: #8b5cf6;
        }
        
        i {
          font-size: 1.25rem;
        }
      }
      
      .message-content {
        flex: 1;
        padding: 1rem;
        border-radius: 0.75rem;
        background-color: #f9fafb;
        position: relative;
        
        :deep(p) {
          margin-bottom: 0.5rem;
          line-height: 1.5;
        }
        
        :deep(ul) {
          padding-left: 1.5rem;
          margin-bottom: 0.5rem;
        }
        
        :deep(li) {
          margin-bottom: 0.25rem;
        }
        
        .message-time {
          font-size: 0.75rem;
          color: #9ca3af;
          text-align: right;
          margin-top: 0.5rem;
        }
      }
    }
    
    .input-area {
      display: flex;
      align-items: flex-end;
      padding: 0.5rem;
      border-top: 1px solid #e5e7eb;
      padding-top: 1rem;
      
      textarea {
        flex: 1;
        resize: none;
        border: 1px solid #d1d5db;
        border-radius: 0.75rem;
        padding: 1rem;
        font-family: inherit;
        font-size: 1rem;
        line-height: 1.5;
        max-height: 150px;
        transition: border-color 0.2s;
        
        &:focus {
          outline: none;
          border-color: #3b82f6;
          box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
        }
        
        &:disabled {
          background-color: #f3f4f6;
          cursor: not-allowed;
        }
      }
      
      .send-button {
        width: 2.75rem;
        height: 2.75rem;
        border-radius: 50%;
        background-color: #3b82f6;
        color: white;
        border: none;
        margin-left: 0.75rem;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        transition: background-color 0.2s;
        
        &:hover:not(:disabled) {
          background-color: #2563eb;
        }
        
        &:disabled {
          background-color: #9ca3af;
          cursor: not-allowed;
        }
        
        i {
          font-size: 1.25rem;
        }
      }
    }
  }
  
  .btn {
    padding: 0.75rem 1.5rem;
    border-radius: 0.75rem;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s;
    font-size: 1rem;
    border: none;
    
    i {
      margin-right: 0.5rem;
    }
    
    &.primary {
      background-color: #3b82f6;
      color: white;
      
      &:hover {
        background-color: #2563eb;
      }
    }
    
    &.secondary {
      background-color: white;
      border: 1px solid #d1d5db;
      color: #4b5563;
      
      &:hover {
        background-color: #f9fafb;
        border-color: #9ca3af;
      }
    }
  }
  
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
  
  /* 暗色模式 */
  &.dark {
    background-color: #111827;
    color: #e5e7eb;
    
    .header {
      background-color: #1f2937;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
      
      h1 {
        color: #f3f4f6;
      }
    }
    
    .card {
      background-color: #1f2937;
      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.15), 0 2px 4px -1px rgba(0, 0, 0, 0.1);
    }
    
    .upload-area {
      border-color: #4b5563;
      background-color: #1f2937;
      
      &.dragging {
        border-color: #3b82f6;
        background-color: rgba(59, 130, 246, 0.1);
      }
      
      &.uploading {
        border-color: #007CF0;
        background-color: rgba(0, 124, 240, 0.1);
      }
      
      .upload-icon {
        background-color: rgba(59, 130, 246, 0.2);
        
        i {
          color: #3b82f6;
        }
      }
      
      h3, p {
        color: #d1d5db;
      }
      
      .file-info {
        color: #9ca3af;
      }
      
      .upload-status {
        .upload-progress {
          .status-text {
            color: #60a5fa;
          }
          
          .filename {
            color: #9ca3af;
          }
        }
      }
    }
    
    .assessment-card {
      .section-header {
        border-color: #374151;
        
        h2 {
          color: #f3f4f6;
        }
        
        .status-badge {
          background-color: #14532d;
          color: #86efac;
        }
      }
      
      .metric-card {
        background-color: #111827;
        border: 1px solid #374151;
        
        .metric-header {
          h4 {
            color: #d1d5db;
          }
        }
        
        .metric-description {
          color: #9ca3af;
        }
        
        .metric-title {
          color: #d1d5db;
        }
        
        .metric-label {
          color: #d1d5db;
        }
      }
      
      .section-title {
        color: #f3f4f6;
        border-color: #374151;
      }
      
      .suggestion-card {
        background-color: #111827;
        border: 1px solid #374151;
        
        h4 {
          color: #f3f4f6;
        }
        
        p {
          color: #9ca3af;
        }
      }
    }
    
    .chat-card {
      .chat-header {
        border-color: #374151;
        
        h2 {
          color: #f3f4f6;
        }
      }
      
      .message-bubble {
        .message-content {
          background-color: #111827;
          color: #e5e7eb;
        }
      }
      
      .input-area {
        border-color: #374151;
        
        textarea {
          background-color: #111827;
          border-color: #374151;
          color: #e5e7eb;
          
          &:focus {
            border-color: #3b82f6;
            box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.2);
          }
          
          &:disabled {
            background-color: #1f2937;
          }
        }
      }
    }
    
    .btn.secondary {
      background-color: #374151;
      color: #d1d5db;
      border-color: #4b5563;
      
      &:hover {
        background-color: #4b5563;
        border-color: #6b7280;
      }
    }
    
    .history-sidebar {
      background-color: #1f2937;
      
      .history-header {
        border-color: #374151;
        
        h3 {
          color: #f3f4f6;
        }
        
        .close-btn {
          color: #9ca3af;
          
          &:hover {
            color: #3b82f6;
          }
        }
      }
      
      .history-item {
        color: #d1d5db;
        
        &:hover {
          background-color: rgba(59, 130, 246, 0.1);
        }
        
        &.active {
          background-color: rgba(59, 130, 246, 0.2);
          border-left-color: #3b82f6;
          
          .title {
            color: #3b82f6;
          }
        }
        
        i {
          color: #9ca3af;
        }
        
        .title {
          color: #d1d5db;
        }
      }
    }
  }

  @media (max-width: 1024px) {
    .main-container {
      padding: 0 1rem;
    }
    
    .assessment-card,
    .chat-card {
      padding: 1.5rem 1rem;
    }
    
    .suggestions-grid {
      grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    }
  }

  @media (max-width: 768px) {
    .header {
      padding: 1rem;
      flex-direction: column;
      gap: 1rem;
      align-items: flex-start;
      
      h1 {
        font-size: 1.5rem;
      }
      
      .header-buttons {
        width: 100%;
        justify-content: space-between;
      }
    }
    
    .upload-area {
      padding: 2rem 1rem;
      
      h3 {
        font-size: 1.25rem;
      }
    }
    
    .metrics-grid {
      grid-template-columns: 1fr;
    }
    
    .suggestions-grid {
      grid-template-columns: 1fr;
    }
    
    .suggestion-card p {
      padding-left: 0;
      padding-top: 0.5rem;
    }
    
    .history-sidebar {
      width: 280px;
    }
  }
}
</style>