<template>
  <div class="container mx-auto py-8">
    <h1 class="text-3xl font-bold text-center text-gray-800 mb-8">AI面试原型</h1>
    
    <div class="page-container">
      <div class="page-title">简历评估页面</div>
      <div class="preview-container">
        <ResumeEvaluationPreview />
      </div>
    </div>

    <div class="page-container mt-10">
      <div class="page-title">AI面试页面</div>
      <div class="preview-container">
        <AIInterviewPreview />
      </div>
    </div>
    
    <footer class="text-center py-6 text-gray-500 text-sm">
      © 2023 Dinglz. 保留所有权利。
    </footer>
  </div>
</template>

<script>
import ResumeEvaluationPreview from './ResumeEvaluation.vue';
import AIInterviewPreview from './AIInterview.vue';

export default {
  name: 'Prototype',
  components: {
    ResumeEvaluationPreview,
    AIInterviewPreview
  }
}
</script>

<style scoped>
.page-container {
  margin: 20px auto;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  width: 90%;
  max-width: 1200px;
}

.page-title {
  background-color: #f8fafc;
  padding: 12px 20px;
  border-bottom: 1px solid #e2e8f0;
  font-weight: 600;
  color: #334155;
  text-align: center;
}

.preview-container {
  padding: 15px;
  background-color: #f8fafc;
  max-height: 600px;
  overflow-y: auto;
}

/* 调整预览组件的尺寸 */
.preview-container > div {
  transform: scale(0.85);
  transform-origin: top center;
  width: 117.6%; /* 100% / 0.85 ≈ 117.6% */
  margin-left: -8.8%; /* (117.6% - 100%)/2 ≈ 8.8% */
}
</style>