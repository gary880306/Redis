<template>
  <div class="home-container">
    <h2>首頁</h2>
    <button @click="testApi">Test</button>
    <p v-if="message">{{ message }}</p>
  </div>
</template>

<script>
import { ref } from "vue";
import api from "@/api/axios"; // 確保 axios.js 有攔截 Authorization
export default {
  setup() {
    const message = ref("");

    const testApi = async () => {
      try {
        const res = await api.get("/user/test");
        console.log("API 回應:", res.data);
        message.value = res.data;
      } catch (error) {
        console.error("API 錯誤:", error);
        message.value = error.response?.data || "請求失敗";
      }
    };

    return { message, testApi };
  },
};
</script>

<style scoped>
.home-container {
  text-align: center;
  margin-top: 50px;
}
button {
  padding: 10px;
  margin-top: 20px;
  cursor: pointer;
}
</style>