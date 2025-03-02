import axios from "axios";

const api = axios.create({
    baseURL: "http://localhost:8080", // Spring Boot API
    timeout: 5000,
});

// 請求攔截器
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

// 響應攔截器，檢查 token 是否過期
api.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Token 過期，清除 token 並跳轉到登入頁面
            localStorage.removeItem("token");
            window.location.href = "/"; // 跳轉到登入頁
        }
        return Promise.reject(error);
    }
);

export default api;
