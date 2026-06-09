<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="审核状态" clearable>
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
      </el-form-item>
      <el-form-item label="类型" prop="type">
        <el-select v-model="queryParams.type" placeholder="凭证类型" clearable>
          <el-option label="支付凭证" :value="0" />
          <el-option label="收货凭证" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="evidenceList">
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="拼车ID" align="center" prop="carId" width="80" />
      <el-table-column label="用户ID" align="center" prop="userId" width="80" />
      <el-table-column label="类型" align="center" prop="type" width="100">
        <template slot-scope="scope">
          <span>{{ scope.row.type === 0 ? '支付凭证' : '收货凭证' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="凭证图片" align="center" prop="imageUrl" width="120">
        <template slot-scope="scope">
          <el-image v-if="scope.row.imageUrl" :src="getImageUrl(scope.row.imageUrl)" style="width: 60px; height: 60px" fit="cover" :preview-src-list="[getImageUrl(scope.row.imageUrl)]" />
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="90">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true" />
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="80">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:evidence:query']">查看</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="凭证详情" :visible.sync="open" width="500px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="拼车ID">{{ form.carId }}</el-descriptions-item>
        <el-descriptions-item label="用户ID">{{ form.userId }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ form.type === 0 ? '支付凭证' : '收货凭证' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(form.status)">{{ statusText(form.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ parseTime(form.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ form.remark }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="form.imageUrl" style="margin-top: 15px; text-align: center;">
        <el-image :src="getImageUrl(form.imageUrl)" style="max-width: 400px; max-height: 400px" fit="contain" :preview-src-list="[getImageUrl(form.imageUrl)]" />
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listEvidence, getEvidence } from "@/api/business/evidence"

export default {
  name: "Evidence",
  data() {
    return {
      loading: true,
      showSearch: true,
      total: 0,
      evidenceList: [],
      open: false,
      form: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        status: undefined,
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
      listEvidence(this.queryParams).then(response => {
        this.evidenceList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusText(status) {
      const map = { 0: '待审核', 1: '已通过', 2: '已拒绝' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'warning', 1: 'success', 2: 'danger' }
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
    handleView(row) {
      getEvidence(row.id).then(response => {
        this.form = response.data
        this.open = true
      })
    },
    getImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('[')) {
        try {
          const urls = JSON.parse(url)
          if (Array.isArray(urls) && urls.length > 0) {
            let first = urls[0]
            if (!first.startsWith('http://') && !first.startsWith('https://')) {
              first = process.env.VUE_APP_BASE_API + first
            }
            return first
          }
        } catch (e) {}
      }
      return process.env.VUE_APP_BASE_API + url
    }
  }
}
</script>
