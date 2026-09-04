import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// Vite 构建配置
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173, // 前端开发服务器端口
    proxy: {
      // 把 /api 开头的请求转发到后端 8888，解决跨域
      '/api': {
        target: 'http://localhost:8888',
        changeOrigin: true
      }
    }
  }
})