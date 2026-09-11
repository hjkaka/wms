<template>
  <el-tabs v-model="activeTab">
    <!-- ===== 页签1：出库单列表（审核流） ===== -->
    <el-tab-pane label="出库单管理" name="list">
      <el-card class="mb">
        <el-form inline>
          <el-form-item label="状态">
            <el-select v-model="query.status" placeholder="全部" clearable style="width: 140px">
              <el-option label="草稿" :value="0" />
              <el-option label="待审" :value="1" />
              <el-option label="已审" :value="2" />
              <el-option label="已过账" :value="3" />
            </el-select>
          </el-form-item>
          <el-form-item label="关键字">
            <el-input v-model="query.keyword" placeholder="单号/领用人" clearable style="width: 180px" @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
          </el-form-item>
        </el-form>
      </el-card>

      <el-card>
        <el-table :data="list" border stripe v-loading="loading">
          <el-table-column prop="orderNo" label="单号" width="150" />
          <el-table-column prop="warehouseName" label="仓库" width="110" />
          <el-table-column prop="receiver" label="领用人" />
          <el-table-column prop="operatorName" label="经办人" width="100" />
          <el-table-column prop="totalAmount" label="金额" width="110" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="160">
            <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button v-if="canSubmit(row)" type="primary" link size="small" @click="doAction(row, 'submit')">提交审核</el-button>
              <el-button v-if="canApprove(row)" type="success" link size="small" @click="doAction(row, 'approve')">通过</el-button>
              <el-button v-if="canReject(row)" type="warning" link size="small" @click="doAction(row, 'reject')">驳回</el-button>
              <el-button v-if="canWithdraw(row)" type="info" link size="small" @click="doAction(row, 'withdraw')">撤回</el-button>
              <el-button v-if="canPost(row)" type="danger" link size="small" @click="doAction(row, 'post')">过账</el-button>
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

    <!-- ===== 页签2：新建出库单（草稿，不动库存） ===== -->
    <el-tab-pane label="新建出库单" name="create">
      <el-card>
        <h3>创建出库单（草稿）</h3>
        <el-form ref="formRef" :model="form" :rules="rules" label-width="90px" style="max-width: 720px">
          <el-form-item label="仓库" prop="warehouseId">
            <el-select v-model="form.warehouseId" placeholder="选择仓库" style="width: 300px" @focus="loadWarehouses">
              <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="领用人" prop="receiver">
            <el-input v-model="form.receiver" placeholder="如：张三" style="width: 300px" />
          </el-form-item>
          <el-form-item label="备注">
            <el-input v-model="form.remark" type="textarea" :rows="2" style="width: 300px" />
          </el-form-item>
          <el-form-item label="出库明细">
            <div style="width: 100%">
              <el-table :data="items" border>
                <el-table-column label="商品" min-width="180">
                  <template #default="{ row }">
                    <el-select v-model="row.productId" filterable placeholder="选择商品" size="small">
                      <el-option v-for="p in products" :key="p.id" :label="`${p.name} (${p.code})`" :value="p.id" />
                    </el-select>
                  </template>
                </el-table-column>
                <el-table-column label="数量" width="140">
                  <template #default="{ row }">
                    <el-input-number v-model="row.quantity" :min="1" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="单价" width="140">
                  <template #default="{ row }">
                    <el-input-number v-model="row.unitPrice" :min="0" :precision="2" size="small" />
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80">
                  <template #default="{ $index }">
                    <el-button type="danger" link size="small" @click="removeItem($index)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <el-button type="primary" plain size="small" class="mt10" @click="addItem">+ 添加明细</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleCreate">保存草稿</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getStockOutPage, submitStockOut, withdrawStockOut,
  approveStockOut, rejectStockOut, postStockOut, createStockOut
} from '../api/stock'
import { getAllWarehouses } from '../api/warehouse'
import { getAllProducts } from '../api/product'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
const role = userStore.userInfo.role
const activeTab = ref('list')

// ===== 列表 =====
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ status: null, keyword: '', pageNum: 1, pageSize: 10 })

const STATUS_TEXT = ['草稿', '待审', '已审', '已过账']
const STATUS_TYPE = ['info', 'warning', 'primary', 'success']
function statusText(s) { return STATUS_TEXT[s] ?? '未知' }
function statusType(s) { return STATUS_TYPE[s] ?? 'info' }
function formatTime(t) { return t ? String(t).replace('T', ' ').slice(0, 16) : '' }
function fmt(p) { return Intl.NumberFormat('zh-CN', { minimumFractionDigits: 2 }).format(p || 0) }

const canGoToAudit = () => role === 'ADMIN' || role === 'MANAGER'
function canSubmit(row) { return row.status === 0 }
function canApprove(row) { return canGoToAudit() && row.status === 1 }
function canReject(row) { return canGoToAudit() && row.status === 1 }
function canWithdraw(row) { return row.status === 1 }
function canPost(row) { return canGoToAudit() && row.status === 2 }

async function fetchPage() {
  loading.value = true
  try {
    const data = await getStockOutPage(query)
    list.value = (data.list || []).map(r => ({ ...r, totalAmount: fmt(r.totalAmount) }))
    total.value = data.total
  } finally {
    loading.value = false
  }
}
function handleSearch() { query.pageNum = 1; fetchPage() }
function onPageChange(p) { query.pageNum = p; fetchPage() }

const ACTION_MSG = {
  submit: '确认提交审核？',
  approve: '确认审核通过？通过后该单不能再修改，可进入过账。',
  reject: '确认驳回？将退回草稿，可修改后重新提交。',
  withdraw: '确认撤回？将退回草稿。',
  post: '确认过账？过账后将正式扣减库存并记录流水，且不可撤销。'
}
const ACTION_API = {
  submit: submitStockOut, approve: approveStockOut,
  reject: rejectStockOut, withdraw: withdrawStockOut, post: postStockOut
}
async function doAction(row, act) {
  try {
    await ElMessageBox.confirm(ACTION_MSG[act], '提示', { type: 'warning' })
  } catch (e) { return }
  try {
    await ACTION_API[act](row.id)
    ElMessage.success('操作成功')
    fetchPage()
  } catch (e) { /* 拦截器已提示 */ }
}

// ===== 新建草稿 =====
const formRef = ref()
const warehouses = ref([])
const products = ref([])
const saving = ref(false)
const form = reactive({ warehouseId: null, receiver: '', operatorId: userStore.userInfo.userId, remark: '', items: [] })
const items = ref([{ productId: null, quantity: 1, unitPrice: 0 }])
const rules = {
  warehouseId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
  receiver: [{ required: true, message: '请输入领用人', trigger: 'blur' }]
}

async function loadWarehouses() {
  if (warehouses.value.length) return
  warehouses.value = await getAllWarehouses()
}
async function loadProducts() {
  products.value = await getAllProducts()
}
function addItem() { items.value.push({ productId: null, quantity: 1, unitPrice: 0 }) }
function removeItem(i) { items.value.splice(i, 1) }

async function handleCreate() {
  await formRef.value.validate()
  const valid = items.value.filter(i => i.productId)
  if (!valid.length) { ElMessage.warning('请至少添加一条有商品的明细'); return }
  saving.value = true
  try {
    await createStockOut({ ...form, items: valid.map(i => ({ productId: i.productId, quantity: i.quantity, unitPrice: i.unitPrice })) })
    ElMessage.success('出库草稿已创建')
    form.warehouseId = null; form.receiver = ''; form.remark = ''
    items.value = [{ productId: null, quantity: 1, unitPrice: 0 }]
  } catch (e) { /* 拦截器已提示 */ } finally {
    saving.value = false
  }
}

onMounted(() => { fetchPage(); loadWarehouses(); loadProducts() })
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.mt10 { margin-top: 10px; }
</style>