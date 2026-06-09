<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="订单号" prop="orderNo">
        <el-input v-model="queryParams.orderNo" placeholder="请输入订单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="订单状态" clearable>
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已退款" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:order:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="orderList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="订单号" align="center" prop="orderNo" :show-overflow-tooltip="true" />
      <el-table-column label="拼车ID" align="center" prop="carId" width="80" />
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="金额" align="center" prop="amount" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="结算状态" align="center" prop="settleStatus" width="90">
        <template slot-scope="scope">
          <el-tag :type="scope.row.settleStatus === 1 ? 'success' : 'info'">{{ scope.row.settleStatus === 1 ? '已结算' : '未结算' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:order:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['business:order:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="订单详情" :visible.sync="open" width="500px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="订单号">{{ form.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="拼车ID">{{ form.carId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ form.userId }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ form.amount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(form.status)">{{ statusText(form.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="结算状态">
          <el-tag :type="form.settleStatus === 1 ? 'success' : 'info'">{{ form.settleStatus === 1 ? '已结算' : '未结算' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(form.createdAt) }}</el-descriptions-item>
      </el-descriptions>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listOrder, getOrder, delOrder } from "@/api/business/order"

export default {
  name: "Order",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      orderList: [],
      open: false,
      form: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        orderNo: undefined,
        status: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listOrder(this.queryParams).then(response => {
        this.orderList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusText(status) {
      const map = { 0: '待支付', 1: '已支付', 2: '已退款' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'info' }
      return map[status] || 'info'
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm("queryForm")
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    handleView(row) {
      getOrder(row.id).then(response => {
        this.form = response.data
        this.open = true
      })
    },
    handleDelete(row) {
      const orderIds = row.id || this.ids
      this.$modal.confirm('是否确认删除订单编号为"' + orderIds + '"的数据项？').then(function() {
        return delOrder(orderIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
