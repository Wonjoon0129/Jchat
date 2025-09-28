// 管理后台JavaScript
class AdminApp {
    constructor() {
        this.baseUrl = 'http://localhost:8082/j-chat';
        this.currentSection = 'dashboard';
        // 音色列表数据
        this.voiceOptions = [
            { name: '龙呼呼', description: '天真烂漫女童', code: 'longhuhu', language: '中、英' },
            { name: '龙安培', description: '青少年教师女', code: 'longanpei', language: '中、英' },
            { name: '龙黛玉', description: '娇率才女音', code: 'longdaiyu', language: '中、英' },
            { name: '龙高僧', description: '得道高僧音', code: 'longgaoseng', language: '中、英' },
            { name: '龙应沐', description: '优雅知性女', code: 'longyingmu', language: '中、英' },
            { name: '龙应询', description: '年轻青涩男', code: 'longyingxun', language: '中、英' },
            { name: '龙应催', description: '严肃催收男', code: 'longyingcui', language: '中、英' },
            { name: '龙应答', description: '开朗高音女', code: 'longyingda', language: '中、英' },
            { name: '龙应静', description: '低调冷静女', code: 'longyingjing', language: '中、英' },
            { name: '龙应严', description: '义正严辞女', code: 'longyingyan', language: '中、英' },
            { name: '龙应甜', description: '温柔甜美女', code: 'longyingtian', language: '中、英' },
            { name: '龙应冰', description: '尖锐强势女', code: 'longyingbing', language: '中、英' },
            { name: '龙应桃', description: '温柔淡定女', code: 'longyingtao', language: '中、英' },
            { name: '龙应聆', description: '温和共情女', code: 'longyingling', language: '中、英' },
            { name: 'YUMI', description: '正经青年女', code: 'longyumi_v2', language: '中、英' },
            { name: '龙小淳', description: '知性积极女', code: 'longxiaochun_v2', language: '中、英' },
            { name: '龙小夏', description: '沉稳权威女', code: 'longxiaoxia_v2', language: '中、英' },
            { name: '龙安燃', description: '活泼质感女', code: 'longanran', language: '中、英' },
            { name: '龙安宣', description: '经典直播女', code: 'longanxuan', language: '中、英' },
            { name: '龙安冲', description: '激情推销男', code: 'longanchong', language: '中、英' },
            { name: '龙安萍', description: '高亢直播女', code: 'longanping', language: '中、英' },
            { name: '龙白芷', description: '睿气旁白女', code: 'longbaizhi', language: '中、英' },
            { name: '龙三叔', description: '沉稳质感男', code: 'longsanshu', language: '中、英' },
            { name: '龙修', description: '博才说书男', code: 'longxiu_v2', language: '中、英' },
            { name: '龙妙', description: '抑扬顿挫女', code: 'longmiao_v2', language: '中、英' },
            { name: '龙悦', description: '温暖磁性女', code: 'longyue_v2', language: '中、英' },
            { name: '龙楠', description: '睿智青年男', code: 'longnan_v2', language: '中、英' },
            { name: '龙媛', description: '温暖治愈女', code: 'longyuan_v2', language: '中、英' },
            { name: '龙安柔', description: '温柔闺蜜女', code: 'longanrou', language: '中、英' },
            { name: '龙嫱', description: '浪漫风情女', code: 'longqiang_v2', language: '中、英' },
            { name: '龙寒', description: '温暖痴情男', code: 'longhan_v2', language: '中、英' },
            { name: '龙星', description: '温婉邻家女', code: 'longxing_v2', language: '中、英' },
            { name: '龙华', description: '元气甜美女', code: 'longhua_v2', language: '中、英' },
            { name: '龙婉', description: '积极知性女', code: 'longwan_v2', language: '中、英' },
            { name: '龙橙', description: '智慧青年男', code: 'longcheng_v2', language: '中、英' },
            { name: '龙菲菲', description: '甜美娇气女', code: 'longfeifei_v2', language: '中、英' },
            { name: '龙小诚', description: '磁性低音男', code: 'longxiaocheng_v2', language: '中、英' },
            { name: '龙哲', description: '呆板大暖男', code: 'longzhe_v2', language: '中、英' },
            { name: '龙颜', description: '温暖春风女', code: 'longyan_v2', language: '中、英' },
            { name: '龙天', description: '磁性理智男', code: 'longtian_v2', language: '中、英' },
            { name: '龙泽', description: '温暖元气男', code: 'longze_v2', language: '中、英' },
            { name: '龙邵', description: '积极向上男', code: 'longshao_v2', language: '中、英' },
            { name: '龙浩', description: '多情忧郁男', code: 'longhao_v2', language: '中、英' },
            { name: '龙深', description: '实力歌手男', code: 'kabuleshen_v2', language: '中、英' },
            { name: '龙杰力豆', description: '阳光顽皮男', code: 'longjielidou_v2', language: '中、英' },
            { name: '龙铃', description: '稚气呆板女', code: 'longling_v2', language: '中、英' },
            { name: '龙可', description: '懵懂乖乖女', code: 'longke_v2', language: '中、英' },
            { name: '龙仙', description: '豪放可爱女', code: 'longxian_v2', language: '中、英' },
            { name: '龙老铁', description: '东北直率男', code: 'longlaotie_v2', language: '中（东北）、英' },
            { name: '龙嘉怡', description: '知性粤语女', code: 'longjiayi_v2', language: '中（粤语）、英' },
            { name: '龙桃', description: '积极粤语女', code: 'longtao_v2', language: '中（粤语）、英' },
            { name: '龙飞', description: '热血磁性男', code: 'longfei_v2', language: '中、英' },
            { name: '李白', description: '古代诗仙男', code: 'libai_v2', language: '中、英' },
            { name: '龙津', description: '优雅温润男', code: 'longjin_v2', language: '中、英' },
            { name: '龙书', description: '沉稳青年男', code: 'longshu_v2', language: '中、英' },
            { name: 'Bella2.0', description: '精准干练女', code: 'loongbella_v2', language: '中、英' },
            { name: '龙硕', description: '博才干练男', code: 'longshuo_v2', language: '中、英' },
            { name: '龙小白', description: '沉稳播报女', code: 'longxiaobai_v2', language: '中、英' },
            { name: '龙婧', description: '典型播音女', code: 'longjing_v2', language: '中、英' },
            { name: 'loongstella', description: '飒爽利落女', code: 'loongstella_v2', language: '中、英' },
            { name: 'loongeva', description: '知性英文女', code: 'loongeva_v2', language: '英式英文' },
            { name: 'loongbrian', description: '沉稳英文男', code: 'loongbrian_v2', language: '英式英文' },
            { name: 'loongluna', description: '英式英文女', code: 'loongluna_v2', language: '英式英文' },
            { name: 'loongluca', description: '英式英文男', code: 'loongluca_v2', language: '英式英文' },
            { name: 'loongemily', description: '英式英文女', code: 'loongemily_v2', language: '英式英文' },
            { name: 'loongeric', description: '英式英文男', code: 'loongeric_v2', language: '英式英文' },
            { name: 'loongabby', description: '美式英文女', code: 'loongabby_v2', language: '美式英文' },
            { name: 'loongannie', description: '美式英文女', code: 'loongannie_v2', language: '美式英文' },
            { name: 'loongandy', description: '美式英文男', code: 'loongandy_v2', language: '美式英文' },
            { name: 'loongava', description: '美式英文女', code: 'loongava_v2', language: '美式英文' },
            { name: 'loongbeth', description: '美式英文女', code: 'loongbeth_v2', language: '美式英文' },
            { name: 'loongbetty', description: '美式英文女', code: 'loongbetty_v2', language: '美式英文' },
            { name: 'loongcindy', description: '美式英文女', code: 'loongcindy_v2', language: '美式英文' },
            { name: 'loongcally', description: '美式英文女', code: 'loongcally_v2', language: '美式英文' },
            { name: 'loongdavid', description: '美式英文男', code: 'loongdavid_v2', language: '美式英文' },
            { name: 'loongdonna', description: '美式英文女', code: 'loongdonna_v2', language: '美式英文' },
            { name: 'loongkyong', description: '韩语女', code: 'loongkyong_v2', language: '韩语' },
            { name: 'loongtomoka', description: '日语女', code: 'loongtomoka_v2', language: '日语' },
            { name: 'loongtomoya', description: '日语男', code: 'loongtomoya_v2', language: '日语' }
        ];
        this.init();
    }

    init() {
        this.loadDashboard();
        this.setupEventListeners();
    }

    setupEventListeners() {
        // 页面加载完成后初始化
        document.addEventListener('DOMContentLoaded', () => {
            this.showSection('dashboard');
        });
    }

    // 显示指定的内容区域
    showSection(sectionName) {
        // 隐藏所有内容区域
        const sections = document.querySelectorAll('.content-section');
        sections.forEach(section => {
            section.style.display = 'none';
        });

        // 显示指定区域
        const targetSection = document.getElementById(sectionName);
        if (targetSection) {
            targetSection.style.display = 'block';
        }

        // 更新导航状态
        const navLinks = document.querySelectorAll('.nav-link');
        navLinks.forEach(link => {
            link.classList.remove('active');
        });
        
        const activeLink = document.querySelector(`[onclick="showSection('${sectionName}')"]`);
        if (activeLink) {
            activeLink.classList.add('active');
        }

        this.currentSection = sectionName;

        // 加载对应数据
        switch (sectionName) {
            case 'dashboard':
                this.loadDashboard();
                break;
            case 'users':
                this.loadUsers();
                break;
            case 'avatars':
                this.loadAvatars();
                break;
            case 'models':
                this.loadModels();
                break;
        }
    }

    // 加载仪表板数据
    async loadDashboard() {
        try {
            const [users, avatars, models] = await Promise.all([
                this.fetchData('/admin/user/list'),
                this.fetchData('/admin/avatar/list'),
                this.fetchData('/admin/model/list')
            ]);

            document.getElementById('userCount').textContent = users.length;
            document.getElementById('avatarCount').textContent = avatars.length;
            document.getElementById('modelCount').textContent = models.length;
        } catch (error) {
            console.error('加载仪表板数据失败:', error);
        }
    }

    // 加载用户数据
    async loadUsers() {
        try {
            const users = await this.fetchData('/admin/user/list');
            this.renderUserTable(users);
        } catch (error) {
            console.error('加载用户数据失败:', error);
            this.showError('加载用户数据失败');
        }
    }

    // 渲染用户表格
    renderUserTable(users) {
        const tbody = document.getElementById('userTableBody');
        tbody.innerHTML = '';

        users.forEach(user => {
            const row = document.createElement('tr');
            const sexText = user.sex === 1 ? '男' : user.sex === 2 ? '女' : '未设置';
            
            row.innerHTML = `
                <td>${user.id}</td>
                <td>${user.name || '-'}</td>
                <td>${user.uuid || '-'}</td>
                <td>${sexText}</td>
                <td>${user.old || '-'}</td>
                <td>${this.formatDate(user.createTime)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="adminApp.editUser(${user.id})">
                        <i class="bi bi-pencil"></i> 编辑
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="adminApp.deleteUser(${user.id})">
                        <i class="bi bi-trash"></i> 删除
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    // 加载Avatar数据
    async loadAvatars() {
        try {
            const avatars = await this.fetchData('/admin/avatar/list');
            this.renderAvatarTable(avatars);
        } catch (error) {
            console.error('加载Avatar数据失败:', error);
            this.showError('加载Avatar数据失败');
        }
    }

    // 渲染Avatar表格
    async renderAvatarTable(avatars) {
        const tbody = document.getElementById('avatarTableBody');
        tbody.innerHTML = '';

        // 获取所有模型信息用于显示模型名称
        let models = [];
        try {
            models = await this.fetchData('/admin/model/list');
        } catch (error) {
            console.error('获取模型列表失败:', error);
        }

        avatars.forEach(avatar => {
            const row = document.createElement('tr');
            
            // 查找对应的模型名称
            const model = models.find(m => m.id === avatar.modelId);
            const modelName = model ? model.modelName : `ID: ${avatar.modelId}`;
            
            // 查找对应的音色名称
            const voice = this.voiceOptions.find(v => v.code === avatar.voice);
            const voiceName = voice ? voice.name : avatar.voice || '-';
            
            row.innerHTML = `
                <td>${avatar.id}</td>
                <td>${avatar.name}</td>
                <td>${this.truncateText(avatar.description, 50)}</td>
                <td>${avatar.category || '-'}</td>
                <td>${modelName}</td>
                <td>${voiceName}</td>
                <td>${this.formatDate(avatar.createTime)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="adminApp.editAvatar(${avatar.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="adminApp.deleteAvatar(${avatar.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    // 加载模型数据
    async loadModels() {
        try {
            const models = await this.fetchData('/admin/model/list');
            this.renderModelTable(models);
        } catch (error) {
            console.error('加载模型数据失败:', error);
            this.showError('加载模型数据失败');
        }
    }

    // 渲染模型表格
    renderModelTable(models) {
        const tbody = document.getElementById('modelTableBody');
        tbody.innerHTML = '';

        models.forEach(model => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${model.id}</td>
                <td>${model.modelName || '-'}</td>
                <td>${this.truncateText(model.baseUrl, 30)}</td>
                <td>${this.maskApiKey(model.apiKey)}</td>
                <td>${model.completionsPath || '-'}</td>
                <td>${model.modelVersion || '-'}</td>
                <td>${this.formatDate(model.createTime)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="adminApp.editModel(${model.id})">
                        <i class="bi bi-pencil"></i> 编辑
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="adminApp.deleteModel(${model.id})">
                        <i class="bi bi-trash"></i> 删除
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    // 显示用户模态框
    showUserModal(user = null) {
        const modal = new bootstrap.Modal(document.getElementById('userModal'));
        const title = document.getElementById('userModalTitle');
        
        if (user) {
            title.textContent = '编辑用户';
            document.getElementById('userId').value = user.id;
            document.getElementById('userName').value = user.name || '';
            document.getElementById('userUuid').value = user.uuid || '';
            document.getElementById('userSex').value = user.sex || 0;
            document.getElementById('userAge').value = user.old || '';
        } else {
            title.textContent = '添加用户';
            document.getElementById('userForm').reset();
            document.getElementById('userId').value = '';
        }
        
        modal.show();
    }

    // 保存用户
    async saveUser() {
        const userId = document.getElementById('userId').value;
        const userData = {
            name: parseInt(document.getElementById('userName').value),
            uuid: document.getElementById('userUuid').value,
            sex: parseInt(document.getElementById('userSex').value),
            old: parseInt(document.getElementById('userAge').value) || null
        };

        try {
            if (userId) {
                await this.updateData(`/admin/user/${userId}`, userData);
                this.showSuccess('用户更新成功');
            } else {
                await this.createData('/admin/user', userData);
                this.showSuccess('用户创建成功');
            }
            
            bootstrap.Modal.getInstance(document.getElementById('userModal')).hide();
            this.loadUsers();
        } catch (error) {
            console.error('保存用户失败:', error);
            this.showError('保存用户失败');
        }
    }

    // 编辑用户
    async editUser(id) {
        try {
            const user = await this.fetchData(`/admin/user/${id}`);
            this.showUserModal(user);
        } catch (error) {
            console.error('获取用户信息失败:', error);
            this.showError('获取用户信息失败');
        }
    }

    // 删除用户
    async deleteUser(id) {
        if (confirm('确定要删除这个用户吗？')) {
            try {
                await this.deleteData(`/admin/user/${id}`);
                this.showSuccess('用户删除成功');
                this.loadUsers();
            } catch (error) {
                console.error('删除用户失败:', error);
                this.showError('删除用户失败');
            }
        }
    }

    // 显示Avatar模态框
    async showAvatarModal(avatar = null) {
        const modal = new bootstrap.Modal(document.getElementById('avatarModal'));
        const title = document.getElementById('avatarModalTitle');
        
        // 加载模型列表到下拉框
        await this.loadModelOptions();
        // 加载音色选项到下拉框
        this.loadVoiceOptions();
        
        if (avatar) {
            title.textContent = '编辑Avatar';
            document.getElementById('avatarId').value = avatar.id;
            document.getElementById('avatarName').value = avatar.name || '';
            document.getElementById('avatarDescription').value = avatar.description || '';
            document.getElementById('avatarCategory').value = avatar.category || '';
            document.getElementById('avatarModelId').value = avatar.modelId || '';
            document.getElementById('avatarVoice').value = avatar.voice || '';
            document.getElementById('avatarSystemPrompt').value = avatar.systemPrompt || '';
        } else {
            title.textContent = '添加Avatar';
            document.getElementById('avatarForm').reset();
            document.getElementById('avatarId').value = '';
        }
        
        modal.show();
    }

    // 加载音色选项到下拉框
    loadVoiceOptions() {
        const voiceSelect = document.getElementById('avatarVoice');
        
        // 清空现有选项，保留默认选项
        voiceSelect.innerHTML = '<option value="">请选择音色</option>';
        
        // 添加音色选项
        this.voiceOptions.forEach(voice => {
            const option = document.createElement('option');
            option.value = voice.code;
            option.textContent = `${voice.name} - ${voice.description} (${voice.language})`;
            voiceSelect.appendChild(option);
        });
    }

    // 渲染Avatar表格
    async renderAvatarTable(avatars) {
        const tbody = document.getElementById('avatarTableBody');
        tbody.innerHTML = '';

        // 获取所有模型信息用于显示模型名称
        let models = [];
        try {
            models = await this.fetchData('/admin/model/list');
        } catch (error) {
            console.error('获取模型列表失败:', error);
        }

        avatars.forEach(avatar => {
            const row = document.createElement('tr');
            
            // 查找对应的模型名称
            const model = models.find(m => m.id === avatar.modelId);
            const modelName = model ? model.modelName : `ID: ${avatar.modelId}`;
            
            // 查找对应的音色名称
            const voice = this.voiceOptions.find(v => v.code === avatar.voice);
            const voiceName = voice ? voice.name : avatar.voice || '-';
            
            row.innerHTML = `
                <td>${avatar.id}</td>
                <td>${avatar.name}</td>
                <td>${this.truncateText(avatar.description, 50)}</td>
                <td>${avatar.category || '-'}</td>
                <td>${modelName}</td>
                <td>${voiceName}</td>
                <td>${this.formatDate(avatar.createTime)}</td>
                <td>
                    <button class="btn btn-sm btn-outline-primary" onclick="adminApp.editAvatar(${avatar.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                    <button class="btn btn-sm btn-outline-danger" onclick="adminApp.deleteAvatar(${avatar.id})">
                        <i class="bi bi-trash"></i>
                    </button>
                </td>
            `;
            tbody.appendChild(row);
        });
    }

    // 加载模型选项到下拉框
    async loadModelOptions() {
        try {
            const models = await this.fetchData('/admin/model/list');
            const modelSelect = document.getElementById('avatarModelId');
            
            // 清空现有选项，保留默认选项
            modelSelect.innerHTML = '<option value="">请选择模型</option>';
            
            // 添加模型选项
            models.forEach(model => {
                const option = document.createElement('option');
                option.value = model.id;
                option.textContent = `${model.modelName} (${model.modelVersion || 'v1.0'})`;
                modelSelect.appendChild(option);
            });
        } catch (error) {
            console.error('加载模型选项失败:', error);
            this.showError('加载模型列表失败');
        }
    }

    // 保存Avatar
    async saveAvatar() {
        const avatarId = document.getElementById('avatarId').value;
        const avatarData = {
            name: document.getElementById('avatarName').value,
            description: document.getElementById('avatarDescription').value,
            category: document.getElementById('avatarCategory').value,
            modelId: parseInt(document.getElementById('avatarModelId').value) || null,
            voice: document.getElementById('avatarVoice').value,
            systemPrompt: document.getElementById('avatarSystemPrompt').value
        };

        try {
            if (avatarId) {
                await this.updateData(`/admin/avatar/${avatarId}`, avatarData);
                this.showSuccess('Avatar更新成功');
            } else {
                await this.createData('/admin/avatar', avatarData);
                this.showSuccess('Avatar创建成功');
            }
            
            bootstrap.Modal.getInstance(document.getElementById('avatarModal')).hide();
            this.loadAvatars();
        } catch (error) {
            console.error('保存Avatar失败:', error);
            this.showError('保存Avatar失败');
        }
    }

    // 编辑Avatar
    async editAvatar(id) {
        try {
            const avatar = await this.fetchData(`/admin/avatar/${id}`);
            this.showAvatarModal(avatar);
        } catch (error) {
            console.error('获取Avatar信息失败:', error);
            this.showError('获取Avatar信息失败');
        }
    }

    // 删除Avatar
    async deleteAvatar(id) {
        if (confirm('确定要删除这个Avatar吗？')) {
            try {
                await this.deleteData(`/admin/avatar/${id}`);
                this.showSuccess('Avatar删除成功');
                this.loadAvatars();
            } catch (error) {
                console.error('删除Avatar失败:', error);
                this.showError('删除Avatar失败');
            }
        }
    }

    // 显示模型模态框
    showModelModal(model = null) {
        const modal = new bootstrap.Modal(document.getElementById('modelModal'));
        const title = document.getElementById('modelModalTitle');
        
        if (model) {
            title.textContent = '编辑模型';
            document.getElementById('modelId').value = model.id;
            document.getElementById('modelName').value = model.modelName || '';
            document.getElementById('modelBaseUrl').value = model.baseUrl || '';
            document.getElementById('modelApiKey').value = model.apiKey || '';
            document.getElementById('modelCompletionsPath').value = model.completionsPath || '';
            document.getElementById('modelVersion').value = model.modelVersion || '';
        } else {
            title.textContent = '添加模型';
            document.getElementById('modelForm').reset();
            document.getElementById('modelId').value = '';
        }
        
        modal.show();
    }

    // 保存模型
    async saveModel() {
        const modelId = document.getElementById('modelId').value;
        const modelData = {
            modelName: document.getElementById('modelName').value,
            baseUrl: document.getElementById('modelBaseUrl').value,
            apiKey: document.getElementById('modelApiKey').value,
            completionsPath: document.getElementById('modelCompletionsPath').value,
            modelVersion: document.getElementById('modelVersion').value
        };

        try {
            if (modelId) {
                await this.updateData(`/admin/model/${modelId}`, modelData);
                this.showSuccess('模型更新成功');
            } else {
                await this.createData('/admin/model', modelData);
                this.showSuccess('模型创建成功');
            }
            
            bootstrap.Modal.getInstance(document.getElementById('modelModal')).hide();
            this.loadModels();
        } catch (error) {
            console.error('保存模型失败:', error);
            this.showError('保存模型失败');
        }
    }

    // 编辑模型
    async editModel(id) {
        try {
            const model = await this.fetchData(`/admin/model/${id}`);
            this.showModelModal(model);
        } catch (error) {
            console.error('获取模型信息失败:', error);
            this.showError('获取模型信息失败');
        }
    }

    // 删除模型
    async deleteModel(id) {
        if (confirm('确定要删除这个模型吗？')) {
            try {
                await this.deleteData(`/admin/model/${id}`);
                this.showSuccess('模型删除成功');
                this.loadModels();
            } catch (error) {
                console.error('删除模型失败:', error);
                this.showError('删除模型失败');
            }
        }
    }

    // HTTP请求方法
    async fetchData(url) {
        const response = await fetch(this.baseUrl + url);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    }

    async createData(url, data) {
        const response = await fetch(this.baseUrl + url, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.text();
    }

    async updateData(url, data) {
        const response = await fetch(this.baseUrl + url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(data)
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.text();
    }

    async deleteData(url) {
        const response = await fetch(this.baseUrl + url, {
            method: 'DELETE'
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.text();
    }

    // 工具方法
    formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleString('zh-CN');
    }

    truncateText(text, maxLength) {
        if (!text) return '-';
        if (text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
    }

    maskApiKey(apiKey) {
        if (!apiKey) return '-';
        if (apiKey.length <= 8) return apiKey;
        return apiKey.substring(0, 4) + '****' + apiKey.substring(apiKey.length - 4);
    }

    showSuccess(message) {
        this.showToast(message, 'success');
    }

    showError(message) {
        this.showToast(message, 'error');
    }

    showToast(message, type) {
        // 简单的提示实现
        const toast = document.createElement('div');
        toast.className = `alert alert-${type === 'success' ? 'success' : 'danger'} position-fixed`;
        toast.style.cssText = 'top: 20px; right: 20px; z-index: 9999; min-width: 300px;';
        toast.textContent = message;
        
        document.body.appendChild(toast);
        
        setTimeout(() => {
            toast.remove();
        }, 3000);
    }
}

// 全局变量
let adminApp;

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', () => {
    adminApp = new AdminApp();
});

// 全局函数（供HTML调用）
function showSection(sectionName) {
    if (adminApp) {
        adminApp.showSection(sectionName);
    }
}

function showUserModal() {
    if (adminApp) {
        adminApp.showUserModal();
    }
}

function saveUser() {
    if (adminApp) {
        adminApp.saveUser();
    }
}

function showAvatarModal() {
    if (adminApp) {
        adminApp.showAvatarModal();
    }
}

function saveAvatar() {
    if (adminApp) {
        adminApp.saveAvatar();
    }
}

function showModelModal() {
    if (adminApp) {
        adminApp.showModelModal();
    }
}

function saveModel() {
    if (adminApp) {
        adminApp.saveModel();
    }
}