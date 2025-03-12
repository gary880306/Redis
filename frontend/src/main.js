import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
import api from "./api/axios";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

const app = createApp(App);
app.use(router);
app.use(ElementPlus);
app.config.globalProperties.$router = router; // 設定全局 router
app.config.globalProperties.$api = api; // 設定全局 axios
window.__VUE_APP__ = app;
app.mount("#app");
