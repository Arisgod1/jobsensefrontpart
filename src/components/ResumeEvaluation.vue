<template>
  <div class="resume-container">
    <div class="header">
      <h1>简历评估</h1>
      <div class="header-buttons">
        <button class="btn secondary">
          <i class="fas fa-history mr-2"></i> 历史记录
        </button>
        <button class="btn primary">
          <i class="fas fa-user mr-2"></i> 我的资料
        </button>
      </div>
    </div>

    <div class="card">
      <div class="upload-area"
           @dragover.prevent="dragover"
           @drop.prevent="drop"
           @click="$refs.fileInput.click()">
        <div class="upload-icon">
          <template v-if="isUploading">
            <i class="fas fa-spinner fa-spin"></i>
          </template>
          <template v-else>
            <i class="fas fa-cloud-upload-alt"></i>
          </template>
        </div>
        <h3 v-if="!result">上传您的简历</h3>
        <h3 v-else>上传新简历</h3>
        <p v-if="fileName">{{ fileName }}</p>
        <p v-else>将简历文件拖放到此处，或点击浏览</p>
        <input type="file" ref="fileInput" style="display: none" 
               @change="handleFileChange" accept=".pdf,.doc,.docx">
        <button class="btn primary" style="left:50%">选择文件</button>
        <p class="file-info">支持 PDF, DOC, DOCX (最大5MB)</p>
        <div v-if="errorMessage" class="error-message">
          {{ errorMessage }}
        </div>
      </div>

      <div v-if="result" class="analysis-section">
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
    </div>

    <div v-if="result" class="card">
      <h2 class="section-title">详细反馈</h2>
      <div class="feedback-content">
        {{ result }}
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ResumeEvaluation',
  data() {
    return {
      result: '',
      isUploading: false,
      fileName: '',
      errorMessage: ''
    };
  },
  methods: {
    async uploadFile(file) {
      if (!this.validateFile(file)) return;
      
      this.isUploading = true;
      this.errorMessage = '';
      this.fileName = file.name;
      
      const formData = new FormData();
      formData.append('resume', file);
      
      try {
        const response = await fetch('/api/upload-resume', {
          method: 'POST',
          body: formData,
          // 注意：使用fetch时不要手动设置Content-Type，
          // 浏览器会自动设置包含boundary的multipart/form-data
        });
        
        if (!response.ok) {
          throw new Error(`上传失败: ${response.status} ${response.statusText}`);
        }
        
        const data = await response.json();
        
        if (data && data.analysisResult) {
          this.result = data.analysisResult;
        } else {
          throw new Error('服务器返回无效数据');
        }
      } catch (error) {
        this.errorMessage = error.message || '上传过程中发生错误';
        this.result = '';
      } finally {
        this.isUploading = false;
      }
    },
    validateFile(file) {
      const validTypes = [
        'application/pdf', 
        'application/msword', 
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
      ];
      
      const validExtensions = ['.pdf', '.doc', '.docx'];
      const maxSize = 5 * 1024 * 1024; // 5MB
      
      // 额外检查文件扩展名，因为某些浏览器可能无法正确识别MIME类型
      const extension = '.' + file.name.split('.').pop().toLowerCase();
      if (!validExtensions.includes(extension)) {
        this.errorMessage = '不支持的文件格式。请上传PDF、DOC或DOCX文件。';
        return false;
      }
      
      if (file.size > maxSize) {
        this.errorMessage = '文件太大。请上传小于5MB的文件。';
        return false;
      }
      
      return true;
    },
    handleFileChange(e) {
      const file = e.target.files[0];
      if (file) this.uploadFile(file);
    },
    dragover(e) {
      e.preventDefault();
      e.currentTarget.classList.add('dragover');
    },
    drop(e) {
      e.preventDefault();
      e.currentTarget.classList.remove('dragover');
      
      const file = e.dataTransfer.files[0];
      if (file) this.uploadFile(file);
    }
  }
}
</script>

<style scoped>
/* 添加拖放时的视觉效果 */
.upload-area.dragover {
  border-color: #3b82f6;
  background-color: #f0f7ff;
}

/* 错误消息样式 */
.error-message {
  margin-top: 15px;
  color: #ef4444;
  font-size: 14px;
}

/* 反馈内容样式 */
.feedback-content {
  white-space: pre-wrap;
  line-height: 1.6;
  padding: 15px;
  background-color: #f8fafc;
  border-radius: 8px;
  max-height: 400px;
  overflow-y: auto;
}

/* 旋转动画 */
.fa-spinner {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 原有样式保持不变 */
.resume-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1rem;
  background-color: #f9fafb;
  font-family: Arial, sans-serif;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
}

.header h1 {
  font-size: 1.875rem;
  font-weight: 700;
  color: #1f2937;
}

.header-buttons {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.5rem 1rem;
  border-radius: 0.5rem;
  display: flex;
  align-items: center;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.primary {
  background-color: #3b82f6;
  color: white;
  border: none;
}

.primary:hover {
  background-color: #2563eb;
}

.secondary {
  background-color: white;
  border: 1px solid #d1d5db;
  color: #4b5563;
}

.secondary:hover {
  background-color: #f9fafb;
}

.card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  margin-bottom: 2rem;
}

.upload-area {
  padding: 2rem;
  text-align: center;
  border: 2px dashed #d1d5db;
  border-radius: 0.5rem;
  margin: 1.5rem;
  transition: all 0.3s;
  cursor: pointer;
  position: relative;
}

.upload-area:hover {
  border-color: #9ca3af;
  background-color: #f9fafb;
}

.upload-icon {
  width: 4rem;
  height: 4rem;
  background-color: #dbeafe;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1rem;
}

.upload-icon i {
  color: #3b82f6;
  font-size: 1.5rem;
}

.upload-area h3 {
  font-size: 1.125rem;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.upload-area p {
  color: #6b7280;
  margin-bottom: 1rem;
}

.file-info {
  font-size: 0.75rem;
  color: #9ca3af;
  margin-top: 0.75rem;
}

.analysis-section {
  padding: 1.5rem;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
  padding-top: 1.5rem;
  border-top: 1px solid #e5e7eb;
}

.section-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
}

.status-badge {
  background-color: #dcfce7;
  color: #166534;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.875rem;
  font-weight: 500;
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 1.5rem;
  margin-bottom: 1.5rem;
}

.metric-card {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1.25rem;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.metric-header h4 {
  font-weight: 500;
  color: #4b5563;
}

.score {
  font-size: 1.5rem;
  font-weight: 700;
  color: #3b82f6;
}

.score-bar {
  height: 0.5rem;
  border-radius: 0.25rem;
  background-color: #e5e7eb;
  overflow: hidden;
  margin-bottom: 1rem;
}

.score-fill {
  height: 100%;
  background-color: #3b82f6;
  border-radius: 0.25rem;
  transition: width 0.5s ease;
}

.yellow {
  background-color: #eab308;
}

.green {
  background-color: #22c55e;
}

.purple {
  background-color: #8b5cf6;
}

.metric-description {
  color: #6b7280;
  font-size: 0.875rem;
}

.metric-title {
  font-weight: 500;
  color: #4b5563;
  margin-bottom: 0.75rem;
}

.metrics-list {
  display: grid;
  gap: 0.75rem;
}

.metric-item {
  margin-bottom: 0.5rem;
}

.metric-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  margin-bottom: 0.25rem;
  color: #4b5563;
}

.section-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1rem;
}

.suggestions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1rem;
}

.suggestion-card {
  background-color: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 1rem;
  transition: all 0.3s;
}

.suggestion-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1);
}

.suggestion-header {
  display: flex;
  align-items: flex-start;
  margin-bottom: 0.5rem;
}

.icon-bg {
  width: 2rem;
  height: 2rem;
  border-radius: 9999px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0.75rem;
}

.icon-bg i {
  font-size: 0.875rem;
}

.blue {
  background-color: #dbeafe;
  color: #3b82f6;
}

.purple {
  background-color: #ede9fe;
  color: #8b5cf6;
}

.green {
  background-color: #dcfce7;
  color: #22c55e;
}

.yellow {
  background-color: #fef9c3;
  color: #eab308;
}

.red {
  background-color: #fee2e2;
  color: #ef4444;
}

.indigo {
  background-color: #e0e7ff;
  color: #6366f1;
}

.suggestion-card h4 {
  font-weight: 500;
  color: #1f2937;
}

.suggestion-card p {
  font-size: 0.875rem;
  color: #6b7280;
  padding-left: 2.75rem;
}

.feedback-list {
  display: grid;
  gap: 1rem;
}

.feedback-item {
  padding-bottom: 1rem;
  border-bottom: 1px solid #e5e7eb;
}

.feedback-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.feedback-item h4 {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.feedback-content {
  display: flex;
  align-items: flex-start;
}

.feedback-content i {
  margin-top: 0.25rem;
  margin-right: 0.75rem;
  font-size: 1.25rem;
}

::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-thumb {
  background-color: #cbd5e1;
  border-radius: 3px;
}
</style>