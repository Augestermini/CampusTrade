<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="release-idle-container">
                <div class="release-idle-container-title">发布闲置</div>
                <div class="release-idle-container-form">
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
                                        class="ai-original-price-input"
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
                                获取价格建议
                            </el-button>
                        </div>
                        <div v-if="priceSuggestion" class="price-suggestion">
                            <div class="price-suggestion-header">
                                <span>建议售价区间</span>
                                <el-tag :type="priceStatusType">
                                    {{ priceStatusText }}
                                </el-tag>
                            </div>
                            <div class="price-values">
                                <div>
                                    <span class="price-label">最低价</span>
                                    <strong>¥{{ formatPrice(priceSuggestion.minPrice) }}</strong>
                                </div>
                                <div>
                                    <span class="price-label">建议价</span>
                                    <strong class="suggested-price">¥{{ formatPrice(priceSuggestion.suggestedPrice) }}</strong>
                                </div>
                                <div>
                                    <span class="price-label">最高价</span>
                                    <strong>¥{{ formatPrice(priceSuggestion.maxPrice) }}</strong>
                                </div>
                            </div>
                            <p v-if="priceSuggestion.reason" class="price-reason">
                                {{ priceSuggestion.reason }}
                            </p>
                            <p v-if="priceSuggestion.warning" class="price-warning">
                                {{ priceSuggestion.warning }}
                            </p>
                            <el-button
                                    v-if="priceSuggestion.suggestedPrice != null"
                                    size="small"
                                    type="primary"
                                    plain
                                    @click="applySuggestedPrice">
                                采用建议价
                            </el-button>
                        </div>
                    </div>
                    <div style="display: flex; justify-content: space-between;">
                        <div>
                            <div class="release-tip">闲置类别</div>
                            <el-select  v-model="idleItemInfo.idleLabel" placeholder="请选择类别">
                                <el-option
                                        v-for="item in options2"
                                        :key="item.value"
                                        :label="item.label"
                                        :value="item.value">
                                </el-option>
                            </el-select>
                        </div>
                        <div style="width: 300px;">
                            <el-input
                                    class="release-price-input"
                                    v-model="idleItemInfo.idlePrice"
                                    type="number"
                                    min="0"
                                    step="0.01"
                                    placeholder="请输入售价">
                                <template slot="prepend">售价</template>
                            </el-input>
                        </div>

                    </div>
                    <div class="release-idle-container-picture">
                        <div class="release-idle-container-picture-title">上传闲置照片</div>
                        <el-upload
                                action="http://localhost:8080/file/"
                                :on-preview="fileHandlePreview"
                                :on-remove="fileHandleRemove"
                                :on-success="fileHandleSuccess"
                                :show-file-list="showFileList"
                                :limit="10"
                                :on-exceed="handleExceed"
                                accept="image/*"
                                drag
                                multiple>
                            <i class="el-icon-upload"></i>
                            <div class="el-upload__text">将图片拖到此处，或<em>点击上传</em></div>
                        </el-upload>
                        <div class="picture-list">
                            <el-image style="width: 600px;margin-bottom: 2px;" fit="contain"
                                      v-for="(img,index) in imgList" :src="img"
                                      :preview-src-list="imgList"></el-image>
                        </div>
                        <el-dialog :visible.sync="imgDialogVisible">
                            <img width="100%" :src="dialogImageUrl" alt="">
                        </el-dialog>
                    </div>
                    <div style="display: flex;justify-content: center;margin-top: 30px;margin-bottom: 30px;">
                        <el-button type="primary" plain @click="releaseButton">确认发布</el-button>
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
                imgDialogVisible:false,
                dialogImageUrl:'',
                showFileList:true,
                options2: [{
                    value: 1,
                    label: '数码'
                }, {
                    value: 2,
                    label: '家电'
                }, {
                    value: 3,
                    label: '户外'
                }, {
                    value: 4,
                    label: '图书'
                }, {
                    value: 5,
                    label: '其他'
                }],
                conditionOptions:[
                    '全新',
                    '未拆封',
                    '几乎全新',
                    '九成新',
                    '八成新',
                    '七成新',
                    '六成新',
                    '五成新及以下'
                ],
                usedTimeOptions:[
                    '一个月',
                    '半年',
                    '一年',
                    '两年',
                    '三年',
                    '更久'
                ],
                aiItemInfo:{
                    originalPrice:'',
                    conditionLevel:'',
                    usedTime:'',
                    accessories:'',
                    campusTrade:true
                },
                descriptionLoading:false,
                priceLoading:false,
                priceSuggestion:null,
                imgList:[],
                idleItemInfo:{
                    idleName:'',
                    idleDetails:'',
                    pictureList:'',
                    idlePrice:'',
                    idleLabel:''
                }
            };
        },
        computed: {
            priceStatusType() {
                const typeMap = {
                    NORMAL: 'success',
                    TOO_LOW: 'warning',
                    TOO_HIGH: 'danger',
                    UNKNOWN: 'info'
                };
                return typeMap[this.priceSuggestion && this.priceSuggestion.priceStatus] || 'info';
            },
            priceStatusText() {
                const textMap = {
                    NORMAL: '价格合理',
                    TOO_LOW: '价格偏低',
                    TOO_HIGH: '价格偏高',
                    UNKNOWN: '无法判断'
                };
                return textMap[this.priceSuggestion && this.priceSuggestion.priceStatus] || '价格建议';
            }
        },
        methods: {
            getAiCategory() {
                const categoryMap = {
                    1: '数码产品',
                    2: '宿舍用品',
                    3: '运动用品',
                    4: '教材书籍',
                    5: '其他'
                };
                return categoryMap[this.idleItemInfo.idleLabel] || '';
            },
            getAiCommonPayload() {
                return {
                    name: this.idleItemInfo.idleName,
                    category: this.getAiCategory(),
                    conditionLevel: this.aiItemInfo.conditionLevel,
                    usedTime: this.aiItemInfo.usedTime,
                    price: this.idleItemInfo.idlePrice > 0 ? this.idleItemInfo.idlePrice : null
                };
            },
            generateDescription() {
                if (!this.idleItemInfo.idleName) {
                    this.$message.warning('请先填写商品名称');
                    return;
                }
                if (!this.idleItemInfo.idleLabel) {
                    this.$message.warning('请先选择商品类别');
                    return;
                }
                const payload = Object.assign(this.getAiCommonPayload(), {
                    accessories: this.aiItemInfo.accessories,
                    campusTrade: this.aiItemInfo.campusTrade
                });
                this.descriptionLoading=true;
                this.$api.generateDescription(payload).then(res=>{
                    if (res.status_code === 1 && res.data && res.data.description) {
                        this.idleItemInfo.idleDetails=res.data.description;
                        this.$message.success('商品描述已生成，可继续修改');
                    } else {
                        this.$message.error('描述生成失败！'+(res.msg || '请稍后重试'));
                    }
                }).catch(()=>{
                    this.$message.error('描述生成失败，请检查后端服务是否启动');
                }).finally(()=>{
                    this.descriptionLoading=false;
                });
            },
            suggestPrice() {
                if (!this.idleItemInfo.idleName) {
                    this.$message.warning('请先填写商品名称');
                    return;
                }
                if (!this.idleItemInfo.idleLabel) {
                    this.$message.warning('请先选择商品类别');
                    return;
                }
                if (!this.aiItemInfo.originalPrice || this.aiItemInfo.originalPrice <= 0) {
                    this.$message.warning('请填写大于 0 的商品原价');
                    return;
                }
                const payload = Object.assign(this.getAiCommonPayload(), {
                    originalPrice: this.aiItemInfo.originalPrice
                });
                this.priceLoading=true;
                this.$api.suggestPrice(payload).then(res=>{
                    if (res.status_code === 1 && res.data) {
                        this.priceSuggestion=res.data;
                    } else {
                        this.priceSuggestion=null;
                        this.$message.error('价格建议生成失败！'+(res.msg || '请稍后重试'));
                    }
                }).catch(()=>{
                    this.priceSuggestion=null;
                    this.$message.error('价格建议生成失败，请检查后端服务是否启动');
                }).finally(()=>{
                    this.priceLoading=false;
                });
            },
            applySuggestedPrice() {
                if (this.priceSuggestion && this.priceSuggestion.suggestedPrice != null) {
                    this.idleItemInfo.idlePrice=Number(this.priceSuggestion.suggestedPrice);
                    this.priceSuggestion=Object.assign({}, this.priceSuggestion, {
                        priceStatus:'NORMAL',
                        warning:'当前售价处于系统建议区间内，价格较为合理。'
                    });
                    this.$message.success('已采用系统建议价');
                }
            },
            formatPrice(price) {
                if (price == null || isNaN(Number(price))) {
                    return '--';
                }
                return Number(price).toFixed(2);
            },
            fileHandleRemove(file, fileList) {
                console.log(file, fileList);
                for(let i=0;i<this.imgList.length;i++){
                    if(this.imgList[i]===file.response.data){
                        this.imgList.splice(i,1);
                    }
                }
            },
            fileHandlePreview(file) {
                console.log(file);
                this.dialogImageUrl=file.response.data;
                this.imgDialogVisible=true;
            },
            fileHandleSuccess(response, file, fileList){
                console.log("file:",response,file,fileList);
                this.imgList.push(response.data);
            },
            releaseButton(){
                this.idleItemInfo.pictureList=JSON.stringify(this.imgList);
                console.log(this.idleItemInfo);
                if(this.idleItemInfo.idleName&&
                    this.idleItemInfo.idleDetails&&
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
    .release-price-input /deep/ input::-webkit-outer-spin-button,
    .release-price-input /deep/ input::-webkit-inner-spin-button,
    .ai-original-price-input /deep/ input::-webkit-outer-spin-button,
    .ai-original-price-input /deep/ input::-webkit-inner-spin-button {
        margin: 0;
        -webkit-appearance: none;
    }
    .release-price-input /deep/ input[type="number"],
    .ai-original-price-input /deep/ input[type="number"] {
        -moz-appearance: textfield;
    }
    .ai-assistant {
        margin: 0 0 20px;
        padding: 20px;
        border: 1px solid #dfe6ec;
        border-radius: 8px;
        background: #f8fbff;
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
