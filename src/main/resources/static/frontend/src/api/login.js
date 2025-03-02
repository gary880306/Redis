import { ref } from "vue";
import api from "@/api/axios"; // 確保 axios.js 正確設置
import { useRouter } from "vue-router";

export function useLogin() {
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
            await api.post("/user/code", null, { params: { phone: phone.value } });

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

    const login = async () => {
        if (!phone.value || !code.value) {
            message.value = "請輸入手機號和驗證碼";
            return;
        }
        try {
            const res = await api.post("/user/login", null, {
                params: { phone: phone.value, code: code.value },
            });

            console.log("Axios Response Headers:", res.headers);

            // **正確讀取 `Authorization`**
            const token = res.headers["authorization"]; // **這裡要用小寫！**
            if (token) {
                localStorage.setItem("token", token.replace("Bearer ", ""));
                console.log("Token 存入 localStorage：", token.replace("Bearer ", ""));
                message.value = "登錄成功，跳轉中...";
                setTimeout(() => {
                    router.push("/home");
                }, 1000);
            } else {
                console.error("未收到 token，請檢查後端是否返回 `Authorization`");
                message.value = "未收到 token，請重試";
            }
        } catch (error) {
            console.error("登錄失敗，錯誤訊息：", error);
            message.value = error.response?.data || "登錄失敗";
        }
    };


    return { phone, code, message, isSending, countdown, sendCode, login };
}