<template>
    <div class="header">
        <div class="header-container">
            <div class="app-name">
                <router-link to="/">校园二手交易平台</router-link>
            </div>
            <div class="search-wrapper">
                <div class="search-container">
                    <el-autocomplete
                            ref="searchAutocomplete"
                            class="search-autocomplete"
                            v-model="searchValue"
                            :fetch-suggestions="querySearchAsync"
                            :trigger-on-focus="false"
                            placeholder="搜闲置..."
                            @select="handleSelectSuggestion" @focus="onSearchFocus" @blur="onSearchBlur"
                            @keyup.enter.native="searchIdle">
                        <el-button slot="append" icon="el-icon-search" @click="searchIdle"></el-button>
                    </el-autocomplete>
                </div>
                <!-- 搜索历史面板 -->
                <div v-show="showHistory && searchHistory.length > 0 && !searchValue" class="search-history-panel">
                    <div class="search-history-header">
                        <span>搜索历史</span>
                        <el-button type="text" size="mini" @click="clearSearchHistory">清空历史</el-button>
                    </div>
                    <div
                        v-for="(item, index) in searchHistory"
                        :key="index"
                        class="search-history-item"
                        @click="searchFromHistory(item)">
                        <i class="el-icon-time"></i>
                        <span class="history-text">{{ item }}</span>
                        <i class="el-icon-close history-delete" @click.stop="deleteHistoryItem(index)"></i>
                    </div>
                </div>
            </div>
            <el-button type="primary" icon="el-icon-plus"  @click="toRelease">发布闲置</el-button>
            <el-button type="primary" icon="el-icon-chat-dot-round" @click="toMessage">消息</el-button>
            <router-link v-if="!isLogin" class="user-name-text" to="/login">登录</router-link>
            <el-dropdown trigger="click" v-else>
                <div style="cursor:pointer;display: flex;align-items: center;">
                    <div style="font-size: 16px;color: #409EFF;padding-right: 5px;">{{nicknameValue?nicknameValue:nickname}}</div>
                    <el-avatar :src="avatarValue?avatarValue:avatar"></el-avatar>
                </div>
                <el-dropdown-menu slot="dropdown">
                    <el-dropdown-item><div @click="toMe">个人中心</div></el-dropdown-item>
                    <el-dropdown-item divided style="color: red;"><div @click="loginOut">退出登录</div></el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
        </div>
    </div>
</template>
<script>

    const HISTORY_KEY = 'campus_trade_search_history';
    const MAX_HISTORY = 10;

    export default {
        name: 'Header',
        props: ['searchInput','nicknameValue','avatarValue'],
        data() {
            return {
                searchValue: this.searchInput,
                nickname:'登录',
                avatar:'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png',
                isLogin:false,
                showHistory: false,
                searchHistory: [],
                suggestionTimeout: null,
            };
        },
        created(){
            // console.log("header");
            this.loadSearchHistory();
            if(! this.$globalData.userInfo.nickname){
                this.$api.getUserInfo().then(res=>{
                    console.log('Header getUserInfo:',res);
                    if(res.status_code===1){
                        this.nickname=res.data.nickname;
                        this.avatar=res.data.avatar;
                        res.data.signInTime=res.data.signInTime.substring(0,10);
                        this.$globalData.userInfo=res.data;
                        this.isLogin=true;
                    }
                })
            }else {
                this.nickname=this.$globalData.userInfo.nickname;
                this.avatar=this.$globalData.userInfo.avatar;
                this.isLogin=true;
            }
        },
        watch: {
            searchValue(val) {
                if (!val || val.trim() === '') {
                    // Only show history if input is focused and empty
                } else {
                    this.showHistory = false;
                }
            }
        },
        methods: {
            // 搜索历史管理
            onSearchBlur() {
                // 延迟关闭，让点击历史/建议的事件先触发
                setTimeout(() => {
                    this.showHistory = false;
                }, 200);
            },
            onSearchFocus() {
                if (!this.searchValue || this.searchValue.trim() === "") {
                    this.showHistory = this.searchHistory.length > 0;
                }
            },
            loadSearchHistory() {
                try {
                    const stored = localStorage.getItem(HISTORY_KEY);
                    this.searchHistory = stored ? JSON.parse(stored) : [];
                } catch(e) {
                    this.searchHistory = [];
                }
            },
            saveSearchHistory() {
                try {
                    localStorage.setItem(HISTORY_KEY, JSON.stringify(this.searchHistory));
                } catch(e) {}
            },
            addToHistory(keyword) {
                if (!keyword || keyword.trim() === '') return;
                keyword = keyword.trim();
                // Remove duplicate
                this.searchHistory = this.searchHistory.filter(h => h !== keyword);
                // Add to front
                this.searchHistory.unshift(keyword);
                // Limit to MAX_HISTORY
                if (this.searchHistory.length > MAX_HISTORY) {
                    this.searchHistory = this.searchHistory.slice(0, MAX_HISTORY);
                }
                this.saveSearchHistory();
            },
            deleteHistoryItem(index) {
                this.searchHistory.splice(index, 1);
                this.saveSearchHistory();
            },
            clearSearchHistory() {
                this.searchHistory = [];
                this.saveSearchHistory();
                this.showHistory = false;
            },
            searchFromHistory(keyword) {
                this.searchValue = keyword;
                this.showHistory = false;
                this.addToHistory(keyword);
                this.doSearch(keyword);
            },

            // 搜索建议 - 异步获取
            querySearchAsync(queryString, cb) {
                if (!queryString || queryString.trim() === '') {
                    cb([]);
                    return;
                }
                // Debounce: clear previous timeout
                if (this.suggestionTimeout) {
                    clearTimeout(this.suggestionTimeout);
                }
                this.suggestionTimeout = setTimeout(() => {
                    this.$api.findIdleTiem({
                        page: 1,
                        nums: 8,
                        findValue: queryString.trim()
                    }).then(res => {
                        if (res.status_code === 1 && res.data && res.data.list) {
                            const suggestions = res.data.list.map(item => ({
                                value: item.idleName,
                                idleName: item.idleName
                            }));
                            // Deduplicate
                            const seen = new Set();
                            const unique = suggestions.filter(s => {
                                if (seen.has(s.value)) return false;
                                seen.add(s.value);
                                return true;
                            });
                            cb(unique);
                        } else {
                            cb([]);
                        }
                    }).catch(() => {
                        cb([]);
                    });
                }, 300);
            },
            handleSelectSuggestion(item) {
                this.addToHistory(item.value);
                this.doSearch(item.value);
            },
            searchIdle() {
                if (!this.searchValue || this.searchValue.trim() === '') return;
                this.addToHistory(this.searchValue.trim());
                this.doSearch(this.searchValue.trim());
            },
            doSearch(keyword) {
                if ('/search' !== this.$route.path) {
                    this.$router.push({path: '/search', query: {searchValue: keyword}});
                } else {
                    this.$router.replace({path: '/search', query: {searchValue: keyword}});
                    this.$router.go(0);
                }
            },
            toMe() {
                if ('/me' !== this.$route.path) {
                    this.$router.push({path: '/me'});
                }
            },
            toMessage(){
                if ('/message' !== this.$route.path) {
                    this.$router.push({path: '/message'});
                }
            },
            toRelease(){
                if ('/release' !== this.$route.path) {
                    this.$router.push({path: '/release'});
                }
            },
            loginOut(){
                this.$api.logout().then(res=>{
                    if(res.status_code===1){
                        this.$globalData.userInfo={};
                        console.log("login out");
                        if ('/index' === this.$route.path) {
                            this.$router.go(0);
                        }else {
                            this.$router.push({path: '/index'});
                        }
                    }else {
                        this.$message.error('网络或系统异常，退出登录失败！');
                    }
                });

            }
        }
    };
</script>
<style scoped>
    .header {
        position: fixed;
        top: 0;
        left: 0;
        right: 0;
        width: 100%;
        height: 58px;
        background: #ffffff;
        display: flex;
        justify-content: center;
        border-bottom: #eeeeee solid 2px;
        z-index: 1000;
    }

    .header-container {
        width: 1000px;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .app-name a {
        color: #409EFF;
        font-size: 24px;
        text-decoration: none;
        transition: color 0.2s ease;
    }
    .app-name a:hover {
        color: #66b1ff;
    }

    /* 搜索框外层容器 */
    .search-wrapper {
        position: relative;
    }
    .search-container {
        width: 320px;
    }
    .search-autocomplete {
        width: 100%;
    }
    .search-container .el-input__inner:focus {
        box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.15);
    }

    /* 搜索历史面板 */
    .search-history-panel {
        position: absolute;
        top: 44px;
        left: 0;
        right: 0;
        background: #fff;
        border: 1px solid #e4e7ed;
        border-radius: 0 0 6px 6px;
        box-shadow: 0 6px 20px rgba(0,0,0,0.10);
        z-index: 2000;
        max-height: 280px;
        overflow-y: auto;
        padding: 6px 0;
    }
    .search-history-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 8px 14px 4px;
        color: #909399;
        font-size: 13px;
        border-bottom: 1px solid #f0f0f0;
    }
    .search-history-header .el-button {
        padding: 0;
        font-size: 12px;
        color: #909399;
    }
    .search-history-item {
        display: flex;
        align-items: center;
        padding: 8px 14px;
        cursor: pointer;
        transition: background-color 0.15s ease;
        font-size: 14px;
        color: #303133;
    }
    .search-history-item:hover {
        background-color: #f5f7fa;
    }
    .search-history-item .el-icon-time {
        color: #c0c4cc;
        margin-right: 8px;
        font-size: 14px;
    }
    .history-text {
        flex: 1;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }
    .history-delete {
        color: #c0c4cc;
        font-size: 12px;
        cursor: pointer;
        transition: color 0.15s ease;
    }
    .history-delete:hover {
        color: #f56c6c;
    }

    .user-name-text{
        font-size: 16px;
        font-weight: 600;
        color: #409EFF;
        cursor: pointer;
        text-decoration: none;
        transition: color 0.2s ease;
    }
    .user-name-text:hover {
        color: #66b1ff;
    }
    /* header 按钮组微间距 */
    .header-container .el-button + .el-button {
        margin-left: 8px;
    }
</style>
