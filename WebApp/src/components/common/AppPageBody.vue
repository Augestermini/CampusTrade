<template>
    <div class="main-container">
        <div class="main-content">
            <slot></slot>
        </div>
    </div>
</template>

<script>
    export default {
        name: "PageBody"
    }
</script>

<style scoped>
    .main-container{
        margin-top: 67px;
        display: flex;
        justify-content: center;
        animation: fadeIn 0.4s ease both;
    }
    .main-content{
        width: 1000px;
        background-color: #ffffff;
        min-height: 90vh;
        transition: min-height 0.3s ease;
    }
</style>
