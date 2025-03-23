const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    port: 3000, // frontend port
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // 指向 Nginx 負載均衡器
        changeOrigin: true
      }
    }
  }
})