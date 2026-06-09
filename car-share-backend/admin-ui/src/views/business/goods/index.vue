<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="queryParams.name" placeholder="请输入商品名称" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="商品类型" clearable>
          <el-option label="零食" value="snack" />
          <el-option label="饮料" value="drink" />
          <el-option label="日用品" value="daily" />
          <el-option label="其他" value="other" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:goods:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="goodsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="商品名称" align="center" prop="name" :show-overflow-tooltip="true" />
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template slot-scope="scope">
          <span>{{ typeText(scope.row.type) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="市场价" align="center" prop="marketPrice" width="100" />
      <el-table-column label="状态" align="center" prop="status" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">{{ scope.row.status === 1 ? '上架' : '下架' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="150">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:goods:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['business:goods:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="商品详情" :visible.sync="open" width="500px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ form.name }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ typeText(form.type) }}</el-descriptions-item>
        <el-descriptions-item label="市场价">{{ form.marketPrice }}</el-descriptions-item>
        <el-descriptions-item label="版本">{{ form.versions }}</el-descriptions-item>
        <el-descriptions-item label="卡类型">{{ form.cards }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="form.status === 1 ? 'success' : 'info'">{{ form.status === 1 ? '上架' : '下架' }}</el-tag>
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
import { listGoods, getGoods, delGoods } from "@/api/business/goods"

export default {
  name: "Goods",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      goodsList: [],
      open: false,
      form: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        name: undefined,
        type: undefined
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listGoods(this.queryParams).then(response => {
        this.goodsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    typeText(type) {
      const map = { snack: '零食', drink: '饮料', daily: '日用品', other: '其他' }
      return map[type] || type
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
      getGoods(row.id).then(response => {
        this.form = response.data
        this.open = true
      })
    },
    handleDelete(row) {
      const goodsIds = row.id || this.ids
      this.$modal.confirm('是否确认删除商品编号为"' + goodsIds + '"的数据项？').then(function() {
        return delGoods(goodsIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
