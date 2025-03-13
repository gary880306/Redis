<template>
  <div class="login-container">
    <h2>手機驗證碼登錄</h2>
    <input v-model="phone" placeholder="輸入手機號" />
    <button @click="sendCode" :disabled="isSending">
      {{ isSending ? `重新發送(${countdown}s)` : '獲取驗證碼' }}
    </button>

    <input v-model="code" placeholder="輸入驗證碼" />
    <button @click="login">登錄</button>

    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script>
import { useLogin } from "@/api/login";
import { onUnmounted } from "vue";

export default {
  setup() {
    const { phone, code, message, isSending, countdown, sendCode, login, cleanup } = useLogin();

    // 在組件卸載時清理資源
    onUnmounted(() => {
      cleanup();
    });

    return { phone, code, message, isSending, countdown, sendCode, login };
  },
};
</script>

<style scoped>
.login-container {
  max-width: 400px;
  margin: 50px auto;
  text-align: center;
}
input {
  width: 100%;
  padding: 10px;
  margin: 10px 0;
}
button {
  padding: 10px;
  margin: 10px 0;
  width: 100%;
  cursor: pointer;
}
</style>