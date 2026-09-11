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
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getStockPage, getStockSummary, getStockTrend } from '../api/stock'
import { getAllWarehouses } from '../api/warehouse'

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
</script>

<style scoped>
.mb { margin-bottom: 16px; }
.pager { margin-top: 16px; justify-content: flex-end; }
</style>