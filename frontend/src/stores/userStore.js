import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useUserStore = defineStore('user', () => {
    const userInfo = ref(null);

    function setUser(user) {
        userInfo.value = user;
    }

    function clearUser() {
        userInfo.value = null;
        localStorage.removeItem('user');
    }

    function loadUser() {
        const saved = localStorage.getItem('user');
        if (saved) {
            try {
                userInfo.value = JSON.parse(saved);
            } catch (e) {
                console.warn('解析 localStorage user 失敗');
            }
        }
    }

    return {
        userInfo,
        setUser,
        clearUser,
        loadUser,
    };
});