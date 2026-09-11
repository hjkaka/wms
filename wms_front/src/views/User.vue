<template>
  <div>
    <!-- 搜索区 -->
    <el-card class="mb">
      <el-form inline>
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="输入用户名" clearable style="width: 160px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="经理" value="MANAGER" />
            <el-option label="员工" value="STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog()">新增用户</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="角色" width="100">
          <template #default="{ row }">
            <el-tag :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button
              v-if="row.role !== 'ADMIN'"
              size="small"
              :type="row.status === 1 ? 'danger' : 'success'"
              link
              @click="handleToggleStatus(row)"
            >{{ row.status === 1 ? '停用' : '启用' }}</el-button>
            <el-tooltip v-else content="管理员账号不可停用" placement="top">
              <span class="dim">不可停用</span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        class="pager"
        background
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page="query.pageNum"
        :page-size="query.pageSize"
        :page-sizes="[5, 10, 20]"
        @current-change="onPageChange"
        @size-change="onSizeChange"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录名，创建后不可改" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            :placeholder="form.id ? '留空则不修改密码' : '请输入初始密码'"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="经理" value="MANAGER" />
            <el-option label="员工" value="STAFF" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUserPage, createUser, updateUser, changeUserStatus } from '../api/user'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

const query = reactive({ username: '', role: null, status: null, pageNum: 1, pageSize: 10 })
const form = reactive({ id: null, username: '', password: '', realName: '', phone: '', role: 'STAFF' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const data = await getUserPage(query)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.pageNum = 1
  fetchList()
}

function handleReset() {
  Object.assign(query, { username: '', role: null, status: null, pageNum: 1 })
  fetchList()
}

function onPageChange(page) {
  query.pageNum = page
  fetchList()
}

function onSizeChange(size) {
  query.pageSize = size
  query.pageNum = 1
  fetchList()
}

function openDialog(row) {
  if (row) {
    Object.assign(form, { id: row.id, username: row.username, password: '', realName: row.realName, phone: row.phone, role: row.role })
  } else {
    Object.assign(form, { id: null, username: '', password: '', realName: '', phone: '', role: 'STAFF' })
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  // 新增时必须填密码
  if (!form.id && !form.password) {
    ElMessage.warning('请填写初始密码')
    return
  }
  saving.value = true
  try {
    const payload = { realName: form.realName, phone: form.phone, role: form.role }
    // 密码留空则不带该字段（后端留空表示不改）
    if (form.password && form.password.trim()) {
      payload.password = form.password
    }
    if (form.id) {
      await updateUser(form.id, payload)
      ElMessage.success('修改成功')
    } else {
      payload.username = form.username
      await createUser(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '停用' : '启用'
  await ElMessageBox.confirm(`确认${action}用户「${row.username}」吗？`, '提示', {
    type: action === '停用' ? 'warning' : 'info'
  })
  await changeUserStatus(row.id, row.status === 1 ? 0 : 1)
  ElMessage.success(`${action}成功`)
  fetchList()
}

function roleLabel(role) {
  return { ADMIN: '管理员', MANAGER: '经理', STAFF: '员工' }[role] || role
}
function roleTagType(role) {
  return { ADMIN: 'danger', MANAGER: 'warning', STAFF: 'success' }[role] || 'info'
}
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.dim { color: #c0c4cc; font-size: 13px; }
</style>