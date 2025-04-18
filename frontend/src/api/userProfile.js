import api from './axios';

/**
 * 取得使用者個人資料
 * @param {string} userId
 */
export const getUserProfile = (userId) => {
    return api.get(`/user/${userId}`);
};

/**
 * 追蹤或取消追蹤使用者
 * @param {string} userId
 * @param {boolean} follow - true 表示追蹤，false 表示取消追蹤
 */
export const toggleFollowUser = (userId, follow) => {
    return api.put(`/follow/${userId}/${follow}`);
};

/**
 * 取得與該使用者的共同關注用戶
 * @param {string} id 畫面點選該用戶的id
 */
export const getFollowCommons = (id) => {
    return api.get(`/follow/common/${id}`);
};
