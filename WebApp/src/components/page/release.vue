<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="back-button-wrap">
                <el-button icon="el-icon-arrow-left" size="mini" @click="goBack">返回首页</el-button>
            </div>
            <div class="release-idle-container">
                <div class="release-idle-container-title animate-fade-in">发布闲置</div>
                <div class="release-idle-container-form animate-fade-in-up">
                    <el-input placeholder="请输入闲置名称" v-model="idleItemInfo.idleName"
                              maxlength="30"
                              show-word-limit>
                    </el-input>
                    <el-input
                            class="release-idle-detiles-text"
                            type="textarea"
                            :autosize="{ minRows: 5, maxRows: 10 }"
                            placeholder="请输入闲置的详细介绍..."
                            v-model="idleItemInfo.idleDetails"
                            maxlength="1000"
                            show-word-limit>
                    </el-input>
                    <div class="ai-assistant">
                        <div class="ai-assistant-title">
                            <div>
                                <span>AI 发布助手</span>
                                <span class="ai-assistant-subtitle">根据商品信息生成描述和价格建议</span>
                            </div>
                        </div>
                        <div class="ai-form-grid">
                            <div class="ai-form-item">
                                <div class="ai-form-label">商品原价</div>
                                <el-input
                                        v-model="aiItemInfo.originalPrice"
                                        type="number"
                                        min="0"
                                        step="0.01"
                                        placeholder="请输入商品原价">
                                    <template slot="prepend">¥</template>
                                </el-input>
                            </div>
                            <div class="ai-form-item">
                                <div class="ai-form-label">商品成色</div>
                                <el-select v-model="aiItemInfo.conditionLevel" placeholder="请选择成色">
                                    <el-option
                                            v-for="item in conditionOptions"
                                            :key="item"
                                            :label="item"
                                            :value="item">
                                    </el-option>
                                </el-select>
                            </div>
                            <div class="ai-form-item">
                                <div class="ai-form-label">使用时间</div>
                                <el-select
                                        v-model="aiItemInfo.usedTime"
                                        filterable
                                        allow-create
                                        default-first-option
                                        placeholder="请选择或输入">
                                    <el-option
                                            v-for="item in usedTimeOptions"
                                            :key="item"
                                            :label="item"
                                            :value="item">
                                    </el-option>
                                </el-select>
                            </div>
                            <div class="ai-form-item">
                                <div class="ai-form-label">商品配件</div>
                                <el-input
                                        v-model.trim="aiItemInfo.accessories"
                                        placeholder="例如：充电器、保护壳">
                                </el-input>
                            </div>
                        </div>
                        <div class="campus-trade-row">
                            <span class="ai-form-label">支持校内线下面交</span>
                            <el-switch v-model="aiItemInfo.campusTrade"></el-switch>
                        </div>
                        <div class="ai-actions">
                            <el-button
                                    type="primary"
                                    icon="el-icon-edit-outline"
                                    :loading="descriptionLoading"
                                    @click="generateDescription">
                                AI 生成描述
                            </el-button>
                            <el-button
                                    type="success"
                                    icon="el-icon-data-analysis"
                                    :loading="priceLoading"
                                    @click="suggestPrice">
                                AI 估价
                            </el-button>
                        </div>
                        <transition name="el-zoom-in-top">
                            <div v-if="lastPriceSuggestion" class="price-suggestion">
                                <div class="price-suggestion-header">
                                    <span>AI 估价参考</span>
                                    <el-tag size="small" type="warning">仅供参考</el-tag>
                                </div>
                                <div class="price-values">
                                    <div>
                                        <span class="price-label">建议定价</span>
                                        <span class="suggested-price">¥{{ lastPriceSuggestion.suggestedPrice }}</span>
                                    </div>
                                    <div>
                                        <span class="price-label">市场均价</span>
                                        <span>¥{{ lastPriceSuggestion.marketPrice }}</span>
                                    </div>
                                    <div>
                                        <span class="price-label">折旧率</span>
                                        <span>{{ lastPriceSuggestion.depreciationRate }}</span>
                                    </div>
                                </div>
                                <div class="price-reason">{{ lastPriceSuggestion.reason }}</div>
                                <div v-if="lastPriceSuggestion.warning" class="price-warning">{{ lastPriceSuggestion.warning }}</div>
                            </div>
                        </transition>
                    </div>
                    <div class="release-idle-place">
                        <el-select v-model="idleItemInfo.idlePlace" placeholder="选择交易地点">
                            <el-option label="校内" value="校内"></el-option>
                            <el-option label="校外" value="校外"></el-option>
                        </el-select>
                    </div>
                    <div class="release-idle-place">
                        <el-select v-model="idleItemInfo.idleLabel" placeholder="选择分类">
                            <el-option label="数码" value="1"></el-option>
                            <el-option label="家电" value="2"></el-option>
                            <el-option label="户外" value="3"></el-option>
                            <el-option label="图书" value="4"></el-option>
                            <el-option label="其他" value="5"></el-option>
                        </el-select>
                    </div>
                    <div class="release-idle-place">
                        <el-input placeholder="请输入价格" v-model="idleItemInfo.idlePrice">
                            <template slot="prepend">¥</template>
                        </el-input>
                    </div>
                    <div class="release-idle-container-picture">
                        <div class="release-idle-container-picture-title">添加图片（最多10张）</div>
                        <el-upload
                                action="http://localhost:8080/file/"
                                list-type="picture-card"
                                :on-success="fileHandleSuccess"
                                :on-remove="fileHandleRemove"
                                :file-list="imgFileList"
                                accept="image/*"
                                :multiple="true"
                                :limit="10"
                                :on-exceed="handleExceed">
                            <i class="el-icon-plus"></i>
                        </el-upload>
                    </div>
                    <div class="picture-list">
                        <el-button type="primary" @click="releaseButton">发布</el-button>
                    </div>
                </div>
            </div>
            <app-foot></app-foot>
        </app-body>
    </div>
</template>

<script>
    import AppHead from '../common/AppHeader.vue';
    import AppBody from '../common/AppPageBody.vue'
    import AppFoot from '../common/AppFoot.vue'

    export default {
        name: "release",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data() {
            return {
                idleItemInfo: {
                    idleName: '',
                    idleDetails: '',
                    idlePlace: '',
                    idleLabel: '',
                    idlePrice: '',
                    pictureList: []
                },
                imgList: [],
                imgFileList: [],
                descriptionLoading: false,
                priceLoading: false,
                lastPriceSuggestion: null,
                conditionOptions: ['全新', '99新', '95新', '9成新', '8成新', '7成新及以下'],
                usedTimeOptions: ['不到1个月', '1-3个月', '3-6个月', '6-12个月', '1年以上', '2年以上'],
                aiItemInfo: {
                    originalPrice: '',
                    conditionLevel: '',
                    usedTime: '',
                    accessories: '',
                    campusTrade: false,
                },
            };
        },
        watch: {
            lastPriceSuggestion(val) {
                if (val && val.suggestedPrice && !this.idleItemInfo.idlePrice) {
                    this.idleItemInfo.idlePrice = val.suggestedPrice;
                }
            }
        },
        methods: {
            generateDescription() {
                const { idleName, idleDetails } = this.idleItemInfo;
                if (!idleName && !idleDetails) {
                    this.$message.warning('请先填写商品名称或简单描述');
                    return;
                }
                this.descriptionLoading = true;
                this.$api.aiGenerateDescription({
                    productName: idleName,
                    productDetails: idleDetails,
                    originalPrice: this.aiItemInfo.originalPrice,
                    conditionLevel: this.aiItemInfo.conditionLevel,
                    usedTime: this.aiItemInfo.usedTime,
                    accessories: this.aiItemInfo.accessories,
                    campusTrade: this.aiItemInfo.campusTrade,
                }).then(res => {
                    if (res.status_code === 1) {
                        this.idleItemInfo.idleDetails = res.data.description;
                        this.$message({ message: '描述生成成功！', type: 'success' });
                    } else {
                        this.$message.error(res.msg || '生成失败，请稍后重试');
                    }
                }).catch(() => {
                    this.$message.error('网络异常，生成失败');
                }).finally(() => {
                    this.descriptionLoading = false;
                });
            },
            suggestPrice() {
                const { originalPrice, conditionLevel, usedTime, accessories, campusTrade } = this.aiItemInfo;
                if (!originalPrice) {
                    this.$message.warning('请填写商品原价');
                    return;
                }
                this.priceLoading = true;
                this.$api.aiSuggestPrice({
                    originalPrice,
                    conditionLevel,
                    usedTime,
                    accessories,
                    campusTrade,
                }).then(res => {
                    if (res.status_code === 1) {
                        this.lastPriceSuggestion = res.data;
                    } else {
                        this.$message.error(res.msg || '估价失败，请稍后重试');
                    }
                }).catch(() => {
                    this.$message.error('网络异常，估价失败');
                }).finally(() => {
                    this.priceLoading = false;
                });
            },
            fileHandleSuccess(response, file, fileList) {
                console.log(response, file, fileList);
                if (response.status_code === 1) {
                    this.imgList.push(response.data);
                } else {
                    this.$message.error('图片上传失败！');
                }
            },
            fileHandleRemove(file, fileList) {
                console.log(file, fileList);
                for (let i = 0; i < this.imgList.length; i++) {
                    if (this.imgList[i].indexOf(file.url) !== -1) {
                        this.imgList.splice(i, 1);
                    }
                }
            },
            goBack(){ this.$router.push({path: '/index'}); },
            releaseButton(){
                this.idleItemInfo.pictureList=JSON.stringify(this.imgList);
                console.log(this.idleItemInfo);
                if(this.idleItemInfo.idleName&&
                    this.idleItemInfo.idleDetails&&
                    this.idleItemInfo.idlePlace&&
                    this.idleItemInfo.idleLabel&&
                    this.idleItemInfo.idlePrice){
                    this.$api.addIdleItem(this.idleItemInfo).then(res=>{
                        if (res.status_code === 1) {
                            this.$message({
                                message: '发布成功！',
                                type: 'success'
                            });
                            console.log(res.data);
                            this.$router.replace({path: '/details', query: {id: res.data.id}});
                        } else {
                            this.$message.error('发布失败！'+res.msg);
                        }
                    }).catch(e=>{
                        this.$message.error('请填写完整信息');
                    })
                }else {
                    this.$message.error('请填写完整信息！');
                }

            },
            handleExceed(files, fileList) {
                this.$message.warning(`限制10张图片，本次选择了 ${files.length} 张图，共选择了 ${files.length + fileList.length} 张图`);
            },
        }
    }
</script>

<style scoped>
    .release-idle-container {
        min-height: 85vh;
    }

    .release-idle-container-title {
        font-size: 18px;
        padding: 30px 0;
        font-weight: 600;
        width: 100%;
        text-align: center;
    }

    .release-idle-container-form {
        padding: 0 180px;
    }

    .release-idle-detiles-text {
        margin: 20px 0;
    }
    .ai-assistant {
        margin: 0 0 20px;
        padding: 20px;
        border: 1px solid #dfe6ec;
        border-radius: 8px;
        background: #f8fbff;
        transition: box-shadow 0.3s ease;
    }
    .ai-assistant:hover {
        box-shadow: 0 4px 16px rgba(64, 158, 255, 0.08);
    }
    .ai-assistant-title {
        margin-bottom: 18px;
        color: #303133;
        font-size: 17px;
        font-weight: 600;
    }
    .ai-assistant-subtitle {
        margin-left: 10px;
        color: #909399;
        font-size: 13px;
        font-weight: 400;
    }
    .ai-form-grid {
        display: grid;
        grid-template-columns: repeat(2, minmax(0, 1fr));
        gap: 14px 20px;
    }
    .ai-form-item .el-select,
    .ai-form-item .el-input {
        width: 100%;
    }
    .ai-form-label {
        display: inline-block;
        margin-bottom: 7px;
        color: #606266;
        font-size: 14px;
    }
    .campus-trade-row {
        display: flex;
        align-items: center;
        gap: 10px;
        margin-top: 16px;
    }
    .campus-trade-row .ai-form-label {
        margin-bottom: 0;
    }
    .ai-actions {
        margin-top: 18px;
    }
    .price-suggestion {
        margin-top: 18px;
        padding: 16px;
        border-radius: 6px;
        background: #ffffff;
        box-shadow: 0 1px 4px rgba(0, 0, 0, .08);
    }
    .price-suggestion-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 14px;
        color: #303133;
        font-weight: 600;
    }
    .price-values {
        display: grid;
        grid-template-columns: repeat(3, 1fr);
        gap: 10px;
        margin-bottom: 12px;
    }
    .price-values > div {
        display: flex;
        flex-direction: column;
        color: #303133;
    }
    .price-label {
        margin-bottom: 4px;
        color: #909399;
        font-size: 12px;
    }
    .suggested-price {
        color: #409eff;
        font-size: 18px;
    }
    .price-reason,
    .price-warning {
        margin: 8px 0;
        color: #606266;
        font-size: 13px;
        line-height: 1.6;
    }
    .price-warning {
        color: #e6a23c;
    }
    .release-idle-place{
        margin-bottom: 15px;
    }
    .release-tip{
        color: #555555;
        float: left;
        padding-right: 5px;
        height: 36px;
        line-height: 36px;
        font-size: 14px;
    }
    .release-idle-container-picture{
        margin: 20px 0;

    }
    .release-idle-container-picture-title{
        margin: 10px 0;
        color: #555555;
        font-size: 14px;
    }
    .picture-list {
        margin: 20px 0;
        display: flex;
        flex-direction: column;
        align-items: center;
    }
    @media (max-width: 760px) {
        .release-idle-container-form {
            padding: 0 20px;
        }
        .ai-form-grid,
        .price-values {
            grid-template-columns: 1fr;
        }
        .ai-assistant-subtitle {
            display: block;
            margin: 6px 0 0;
        }
    }
</style>
