import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080" // backend baseURL
    // timeout: 5000, // api timeout
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
                    localStorage.removeItem("token");
                    if (window.__VUE_APP__) {
                        window.__VUE_APP__.$router.push("/login");
                    } else {
                        window.location.href = "/login"; // 備用方案
                    }
                    break;
                case 403: // 禁止訪問（可能是權限不足）
                    alert("你沒有權限執行此操作！");
                    break;
                case 500: // 伺服器錯誤
                    alert("伺服器發生錯誤，請稍後再試！");
                    break;
                default:
                    console.error("發生錯誤:", error.response);
            }
        } else {
            console.error("無法連接到伺服器:", error);
        }
        return Promise.reject(error);
    }
);

export default api;
