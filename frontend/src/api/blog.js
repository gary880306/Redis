import {ref, reactive} from 'vue';
import api from './axios';
import {ElMessage} from 'element-plus';

// API start
/**
 * 獲取最新Blog文章
 * @param {number} limit - 限制返回的數量，默認為4
 * @returns {Promise} - API回應
 */
export const getLatestBlogs = (limit = 4) => {
    return api.get(`/blog/latest?limit=${limit}`);
};

/**
 * 獲取Blog詳情
 * @param {number} id - blogId
 * @returns {Promise} - API回應
 */
export const getBlogDetail = (id) => {
    return api.get(`/blog/${id}`);
};

/**
 * 獲取Blog按讚排行榜
 * @param {number} id - blogId
 * @returns {Promise} - API回應
 */
export const getBlogLikes = (id) => {
    return api.get(`/blog/likes/${id}`);
};

/**
 * Blog按讚
 * @param {number} id - blogId
 * @returns {Promise} - API回應
 */
export const likeBlog = (id) => {
    return api.put(`/blog/like/${id}`);
};

/**
 * 上傳圖片
 * @param {FormData} formData - 包含圖片的FormData
 * @returns {Promise} - API回應
 */
export const uploadImage = (formData) => {
    return api.post('/upload', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
};
// API end


// function start
/**
 * Blog列表功能模組
 */
export function useBlogList() {
    const latestBlogs = ref([]);
    const blogsLoading = ref(false);

    // 獲取最新Blog列表
    const fetchLatestBlogs = async (limit = 4) => {
        blogsLoading.value = true;
        try {
            const response = await getLatestBlogs(limit);
            if (response.data && response.data.code === 200) {
                latestBlogs.value = response.data.data || [];
            }
        } catch (error) {
            console.error('獲取最新Blog失敗:', error);
        } finally {
            blogsLoading.value = false;
        }
    };

    // 查看Blog詳情
    const viewBlogDetail = (id) => {
        window.location.href = `/blog/${id}`;
    };

    // 查看全部Blog
    const viewAllBlogs = () => {
        window.location.href = '/blog';
    };

    return {
        latestBlogs,
        blogsLoading,
        fetchLatestBlogs,
        viewBlogDetail,
        viewAllBlogs
    };
}

/**
 * Blog發布功能模組
 */
export function useBlogPublish() {
    const blogFormRef = ref(null);
    const blogForm = reactive({
        title: '',
        shopId: '',
        content: '',
        images: ''
    });

    const blogRules = {
        title: [
            {required: true, message: '請輸入標題', trigger: 'blur'},
            {min: 3, max: 50, message: '標題長度需介於 3 到 50 個字元之間', trigger: 'blur'}
        ],
        shopId: [
            {required: true, message: '請選擇商店', trigger: 'change'}
        ],
        content: [
            {required: true, message: '請輸入分享內容', trigger: 'blur'},
            {min: 10, max: 2000, message: '內容長度需介於 10 到 2000 個字元之間', trigger: 'blur'}
        ]
    };

    const blogImageFiles = ref([]);
    const previewVisible = ref(false);
    const previewImage = ref('');
    const publishingBlog = ref(false);

    const handlePreview = (file) => {
        previewImage.value = file.url || URL.createObjectURL(file.raw);
        previewVisible.value = true;
    };

    const handleImageChange = (file, fileList) => {
        // 驗證文件類型和大小
        const isImage = file.raw.type.indexOf('image/') !== -1;
        const isLt2M = file.raw.size / 1024 / 1024 < 2;

        if (!isImage) {
            ElMessage.error('只能上傳圖片文件!');
            const index = fileList.findIndex(item => item.uid === file.uid);
            if (index !== -1) {
                fileList.splice(index, 1);
            }
            return false;
        }
        if (!isLt2M) {
            ElMessage.error('圖片大小不能超過 2MB!');
            const index = fileList.findIndex(item => item.uid === file.uid);
            if (index !== -1) {
                fileList.splice(index, 1);
            }
            return false;
        }

        blogImageFiles.value = fileList;
        return true;
    };

    const uploadAllImages = async () => {
        if (blogImageFiles.value.length === 0) {
            return '';
        }

        const uploadPromises = blogImageFiles.value.map(file => {
            const formData = new FormData();
            formData.append('file', file.raw);
            return uploadImage(formData);
        });

        try {
            const results = await Promise.all(uploadPromises);
            const imageNames = results.map(res => res.data.data);
            return imageNames.join(',');
        } catch (error) {
            console.error('上傳圖片失敗:', error);
            throw new Error('上傳圖片失敗，請重試');
        }
    };

    return {
        blogFormRef,
        blogForm,
        blogRules,
        blogImageFiles,
        previewVisible,
        previewImage,
        publishingBlog,
        handlePreview,
        handleImageChange,
        uploadAllImages
    };
}

/**
 * 工具函數
 */
export const blogUtils = {
    // 獲取Blog圖片 URL
    getBlogImageUrl: (imagePath) => {
        if (!imagePath) return '';
        return `http://localhost:8080/images/upload/blog/${imagePath}`;
    },

    // 獲取用戶頭像 URL
    getUserAvatarUrl: (avatar) => {
        if (!avatar) {
            return '';
        }
        return `http://localhost:8080/images/upload/avatar/${avatar}`;
    },

    // 文字截斷功能
    truncateText: (text, maxLength) => {
        if (!text) return '';
        return text.length > maxLength
            ? text.substring(0, maxLength) + '...'
            : text;
    },

    // 格式化時間
    formatTime: (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('zh-TW', {
            year: 'numeric',
            month: '2-digit',
            day: '2-digit'
        });
    }
};

/**
 * Blog詳情模態框功能模組
 */
export function useBlogDetailModal() {
    const showModal = ref(false);
    const selectedBlog = ref(null);
    const commentText = ref('');
    const commentInput = ref(null);
    const comments = ref([]);
    const likes = ref(null);

    // 打開Blog彈窗
    const openBlogModal = async (blog) => {
        // 先顯示已有的基本資訊
        selectedBlog.value = {...blog, isLike: false};
        showModal.value = true;

        try {
            // 再從API獲取完整資訊
            const response = await getBlogDetail(blog.id);
            const likeList = await getBlogLikes(blog.id);
            likes.value = likeList.data.data;
            if (response.data && response.data.code === 200) {
                // 保留已設定的 isLiked 狀態
                selectedBlog.value = {
                    ...response.data.data
                };

                // TODO:同時獲取評論
                // const commentsResponse = await getComments(blog.id);
                // if (commentsResponse.data && commentsResponse.data.code === 200) {
                //     comments.value = commentsResponse.data.data || [];
                // }
            }
        } catch (error) {
            console.error('獲取Blog詳情失敗:', error);
            ElMessage.error('獲取詳情失敗，顯示簡略資訊');
        }
    };

    // 關閉Blog彈窗
    const closeModal = () => {
        showModal.value = false;
        selectedBlog.value = null;
        commentText.value = '';
    };

    // 處理點讚
    const handleLike = async () => {

        // 先在前端更新顯示狀態（樂觀更新）
        const isCurrentlyLiked = selectedBlog.value.isLike;

        // 更新按鈕顯示狀態
        selectedBlog.value.isLike = !isCurrentlyLiked;

        // 更新讚數（如果是取消讚，則減1；如果是點讚，則加1）
        selectedBlog.value.liked = isCurrentlyLiked
            ? Math.max(0, (selectedBlog.value.liked || 0) - 1)
            : (selectedBlog.value.liked || 0) + 1;

        try {
            // 點讚API
            await likeBlog(selectedBlog.value.id);

            // API呼叫成功後重新獲取點讚列表
            const likeListResponse = await getBlogLikes(selectedBlog.value.id);
            if (likeListResponse.data && likeListResponse.data.code === 200) {
                likes.value = likeListResponse.data.data;
            }
        } catch (error) {
            // 如果API呼叫失敗，回復原狀
            console.error('點讚操作失敗:', error);
            selectedBlog.value.isLike = isCurrentlyLiked;
            selectedBlog.value.liked = isCurrentlyLiked
                ? (selectedBlog.value.liked || 0) + 1
                : Math.max(0, (selectedBlog.value.liked || 0) - 1);
            ElMessage.error('操作失敗，請重試');
        }
    };

    // 焦點到評論框
    const focusComment = () => {
        if (commentInput.value) {
            commentInput.value.focus();
        }
    };

    // 處理分享
    const handleShare = () => {
        ElMessage.info('分享功能尚待實現');
    };

    // TODO:提交評論
    const submitComment = () => {
        if (!commentText.value.trim()) {
            ElMessage.warning('評論內容不能為空');
            return;
        }

        // 模擬提交評論
        const newComment = {
            id: comments.value.length + 1,
            user: '當前用戶',
            content: commentText.value,
            createTime: new Date()
        };

        comments.value.unshift(newComment);

        // 更新評論數
        if (selectedBlog.value) {
            selectedBlog.value.comments = (selectedBlog.value.comments || 0) + 1;
        }

        // 清空評論框
        commentText.value = '';

        ElMessage.success('評論發佈成功');

        // 實際應該呼叫API
        // postComment(selectedBlog.value.id, commentText.value);
    };

    return {
        showModal,
        selectedBlog,
        likes,
        commentText,
        commentInput,
        comments,
        openBlogModal,
        closeModal,
        handleLike,
        focusComment,
        handleShare,
        submitComment
    };
}

/**
 * Blog發布模態框功能模組
 */
export function useBlogPublishModal() {
    const blogDialogVisible = ref(false);
    const shops = ref([]);

    // 獲取商店列表
    const fetchShops = async () => {
        try {
            const res = await api.get("/shop/all");
            shops.value = res.data.data || [];
        } catch (error) {
            console.error("獲取商店列表失敗:", error);
            ElMessage.error("獲取商店列表失敗");
        }
    };

    // 打開發布Blog模態框
    const openBlogModal = () => {
        blogDialogVisible.value = true;
    };

    // 提交Blog
    const submitBlog = async (blogFormRef, blogForm, publishingBlog, uploadAllImages) => {
        if (!blogFormRef.value) return false;

        try {
            let success = false;
            await blogFormRef.value.validate(async (valid) => {
                if (valid) {
                    publishingBlog.value = true;

                    // 上傳圖片
                    const imageNames = await uploadAllImages();

                    // 構建Blog數據
                    const blogData = {
                        ...blogForm,
                        images: imageNames
                    };

                    // 發布Blog
                    const res = await api.post('/blog', blogData);

                    if (res.data.code === 200) {
                        ElMessage.success('文章發布成功');
                        blogDialogVisible.value = false;
                        success = true;
                    } else {
                        ElMessage.error(res.data.message || '文章發布失敗');
                    }
                }
            });
            return success;
        } catch (error) {
            console.error('Blog發布錯誤:', error);
            ElMessage.error('文章發布失敗');
            return false;
        } finally {
            publishingBlog.value = false;
        }
    };

    return {
        blogDialogVisible,
        shops,
        fetchShops,
        openBlogModal,
        submitBlog
    };
}
// function end