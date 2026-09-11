<template>
  <div>
    <!-- 搜索区 -->
    <el-card class="mb">
      <el-form inline>
        <el-form-item label="商品名称">
          <el-input v-model="query.name" placeholder="输入名称" clearable style="width: 180px" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="分类">
          <el-tree-select
            v-model="query.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', children: 'children' }"
            placeholder="全部分类"
            clearable
            style="width: 200px"
            check-strictly
            @change="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="上架" :value="1" />
            <el-option label="下架" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="openDialog()">新增商品</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格 -->
    <el-card>
      <el-table :data="list" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="code" label="编码" width="130" />
        <el-table-column label="分类" width="130">
          <template #default="{ row }">{{ categoryName(row.categoryId) }}</template>
        </el-table-column>
        <el-table-column prop="unit" label="单位" width="70" />
        <el-table-column prop="spec" label="规格" />
        <el-table-column prop="price" label="售价" width="100" />
        <el-table-column prop="warningQty" label="预警值" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
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
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="商品编码" prop="code">
          <el-input v-model="form.code" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select
            v-model="form.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', children: 'children' }"
            placeholder="请选择分类"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="件/箱/kg" />
        </el-form-item>
        <el-form-item label="规格" prop="spec">
          <el-input v-model="form.spec" />
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="预警值" prop="warningQty">
          <el-input-number v-model="form.warningQty" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductPage, createProduct, updateProduct, deleteProduct } from '../api/product'
import { getCategoryTree } from '../api/category'

const list = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const saving = ref(false)
const formRef = ref()

// 分类树与 id->名称 映射
const categoryTree = ref([])
const categoryNameMap = ref({})

async function loadCategories() {
  categoryTree.value = await getCategoryTree()
  const map = {}
  const walk = nodes => {
    for (const n of nodes) {
      map[n.id] = n.name
      if (n.children && n.children.length) walk(n.children)
    }
  }
  walk(categoryTree.value)
  categoryNameMap.value = map
}

function categoryName(id) {
  return id ? (categoryNameMap.value[id] || '#' + id) : '-'
}

const query = reactive({ name: '', categoryId: null, status: null, pageNum: 1, pageSize: 10 })
const form = reactive({
  id: null, name: '', code: '', categoryId: null, unit: '',
  spec: '', price: null, warningQty: null, status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }]
}

onMounted(() => {
  loadCategories()
  fetchList()
})

async function fetchList() {
  loading.value = true
  try {
    const data = await getProductPage(query)
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
  Object.assign(query, { name: '', categoryId: null, status: null, pageNum: 1 })
  fetchList()
}

function onPageChange(page) { query.pageNum = page; fetchList() }
function onSizeChange(size) { query.pageSize = size; query.pageNum = 1; fetchList() }

function openDialog(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, name: '', code: '', categoryId: null, unit: '', spec: '', price: null, warningQty: null, status: 1 })
  }
  dialogVisible.value = true
}

async function handleSave() {
  await formRef.value.validate()
  saving.value = true
  try {
    if (form.id) {
      await updateProduct(form.id, form)
      ElMessage.success('修改成功')
    } else {
      await createProduct(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确认删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
  await deleteProduct(row.id)
  ElMessage.success('删除成功')
  fetchList()
}
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>