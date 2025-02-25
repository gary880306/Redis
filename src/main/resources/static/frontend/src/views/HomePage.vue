<template>
  <div class="home-container">
    <h2>歡迎來到首頁</h2>
    <p>你已成功登錄！</p>
    <button @click="logout">登出</button>
  </div>
</template>

<script>
import { useRouter } from "vue-router";

export default {
  setup() {
    const router = useRouter();

    const logout = () => {
      localStorage.removeItem("token");
      router.push("/");
    };

    return { logout };
  },
};
</script>

<style scoped>
.home-container {
  max-width: 400px;
  margin: 50px auto;
  text-align: center;
}
button {
  padding: 10px;
  margin: 10px 0;
  width: 100%;
  cursor: pointer;
}
</style>