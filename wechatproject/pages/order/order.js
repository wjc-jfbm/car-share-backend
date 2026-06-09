const { request } = require('../../utils/request');

Page({
  data: {
    orderList: [],
    currentTab: 'all',
    loading: false
  },

  onLoad(options) {
    if (options && options.tab) {
      this.setData({ currentTab: options.tab });
    }
  },

  onShow() {
    this.loadOrders();
  },

  async loadOrders() {
    var token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ orderList: [] });
      return;
    }

    this.setData({ loading: true });
    try {
      var url = '/car/my';
      if (this.data.currentTab === 'joined') {
        url = '/car/joined';
      } else if (this.data.currentTab === 'all') {
        url = '/car/my-all';
      }

      var result = await request({
        url: url,
        method: 'GET',
        loading: false
      });

      var userId = wx.getStorageSync('userId');
      var list = result.list || result || [];
      for (var i = 0; i < list.length; i++) {
        list[i].status = parseInt(list[i].status);
        var carUserId = list[i].userId || list[i].user_id;
        list[i].isOwner = String(carUserId) === String(userId);
      }

      this.setData({
        orderList: list,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  onTabChange(e) {
    var tab = e.currentTarget.dataset.tab;
    this.setData({ currentTab: tab });
    this.loadOrders();
  },

  goToDetail(e) {
    var carId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + carId
    });
  },

  goToShip(e) {
    var carId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/logistics/logistics?carId=' + carId + '&autoShip=1'
    });
  },

  goToIndex() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
