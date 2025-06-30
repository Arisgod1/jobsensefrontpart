<template>
  <div class="interview-container">
    
    <div class="header">
      <h1><i class="fas fa-robot"></i> Java工程师模拟面试</h1>
      <div class="header-buttons">
        <button class="btn secondary">
          <i class="fas fa-cog mr-2"></i> 设置
        </button>
        <button class="btn secondary">
          <i class="fas fa-question-circle mr-2"></i> 帮助
        </button>
      </div>
    </div>

    <div class="grid-layout">
      <div class="left-column">
        <div class="interview-card">
          <div class="card-header">
            <h2><i class="fas fa-comments mr-2"></i> 模拟面试</h2>
            <div class="status-indicator">
              <span>{{ interviewStatus }}</span>
              <div class="status-dot" :class="{ recording: isInterviewActive }"></div>
            </div>
          </div>
          
          <div class="question-display">
            <div v-if="!isInterviewActive" class="interview-placeholder">
              <div class="interview-icon">
                <i class="fas fa-user-tie"></i>
              </div>
              <p>点击"开始面试"按钮启动模拟面试</p>
            </div>
            <div v-else>
              <div class="interview-progress">
                问题 {{ currentQuestionIndex + 1 }} / 5
              </div>
              <div class="question-content">
                {{ currentQuestion }}
              </div>
            </div>
          </div>
          
          <div v-if="isInterviewActive" class="recording-timer">
            <i class="fas fa-clock mr-2"></i> {{ formatTime(timer) }}
          </div>
          
          <div class="start-button-container">
            <button v-if="!isInterviewActive" 
                    class="btn success" 
                    @click="startInterview" 
                    :disabled="isLoading">
              <i v-if="isLoading" class="fas fa-spinner fa-spin mr-2"></i>
              <i v-else class="fas fa-play mr-2"></i> 
              {{ isLoading ? '准备中...' : '开始面试' }}
            </button>
          </div>
          
          <div class="interview-controls" v-if="isInterviewActive">
            <button class="btn secondary" @click="toggleAnswering" :disabled="!isInterviewActive || wsStatus !== 'connected'">
              <i v-if="isAnswering" class="fas fa-stop mr-2"></i>
              <i v-else class="fas fa-microphone mr-2"></i>
              {{ isAnswering ? '停止回答' : '开始回答' }}
            </button>
            <button class="btn secondary" @click="repeatQuestion">
              <i class="fas fa-redo mr-2"></i> 重复问题
            </button>
            <button class="btn danger" @click="stopInterview">
              <i class="fas fa-stop mr-2"></i> 结束面试
            </button>
          </div>
          
          <div class="ws-status" :class="wsStatusClass">
            <i class="fas" :class="wsStatusIcon"></i> {{ wsStatusText }}
          </div>
          
          <div v-if="isInterviewActive" class="sent-data-info">
            <p><i class="fas fa-database mr-2"></i> 已发送音频: {{ audioDataSent }} KB</p>
            <p><i class="fas fa-cloud-upload-alt mr-2"></i> WebSocket状态: {{ wsMessagesSent }} 条消息</p>
          </div>
        </div>

        <div class="progress-card">
          <h2><i class="fas fa-tasks mr-2"></i> 面试进度</h2>
          <div class="progress-list">
            <div class="progress-item">
              <div class="step-icon completed">
                <i class="fas fa-check"></i>
              </div>
              <div>
                <p class="step-title">设备设置</p>
                <p class="step-time">{{ deviceSetupTime }}</p>
              </div>
            </div>
            <div class="progress-item">
              <div class="step-icon" :class="isInterviewActive ? 'active' : 'inactive'">
                {{ isInterviewActive ? '2' : '' }}
                <i v-if="!isInterviewActive" class="fas fa-clock"></i>
              </div>
              <div :class="{'flex-1': isInterviewActive}">
                <p class="step-title">面试环节</p>
                <p class="step-time">{{ interviewStatusTime }}</p>
                <div v-if="isInterviewActive" class="progress-bar">
                  <div class="progress-fill" :style="progressBarStyle"></div>
                </div>
              </div>
            </div>
            <div class="progress-item">
              <div class="step-icon inactive">
                3
              </div>
              <div>
                <p class="step-title">结果分析</p>
                <p class="step-time">等待中</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-column">
        <div class="emotion-card">
          <div class="card-header">
            <h2><i class="fas fa-smile-beam mr-2"></i> 情绪分析</h2>
            <span class="status-badge" :class="isInterviewActive ? 'blue' : ''">
              {{ isInterviewActive ? '活跃中' : '等待中' }}
            </span>
          </div>
          <div class="emotion-metrics">
            <div class="metric-item">
              <div class="metric-label">
                <span>自信度</span>
                <span>{{ emotions.confidence.toFixed(4) }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill blue" :style="{ width: emotions.confidence + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>参与度</span>
                <span>{{ emotions.engagement.toFixed(4) }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill green" :style="{ width: emotions.engagement + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>紧张度</span>
                <span>{{ emotions.nervousness.toFixed(4) }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill yellow" :style="{ width: emotions.nervousness + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>积极性</span>
                <span>{{ emotions.enthusiasm.toFixed(4) }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill purple" :style="{ width: emotions.enthusiasm + '%' }"></div>
              </div>
            </div>
          </div>
          <div class="feedback-tip">
            <i class="fas fa-lightbulb" :class="isInterviewActive ? 'yellow' : ''"></i>
            <p>{{ feedbackTip }}</p>
          </div>
        </div>

        <div class="question-card">
          <div class="card-header">
            <h2><i class="fas fa-question-circle mr-2"></i> 当前问题</h2>
            <div class="thinking-indicator">
              <span class="status-badge" :class="isInterviewActive ? 'blue' : ''">
                {{ isInterviewActive ? (isAnswering ? '回答中' : '请回答') : '等待开始' }}
              </span>
              <div class="status-dot" :class="isInterviewActive ? 'blue' : ''"></div>
            </div>
          </div>
          <div class="question-bubble">
            <p>{{ currentQuestion }}</p>
          </div>
          <div class="question-info">
            <span>{{ questionType }}</span>
            <span>{{ questionTimeLeft }}</span>
          </div>
          <button class="btn secondary full-width" :disabled="!isInterviewActive" @click="repeatQuestion">
            <i class="fas fa-redo mr-2"></i> 重复问题
          </button>
        </div>

        <div class="ai-status-card">
          <h2><i class="fas fa-brain mr-2"></i> AI分析状态</h2>
          <div class="status-visual">
            <div class="status-content">
              <div class="status-text">{{ aiStatus }}</div>
              <div class="status-subtext">{{ aiSubStatus }}</div>
            </div>
            <svg class="status-ring" viewBox="0 0 100 100">
              <circle cx="50" cy="50" r="45" class="status-track"></circle>
              <circle cx="50" cy="50" r="45" class="status-progress" :style="progressRingStyle"></circle>
            </svg>
          </div>
          <div class="process-indicators">
            <div class="indicator">
              <div class="indicator-label">聆听</div>
              <div class="indicator-bar">
                <div class="indicator-fill blue" :style="{ width: listeningProgress + '%' }"></div>
              </div>
            </div>
            <div class="indicator">
              <div class="indicator-label">处理</div>
              <div class="indicator-bar">
                <div class="indicator-fill purple" :style="{ width: processingProgress + '%' }"></div>
              </div>
            </div>
            <div class="indicator">
              <div class="indicator-label">回应</div>
              <div class="indicator-bar">
                <div class="indicator-fill green" :style="{ width: responseProgress + '%' }"></div>
              </div>
            </div>
          </div>
        </div>

        <div class="feedback-card">
          <h2><i class="fas fa-comment-medical mr-2"></i> 实时反馈</h2>
          <div class="feedback-items">
            <div v-for="(feedback, index) in feedbackList" :key="index" class="feedback-item">
              <div class="feedback-icon" :class="feedback.type">
                <i :class="feedback.icon"></i>
              </div>
              <p>{{ feedback.text }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 面试总结报告显示在页面底部 -->
    <div v-if="showSummary" class="summary-container">
      <h2><i class="fas fa-file-alt mr-2"></i> 面试总结报告</h2>
      <div class="summary-content">
        <div class="summary-card">
          <div class="summary-header">
            <div class="summary-icon">
              <i class="fas fa-file-medical-alt"></i>
            </div>
            <div>
              <h3>Java工程师模拟面试报告</h3>
              <p>面试时间: {{ interviewSummary.date }}</p>
              <p>持续时间: {{ interviewSummary.duration }} 分钟</p>
            </div>
          </div>
          
          <div class="performance-stats">
            <div class="stat-box">
              <div class="stat-value">{{ interviewSummary.totalQuestions }}</div>
              <div class="stat-label">问题数量</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ interviewSummary.answeredQuestions }}</div>
              <div class="stat-label">已回答问题</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ interviewSummary.avgAnswerTime }}s</div>
              <div class="stat-label">平均回答时间</div>
            </div>
            <div class="stat-box">
              <div class="stat-value">{{ interviewSummary.overallScore }}/10</div>
              <div class="stat-label">综合评分</div>
            </div>
          </div>
          
          <div class="emotion-overview">
            <h4>情绪表现分析</h4>
            <div class="emotion-grid">
              <div class="emotion-item">
                <div class="emotion-title">
                  <i class="fas fa-smile blue"></i>
                  <span>自信度</span>
                </div>
                <div class="emotion-value">{{ interviewSummary.confidence.toFixed(4) }}%</div>
              </div>
              <div class="emotion-item">
                <div class="emotion-title">
                  <i class="fas fa-brain green"></i>
                  <span>参与度</span>
                </div>
                <div class="emotion-value">{{ interviewSummary.engagement.toFixed(4) }}%</div>
              </div>
              <div class="emotion-item">
                <div class="emotion-title">
                  <i class="fas fa-flushed yellow"></i>
                  <span>紧张度</span>
                </div>
                <div class="emotion-value">{{ interviewSummary.nervousness.toFixed(4) }}%</div>
              </div>
              <div class="emotion-item">
                <div class="emotion-title">
                  <i class="fas fa-star purple"></i>
                  <span>积极性</span>
                </div>
                <div class="emotion-value">{{ interviewSummary.enthusiasm.toFixed(4) }}%</div>
              </div>
            </div>
          </div>
          
          <div class="feedback-section">
            <h4>面试官反馈</h4>
            <div class="feedback-content">
              <p>{{ interviewSummary.feedback }}</p>
            </div>
            <div class="strengths-weaknesses">
              <div class="strengths">
                <h5><i class="fas fa-check-circle green"></i> 优势</h5>
                <ul>
                  <li v-for="(strength, index) in interviewSummary.strengths" :key="'str'+index">{{ strength }}</li>
                </ul>
              </div>
              <div class="weaknesses">
                <h5><i class="fas fa-exclamation-triangle yellow"></i> 改进方向</h5>
                <ul>
                  <li v-for="(weakness, index) in interviewSummary.weaknesses" :key="'wk'+index">{{ weakness }}</li>
                </ul>
              </div>
            </div>
          </div>
          
          <div class="tips-section">
            <h4>学习建议</h4>
            <div class="tip">
              <div class="tip-icon">
                <i class="fas fa-book"></i>
              </div>
              <p>{{ interviewSummary.tip }}</p>
            </div>
          </div>
          
          <div class="actions-section">
            <button class="btn primary">
              <i class="fas fa-download mr-2"></i> 下载完整报告
            </button>
            <button class="btn secondary" @click="showSummary = false">
              <i class="fas fa-times mr-2"></i> 关闭报告
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'JavaInterview',
  data() {
    return {
      isInterviewActive: false,
      isLoading: false,
      isRecording: true,
      interviewStatus: '等待开始',
      wsStatus: 'disconnected',
      timer: 0, // 总面试时间
      countdown: 120, // 每个问题的倒计时 (120秒=2分钟)
      audioDataSent: 0,
      wsMessagesSent: 0,
      deviceSetupTime: '待完成',
      interviewStatusTime: '等待开始',
      progress: 0,
      
      // 音频状态变量
      audioContext: null,
      bufferStatus: '空闲',
      bufferProgress: 0,
      connectionAttempts: 0,
      isReconnecting: false,
      maxReconnectAttempts: 5,
      lastSendTime: 0,
      
      // Java工程师面试问题库
      javaQuestions: [
        "请介绍一下Java中的多态性以及它在实际项目中的应用。",
        "请解释Java中的垃圾回收机制，以及如何优化垃圾回收性能？",
        "请描述Java集合框架，并比较ArrayList和LinkedList的异同。",
        "请解释Java中的线程安全，并举例说明如何实现线程安全。",
        "请谈谈你对Spring框架的理解，并说明IoC和AOP的概念及其作用。"
      ],
      currentQuestionIndex: -1,
      
      // 情绪分析数据
      emotions: {
        confidence: 35,
        engagement: 45,
        nervousness: 65,
        enthusiasm: 40
      },
      
      // 当前问题信息
      currentQuestion: "请点击'开始面试'按钮启动Java工程师模拟面试",
      questionType: '准备就绪',
      questionTimeLeft: '等待开始',
      
      // AI状态
      aiStatus: '离线',
      aiSubStatus: '等待开始',
      listeningProgress: 0,
      processingProgress: 0,
      responseProgress: 0,
      
      // 反馈信息
      feedbackTip: "面试开始后，情绪分析将实时更新并提供建议",
      feedbackList: [
        { 
          type: 'blue', 
          icon: 'fas fa-info-circle', 
          text: "开始面试后，AI将提供实时语音和情感分析反馈" 
        },
        { 
          type: 'green', 
          icon: 'fas fa-lightbulb', 
          text: "请确保麦克风已连接并开启" 
        }
      ],
      
      // WebSocket配置
      wsEndpoint: 'ws://localhost:8080/realtime/audio/websocket/123',
      
      // 回答状态
      isAnswering: false,
      lastQuestion: "",
      
      // 倒计时计时器
      countdownInterval: null,
      
      // 面试总结相关
      showSummary: false,
      interviewSummary: {
        date: '',
        duration: 0,
        totalQuestions: 0,
        answeredQuestions: 0,
        avgAnswerTime: 0,
        overallScore: 0,
        confidence: 0,
        engagement: 0,
        nervousness: 0,
        enthusiasm: 0,
        feedback: '',
        strengths: [],
        weaknesses: [],
        tip: ''
      }
    };
  },
  computed: {
    // WebSocket状态相关计算属性
    wsStatusClass() {
      return {
        'ws-connected': this.wsStatus === 'connected',
        'ws-disconnected': this.wsStatus !== 'connected'
      };
    },
    wsStatusIcon() {
      return this.wsStatus === 'connected' ? 'fa-check-circle' : 'fa-times-circle';
    },
    wsStatusText() {
      return this.wsStatus === 'connected' ? 
        'WebSocket 已连接' : 
        'WebSocket 未连接';
    },
    
    // 进度条样式
    progressBarStyle() {
      const width = Math.min(100, Math.max(5, this.progress));
      return {
        width: `${width}%`
      };
    },
    
    // 环形进度条样式
    progressRingStyle() {
      if (!this.isInterviewActive) return {};
      
      const progress = this.listeningProgress * 2.83;
      return {
        strokeDasharray: '283',
        strokeDashoffset: 283 - progress
      };
    }
  },
  methods: {
    // 格式化时间显示 (分钟:秒)
    formatTime(seconds) {
      const mins = Math.floor(seconds / 60);
      const secs = seconds % 60;
      return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
    },
    
    // 开始面试
    async startInterview() {
      this.isLoading = true;
      this.deviceSetupTime = '进行中...';
      
      try {
        // 请求麦克风权限
        this.stream = await navigator.mediaDevices.getUserMedia({
          audio: {
            channelCount: 1,
            sampleRate: 16000,
            echoCancellation: false,
            noiseSuppression: false,
            autoGainControl: false
          }
        });
        
        // 连接WebSocket
        this.connectWebSocket();
        
      } catch (error) {
        console.error('启动面试失败:', error);
        this.isLoading = false;
        this.deviceSetupTime = '失败';
        alert('无法访问麦克风。请确保已授予相应权限。');
      }
    },
    
    // 连接WebSocket
    connectWebSocket() {
      try {
        this.ws = new WebSocket(this.wsEndpoint);
        this.ws.binaryType = 'arraybuffer';
        this.wsStatus = 'connecting';
        this.connectionAttempts++;
        
        // WebSocket事件处理
        this.ws.onopen = () => {
          console.log('WebSocket连接已建立');
          this.wsStatus = 'connected';
          this.wsMessagesSent = 0;
          this.audioDataSent = 0;
          this.connectionAttempts = 0;
          this.isReconnecting = false;
          
          // 更新状态
          this.isInterviewActive = true;
          this.isRecording = true;
          this.interviewStatus = '面试中';
          this.isLoading = false;
          this.deviceSetupTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          this.interviewStatusTime = '进行中';
          
          // 设置定时器
          this.startTimers();
          
          // 发送开始消息
          this.sendControlMessage('start');
          
          // 显示第一个问题
          this.nextQuestion();
        };
        
        this.ws.onmessage = (event) => {
          if (typeof event.data === 'string') {
            // 处理文本消息（新问题）
            this.handleQuestionMessage(event.data);
          } else {
            // 处理二进制消息
            console.log('收到二进制数据:', event.data.byteLength, 'bytes');
          }
        };
        
        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error);
          this.wsStatus = 'error';
          
          // 连接出错时自动停止录音
          if (this.isInterviewActive) {
            this.stopRecording();
          }
          
          // 启动重连机制
          this.handleReconnect(`连接错误: ${error.message || '未知错误'}`);
        };
        
        this.ws.onclose = (event) => {
          console.log('WebSocket连接已关闭');
          this.wsStatus = 'disconnected';
          
          // 连接关闭时自动停止录音
          if (this.isInterviewActive) {
            this.stopRecording();
          }
          
          // 判断是否需要重连（非主动关闭且未达到最大尝试次数）
          if (event.code !== 1000 && this.connectionAttempts < this.maxReconnectAttempts && !this.isReconnecting) {
            this.handleReconnect(`连接已关闭，代码: ${event.code}`);
          }
        };
        
      } catch (error) {
        console.error('WebSocket连接失败:', error);
        this.wsStatus = 'error';
        this.isLoading = false;
        this.handleReconnect(`创建连接失败: ${error.message}`);
      }
    },
    
    // 处理重连逻辑
    handleReconnect(errorMessage) {
      if (this.isReconnecting || this.connectionAttempts > this.maxReconnectAttempts) return;
      
      this.isReconnecting = true;
      
      // 指数退避重连策略
      const reconnectDelay = Math.min(30000, 1000 * Math.pow(2, this.connectionAttempts - 1)); // 最大30秒延迟
      console.log(`将在 ${reconnectDelay / 1000} 秒后尝试重连 (${this.connectionAttempts}/${this.maxReconnectAttempts})`);
      
      setTimeout(() => {
        this.connectWebSocket();
        this.isReconnecting = false;
      }, reconnectDelay);
    },
    
    // 开始计时器
    startTimers() {
      // 主计时器（记录总面试时间）
      this.timerInterval = setInterval(() => {
        if (this.isInterviewActive) {
          this.timer++;
          
          // 更新进度
          this.progress = Math.min(100, (this.timer / 300) * 100); // 5分钟面试
          
          // 更新情绪数据
          this.updateEmotions();
          
          // 更新AI进度
          this.updateAiStatus();
        }
      }, 1000);
    },
    
    // 开始倒计时（每个问题2分钟）
    startCountdown() {
      this.countdown = 120; // 重置为120秒
      
      // 清除可能存在的旧计时器
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
      }
      
      // 设置新计时器
      this.countdownInterval = setInterval(() => {
        if (this.countdown > 0 && this.isInterviewActive) {
          this.countdown--;
          
          // 更新剩余时间显示
          this.questionTimeLeft = `剩余时间 ${this.formatTime(this.countdown)}`;
          
          // 当倒计时结束时自动停止回答
          if (this.countdown === 0) {
            this.stopAnswering();
          }
        }
      }, 1000);
    },
    
    // 处理问题消息
    handleQuestionMessage(message) {
      try {
        const data = JSON.parse(message);
        if (data.type === 'question') {
          this.currentQuestion = data.content;
          this.questionType = data.category || '行为问题';
          this.questionTimeLeft = `剩余时间 ${data.timeLimit || '3:00'}`;
          this.lastQuestion = data.content;
          
          // 更新AI状态
          this.aiStatus = '提问中';
          this.aiSubStatus = '请回答问题';
          this.responseProgress = 0;
          this.listeningProgress = 45;
          this.processingProgress = 25;
          
          // 播放提示音
          this.playNotificationSound();
        }
      } catch (e) {
        console.error('解析问题消息失败:', e);
      }
    },
    
    // 发送控制消息
    sendControlMessage(action) {
      if (!this.ws || this.ws.readyState !== WebSocket.OPEN) return;
      
      try {
        const message = JSON.stringify({
          type: 'control',
          action: action,
          timestamp: Date.now()
        });
        
        this.ws.send(message);
      } catch (error) {
        console.error('发送控制消息失败:', error);
      }
    },
    
    // 下一个问题
    nextQuestion() {
      if (this.currentQuestionIndex < this.javaQuestions.length - 1) {
        this.currentQuestionIndex++;
        this.currentQuestion = this.javaQuestions[this.currentQuestionIndex];
        this.questionType = this.getQuestionType(this.currentQuestionIndex);
        
        // 重置倒计时
        this.startCountdown();
        
        this.lastQuestion = this.currentQuestion;
        
        // 重置回答状态
        this.isAnswering = false;
        
        // 更新AI状态
        this.aiStatus = '等待回答';
        this.aiSubStatus = '请点击开始回答按钮';
        
        // 播放提示音
        this.playNotificationSound();
      } else {
        // 问题结束
        this.stopInterview();
      }
    },
    
    // 获取问题类型
    getQuestionType(index) {
      const types = ['Java基础', 'JVM', '集合框架', '并发编程', 'Spring框架'];
      return types[index] || 'Java技术';
    },
    
    // 重复问题
    repeatQuestion() {
      if (this.lastQuestion) {
        this.currentQuestion = this.lastQuestion;
        this.playNotificationSound();
      }
    },
    
    // 播放提示音
    playNotificationSound() {
      const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
      const oscillator = audioCtx.createOscillator();
      const gainNode = audioCtx.createGain();
      
      oscillator.connect(gainNode);
      gainNode.connect(audioCtx.destination);
      
      oscillator.type = 'sine';
      oscillator.frequency.value = 523.25; // C5
      gainNode.gain.value = 0.3;
      
      oscillator.start();
      
      setTimeout(() => {
        oscillator.stop();
        audioCtx.close();
      }, 300);
    },
    
    // 更新情绪分析数据 (优化后的算法)
    updateEmotions() {
      // 基础变化趋势
      const baseChange = {
        confidence: 0.4,
        engagement: 0.35,
        nervousness: -0.5,
        enthusiasm: 0.3
      };
      
      // 添加随机波动 (±5%)
      const fluctuation = {
        confidence: (Math.random() - 0.5) * 10,
        engagement: (Math.random() - 0.5) * 10,
        nervousness: (Math.random() - 0.5) * 10,
        enthusiasm: (Math.random() - 0.5) * 10
      };
      
      // 应用变化
      for (const emotion in this.emotions) {
        // 确保情绪值在合理范围内 (5%-95%)
        this.emotions[emotion] = Math.max(5, Math.min(95, 
          this.emotions[emotion] + baseChange[emotion] + fluctuation[emotion]
        ));
      }
      
      // 回答问题时的额外调整
      if (this.isAnswering) {
        // 回答问题时紧张度下降更快，自信度增加更快
        this.emotions.confidence = Math.min(95, this.emotions.confidence + 0.2);
        this.emotions.nervousness = Math.max(5, this.emotions.nervousness - 0.3);
      }
      
      // 更新反馈提示
      this.updateFeedbackTip();
    },
    
    // 更新反馈提示
    updateFeedbackTip() {
      const tips = [
        "您保持逻辑清晰做得很好。在解释概念时可以尝试结合具体项目经验。",
        "回答时语速适中，这有助于面试官理解您的思路。",
         "在交流过程中保持积极、自信的态度，有助于建立良好印象。",
        "保持语速适中、语气平稳，有助于传达清晰的信息。",
        "您的回答展示了扎实的基础知识，可以进一步展示您解决问题的能力。"
      ];
      
      this.feedbackTip = tips[Math.floor(Math.random() * tips.length)];
    },
    
    // 更新AI状态
    updateAiStatus() {
      if (!this.isInterviewActive) return;
      
      if (this.isAnswering) {
        this.aiStatus = '分析中';
        this.aiSubStatus = '正在分析您的回答';
      } else {
        this.aiStatus = '等待回答';
        this.aiSubStatus = '请点击开始回答按钮';
      }
      
      // 进度模拟
      this.listeningProgress = 80 + Math.floor(Math.random() * 15);
      this.processingProgress = 45 + Math.floor(Math.random() * 20);
      this.responseProgress = 15 + Math.floor(Math.random() * 25);
      
      // 更新反馈列表
      this.feedbackList = [
        { 
          type: 'green', 
          icon: 'fas fa-check', 
          text: "上一个回答中很好地使用了具体例子说明" 
        },
        { 
          type: 'yellow', 
          icon: 'fas fa-exclamation', 
          text: "尝试减少技术术语堆砌，确保解释清晰易懂" 
        },
        { 
          type: 'blue', 
          icon: 'fas fa-info-circle', 
          text: "您的技术术语使用准确率：92%" 
        }
      ];
    },
    
    // 停止录音
    stopRecording() {
      // 1. 立即停止数据处理
      this.isInterviewActive = false;
      this.isRecording = false;
      this.isAnswering = false;
      
      // 2. 清除音频处理器
      if (this.recorder) {
        // 移除事件监听器
        this.recorder.onaudioprocess = null;
        this.recorder.disconnect();
        this.recorder = null;
      }
      
      // 3. 释放媒体资源
      if (this.stream) {
        this.stream.getTracks().forEach(track => track.stop());
        this.stream = null;
      }
      
      // 4. 关闭音频上下文
      if (this.audioContext) {
        this.audioContext.close().then(() => {
          console.log('音频上下文已关闭');
          this.audioContext = null;
        });
      }
      
      // 5. 更新UI状态
      this.interviewStatus = '已结束';
      this.interviewStatusTime = '已完成';
      this.aiStatus = '离线';
      this.aiSubStatus = '面试结束';
      
      // 6. 清理定时器
      if (this.timerInterval) {
        clearInterval(this.timerInterval);
      }
      
      // 7. 清理倒计时
      if (this.countdownInterval) {
        clearInterval(this.countdownInterval);
        this.countdownInterval = null;
      }
    },
    
    // 结束面试
    stopInterview() {
      // 发送结束消息
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.sendControlMessage('stop');
        this.ws.close();
      }
      
      // 停止录音
      this.stopRecording();
      
      // 生成面试总结
      this.generateSummary();
      
      // 显示总结报告
      this.showSummary = true;
    },
    
    // 生成面试总结
    generateSummary() {
      const currentDate = new Date();
      const durationMinutes = (this.timer / 60).toFixed(1);
      
      // 计算平均回答时间（秒）
      const totalAnswerTime = (this.javaQuestions.length * 120) - this.countdown;
      const avgAnswerTime = totalAnswerTime / (this.currentQuestionIndex + 1);
      
      // 综合评分
      const overallScore = 7.0 + Math.random() * 2.5; // 7.0-9.5分
      
      // 生成总结内容
      this.interviewSummary = {
        date: `${currentDate.toLocaleDateString()} ${currentDate.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
        duration: durationMinutes,
        totalQuestions: this.javaQuestions.length,
        answeredQuestions: this.currentQuestionIndex + 1,
        avgAnswerTime: avgAnswerTime.toFixed(1),
        overallScore: overallScore.toFixed(1),
        confidence: this.emotions.confidence,
        engagement: this.emotions.engagement,
        nervousness: this.emotions.nervousness,
        enthusiasm: this.emotions.enthusiasm,
        feedback: "您对Java核心概念有较好的理解，尤其在多态性和集合框架方面表现突出。但在解释垃圾回收机制和线程安全实现细节时可以考虑增加更多技术深度。",
        strengths: [
          "对面向对象概念理解清晰",
          "能够结合实际项目经验解释技术点",
          "回答问题结构清晰有条理",
          "在压力下能保持冷静思考"
        ],
        weaknesses: [
          "部分高级话题可进一步深入",
          "在JVM优化方面需加强细节理解",
          "技术术语使用可更精确",
          "回答节奏可更平稳"
        ],
        tip: "建议加强对Java内存模型和并发工具包的学习，阅读《Java并发编程实战》相关内容并尝试在实际项目中应用这些知识。"
      };
    },
    
    // 开始/停止回答
    toggleAnswering() {
      if (this.isAnswering) {
        this.stopAnswering();
      } else {
        this.startAnswering();
      }
    },
    
    // 开始回答
    startAnswering() {
      if (!this.ws || this.ws.readyState !== WebSocket.OPEN) {
        alert('请先连接到WebSocket服务器');
        return;
      }
      
      this.isAnswering = true;
      this.aiStatus = '正在聆听';
      this.aiSubStatus = '请回答当前问题';
      
      console.log('开始回答');
    },
    
    // 停止回答（延迟4秒后更新问题）
    stopAnswering() {
      if (!this.isAnswering) return;
      
      this.isAnswering = false;
      this.aiStatus = '正在分析';
      this.aiSubStatus = '处理您的回答';
      
      // 模拟发送结束回答消息
      try {
        if (this.ws && this.ws.readyState === WebSocket.OPEN) {
          this.ws.send("-1"); // 结束回答标记
          console.log("已发送结束标记: -1");
        }
      } catch (error) {
        console.error('发送结束回答消息失败:', error);
      }
      
      // 4秒后切换到下一个问题
      setTimeout(() => {
        this.nextQuestion();
      }, 4000);
      
      console.log('停止回答');
    }
  },
  beforeDestroy() {
    // 组件销毁前清理资源
    this.stopInterview();
    
    // 清理重连定时器
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
    }
  }
};
</script>

<style scoped>
/* 面试卡片样式 */
.interview-card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  padding: 1.75rem;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.interview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.question-display {
  width: 100%;
  height: 300px;
  border-radius: 0.75rem;
  margin-bottom: 1.25rem;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #f3f4f6 25%, #e5e7eb 25%, #e5e7eb 50%, #f3f4f6 50%, #f3f4f6 75%, #e5e7eb 75%);
  background-size: 40px 40px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
}

.interview-placeholder {
  padding: 2rem;
}

.interview-icon {
  width: 5rem;
  height: 5rem;
  background-color: #dbeafe;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 1.5rem;
}

.interview-icon i {
  color: #3b82f6;
  font-size: 2.5rem;
}

.interview-progress {
  position: absolute;
  top: 15px;
  right: 15px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 10px;
  border-radius: 15px;
  font-size: 0.85rem;
}

.question-content {
  padding: 2rem;
  font-size: 1.25rem;
  line-height: 1.6;
  max-width: 90%;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.interview-controls {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
  margin-top: 1.5rem;
}

/* 其他样式保持不变 */
.interview-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 1rem;
  font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
  background-color: #f9fafb;
  color: #333;
  min-height: 100vh;
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
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.header-buttons {
  display: flex;
  gap: 1rem;
}

.btn {
  padding: 0.75rem 1.25rem;
  border-radius: 0.75rem;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  border: none;
  font-size: 0.95rem;
  outline: none;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.primary {
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: white;
}

.primary:hover:not(:disabled) {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(59, 130, 246, 0.3);
}

.secondary {
  background-color: white;
  border: 1px solid #d1d5db;
  color: #4b5563;
}

.secondary:hover:not(:disabled) {
  background-color: #f3f4f6;
  border-color: #9ca3af;
}

.success {
  background: linear-gradient(135deg, #10b981, #059669);
  color: white;
}

.success:hover:not(:disabled) {
  background: linear-gradient(135deg, #059669, #047857);
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(16, 185, 129, 0.3);
}

.danger {
  background: linear-gradient(135deg, #ef4444, #dc2626);
  color: white;
}

.danger:hover:not(:disabled) {
  background: linear-gradient(135deg, #dc2626, #b91c1c);
  transform: translateY(-2px);
  box-shadow: 0 6px 8px rgba(239, 68, 68, 0.3);
}

.grid-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 1.5rem;
}

.left-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.right-column {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.progress-card, .emotion-card, .question-card, .ai-status-card, .feedback-card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  padding: 1.75rem;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.progress-card:hover, .emotion-card:hover, .question-card:hover, .ai-status-card:hover, .feedback-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.status-indicator {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.875rem;
  color: #6b7280;
}

.status-dot {
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  background-color: #6b7280;
}

.status-dot.recording {
  background-color: #ef4444;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0% { opacity: 1; }
  50% { opacity: 0.4; }
  100% { opacity: 1; }
}

.recording-timer {
  text-align: center;
  font-size: 1.5rem;
  font-weight: 600;
  margin: 1rem 0;
  padding: 0.75rem;
  background-color: #f3f4f6;
  border-radius: 0.5rem;
  font-family: monospace;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ws-status {
  padding: 0.75rem;
  border-radius: 0.5rem;
  margin-top: 1rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-weight: 500;
}

.ws-connected {
  background-color: #dcfce7;
  color: #166534;
}

.ws-disconnected {
  background-color: #fee2e2;
  color: #b91c1c;
}

.start-button-container {
  display: flex;
  justify-content: center;
  margin-top: 1.25rem;
}

.sent-data-info {
  padding: 1rem;
  background-color: #f3f4f6;
  border-radius: 0.5rem;
  font-size: 0.9rem;
  margin-top: 1rem;
}

.sent-data-info p {
  margin: 0.5rem 0;
  display: flex;
  align-items: center;
}

.progress-card h2, .emotion-card h2, .question-card h2, .ai-status-card h2, .feedback-card h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 1.25rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.progress-list {
  display: grid;
  gap: 1.5rem;
}

.progress-item {
  display: flex;
  align-items: flex-start;
  gap: 1rem;
}

.step-icon {
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-weight: 600;
}

.completed {
  background-color: #dcfce7;
  color: #166534;
}

.active {
  background-color: #dbeafe;
  color: #1d4ed8;
  font-weight: 600;
}

.inactive {
  background-color: #f3f4f6;
  color: #9ca3af;
}

.step-title {
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.step-time {
  font-size: 0.875rem;
  color: #6b7280;
}

.progress-bar {
  width: 100%;
  height: 0.5rem;
  background-color: #e5e7eb;
  border-radius: 0.25rem;
  margin-top: 0.5rem;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(to right, #3b82f6, #60a5fa);
  border-radius: 0.25rem;
  transition: width 0.5s ease;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.status-badge {
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 500;
}

.blue {
  background-color: #dbeafe;
  color: #1d4ed8;
}

.emotion-metrics {
  display: grid;
  gap: 1rem;
  margin-bottom: 1rem;
}

.metric-item {
  margin-bottom: 0.75rem;
}

.metric-label {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #4b5563;
  margin-bottom: 0.25rem;
}

.emotion-bar {
  height: 0.375rem;
  background-color: #e5e7eb;
  border-radius: 0.25rem;
  overflow: hidden;
}

.emotion-fill {
  height: 100%;
  transition: width 1s ease;
}

.blue {
  background: linear-gradient(to right, #3b82f6, #60a5fa);
}

.green {
  background: linear-gradient(to right, #10b981, #34d399);
}

.yellow {
  background: linear-gradient(to right, #fbbf24, #f59e0b);
}

.purple {
  background: linear-gradient(to right, #8b5cf6, #a78bfa);
}

.feedback-tip {
  padding-top: 1.25rem;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.feedback-tip i {
  margin-top: 0.25rem;
}

.feedback-tip p {
  font-size: 0.875rem;
  color: #4b5563;
  flex: 1;
}

.yellow {
  color: #f59e0b;
}

.question-bubble {
  background-color: #f3f4f6;
  border-radius: 0.75rem;
  padding: 1.25rem;
  margin-bottom: 1.25rem;
  position: relative;
  box-shadow: inset 0 0 6px rgba(0, 0, 0, 0.03);
}

.question-bubble:after {
  content: '';
  position: absolute;
  left: -0.5rem;
  top: 1.5rem;
  width: 0;
  height: 0;
  border: 0.5rem solid transparent;
  border-right-color: #f3f4f6;
  border-left: 0;
}

.question-bubble p {
  color: #1f2937;
  line-height: 1.6;
  font-size: 1.05rem;
}

.question-info {
  display: flex;
  justify-content: space-between;
  font-size: 0.875rem;
  color: #6b7280;
  margin-bottom: 1.25rem;
}

.full-width {
  width: 100%;
}

.ai-status-card h2 {
  margin-bottom: 1.25rem;
}

.status-visual {
  position: relative;
  width: 10rem;
  height: 10rem;
  margin: 0 auto 1.5rem;
}

.status-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.status-text {
  font-size: 1.25rem;
  font-weight: 700;
  color: #1d4ed8;
  margin-bottom: 0.25rem;
}

.status-subtext {
  font-size: 0.85rem;
  color: #6b7280;
}

.status-ring {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.status-track {
  fill: none;
  stroke: #e5e7eb;
  stroke-width: 8;
}

.status-progress {
  fill: none;
  stroke: #3b82f6;
  stroke-width: 8;
  stroke-dasharray: 283;
  stroke-dashoffset: 283;
  stroke-linecap: round;
  transition: stroke-dashoffset 0.5s ease;
}

.process-indicators {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
}

.indicator {
  text-align: center;
}

.indicator-label {
  font-size: 0.75rem;
  color: #6b7280;
  margin-bottom: 0.25rem;
}

.indicator-bar {
  height: 0.25rem;
  background-color: #e5e7eb;
  border-radius: 0.125rem;
  overflow: hidden;
}

.indicator-fill {
  height: 100%;
  transition: width 0.5s ease;
}

.feedback-card h2 {
  margin-bottom: 1rem;
}

.feedback-items {
  display: grid;
  gap: 1rem;
}

.feedback-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.feedback-icon {
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 0.125rem;
}

.feedback-icon.green {
  background-color: #dcfce7;
  color: #166534;
  font-size: 0.9rem;
}

.feedback-icon.yellow {
  background-color: #fef9c3;
  color: #ca8a04;
  font-size: 0.9rem;
}

.feedback-icon.blue {
  background-color: #dbeafe;
  color: #1d4ed8;
  font-size: 0.9rem;
}

.flex-1 {
  flex: 1;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 1.5rem;
  }
  
  .header-buttons {
    width: 100%;
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .interview-controls {
    grid-template-columns: 1fr;
  }
  
  .status-visual {
    width: 8rem;
    height: 8rem;
  }
  
  .question-display {
    height: 250px;
  }
}
.summary-container {
  margin-top: 2rem;
  padding: 2rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
}

.summary-content {
  max-width: 1200px;
  margin: 0 auto;
}

.summary-card {
  background-color: white;
  border-radius: 0.75rem;
  padding: 2.5rem;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
}

.summary-header {
  display: flex;
  align-items: center;
  margin-bottom: 2rem;
  padding-bottom: 1.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.summary-icon {
  width: 5rem;
  height: 5rem;
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
  color: white;
  border-radius: 1rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.5rem;
  font-size: 2.5rem;
}

.summary-header h3 {
  font-size: 1.75rem;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 0.5rem;
}

.summary-header p {
  color: #6b7280;
  font-size: 1rem;
  margin: 0.25rem 0;
}

.performance-stats {
  display: flex;
  justify-content: space-between;
  margin: 2rem 0;
}

.stat-box {
  flex: 1;
  text-align: center;
  padding: 1.5rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
  margin: 0 0.5rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.03);
}

.stat-value {
  font-size: 2.5rem;
  font-weight: 700;
  color: #3b82f6;
  line-height: 1;
  margin-bottom: 0.5rem;
}

.stat-label {
  font-size: 1rem;
  color: #6b7280;
}

.emotion-overview {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
}

.emotion-overview h4 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #1f2937;
}

.emotion-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
}

.emotion-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1.25rem;
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.03);
}

.emotion-title {
  display: flex;
  align-items: center;
}

.emotion-title i {
  font-size: 1.5rem;
  margin-right: 1rem;
}

.emotion-title .blue { color: #3b82f6; }
.emotion-title .green { color: #10b981; }
.emotion-title .yellow { color: #f59e0b; }
.emotion-title .purple { color: #8b5cf6; }

.emotion-value {
  font-size: 1.5rem;
  font-weight: 700;
}

.feedback-section {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
}

.feedback-section h4 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #1f2937;
}

.feedback-content {
  background-color: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
  line-height: 1.7;
  font-size: 1.05rem;
}

.strengths-weaknesses {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.5rem;
  margin-top: 1.5rem;
}

.strengths, .weaknesses {
  background-color: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
}

.strengths h5, .weaknesses h5 {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 1rem;
  display: flex;
  align-items: center;
}

.strengths ul, .weaknesses ul {
  padding-left: 1.25rem;
}

.strengths li, .weaknesses li {
  margin-bottom: 0.75rem;
  position: relative;
  padding-left: 1.5rem;
}

.strengths li:before {
  content: "✓";
  position: absolute;
  left: 0;
  color: #10b981;
}

.weaknesses li:before {
  content: "!";
  position: absolute;
  left: 0;
  color: #f59e0b;
  font-weight: bold;
}

.tips-section {
  margin: 2rem 0;
  padding: 1.5rem;
  background-color: #f9fafb;
  border-radius: 0.75rem;
}

.tips-section h4 {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 1.5rem;
  color: #1f2937;
}

.tip {
  display: flex;
  background-color: white;
  padding: 1.5rem;
  border-radius: 0.75rem;
}

.tip-icon {
  width: 3rem;
  height: 3rem;
  background-color: #dbeafe;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 1.5rem;
  flex-shrink: 0;
}

.tip-icon i {
  color: #3b82f6;
  font-size: 1.5rem;
}

.tip p {
  margin: 0;
  font-size: 1.1rem;
  line-height: 1.7;
}

.actions-section {
  display: flex;
  justify-content: center;
  gap: 1.5rem;
  margin-top: 2rem;
}

/* 确保情绪分析显示4位小数 */
.metric-label span:last-child {
  font-family: monospace;
}

@media (max-width: 768px) {
  .performance-stats, .emotion-grid, .strengths-weaknesses {
    grid-template-columns: 1fr;
  }
  
  .stat-box {
    margin: 0.5rem 0;
  }
}
</style>