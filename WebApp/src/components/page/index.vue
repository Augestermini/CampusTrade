<template>
    <div>
        <app-head></app-head>
        <app-body>
            <div class="home-page">
                <section class="home-hero">
                    <div class="hero-content">
                        <div class="hero-kicker">Campus Trade</div>
                        <h1>校园二手交易平台</h1>
                        <p>让教材、数码、生活用品在校园里轻松流转，发现身边更实惠的闲置好物。</p>
                        <div class="hero-actions">
                            <el-input
                                    class="hero-search"
                                    placeholder="搜索教材、耳机、自行车..."
                                    v-model="searchValue"
                                    @keyup.enter.native="searchIdle">
                                <el-button slot="append" icon="el-icon-search" @click="searchIdle"></el-button>
                            </el-input>
                            <el-button class="release-btn" type="primary" icon="el-icon-plus" @click="toRelease">
                                发布闲置
                            </el-button>
                        </div>
                    </div>
                    <div class="hero-panel">
                        <div class="panel-title">今日逛逛</div>
                        <div class="panel-number">{{ totalItem }}</div>
                        <div class="panel-desc">件闲置正在等待新主人</div>
                    </div>
                </section>

                <section class="category-section">
                    <div
                            v-for="category in categories"
                            :key="category.name"
                            :class="['category-card', {'active': labelName === category.name}]"
                            @click="selectCategory(category.name)">
                        <i :class="category.icon"></i>
                        <span>{{ category.label }}</span>
                    </div>
                </section>

                <section class="goods-section">
                    <div class="section-head">
                        <div>
                            <h2>{{ currentCategoryLabel }}</h2>
                            <p>最新发布的校园闲置，点击卡片查看详情</p>
                        </div>
                    </div>

                    <div class="empty-state" v-if="idleList.length===0">
                        <i class="el-icon-box"></i>
                        <div>暂无相关闲置，换个分类或关键词试试</div>
                    </div>

                    <el-row :gutter="24" v-else>
                        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="idle in idleList" :key="idle.id">
                            <div class="idle-card" @click="toDetails(idle)">
                                <el-image
                                        class="idle-image"
                                        :src="idle.imgUrl"
                                        fit="cover">
                                    <div slot="error" class="image-slot">
                                        <i class="el-icon-picture-outline"></i>
                                        <span>暂无图片</span>
                                    </div>
                                </el-image>
                                <div class="idle-info">
                                    <div class="idle-title">{{ idle.idleName }}</div>
                                    <div class="idle-meta">
                                        <div class="idle-price">￥{{ idle.idlePrice }}</div>
                                        <div class="idle-place">
                                            <i class="el-icon-location-outline"></i>
                                            {{ idle.idlePlace || '校内' }}
                                        </div>
                                    </div>
                                    <div class="idle-time">{{ idle.timeStr }}</div>
                                    <div class="user-info">
                                        <el-avatar :size="30" :src="idle.user && idle.user.avatar"></el-avatar>
                                        <div class="user-nickname">{{ idle.user ? idle.user.nickname : '校园用户' }}</div>
                                    </div>
                                </div>
                            </div>
                        </el-col>
                    </el-row>
                </section>

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
                searchValue: '',
                idleList: [],
                currentPage: 1,
                totalItem: 1,
                categories: [
                    {label: '全部', name: '0', icon: 'el-icon-menu'},
                    {label: '数码', name: '1', icon: 'el-icon-mobile-phone'},
                    {label: '家电', name: '2', icon: 'el-icon-monitor'},
                    {label: '户外', name: '3', icon: 'el-icon-bicycle'},
                    {label: '图书', name: '4', icon: 'el-icon-reading'},
                    {label: '其他', name: '5', icon: 'el-icon-more'}
                ]
            };
        },
        computed: {
            currentCategoryLabel() {
                const category = this.categories.find(item => item.name === this.labelName);
                return category ? category.label + '闲置' : '最新闲置';
            }
        },
        created() {
            this.labelName = this.$route.query.labelName || '0';
            this.currentPage = parseInt(this.$route.query.page) ? parseInt(this.$route.query.page) : 1;
            this.findIdleTiem(this.currentPage)
        },
        watch: {
            $route(to) {
                this.labelName = to.query.labelName || '0';
                let val = parseInt(to.query.page) ? parseInt(to.query.page) : 1;
                this.currentPage = val;
                this.findIdleTiem(val);
            }
        },
        methods: {
            formatIdleList(list) {
                return list.map(item => {
                    item.timeStr = item.releaseTime ? item.releaseTime.substring(0, 10) + " " + item.releaseTime.substring(11, 19) : '';
                    let pictureList = [];
                    try {
                        pictureList = JSON.parse(item.pictureList || '[]');
                    } catch (e) {
                        pictureList = [];
                    }
                    item.imgUrl = pictureList.length > 0 ? pictureList[0] : '';
                    return item;
                });
            },
            findIdleTiem(page) {
                const loading = this.$loading({
                    lock: true,
                    text: '加载数据中',
                    spinner: 'el-icon-loading',
                    background: 'rgba(0, 0, 0, 0)'
                });
                const request = this.labelName > 0
                    ? this.$api.findIdleTiemByLable({idleLabel: this.labelName, page: page, nums: 8})
                    : this.$api.findIdleTiem({page: page, nums: 8});

                request.then(res => {
                    let list = res.data.list || [];
                    this.idleList = this.formatIdleList(list);
                    this.totalItem = res.data.count || 0;
                }).catch(e => {
                    console.log(e)
                }).finally(() => {
                    loading.close();
                })
            },
            selectCategory(name) {
                this.labelName = name;
                this.$router.replace({query: {page: 1, labelName: this.labelName}});
            },
            searchIdle() {
                if (!this.searchValue) {
                    return;
                }
                this.$router.push({path: '/search', query: {searchValue: this.searchValue}});
            },
            toRelease() {
                this.$router.push({path: '/release'});
            },
            handleCurrentChange(val) {
                this.$router.replace({query: {page: val, labelName: this.labelName}});
            },
            toDetails(idle) {
                this.$router.push({path: '/details', query: {id: idle.id}});
            }
        }
    }
</script>

<style scoped>
    .home-page {
        min-height: 85vh;
        padding: 22px 0 10px;
        background: #f6f8fb;
    }

    .home-hero {
        display: flex;
        justify-content: space-between;
        align-items: stretch;
        gap: 24px;
        margin: 0 20px 22px;
        padding: 30px;
        background: linear-gradient(135deg, #e9f8f5 0%, #f4fbff 55%, #fff7ed 100%);
        border: 1px solid #e6eef2;
        border-radius: 8px;
    }

    .hero-content {
        flex: 1;
        min-width: 0;
    }

    .hero-kicker {
        color: #0f9f86;
        font-size: 13px;
        font-weight: 700;
        margin-bottom: 8px;
    }

    .hero-content h1 {
        margin: 0;
        color: #1f2d3d;
        font-size: 32px;
        line-height: 1.3;
        font-weight: 700;
        letter-spacing: 0;
    }

    .hero-content p {
        width: 640px;
        max-width: 100%;
        margin: 12px 0 22px;
        color: #5f6f7d;
        font-size: 15px;
        line-height: 1.8;
    }

    .hero-actions {
        display: flex;
        gap: 12px;
        align-items: center;
    }

    .hero-search {
        width: 430px;
        max-width: 100%;
    }

    .release-btn {
        background: #12a98e;
        border-color: #12a98e;
    }

    .hero-panel {
        width: 210px;
        padding: 22px;
        background: #ffffff;
        border: 1px solid #e7edf1;
        border-radius: 8px;
        box-shadow: 0 12px 28px rgba(31, 45, 61, 0.08);
    }

    .panel-title {
        color: #5f6f7d;
        font-size: 14px;
    }

    .panel-number {
        margin-top: 12px;
        color: #ff6b35;
        font-size: 42px;
        font-weight: 700;
        line-height: 1;
    }

    .panel-desc {
        margin-top: 10px;
        color: #7a8792;
        font-size: 13px;
    }

    .category-section {
        display: grid;
        grid-template-columns: repeat(6, 1fr);
        gap: 12px;
        margin: 0 20px 22px;
    }

    .category-card {
        height: 74px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 8px;
        background: #ffffff;
        border: 1px solid #e8edf2;
        border-radius: 8px;
        color: #475669;
        cursor: pointer;
        transition: all .2s ease;
    }

    .category-card i {
        color: #12a98e;
        font-size: 24px;
    }

    .category-card span {
        font-size: 14px;
        font-weight: 600;
    }

    .category-card:hover,
    .category-card.active {
        color: #0f8f79;
        border-color: #12a98e;
        box-shadow: 0 8px 20px rgba(18, 169, 142, 0.14);
        transform: translateY(-2px);
    }

    .goods-section {
        margin: 0 20px;
    }

    .section-head {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        margin-bottom: 16px;
    }

    .section-head h2 {
        margin: 0;
        color: #1f2d3d;
        font-size: 22px;
        letter-spacing: 0;
    }

    .section-head p {
        margin: 6px 0 0;
        color: #8492a6;
        font-size: 13px;
    }

    .idle-card {
        height: 330px;
        margin-bottom: 24px;
        background: #ffffff;
        border: 1px solid #e8edf2;
        border-radius: 8px;
        overflow: hidden;
        cursor: pointer;
        transition: all .2s ease;
    }

    .idle-card:hover {
        transform: translateY(-4px);
        box-shadow: 0 14px 28px rgba(31, 45, 61, 0.12);
    }

    .idle-image {
        width: 100%;
        height: 175px;
        display: block;
        background: #f1f5f8;
    }

    .image-slot {
        height: 175px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 8px;
        color: #99a9bf;
        font-size: 13px;
    }

    .image-slot i {
        font-size: 28px;
    }

    .idle-info {
        padding: 12px 14px;
    }

    .idle-title {
        height: 44px;
        color: #1f2d3d;
        font-size: 16px;
        font-weight: 700;
        line-height: 22px;
        overflow: hidden;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
    }

    .idle-meta {
        display: flex;
        justify-content: space-between;
        align-items: center;
        gap: 10px;
        margin-top: 8px;
    }

    .idle-price {
        color: #ff5a1f;
        font-size: 19px;
        font-weight: 700;
    }

    .idle-place {
        max-width: 110px;
        color: #708090;
        font-size: 13px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .idle-time {
        margin-top: 8px;
        color: #99a9bf;
        font-size: 12px;
    }

    .user-info {
        height: 30px;
        display: flex;
        align-items: center;
        margin-top: 10px;
    }

    .user-nickname {
        min-width: 0;
        padding-left: 8px;
        color: #8492a6;
        font-size: 13px;
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
    }

    .empty-state {
        height: 260px;
        display: flex;
        flex-direction: column;
        justify-content: center;
        align-items: center;
        gap: 12px;
        background: #ffffff;
        border: 1px dashed #cfd8e3;
        border-radius: 8px;
        color: #8492a6;
    }

    .empty-state i {
        color: #12a98e;
        font-size: 42px;
    }

    .fenye {
        display: flex;
        justify-content: center;
        min-height: 70px;
        align-items: center;
    }

    @media (max-width: 900px) {
        .home-hero {
            flex-direction: column;
        }

        .hero-panel {
            width: auto;
        }

        .category-section {
            grid-template-columns: repeat(3, 1fr);
        }
    }

    @media (max-width: 640px) {
        .home-hero {
            padding: 22px;
        }

        .hero-content h1 {
            font-size: 26px;
        }

        .hero-actions {
            flex-direction: column;
            align-items: stretch;
        }

        .hero-search {
            width: 100%;
        }

        .category-section {
            grid-template-columns: repeat(2, 1fr);
        }
    }
</style>
