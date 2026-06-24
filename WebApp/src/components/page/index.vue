<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div style="min-height: 85vh;">
            <el-tabs class="campus-category-tabs" v-model="labelName" type="card" @tab-click="handleClick">
                <el-tab-pane
                        v-for="category in categoryOptions"
                        :key="category.value"
                        :label="category.label"
                        :name="category.value">
                </el-tab-pane>
            </el-tabs>
            <div style="margin: 0 20px;">
                <transition name="list" mode="out-in">
                    <el-row :gutter="30" key="idleRow">
                        <el-col :span="6" v-for="(idle,index) in idleList">
                            <div class="idle-card hover-lift" @click="toDetails(idle)">
                                <div class="img-zoom">
                                    <el-image
                                            style="width: 100%; height: 160px"
                                            :src="idle.imgUrl"
                                            fit="contain">
                                        <div slot="error" class="img-error-placeholder">
                                            <i class="el-icon-picture-outline"></i>
                                            <span>暂无图片</span>
                                        </div>
                                    </el-image>
                                </div>
                                <div class="idle-title" :class="idle.idleStatus===2?'text-strikethrough':''">
                                    {{idle.idleName}}
                                    <span v-if="idle.idleStatus===2" class="offline-badge">已下架</span>
                                </div>
                                <el-row style="margin: 5px 10px;">
                                    <el-col :span="24">
                                        <div class="idle-prive" :class="idle.idleStatus===2?'text-strikethrough':''">￥{{idle.idlePrice}}</div>
                                    </el-col>
                                </el-row>
                                <div class="idle-time">{{idle.timeStr}}</div>
                                <div class="user-info">
                                    <el-image
                                            style="width: 30px; height: 30px"
                                            :src="idle.user.avatar"
                                            fit="contain">
                                        <div slot="error" class="img-error-placeholder" style="border-radius:50%;">
                                            <i class="el-icon-user" style="font-size:16px;margin:0;"></i>
                                        </div>
                                    </el-image>
                                    <div class="user-nickname">{{idle.user.nickname}}</div>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </transition>
            </div>
            <div class="fenye">
                <el-pagination
                        background
                        @current-change="handleCurrentChange"
                        :current-page.sync="currentPage"
                        :page-size="8"
                        layout="prev, pager, next, jumper"
                        :total="totalItem">
                </el-pagination>
            </div>
            </div>
            <button class="ai-chat-trigger" type="button" @click="toggleAiChat">
                <i class="el-icon-chat-dot-round"></i>
                <span>AI 交易助手</span>
            </button>
            <transition name="ai-chat">
                <section v-if="aiChatVisible" class="ai-chat-panel">
                    <header class="ai-chat-header">
                        <div>
                            <div class="ai-chat-title">
                                <i class="el-icon-chat-dot-round"></i>
                                AI 交易助手
                            </div>
                            <div class="ai-chat-status">
                                <span></span> 校园二手购买建议
                            </div>
                        </div>
                        <button type="button" class="ai-chat-close" @click="aiChatVisible=false">
                            <i class="el-icon-close"></i>
                        </button>
                    </header>

                    <div class="ai-product-selector">
                        <span>咨询商品</span>
                        <el-select
                                v-model="selectedProductId"
                                size="small"
                                placeholder="请先选择当前页商品"
                                @change="changeChatProduct">
                            <el-option
                                    v-for="idle in idleList"
                                    :key="idle.id"
                                    :label="idle.idleName+' · ¥'+idle.idlePrice"
                                    :value="idle.id">
                            </el-option>
                        </el-select>
                    </div>

                    <div ref="chatMessages" class="ai-chat-messages">
                        <div
                                v-for="(message,index) in chatMessages"
                                :key="index"
                                class="chat-message"
                                :class="'chat-message-'+message.role">
                            <div class="chat-avatar">
                                <i :class="message.role==='assistant'?'el-icon-cpu':'el-icon-user'"></i>
                            </div>
                            <div class="chat-bubble">
                                <div class="chat-text">{{message.text}}</div>
                                <div v-if="message.riskLevel" class="chat-risk">
                                    <el-tag size="mini" :type="riskTagType(message.riskLevel)">
                                        {{riskText(message.riskLevel)}}
                                    </el-tag>
                                </div>
                                <div v-if="message.sections" class="chat-sections">
                                    <div v-for="section in message.sections" :key="section.title">
                                        <strong>{{section.title}}</strong>
                                        <ul>
                                            <li v-for="(item,itemIndex) in section.items" :key="itemIndex">{{item}}</li>
                                        </ul>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div v-if="adviceLoading" class="chat-message chat-message-assistant">
                            <div class="chat-avatar"><i class="el-icon-cpu"></i></div>
                            <div class="chat-bubble chat-typing">
                                <span></span><span></span><span></span>
                            </div>
                        </div>
                    </div>

                    <div class="quick-questions">
                        <button
                                v-for="question in quickQuestions"
                                :key="question"
                                type="button"
                                @click="sendQuickQuestion(question)">
                            {{question}}
                        </button>
                    </div>

                    <div class="ai-chat-input">
                        <el-input
                                v-model.trim="chatInput"
                                maxlength="100"
                                placeholder="例如：这件商品值得买吗？"
                                @keyup.enter.native="sendChatMessage">
                        </el-input>
                        <el-button
                                type="primary"
                                icon="el-icon-position"
                                :loading="adviceLoading"
                                @click="sendChatMessage">
                        </el-button>
                    </div>
                    <div class="ai-chat-disclaimer">AI 建议仅供参考，请当面验货后再付款</div>
                </section>
            </transition>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script>
    import AppHead from '../common/AppHeader.vue';
    import AppBody from '../common/AppPageBody.vue'
    import AppFoot from '../common/AppFoot.vue'

    export default {
        name: "index",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data() {
            return {
                labelName: '0',
                categoryOptions: [
                    {value: '0', label: '全部'},
                    {value: '1', label: '数码产品'},
                    {value: '4', label: '教材书籍'},
                    {value: '2', label: '宿舍用品'},
                    {value: '6', label: '生活用品'},
                    {value: '3', label: '运动用品'},
                    {value: '5', label: '其他'}
                ],
                idleList: [],
                currentPage: 1,
                totalItem:1,
                aiChatVisible:false,
                selectedProductId:null,
                chatInput:'',
                adviceLoading:false,
                quickQuestions:['值得买吗？','验货看什么？','有哪些交易风险？'],
                chatMessages:[{
                    role:'assistant',
                    text:'你好，我可以帮你分析商品是否值得购买。请先选择当前页的一件商品，然后告诉我你想了解什么。'
                }]
            };
        },
        created() {
            this.findIdleTiem(1)
        },
        watch:{
            $route(to,from){
                this.labelName=to.query.labelName;
                let val=parseInt(to.query.page)?parseInt(to.query.page):1;
                this.currentPage=parseInt(to.query.page)?parseInt(to.query.page):1;
                this.findIdleTiem(val);
            }
        },
        methods: {
            findIdleTiem(page){
                const loading = this.$loading({
                    lock: true,
                    text: '加载数据中',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0)'
                });
                if(this.labelName>0){
                    this.$api.findIdleTiemByLable({
                        idleLabel:this.labelName,
                        page: page,
                        nums: 8
                    }).then(res => {
                        console.log(res);
                        let list = res.data.list;
                        for (let i = 0; i < list.length; i++) {
                            list[i].timeStr = list[i].releaseTime.substring(0, 10) + " " + list[i].releaseTime.substring(11, 19);
                            let pictureList = JSON.parse(list[i].pictureList);
                            list[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                        }
                        this.idleList = list;
                        this.totalItem=res.data.count;
                        console.log(this.totalItem);
                    }).catch(e => {
                        console.log(e)
                    }).finally(()=>{
                        loading.close();
                    })
                }else{
                    this.$api.findIdleTiem({
                        page: page,
                        nums: 8
                    }).then(res => {
                        console.log(res);
                        let list = res.data.list;
                        for (let i = 0; i < list.length; i++) {
                            list[i].timeStr = list[i].releaseTime.substring(0, 10) + " " + list[i].releaseTime.substring(11, 19);
                            let pictureList = JSON.parse(list[i].pictureList);
                            list[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                        }
                        this.idleList = list;
                        this.totalItem=res.data.count;
                        console.log(this.totalItem);
                    }).catch(e => {
                        console.log(e)
                    }).finally(()=>{
                        loading.close();
                    })
                }
            },
            handleClick(tab, event) {
                console.log(this.labelName);
                this.$router.replace({query: {page: 1,labelName:this.labelName}});
            },
            handleCurrentChange(val) {
                console.log(`当前页: ${val}`);
                this.$router.replace({query: {page: val,labelName:this.labelName}});
            },
            toggleAiChat() {
                this.aiChatVisible=!this.aiChatVisible;
                if (this.aiChatVisible) {
                    this.scrollChatToBottom();
                }
            },
            changeChatProduct() {
                const product=this.getSelectedProduct();
                if (!product) {
                    return;
                }
                this.chatMessages=[{
                    role:'assistant',
                    text:'已选择“'+product.idleName+'”，售价 ¥'+product.idlePrice+'。你可以问我它是否值得买、验货重点或交易风险。'
                }];
                this.scrollChatToBottom();
            },
            getSelectedProduct() {
                return this.idleList.find(item=>item.id===this.selectedProductId);
            },
            getCategoryName(label) {
                const category=this.categoryOptions.find(item=>Number(item.value)===Number(label));
                return category && category.value!=='0' ? category.label : '其他';
            },
            getPlainText(content) {
                if (!content) {
                    return '';
                }
                const container=document.createElement('div');
                container.innerHTML=content;
                return (container.textContent || container.innerText || '').trim();
            },
            sendQuickQuestion(question) {
                this.chatInput=question;
                this.sendChatMessage();
            },
            sendChatMessage() {
                const product=this.getSelectedProduct();
                const question=this.chatInput.trim();
                if (!product) {
                    this.$message.warning('请先选择要咨询的商品');
                    return;
                }
                if (!question || this.adviceLoading) {
                    return;
                }
                this.chatMessages.push({role:'user', text:question});
                this.chatInput='';
                this.adviceLoading=true;
                this.scrollChatToBottom();
                this.$api.getTradeAdvice({
                    productName:product.idleName || '',
                    category:this.getCategoryName(product.idleLabel),
                    description:this.getPlainText(product.idleDetails),
                    price:product.idlePrice || null,
                    originalPrice:null,
                    conditionLevel:'',
                    usedTime:'',
                    userRole:'BUYER',
                    userQuestion:question
                }).then(res=>{
                    if (res.status_code===1 && res.data) {
                        const data=res.data;
                        this.chatMessages.push({
                            role:'assistant',
                            text:data.advice || '请结合商品实物情况谨慎判断。',
                            riskLevel:data.riskLevel,
                            sections:[
                                {title:'验货重点', items:data.checkItems || []},
                                {title:'建议询问', items:data.questions || []},
                                {title:'安全提醒', items:data.safetyTips || []}
                            ].filter(section=>section.items.length)
                        });
                    } else {
                        this.addChatError();
                    }
                }).catch(()=>{
                    this.addChatError();
                }).finally(()=>{
                    this.adviceLoading=false;
                    this.scrollChatToBottom();
                });
            },
            addChatError() {
                this.chatMessages.push({
                    role:'assistant',
                    text:'暂时无法生成建议，请确认后端服务和 DeepSeek 配置后重试。'
                });
            },
            riskTagType(level) {
                return {LOW:'success', MEDIUM:'warning', HIGH:'danger'}[level] || 'info';
            },
            riskText(level) {
                return {LOW:'低风险', MEDIUM:'中风险', HIGH:'高风险'}[level] || '风险未知';
            },
            scrollChatToBottom() {
                this.$nextTick(()=>{
                    const container=this.$refs.chatMessages;
                    if (container) {
                        container.scrollTop=container.scrollHeight;
                    }
                });
            },
            toDetails(idle) {
                this.$router.push({path: '/details', query: {id: idle.id}});
            }
        }
    }
</script>

<style scoped>
    .idle-card {
        height: 300px;
        border: #eeeeee solid 1px;
        margin-bottom: 15px;
        cursor: pointer;
        border-radius: 6px;
        overflow: hidden;
        transition: transform 0.25s ease, box-shadow 0.25s ease, border-color 0.25s ease;
    }
    .idle-card:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 28px rgba(0, 0, 0, 0.10);
        border-color: #d0d0d0;
    }

    .campus-category-tabs /deep/ .el-tabs__nav {
        display: flex;
        width: 100%;
    }

    .campus-category-tabs /deep/ .el-tabs__item {
        flex: 1;
        min-width: 110px;
        padding: 0 12px;
        text-align: center;
    }

    .campus-category-tabs /deep/ .el-tabs__nav-scroll {
        overflow-x: auto;
    }

    .fenye {
        display: flex;
        justify-content: center;
        height: 60px;
        align-items: center;
    }

    .idle-title {
        font-size: 18px;
        font-weight: 600;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        margin: 10px;
    }

    .idle-prive {
        font-size: 16px;
        color: red;
    }

    .idle-time {
        color: #666666;
        font-size: 12px;
        margin: 0 10px;
    }

    .user-nickname {
        color: #999999;
        font-size: 12px;
        display: flex;
        align-items: center;
        height: 30px;
        padding-left: 10px;
    }

    .user-info {
        padding: 5px 10px;
        height: 30px;
        display: flex;
    }

    .ai-chat-trigger {
        position: fixed;
        right: 28px;
        bottom: 28px;
        z-index: 1001;
        display: flex;
        align-items: center;
        gap: 9px;
        height: 52px;
        padding: 0 20px;
        border: 0;
        border-radius: 26px;
        color: #fff;
        background: linear-gradient(135deg, #409eff, #1769d2);
        box-shadow: 0 10px 28px rgba(23, 105, 210, 0.35);
        cursor: pointer;
        font-size: 15px;
        font-weight: 600;
    }

    .ai-chat-trigger i {
        font-size: 21px;
    }

    .ai-chat-panel {
        position: fixed;
        right: 28px;
        bottom: 92px;
        z-index: 1000;
        width: 390px;
        height: calc(100vh - 170px);
        max-height: 610px;
        display: flex;
        flex-direction: column;
        overflow: hidden;
        border: 1px solid #e4e7ed;
        border-radius: 16px;
        background: #fff;
        box-shadow: 0 18px 55px rgba(0, 0, 0, 0.2);
    }

    .ai-chat-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 17px 18px;
        color: #fff;
        background: linear-gradient(135deg, #246fdb, #409eff);
    }

    .ai-chat-title {
        font-size: 17px;
        font-weight: 600;
    }

    .ai-chat-status {
        margin-top: 4px;
        font-size: 12px;
        opacity: 0.9;
    }

    .ai-chat-status span {
        display: inline-block;
        width: 7px;
        height: 7px;
        margin-right: 4px;
        border-radius: 50%;
        background: #7cff9b;
    }

    .ai-chat-close {
        border: 0;
        color: #fff;
        background: transparent;
        cursor: pointer;
        font-size: 20px;
    }

    .ai-product-selector {
        display: flex;
        align-items: center;
        gap: 10px;
        padding: 12px 14px;
        border-bottom: 1px solid #ebeef5;
        background: #fafcff;
        font-size: 13px;
    }

    .ai-product-selector > span {
        flex: none;
        color: #606266;
    }

    .ai-product-selector .el-select {
        width: 100%;
    }

    .ai-chat-messages {
        flex: 1;
        overflow-y: auto;
        padding: 16px 14px 8px;
        background: #f5f7fa;
    }

    .chat-message {
        display: flex;
        align-items: flex-start;
        gap: 8px;
        margin-bottom: 14px;
    }

    .chat-message-user {
        flex-direction: row-reverse;
    }

    .chat-avatar {
        flex: none;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        color: #fff;
        background: #409eff;
    }

    .chat-message-user .chat-avatar {
        background: #909399;
    }

    .chat-bubble {
        max-width: 285px;
        padding: 10px 12px;
        border-radius: 4px 13px 13px 13px;
        color: #303133;
        background: #fff;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
        font-size: 13px;
        line-height: 1.65;
    }

    .chat-message-user .chat-bubble {
        color: #fff;
        border-radius: 13px 4px 13px 13px;
        background: #409eff;
    }

    .chat-risk {
        margin-top: 8px;
    }

    .chat-sections {
        margin-top: 9px;
        padding-top: 8px;
        border-top: 1px solid #ebeef5;
    }

    .chat-sections > div + div {
        margin-top: 8px;
    }

    .chat-sections ul {
        margin: 3px 0 0;
        padding-left: 18px;
    }

    .chat-typing span {
        display: inline-block;
        width: 6px;
        height: 6px;
        margin: 0 2px;
        border-radius: 50%;
        background: #909399;
        animation: typing-dot 1.2s infinite ease-in-out;
    }

    .chat-typing span:nth-child(2) {
        animation-delay: 0.15s;
    }

    .chat-typing span:nth-child(3) {
        animation-delay: 0.3s;
    }

    .quick-questions {
        display: flex;
        gap: 6px;
        overflow-x: auto;
        padding: 9px 12px 4px;
        background: #fff;
    }

    .quick-questions button {
        flex: none;
        padding: 5px 9px;
        border: 1px solid #c6e2ff;
        border-radius: 13px;
        color: #409eff;
        background: #ecf5ff;
        cursor: pointer;
        font-size: 12px;
    }

    .ai-chat-input {
        display: flex;
        gap: 8px;
        padding: 8px 12px 5px;
        background: #fff;
    }

    .ai-chat-disclaimer {
        padding: 2px 0 9px;
        color: #a0a4aa;
        background: #fff;
        text-align: center;
        font-size: 11px;
    }

    .ai-chat-enter-active,
    .ai-chat-leave-active {
        transition: opacity 0.2s ease, transform 0.2s ease;
    }

    .ai-chat-enter,
    .ai-chat-leave-to {
        opacity: 0;
        transform: translateY(12px) scale(0.98);
    }

    @keyframes typing-dot {
        0%, 60%, 100% { transform: translateY(0); opacity: 0.45; }
        30% { transform: translateY(-4px); opacity: 1; }
    }

    @media (max-width: 600px) {
        .ai-chat-panel {
            right: 12px;
            bottom: 82px;
            width: calc(100vw - 24px);
            height: 70vh;
        }

        .ai-chat-trigger {
            right: 16px;
            bottom: 18px;
        }
    }
</style>
