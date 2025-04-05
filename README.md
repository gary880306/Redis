# 專案系統架構與流程文檔

## 專案概述

待補

### 技術棧
- **前端**：Vue.js 3、Element Plus、Axios
- **後端**：Spring Boot 、 MyBatis-Plus
- **資料庫**：MySQL、Redis
- **分散式工具**：Redisson

## 系統架構

系統採用前後端分離架構，使用 Redis 作為核心組件

1. **Lua 腳本**：保證複雜邏輯的原子性執行
2. **Redis Stream**：作為消息隊列，實現異步處理訂單
3. **Redisson 分散式鎖**：確保同一用戶不會重複下單

<details>
<summary>優惠券相關</summary>

## 優惠券搶購流程

### 1. 準備階段

1. **優惠券創建**：商家通過 `VoucherController` 的 `addSeckillVoucher` 方法創建秒殺優惠券
2. **資料同步**：優惠券資訊同時保存在 MySQL 和 Redis 中
   ```java
   // 保存優惠券到 MySQL
   save(voucher);
   
   // 保存秒殺資訊到 MySQL
   seckillVoucherService.save(seckillVoucher);
   
   // 保存庫存到 Redis
   stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucherId, stock.toString());
   ```

### 2. 同步處理階段（主線程）

1. **接收請求**：用戶點擊搶購按鈕，前端向 `/voucher-order/seckill/{id}` 發送 POST 請求
2. **控制器處理**：`VoucherOrderController` 接收請求並調用 `voucherOrderService.seckillVoucher`
3. **執行 Lua 腳本**：使用 Redis 執行 `seckill.lua` 腳本處理搶購邏輯

   ```java
   // 獲取用戶 ID
   Long userId = UserHolder.getUser().getUserId();
   
   // 生成訂單 ID
   long orderId = redisIdWorker.nextId("order");
   
   // 執行 Lua 腳本
   Long result = stringRedisTemplate.execute(
       SECKILL_SCRIPT,
       Collections.emptyList(),
       voucherId.toString(), userId.toString(), String.valueOf(orderId)
   );
   ```

4. **Lua 腳本內容**：
   ```lua
   -- 1.判斷庫存是否充足
   if (tonumber(redis.call('get', stockKey)) <= 0) then
       -- 庫存不足，返回 1
       return 1;
   end
   -- 2.判斷用戶是否下單
   if (redis.call('sismember', orderKey, userId) == 1) then
       -- 存在，重複下單，返回 2
       return 2;
   end
   -- 3.扣庫存
   redis.call('incrBy', stockKey, -1)
   -- 4.下單(保存用戶)
   redis.call('sadd', orderKey, userId)
   -- 5.發送消息到隊列
   redis.call('xadd', 'stream.orders', '*', 'userId', userId, 'voucherId', voucherId, 'id', orderId)
   return 0;
   ```

5. **返回結果**：根據 Lua 腳本執行結果返回相應訊息
   ```java
   int r = result.intValue();
   if (r != 0) {
       // 不為0，代表沒有資格購買
       return Result.error(r == 1 ? "庫存不足!" : "不能重複下單!");
   }
   
   // 返回訂單id
   return Result.success(orderId);
   ```

### 3. 異步處理階段（背景執行緒）

1. **初始化**：系統啟動時初始化 `VoucherOrderHandler` 執行緒處理訂單
   ```java
   @PostConstruct
   private void init() {
       SECKILL_ORDER_EXECUTOR.submit(new VoucherOrderHandler());
   }
   ```

2. **監聽消息隊列**：持續從 Redis Stream 中讀取訂單資訊
   ```java
   List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
       Consumer.from("g1", "c1"),
       StreamReadOptions.empty().count(1).block(Duration.ofSeconds(2)),
       StreamOffset.create(queueName, ReadOffset.lastConsumed())
   );
   ```

3. **處理訂單**：接收到訂單後，使用 Redisson 分散式鎖確保同一用戶訂單處理的互斥性
   ```java
   // 創建鎖對象
   RLock lock = redissonClient.getLock("lock:order:" + userId);
   
   // 獲取鎖
   boolean isLock = lock.tryLock();
   
   try {
       proxy.createVoucherOrder(voucherOrder);
   } finally {
       // 釋放鎖
       lock.unlock();
   }
   ```

4. **事務處理**：在 `createVoucherOrder` 方法中執行資料庫事務
   ```java
   @Transactional
   public void createVoucherOrder(VoucherOrder voucherOrder) {
       // 檢查一人一單
       Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
       if (count > 0) {
           log.error("用戶已經購買過一次！");
           return;
       }

       // 扣減庫存
       boolean success = seckillVoucherService.update()
           .setSql("stock = stock - 1")
           .eq("voucher_id", voucherId)
           .gt("stock", 0)
           .update();

       if (!success) {
           log.error("庫存不足！");
           return;
       }

       // 創建訂單
       save(voucherOrder);
   }
   ```

5. **消息確認**：處理完成後確認消息已處理（ACK）
   ```java
   stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
   ```

### 4. 異常處理階段

1. **捕獲異常**：如果訂單處理過程中出現異常，捕獲並處理
   ```java
   try {
       // 處理訂單
   } catch (Exception e) {
       log.error("處理訂單異常", e);
       // 處理異常訂單
       handlePendingList();
   }
   ```

2. **處理 Pending List**：獲取並重新處理未成功確認的訂單
   ```java
   private void handlePendingList() {
       while (true) {
           try {
               // 獲取 pending-list 中的訂單
               List<MapRecord<String, Object, Object>> list = stringRedisTemplate.opsForStream().read(
                   Consumer.from("g1", "c1"),
                   StreamReadOptions.empty().count(1),
                   StreamOffset.create(queueName, ReadOffset.from("0"))
               );
               
               // 判斷是否有待處理訂單
               if (list == null || list.isEmpty()) {
                   break;
               }
               
               // 解析並處理訂單
               // ...
               
               // 確認處理完成
               stringRedisTemplate.opsForStream().acknowledge(queueName, "g1", record.getId());
           } catch (Exception e) {
               log.error("處理 Pending List 異常", e);
               try {
                   Thread.sleep(20);
               } catch (InterruptedException e1) {
                   e1.printStackTrace();
               }
           }
       }
   }
   ```

## 系統優勢

1. **高效能**：使用 Redis 作為快取和消息隊列，大幅提高系統吞吐量
2. **高可靠**：通過異步處理和異常重試機制，確保訂單不丟失
3. **一致性保證**：
   - 使用 Lua 腳本確保 Redis 操作的原子性
   - 使用分散式鎖保證同一用戶訂單處理的互斥性
   - 使用資料庫事務和條件更新確保資料一致性
4. **防超賣**：多層次庫存檢查，確保庫存不會出現負數
5. **防重複**：多層次一人一單檢查，確保用戶不會重複下單

## 優惠券搶購流程圖
![優惠券搶購流程圖](/seckillWorkflow.png)
https://www.canva.com/design/DAGjvu7EWos/WGWJld7Njkt6iniUOe3Btg/edit?utm_content=DAGjvu7EWos&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton
</details>