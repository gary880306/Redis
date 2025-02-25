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
import { ref } from "vue";
import axios from "axios";
import { useRouter } from "vue-router";

export default {
  setup() {
    const phone = ref("");
    const code = ref("");
    const message = ref("");
    const isSending = ref(false);
    const countdown = ref(60);
    const router = useRouter();

    // 發送驗證碼
    const sendCode = async () => {
      if (!phone.value) {
        message.value = "請輸入手機號";
        return;
      }
      try {
        isSending.value = true;
        message.value = "正在發送驗證碼...";
        await axios.post("http://localhost:8080/user/code", null, {
          params: { phone: phone.value }
        });

        // 倒計時
        let timer = setInterval(() => {
          countdown.value--;
          if (countdown.value <= 0) {
            clearInterval(timer);
            isSending.value = false;
            countdown.value = 60;
          }
        }, 1000);

        message.value = "驗證碼發送成功";
      } catch (error) {
        console.error(error);
        message.value = "發送失敗";
        isSending.value = false;
      }
    };

    // 登錄
    const login = async () => {
      if (!phone.value || !code.value) {
        message.value = "請輸入手機號和驗證碼";
        return;
      }
      try {
        const res = await axios.post("http://localhost:8080/user/login", null, {
          params: { phone: phone.value, code: code.value }
        });

        if (res.data.includes("token")) {
          const token = res.data.split(": ")[1];
          localStorage.setItem("token", token);
          message.value = "登錄成功，跳轉中...";
          setTimeout(() => {
            router.push("/home"); // 登錄成功後跳轉首頁
          }, 1000);
        } else {
          message.value = res.data;
        }
      } catch (error) {
        console.error(error);
        message.value = "登錄失敗";
      }
    };

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