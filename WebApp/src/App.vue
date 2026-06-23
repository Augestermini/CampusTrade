<template>
    <div id="app">
        <router-view></router-view>
    </div>
</template>
<script>
    export default {
        name: 'App',
        watch: {
            $route() {
                window.scrollTo(0, 0);
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
