class VoiceChatApp {
    constructor() {
        this.stompClient = null;
        this.mediaRecorder = null;
        this.audioChunks = [];
        this.isRecording = false;
        this.isConnected = false;
        this.serverUrl = 'http://localhost:8082/j-chat';
        
        // 流式音频播放相关 - 使用MediaSource
        this.audioContext = null;
        this.mediaSource = null;
        this.sourceBuffer = null;
        this.audioElement = null;
        this.audioQueue = [];
        this.isPlaying = false;
        this.isStreamingAudio = false;
        this.pendingAudioData = [];

        this.initializeElements();
        this.setupEventListeners();
        this.requestMicrophonePermission();
        this.initializeMediaSource();
        this.loadAvatarOptions(); // 添加加载角色选项的调用
    }

    initializeElements() {
        this.elements = {
            avatarId: document.getElementById('avatarId'),
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

    // 添加加载角色选项的方法
    async loadAvatarOptions() {
        try {
            const response = await fetch(`${this.serverUrl}/admin/avatar/list`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            const avatars = await response.json();
            
            const avatarSelect = this.elements.avatarId;
            // 清空现有选项，保留默认选项
            avatarSelect.innerHTML = '<option value="">选择角色...</option>';
            
            // 添加角色选项
            avatars.forEach(avatar => {
                const option = document.createElement('option');
                option.value = avatar.id;
                option.textContent = `${avatar.name} (${avatar.category || '未分类'})`;
                avatarSelect.appendChild(option);
            });
            
            console.log('角色选项加载成功');
        } catch (error) {
            console.error('加载角色选项失败:', error);
            this.addMessage('系统', '加载角色选项失败，请检查网络连接', 'error');
        }
    }

    // 添加生成房间ID的方法
    generateRoomId() {
        const username = this.elements.username.value.trim();
        const avatarId = this.elements.avatarId.value.trim();

        if (username && avatarId) {
            const roomId = `${username}_${avatarId}`;

            this.elements.roomId.value = roomId;
            return roomId;
        } else {
            this.elements.roomId.value = '';
            return '';
        }
    }

    setupEventListeners() {
        this.elements.connectBtn.addEventListener('click', () => this.connect());
        this.elements.disconnectBtn.addEventListener('click', () => this.disconnect());
        this.elements.recordBtn.addEventListener('click', () => this.toggleRecording());
        this.elements.sendBtn.addEventListener('click', () => this.sendTextMessage());
        this.elements.stopAudioBtn = document.getElementById('stopAudioBtn');
        this.elements.enableAudioBtn = document.getElementById('enableAudioBtn');
        
        // 添加用户名和角色ID变化时自动生成房间ID的监听器
        this.elements.username.addEventListener('input', () => this.generateRoomId());
        this.elements.avatarId.addEventListener('change', () => this.generateRoomId());
        
        this.elements.stopAudioBtn.addEventListener('click', () => {
            this.stopCurrentAudio();
        });
    
        this.elements.enableAudioBtn.addEventListener('click', async () => {
            try {
                if (this.audioContext && this.audioContext.state === 'suspended') {
                    await this.audioContext.resume();
                    console.log('音频上下文已恢复');
                    this.addMessage('系统', '音频已启用', 'system');
                } else {
                    await this.initializeAudioContext();
                    console.log('音频上下文已初始化');
                    this.addMessage('系统', '音频已启用', 'system');
                }
            } catch (error) {
                console.error('启用音频失败:', error);
                this.addMessage('系统', '启用音频失败: ' + error.message, 'error');
            }
        });

        this.elements.messageInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.sendTextMessage();
            }
        });
    }

    // 新增：切换录音状态的方法
    toggleRecording() {
        if (this.isRecording) {
            this.stopRecording();
        } else {
            this.startRecording();
        }
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
        const avatarId = this.elements.avatarId.value.trim();
        
        if (!username) {
            alert('请输入用户名');
            return;
        }
        
        if (!avatarId) {
            alert('请选择角色');
            return;
        }
        
        // 生成房间ID
        const roomId = this.generateRoomId();
        
        if (!roomId) {
            alert('房间ID生成失败');
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
                sender: username,
                roomId: roomId,
                avatarId: avatarId

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
                roomId: this.elements.roomId.value.trim(),
                avatarId: this.elements.avatarId.value.trim() || null

            };

            console.log('发送语音消息到服务器:', this.serverUrl);
            this.stompClient.send('/app/voice', {}, JSON.stringify(message));

        } catch (error) {
            console.error('处理录音失败:', error);
            this.addMessage('系统', '处理录音失败: ' + error.message, 'error');
        }
    }

    sendTextMessage() {
        const username = this.elements.username.value.trim();
        const content = this.elements.messageInput.value.trim();
        if (!content) return;

        if (!this.isConnected) {
            this.addMessage('系统', '未连接到服务器，无法发送消息', 'error');
            return;
        }

        const message = {
            type: 'TEXT_MESSAGE',
            content: content,
            roomId: this.elements.roomId.value.trim(),
            sender: username,
            avatarId: this.elements.avatarId.value.trim() || null

        };

        console.log('发送文本消息到服务器:', this.serverUrl);
        this.stompClient.send('/app/voice', {}, JSON.stringify(message));
        this.elements.messageInput.value = '';
    }

    async initializeAudioContext() {
        try {
            this.audioContext = new (window.AudioContext || window.webkitAudioContext)({
                sampleRate: 24000, // 匹配后端音频采样率
                latencyHint: 'interactive' // 低延迟模式
            });
            console.log('音频上下文初始化成功，采样率:', this.audioContext.sampleRate);
        } catch (error) {
            console.error('音频上下文初始化失败:', error);
        }
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

            case 'AI_AUDIO_CHUNK':
                // 处理流式音频数据
                this.handleAudioChunk(message.audioData);
                break;

            case 'AI_AUDIO_END':
                // 音频流结束
                this.handleAudioEnd();
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

            case 'ERROR':
                this.handleErrorMessage(message);
                break;

            default:
                console.log('未知消息类型:', message.type);
        }
    }

    async initializeMediaSource() {
        try {
            // 创建音频元素
            if (!this.audioElement) {
                this.audioElement = document.createElement('audio');
                this.audioElement.controls = false;
                this.audioElement.autoplay = true;
                document.body.appendChild(this.audioElement);
            }

            // 检查MediaSource支持
            if ('MediaSource' in window) {
                this.mediaSource = new MediaSource();
                this.audioElement.src = URL.createObjectURL(this.mediaSource);
                
                this.mediaSource.addEventListener('sourceopen', () => {
                    console.log('MediaSource已打开');
                    try {
                        // 为MP3格式创建SourceBuffer
                        this.sourceBuffer = this.mediaSource.addSourceBuffer('audio/mpeg');
                        
                        this.sourceBuffer.addEventListener('updateend', () => {
                            // 处理待处理的音频数据
                            if (this.pendingAudioData.length > 0 && !this.sourceBuffer.updating) {
                                const nextData = this.pendingAudioData.shift();
                                this.sourceBuffer.appendBuffer(nextData);
                            }
                        });
                        
                        console.log('SourceBuffer已创建，支持MP3格式');
                    } catch (error) {
                        console.error('创建SourceBuffer失败:', error);
                        this.fallbackToAudioElement();
                    }
                });
                
                this.mediaSource.addEventListener('sourceended', () => {
                    console.log('MediaSource流结束');
                });
                
            } else {
                console.warn('浏览器不支持MediaSource，使用备用方案');
                this.fallbackToAudioElement();
            }
        } catch (error) {
            console.error('初始化MediaSource失败:', error);
            this.fallbackToAudioElement();
        }
    }

    fallbackToAudioElement() {
        // 备用方案：使用传统Audio元素
        this.mediaSource = null;
        this.sourceBuffer = null;
        console.log('使用传统Audio元素作为备用播放方案');
    }

    async handleAudioChunk(base64Audio) {
        try {
            console.log('处理MP3音频块，长度:', base64Audio.length);
            
            // 如果这是新音频流的第一个块，先停止当前播放的音频
            if (!this.isStreamingAudio) {
                this.stopCurrentAudio();
            }
            
            // 将Base64转换为Uint8Array
            const binaryString = atob(base64Audio);
            const bytes = new Uint8Array(binaryString.length);
            for (let i = 0; i < binaryString.length; i++) {
                bytes[i] = binaryString.charCodeAt(i);
            }

            // 使用MediaSource进行流式播放
            if (this.mediaSource && this.sourceBuffer) {
                if (!this.isStreamingAudio) {
                    this.isStreamingAudio = true;
                    console.log('开始MP3流式播放');
                }

                // 如果SourceBuffer正在更新，将数据加入队列
                if (this.sourceBuffer.updating) {
                    this.pendingAudioData.push(bytes.buffer);
                } else {
                    try {
                        this.sourceBuffer.appendBuffer(bytes.buffer);
                    } catch (error) {
                        console.error('添加音频数据到SourceBuffer失败:', error);
                        // 降级到传统播放方式
                        this.fallbackPlayAudio(base64Audio, 'mp3');
                    }
                }
            } else {
                // 使用备用播放方案
                this.fallbackPlayAudio(base64Audio, 'mp3');
            }

        } catch (error) {
            console.error('处理MP3音频块失败:', error);
            this.fallbackPlayAudio(base64Audio, 'mp3');
        }
    }

    handleAudioEnd() {
        console.log('音频流结束');
        this.isStreamingAudio = false;

        // 清理待处理的音频数据队列
        this.pendingAudioData = [];

        // 清理音频队列
        this.audioQueue = [];

        // 结束MediaSource流
        if (this.mediaSource && this.mediaSource.readyState === 'open') {
            try {
                // 等待所有待处理数据完成
                if (this.sourceBuffer && !this.sourceBuffer.updating) {
                    this.mediaSource.endOfStream();
                } else if (this.sourceBuffer) {
                    // 如果还在更新，等待更新完成后结束流
                    this.sourceBuffer.addEventListener('updateend', () => {
                        if (this.mediaSource.readyState === 'open') {
                            this.mediaSource.endOfStream();
                        }
                    }, { once: true });
                }
            } catch (error) {
                console.error('结束MediaSource流失败:', error);
            }
        }
    }


    stopCurrentAudio() {
        console.log('停止当前音频播放');
        
        // 停止MediaSource播放
        if (this.audioElement) {
            this.audioElement.pause();
            this.audioElement.currentTime = 0;
        }
        
        // 重置MediaSource
        if (this.mediaSource && this.mediaSource.readyState === 'open') {
            try {
                if (this.sourceBuffer && !this.sourceBuffer.updating) {
                    this.sourceBuffer.abort();
                }
                this.mediaSource.endOfStream();
            } catch (error) {
                console.error('停止MediaSource失败:', error);
            }
        }
        
        // 清理状态
        this.isStreamingAudio = false;
        this.isPlaying = false;
        this.pendingAudioData = [];
        
        // 重新初始化MediaSource以备下次使用
        setTimeout(() => {
            this.initializeMediaSource();
        }, 100);
    }

    // 改进的备用播放方法，支持MP3格式
    async fallbackPlayAudio(base64Audio, format) {
        try {
            console.log('使用备用MP3播放方法');
            
            // 创建新的音频元素用于备用播放
            const audio = new Audio();
            audio.preload = 'auto';
            audio.volume = 0.8;
            
            const binaryString = atob(base64Audio);
            const bytes = new Uint8Array(binaryString.length);
            for (let i = 0; i < binaryString.length; i++) {
                bytes[i] = binaryString.charCodeAt(i);
            }
            
            const blob = new Blob([bytes], { type: `audio/${format}` });
            const audioUrl = URL.createObjectURL(blob);
            
            audio.src = audioUrl;
            
            // 预加载并播放
            await new Promise((resolve, reject) => {
                audio.addEventListener('canplaythrough', resolve, { once: true });
                audio.addEventListener('error', reject, { once: true });
                audio.load();
            });
            
            const playPromise = audio.play();
            if (playPromise !== undefined) {
                playPromise.catch(error => {
                    console.error('MP3音频播放被阻止:', error);
                    this.addMessage('系统', '音频播放被浏览器阻止，请点击"启用音频"按钮', 'error');
                });
            }
            
            audio.onended = () => {
                URL.revokeObjectURL(audioUrl);
            };

        } catch (error) {
            console.error('备用MP3播放失败:', error);
            this.addMessage('系统', 'MP3音频播放失败: ' + error.message, 'error');
        }
    }

    async playAudio(base64Audio, format) {
        try {
            console.log('播放完整音频，格式:', format);
            
            // 停止当前的流式播放
            this.stopCurrentAudio();
            
            // 确保音频上下文已初始化
            if (!this.audioContext) {
                await this.initializeAudioContext();
            }

            // 如果音频上下文被暂停，恢复它
            if (this.audioContext.state === 'suspended') {
                await this.audioContext.resume();
            }

            // 使用Web Audio API播放完整音频
            const binaryString = atob(base64Audio);
            const bytes = new Uint8Array(binaryString.length);
            for (let i = 0; i < binaryString.length; i++) {
                bytes[i] = binaryString.charCodeAt(i);
            }

            const audioBuffer = await this.audioContext.decodeAudioData(bytes.buffer.slice());
            const source = this.audioContext.createBufferSource();
            
            // 添加增益节点
            const gainNode = this.audioContext.createGain();
            gainNode.gain.setValueAtTime(0.8, this.audioContext.currentTime);
            
            source.buffer = audioBuffer;
            source.connect(gainNode);
            gainNode.connect(this.audioContext.destination);
            
            source.start();
            this.currentSource = source;
            
            console.log('完整音频播放成功');

        } catch (error) {
            console.error('播放音频失败:', error);
            // 降级到传统播放方式
            this.fallbackPlayAudio(base64Audio, format);
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
        this.elements.recordBtn.disabled = !this.isConnected;

        if (this.isRecording) {
            this.elements.recordBtn.textContent = '⏹️ 发送';
            this.elements.recordBtn.style.background = '#4bae4c';
            this.elements.recordingStatus.textContent = '🔴 收集中...';
            this.elements.recordingStatus.className = 'recording-status recording';
        } else {
            this.elements.recordBtn.textContent = '🎤 发送语音';
            this.elements.recordBtn.style.background = '#4CAF50';
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
