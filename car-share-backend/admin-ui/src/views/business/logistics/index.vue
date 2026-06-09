<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="80px">
      <el-form-item label="快递单号" prop="expressNo">
        <el-input v-model="queryParams.expressNo" placeholder="请输入快递单号" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="物流状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="物流状态" clearable>
          <el-option label="待发货" :value="0" />
          <el-option label="已发货" :value="1" />
          <el-option label="运输中" :value="2" />
          <el-option label="派送中" :value="3" />
          <el-option label="已签收" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="primary" plain icon="el-icon-s-promotion" size="mini" @click="handleShip" v-hasPermi="['business:logistics:ship']">发货</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:logistics:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="logisticsList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="拼车标题" align="center" prop="carTitle" :show-overflow-tooltip="true" width="150" />
      <el-table-column label="车主" align="center" prop="ownerName" width="90" />
      <el-table-column label="快递公司" align="center" prop="expressCompany" width="110" />
      <el-table-column label="快递单号" align="center" prop="expressNo" :show-overflow-tooltip="true" width="160" />
      <el-table-column label="收件人" align="center" prop="receiverName" width="90" />
      <el-table-column label="物流状态" align="center" prop="status" width="100">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:logistics:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)" v-hasPermi="['business:logistics:edit']">编辑</el-button>
          <el-dropdown size="mini" @command="(cmd) => handleStatusCommand(cmd, scope.row)" v-hasPermi="['business:logistics:edit']">
            <el-button size="mini" type="text">更新状态<i class="el-icon-arrow-down el-icon--right"></i></el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="1" :disabled="scope.row.status >= 1">已发货</el-dropdown-item>
              <el-dropdown-item command="2" :disabled="scope.row.status >= 2">运输中</el-dropdown-item>
              <el-dropdown-item command="3" :disabled="scope.row.status >= 3">派送中</el-dropdown-item>
              <el-dropdown-item command="4" :disabled="scope.row.status >= 4">已签收</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <el-dialog title="发货" :visible.sync="shipOpen" width="650px" append-to-body>
      <el-form ref="shipForm" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="拼车ID" prop="carId">
          <el-input v-model.number="shipForm.carId" placeholder="请输入拼车ID" />
        </el-form-item>
        <el-divider content-position="left">快递信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="快递公司" prop="expressCompany">
              <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width:100%" @change="onExpressCompanyChange">
                <el-option v-for="item in expressCompanies" :key="item.code" :label="item.name" :value="item.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="快递单号" prop="expressNo">
              <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider content-position="left">寄件人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="寄件人" prop="senderName">
              <el-input v-model="shipForm.senderName" placeholder="请输入寄件人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="senderPhone">
              <el-input v-model="shipForm.senderPhone" placeholder="请输入寄件人电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="寄件地址" prop="senderAddress">
          <el-input v-model="shipForm.senderAddress" placeholder="请输入寄件人地址" />
        </el-form-item>
        <el-divider content-position="left">收件人信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收件人" prop="receiverName">
              <el-input v-model="shipForm.receiverName" placeholder="请输入收件人姓名" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="receiverPhone">
              <el-input v-model="shipForm.receiverPhone" placeholder="请输入收件人电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收件地址" prop="receiverAddress">
          <el-input v-model="shipForm.receiverAddress" placeholder="请输入收件人地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="shipForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shipOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </div>
    </el-dialog>

    <el-dialog title="编辑物流" :visible.sync="editOpen" width="600px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="快递公司" prop="expressCompany">
          <el-select v-model="editForm.expressCompany" placeholder="请选择快递公司" style="width:100%">
            <el-option v-for="item in expressCompanies" :key="item.code" :label="item.name" :value="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="expressNo">
          <el-input v-model="editForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
        <el-form-item label="寄件人" prop="senderName">
          <el-input v-model="editForm.senderName" placeholder="请输入寄件人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="senderPhone">
          <el-input v-model="editForm.senderPhone" placeholder="请输入寄件人电话" />
        </el-form-item>
        <el-form-item label="寄件地址" prop="senderAddress">
          <el-input v-model="editForm.senderAddress" placeholder="请输入寄件人地址" />
        </el-form-item>
        <el-form-item label="收件人" prop="receiverName">
          <el-input v-model="editForm.receiverName" placeholder="请输入收件人姓名" />
        </el-form-item>
        <el-form-item label="联系电话" prop="receiverPhone">
          <el-input v-model="editForm.receiverPhone" placeholder="请输入收件人电话" />
        </el-form-item>
        <el-form-item label="收件地址" prop="receiverAddress">
          <el-input v-model="editForm.receiverAddress" placeholder="请输入收件人地址" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="editOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitEdit">确 认</el-button>
      </div>
    </el-dialog>

    <el-dialog title="物流详情" :visible.sync="detailOpen" width="750px" append-to-body>
      <div v-if="detailData.logistics">
        <el-descriptions :column="2" border title="物流信息">
          <el-descriptions-item label="物流ID">{{ detailData.logistics.id }}</el-descriptions-item>
          <el-descriptions-item label="拼车ID">{{ detailData.logistics.carId }}</el-descriptions-item>
          <el-descriptions-item label="快递公司">{{ detailData.logistics.expressCompany }}</el-descriptions-item>
          <el-descriptions-item label="快递单号">{{ detailData.logistics.expressNo }}</el-descriptions-item>
          <el-descriptions-item label="物流状态">
            <el-tag :type="statusTagType(detailData.logistics.status)">{{ statusText(detailData.logistics.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ parseTime(detailData.logistics.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="寄件人">{{ detailData.logistics.senderName }}</el-descriptions-item>
          <el-descriptions-item label="寄件电话">{{ detailData.logistics.senderPhone }}</el-descriptions-item>
          <el-descriptions-item label="寄件地址" :span="2">{{ detailData.logistics.senderAddress }}</el-descriptions-item>
          <el-descriptions-item label="收件人">{{ detailData.logistics.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收件电话">{{ detailData.logistics.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收件地址" :span="2">{{ detailData.logistics.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ detailData.logistics.remark || '无' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="detailData.car" style="margin-top: 16px">
          <el-descriptions :column="2" border title="拼车信息">
            <el-descriptions-item label="标题">{{ detailData.car.title }}</el-descriptions-item>
            <el-descriptions-item label="商品">{{ detailData.car.goodsName }}</el-descriptions-item>
            <el-descriptions-item label="发起人">{{ detailData.car.userNickname }}</el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="carStatusTagType(detailData.car.status)">{{ carStatusText(detailData.car.status) }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div v-if="detailData.members && detailData.members.length > 0" style="margin-top: 16px">
          <h4>收货成员</h4>
          <el-table :data="detailData.members" border size="small">
            <el-table-column label="用户" prop="nickname" />
            <el-table-column label="金额" prop="amount" width="100" />
            <el-table-column label="收货地址" prop="address" :show-overflow-tooltip="true" />
            <el-table-column label="电话" prop="phone" width="130" />
            <el-table-column label="分配状态" width="100">
              <template slot-scope="scope">
                <el-tag :type="scope.row.distributionStatus === 2 ? 'success' : 'warning'" size="mini">
                  {{ scope.row.distributionStatus === 2 ? '已收货' : '待收货' }}
                </el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button @click="detailOpen = false">关 闭</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listLogistics, getLogistics, shipLogistics, updateLogistics, updateLogisticsStatus, delLogistics } from "@/api/business/logistics"

export default {
  name: "Logistics",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      logisticsList: [],
      shipOpen: false,
      editOpen: false,
      detailOpen: false,
      shipForm: {},
      editForm: {},
      detailData: {},
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        expressNo: undefined,
        status: undefined
      },
      expressCompanies: [
        { name: '顺丰速运', code: 'SF' },
        { name: '中通快递', code: 'ZTO' },
        { name: '圆通速递', code: 'YTO' },
        { name: '韵达快递', code: 'YD' },
        { name: '申通快递', code: 'STO' },
        { name: '百世快递', code: 'HTKY' },
        { name: '极兔速递', code: 'JTSD' },
        { name: '邮政EMS', code: 'EMS' },
        { name: '京东物流', code: 'JD' },
        { name: '德邦快递', code: 'DBL' }
      ],
      shipRules: {
        carId: [{ required: true, message: '请输入拼车ID', trigger: 'blur' }],
        expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
        expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
        senderName: [{ required: true, message: '请输入寄件人姓名', trigger: 'blur' }],
        senderPhone: [{ required: true, message: '请输入寄件人电话', trigger: 'blur' }],
        senderAddress: [{ required: true, message: '请输入寄件地址', trigger: 'blur' }],
        receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
        receiverPhone: [{ required: true, message: '请输入收件人电话', trigger: 'blur' }],
        receiverAddress: [{ required: true, message: '请输入收件地址', trigger: 'blur' }]
      },
      editRules: {
        expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
        expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.getList()
    if (this.$route.query.carId) {
      this.handleShipWithCarId(this.$route.query.carId)
    }
  },
  watch: {
    '$route.query'(newVal) {
      if (newVal.carId && newVal.autoShip === '1') {
        this.handleShipWithCarId(newVal.carId)
      }
    }
  },
  methods: {
    getList() {
      this.loading = true
      listLogistics(this.queryParams).then(response => {
        this.logisticsList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusText(status) {
      const map = { 0: '待发货', 1: '已发货', 2: '运输中', 3: '派送中', 4: '已签收' }
      return map[status] || '未知'
    },
    statusTagType(status) {
      const map = { 0: 'info', 1: 'primary', 2: '', 3: 'warning', 4: 'success' }
      return map[status] || 'info'
    },
    carStatusText(status) {
      const map = { 0: '招募中', 1: '进行中', 2: '已结算', 3: '配送中', 4: '已完成', 5: '已取消' }
      return map[status] || '未知'
    },
    carStatusTagType(status) {
      const map = { 0: 'warning', 1: '', 2: 'primary', 3: '', 4: 'success', 5: 'info' }
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
    handleShip() {
      this.shipForm = {
        carId: undefined,
        expressCompany: undefined,
        expressCompanyCode: undefined,
        expressNo: undefined,
        senderName: undefined,
        senderPhone: undefined,
        senderAddress: undefined,
        receiverName: undefined,
        receiverPhone: undefined,
        receiverAddress: undefined,
        remark: undefined
      }
      this.shipOpen = true
      this.$nextTick(() => {
        this.$refs.shipForm && this.$refs.shipForm.clearValidate()
      })
    },
    handleShipWithCarId(carId) {
      this.shipForm = {
        carId: parseInt(carId),
        expressCompany: undefined,
        expressCompanyCode: undefined,
        expressNo: undefined,
        senderName: undefined,
        senderPhone: undefined,
        senderAddress: undefined,
        receiverName: undefined,
        receiverPhone: undefined,
        receiverAddress: undefined,
        remark: undefined
      }
      this.shipOpen = true
      this.$nextTick(() => {
        this.$refs.shipForm && this.$refs.shipForm.clearValidate()
      })
    },
    onExpressCompanyChange(val) {
      const found = this.expressCompanies.find(c => c.name === val)
      if (found) {
        this.shipForm.expressCompanyCode = found.code
      }
    },
    submitShip() {
      this.$refs.shipForm.validate(valid => {
        if (valid) {
          shipLogistics(this.shipForm).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("发货成功")
              this.shipOpen = false
              this.getList()
            } else {
              this.$modal.msgError(response.msg || "发货失败")
            }
          })
        }
      })
    },
    handleView(row) {
      getLogistics(row.id).then(response => {
        this.detailData = response.data || {}
        this.detailOpen = true
      })
    },
    handleEdit(row) {
      this.editForm = {
        id: row.id,
        expressCompany: row.expressCompany,
        expressCompanyCode: row.expressCompanyCode,
        expressNo: row.expressNo,
        senderName: row.senderName,
        senderPhone: row.senderPhone,
        senderAddress: row.senderAddress,
        receiverName: row.receiverName,
        receiverPhone: row.receiverPhone,
        receiverAddress: row.receiverAddress,
        remark: row.remark
      }
      this.editOpen = true
      this.$nextTick(() => {
        this.$refs.editForm && this.$refs.editForm.clearValidate()
      })
    },
    submitEdit() {
      this.$refs.editForm.validate(valid => {
        if (valid) {
          updateLogistics(this.editForm).then(response => {
            if (response.code === 200) {
              this.$modal.msgSuccess("修改成功")
              this.editOpen = false
              this.getList()
            } else {
              this.$modal.msgError(response.msg || "修改失败")
            }
          })
        }
      })
    },
    handleStatusCommand(cmd, row) {
      const status = parseInt(cmd)
      const statusLabel = this.statusText(status)
      this.$modal.confirm('确认将物流状态更新为「' + statusLabel + '」？').then(() => {
        updateLogisticsStatus(row.id, status).then(response => {
          if (response.code === 200) {
            this.$modal.msgSuccess("状态更新成功")
            this.getList()
          } else {
            this.$modal.msgError(response.msg || "状态更新失败")
          }
        })
      }).catch(() => {})
    },
    handleDelete(row) {
      const logisticsIds = row.id || this.ids
      this.$modal.confirm('是否确认删除物流编号为"' + logisticsIds + '"的数据项？').then(function() {
        return delLogistics(logisticsIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    }
  }
}
</script>
