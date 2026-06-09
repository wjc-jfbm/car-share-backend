<template>
  <div class="app-container dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stat-cards">
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-user" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-user"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">注册用户</div>
            <div class="stat-value">{{ dashboard.userTotal || 0 }}</div>
            <div class="stat-sub">
              <span>团长 <b>{{ dashboard.userLeader || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-car" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-van"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">拼车活动</div>
            <div class="stat-value">{{ dashboard.carTotal || 0 }}</div>
            <div class="stat-sub">
              <span>进行中 <b>{{ dashboard.carActive || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-order" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-document"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">订单总数</div>
            <div class="stat-value">{{ dashboard.orderTotal || 0 }}</div>
            <div class="stat-sub">
              <span>已支付 <b>{{ dashboard.orderPaid || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-goods" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-goods"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">商品总数</div>
            <div class="stat-value">{{ dashboard.goodsTotal || 0 }}</div>
            <div class="stat-sub">
              <span>在售 <b>{{ dashboard.goodsOnSale || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-logistics" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-truck"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">物流总数</div>
            <div class="stat-value">{{ dashboard.logisticsTotal || 0 }}</div>
            <div class="stat-sub">
              <span>已发货 <b>{{ dashboard.logisticsShipped || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="8" :lg="4">
        <el-card class="stat-card stat-settle" shadow="hover">
          <div class="stat-icon">
            <i class="el-icon-money"></i>
          </div>
          <div class="stat-info">
            <div class="stat-label">已结算</div>
            <div class="stat-value">{{ dashboard.orderSettled || 0 }}</div>
            <div class="stat-sub">
              <span>已完成 <b>{{ dashboard.carCompleted || 0 }}</b></span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 今日动态 -->
    <el-row :gutter="16" class="today-row">
      <el-col :span="8">
        <div class="today-item today-user">
          <i class="el-icon-user"></i>
          <span>今日新增用户</span>
          <b>{{ dashboard.todayNewUser || 0 }}</b>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="today-item today-car">
          <i class="el-icon-van"></i>
          <span>今日新增拼车</span>
          <b>{{ dashboard.todayNewCar || 0 }}</b>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="today-item today-order">
          <i class="el-icon-document"></i>
          <span>今日新增订单</span>
          <b>{{ dashboard.todayNewOrder || 0 }}</b>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :lg="14">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>近7天趋势</span>
          </div>
          <div class="chart-wrapper">
            <div ref="trendChart" style="height: 350px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="10">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>拼车状态分布</span>
          </div>
          <div class="chart-wrapper">
            <div ref="carStatusChart" style="height: 350px;"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :sm="24" :lg="10">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>订单状态分布</span>
          </div>
          <div class="chart-wrapper">
            <div ref="orderStatusChart" style="height: 350px;"></div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="24" :lg="14">
        <el-card shadow="hover">
          <div slot="header" class="card-header">
            <span>最近拼车活动</span>
          </div>
          <el-table :data="recentCars" style="width: 100%" size="small" :show-header="true">
            <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
            <el-table-column prop="ownerName" label="团长" width="80" />
            <el-table-column label="状态" width="80" align="center">
              <template slot-scope="scope">
                <el-tag :type="carStatusType(scope.row.status)" size="mini">
                  {{ carStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="memberCount" label="人数" width="60" align="center" />
            <el-table-column label="创建时间" width="100" align="center">
              <template slot-scope="scope">
                {{ formatDate(scope.row.createdAt) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import { getDashboard, getOrderTrend, getCarStatusDist, getOrderStatusDist, getRecentCars } from '@/api/business/statistics'

export default {
  name: 'Index',
  data() {
    return {
      dashboard: {},
      recentCars: [],
      trendChart: null,
      carStatusChart: null,
      orderStatusChart: null
    }
  },
  created() {
    this.loadData()
  },
  mounted() {
    window.addEventListener('resize', this.handleResize)
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.handleResize)
    if (this.trendChart) this.trendChart.dispose()
    if (this.carStatusChart) this.carStatusChart.dispose()
    if (this.orderStatusChart) this.orderStatusChart.dispose()
  },
  methods: {
    loadData() {
      getDashboard().then(res => {
        this.dashboard = res.data
      })
      getOrderTrend().then(res => {
        this.$nextTick(() => this.renderTrendChart(res.data))
      })
      getCarStatusDist().then(res => {
        this.$nextTick(() => this.renderCarStatusChart(res.data))
      })
      getOrderStatusDist().then(res => {
        this.$nextTick(() => this.renderOrderStatusChart(res.data))
      })
      getRecentCars().then(res => {
        this.recentCars = res.data || []
      })
    },
    handleResize() {
      if (this.trendChart) this.trendChart.resize()
      if (this.carStatusChart) this.carStatusChart.resize()
      if (this.orderStatusChart) this.orderStatusChart.resize()
    },
    renderTrendChart(data) {
      if (!data) return
      this.trendChart = echarts.init(this.$refs.trendChart)
      this.trendChart.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['新增订单', '新增拼车'] },
        grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
        xAxis: { type: 'category', data: data.dates, boundaryGap: false },
        yAxis: { type: 'value', minInterval: 1 },
        series: [
          {
            name: '新增订单',
            type: 'line',
            smooth: true,
            data: data.orderCounts,
            itemStyle: { color: '#409EFF' },
            areaStyle: { color: 'rgba(64,158,255,0.15)' }
          },
          {
            name: '新增拼车',
            type: 'line',
            smooth: true,
            data: data.carCounts,
            itemStyle: { color: '#67C23A' },
            areaStyle: { color: 'rgba(103,194,58,0.15)' }
          }
        ]
      })
    },
    renderCarStatusChart(data) {
      if (!data) return
      this.carStatusChart = echarts.init(this.$refs.carStatusChart)
      this.carStatusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 10, left: 'center' },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data: data,
          label: { show: true, formatter: '{b}\n{c}' },
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
          itemStyle: {
            color: function(params) {
              var colors = ['#409EFF', '#67C23A', '#E6A23C', '#909399']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      })
    },
    renderOrderStatusChart(data) {
      if (!data) return
      this.orderStatusChart = echarts.init(this.$refs.orderStatusChart)
      this.orderStatusChart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
        legend: { bottom: 10, left: 'center' },
        series: [{
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '45%'],
          data: data,
          label: { show: true, formatter: '{b}\n{c}' },
          emphasis: { itemStyle: { shadowBlur: 10, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.5)' } },
          itemStyle: {
            color: function(params) {
              var colors = ['#E6A23C', '#67C23A', '#909399']
              return colors[params.dataIndex % colors.length]
            }
          }
        }]
      })
    },
    carStatusText(status) {
      var map = { 1: '招募中', 2: '进行中', 3: '已截止', 4: '已完成' }
      return map[status] || '未知'
    },
    carStatusType(status) {
      var map = { 1: 'warning', 2: 'success', 3: 'info', 4: '' }
      return map[status] || 'info'
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      return dateStr.substring(5, 10)
    }
  }
}
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background: #f0f2f5;
  min-height: calc(100vh - 84px);
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 8px;
  border: none;
  position: relative;
  overflow: hidden;
  transition: transform 0.3s;

  &:hover {
    transform: translateY(-4px);
  }

  .el-card__body {
    display: flex;
    align-items: center;
    padding: 20px;
  }
}

.stat-card ::v-deep .el-card__body {
  display: flex;
  align-items: center;
  padding: 20px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  flex-shrink: 0;

  i {
    font-size: 28px;
    color: #fff;
  }
}

.stat-car .stat-icon { background: linear-gradient(135deg, #409EFF, #66b1ff); }
.stat-order .stat-icon { background: linear-gradient(135deg, #67C23A, #85ce61); }
.stat-goods .stat-icon { background: linear-gradient(135deg, #E6A23C, #ebb563); }
.stat-logistics .stat-icon { background: linear-gradient(135deg, #F56C6C, #f78989); }
.stat-user .stat-icon { background: linear-gradient(135deg, #9b59b6, #b07cd8); }
.stat-settle .stat-icon { background: linear-gradient(135deg, #1abc9c, #48d1b5); }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #303133;
  line-height: 1.2;
}

.stat-sub {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;

  span {
    margin-right: 12px;
  }

  b {
    color: #606266;
  }
}

.today-row {
  margin-bottom: 20px;

  .today-item {
    display: flex;
    align-items: center;
    padding: 14px 20px;
    border-radius: 8px;
    background: #fff;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    i {
      font-size: 22px;
      margin-right: 10px;
    }

    span {
      flex: 1;
      font-size: 14px;
      color: #606266;
    }

    b {
      font-size: 22px;
      font-weight: 700;
    }
  }

  .today-car {
    i { color: #409EFF; }
    b { color: #409EFF; }
  }

  .today-order {
    i { color: #67C23A; }
    b { color: #67C23A; }
  }

  .today-user {
    i { color: #9b59b6; }
    b { color: #9b59b6; }
  }
}

.chart-row {
  margin-bottom: 20px;
}

.card-header {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-wrapper {
  padding: 10px 0;
}
</style>
