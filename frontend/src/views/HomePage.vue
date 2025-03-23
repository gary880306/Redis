<template>
  <div class="ecommerce-app">
    <!-- 頂部導航欄 -->
    <header class="site-header">
      <div class="header-container">
        <!-- Logo 區域 -->
        <div class="logo-area">
          <h1 class="logo">SHOPNOW</h1>
        </div>

        <!-- 搜索欄 -->
        <div class="search-bar">
          <el-input
              placeholder="搜尋商品或商店..."
              prefix-icon="Search"
              clearable
              v-model="searchQuery"
          >
            <template #append>
              <el-button>搜尋</el-button>
            </template>
          </el-input>
        </div>

        <!-- 用戶功能區 -->
        <div class="user-actions">
          <el-button type="text" class="action-btn">
            <el-icon><BellFilled /></el-icon>
            <span>通知</span>
          </el-button>
          <el-button type="text" class="action-btn">
            <el-icon><ShoppingCart /></el-icon>
            <span>購物車</span>
          </el-button>
          <el-avatar :size="32" class="user-avatar">
            <el-icon><User /></el-icon>
          </el-avatar>
        </div>
      </div>

      <!-- 導航菜單 -->
      <nav class="main-nav">
        <div class="nav-container">
          <ul class="nav-list">
            <li class="nav-item">
              <el-button link type="primary">首頁</el-button>
            </li>
            <li class="nav-item">
              <el-button link>全部商品</el-button>
            </li>
            <li class="nav-item">
              <el-button link>本月推薦</el-button>
            </li>
            <li class="nav-item">
              <el-button link>限時特惠</el-button>
            </li>
            <li class="nav-item">
              <el-button link>新品上架</el-button>
            </li>
            <li class="nav-item">
              <el-button link>超值優惠</el-button>
            </li>
          </ul>
        </div>
      </nav>
    </header>

    <!-- 主內容區域 -->
    <main>
      <!-- 主視覺輪播圖 -->
      <section class="hero-carousel">
        <el-carousel height="500px" indicator-position="outside" arrow="always">
          <el-carousel-item v-for="(slide, index) in heroSlides" :key="index">
            <div class="carousel-content" :style="{ backgroundColor: slide.bgColor }">
              <div class="carousel-text">
                <h2>{{ slide.title }}</h2>
                <p>{{ slide.description }}</p>
                <el-button type="primary" size="large">{{ slide.buttonText }}</el-button>
              </div>
              <div class="carousel-image">
                <img :src="slide.image" :alt="slide.title" />
              </div>
            </div>
          </el-carousel-item>
        </el-carousel>
      </section>

      <!-- 快速購物功能 -->
      <section class="quick-access">
        <div class="section-container">
          <div class="feature-tiles">
            <div class="feature-tile" v-for="(feature, index) in quickFeatures" :key="index">
              <el-icon :size="32"><component :is="feature.icon" /></el-icon>
              <span>{{ feature.title }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 熱門分類 -->
      <section class="popular-categories">
        <div class="section-container">
          <div class="section-header">
            <h2>熱門分類</h2>
            <el-button text>查看全部 <el-icon><ArrowRight /></el-icon></el-button>
          </div>

          <div class="category-grid">
            <div class="category-card" v-for="(category, index) in categories" :key="index">
              <div class="category-image">
                <img :src="category.image" :alt="category.name" />
              </div>
              <div class="category-name">{{ category.name }}</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 精選商店 -->
      <section class="featured-shops">
        <div class="section-container">
          <div class="section-header">
            <h2>精選商店</h2>
            <el-button text>查看全部 <el-icon><ArrowRight /></el-icon></el-button>
          </div>

          <el-tabs tab-position="top" class="shop-tabs">
            <el-tab-pane v-for="tab in shopTabs" :key="tab.name" :label="tab.label">
              <div class="shop-grid">
                <div class="shop-card" v-for="shop in shops" :key="shop.shopId" @click="openModal(shop.shopId)">
                  <div class="shop-header">
                    <div class="shop-image">
                      <el-image
                          src="/images/levi.jpeg"
                          fit="cover"
                          :alt="shop.name"
                      />
                    </div>
                    <div class="shop-rating">
                      <span class="rating-value">4.8</span>
                      <div class="rating-stars">
                        <el-icon><Star /></el-icon>
                        <el-icon><Star /></el-icon>
                        <el-icon><Star /></el-icon>
                        <el-icon><Star /></el-icon>
                        <el-icon><StarFilled /></el-icon>
                      </div>
                    </div>
                  </div>
                  <div class="shop-body">
                    <h3 class="shop-name">{{ shop.name }}</h3>
                    <p class="shop-description">{{ truncateText(shop.address, 40) }}</p>
                    <div class="shop-tags">
                      <el-tag size="small" effect="plain">優質商店</el-tag>
                      <el-tag size="small" effect="plain" type="success">免運費</el-tag>
                    </div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>
      </section>

      <!-- 促銷專區 -->
      <section class="promotions-section">
        <div class="section-container">
          <div class="section-header">
            <h2>限時優惠</h2>
            <el-button text>查看全部 <el-icon><ArrowRight /></el-icon></el-button>
          </div>

          <div class="promo-grid">
            <div class="promo-main">
              <div class="promo-content">
                <h3>夏季大促銷</h3>
                <p>全場商品低至5折</p>
                <div class="countdown">
                  <span class="countdown-item">02</span>:
                  <span class="countdown-item">18</span>:
                  <span class="countdown-item">45</span>:
                  <span class="countdown-item">33</span>
                </div>
                <el-button type="danger">立即搶購</el-button>
              </div>
            </div>
            <div class="promo-side">
              <div class="promo-item">
                <div class="promo-item-content">
                  <h4>新品體驗</h4>
                  <p>新用戶專享85折</p>
                  <el-button link>了解更多</el-button>
                </div>
              </div>
              <div class="promo-item">
                <div class="promo-item-content">
                  <h4>會員專享</h4>
                  <p>會員日雙倍積分</p>
                  <el-button link>了解更多</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 推薦商店 -->
      <section class="recommended-shops">
        <div class="section-container">
          <div class="section-header">
            <h2>為您推薦</h2>
            <el-button text>查看全部 <el-icon><ArrowRight /></el-icon></el-button>
          </div>

          <div class="shop-slider">
            <el-scrollbar>
              <div class="slider-container">
                <div class="slider-card" v-for="(shop, index) in [...shops, ...shops].slice(0, 8)" :key="index" @click="openModal(shop.shopId)">
                  <el-image
                      :src="getImageUrl(shop.imageUrl || '/images/default-shop.jpg')"
                      fit="cover"
                      class="slider-image"
                      :alt="shop.name"
                  />
                  <div class="slider-content">
                    <h3>{{ shop.name }}</h3>
                    <p>{{ truncateText(shop.address, 20) }}</p>
                  </div>
                </div>
              </div>
            </el-scrollbar>
          </div>
        </div>
      </section>

      <!-- 信任元素 -->
      <section class="trust-elements">
        <div class="section-container">
          <div class="trust-grid">
            <div class="trust-item" v-for="(item, index) in trustItems" :key="index">
              <el-icon :size="40"><component :is="item.icon" /></el-icon>
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </div>
        </div>
      </section>
    </main>

    <!-- 商店詳情彈窗 -->
    <el-dialog
        v-model="showModal"
        title="商店詳情"
        width="560px"
        class="shop-detail-dialog"
        destroy-on-close
    >
      <div v-if="selectedShop" class="shop-detail">
        <el-image
            v-if="selectedShop.imageUrl"
            :src="getImageUrl(selectedShop.imageUrl)"
            fit="cover"
            class="shop-detail-image"
        />

        <div class="shop-detail-header">
          <h2>{{ selectedShop.name }}</h2>
          <div class="shop-detail-rating">
            <div class="rating-stars">
              <el-icon><StarFilled /></el-icon>
              <el-icon><StarFilled /></el-icon>
              <el-icon><StarFilled /></el-icon>
              <el-icon><StarFilled /></el-icon>
              <el-icon><Star /></el-icon>
            </div>
            <span>4.8 (24 評價)</span>
          </div>
          <div class="shop-detail-tags">
            <el-tag size="small" effect="plain">優質商店</el-tag>
            <el-tag size="small" effect="plain" type="success">免運費</el-tag>
            <el-tag size="small" effect="plain" type="warning">新店開張</el-tag>
          </div>
        </div>

        <div class="shop-detail-info">
          <el-descriptions :column="1" border>
            <el-descriptions-item label="擁有者">
              <el-icon><User /></el-icon> {{ selectedShop.owner }}
            </el-descriptions-item>
            <el-descriptions-item label="電話">
              <el-icon><Phone /></el-icon> {{ selectedShop.phone }}
            </el-descriptions-item>
            <el-descriptions-item label="地址">
              <el-icon><Location /></el-icon> {{ selectedShop.address }}
            </el-descriptions-item>
            <el-descriptions-item label="營業時間">
              <el-icon><Clock /></el-icon> 09:00 - 21:00 (週一至週日)
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 優惠券區域 -->
        <div class="shop-vouchers">
          <h3 class="voucher-title">優惠券</h3>
          <div v-if="vouchersLoading" class="vouchers-loading">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else-if="vouchers.length > 0" class="voucher-list">
            <div class="voucher-item" v-for="voucher in vouchers" :key="voucher.voucherId">
              <div class="voucher-left" :class="{ 'seckill': voucher.type === 1 }">
                <div class="voucher-value">
                  <span class="symbol">¥</span>
                  <span class="amount">{{ (voucher.actualValue / 100).toFixed(0) }}</span>
                </div>
                <div class="voucher-condition" v-if="voucher.payValue > 0">
                  滿{{ (voucher.payValue / 100).toFixed(0) }}元可用
                </div>
                <div class="voucher-condition" v-else>
                  無門檻使用
                </div>
              </div>
              <div class="voucher-right">
                <div class="voucher-info">
                  <div class="voucher-name">{{ voucher.title }}</div>
                  <div class="voucher-desc">{{ voucher.subTitle || '暫無描述' }}</div>
                  <div class="voucher-time" v-if="voucher.beginTime && voucher.endTime">
                    {{ formatTime(voucher.beginTime) }} - {{ formatTime(voucher.endTime) }}
                  </div>
                </div>
                <div class="voucher-action">
                  <el-button v-if="voucher.type === 1" type="danger" size="small" :disabled="voucher.status !== 1" @click="handleSeckill(voucher.voucherId)">
                    {{ voucher.status === 1 ? '搶購' : (voucher.status === 2 ? '已下架' : '已過期') }}
                  </el-button>
                  <el-button v-else type="primary" size="small" @click="handleClaim(voucher.voucherId)">
                    領取
                  </el-button>
                </div>
              </div>
              <div class="voucher-type" v-if="voucher.type === 1">秒殺券</div>
            </div>
          </div>
          <div v-else class="no-vouchers">
            <el-empty description="暫無優惠券" />
          </div>
        </div>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="closeModal">關閉</el-button>
          <el-button type="primary" @click="visitShop">
            瀏覽商店
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 頁腳 -->
    <footer class="site-footer">
      <div class="footer-main">
        <div class="footer-content">
          <div class="footer-about">
            <h3 class="footer-logo">SHOPNOW</h3>
            <p>為您提供最優質的商店資訊和購物體驗，連接商家與消費者的橋樑。</p>
            <div class="footer-social">
              <el-button circle><el-icon><Share /></el-icon></el-button>
              <el-button circle><el-icon><ChatDotRound /></el-icon></el-button>
              <el-button circle><el-icon><Link /></el-icon></el-button>
            </div>
          </div>

          <div class="footer-links">
            <div class="footer-links-column">
              <h4>購物指南</h4>
              <ul>
                <li><a href="#">新手上路</a></li>
                <li><a href="#">常見問題</a></li>
                <li><a href="#">購物流程</a></li>
                <li><a href="#">會員制度</a></li>
              </ul>
            </div>
            <div class="footer-links-column">
              <h4>商家服務</h4>
              <ul>
                <li><a href="#">商家入駐</a></li>
                <li><a href="#">店舖管理</a></li>
                <li><a href="#">營銷推廣</a></li>
                <li><a href="#">規則中心</a></li>
              </ul>
            </div>
            <div class="footer-links-column">
              <h4>售後服務</h4>
              <ul>
                <li><a href="#">退換貨政策</a></li>
                <li><a href="#">投訴管道</a></li>
                <li><a href="#">售後保障</a></li>
                <li><a href="#">服務協議</a></li>
              </ul>
            </div>
            <div class="footer-links-column">
              <h4>關於我們</h4>
              <ul>
                <li><a href="#">公司簡介</a></li>
                <li><a href="#">聯絡我們</a></li>
                <li><a href="#">加入我們</a></li>
                <li><a href="#">隱私政策</a></li>
              </ul>
            </div>
          </div>

          <div class="footer-contact">
            <h4>聯絡我們</h4>
            <div class="contact-item">
              <el-icon><Phone /></el-icon>
              <span>客服熱線: 02-1234-5678</span>
            </div>
            <div class="contact-item">
              <el-icon><Message /></el-icon>
              <span>客服郵箱: service@shopnow.com</span>
            </div>
            <div class="contact-item">
              <el-icon><Location /></el-icon>
              <span>台北市信義區松高路100號</span>
            </div>
            <el-button type="primary" class="contact-btn">
              線上客服
            </el-button>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="footer-container">
          <div class="copyright">
            &copy; {{ new Date().getFullYear() }} SHOPNOW 電商平台 | 保留所有權利
          </div>
          <div class="footer-policy">
            <a href="#">隱私政策</a>
            <a href="#">使用條款</a>
            <a href="#">Cookie 政策</a>
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import api from "@/api/axios";
import { ElMessage } from "element-plus";
import {
  User, Location, Phone, ShoppingCart, BellFilled, Search, Star, StarFilled,
  Share, ChatDotRound, Link, Message, Clock, ArrowRight,
  Goods, PriceTag, Van, Medal, SuccessFilled, CreditCard
} from '@element-plus/icons-vue';

export default {
  components: {
    User, Location, Phone, ShoppingCart, BellFilled, Search, Star, StarFilled,
    Share, ChatDotRound, Link, Message, Clock, ArrowRight,
    Goods, PriceTag, Van, Medal, SuccessFilled, CreditCard
  },
  setup() {
    const searchQuery = ref('');
    const shops = ref([]);
    const showModal = ref(false);
    const selectedShop = ref({});
    const vouchers = ref([]);
    const vouchersLoading = ref(false);

    // 輪播圖數據
    const heroSlides = ref([
      {
        title: '探索本地優質商店',
        description: '發現您附近的優質商家，享受便捷的購物體驗',
        buttonText: '立即探索',
        image: '/api/placeholder/600/500',
        bgColor: '#f5f7fa'
      },
      {
        title: '新店開張優惠',
        description: '新註冊商家首月免服務費，還有多項優惠等著您',
        buttonText: '了解詳情',
        image: '/api/placeholder/600/500',
        bgColor: '#fef6e4'
      },
      {
        title: '會員獨享折扣',
        description: '成為會員即可享受專屬優惠和積分獎勵',
        buttonText: '立即加入',
        image: '/api/placeholder/600/500',
        bgColor: '#e6f4ff'
      }
    ]);

    // 快速訪問功能
    const quickFeatures = ref([
      { title: '今日優惠', icon: 'PriceTag' },
      { title: '熱門商店', icon: 'Star' },
      { title: '附近商家', icon: 'Location' },
      { title: '免運專區', icon: 'Van' },
      { title: '優惠券', icon: 'Goods' }
    ]);

    // 分類數據
    const categories = ref([
      { name: '餐廳美食', image: '/api/placeholder/200/200' },
      { name: '咖啡甜點', image: '/api/placeholder/200/200' },
      { name: '服飾鞋包', image: '/api/placeholder/200/200' },
      { name: '美妝保養', image: '/api/placeholder/200/200' },
      { name: '電子數碼', image: '/api/placeholder/200/200' },
      { name: '居家生活', image: '/api/placeholder/200/200' },
      { name: '運動戶外', image: '/api/placeholder/200/200' },
      { name: '親子用品', image: '/api/placeholder/200/200' }
    ]);

    // 商店分類標籤
    const shopTabs = ref([
      { name: 'all', label: '全部' },
      { name: 'restaurant', label: '餐廳美食' },
      { name: 'fashion', label: '服飾鞋包' },
      { name: 'electronics', label: '電子數碼' },
      { name: 'beauty', label: '美妝保養' }
    ]);

    // 信任元素
    const trustItems = ref([
      {
        icon: 'SuccessFilled',
        title: '安心保障',
        description: '正品保證，假一賠十'
      },
      {
        icon: 'Van',
        title: '快速配送',
        description: '全島24小時極速配送'
      },
      {
        icon: 'CreditCard',
        title: '安全支付',
        description: '多種支付方式，安全無憂'
      },
      {
        icon: 'Medal',
        title: '優質服務',
        description: '7x24小時專業客服'
      }
    ]);

    const fetchShops = async () => {
      try {
        const res = await api.get("/shop/all");
        shops.value = res.data.data;
      } catch (error) {
        console.error("API 錯誤:", error);
        ElMessage.error("請求失敗");
      }
    };

    const fetchShopDetail = async (shopId) => {
      try {
        const res = await api.get(`/shop/${shopId}`);
        selectedShop.value = res.data.data;
        showModal.value = true;

        // 顯示商店詳情後立即獲取該商店的優惠券
        fetchShopVouchers(shopId);
      } catch (error) {
        ElMessage.error("獲取商店詳情失敗");
      }
    };

    // 獲取商店優惠券
    const fetchShopVouchers = async (shopId) => {
      vouchersLoading.value = true;
      try {
        const res = await api.get(`/voucher/list/${shopId}`);
        if (res.data && res.data.code === 200) {
          vouchers.value = res.data.data || [];
        } else {
          vouchers.value = [];
          console.error("獲取優惠券失敗:", res.data?.message || "未知錯誤");
        }
      } catch (error) {
        console.error("獲取優惠券API錯誤:", error);
        vouchers.value = [];
        ElMessage.error("獲取優惠券失敗");
      } finally {
        vouchersLoading.value = false;
      }
    };

    // 格式化時間
    const formatTime = (timeString) => {
      if (!timeString) return '';
      const date = new Date(timeString);
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`;
    };

    // 秒殺券搶購邏輯
    const handleSeckill = (voucherId) => {
      ElMessage.info(`開始搶購 ID 為 ${voucherId} 的秒殺券 (此功能尚在開發中)`);
      // TODO: 實現秒殺邏輯
    };

    // 普通券領取邏輯
    const handleClaim = (voucherId) => {
      ElMessage.info(`領取 ID 為 ${voucherId} 的優惠券 (此功能尚在開發中)`);
      // TODO: 實現領券邏輯
    };

    const openModal = (shopId) => {
      fetchShopDetail(shopId);
    };

    const closeModal = () => {
      showModal.value = false;
      selectedShop.value = {};
      vouchers.value = []; // 清空優惠券數據
    };

    const visitShop = () => {
      ElMessage.info('此功能尚在開發中');
    };

    const getImageUrl = (imagePath) => {
      if (!imagePath) return '';
      if (imagePath.startsWith('http')) {
        return imagePath;
      }
      return `http://localhost:8080${imagePath}`;
    };

    const truncateText = (text, maxLength) => {
      if (text && text.length > maxLength) {
        return text.substring(0, maxLength) + '...';
      }
      return text;
    };

    onMounted(fetchShops);

    return {
      searchQuery,
      shops,
      showModal,
      selectedShop,
      vouchers,
      vouchersLoading,
      heroSlides,
      quickFeatures,
      categories,
      shopTabs,
      trustItems,
      openModal,
      closeModal,
      visitShop,
      getImageUrl,
      truncateText,
      formatTime,
      handleSeckill,
      handleClaim
    };
  },
};
</script>

<style>
/* 全局重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  margin: 0;
  padding: 0;
  overflow-x: hidden;
  width: 100%;
  font-family: 'Helvetica Neue', Arial, 'PingFang TC', '微軟正黑體', sans-serif;
  color: #333;
  background-color: #f5f7fa;
  line-height: 1.5;
}

#app {
  margin: 0;
  padding: 0;
  width: 100%;
  overflow-x: hidden;
}

a {
  text-decoration: none;
  color: inherit;
}

h1, h2, h3, h4, h5, h6 {
  font-weight: 600;
  line-height: 1.3;
}

ul {
  list-style: none;
}

/* Element Plus 全局樣式覆蓋 */
.el-button--primary {
  background-color: #1890ff;
}

.el-carousel__arrow {
  background-color: rgba(255, 255, 255, 0.8);
  color: #333;
}

.el-carousel__arrow:hover {
  background-color: #fff;
}

.el-tabs__active-bar {
  background-color: #1890ff;
}

.el-tabs__item.is-active {
  color: #1890ff;
}
</style>

<style scoped>
/* 布局容器 */
.section-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #252b3a;
}

/* 頂部導航欄 */
.site-header {
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-container {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 70px;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.logo {
  font-size: 24px;
  font-weight: 700;
  color: #1890ff;
  margin: 0;
}

.search-bar {
  flex: 1;
  max-width: 500px;
  margin: 0 24px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.action-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  font-size: 12px;
}

.action-btn .el-icon {
  font-size: 20px;
}

.user-avatar {
  cursor: pointer;
  background-color: #f0f2f5;
  color: #909399;
}

.main-nav {
  background-color: #fff;
  border-top: 1px solid #ebeef5;
}

.nav-container {
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.nav-list {
  display: flex;
  gap: 32px;
  height: 48px;
}

.nav-item {
  display: flex;
  align-items: center;
}

/* 主視覺輪播圖 */
.hero-carousel {
  margin-bottom: 40px;
}

.carousel-content {
  display: flex;
  height: 100%;
  align-items: center;
  justify-content: space-between;
  padding: 0 10%;
}

.carousel-text {
  flex: 1;
  max-width: 500px;
}

.carousel-text h2 {
  font-size: 36px;
  margin-bottom: 16px;
  color: #252b3a;
}

.carousel-text p {
  font-size: 18px;
  margin-bottom: 24px;
  color: #5a6478;
}

.carousel-image {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}

.carousel-image img {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

/* 快速購物功能 */
.quick-access {
  margin-bottom: 40px;
}

.feature-tiles {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  padding: 24px 0;
}

.feature-tile {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  width: 100px;
  height: 100px;
  background-color: #fff;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: all 0.3s ease;
}

.feature-tile:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.feature-tile .el-icon {
  color: #1890ff;
}

.feature-tile span {
  font-size: 14px;
  color: #5a6478;
}

/* 熱門分類 */
.popular-categories {
  margin-bottom: 40px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 16px;
}

.category-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.category-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.category-image {
  height: 150px;
  overflow: hidden;
}

.category-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s ease;
}

.category-card:hover .category-image img {
  transform: scale(1.05);
}

.category-name {
  padding: 12px;
  text-align: center;
  font-weight: 500;
}

/* 精選商店 */
.featured-shops {
  margin-bottom: 40px;
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 24px;
}

.shop-card {
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  cursor: pointer;
}

.shop-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.shop-header {
  position: relative;
}

.shop-image {
  height: 180px;
  overflow: hidden;
}

.shop-image .el-image {
  width: 100%;
  height: 100%;
}

.shop-rating {
  position: absolute;
  bottom: 12px;
  right: 12px;
  background-color: rgba(255, 255, 255, 0.9);
  border-radius: 16px;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.rating-value {
  font-weight: 600;
  color: #f9ca4d;
}

.rating-stars {
  display: flex;
  color: #f9ca4d;
}

.shop-body {
  padding: 16px;
}

.shop-name {
  font-size: 18px;
  margin-bottom: 8px;
}

.shop-description {
  color: #5a6478;
  margin-bottom: 12px;
  font-size: 14px;
  line-height: 1.4;
}

.shop-tags {
  display: flex;
  gap: 8px;
}

/* 促銷專區 */
.promotions-section {
  margin-bottom: 40px;
}

.promo-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.promo-main {
  background: linear-gradient(135deg, #ff6b6b, #ffa8a8);
  border-radius: 12px;
  height: 300px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.promo-main::before {
  content: '';
  position: absolute;
  top: -30px;
  right: -30px;
  width: 150px;
  height: 150px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.promo-main::after {
  content: '';
  position: absolute;
  bottom: -50px;
  left: -50px;
  width: 200px;
  height: 200px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.promo-content {
  text-align: center;
  z-index: 1;
}

.promo-content h3 {
  font-size: 28px;
  margin-bottom: 8px;
}

.promo-content p {
  font-size: 18px;
  margin-bottom: 24px;
  opacity: 0.9;
}

.countdown {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
  font-size: 24px;
}

.countdown-item {
  background-color: rgba(255, 255, 255, 0.2);
  padding: 8px 12px;
  border-radius: 4px;
  font-weight: 600;
}

.promo-side {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.promo-item {
  background: linear-gradient(135deg, #4facfe, #00f2fe);
  border-radius: 12px;
  height: 138px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
  overflow: hidden;
}

.promo-item:last-child {
  background: linear-gradient(135deg, #a18cd1, #fbc2eb);
}

.promo-item::before {
  content: '';
  position: absolute;
  top: -20px;
  right: -20px;
  width: 100px;
  height: 100px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
}

.promo-item-content {
  text-align: center;
  z-index: 1;
}

.promo-item-content h4 {
  font-size: 20px;
  margin-bottom: 8px;
}

.promo-item-content p {
  font-size: 16px;
  margin-bottom: 12px;
  opacity: 0.9;
}

/* 推薦商店 */
.recommended-shops {
  margin-bottom: 40px;
}

.shop-slider {
  overflow: hidden;
}

.slider-container {
  display: flex;
  gap: 16px;
  padding: 8px 0;
}

.slider-card {
  flex: 0 0 260px;
  background-color: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
  cursor: pointer;
  transition: transform 0.3s ease;
}

.slider-card:hover {
  transform: translateY(-5px);
}

.slider-image {
  height: 150px;
  width: 100%;
}

.slider-content {
  padding: 12px;
}

.slider-content h3 {
  font-size: 16px;
  margin-bottom: 4px;
}

.slider-content p {
  font-size: 14px;
  color: #5a6478;
}

/* 信任元素 */
.trust-elements {
  margin-bottom: 60px;
}

.trust-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 24px;
  background-color: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.trust-item {
  text-align: center;
  padding: 16px;
}

.trust-item .el-icon {
  color: #1890ff;
  margin-bottom: 16px;
}

.trust-item h3 {
  font-size: 18px;
  margin-bottom: 8px;
  color: #252b3a;
}

.trust-item p {
  color: #5a6478;
  font-size: 14px;
}

/* 商店詳情彈窗 */
.shop-detail {
  display: flex;
  flex-direction: column;
}

.shop-detail-image {
  width: 100%;
  height: 250px;
  object-fit: cover;
  border-radius: 8px;
  margin-bottom: 20px;
}

.shop-detail-header {
  margin-bottom: 24px;
}

.shop-detail-header h2 {
  font-size: 24px;
  margin-bottom: 8px;
}

.shop-detail-rating {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.shop-detail-rating .rating-stars {
  color: #f9ca4d;
}

.shop-detail-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

/* 優惠券區域樣式 */
.shop-vouchers {
  margin-top: 24px;
}

.voucher-title {
  font-size: 18px;
  margin-bottom: 16px;
  color: #252b3a;
  position: relative;
  padding-left: 12px;
}

.voucher-title:before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 4px;
  background-color: #1890ff;
  border-radius: 2px;
}

.vouchers-loading {
  padding: 16px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.voucher-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.voucher-item {
  display: flex;
  height: 100px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  position: relative;
  overflow: hidden;
}

.voucher-left {
  width: 120px;
  background-color: #1890ff;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
}

.voucher-left.seckill {
  background-color: #ff4d4f;
}

.voucher-left:after {
  content: '';
  position: absolute;
  right: 0;
  top: 0;
  bottom: 0;
  width: 4px;
  background: linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.5) 100%);
}

.voucher-value {
  display: flex;
  align-items: baseline;
}

.symbol {
  font-size: 16px;
  font-weight: 500;
}

.amount {
  font-size: 30px;
  font-weight: 700;
  line-height: 1;
}

.voucher-condition {
  font-size: 12px;
  margin-top: 8px;
  opacity: 0.9;
}

.voucher-right {
  flex: 1;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.voucher-info {
  flex: 1;
}

.voucher-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 6px;
  color: #252b3a;
}

.voucher-desc {
  font-size: 12px;
  color: #5a6478;
  margin-bottom: 8px;
}

.voucher-time {
  font-size: 12px;
  color: #909399;
}

.voucher-action {
  margin-left: 16px;
}

.voucher-type {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: #ff4d4f;
  color: white;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 4px;
}

.no-vouchers {
  padding: 24px 0;
  background-color: #f9f9f9;
  border-radius: 8px;
  text-align: center;
}

/* 頁腳 */
.site-footer {
  background-color: #2c3e50;
  color: #fff;
}

.footer-main {
  padding: 60px 0 40px;
}

.footer-content {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.footer-about {
  flex: 0 0 300px;
  margin-bottom: 32px;
}

.footer-logo {
  font-size: 24px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 16px;
}

.footer-social {
  margin-top: 24px;
  display: flex;
  gap: 12px;
}

.footer-links {
  flex: 1;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  margin-right: 32px;
}

.footer-links-column {
  flex: 0 0 calc(25% - 24px);
  margin-bottom: 32px;
}

.footer-links-column h4 {
  font-size: 16px;
  margin-bottom: 16px;
  color: #fff;
}

.footer-links-column ul li {
  margin-bottom: 8px;
}

.footer-links-column ul li a {
  color: rgba(255, 255, 255, 0.7);
  transition: color 0.3s ease;
}

.footer-links-column ul li a:hover {
  color: #fff;
}

.footer-contact {
  flex: 0 0 300px;
}

.footer-contact h4 {
  font-size: 16px;
  margin-bottom: 16px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.contact-btn {
  margin-top: 16px;
  width: 100%;
}

.footer-bottom {
  background-color: rgba(0, 0, 0, 0.1);
  padding: 20px 0;
}

.footer-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 16px;
}

.copyright {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.footer-policy {
  display: flex;
  gap: 24px;
}

.footer-policy a {
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
  transition: color 0.3s ease;
}

.footer-policy a:hover {
  color: #fff;
}

/* 響應式設計 */
@media (max-width: 1024px) {
  .carousel-content {
    padding: 0 5%;
  }

  .carousel-text h2 {
    font-size: 28px;
  }

  .promo-grid {
    grid-template-columns: 1fr;
  }

  .promo-main {
    height: 250px;
  }

  .footer-content {
    flex-direction: column;
  }

  .footer-about {
    flex: 0 0 100%;
  }

  .footer-links {
    margin-right: 0;
  }

  .footer-contact {
    flex: 0 0 100%;
  }
}

@media (max-width: 768px) {
  .header-container {
    flex-direction: column;
    height: auto;
    padding: 16px;
  }

  .logo-area {
    margin-bottom: 16px;
  }

  .search-bar {
    width: 100%;
    max-width: 100%;
    margin: 0 0 16px;
  }

  .carousel-content {
    flex-direction: column;
    text-align: center;
    padding: 32px 16px;
  }

  .carousel-text {
    margin-bottom: 32px;
  }

  .feature-tiles {
    overflow-x: auto;
    justify-content: flex-start;
    padding: 24px 0;
  }

  .feature-tile {
    flex: 0 0 auto;
  }

  .footer-container {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .footer-policy {
    justify-content: center;
  }
}

@media (max-width: 576px) {
  .nav-list {
    overflow-x: auto;
    justify-content: flex-start;
  }

  .nav-item {
    flex: 0 0 auto;
  }

  .footer-links-column {
    flex: 0 0 100%;
  }
}
</style>