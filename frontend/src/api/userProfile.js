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


/**
 * 取得目前使用者追蹤對象所發布的文章（支援滾動分頁）
 * @param {number} maxTime 查詢的最大時間戳（後端會取 score <= maxTime 的資料）
 * @param {number} offset 若有多筆資料 score 相同，用此偏移量來跳過已顯示過的資料
 */
export const getFollowedBlogs = (maxTime = Date.now(), offset = 0) => {
    return api.get(`/blog/of/follow`, {
        params: {
            lastId: maxTime,
            offset,
        },
    });
};