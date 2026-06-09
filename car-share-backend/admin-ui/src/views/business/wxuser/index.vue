<template>
  <div class="app-container">
    <el-form :model="queryParams" ref="queryForm" size="small" :inline="true" v-show="showSearch" label-width="68px">
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="queryParams.nickname" placeholder="请输入用户昵称" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="手机号" prop="phone">
        <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable style="width: 200px" @keyup.enter.native="handleQuery" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="queryParams.status" placeholder="用户状态" clearable style="width: 200px">
          <el-option label="正常" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item label="角色" prop="role">
        <el-select v-model="queryParams.role" placeholder="用户角色" clearable style="width: 200px">
          <el-option label="普通用户" :value="0" />
          <el-option label="团长" :value="1" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="el-icon-search" size="mini" @click="handleQuery">搜索</el-button>
        <el-button icon="el-icon-refresh" size="mini" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button type="danger" plain icon="el-icon-delete" size="mini" :disabled="multiple" @click="handleDelete" v-hasPermi="['business:wxuser:remove']">删除</el-button>
      </el-col>
      <right-toolbar :showSearch.sync="showSearch" @queryTable="getList"></right-toolbar>
    </el-row>

    <el-table v-loading="loading" :data="userList" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" align="center" />
      <el-table-column label="ID" align="center" prop="id" width="60" />
      <el-table-column label="头像" align="center" width="70">
        <template slot-scope="scope">
          <el-avatar :size="36" :src="scope.row.avatar" v-if="scope.row.avatar">
            <img src="https://cube.elemecdn.com/e/fd/0fc7d20532fdaf769a25683617711png.png"/>
          </el-avatar>
          <el-avatar :size="36" v-else>用户</el-avatar>
        </template>
      </el-table-column>
      <el-table-column label="昵称" align="center" prop="nickname" :show-overflow-tooltip="true" min-width="100" />
      <el-table-column label="手机号" align="center" prop="phone" width="120" />
      <el-table-column label="真实姓名" align="center" prop="realName" width="90" />
      <el-table-column label="性别" align="center" width="60">
        <template slot-scope="scope">
          <span v-if="scope.row.gender === 1">男</span>
          <span v-else-if="scope.row.gender === 2">女</span>
          <span v-else>未知</span>
        </template>
      </el-table-column>
      <el-table-column label="角色" align="center" width="80">
        <template slot-scope="scope">
          <el-tag :type="scope.row.role === 1 ? 'warning' : 'info'" size="mini">
            {{ scope.row.role === 1 ? '团长' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="信用分" align="center" prop="creditScore" width="75" />
      <el-table-column label="交易" align="center" width="80">
        <template slot-scope="scope">
          <span>{{ scope.row.successTransactions || 0 }}/{{ scope.row.totalTransactions || 0 }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" align="center" width="70">
        <template slot-scope="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="mini">
            {{ scope.row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" align="center" prop="createdAt" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.createdAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="最后登录" align="center" prop="lastLoginAt" width="150">
        <template slot-scope="scope">
          <span>{{ parseTime(scope.row.lastLoginAt) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" align="center" class-name="small-padding fixed-width" width="140">
        <template slot-scope="scope">
          <el-button size="mini" type="text" icon="el-icon-view" @click="handleView(scope.row)" v-hasPermi="['business:wxuser:query']">详情</el-button>
          <el-button size="mini" type="text" icon="el-icon-edit" @click="handleUpdate(scope.row)" v-hasPermi="['business:wxuser:edit']">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <pagination v-show="total > 0" :total="total" :page.sync="queryParams.pageNum" :limit.sync="queryParams.pageSize" @pagination="getList" />

    <!-- 用户详情对话框 -->
    <el-dialog title="用户详情" :visible.sync="viewOpen" width="550px" append-to-body>
      <el-descriptions :column="2" border size="medium">
        <el-descriptions-item label="用户ID">{{ viewForm.id }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ viewForm.nickname }}</el-descriptions-item>
        <el-descriptions-item label="真实姓名">{{ viewForm.realName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ viewForm.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="性别">
          <span v-if="viewForm.gender === 1">男</span>
          <span v-else-if="viewForm.gender === 2">女</span>
          <span v-else>未知</span>
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <el-tag :type="viewForm.role === 1 ? 'warning' : 'info'" size="mini">
            {{ viewForm.role === 1 ? '团长' : '用户' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="信用分">{{ viewForm.creditScore || 100 }}</el-descriptions-item>
        <el-descriptions-item label="信用等级">{{ viewForm.creditLevel || 1 }}</el-descriptions-item>
        <el-descriptions-item label="总交易数">{{ viewForm.totalTransactions || 0 }}</el-descriptions-item>
        <el-descriptions-item label="成功交易数">{{ viewForm.successTransactions || 0 }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="viewForm.status === 1 ? 'success' : 'danger'" size="mini">
            {{ viewForm.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ parseTime(viewForm.createdAt) }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ parseTime(viewForm.lastLoginAt) || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 编辑用户对话框 -->
    <el-dialog title="编辑用户" :visible.sync="editOpen" width="450px" append-to-body>
      <el-form ref="editForm" :model="editForm" :rules="editRules" label-width="80px">
        <el-input v-model="editForm.id" type="hidden" />
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="editForm.role" placeholder="请选择角色">
            <el-option label="普通用户" :value="0" />
            <el-option label="团长" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="editForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitEdit">确 定</el-button>
        <el-button @click="editOpen = false">取 消</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { listWxUser, getWxUser, updateWxUser, delWxUser } from '@/api/business/wxuser'

export default {
  name: 'WxUser',
  data() {
    return {
      loading: true,
      ids: [],
      multiple: true,
      showSearch: true,
      total: 0,
      userList: [],
      queryParams: {
        pageNum: 1,
        pageSize: 10,
        nickname: undefined,
        phone: undefined,
        status: undefined,
        role: undefined
      },
      viewOpen: false,
      viewForm: {},
      editOpen: false,
      editForm: {},
      editRules: {
        status: [{ required: true, message: '请选择状态', trigger: 'change' }]
      }
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getList() {
      this.loading = true
      listWxUser(this.queryParams).then(response => {
        this.userList = response.rows
        this.total = response.total
        this.loading = false
      })
    },
    handleQuery() {
      this.queryParams.pageNum = 1
      this.getList()
    },
    resetQuery() {
      this.resetForm('queryForm')
      this.handleQuery()
    },
    handleSelectionChange(selection) {
      this.ids = selection.map(item => item.id)
      this.multiple = !selection.length
    },
    handleView(row) {
      getWxUser(row.id).then(response => {
        this.viewForm = response.data
        this.viewOpen = true
      })
    },
    handleUpdate(row) {
      getWxUser(row.id).then(response => {
        this.editForm = response.data
        this.editOpen = true
      })
    },
    submitEdit() {
      this.$refs['editForm'].validate(valid => {
        if (valid) {
          updateWxUser(this.editForm).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.editOpen = false
            this.getList()
          })
        }
      })
    },
    handleDelete(row) {
      const userIds = row.id || this.ids
      this.$modal.confirm('是否确认删除用户编号为"' + userIds + '"的数据项？').then(() => {
        return delWxUser(userIds)
      }).then(() => {
        this.getList()
        this.$modal.msgSuccess('删除成功')
      }).catch(() => {})
    }
  }
}
</script>
