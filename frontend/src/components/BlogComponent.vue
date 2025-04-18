<template>
  <!-- HomePage 頁面 Blog 區塊 -->
  <section class="latest-blogs" v-if="blogs && blogs.length > 0">
    <div class="section-container">
      <div class="section-header">
        <h2>最新探店文章</h2>
        <div class="blog-actions">
          <el-button @click="openBlogModal" type="primary" plain>
            <el-icon>
              <EditPen/>
            </el-icon>
            發布探店文章
          </el-button>
          <el-button text @click="viewAllBlogs">查看全部
            <el-icon>
              <ArrowRight/>
            </el-icon>
          </el-button>
        </div>
      </div>

      <div v-if="blogsLoading" class="blogs-loading">
        <el-skeleton :rows="3" animated/>
      </div>

      <div v-else-if="blogs.length === 0" class="no-blogs">
        <el-empty description="暫無探店文章"/>
      </div>

      <div v-else class="blogs-grid">
        <div class="blog-card" v-for="blog in blogs" :key="blog.id" @click="openDetailModal(blog)">
          <div class="blog-image">
            <el-image v-if="blog.images" :src="getBlogImageUrl(blog.images.split(',')[0])" fit="cover"
                      class="blog-thumbnail"/>
            <div v-else class="blog-image-placeholder">
              <el-icon :size="32">
                <Picture/>
              </el-icon>
            </div>
          </div>
          <div class="blog-info">
            <h3 class="blog-title">{{ blog.title }}</h3>
            <p class="blog-excerpt">{{ truncateText(blog.content, 80) }}</p>
            <div class="blog-meta">
              <span class="blog-author">
                {{ blog.userInfo ? blog.userInfo.username : '匿名用戶' }}
              </span>
              <span class="blog-date">{{ formatTime(blog.createTime) }}</span>
              <div class="blog-stats">
                <span class="blog-likes">
                  <el-icon><Star/></el-icon> {{ blog.liked || 0 }}
                </span>
                <span class="blog-comments">
                  <el-icon><ChatDotRound/></el-icon> {{ blog.comments || 0 }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </section>

  <!-- Blog 發布探店文章 Modal -->
  <el-dialog v-model="blogDialogVisible" title="發布探店體驗" width="650px" destroy-on-close class="blog-dialog">
    <el-form ref="blogFormRef" :model="blogForm" :rules="blogRules" label-position="top">
      <el-form-item label="標題" prop="title">
        <el-input v-model="blogForm.title" placeholder="請輸入文章標題（必填）" maxlength="50" show-word-limit/>
      </el-form-item>

      <el-form-item label="選擇商店" prop="shopId">
        <el-select v-model="blogForm.shopId" placeholder="請選擇要評價的商店" filterable style="width: 100%">
          <el-option v-for="shop in shops" :key="shop.shopId" :label="shop.name" :value="shop.shopId"/>
        </el-select>
      </el-form-item>

      <el-form-item label="上傳照片（最多 9 張）">
        <el-upload v-model:file-list="blogImageFiles" class="blog-upload" action="#" list-type="picture-card" :limit="9"
                   :on-preview="handlePreview" :on-change="handleImageChange" :auto-upload="false" :multiple="true">
          <el-icon>
            <Plus/>
          </el-icon>
        </el-upload>

        <el-dialog v-model="previewVisible">
          <img class="preview-image" :src="previewImage" alt="Preview"/>
        </el-dialog>
      </el-form-item>

      <el-form-item label="分享內容" prop="content">
        <el-input v-model="blogForm.content" type="textarea" :rows="5" placeholder="分享你的探店心得..."
                  maxlength="2000" show-word-limit/>
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="blogDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="publishingBlog" @click="submitBlog">發布</el-button>
      </span>
    </template>
  </el-dialog>

  <!-- Blog 文章詳情 Modal -->
  <el-dialog v-model="showModal" :title="selectedBlog ? selectedBlog.title : '部落格詳情'" width="650px"
             @close="closeModal">
    <div v-if="selectedBlog" class="blog-detail">
      <div class="blog-detail-images">
        <el-carousel v-if="selectedBlog.images" trigger="click" height="300px">
          <el-carousel-item v-for="(image, index) in selectedBlog.images.split(',')" :key="index">
            <el-image :src="getBlogImageUrl(image)" fit="cover" class="blog-detail-image"/>
          </el-carousel-item>
        </el-carousel>
      </div>

      <div class="blog-detail-content">
        <div class="blog-detail-meta">
          <span>
            {{ selectedBlog.content }}
          </span>
          <span>
            <el-button
                v-if="selectedBlog.userId !== loginUserId"
                type="danger"
                :plain="!isFollowing"
                @click="handleFollowClick"
                :loading="followLoading"
                size="small"
            >
  {{ isFollowing ? '已關注' : '關注' }}
</el-button>
          </span>
        </div>

        <div class="blog-detail-meta">
          <!-- 點擊頭像跳轉 -->
          <el-avatar
              class="user-avatar clickable-avatar"
              :size="32"
              :src="getUserAvatarUrl(selectedBlog.userInfo.avatar)"
              @click="goToUserProfile(selectedBlog.userInfo?.userId)"
          />

          <!-- 使用者名稱（純顯示） -->
          <span class="user-name">
            {{ selectedBlog.userInfo ? selectedBlog.userInfo.username : '匿名用戶' }}
          </span>

          <!-- 時間區塊（你說要保留） -->
          <span class="blog-time">
            <el-icon><Calendar/></el-icon>
            {{ formatTime(selectedBlog.createTime) }}
          </span>
        </div>


        <div class="blog-actions">
          <el-button @click="handleLike" :type="selectedBlog.isLike ? 'primary' : 'default'">
            <el-icon>
              <Star/>
            </el-icon>
            {{ selectedBlog.isLike ? '收回讚' : '讚' }} {{ selectedBlog.liked || 0 }}
          </el-button>

          <!-- 點讚用戶頭像列表 -->
          <div class="like-users">
            <el-avatar
                v-for="user in likes"
                :key="user.userId"
                :size="32"
                :src="getUserAvatarUrl(user.avatar)"
                class="user-avatar"
            />
          </div>

          <el-button @click="handleShare">
            <el-icon>
              <Share/>
            </el-icon>
            分享
          </el-button>
        </div>

        <div class="comments-section">
          <h3>評論 ({{ comments.length }})</h3>

          <div class="comment-input">
            <el-input v-model="commentText" placeholder="寫下你的評論..." @keyup.enter="submitComment">
              <template #append>
                <el-button type="primary" @click="submitComment">發送</el-button>
              </template>
            </el-input>
          </div>

          <div v-if="comments.length === 0" class="no-comments">
            <el-empty description="暫無評論"/>
          </div>

          <div v-else class="comments-list">
            <div v-for="comment in comments" :key="comment.id" class="comment">
              <div class="comment-header">
                <span class="comment-user">{{ comment.user }}</span>
                <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import {ref, onMounted, watch, computed} from 'vue'; // mounted

import {
  Picture,
  EditPen,
  ArrowRight,
  Star,
  ChatDotRound,
  Plus,
  Calendar,
  Share
} from '@element-plus/icons-vue'; // icon

import {
  useBlogList,
  useBlogPublish,
  useBlogDetailModal,
  useBlogPublishModal,
  blogUtils,
  followUser,
  checkIsFollow
} from '@/api/blog'; // function

import {ElMessage} from 'element-plus';

import {useRouter} from 'vue-router';

import { useUserStore } from '@/stores/userStore';

export default {
  name: 'BlogComponent',
  computed: {
    blogUtils() {
      return blogUtils
    }
  },
  components: {
    Picture,
    EditPen,
    ArrowRight,
    Star,
    ChatDotRound,
    Plus,
    Calendar,
    Share
  },
  setup() {
    const userStore = useUserStore(); // store userInfo
    const loginUserId = computed(() => userStore.userInfo?.userId);
    // 獲取Blog列表
    const {
      latestBlogs,
      blogsLoading,
      fetchLatestBlogs,
      viewBlogDetail,
      viewAllBlogs
    } = useBlogList();

    // Blog發布
    const {
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
    } = useBlogPublish();

    // Blog詳情Modal
    const {
      showModal,
      selectedBlog,
      commentText,
      comments,
      likes,
      openBlogModal: openDetailModal,
      closeModal,
      handleLike,
      handleShare,
      submitComment
    } = useBlogDetailModal();

    // Blog發布Modal
    const {
      blogDialogVisible,
      shops,
      fetchShops,
      openBlogModal,
      submitBlog: submitBlogFn
    } = useBlogPublishModal();

    // 關注功能相關
    const isFollowing = ref(false);
    const followLoading = ref(false);

    // 檢查是否已關注
    const checkFollowStatus = async (userId) => {
      if (!userId) return;

      try {
        const res = await checkIsFollow(userId);
        if (res.data && res.data.code === 200) {
          isFollowing.value = res.data.data;
        }
      } catch (error) {
        console.error('檢查關注狀態失敗:', error);
      }
    };

    // 處理關注/取消關注
    const handleFollowClick = async () => {
      if (!selectedBlog.value || !selectedBlog.value.userId) {
        ElMessage.warning('無法獲取作者信息');
        return;
      }

      const userId = selectedBlog.value.userId;
      const toFollow = !isFollowing.value;
      followLoading.value = true;

      try {
        const res = await followUser(userId, toFollow);
        if (res.data && res.data.code === 200) {
          isFollowing.value = toFollow;
          ElMessage.success(toFollow ? '關注成功' : '已取消關注');
        } else {
          ElMessage.error(res.data?.message || '操作失敗');
        }
      } catch (error) {
        console.error('關注操作失敗:', error);
        ElMessage.error('操作失敗，請稍後再試');
      } finally {
        followLoading.value = false;
      }
    };

    // 監聽選中的博客變化，檢查關注狀態
    watch(() => selectedBlog.value, (newBlog) => {
      if (newBlog && newBlog.userId) {
        checkFollowStatus(newBlog.userId);
      }
    });

    // 自定義打開詳情模態框函數
    const openBlogDetail = async (blog) => {
      // 調用原有的打開詳情函數
      openDetailModal(blog);

      // 檢查關注狀態
      if (blog.userId) {
        await checkFollowStatus(blog.userId);
      }
    };

    // 封裝提交Blog方法
    const submitBlog = () => {
      const success = submitBlogFn(
          blogFormRef,
          blogForm,
          publishingBlog,
          uploadAllImages
      );
      if (success) {
        fetchLatestBlogs(); // 重新獲取最新Blog
      }
    };

    // 跳轉到博客頁面
    const router = useRouter();

    const goToUserProfile = (userId) => {
      if (!userId) return;
      router.push(`/user/${userId}`);
    };

    // 初始化
    onMounted(() => {
      fetchLatestBlogs();
      fetchShops();
    });

    return {
      loginUserId,
      // Blog列表
      blogs: latestBlogs,
      blogsLoading,

      // 發布功能
      blogFormRef,
      blogForm,
      blogRules,
      blogImageFiles,
      previewVisible,
      previewImage,
      publishingBlog,
      handlePreview,
      handleImageChange,
      submitBlog,
      openBlogModal,
      blogDialogVisible,

      // Blog詳情
      showModal,
      selectedBlog,
      commentText,
      comments,
      likes,
      openDetailModal: openBlogDetail, // 使用新的打開詳情函數
      closeModal,
      handleLike,
      handleShare,
      submitComment,

      // 關注功能
      isFollowing,
      followLoading,
      handleFollowClick,

      // Shop列表
      shops,

      // 工具方法
      getBlogImageUrl: blogUtils.getBlogImageUrl,
      getUserAvatarUrl: blogUtils.getUserAvatarUrl,
      truncateText: blogUtils.truncateText,
      formatTime: blogUtils.formatTime,
      viewBlogDetail,
      viewAllBlogs,

      // 跳轉博客頁面
      goToUserProfile
    };
  }
};
</script>

<style scoped>
@import '@/assets/styles/blog.css';
</style>