<template>
  <div class="interview-container">
    <div class="header">
      <h1><i class="fas fa-robot"></i> AI模拟面试系统</h1>
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
        <div class="camera-card">
          <div class="camera-header">
            <h2><i class="fas fa-video"></i> 面试摄像头</h2>
            <div class="status-indicator">
              <span>{{ cameraStatus }}</span>
              <div class="status-dot" :class="{ recording: isInterviewActive }"></div>
            </div>
          </div>
          
          <div class="camera-preview">
            <video ref="videoFeed" autoplay playsinline muted></video>
            <div class="camera-placeholder" v-if="!isInterviewActive">
              <div class="camera-icon">
                <i class="fas fa-video"></i>
              </div>
              <p>面试开始后摄像头将启动</p>
            </div>
            <div v-else class="status-overlay">
              <i class="fas fa-circle"></i> 
              <span>{{ isRecording ? '录制中' : '暂停中' }}</span>
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
          
          <div class="camera-controls" v-if="isInterviewActive">
            <button class="btn secondary" @click="toggleMute">
              <i class="fas" :class="isMuted ? 'fa-microphone-slash' : 'fa-microphone'"></i> 
              {{ isMuted ? '取消静音' : '静音' }}
            </button>
            <button class="btn secondary" @click="toggleCamera">
              <i class="fas" :class="cameraEnabled ? 'fa-video' : 'fa-video-slash'"></i> 
              {{ cameraEnabled ? '关闭视频' : '开启视频' }}
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
                <span>{{ emotions.confidence }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill blue" :style="{ width: emotions.confidence + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>参与度</span>
                <span>{{ emotions.engagement }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill green" :style="{ width: emotions.engagement + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>紧张度</span>
                <span>{{ emotions.nervousness }}%</span>
              </div>
              <div class="emotion-bar">
                <div class="emotion-fill yellow" :style="{ width: emotions.nervousness + '%' }"></div>
              </div>
            </div>
            <div class="metric-item">
              <div class="metric-label">
                <span>积极性</span>
                <span>{{ emotions.enthusiasm }}%</span>
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
                {{ isInterviewActive ? '思考中' : '等待开始' }}
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
  </div>
</template>

<script>
export default {
  name: 'AIInterview',
  data() {
    return {
      isInterviewActive: false,
      isLoading: false,
      isRecording: true,
      cameraEnabled: true,
      isMuted: false,
      cameraStatus: '等待开始',
      wsStatus: 'disconnected',
      timer: 0,
      audioDataSent: 0,
      wsMessagesSent: 0,
      deviceSetupTime: '待完成',
      interviewStatusTime: '等待开始',
      progress: 0,
      
      // 情绪分析数据
      emotions: {
        confidence: 12,
        engagement: 18,
        nervousness: 72,
        enthusiasm: 26
      },
      
      // 当前问题信息
      currentQuestion: "请点击'开始面试'按钮启动AI模拟面试",
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
          text: "请确保摄像头和麦克风已连接并开启" 
        }
      ],
      
      // 媒体与WebSocket对象
      mediaStream: null,
      audioContext: null,
      processor: null,
      ws: null,
      audioDataChunks: [],
      timerInterval: null,
      sendInterval: null,
      
      // WebSocket配置
      wsEndpoint: 'wss://ai-interview-service.example.com/ws', // 替换为您的WebSocket服务器地址
      sampleRate: 16000,
      bufferSize: 4096,
      lastQuestion: ""
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
    // 格式化时间显示
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
        // 请求摄像头和麦克风权限
        this.mediaStream = await navigator.mediaDevices.getUserMedia({
          video: true,
          audio: true
        });
        
        // 启动视频流
        this.$refs.videoFeed.srcObject = this.mediaStream;
        this.$refs.videoFeed.style.display = 'block';
        this.cameraEnabled = true;
        
        // 连接WebSocket
        this.connectWebSocket();
        
      } catch (error) {
        console.error('启动面试失败:', error);
        this.isLoading = false;
        this.deviceSetupTime = '失败';
        alert('无法访问摄像头和麦克风。请确保已授予相应权限。');
      }
    },
    
    // 连接WebSocket
    connectWebSocket() {
      try {
        this.ws = new WebSocket(this.wsEndpoint);
        this.ws.binaryType = 'arraybuffer';
        this.wsStatus = 'connecting';
        
        // WebSocket事件处理
        this.ws.onopen = () => {
          console.log('WebSocket连接已建立');
          this.wsStatus = 'connected';
          this.wsMessagesSent = 0;
          this.audioDataSent = 0;
          
          // 初始化音频处理
          this.setupAudioProcessing();
          
          // 更新状态
          this.isInterviewActive = true;
          this.isRecording = true;
          this.cameraStatus = '直播中';
          this.isLoading = false;
          this.deviceSetupTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
          this.interviewStatusTime = '进行中';
          
          // 设置定时器
          this.startTimers();
          
          // 发送开始消息
          this.sendControlMessage('start');
        };
        
        this.ws.onmessage = (event) => {
          if (typeof event.data === 'string') {
            // 处理文本消息（新问题）
            this.handleQuestionMessage(event.data);
          } else {
            // 处理二进制消息（这里不处理音频）
            console.log('收到二进制数据:', event.data.byteLength, 'bytes');
          }
        };
        
        this.ws.onerror = (error) => {
          console.error('WebSocket错误:', error);
          this.wsStatus = 'error';
        };
        
        this.ws.onclose = () => {
          console.log('WebSocket连接已关闭');
          this.wsStatus = 'disconnected';
          if (this.isInterviewActive) {
            this.stopInterview();
          }
        };
        
      } catch (error) {
        console.error('WebSocket连接失败:', error);
        this.wsStatus = 'error';
        this.isLoading = false;
      }
    },
    
    // 设置音频处理
    async setupAudioProcessing() {
      try {
        // 创建音频上下文
        this.audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const source = this.audioContext.createMediaStreamSource(this.mediaStream);
        
        // 创建脚本处理器处理原始音频数据
        this.processor = this.audioContext.createScriptProcessor(this.bufferSize, 1, 1);
        source.connect(this.processor);
        this.processor.connect(this.audioContext.destination);
        
        // 处理音频数据
        this.processor.onaudioprocess = (event) => {
          if (!this.isInterviewActive || !this.isRecording || this.wsStatus !== 'connected') return;
          
          // 获取PCM数据
          const inputData = event.inputBuffer.getChannelData(0);
          const pcmData = this.convertFloat32ToInt16(inputData);
          
          // 存储数据
          this.audioDataChunks.push(pcmData);
        };
        
      } catch (error) {
        console.error('音频处理初始化失败:', error);
      }
    },
    
    // 开始计时器
    startTimers() {
      // 主计时器
      this.timerInterval = setInterval(() => {
        if (this.isInterviewActive) {
          this.timer++;
          
          // 更新进度
          this.progress = Math.min(100, this.timer / 2);
          
          // 更新情绪数据（模拟）
          this.updateEmotions();
          
          // 更新AI进度
          this.updateAiStatus();
        }
      }, 1000);
      
      // 数据发送计时器
      this.sendInterval = setInterval(() => {
        if (this.isInterviewActive && this.isRecording && this.wsStatus === 'connected') {
          this.sendAudioData();
        }
      }, 200);
    },
    
    // 发送音频数据
    sendAudioData() {
      if (this.audioDataChunks.length === 0 || !this.ws) return;
      
      try {
        // 合并所有音频块
        const totalLength = this.audioDataChunks.reduce((acc, chunk) => acc + chunk.byteLength, 0);
        const combinedBuffer = new Int16Array(totalLength / 2);
        
        let offset = 0;
        this.audioDataChunks.forEach(chunk => {
          combinedBuffer.set(new Int16Array(chunk), offset);
          offset += chunk.byteLength / 2;
        });
        
        // 发送数据
        this.ws.send(combinedBuffer.buffer);
        
        // 更新统计数据
        this.audioDataSent += Math.round(totalLength / 1024);
        this.wsMessagesSent++;
        
        // 清空缓存
        this.audioDataChunks = [];
        
      } catch (error) {
        console.error('发送音频数据失败:', error);
      }
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
    
    // 更新情绪分析数据
    updateEmotions() {
      // 这些数值仅用于演示，实际应用中应由真实AI分析提供
      this.emotions.confidence = Math.min(95, this.emotions.confidence + Math.random() * 2);
      this.emotions.engagement = Math.min(98, this.emotions.engagement + Math.random() * 1.5);
      this.emotions.nervousness = Math.max(5, this.emotions.nervousness - Math.random() * 2);
      this.emotions.enthusiasm = Math.min(90, this.emotions.enthusiasm + Math.random() * 1.8);
    },
    
    // 更新AI状态
    updateAiStatus() {
      this.aiStatus = '聆听中';
      this.aiSubStatus = '分析回答';
      
      // 进度模拟
      this.listeningProgress = 80 + Math.floor(Math.random() * 15);
      this.processingProgress = 45 + Math.floor(Math.random() * 20);
      this.responseProgress = 15 + Math.floor(Math.random() * 25);
      
      // 更新反馈
      this.feedbackTip = "您保持眼神交流做得很好。在讨论成就时尝试多微笑。";
      
      // 更新反馈列表
      this.feedbackList = [
        { 
          type: 'green', 
          icon: 'fas fa-check', 
          text: "上一个回答中很好地使用了STAR方法" 
        },
        { 
          type: 'yellow', 
          icon: 'fas fa-exclamation', 
          text: "尝试减少填充词('嗯'、'那个')的使用" 
        },
        { 
          type: 'blue', 
          icon: 'fas fa-info-circle', 
          text: "您的语速是148词/分钟（理想范围为120-160词/分钟）" 
        }
      ];
    },
    
    // 结束面试
    stopInterview() {
      // 清理定时器
      clearInterval(this.timerInterval);
      clearInterval(this.sendInterval);
      
      // 发送结束消息
      if (this.ws && this.ws.readyState === WebSocket.OPEN) {
        this.sendControlMessage('stop');
        this.ws.close();
      }
      
      // 停止媒体流
      if (this.mediaStream) {
        this.mediaStream.getTracks().forEach(track => track.stop());
      }
      
      // 断开音频处理器
      if (this.processor) {
        this.processor.disconnect();
      }
      
      // 重置状态
      this.isInterviewActive = false;
      this.isRecording = false;
      this.cameraStatus = '已结束';
      this.interviewStatusTime = '已完成';
      this.aiStatus = '离线';
      this.aiSubStatus = '面试结束';
      
      // 显示提示
      alert('面试已结束！数据已保存');
    },
    
    // 切换静音状态
    toggleMute() {
      this.isMuted = !this.isMuted;
      if (this.mediaStream) {
        this.mediaStream.getAudioTracks().forEach(track => {
          track.enabled = !this.isMuted;
        });
      }
    },
    
    // 切换摄像头状态
    toggleCamera() {
      this.cameraEnabled = !this.cameraEnabled;
      if (this.mediaStream) {
        this.mediaStream.getVideoTracks().forEach(track => {
          track.enabled = this.cameraEnabled;
        });
      }
      this.$refs.videoFeed.style.display = this.cameraEnabled ? 'block' : 'none';
    },
    
    // 将Float32音频数据转换为Int16 PCM格式
    convertFloat32ToInt16(buffer) {
      const length = buffer.length;
      const int16Buffer = new Int16Array(length);
      for (let i = 0; i < length; i++) {
        const s = Math.max(-1, Math.min(1, buffer[i]));
        int16Buffer[i] = s < 0 ? s * 0x8000 : s * 0x7FFF;
      }
      return int16Buffer;
    }
  },
  beforeDestroy() {
    // 组件销毁前清理资源
    if (this.isInterviewActive) {
      this.stopInterview();
    }
  }
};
</script>

<style scoped>
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

.camera-card, .progress-card, .emotion-card, .question-card, .ai-status-card, .feedback-card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  padding: 1.75rem;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.camera-card:hover, .progress-card:hover, .emotion-card:hover, .question-card:hover, .ai-status-card:hover, .feedback-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.camera-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.25rem;
}

.camera-header h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  display: flex;
  align-items: center;
  gap: 0.5rem;
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

.camera-preview {
  width: 100%;
  height: 0;
  padding-bottom: 56.25%;
  border-radius: 0.75rem;
  margin-bottom: 1.25rem;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #e5e7eb 25%, #f3f4f6 25%, #f3f4f6 50%, #e5e7eb 50%, #e5e7eb 75%, #f3f4f6 75%);
  background-size: 40px 40px;
  box-shadow: inset 0 0 10px rgba(0, 0, 0, 0.05);
}

video {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  background-color: #000;
  border-radius: 0.75rem;
  display: none;
}

.camera-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  color: #6b7280;
  padding: 1rem;
}

.camera-icon {
  width: 5rem;
  height: 5rem;
  background-color: #dbeafe;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.camera-icon i {
  color: #3b82f6;
  font-size: 2rem;
}

.status-overlay {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background-color: rgba(0,0,0,0.6);
  color: white;
  padding: 0.5rem 0.75rem;
  border-radius: 0.5rem;
  font-size: 0.8rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  z-index: 10;
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

.camera-controls {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.start-button-container {
  display: flex;
  justify-content: center;
  margin-top: 1.25rem;
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
  display: flex;
  flex-direction: column;
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
  
  .camera-controls {
    grid-template-columns: 1fr;
  }
  
  .status-visual {
    width: 8rem;
    height: 8rem;
  }
}
</style>