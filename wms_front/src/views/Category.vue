<template>
  <div>
    <!-- 操作区 -->
    <el-card class="mb">
      <div style="display: flex; align-items: center; gap: 12px;">
        <span style="font-weight: bold;">商品分类</span>
        <el-button v-if="isAdminOrManager" type="primary" @click="openDialog()">新增顶级分类</el-button>
      </div>
    </el-card>

    <!-- 树形表格 -->
    <el-card>
      <el-table
        :data="treeData"
        row-key="id"
        border
        stripe
        v-loading="loading"
        default-expand-all
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="name" label="分类名称" min-width="220">
          <template #default="{ row }">
            <span>{{ row.name }}</span>
            <el-tag v-if="!row.children || row.children.length === 0" size="small" type="info" style="margin-left: 8px;">末级</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260">
          <template #default="{ row }">
            <template v-if="isAdminOrManager">
              <el-button size="small" type="primary" link @click="openDialog(null, row.id)">新增子分类</el-button>
              <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
              <el-button
                v-if="row.status === 1"
                size="small"
                type="warning"
                link
                @click="handleToggleStatus(row, 0)"
              >禁用</el-button>
              <el-button
                v-else
                size="small"
                type="success"
                link
                @click="handleToggleStatus(row, 1)"
              >启用</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
            </template>
            <span v-else style="color: #bbb;">只读</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑分类' : '新增分类'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="如：数码产品 / 手机" />
        </el-form-item>
        <el-form-item label="父分类" prop="parentId">
          <el-select v-model="form.parentId" style="width: 100%" placeholder="不选=顶级分类">
            <el-option label="（顶级分类）" :value="0" />
            <el-option
              v-for="node in flatRoots"
              :key="node.id"
              :value="node.id"
              :label="node.name"
              :disabled="form.id ? node.id === form.id : false"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useUserStore } from '../store/user'
import { getCategoryTree, createCategory, updateCategory, changeCategoryStatus, deleteCategory } from '../api/category'

const userStore = useUserStore()
const isAdminOrManager = computed(() => ['ADMIN', 'MANAGER'].includes(userStore.userInfo.role))

const treeData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

const form = reactive({ id: null, name: '', parentId: 0, sort: 0, status: 1 })

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 顶级分类列表（供"父分类"下拉选父级）
const flatRoots = computed(() => treeData.value)

onMounted(fetchTree)

async function fetchTree() {
  loading.value = true
  try {
    treeData.value = await getCategoryTree()
  } finally {
    loading.value = false
  }
}

function openDialog(row, parentId) {
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, parentId: row.parentId || 0, sort: row.sort || 0, status: row.status })
  } else {
    Object.assign(form, { id: null, name: '', parentId: parentId || 0, sort: 0, status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateCategory(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createCategory(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchTree()
  } finally {
    saving.value = false
  }
}

async function handleToggleStatus(row, status) {
  await ElMessageBox.confirm(
    `确认${status === 1 ? '启用' : '禁用'}分类「${row.name}」吗？`,
    '提示', { type: 'warning' }
  )
  await changeCategoryStatus(row.id, status)
  ElMessage.success(status === 1 ? '已启用' : '已禁用')
  fetchTree()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除分类「${row.name}」吗？`, '提示', { type: 'warning' })
  try {
    await deleteCategory(row.id)
    ElMessage.success('删除成功')
    fetchTree()
  } catch (e) {
    // 后端已返回错误信息（如"该分类下存在商品，无法删除"），request.js 已提示
  }
}
</script>

<style scoped>
.mb { margin-bottom: 16px; }
</style>