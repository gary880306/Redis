import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080", // backend baseURL
    timeout: 30000, // 超時時間30秒
});

// api request interceptor
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("token");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// response interceptor
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response) {
            switch (error.response.status) {
                case 401: // 未授權，可能 token 過期
                    // 只有當路徑不是 /login 時才跳轉
                    if (!window.location.pathname.includes('/login')) {
                        console.warn('登入已過期，請重新登入');
                        localStorage.removeItem("token");

                        // 使用 Vue Router 或備用導航
                        if (window.__VUE_APP__ && window.__VUE_APP__.$router) {
                            window.__VUE_APP__.$router.push("/");
                        } else {
                            window.location.href = "/";
                        }
                    }
                    break;
                case 403: // 禁止訪問（可能是權限不足）
                    console.error("權限不足，無法執行此操作");
                    // 可以彈出提示或跳轉到錯誤頁面
                    break;
                case 429: // 請求過於頻繁
                    console.error("請求過於頻繁，請稍後再試");
                    break;
                case 500: // 伺服器錯誤
                    console.error("伺服器發生錯誤，請稍後再試");
                    break;
                default:
                    console.error("發生錯誤:", error.response);
            }
        } else if (error.request) {
            // 請求已發出但沒有收到回應
            console.error("無法連接到伺服器，請檢查網絡連接");
        } else {
            // 請求配置有錯誤
            console.error("請求配置錯誤:", error.message);
        }
        return Promise.reject(error);
    }
);

export default api;