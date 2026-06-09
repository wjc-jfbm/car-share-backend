const { request } = require('../../utils/request');

var expressCompanies = [
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
];

Page({
  data: {
    carId: '',
    car: {},
    isOwner: false,
    logisticsList: [],
    showCreateModal: false,
    showUpdateModal: false,
    showCompanyPicker: false,
    expressCompanies: expressCompanies,
    selectedCompanyIndex: -1,
    currentLogistics: null,
    newLogistics: {
      expressNo: '',
      expressCompany: '',
      expressCompanyCode: '',
      receiverName: '',
      receiverPhone: '',
      receiverAddress: '',
      remark: ''
    },
    statusMap: {
      0: { text: '待发货', color: '#999', icon: '⏳' },
      1: { text: '已发货', color: '#2196F3', icon: '📦' },
      2: { text: '运输中', color: '#FF9800', icon: '🚚' },
      3: { text: '已到达', color: '#9C27B0', icon: '📍' },
      4: { text: '已签收', color: '#4CAF50', icon: '✅' },
      5: { text: '异常', color: '#F44336', icon: '⚠️' }
    },
    memberList: [],
    showMemberSelect: false,
    selectedMemberIndex: -1
  },

  onLoad(options) {
    if (options && options.carId) {
      this.setData({ carId: options.carId });
      this.loadLogistics(options.carId);
      if (options.autoShip === '1') {
        var that = this;
        setTimeout(function () {
          that.onShowCreateModal();
        }, 800);
      }
    }
  },

  onShow() {
    if (this.data.carId) {
      this.loadLogistics(this.data.carId);
    }
  },

  async loadLogistics(carId) {
    try {
      var result = await request({
        url: '/logistics/car/' + carId,
        method: 'GET'
      });

      var userId = wx.getStorageSync('userId');
      var isOwner = false;
      if (result && result.car) {
        result.car.status = parseInt(result.car.status);
        var carUserId = result.car.userId || result.car.user_id;
        isOwner = String(carUserId) === String(userId);
      }

      var logisticsList = [];
      if (result && result.list) {
        logisticsList = result.list;
      } else if (result && result.logistics) {
        logisticsList = Array.isArray(result.logistics) ? result.logistics : [result.logistics];
      }

      for (var i = 0; i < logisticsList.length; i++) {
        logisticsList[i].status = parseInt(logisticsList[i].status);
        if (logisticsList[i].nodes) {
          try {
            logisticsList[i].nodes = typeof logisticsList[i].nodes === 'string'
              ? JSON.parse(logisticsList[i].nodes) : logisticsList[i].nodes;
          } catch (e) {
            logisticsList[i].nodes = [];
          }
        } else {
          logisticsList[i].nodes = [];
        }
      }

      var memberList = (result && result.members) || [];

      this.setData({
        car: (result && result.car) || {},
        isOwner: isOwner,
        logisticsList: logisticsList,
        memberList: memberList
      });
    } catch (err) {
      console.error('loadLogistics error:', err);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  onShowCreateModal() {
    this.setData({
      showCreateModal: true,
      newLogistics: {
        expressNo: '',
        expressCompany: '',
        expressCompanyCode: '',
        receiverName: '',
        receiverPhone: '',
        receiverAddress: '',
        remark: ''
      },
      selectedCompanyIndex: -1,
      selectedMemberIndex: -1
    });
  },

  onHideCreateModal() {
    this.setData({ showCreateModal: false });
  },

  onExpressNoInput(e) {
    var obj = this.data.newLogistics;
    obj.expressNo = e.detail.value;
    this.setData({ newLogistics: obj });
  },

  onShowCompanyPicker() {
    this.setData({ showCompanyPicker: true });
  },

  onHideCompanyPicker() {
    this.setData({ showCompanyPicker: false });
  },

  onCompanySelect(e) {
    var idx = e.currentTarget.dataset.index;
    var company = expressCompanies[idx];
    var obj = this.data.newLogistics;
    obj.expressCompany = company.name;
    obj.expressCompanyCode = company.code;
    this.setData({
      newLogistics: obj,
      selectedCompanyIndex: idx,
      showCompanyPicker: false
    });
  },

  onShowMemberSelect() {
    this.setData({ showMemberSelect: true });
  },

  onHideMemberSelect() {
    this.setData({ showMemberSelect: false });
  },

  onMemberSelect(e) {
    var idx = e.currentTarget.dataset.index;
    var member = this.data.memberList[idx];
    var obj = this.data.newLogistics;
    obj.receiverName = member.nickname || '';
    obj.receiverPhone = member.phone || '';
    obj.receiverAddress = member.address || '';
    this.setData({
      newLogistics: obj,
      selectedMemberIndex: idx,
      showMemberSelect: false
    });
  },

  onReceiverNameInput(e) {
    var obj = this.data.newLogistics;
    obj.receiverName = e.detail.value;
    this.setData({ newLogistics: obj });
  },

  onReceiverPhoneInput(e) {
    var obj = this.data.newLogistics;
    obj.receiverPhone = e.detail.value;
    this.setData({ newLogistics: obj });
  },

  onReceiverAddressInput(e) {
    var obj = this.data.newLogistics;
    obj.receiverAddress = e.detail.value;
    this.setData({ newLogistics: obj });
  },

  onRemarkInput(e) {
    var obj = this.data.newLogistics;
    obj.remark = e.detail.value;
    this.setData({ newLogistics: obj });
  },

  async onCreateLogistics() {
    var data = this.data.newLogistics;
    if (!data.expressNo.trim()) {
      wx.showToast({ title: '请输入快递单号', icon: 'none' });
      return;
    }
    if (!data.expressCompany) {
      wx.showToast({ title: '请选择快递公司', icon: 'none' });
      return;
    }
    if (!data.receiverName.trim()) {
      wx.showToast({ title: '请输入收件人', icon: 'none' });
      return;
    }
    if (!data.receiverPhone.trim()) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' });
      return;
    }
    if (!data.receiverAddress.trim()) {
      wx.showToast({ title: '请输入收货地址', icon: 'none' });
      return;
    }

    try {
      await request({
        url: '/logistics',
        method: 'POST',
        data: {
          car_id: this.data.carId,
          express_no: data.expressNo.trim(),
          express_company: data.expressCompany,
          express_company_code: data.expressCompanyCode,
          receiver_name: data.receiverName.trim(),
          receiver_phone: data.receiverPhone.trim(),
          receiver_address: data.receiverAddress.trim(),
          remark: data.remark.trim()
        }
      });
      wx.showToast({ title: '发货成功', icon: 'success' });
      this.setData({ showCreateModal: false });
      this.loadLogistics(this.data.carId);
    } catch (err) {
      wx.showToast({ title: err.message || '创建失败', icon: 'none' });
    }
  },

  onShowUpdateModal(e) {
    var logistics = e.currentTarget.dataset.logistics;
    this.setData({
      showUpdateModal: true,
      currentLogistics: logistics
    });
  },

  onHideUpdateModal() {
    this.setData({ showUpdateModal: false, currentLogistics: null });
  },

  async onUpdateStatus(e) {
    var status = parseInt(e.currentTarget.dataset.status);
    var logistics = this.data.currentLogistics;
    if (!logistics) return;

    try {
      await request({
        url: '/logistics/' + logistics.id + '/status',
        method: 'PUT',
        data: {
          status: status
        }
      });
      wx.showToast({ title: '状态更新成功', icon: 'success' });
      this.setData({ showUpdateModal: false });
      this.loadLogistics(this.data.carId);
    } catch (err) {
      wx.showToast({ title: err.message || '更新失败', icon: 'none' });
    }
  },

  async onConfirmReceive(e) {
    var logistics = e.currentTarget.dataset.logistics;
    var that = this;
    wx.showModal({
      title: '确认签收',
      content: '确认已收到包裹？',
      success: async function (res) {
        if (res.confirm) {
          try {
            await request({
              url: '/logistics/' + logistics.id + '/status',
              method: 'PUT',
              data: { status: 4 }
            });
            wx.showToast({ title: '签收成功', icon: 'success' });
            that.loadLogistics(that.data.carId);
          } catch (err) {
            wx.showToast({ title: err.message || '操作失败', icon: 'none' });
          }
        }
      }
    });
  },

  onCopyExpressNo(e) {
    var no = e.currentTarget.dataset.no;
    wx.setClipboardData({
      data: no,
      success: function () {
        wx.showToast({ title: '已复制单号', icon: 'success' });
      }
    });
  },

  onCallReceiver(e) {
    var phone = e.currentTarget.dataset.phone;
    if (!phone) {
      wx.showToast({ title: '无联系电话', icon: 'none' });
      return;
    }
    wx.makePhoneCall({ phoneNumber: phone });
  }
});
