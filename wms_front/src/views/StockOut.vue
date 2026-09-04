<template>
  <el-card>
    <h3>创建出库单</h3>
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

      <!-- 明细列表 -->
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
        <el-button type="primary" :loading="saving" @click="handleSubmit">提交出库单</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { createStockOut } from '../api/stock'
import { getAllWarehouses } from '../api/warehouse'
import { getAllProducts } from '../api/product'
import { useUserStore } from '../store/user'

const userStore = useUserStore()
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

onMounted(() => {
  loadWarehouses()
  loadProducts()
})

async function loadWarehouses() {
  if (warehouses.value.length) return
  warehouses.value = await getAllWarehouses()
}

async function loadProducts() {
  products.value = await getAllProducts()
}

function addItem() {
  items.value.push({ productId: null, quantity: 1, unitPrice: 0 })
}

function removeItem(index) {
  items.value.splice(index, 1)
}

async function handleSubmit() {
  await formRef.value.validate()
  const validItems = items.value.filter(i => i.productId)
  if (!validItems.length) {
    ElMessage.warning('请至少添加一条有商品的明细')
    return
  }
  saving.value = true
  try {
    await createStockOut({ ...form, items: validItems.map(i => ({
      productId: i.productId, quantity: i.quantity, unitPrice: i.unitPrice
    })) })
    ElMessage.success('出库单创建成功')
    form.warehouseId = null; form.receiver = ''; form.remark = ''
    items.value = [{ productId: null, quantity: 1, unitPrice: 0 }]
  } catch (e) {
    // 库存不足等业务错误已由拦截器提示
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.mt10 { margin-top: 10px; }
</style>