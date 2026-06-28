<template>
    <div class="foot-container">
        <div class="author">明账细算，以需为度，不趋浮华媚眼，买前思三日</div>
    </div>
</template>

<script>
    export default {
        name: "Foot"
    }
</script>

<style scoped>
    .foot-container {
        width: 100%;
        display: flex;
        justify-content: center;
        height: 50px;
        padding-top: 10px;
        animation: fadeIn 0.5s ease both;
        align-items: center;
    }

    .author {
        color: #999999;
        font-size: 14px;
        letter-spacing: 1px;
    }
</style>
