<template>
  <div class="front-container">
    <div class="sse-demo-box">
      <div class="message-container">
        <div class="message-box" ref="messageBox">
          <div class="messages-wrapper">
            <template v-if="messageList.length === 0">
              <div class="welcome-message">
                Hi~ 我是AI,你身边的智能助手(模型:deepseek-r1-250120)，可以为你答疑解惑、助你学习、聊天，提升工作效率！
              </div>
            </template>
            <template v-for="(item, index) in messageList">
              <div v-if="item.type === 'answer'" :key="'a'+index" class="message-item answer">
                <div class="avatar">AI</div>
                <div class="content">
                  {{ item.content }}
                  <span v-if="item.isTyping" class="cursor"></span>
                </div>
              </div>
              <div v-else :key="'q'+index" class="message-item question">
                <div class="content">{{ item.content }}</div>
                <div class="avatar">我</div>
              </div>
            </template>
          </div>
        </div>
      </div>
      <div class="input-area">
        <div class="input-container">
          <el-input
            v-model="question"
            type="textarea"
            :rows="3"
            placeholder="有问题，尽管问，shift+enter换行"
            :disabled="isConnected"
            resize="none"
            @keyup.enter.native="handleEnter"
          />
        </div>
        <div class="button-container">
          <div class="left-buttons">
            <el-dropdown trigger="click" @command="handleModeChange">
              <el-button type="text">
                {{ currentMode }}
                <i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="deepseek">DeepSeek</el-dropdown-item>
                <el-dropdown-item command="local">Local</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button type="text">
              <i class="el-icon-reading"></i>
              深度思考
            </el-button>
            <el-button type="text">
              <i class="el-icon-search"></i>
              联网搜索
            </el-button>
          </div>
          <div class="right-buttons">
            <el-button
              type="primary"
              icon="el-icon-position"
              :disabled="isConnected || !question.trim()"
              @click="startSSE"
            ></el-button>
          </div>
        </div>
        <div class="footer-text">
          内容由AI生成，仅供参考，长内容需要思考，请耐心等待！
          <br><strong><a href="https://github.com/Litaibai-Geek/sse.luochengbin.com" target="_blank">GitHub源码以及开发过程详解</a></strong>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: "FrontSseDemo",
  data() {
    return {
      eventSource: null,
      isConnected: false,
      isTyping: false,
      receivedMessage: "",
      displayMessage: "",
      question: "",
      typingSpeed: 50,
      messageQueue: [],
      messageList: [],
      currentMode: "DeepSeek"
    };
  },
  methods: {
    handleModeChange(mode) {
      this.currentMode = mode === 'local' ? 'Local' : 'DeepSeek';
    },
    handleEnter(e) {
      // 如果按下shift键，则不发送
      if (e.shiftKey) {
        return;
      }
      // 阻止默认的换行行为
      e.preventDefault();
      // 如果可以发送，则发送消息
      if (!this.isConnected && this.question.trim()) {
        this.startSSE();
      }
    },
    startSSE() {
      if (!this.question.trim()) {
        this.$message.warning('请输入问题');
        return;
      }

      if (this.eventSource) {
        this.stopSSE();
      }

      // 添加问题到消息列表
      this.messageList.push({
        type: 'question',
        content: this.question.trim()
      });

      // 添加一个空的回答，用于显示打字效果
      const answerIndex = this.messageList.length;
      this.messageList.push({
        type: 'answer',
        content: '思考中...',
        isTyping: true
      });

      // 滚动到底部
      this.scrollToBottom();

      // 清空上一次的回答
      this.receivedMessage = "";
      this.displayMessage = "";
      this.messageQueue = [];

      // 创建 EventSource 实例
      const encodedQuestion = encodeURIComponent(this.question.trim());
      const timestamp = new Date().getTime();
      const mode = this.currentMode.toLowerCase();
      const url = `/prod-api/sse/stream?question=${encodedQuestion}&mode=${mode}&_t=${timestamp}`;
      this.eventSource = new EventSource(url);

      // 监听ready事件
      this.eventSource.addEventListener('ready', (event) => {
        console.log('SSE连接已就绪:', event.data);
        this.isConnected = true;
        // 收到ready事件后，清空"思考中..."
        this.messageList[answerIndex].content = '';
      });

      // 监听message事件
      this.eventSource.addEventListener('message', (event) => {
        try {
          const data = JSON.parse(event.data);
          if (!data.v) {
            return;
          }

          const text = data.v;
          this.receivedMessage += text;
          this.messageQueue.push(text);

          if (!this.isTyping) {
            this.processMessageQueue(answerIndex);
          }
        } catch (error) {
          console.warn('解析消息失败:', error);
        }
      });

      // 监听close事件
      this.eventSource.addEventListener('close', (event) => {
        console.log('服务器发送完成:', event.data);
        // 立即关闭 EventSource 连接
        if (this.eventSource) {
          this.eventSource.close();
          this.eventSource = null;
        }
        // 等待消息队列处理完成
        const checkComplete = setInterval(() => {
          if (!this.isTyping && this.messageQueue.length === 0) {
            clearInterval(checkComplete);
            this.isConnected = false;
            if (answerIndex !== undefined && this.messageList[answerIndex]) {
              this.messageList[answerIndex].isTyping = false;
            }
            // 清空输入框
            this.question = '';
          }
        }, 100);
      });

      // 监听错误
      this.eventSource.onerror = (error) => {
        console.error('SSE错误:', error);
        if (this.eventSource.readyState === EventSource.CLOSED) {
          if (this.messageQueue.length > 0 || this.isTyping) {
            console.log('继续处理剩余消息...');
            return;
          }
          this.finishSSE(answerIndex);
        }
      };
    },
    async processMessageQueue(answerIndex) {
      if (this.messageQueue.length === 0) {
        this.isTyping = false;
        return;
      }

      this.isTyping = true;
      const text = this.messageQueue.shift();

      for (let i = 0; i < text.length; i++) {
        this.messageList[answerIndex].content += text[i];
        const isPunctuation = /[，。！？、：；]/.test(text[i]);
        await this.sleep(isPunctuation ? this.typingSpeed / 2 : this.typingSpeed);
        // 每添加一个字符就滚动到底部
        this.scrollToBottom();
      }

      if (this.messageQueue.length > 0) {
        const nextText = this.messageQueue[0];
        if (nextText && !/^[，。！？、：；]/.test(nextText)) {
          this.messageList[answerIndex].content += ' ';
        }
      }

      this.processMessageQueue(answerIndex);
    },
    finishSSE(answerIndex) {
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
      if (answerIndex !== undefined && this.messageList[answerIndex]) {
        this.messageList[answerIndex].isTyping = false;
      }
      this.isConnected = false;
      this.isTyping = false;
      // 清空输入框
      this.question = '';
    },
    stopSSE() {
      if (this.eventSource) {
        this.eventSource.close();
        this.eventSource = null;
      }
      this.isConnected = false;
      this.isTyping = false;
      this.messageQueue = [];
      // 清空当前正在输入的内容
      this.receivedMessage = "";
      this.displayMessage = "";
    },
    sleep(ms) {
      return new Promise(resolve => setTimeout(resolve, ms));
    },
    // 添加滚动到底部的方法
    scrollToBottom() {
      this.$nextTick(() => {
        const messageBox = this.$refs.messageBox;
        if (messageBox) {
          messageBox.scrollTop = messageBox.scrollHeight;
        }
      });
    }
  },
  beforeDestroy() {
    this.stopSSE();
  },
  destroyed() {
    // 确保在组件完全销毁时清理所有资源
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
    this.messageList = [];
    this.question = "";
  }
};
</script>

<style scoped>
.front-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #fff;
  padding: 0;
}

.sse-demo-box {
  background-color: #fff;
  width: 100%;
  max-width: 800px;
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.message-container {
  flex: 1;
  background-color: #fff;
  overflow: hidden;
  margin-bottom: 0;
  border: none;
}

.message-box {
  height: 100%;
  overflow-y: auto;
  padding: 20px;
  -webkit-overflow-scrolling: touch;
}

.welcome-message {
  text-align: center;
  color: #666;
  padding: 20px;
  font-size: 14px;
  line-height: 1.6;
}

.messages-wrapper {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 20px;
  max-width: 100%;
}

.message-item .avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  flex-shrink: 0;
  background-color: #f4f4f4;
  color: #666;
}

.message-item .content {
  max-width: calc(100% - 100px);
  padding: 12px 16px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 12px;
  white-space: pre-wrap;
  word-break: break-all;
  background-color: #f4f4f4;
  color: #333;
}

.message-item.question {
  flex-direction: row-reverse;
}

.message-item.question .content {
  background-color: #e8f3ff;
  color: #333;
}

.input-area {
  background-color: #fff;
  border-top: 1px solid #eee;
  padding: 20px;
}

.input-container {
  margin-bottom: 12px;
}

.input-container .el-textarea__inner {
  resize: none;
  border-radius: 8px;
  padding: 12px;
  min-height: 60px !important;
  font-size: 14px;
}

.button-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.left-buttons {
  display: flex;
  gap: 16px;
  align-items: center;
}

.left-buttons .el-button {
  font-size: 14px;
  color: #666;
}

.right-buttons {
  display: flex;
  gap: 8px;
  align-items: center;
}

.right-buttons .el-button {
  padding: 8px;
}

.right-buttons .el-button--primary {
  background-color: #3370ff;
}

.footer-text {
  text-align: center;
  color: #999;
  font-size: 12px;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background-color: #333;
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: middle;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

/* 响应式设计 */
@media screen and (max-width: 768px) {
  .front-container {
    padding: 0;
  }

  .sse-demo-box {
    height: 100vh;
  }

  .input-area {
    padding: 12px;
  }

  .message-box {
    padding: 12px;
  }

  .left-buttons {
    gap: 8px;
  }

  .left-buttons .el-button {
    font-size: 12px;
  }

  .right-buttons .el-button {
    padding: 6px;
  }
}

/* 超小屏幕优化 */
@media screen and (max-width: 320px) {
  .input-area {
    padding: 8px;
  }

  .message-box {
    padding: 8px;
  }

  .left-buttons {
    gap: 4px;
  }

  .left-buttons .el-button {
    font-size: 12px;
    padding: 4px;
  }
}
</style>
