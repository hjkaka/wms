<template>
  <el-tabs v-model="activeTab" @tab-change="onTabChange">
    <!-- ===== 页签1：盘点单管理（列表） ===== -->
    <el-tab-pane label="盘点单管理" name="list">
      <el-card class="mb">
        <el-form inline>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
              <el-option label="草稿" :value="0" />
              <el-option label="已过账" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="单号/仓库" clearable style="width: 180px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card>
        <el-table :data="list" border stripe v-loading="loading">
          <el-table-column prop="orderNo" label="盘点单号" width="160" />
          <el-table-column prop="warehouseName" label="仓库" width="120" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="账面快照时间" width="170">
            <template #default="{ row }">{{ formatTime(row.snapshotTime) }}</template>
          </el-table-column>
          <el-table-column prop="operatorName" label="经办人" width="100" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="创建时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="small" @click="openDetail(row)">{{ row.status === 0 ? '录入实盘' : '查看' }}</el-button>
              <el-button v-if="canPost(row)" type="danger" link size="small" @click="doPost(row)">过账</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="pager"
          background
          layout="total, prev, pager, next"
          :total="total"
          :current-page="query.pageNum"
          :page-size="query.pageSize"
          @current-change="onPageChange"
        />
      </el-card>
    </el-tab-pane>

    <!-- ===== 页签2：新建盘点单 ===== -->
    <el-tab-pane label="新建盘点" name="create">
      <el-card style="max-width: 520px">
        <h3>新建盘点单</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
          <el-form-item label="盘点仓库" prop="warehouseId">
            <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 300px" @focus="loadWarehouses">
              <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" style="width: 300px" placeholder="如：月度全盘" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="creating" @click="handleCreate">创建并自动纳入库存</el-button>
          </el-form-item>
        </el-form>
        <el-alert type="info" :closable="false" show-icon
          title="创建后系统自动将该仓库所有有库存商品作为账面快照；随后在列表中点击「录入实盘」填写实际数量，获批权限的人员可「过账」生成盈亏流水。" />
      </el-card>
    </el-tab-pane>

    <!-- ===== 盘点详情抽屉（录入实盘 / 查看） ===== -->
    <el-drawer v-model="drawerVisible" :title="drawerTitle" size="640px">
      <template v-if="currentOrder">
        <el-descriptions :column="2" border size="small" class="mb">
          <el-descriptions-item label="盘点单号">{{ currentOrder.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="仓库">{{ currentOrder.warehouseName }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusType(currentOrder.status)">{{ statusText(currentOrder.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="经办人">{{ currentOrder.operatorName }}</el-descriptions-item>
        </el-descriptions>

        <el-table :data="detailItems" border size="small" v-loading="itemsLoading">
          <el-table-column prop="productCode" label="编码" width="110" />
          <el-table-column prop="productName" label="商品" min-width="120" />
          <el-table-column label="账面" width="80">
            <template #default="{ row }">{{ row.systemQuantity }}</template>
          </el-table-column>
          <el-table-column label="实盘" width="120">
            <template #default="{ row }">
              <el-input-number
                v-if="canEdit"
                v-model="row.countedQuantity"
                :min="0"
                :controls="false"
                placeholder="未盘点"
                size="small"
              />
              <span v-else>{{ row.countedQuantity ?? '/' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="盈亏" width="90">
            <template #default="{ row }">
              <span v-if="row.countedQuantity != null" :class="rowVar(row) > 0 ? 'var-in' : rowVar(row) < 0 ? 'var-out' : 'var-zero'">
                {{ rowVar(row) > 0 ? '+' : '' }}{{ rowVar(row) }}
              </span>
              <span v-else>/</span>
            </template>
          </el-table-column>
          <el-table-column label="盈亏原因" min-width="120">
            <template #default="{ row }">
              <el-input v-if="canEdit" v-model="row.remark" size="small" placeholder="可选" />
              <span v-else>{{ row.remark || '' }}</span>
            </template>
          </el-table-column>
        </el-table>

        <div class="drawer-actions" v-if="canEdit">
          <el-button :loading="saving" type="primary" @click="saveItems">保存实盘</el-button>
          <el-button v-if="canPost(currentOrder)" type="danger" @click="doPost(currentOrder)">过账</el-button>
        </div>
        <el-alert v-else-if="currentOrder.status === 1" type="success" :closable="false" show-icon
          title="该盘点单已过账，盈亏已调整库存并写入流水，不可再修改。" />
      </template>
    </el-drawer>
  </el-tabs>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStockTakePage, createStockTake, getStockTake,
  getStockTakeItems, updateStockTakeItems, postStockTake
} from '../api/stocktake'
import { getAllWarehouses } from '../api/warehouse'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const role = userStore.userInfo.role
const canAudit = role === 'ADMIN' || role === 'MANAGER'

const activeTab = ref('list')

// ===== 列表 =====
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: null, keyword: '', pageNum: 1, pageSize: 10 })

const STATUS_TEXT = ['草稿', '已过账']
const STATUS_TYPE = ['warning', 'success']
function statusText(s) { return STATUS_TEXT[s] ?? '未知' }
function statusType(s) { return STATUS_TYPE[s] ?? 'info' }
function formatTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
function canPost(row) { return canAudit && row.status === 0 }

async function fetchPage() {
  loading.value = true
  try {
    const data = await getStockTakePage(query)
    list.value = data.list || []
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.pageNum = 1; fetchPage() }
function onPageChange(p) { query.pageNum = p; fetchPage() }

// ===== 新建 =====
const formRef = ref()
const warehouses = ref([])
const creating = ref(false)
const form = reactive({ warehouseId: null, remark: '' })
const rules = { warehouseId: [{ required: true, message: '请选择盘点仓库', trigger: 'change' }] }

async function loadWarehouses() {
  if (warehouses.value.length) return
  warehouses.value = await getAllWarehouses()
}

async function handleCreate() {
  await formRef.value.validate()
  creating.value = true
  try {
    const order = await createStockTake({ warehouseId: form.warehouseId, remark: form.remark })
    ElMessage.success('盘点单已创建，已自动纳入库存快照')
    form.warehouseId = null; form.remark = ''
    activeTab.value = 'list'
    fetchPage()
    openDetail(order)
  } finally {
    creating.value = false
  }
}

// ===== 详情抽屉 =====
const drawerVisible = ref(false)
const drawerTitle = computed(() => currentOrder.value ? `${statusText(currentOrder.value.status)} · ${currentOrder.value.orderNo}` : '盘点详情')
const currentOrder = ref(null)
const currentId = ref(null)
const detailItems = ref([])
const itemsLoading = ref(false)
const saving = ref(false)

const canEdit = computed(() => currentOrder.value && currentOrder.value.status === 0)
function rowVar(row) { return row.countedQuantity == null ? null : row.countedQuantity - row.systemQuantity }

async function openDetail(row) {
  currentId.value = row.id
  drawerVisible.value = true
  itemsLoading.value = true
  try {
    currentOrder.value = await getStockTake(row.id)
  } finally {
    itemsLoading.value = false
  }
  loadItems()
}

async function loadItems() {
  itemsLoading.value = true
  try {
    detailItems.value = await getStockTakeItems(currentId.value)
  } finally {
    itemsLoading.value = false
  }
}

async function saveItems() {
  const payload = detailItems.value.map(it => ({ id: it.id, countedQuantity: it.countedQuantity, remark: it.remark || null }))
  saving.value = true
  try {
    await updateStockTakeItems(currentId.value, payload)
    ElMessage.success('实盘数量已保存')
    loadItems()
  } finally {
    saving.value = false
  }
}

async function doPost(row) {
  try {
    await ElMessageBox.confirm(`确认对盘点单 ${row.orderNo} 执行过账？\n系统将按 实盘≠账面 调整库存并生成盈亏流水，且不可撤销。`, '过账确认', { type: 'warning' })
  } catch (e) { return }
  try {
    await postStockTake(row.id)
    ElMessage.success('过账成功，盈亏流水已生成')
    drawerVisible.value = false
    fetchPage()
  } catch (e) { /* 拦截器已提示 */ }
}

function onTabChange(name) {
  if (name === 'list') fetchPage()
}

onMounted(() => { fetchPage(); loadWarehouses() })
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.drawer-actions { margin-top: 16px; text-align: right; }
.var-in { color: #67c23a; font-weight: 600; }
.var-out { color: #f56c6c; font-weight: 600; }
.var-zero { color: #909399; }
</style>