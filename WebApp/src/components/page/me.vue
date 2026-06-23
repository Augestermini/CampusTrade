<template>
    <div>
        <app-head :nickname-value="userInfo.nickname"
                  :avatarValue="userInfo.avatar"></app-head>
        <app-body>
            <div class="back-button-wrap">
                <el-button icon="el-icon-arrow-left" size="small" @click="goBack">返回首页</el-button>
            </div>
            <div>
            <div>
                    <div class="user-info-details">

                        <el-upload
                                action="http://localhost:8080/file/"
                                :on-success="fileHandleSuccess"
                                :file-list="imgFileList"
                                accept="image/*"
                        >
                            <el-image
                                    style="width: 120px; height: 120px;border-radius: 10px;"
                                    :src="userInfo.avatar"
                                    fit="contain">
                            <div slot="placeholder" class="img-error-placeholder" style="border-radius:10px;width:120px;height:120px;">
                                <i class="el-icon-user" style="font-size:36px;margin:0;"></i>
                            </div>
                            <div slot="error" class="img-error-placeholder" style="border-radius:10px;width:120px;height:120px;">
                                <i class="el-icon-user" style="font-size:36px;margin:0;"></i>
                            </div>
                        </el-image>
                        </el-upload>
                        <div class="user-info-details-text">
                            <div class="user-info-details-text-nickname">{{userInfo.nickname}}</div>
                            <div class="user-info-details-text-time">{{userInfo.signInTime}} 加入平台</div>
                            <div class="user-info-details-text-edit">
                                <el-button type="primary" plain @click="userInfoDialogVisible = true">编辑个人信息</el-button>
                            </div>
                            <el-dialog append-to-body
                                    @close="finishEdit"
                                    title="编辑个人信息"
                                    :visible.sync="userInfoDialogVisible"
                                    width="400px">
                                <div class="edit-tip">昵称</div>
                                <el-input
                                        v-model="userInfo.nickname"
                                        :disabled="notUserNicknameEdit"
                                        @change="saveUserNickname">
                                    <el-button slot="append" type="warning" icon="el-icon-edit"
                                               @click="notUserNicknameEdit = false">编辑
                                    </el-button>
                                </el-input>

                                <div v-if="userPasswordEdit">
                                    <div class="edit-tip">原密码</div>
                                    <el-input v-model="userPassword1" show-password></el-input>
                                    <div class="edit-tip">新密码</div>
                                    <el-input v-model="userPassword2" show-password></el-input>
                                    <div class="edit-tip">确认新密码</div>
                                    <el-input v-model="userPassword3" show-password></el-input>
                                    <div class="edit-tip"></div>
                                    <el-button @click="savePassword" plain>确认修改</el-button>
                                </div>
                                <div v-else>
                                    <div class="edit-tip">密码</div>
                                    <el-input
                                            value="123456"
                                            :disabled="true"
                                            show-password>
                                        <el-button slot="append" type="warning" icon="el-icon-edit"
                                                   @click="userPasswordEdit = true">编辑
                                        </el-button>
                                    </el-input>
                                </div>
                                <span slot="footer" class="dialog-footer">
                                <el-button @click="userInfoDialogVisible=false">完成</el-button>
                            </span>
                            </el-dialog>
                        </div>
                    </div>
                </div>
                <div class="idle-container">
                    <el-tabs v-model="activeName" @tab-click="handleClick">
                        <el-tab-pane label="我发布的" name="1"></el-tab-pane>
                        <el-tab-pane label="我下架的" name="2"></el-tab-pane>
                        <el-tab-pane label="我收藏的" name="3"></el-tab-pane>
                        <el-tab-pane label="我卖出的" name="4"></el-tab-pane>
                        <el-tab-pane label="我买到的" name="5"></el-tab-pane>
                    </el-tabs>
                    <div class="idle-container-list">
                        <div v-for="(item,index) in dataList[activeName-1]" class="idle-container-list-item">
                            <div class="idle-container-list-item-detile" @click="toDetails(activeName,item)">
                                <el-image
                                        style="width: 100px; height: 100px;"
                                        :src="item.imgUrl"
                                        fit="cover">
                                    <div slot="placeholder" class="img-error-placeholder" style="border-radius:4px;width:100px;height:100px;">
                                        <i class="el-icon-picture-outline" style="font-size:28px;margin:0;"></i>
                                        <span>暂无图片</span>
                                    </div>
                                    <div slot="error" class="img-error-placeholder" style="border-radius:4px;width:100px;height:100px;">
                                        <i class="el-icon-picture-outline" style="font-size:28px;margin:0;"></i>
                                        <span>暂无图片</span>
                                    </div>
                                </el-image>
                                <div class="idle-container-list-item-text">
                                    <div class="idle-container-list-title" :class="activeName==='2'||item.idleStatus===2?'text-strikethrough':''">
                                        {{item.idleName}}
                                        <span v-if="activeName==='2'||item.idleStatus===2" class="offline-badge">已下架</span>
                                    </div>
                                    <div class="idle-container-list-idle-details" v-html="item.idleDetails">
                                        {{item.idleDetails}}
                                    </div>
                                    <div class="idle-container-list-idle-time">{{item.timeStr}}</div>

                                    <div class="idle-item-foot">
                                        <div class="idle-prive">￥{{item.idlePrice}}
                                            {{(activeName==='4'||activeName==='5')?orderStatus[item.orderStatus]:''}}
                                        </div>
                                        <el-button v-if="activeName!=='4'&&activeName!=='5'" type="danger" size="mini" slot="reference"
                                                   plain @click.stop="handle(activeName,item,index)">{{handleName[activeName-1]}}
                                        </el-button>
                                    </div>
                                </div>
                            </div>
                        </div>
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
        name: "me",
        components: {
            AppHead,
            AppBody,
            AppFoot
        },
        data() {
            return {
                imgFileList: [],
                activeName: '1',
                handleName: ['下架', '删除', '取消收藏', '', ''],
                dataList: [
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                    [],
                ],
                orderStatus: ['待付款', '待发货', '待收货', '已完成', '已取消'],
                userInfoDialogVisible: false,
                notUserNicknameEdit: true,
                userPasswordEdit: false,
                userPassword1: '',
                userPassword2: '',
                userPassword3: '',
                userInfo: {
                    accountNumber: "",
                    avatar: "",
                    nickname: "",
                    signInTime: "",
                },
            };
        },
        created() {
            if (!this.$globalData.userInfo.nickname) {
                this.$api.getUserInfo().then(res => {
                    if (res.status_code === 1) {
                        res.data.signInTime = res.data.signInTime.substring(0, 10);
                        console.log(res.data);
                        this.$globalData.userInfo = res.data;
                        this.userInfo = this.$globalData.userInfo;
                    }
                })
            } else {
                this.userInfo = this.$globalData.userInfo;
                console.log(this.userInfo);
            }
            this.getIdleItemData();
            this.getMyOrder();
            this.getMySoldIdle();
            this.getMyFavorite();
        },
        methods: {
            getMyFavorite(){
                this.$api.getMyFavorite().then(res=>{
                    console.log('getMyFavorite',res);
                    if (res.status_code === 1){
                        for (let i = 0; i < res.data.length; i++) {
                            let pictureList = JSON.parse(res.data[i].idleItem.pictureList);
                            this.dataList[2].push({
                                favoriteId:res.data[i].id,
                                id:res.data[i].idleItem.id,
                                imgUrl:pictureList.length > 0 ? pictureList[0] : '',
                                idleName:res.data[i].idleItem.idleName,
                                idleDetails:res.data[i].idleItem.idleDetails,
                                timeStr:res.data[i].createTime.substring(0, 10) + " " + res.data[i].createTime.substring(11, 19),
                                idlePrice:res.data[i].idleItem.idlePrice
                            });
                        }
                    }
                })
            },
            getMySoldIdle(){
                this.$api.getMySoldIdle().then(res=>{
                    if (res.status_code === 1){
                        console.log('getMySoldIdle',res.data);
                        for (let i = 0; i < res.data.length; i++) {
                            let pictureList = JSON.parse(res.data[i].idleItem.pictureList);
                            this.dataList[3].push({
                                id:res.data[i].id,
                                imgUrl:pictureList.length > 0 ? pictureList[0] : '',
                                idleName:res.data[i].idleItem.idleName,
                                idleDetails:res.data[i].idleItem.idleDetails,
                                timeStr:res.data[i].createTime.substring(0, 10) + " " + res.data[i].createTime.substring(11, 19),
                                idlePrice:res.data[i].orderPrice,
                                orderStatus:res.data[i].orderStatus
                            });
                        }
                    }
                })
            },
            getMyOrder(){
                this.$api.getMyOrder().then(res=>{
                    if (res.status_code === 1){
                        console.log('getMyOrder',res.data);
                        for (let i = 0; i < res.data.length; i++) {
                            let pictureList = JSON.parse(res.data[i].idleItem.pictureList);
                            this.dataList[4].push({
                                id:res.data[i].id,
                                imgUrl:pictureList.length > 0 ? pictureList[0] : '',
                                idleName:res.data[i].idleItem.idleName,
                                idleDetails:res.data[i].idleItem.idleDetails,
                                timeStr:res.data[i].createTime.substring(0, 10) + " " + res.data[i].createTime.substring(11, 19),
                                idlePrice:res.data[i].orderPrice,
                                orderStatus:res.data[i].orderStatus
                            });
                        }
                    }
                })
            },
            getIdleItemData() {
                this.$api.getAllIdleItem().then(res => {
                    console.log(res);
                    if (res.status_code === 1) {
                        for (let i = 0; i < res.data.length; i++) {
                            res.data[i].timeStr = res.data[i].releaseTime.substring(0, 10) + " " + res.data[i].releaseTime.substring(11, 19);
                            let pictureList = JSON.parse(res.data[i].pictureList);
                            res.data[i].imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                            if (res.data[i].idleStatus === 1) {
                                this.dataList[0].push(res.data[i]);
                            } else if (res.data[i].idleStatus === 2) {
                                this.dataList[1].push(res.data[i]);
                            }
                        }
                    }
                })
            },
            handleClick(tab, event) {
                // console.log(tab, event);
                console.log(this.activeName);
            },
            saveUserNickname() {
                this.notUserNicknameEdit = true;
                this.$api.updateUserPublicInfo({
                    nickname: this.userInfo.nickname
                }).then(res => {
                    console.log(res);
                    this.$globalData.userInfo.nickname = this.userInfo.nickname;
                })

            },
            savePassword() {
                if (!this.userPassword1 || !this.userPassword2) {
                    this.$message.error('密码为空！');
                } else if (this.userPassword2 === this.userPassword3) {
                    this.$api.updatePassword({
                        oldPassword: this.userPassword1,
                        newPassword: this.userPassword2
                    }).then(res => {
                        if (res.status_code === 1) {
                            this.userPasswordEdit = false;
                            this.$message({
                                message: '修改成功！',
                                type: 'success'
                            });
                            this.userPassword1 = '';
                            this.userPassword2 = '';
                            this.userPassword3 = '';
                        } else {
                            this.$message.error('旧密码错误，修改失败！');
                        }
                    })
                } else {
                    this.$message.error('两次输入的密码不一致！');
                }

            },
            finishEdit() {
                this.notUserNicknameEdit = true;
                this.userInfoDialogVisible = false;
                this.userPasswordEdit = false;
            },
            toDetails(activeName, item) {
                if (activeName === '4'||activeName === '5') {
                    this.$router.push({path: '/order', query: {id: item.id}});
                } else {
                    this.$router.push({path: '/details', query: {id: item.id}});
                }
            },
            handle(activeName,item,index) {
                console.log(activeName,item,index);
                this.$confirm('是否确认？', '提示', {
                    confirmButtonText: '确认',
                    cancelButtonText: '取消',
                    type: 'warning'
                }).then(() => {
                    if(activeName==='1'){
                        this.$api.updateIdleItem({
                            id:item.id,
                            idleStatus:2
                        }).then(res=>{
                            console.log(res);
                            if(res.status_code===1){
                                this.dataList[0].splice(index,1);
                                item.idleStatus=2;
                                this.dataList[1].unshift(item);
                            }else {
                                this.$message.error(res.msg)
                            }
                        });
                    }else if(activeName==='2'){
                        this.$api.updateIdleItem({
                            id:item.id,
                            idleStatus:0
                        }).then(res=>{
                            console.log(res);
                            if(res.status_code===1){
                                this.dataList[1].splice(index,1);
                            }else {
                                this.$message.error(res.msg)
                            }
                        });
                    }else if(activeName==='3'){
                        this.$api.deleteFavorite({
                            id: item.favoriteId
                        }).then(res=>{
                            console.log(res);
                            if(res.status_code===1){
                                this.$message({
                                    message: '已取消收藏！',
                                    type: 'success'
                                });
                                this.dataList[2].splice(index,1);
                            }else {
                                this.$message.error(res.msg)
                            }
                        }).catch(e=>{
                        })
                    }
                }).catch(() => {
                });

            },
            fileHandleSuccess(response, file, fileList) {
                console.log("file:", response, file, fileList);
                let imgUrl = response.data;
                this.imgFileList = [];
                this.$api.updateUserPublicInfo({
                    avatar: imgUrl
                }).then(res => {
                    console.log(res);
                    this.userInfo.avatar = imgUrl;
                    this.$globalData.userInfo.avatar = imgUrl;
                })
            },
            goBack(){
                this.$router.push({path: "/index"});
            },
        }
    }
</script>

<style scoped>
    .user-info-container {
        width: 100%;
        height: 200px;
        border-bottom: 15px solid #f6f6f6;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .user-info-details {
        display: flex;
        height: 140px;
        align-items: center;
        margin: 20px 40px;
    }

    .user-info-details-text {
        margin-left: 20px;
    }

    .user-info-details-text-nickname {
        font-size: 26px;
        font-weight: 600;
        margin: 10px 0;
    }

    .user-info-details-text-time {
        font-size: 14px;
        margin-bottom: 10px;
    }

    .user-info-splace {
        margin-right: 90px;
    }

    .idle-container {
        padding: 0 20px;
    }

    .idle-container-list {
        min-height: 55vh;
    }

    .idle-container-list-item {
        border-bottom: 1px solid #eeeeee;
        cursor: pointer;
    }

    .idle-container-list-item:last-child {
        border-bottom: none;
    }

    .idle-container-list-item-detile {
        height: 120px;
        display: flex;
        align-items: center;
    }

    .idle-container-list-item-text {
        margin-left: 10px;
        height: 100px;
        max-width: 800px;
    }

    .idle-container-list-title {
        font-weight: 600;
        font-size: 18px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .idle-container-list-idle-details {
        font-size: 14px;
        color: #555555;
        padding-top: 5px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .idle-container-list-idle-time {
        font-size: 13px;
        padding-top: 5px;
    }

    .idle-prive {
        font-size: 15px;
        padding-top: 5px;
        color: red;
    }

    .edit-tip {
        font-size: 14px;
        margin: 10px 5px;
    }








    .idle-item-foot {
        width: 800px;
        display: flex;
        justify-content: space-between;
    }
</style>