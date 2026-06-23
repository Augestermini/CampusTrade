<template>
    <div id="app">
        <transition name="fade" mode="out-in">
            <router-view></router-view>
        </transition>
    </div>
</template>
<script>
    export default {
        name: 'App',
        watch: {
            $route() {
                // 路由变化时滚动到顶部，让过渡动画从顶部开始
                window.scrollTo({ top: 0, behavior: 'smooth' });
            }
        }
    }
</script>
<style>
    html, body, #app{
        overflow: visible;
        background-color: #f6f6f6;
    }
    ::-webkit-scrollbar {
        width: 6px;
    }
    ::-webkit-scrollbar-track {
        background: transparent;
    }
    ::-webkit-scrollbar-thumb {
        background: #c0c4cc;
        border-radius: 3px;
    }
    ::-webkit-scrollbar-thumb:hover {
        background: #909399;
    }
</style>
