<template>
  <div>
    <!-- 页签：库存查询 / 汇总 / 出入库趋势 -->
    <el-tabs v-model="activeTab">
      <!-- ===== 库存分页查询 ===== -->
      <el-tab-pane label="库存查询" name="page">
        <el-card class="mb">
          <el-form inline>
            <el-form-item label="关键字">
              <el-input v-model="query.keyword" placeholder="商品名/编码" clearable style="width: 180px" @keyup.enter="handleSearch" />
            </el-form-item>
            <el-form-item label="仓库">
              <el-select v-model="query.warehouseId" placeholder="全部" clearable style="width: 160px" :loading="whLoading" @visible-change="loadWarehouses">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card>
          <el-table :data="list" border stripe v-loading="loading">
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="warehouseName" label="仓库" />
            <el-table-column prop="quantity" label="在库" width="90" />
            <el-table-column prop="lockedQuantity" label="锁定" width="90" />
            <el-table-column prop="availableQuantity" label="可售" width="90" />
            <el-table-column prop="warningQty" label="预警值" width="90" />
            <el-table-column label="预警" width="90">
              <template #default="{ row }">
                <el-tag :type="row.warnFlag ? 'danger' : 'success'">{{ row.warnFlag ? '预警' : '正常' }}</el-tag>
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

      <!-- ===== 库存汇总（按商品） ===== -->
      <el-tab-pane label="库存汇总" name="summary">
        <el-card>
          <el-table :data="summaryList" border stripe v-loading="summaryLoading">
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="totalQty" label="总库存" />
            <el-table-column prop="warehouseCount" label="仓库数" />
            <el-table-column label="预警" width="90">
              <template #default="{ row }">
                <el-tag :type="row.warnFlag ? 'danger' : 'success'">{{ row.warnFlag ? '预警' : '正常' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ===== 出入库日报趋势 ===== -->
      <el-tab-pane label="出入库趋势" name="trend">
        <el-card class="mb">
          <el-form inline>
            <el-form-item label="开始日期">
              <el-date-picker v-model="trend.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
            </el-form-item>
            <el-form-item label="结束日期">
              <el-date-picker v-model="trend.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadTrend">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card>
          <el-table :data="trendList" border stripe v-loading="trendLoading">
            <el-table-column prop="date" label="日期" />
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-tag :type="row.changeType === 'IN' ? 'success' : 'warning'">{{ row.changeType === 'IN' ? '入库' : '出库' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="totalQty" label="数量" />
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ===== 库存流水（s3-6） ===== -->
      <el-tab-pane label="库存流水" name="log">
        <el-card class="mb">
          <el-form inline>
            <el-form-item label="商品">
              <el-select v-model="logQuery.productId" placeholder="全部" clearable style="width: 180px" :loading="prdLoading" @visible-change="loadProducts">
                <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="仓库">
              <el-select v-model="logQuery.warehouseId" placeholder="全部" clearable style="width: 160px" :loading="whLoading" @visible-change="loadWarehouses">
                <el-option v-for="w in warehouses" :key="w.id" :label="w.name" :value="w.id" />
              </el-select>
            </el-form-item>
            <el-form-item label="类型">
              <el-select v-model="logQuery.changeType" placeholder="全部" clearable style="width: 120px">
                <el-option label="入库" value="IN" />
                <el-option label="出库" value="OUT" />
              </el-select>
            </el-form-item>
            <el-form-item label="日期">
              <el-date-picker
                v-model="logRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                value-format="YYYY-MM-DD"
                style="width: 240px"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="searchLog">查询</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card>
          <el-table :data="logList" border stripe v-loading="logLoading">
            <el-table-column prop="createTime" label="时间" width="180" />
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="warehouseName" label="仓库" />
            <el-table-column label="类型" width="90">
              <template #default="{ row }">
                <el-tag :type="row.changeType === 'IN' ? 'success' : 'danger'">{{ row.changeType === 'IN' ? '入库' : '出库' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="变动数量" width="110">
              <template #default="{ row }">
                <span :class="row.changeType === 'IN' ? 'in-qty' : 'out-qty'">
                  {{ row.changeType === 'IN' ? '+' : '-' }}{{ row.changeQuantity }}
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="beforeQuantity" label="变动前" width="80" />
            <el-table-column prop="afterQuantity" label="变动后" width="80" />
            <el-table-column prop="orderNo" label="单据号" width="180" />
            <el-table-column prop="operatorName" label="操作人" width="100" />
          </el-table>
          <el-pagination
            class="pager"
            background
            layout="total, prev, pager, next"
            :total="logTotal"
            :current-page="logQuery.pageNum"
            :page-size="logQuery.pageSize"
            @current-change="onLogPageChange"
          />
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockPage, getStockSummary, getStockTrend, getStockLogPage } from '../api/stock'
import { getAllWarehouses } from '../api/warehouse'
import { getAllProducts } from '../api/product'

const activeTab = ref('page')

// ===== 库存分页 =====
const list = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ keyword: '', warehouseId: null, pageNum: 1, pageSize: 10 })

// ===== 汇总 =====
const summaryList = ref([])
const summaryLoading = ref(false)

// ===== 趋势 =====
const trendList = ref([])
const trendLoading = ref(false)
const trend = reactive({ startDate: '', endDate: '' })

// ===== 流水（s3-6） =====
const logList = ref([])
const logTotal = ref(0)
const logLoading = ref(false)
const logRange = ref(null)
const logQuery = reactive({ productId: null, warehouseId: null, changeType: null, startDate: '', endDate: '', pageNum: 1, pageSize: 10 })
const products = ref([])
const prdLoading = ref(false)

// 仓库下拉
const warehouses = ref([])
const whLoading = ref(false)
async function loadWarehouses(visible) {
  if (!visible || warehouses.value.length) return
  whLoading.value = true
  try {
    warehouses.value = await getAllWarehouses()
  } finally {
    whLoading.value = false
  }
}

// 商品下拉
async function loadProducts(visible) {
  if (!visible || products.value.length) return
  prdLoading.value = true
  try {
    products.value = await getAllProducts()
  } finally {
    prdLoading.value = false
  }
}

onMounted(() => {
  fetchPage()
  loadWarehouses(true)
})

async function fetchPage() {
  loading.value = true
  try {
    const data = await getStockPage(query)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleSearch() {
  query.pageNum = 1
  fetchPage()
}

function onPageChange(page) {
  query.pageNum = page
  fetchPage()
}

// 切到汇总页时加载
// 用 watch 处理页签切换
import { watch } from 'vue'
watch(activeTab, (val) => {
  if (val === 'summary' && !summaryList.value.length) loadSummary()
  if (val === 'trend' && !trendList.value.length) loadTrend()
  if (val === 'log' && !logList.value.length) loadLog()
})

async function loadSummary() {
  summaryLoading.value = true
  try {
    summaryList.value = await getStockSummary()
  } finally {
    summaryLoading.value = false
  }
}

async function loadTrend() {
  trendLoading.value = true
  try {
    trendList.value = await getStockTrend({
      startDate: trend.startDate || undefined,
      endDate: trend.endDate || undefined
    })
  } finally {
    trendLoading.value = false
  }
}

// ===== 流水（s3-6） =====
async function loadLog() {
  logLoading.value = true
  try {
    // 日期范围组 ['开始','结束'] → 拆成两个参数字段
    const [startDate, endDate] = logRange.value || []
    const data = await getStockLogPage({
      productId: logQuery.productId,
      warehouseId: logQuery.warehouseId,
      changeType: logQuery.changeType || undefined,
      startDate: startDate || undefined,
      endDate: endDate || undefined,
      pageNum: logQuery.pageNum,
      pageSize: logQuery.pageSize
    })
    logList.value = data.list
    logTotal.value = data.total
  } finally {
    logLoading.value = false
  }
}

function searchLog() {
  logQuery.pageNum = 1
  loadLog()
}

function onLogPageChange(page) {
  logQuery.pageNum = page
  loadLog()
}
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
.in-qty { color: #67c23a; font-weight: 600; }
.out-qty { color: #f56c6c; font-weight: 600; }
</style>