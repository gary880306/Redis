<template>
  <div class="profile-page">
    <div class="cover-photo"></div>

    <div class="profile-container">
      <div class="profile-header">
        <img class="avatar" :src="blogUtils.getUserAvatarUrl(user.avatar)"/>
        <h2 class="username">{{ user.username }}</h2>
        <p class="userid">@{{ user.userId }}</p>

        <button class="follow-btn" @click="toggleFollow">
          {{ isFollowing ? '已追蹤' : '追蹤' }}
        </button>
      </div>

      <!-- Tabs -->
      <el-tabs v-model="activeTab" class="user-tabs">
        <el-tab-pane label="追蹤中" name="articles"/>
        <el-tab-pane label="共同關注" name="mutuals"/>
        <el-tab-pane label="小卡" name="cards"/>
      </el-tabs>

      <!-- 追蹤文章 Tab -->
      <div v-if="activeTab === 'articles'">
        <div v-if="blogList.length === 0 && !blogLoading">尚無追蹤文章</div>

        <div v-for="post in blogList" :key="post.id" class="post-card social-post">
          <div class="post-content-wrap">
            <div class="post-main">
              <div class="post-meta">
                <div class="post-user-info">
                  <span class="post-author">{{ post.userInfo?.username || '匿名用戶' }}</span>
                </div>
              </div>

              <div class="post-title">#{{ post.topic }} {{ post.title }}</div>
              <div class="post-preview">
                {{ post.content.slice(0, 80) }}{{ post.content.length > 80 ? '…' : '' }}
              </div>

              <div class="post-actions social-icons">
                <span>❤️ {{ post.liked || 0 }}</span>
                <span>💬 {{ post.commentCount || 0 }}</span>
                <span>🔖 {{ post.bookmarked || 0 }}</span>
              </div>
            </div>

            <div v-if="post.images" class="post-thumbnail">
              <img
                  class="post-blog"
                  :src="blogUtils.getBlogImageUrl(post.images)"
                  alt="blog images"
              />
            </div>
          </div>
        </div>

        <div v-if="blogLoading" style="text-align:center;padding: 10px;">
          載入中...
        </div>
      </div>

      <!-- 共同關注 Tab -->
      <div v-else-if="activeTab === 'mutuals'">
        <div v-if="mutuals.length === 0">沒有共同關注的用戶</div>
        <div v-else class="mutual-user-list">
          <div class="mutual-user" v-for="m in mutuals" :key="m.userId">
            <img class="mutual-avatar" :src="blogUtils.getUserAvatarUrl(m.avatar)"/>
            <div class="mutual-info">
              <div class="mutual-name">{{ m.username }}</div>
              <div class="mutual-id">@{{ m.userId }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 小卡 Tab -->
      <div v-else-if="activeTab === 'cards'">
        <p>這裡顯示使用者的小卡（之後可串資料）</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref, onMounted, watch, onBeforeUnmount} from 'vue';
import {useRoute} from 'vue-router';
import {getFollowCommons, getUserProfile, getFollowedBlogs} from '@/api/userProfile';
import {blogUtils, useFollow} from '@/api/blog';

const route = useRoute();
const user = ref({});
const mutuals = ref([]);
const activeTab = ref('articles');
const {isFollowing, handleFollow, checkFollowStatus} = useFollow();

// 追蹤文章分頁滾動變數
const blogList = ref([]);
const minTime = ref(Date.now());
const offset = ref(0);
const blogLoading = ref(false);
const noMoreBlogs = ref(false);

// 關注按鈕
const toggleFollow = () => {
  handleFollow(user.value.id);
};

// 載入使用者個人資料
const fetchUser = async () => {
  const userId = route.params.userId;
  const res = await getUserProfile(userId);
  if (res.data && res.data.code === 200) {
    user.value = res.data.data;
    await checkFollowStatus(userId);
  }
};

// 載入共同關注
const fetchMutuals = async () => {
  const userId = route.params.userId;
  const res = await getFollowCommons(userId);
  if (res.data && res.data.code === 200) {
    mutuals.value = res.data.data;
  }
};

const fetchBlogs = async () => {
  if (blogLoading.value || noMoreBlogs.value) return;

  blogLoading.value = true;

  try {
    const res = await getFollowedBlogs(minTime.value, offset.value);

    if (res.data && res.data.code === 200) {
      const result = res.data.data;

      if (result?.list?.length > 0) {
        blogList.value.push(...result.list);
        offset.value = result.offset;
        minTime.value = result.minTime;

        // 後端回傳 hasNext 為 false，代表沒有更多資料
        if (result.hasNext === false) {
          noMoreBlogs.value = true;
        }
      } else {
        // 回傳空列表，表示沒資料
        noMoreBlogs.value = true;
      }
    } else {
      // API 回傳非 200 代碼，也視為錯誤
      noMoreBlogs.value = true;
    }
  } catch (err) {
    // 請求出錯，也設定為無更多資料，避免一直重試
    noMoreBlogs.value = true;
  } finally {
    blogLoading.value = false;
  }
};

// 監聽 tab 切換
watch(activeTab, (val) => {
  if (val === 'mutuals') {
    fetchMutuals();
  } else if (val === 'articles' && blogList.value.length === 0) {
    fetchBlogs();
  }
});

// scroll 加載追蹤文章
const handleScroll = () => {
  if (activeTab.value !== 'articles') return;
  const scrollTop = window.scrollY;
  const windowHeight = window.innerHeight;
  const scrollHeight = document.documentElement.scrollHeight;

  const isAtBottom = Math.ceil(scrollTop + windowHeight) >= scrollHeight;

  if (isAtBottom) {
    console.log('加載更多追蹤文章...');
    fetchBlogs();
  }
};


// 頁面初始化
onMounted(() => {
  fetchUser();
  if (activeTab.value === 'articles') {
    fetchBlogs();
  }
  window.addEventListener('scroll', handleScroll);
});

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>


<style scoped src="@/assets/styles/userProfile.css"></style>