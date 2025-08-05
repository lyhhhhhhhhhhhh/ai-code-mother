<template>
  <a-layout-footer class="global-footer">
    <div class="footer-container">
      <p class="copyright">
        by
        <a
          href="https://github.com/lyhhhhhhhhhhhh/ai-code-mother"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          lyhhhhhhhhhhhh
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的逻辑
</script>

<style scoped>
.global-footer {
  background: #f0f2f5;
  border-top: 1px solid #e8e8e8;
  padding: 16px 0;
  text-align: center;
  position: relative;
  bottom: 0;
  width: 100%;
}

.footer-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.copyright {
  margin: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.author-link {
  color: #1890ff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.author-link:hover {
  color: #40a9ff;
  text-decoration: underline;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .footer-container {
    padding: 0 16px;
  }

  .copyright {
    font-size: 12px;
  }
}
</style>
