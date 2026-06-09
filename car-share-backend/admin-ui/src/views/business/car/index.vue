<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="拼车标题" prop="title">
        <el-input v-model="queryParams.title" placeholder="请输入拼车标题" clearable @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="拼车状态" clearable>
          <el-option label="招募中" :value="0" />
          <el-option label="进行中" :value="1" />
          <el-option label="已结算" :value="2" />
          <el-option label="配送中" :value="3" />
          <el-option label="已完成" :value="4" />
          <el-option label="已取消" :value="5" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:car:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="carList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="标题" align="center" prop="title" :show-overflow-tooltip="true" />
      <el-table-column label="商品" align="center" prop="goodsName" :show-overflow-tooltip="true" />
      <el-table-column label="人数" align="center" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.currentCount }}/{{ scope.row.totalCount }}</span>
        </template>
      </el-table-column>
      <el-table-column label="总价" align="center" prop="priceTotal" width="100" />
      <el-table-column label="单价" align="center" prop="pricePer" width="100" />
      <el-table-column label="截止时间" align="center" prop="deadline" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.deadline) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" prop="status" width="130">
        <template slot-scope="scope">
          <el-tag :type="statusTagType(scope.row.status)">{{ statusText(scope.row.status) }}</el-tag>
          <el-tag v-if="scope.row.status === 2" type="danger" size="mini" style="margin-left:4px">待发货</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" align="center" prop="createdAt" width="160">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="220">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:car:query']">查看</el-button>
          <el-button size="mini" type="text" icon="el-icon-s-promotion" @click="handleShip(scope.row)" v-if="scope.row.status === 2" v-hasPermi="['business:car:edit']">发货</el-button>
          <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" v-hasPermi="['business:car:remove']">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total>0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 拼车详情弹窗 -->
    <el-dialog title="拼车详情" :visible.sync="open" width="700px" append-to-body>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ form.id }}</el-descriptions-item>
        <el-descriptions-item label="标题">{{ form.title }}</el-descriptions-item>
        <el-descriptions-item label="商品名称">{{ form.goodsName }}</el-descriptions-item>
        <el-descriptions-item label="发起人">{{ form.userNickname }}</el-descriptions-item>
        <el-descriptions-item label="人数">{{ form.currentCount }}/{{ form.totalCount }}</el-descriptions-item>
        <el-descriptions-item label="总价">{{ form.priceTotal }}</el-descriptions-item>
        <el-descriptions-item label="单价">{{ form.pricePer }}</el-descriptions-item>
        <el-descriptions-item label="押金">{{ form.depositAmount }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ parseTime(form.deadline) }}</el-descriptions-item>
        <el-descriptions-item label="分配方式">{{ form.distributionType === 0 ? '智能分配' : '手动分配' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(form.status)">{{ statusText(form.status) }}</el-tag>
          <el-tag v-if="form.status === 2" type="danger" size="mini" style="margin-left:4px">待发货</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="成功率">{{ form.successRate }}%</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ form.description }}</el-descriptions-item>
      </el-descriptions>
      <div v-if="form.members && form.members.length > 0" style="margin-top: 16px">
        <h4>成员列表</h4>
        <el-table :data="form.members" border size="small">
          <el-table-column label="用户" prop="nickname" />
          <el-table-column label="金额" prop="amount" width="100" />
          <el-table-column label="身份" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.isOwner === 1 ? 'danger' : 'info'" size="mini">{{ scope.row.isOwner === 1 ? '团长' : '成员' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="支付状态" width="80">
            <template slot-scope="scope">
              <el-tag :type="scope.row.payStatus === 1 ? 'success' : 'warning'" size="mini">{{ scope.row.payStatus === 1 ? '已支付' : '未支付' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" icon="el-icon-s-promotion" @click="handleShipFromDetail" v-if="form.status === 2">去发货</el-button>
        <el-button @click="open = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 发货弹窗 -->
    <el-dialog title="发货" :visible.sync="shipOpen" width="650px" append-to-body>
      <el-form ref="shipForm" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="拼车ID">
          <el-input v-model="shipForm.carId" disabled />
        </el-form-item>
        <el-form-item label="拼车标题">
          <el-input v-model="shipForm.carTitle" disabled />
        </el-form-item>
        <el-divider content-position="left">快递信息</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="快递公司" prop="expressCompany">
              <el-select v-model="shipForm.expressCompany" placeholder="请选择快递公司" style="width:100%">
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
        <el-form-item label="备注">
          <el-input v-model="shipForm.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="shipOpen = false">取 消</el-button>
        <el-button type="primary" @click="submitShip">确认发货</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listCar, getCar, delCar } from "@/api/business/car"
import { shipLogistics } from "@/api/business/logistics"

export default {
  name: "Car",
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      carList: [],
      open: false,
      form: {},
      shipOpen: false,
      shipForm: {},
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
        expressCompany: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
        expressNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }],
        senderName: [{ required: true, message: '请输入寄件人姓名', trigger: 'blur' }],
        senderPhone: [{ required: true, message: '请输入寄件人电话', trigger: 'blur' }],
        senderAddress: [{ required: true, message: '请输入寄件地址', trigger: 'blur' }],
        receiverName: [{ required: true, message: '请输入收件人姓名', trigger: 'blur' }],
        receiverPhone: [{ required: true, message: '请输入收件人电话', trigger: 'blur' }],
        receiverAddress: [{ required: true, message: '请输入收件地址', trigger: 'blur' }]
      },
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        title: undefined,
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
      listCar(this.queryParams).then(response => {
        this.carList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    statusText(status) {
      const map = { 0: '招募中', 1: '进行中', 2: '已结算', 3: '配送中', 4: '已完成', 5: '已取消' }
      return map[status] || '未知'
    },
    statusTagType(status) {
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
    handleView(row) {
      getCar(row.id).then(response => {
        this.form = response.data
        this.open = true
      })
    },
    handleDelete(row) {
      const carIds = row.id || this.ids
      this.$modal.confirm('是否确认删除拼车编号为"' + carIds + '"的数据项？').then(function() {
        return delCar(carIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess("删除成功")
      }).catch(() => {})
    },
    handleShip(row) {
      this.shipForm = {
        carId: row.id,
        carTitle: row.title,
        expressCompany: undefined,
        expressNo: undefined,
        senderName: row.userNickname,
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
    handleShipFromDetail() {
      this.open = false
      this.shipForm = {
        carId: this.form.id,
        carTitle: this.form.title,
        expressCompany: undefined,
        expressNo: undefined,
        senderName: this.form.userNickname,
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
    }
  }
}
</script>
