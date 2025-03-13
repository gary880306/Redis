import { ref } from "vue";
import api from "@/api/axios";
import { useRouter } from "vue-router";

export function useLogin() {
    const phone = ref("");
    const code = ref("");
    const message = ref("");
    const isSending = ref(false);
    const countdown = ref(60);
    const router = useRouter();
    let timer = null;

    // 發送驗證碼
    const sendCode = async () => {
        // 1. 驗證手機號格式
        if (!phone.value) {
            message.value = "請輸入手機號";
            return;
        }

        // 簡單驗證手機號格式（可根據實際需要調整）
        const phoneRegex = /^[0-9]{10}$/;  // 台灣手機號 10 位數字
        if (!phoneRegex.test(phone.value)) {
            message.value = "請輸入有效的手機號";
            return;
        }

        try {
            isSending.value = true;
            message.value = "正在發送驗證碼...";

            await api.post("/user/code", null, { params: { phone: phone.value } });

            // 倒計時
            clearInterval(timer); // 清除之前的計時器
            countdown.value = 60;
            timer = setInterval(() => {
                countdown.value--;
                if (countdown.value <= 0) {
                    clearInterval(timer);
                    isSending.value = false;
                    countdown.value = 60;
                }
            }, 1000);

            message.value = "驗證碼發送成功";
        } catch (error) {
            console.error("驗證碼發送錯誤", error);

            // 根據錯誤類型顯示不同訊息
            if (error.response && error.response.status === 429) {
                message.value = error.response.data || "請求過於頻繁，請稍後再試";
            } else {
                message.value = "驗證碼發送失敗，請稍後再試";
            }

            isSending.value = false;
        }
    };

    const login = async () => {
        // 1. 表單驗證
        if (!phone.value) {
            message.value = "請輸入手機號";
            return;
        }
        if (!code.value) {
            message.value = "請輸入驗證碼";
            return;
        }

        try {
            message.value = "正在登入...";
            const res = await api.post("/user/login", null, {
                params: { phone: phone.value, code: code.value },
            });

            console.log("登入成功，響應頭：", res.headers);

            // 讀取 Authorization 頭
            const token = res.headers["authorization"];
            if (token) {
                // 存入 localStorage
                const cleanToken = token.replace("Bearer ", "");
                localStorage.setItem("token", cleanToken);
                console.log("Token 存入 localStorage：", cleanToken);

                message.value = "登錄成功，跳轉中...";

                // 清理計時器
                if (timer) {
                    clearInterval(timer);
                }

                setTimeout(() => {
                    router.push("/home");
                }, 1000);
            } else {
                console.error("未收到 token，請檢查後端是否返回 `Authorization`");
                message.value = "未收到驗證信息，請重試";
            }
        } catch (error) {
            console.error("登錄失敗：", error);

            if (error.response && error.response.data) {
                message.value = error.response.data;
            } else {
                message.value = "登錄失敗，請檢查網絡連接";
            }
        }
    };

    // 組件卸載時清理計時器
    const cleanup = () => {
        if (timer) {
            clearInterval(timer);
        }
    };

    return { phone, code, message, isSending, countdown, sendCode, login, cleanup };
}