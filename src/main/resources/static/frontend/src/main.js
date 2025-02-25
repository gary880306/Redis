import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import api from "./api/axios";

const app = createApp(App);
app.use(router);
app.config.globalProperties.$api = api; // 設定全局 axios
app.mount("#app");
