class VoiceChatApp {
    constructor() {
        this.stompClient = null;
        this.mediaRecorder = null;
        this.audioChunks = [];
        this.isRecording = false;
        this.isConnected = false;
        this.serverUrl = 'http://localhost:8080/j-chat'; // 明确指定后端服务器地址

        this.initializeElements();
        this.setupEventListeners();
        this.requestMicrophonePermission();
    }

    initializeElements() {
        this.elements = {
            username: document.getElementById('username'),
            roomId: document.getElementById('roomId'),
            connectBtn: document.getElementById('connectBtn'),
            disconnectBtn: document.getElementById('disconnectBtn'),
            recordBtn: document.getElementById('recordBtn'),
            stopBtn: document.getElementById('stopBtn'),
            messageInput: document.getElementById('messageInput'),
            sendBtn: document.getElementById('sendBtn'),
            messages: document.getElementById('messages'),
            recordingStatus: document.getElementById('recordingStatus'),
            voiceSelect: document.getElementById('voiceSelect')
        };
    }

    setupEventListeners() {
        this.elements.connectBtn.addEventListener('click', () => this.connect());
        this.elements.disconnectBtn.addEventListener('click', () => this.disconnect());
        this.elements.recordBtn.addEventListener('click', () => this.startRecording());
        this.elements.stopBtn.addEventListener('click', () => this.stopRecording());
        this.elements.sendBtn.addEventListener('click', () => this.sendTextMessage());

        this.elements.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendTextMessage();
            }
        });
    }

    async requestMicrophonePermission() {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            console.log('麦克风权限已获取');
            stream.getTracks().forEach(track => track.stop()); // 停止流，只是为了获取权限
        } catch (error) {
            console.error('无法获取麦克风权限:', error);
            this.addMessage('系统', '无法获取麦克风权限，语音功能将不可用', 'system');
        }
    }

    connect() {
        const username = this.elements.username.value.trim();
        const roomId = this.elements.roomId.value.trim();

        if (!username || !roomId) {
            alert('请输入用户名和房间ID');
            return;
        }

        // 使用完整的WebSocket URL，明确指定端口8080
        const socketUrl = `${this.serverUrl}/ws`;
        console.log('连接到WebSocket服务器:', socketUrl);

        const socket = new SockJS(socketUrl);
        this.stompClient = Stomp.over(socket);

        // 设置连接超时
        this.stompClient.heartbeat.outgoing = 20000;
        this.stompClient.heartbeat.incoming = 20000;

        this.stompClient.connect({}, (frame) => {
            console.log('WebSocket连接成功:', frame);
            this.isConnected = true;
            this.updateUI();

            // 订阅房间消息
            this.stompClient.subscribe(`/topic/room/${roomId}`, (message) => {
                this.handleIncomingMessage(JSON.parse(message.body));
            });

            // 订阅个人错误消息
            this.stompClient.subscribe('/user/queue/errors', (message) => {
                this.handleErrorMessage(JSON.parse(message.body));
            });

            // 发送加入房间消息
            this.stompClient.send('/app/join', {}, JSON.stringify({
                type: 'USER_JOIN',
                roomId: roomId
            }));

            this.addMessage('系统', `已连接到房间: ${roomId} (服务器: ${this.serverUrl})`, 'system');

        }, (error) => {
            console.error('WebSocket连接失败:', error);
            this.addMessage('系统', `连接失败: ${error}。请确保后端服务器在 ${this.serverUrl} 上运行`, 'error');
            this.isConnected = false;
            this.updateUI();
        });
    }

    disconnect() {
        if (this.stompClient && this.isConnected) {
            this.stompClient.disconnect(() => {
                console.log('WebSocket连接已断开');
                this.isConnected = false;
                this.updateUI();
                this.addMessage('系统', '已断开连接', 'system');
            });
        }
    }

    async startRecording() {
        try {
            const stream = await navigator.mediaDevices.getUserMedia({
                audio: {
                    sampleRate: 16000,
                    channelCount: 1,
                    echoCancellation: true,
                    noiseSuppression: true
                }
            });

            this.mediaRecorder = new MediaRecorder(stream, {
                mimeType: 'audio/webm;codecs=opus'
            });

            this.audioChunks = [];
            this.isRecording = true;

            this.mediaRecorder.ondataavailable = (event) => {
                if (event.data.size > 0) {
                    this.audioChunks.push(event.data);
                }
            };

            this.mediaRecorder.onstop = () => {
                this.processRecording();
            };

            this.mediaRecorder.start();
            this.updateRecordingUI();

        } catch (error) {
            console.error('开始录音失败:', error);
            this.addMessage('系统', '开始录音失败: ' + error.message, 'error');
        }
    }

    stopRecording() {
        if (this.mediaRecorder && this.isRecording) {
            this.mediaRecorder.stop();
            this.mediaRecorder.stream.getTracks().forEach(track => track.stop());
            this.isRecording = false;
            this.updateRecordingUI();
        }
    }

    async processRecording() {
        if (!this.isConnected) {
            this.addMessage('系统', '未连接到服务器，无法发送语音消息', 'error');
            return;
        }

        try {
            const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm;codecs=opus' });
            const arrayBuffer = await audioBlob.arrayBuffer();
            const base64Audio = this.arrayBufferToBase64(arrayBuffer);

            const message = {
                type: 'VOICE_DATA',
                audioData: base64Audio,
                audioFormat: 'webm',
                roomId: this.elements.roomId.value.trim()
            };

            console.log('发送语音消息到服务器:', this.serverUrl);
            this.stompClient.send('/app/voice', {}, JSON.stringify(message));
            this.addMessage('你', '🎤 发送了语音消息', 'user');

        } catch (error) {
            console.error('处理录音失败:', error);
            this.addMessage('系统', '处理录音失败: ' + error.message, 'error');
        }
    }

    sendTextMessage() {
        const content = this.elements.messageInput.value.trim();
        if (!content) return;

        if (!this.isConnected) {
            this.addMessage('系统', '未连接到服务器，无法发送消息', 'error');
            return;
        }

        const message = {
            type: 'TEXT_MESSAGE',
            content: content,
            roomId: this.elements.roomId.value.trim()
        };

        console.log('发送文本消息到服务器:', this.serverUrl);
        this.stompClient.send('/app/voice', {}, JSON.stringify(message));
        this.elements.messageInput.value = '';
    }

    handleIncomingMessage(message) {
        console.log('收到来自服务器的消息:', message);

        switch (message.type) {
            case 'TRANSCRIPTION':
                this.addMessage(message.sender, `📝 ${message.content}`, 'transcription');
                break;

            case 'AI_RESPONSE':
                this.addMessage(message.sender, message.content, 'ai');
                if (message.audioData) {
                    this.playAudio(message.audioData, message.audioFormat);
                }
                break;

            case 'TEXT_MESSAGE':
                this.addMessage(message.sender, message.content, 'text');
                break;

            case 'USER_JOIN':
                this.addMessage('系统', message.content, 'system');
                break;

            case 'USER_LEAVE':
                this.addMessage('系统', message.content, 'system');
                break;
        }
    }

    handleErrorMessage(message) {
        this.addMessage('错误', message.content, 'error');
    }

    async playAudio(base64Audio, format) {
        try {
            const audioData = this.base64ToArrayBuffer(base64Audio);
            const audioBlob = new Blob([audioData], { type: `audio/${format}` });
            const audioUrl = URL.createObjectURL(audioBlob);

            const audio = new Audio(audioUrl);
            audio.play();

            audio.onended = () => {
                URL.revokeObjectURL(audioUrl);
            };

        } catch (error) {
            console.error('播放音频失败:', error);
            this.addMessage('系统', '播放音频失败: ' + error.message, 'error');
        }
    }

    addMessage(sender, content, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `message ${type}`;

        const timestamp = new Date().toLocaleTimeString();
        messageDiv.innerHTML = `
            <div class="message-header">
                <span class="sender">${sender}</span>
                <span class="timestamp">${timestamp}</span>
            </div>
            <div class="message-content">${content}</div>
        `;

        this.elements.messages.appendChild(messageDiv);
        this.elements.messages.scrollTop = this.elements.messages.scrollHeight;
    }

    updateUI() {
        const connected = this.isConnected;

        this.elements.connectBtn.disabled = connected;
        this.elements.disconnectBtn.disabled = !connected;
        this.elements.recordBtn.disabled = !connected;
        this.elements.messageInput.disabled = !connected;
        this.elements.sendBtn.disabled = !connected;
        this.elements.username.disabled = connected;
        this.elements.roomId.disabled = connected;

        // 更新连接状态显示
        if (connected) {
            this.elements.connectBtn.textContent = '已连接';
            this.elements.connectBtn.style.background = '#4caf50';
        } else {
            this.elements.connectBtn.textContent = '连接';
            this.elements.connectBtn.style.background = '';
        }
    }

    updateRecordingUI() {
        this.elements.recordBtn.disabled = this.isRecording || !this.isConnected;
        this.elements.stopBtn.disabled = !this.isRecording;

        if (this.isRecording) {
            this.elements.recordingStatus.textContent = '🔴 录音中...';
            this.elements.recordingStatus.className = 'recording-status recording';
        } else {
            this.elements.recordingStatus.textContent = '';
            this.elements.recordingStatus.className = 'recording-status';
        }
    }

    arrayBufferToBase64(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = '';
        for (let i = 0; i < bytes.byteLength; i++) {
            binary += String.fromCharCode(bytes[i]);
        }
        return btoa(binary);
    }

    base64ToArrayBuffer(base64) {
        const binaryString = atob(base64);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }
        return bytes.buffer;
    }

    // 检查服务器连接状态
    checkServerConnection() {
        fetch(`${this.serverUrl}/actuator/health`)
            .then(response => {
                if (response.ok) {
                    console.log('服务器连接正常');
                } else {
                    console.warn('服务器响应异常');
                }
            })
            .catch(error => {
                console.error('无法连接到服务器:', error);
                this.addMessage('系统', `无法连接到服务器 ${this.serverUrl}，请检查服务器是否运行`, 'error');
            });
    }
}

// 初始化应用
document.addEventListener('DOMContentLoaded', () => {
    const app = new VoiceChatApp();

    // 页面加载时检查服务器连接
    setTimeout(() => {
        app.checkServerConnection();
    }, 1000);
});