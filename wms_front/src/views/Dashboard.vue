<template>
  <div class="dashboard">
    <el-card>
      <h2>欢迎使用 WMS 仓储管理系统</h2>
      <p>当前用户：{{ userStore.userInfo.realName }}（{{ userStore.userInfo.role }}）</p>
      <p>通过左侧菜单进入对应管理模块。</p>
    </el-card>
  </div>
</template>

<script setup>
import { useUserStore } from '../store/user'
const userStore = useUserStore()
</script>