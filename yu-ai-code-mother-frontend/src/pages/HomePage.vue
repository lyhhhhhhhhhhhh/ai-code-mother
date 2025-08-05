<template>
  <div class="home-page">
    <div class="welcome-content">
      <h1 class="welcome-title">这是首页</h1>
      <p class="welcome-text">这是欢迎页</p>
    </div>
  </div>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.home-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.welcome-content {
  text-align: center;
  background: white;
  padding: 60px 40px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.welcome-title {
  font-size: 2.5rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 1rem;
}

.welcome-text {
  font-size: 1.25rem;
  color: #666;
  margin: 0;
}
</style> 