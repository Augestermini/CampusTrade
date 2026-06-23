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
                totalItem:1
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
</style>
