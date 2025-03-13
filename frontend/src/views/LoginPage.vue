<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">歡迎回來</h1>
        <p class="login-subtitle">使用手機驗證碼輕鬆登入您的帳戶</p>
      </div>

      <div class="login-form">
        <div class="form-group">
          <label for="phone">手機號碼</label>
          <el-input
              v-model="phone"
              placeholder="請輸入您的手機號碼"
              maxlength="10"
              :formatter="value => value.replace(/[^0-9]/g, '')"
              clearable
              class="custom-input"
          >
            <template #suffix>
              <span class="input-counter">{{ phone.length }} / 10</span>
            </template>
          </el-input>
        </div>

        <div class="form-group">
          <label for="code">驗證碼</label>
          <div class="code-group">
            <el-input
                v-model="code"
                placeholder="請輸入驗證碼"
                maxlength="6"
                :formatter="value => value.replace(/[^0-9]/g, '')"
                clearable
                class="custom-input"
            >
              <template #suffix>
                <span class="input-counter">{{ code.length }} / 6</span>
              </template>
            </el-input>
            <el-button
                type="primary"
                :disabled="isSending || !isPhoneValid"
                class="code-button"
                @click="sendCode"
            >
              {{ isSending ? `${countdown}秒` : '獲取驗證碼' }}
            </el-button>
          </div>
        </div>

        <div class="login-message" v-if="message">
          <el-alert
              :title="message"
              :type="messageType"
              :closable="false"
              show-icon
          />
        </div>

        <el-button
            type="primary"
            class="login-button"
            :disabled="!isFormValid"
            :loading="isLoading"
            @click="login"
        >
          登入
        </el-button>

        <div class="login-options">
          <el-checkbox v-model="rememberPhone">記住手機號</el-checkbox>
          <a href="#" class="help-link">需要幫助？</a>
        </div>
      </div>

      <div class="login-footer">
        <p>還沒有帳戶？<a href="#" class="register-link">立即註冊</a></p>
      </div>
    </div>

    <div class="login-decoration">
      <img src="/images/login-banner.jpg" alt="購物體驗" class="decoration-image" />
      <div class="decoration-overlay">
        <h2>探索最優質的購物體驗</h2>
        <p>享受會員專屬優惠與快速配送服務</p>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted } from "vue";
import { useLogin } from "@/api/login";
import { ElMessage } from "element-plus";

export default {
  setup() {
    const { phone, code, message, isSending, countdown, sendCode, login: doLogin, cleanup } = useLogin();
    const rememberPhone = ref(false);
    const isLoading = ref(false);
    const messageType = ref("info");

    // 驗證手機號格式
    const isPhoneValid = computed(() => {
      const phoneRegex = /^[0-9]{10}$/;  // 台灣手機號 10 位數字
      return phoneRegex.test(phone.value);
    });

    // 表單驗證
    const isFormValid = computed(() => {
      return isPhoneValid.value && code.value.length === 6;
    });

    // 封裝發送驗證碼功能
    const handleSendCode = async () => {
      if (!isPhoneValid.value) {
        ElMessage.warning("請輸入有效的手機號碼");
        return;
      }

      try {
        await sendCode();
        messageType.value = "success";
      } catch (error) {
        messageType.value = "error";
      }
    };

    // 封裝登入功能
    const handleLogin = async () => {
      if (!isFormValid.value) {
        ElMessage.warning("請填寫完整的登入資訊");
        return;
      }

      isLoading.value = true;

      try {
        await doLogin();
        messageType.value = "success";

        // 如果選擇記住手機號，則存儲到本地
        if (rememberPhone.value) {
          localStorage.setItem("rememberedPhone", phone.value);
        } else {
          localStorage.removeItem("rememberedPhone");
        }
      } catch (error) {
        messageType.value = "error";
      } finally {
        isLoading.value = false;
      }
    };

    // 載入記住的手機號
    onMounted(() => {
      const savedPhone = localStorage.getItem("rememberedPhone");
      if (savedPhone) {
        phone.value = savedPhone;
        rememberPhone.value = true;
      }
    });

    // 組件卸載時清理資源
    onUnmounted(() => {
      cleanup();
    });

    return {
      phone,
      code,
      message,
      messageType,
      isSending,
      countdown,
      rememberPhone,
      isLoading,
      isPhoneValid,
      isFormValid,
      sendCode: handleSendCode,
      login: handleLogin
    };
  },
};
</script>

<style scoped>
.login-container {
  display: flex;
  min-height: 100vh;
  background-color: #f9f9f9;
}

.login-card {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2rem 3rem;
  max-width: 450px;
  background-color: #fff;
}

.login-decoration {
  flex: 1;
  position: relative;
  display: none;
  background-color: #e0e0e0;
}

@media (min-width: 1024px) {
  .login-decoration {
    display: block;
  }
}

.decoration-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.decoration-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(rgba(0, 0, 0, 0.4), rgba(0, 0, 0, 0.7));
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  padding: 2rem;
}

.decoration-overlay h2 {
  font-size: 2.2rem;
  margin-bottom: 1rem;
  font-weight: 600;
}

.decoration-overlay p {
  font-size: 1.1rem;
  max-width: 400px;
}

.login-header {
  margin-bottom: 2.5rem;
}

.login-title {
  font-size: 1.8rem;
  font-weight: 600;
  color: #333;
  margin-bottom: 0.5rem;
}

.login-subtitle {
  color: #666;
  font-size: 0.95rem;
}

.login-form {
  margin-bottom: 2rem;
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 500;
  color: #333;
  font-size: 0.95rem;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 4px;
  box-shadow: 0 0 0 1px #dcdfe6;
  padding: 0 12px;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff;
}

.input-counter {
  font-size: 12px;
  color: #909399;
}

.code-group {
  display: flex;
  gap: 12px;
}

.code-group .custom-input {
  flex: 1;
}

.code-button {
  min-width: 110px;
  font-size: 14px;
  border-radius: 4px;
}

.login-message {
  margin: 1rem 0;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  margin-top: 1rem;
  border-radius: 4px;
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 1rem;
  font-size: 14px;
}

.help-link {
  color: #666;
  text-decoration: none;
  transition: color 0.2s;
}

.help-link:hover {
  color: #409eff;
}

.login-footer {
  text-align: center;
  color: #666;
  margin-top: auto;
  padding-top: 2rem;
  font-size: 14px;
}

.register-link {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.register-link:hover {
  color: #66b1ff;
}
</style>