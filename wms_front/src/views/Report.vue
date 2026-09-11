<template>
  <div>
    <el-card class="mb">
      <!-- 时间段筛选（三个报表共用），呆滞另有天数阈值 -->
      <el-form inline>
        <el-form-item label="开始日期">
          <el-date-picker v-model="range.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="range.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" />
        </el-form-item>
        <el-form-item label="呆滞天数" v-if="activeTab === 'dormant'">
          <el-input-number v-model="days" :min="1" :max="365" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="refresh">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card>
      <el-tabs v-model="activeTab" @tab-change="onTabChange">
        <!-- ===== 库存周转率 ===== -->
        <el-tab-pane label="库存周转率" name="turnover">
          <el-table :data="turnoverList" border stripe v-loading="loading" empty-text="暂无数据">
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="outQuantity" label="期间出库量" width="110" />
            <el-table-column prop="outAmount" label="出库金额" width="120">
              <template #default="{ row }">{{ formatMoney(row.outAmount) }}</template>
            </el-table-column>
            <el-table-column prop="beginQuantity" label="期初库存" width="90" />
            <el-table-column prop="endQuantity" label="期末库存" width="90" />
            <el-table-column prop="avgQuantity" label="平均库存" width="90" />
            <el-table-column prop="turnoverTimes" label="周转次数" width="100" />
            <el-table-column prop="turnoverDays" label="周转天数" width="100">
              <template #default="{ row }">{{ row.turnoverDays === null ? '-' : row.turnoverDays }}</template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <!-- ===== 呆滞分析 ===== -->
        <el-tab-pane label="呆滞分析" name="dormant">
          <el-table :data="dormantList" border stripe v-loading="loading" empty-text="暂无呆滞商品（未超过阈值天数未出库）">
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="quantity" label="库存数量" width="100" />
            <el-table-column prop="amount" label="库存金额" width="130">
              <template #default="{ row }">{{ formatMoney(row.amount) }}</template>
            </el-table-column>
            <el-table-column label="呆滞天数" width="100">
              <template #default="{ row }">
                <el-tag :type="row.dormantDays >= 90 ? 'danger' : 'warning'">{{ row.dormantDays }} 天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastOutDate" label="最后出库时间" />
          </el-table>
        </el-tab-pane>

        <!-- ===== ABC 分类 ===== -->
        <el-tab-pane label="ABC 分类" name="abc">
          <el-table :data="abcList" border stripe v-loading="loading" empty-text="暂无数据">
            <el-table-column prop="productCode" label="编码" width="130" />
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="outQuantity" label="出库量" width="100" />
            <el-table-column prop="outAmount" label="出库金额" width="130">
              <template #default="{ row }">{{ formatMoney(row.outAmount) }}</template>
            </el-table-column>
            <el-table-column label="金额占比" width="110">
              <template #default="{ row }">{{ row.amountRatio }}%</template>
            </el-table-column>
            <el-table-column label="累计占比" width="110">
              <template #default="{ row }">{{ row.cumulativeRatio }}%</template>
            </el-table-column>
            <el-table-column label="分类" width="90">
              <template #default="{ row }">
                <el-tag :type="abcTag(row.abcClass)">{{ row.abcClass }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { getTurnover, getDormant, getAbc } from '../api/report'

const activeTab = ref('turnover')
const loading = ref(false)
const range = reactive({ startDate: '', endDate: '' })
const days = ref(30)

const turnoverList = ref([])
const dormantList = ref([])
const abcList = ref([])

function params() {
  return {
    startDate: range.startDate || undefined,
    endDate: range.endDate || undefined
  }
}

function formatMoney(v) {
  return v === null || v === undefined ? '' : Number(v).toFixed(2)
}

function abcTag(cls) {
  return cls === 'A' ? 'danger' : cls === 'B' ? 'warning' : 'info'
}

async function refresh() {
  loading.value = true
  try {
    if (activeTab.value === 'turnover') turnoverList.value = await getTurnover(params())
    else if (activeTab.value === 'dormant') dormantList.value = await getDormant({ ...params(), days: days.value })
    else abcList.value = await getAbc(params())
  } finally {
    loading.value = false
  }
}

function onTabChange() {
  refresh()
}

onMounted(refresh)
</script>

<style scoped>
.mb { margin-bottom: 16px; }
</style>