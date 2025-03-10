<template>
  <div class="home-container">
    <h2>首頁 - 商店列表</h2>

    <div v-if="shops.length">
      <h3>商店列表：</h3>
      <ul>
        <li v-for="shop in shops" :key="shop.shopId">
          <!-- 使用 Element Plus 樣式 -->
          <el-link type="primary" @click="openModal(shop.shopId)">{{ shop.name }}</el-link>
        </li>
      </ul>
    </div>

    <p v-if="message">{{ message }}</p>

    <!-- 使用 Element Plus 的 Dialog -->
    <el-dialog v-model="showModal" title="商店詳情" width="500px">
      <div v-if="selectedShop">
        <p><strong>擁有者：</strong>{{ selectedShop.owner }}</p>
        <p><strong>電話：</strong>{{ selectedShop.phone }}</p>
        <p><strong>地址：</strong>{{ selectedShop.address }}</p>
        <el-image v-if="selectedShop.imageUrl" :src="getImageUrl(selectedShop.imageUrl)" fit="cover" class="shop-image" />
      </div>
      <template #footer>
        <el-button @click="closeModal">關閉</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted } from "vue";
import api from "@/api/axios";
import { ElMessage } from "element-plus";

export default {
  setup() {
    const shops = ref([]);
    const message = ref("");
    const showModal = ref(false);
    const selectedShop = ref({});

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
      } catch (error) {
        ElMessage.error("獲取商店詳情失敗");
      }
    };

    const openModal = (shopId) => {
      fetchShopDetail(shopId);
    };

    const closeModal = () => {
      showModal.value = false;
      selectedShop.value = {};
    };

    const getImageUrl = (imagePath) => {
      return `http://localhost:8080${imagePath}`;
    };

    onMounted(fetchShops);

    return { shops, message, showModal, selectedShop, openModal, closeModal, getImageUrl };
  },
};
</script>

<style scoped>
.shop-image {
  width: 100%;
  height: 200px;
  object-fit: cover;
  border-radius: 5px;
}
</style>