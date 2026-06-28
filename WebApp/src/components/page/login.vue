<template>
    <div class="login-container">
        <el-card class="box-card login-card">
            <div class="login-body">
                <div class="login-title" @click="toIndex">校园二手交易平台
                    <div style="font-size:13px;font-weight:400;color:#909399;margin-top:6px;cursor:default;">
                        <router-link to="/index" style="color:#409EFF;text-decoration:none;">← 返回首页</router-link>
                    </div>
                </div>
                <el-form ref="form" :model="userForm">
                    <el-input placeholder="请输入手机号..." v-model="userForm.accountNumber" class="login-input">
                        <template slot="prepend">
                            <div class="el-icon-user-solid"></div>
                        </template>
                    </el-input>
                    <el-input placeholder="请输入密码..." v-model="userForm.userPassword" class="login-input"
                              @keyup.enter.native="login"  show-password>
                        <template slot="prepend">
                            <div class="el-icon-lock"></div>
                        </template>
                    </el-input>
                    <div class="login-submit">
                        <el-button type="primary" @click="login" class="login-btn">登录</el-button>
                    </div>
                    <div class="other-submit">
                        <router-link to="/sign-in" class="sign-in-text">注册</router-link>
                        <router-link to="/login-admin" class="sign-in-text">后台登录</router-link>
                    </div>
                </el-form>
            </div>
        </el-card>
    </div>
</template>

<script>
    export default {
        name: "login",
        data() {
            return {
                userForm: {
                    accountNumber: '17322611234',
                    userPassword: '123456'
                }
            };
        },

        methods: {
            login() {
                this.$api.userLogin({
                    accountNumber: this.userForm.accountNumber,
                    userPassword: this.userForm.userPassword
                }).then(res => {
                    console.log(res);
                    if (res.status_code === 1) {
                        res.data.signInTime=res.data.signInTime.substring(0,10);
                        this.$globalData.userInfo = res.data;
                        this.$router.replace({path: '/index'});
                    } else {
                        this.$message.error(res.msg);
                    }
                });
            },
            toIndex() {
                this.$router.replace({path: '/index'});
            }
        }
    }
</script>

<style scoped>
    .login-container {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 100vh;
        width: 100%;
        background-color: #f1f1f1;
    }
    .login-card {
        animation: bounceIn 0.5s ease both;
        border-radius: 10px;
        transition: box-shadow 0.3s ease;
    }
    .login-card:hover {
        box-shadow: 0 12px 40px rgba(0, 0, 0, 0.10);
    }

    .login-body {
        padding: 30px;
        width: 400px;
        height: 100%;
    }

    .login-title {
        padding-bottom: 30px;
        text-align: center;
        font-weight: 600;
        font-size: 20px;
        color: #409EFF;
        cursor: pointer;
        transition: color 0.2s ease, transform 0.2s ease;
    }
    .login-title:hover {
        color: #66b1ff;
        transform: scale(1.02);
    }

    .login-input {
        margin-bottom: 20px;
    }

    .login-submit {
        display: flex;
        justify-content: center;
    }
    .login-btn {
        width: 160px;
        transition: all 0.25s ease;
    }
    .login-btn:hover {
        width: 180px;
    }

    .sign-in-container {
        padding: 0 10px;
    }

    .sign-in-text {
        color: #409EFF;
        font-size: 16px;
        text-decoration: none;
        line-height:28px;
        transition: color 0.2s ease;
    }
    .sign-in-text:hover {
        color: #66b1ff;
    }
    .other-submit{
        display:flex;
        justify-content: space-between;
        margin-top: 10px;
    }
</style>
