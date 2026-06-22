const { request } = require('../../utils/request');
const { getStatusInfo } = require('../../utils/status');

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
        var statusInfo = getStatusInfo(list[i].status);
        list[i]._statusText = statusInfo.text;
        list[i]._statusClass = 'tag-status-' + list[i].status;
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

  goToEdit(e) {
    var carId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/publish/publish?editId=' + carId
    });
  },

  onExport() {
    var that = this;
    wx.showLoading({ title: '生成中...' });
    request({
      url: '/car/export',
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      wx.hideLoading();
      if (!result || result.length === 0) {
        wx.showToast({ title: '暂无数据可导出', icon: 'none' });
        return;
      }
      // 导出CSV
      var csv = '﻿标题,商品,状态,总价,人均,人数,创建时间\n';
      for (var i = 0; i < result.length; i++) {
        var r = result[i];
        csv += r.title + ',' + r.goods + ',' + r.status + ',' + r.totalPrice + ',' + r.perPrice + ',' + r.members + ',' + r.createdAt + '\n';
      }
      var fs = wx.getFileSystemManager();
      var path = wx.env.USER_DATA_PATH + '/我的拼车记录.csv';
      fs.writeFileSync(path, csv, 'utf-8');
      wx.openDocument({
        filePath: path,
        success: function () {
          wx.showToast({ title: '导出成功', icon: 'success' });
        }
      });
    }).catch(function () {
      wx.hideLoading();
      wx.showToast({ title: '导出失败', icon: 'none' });
    });
  },

  goToIndex() {
    wx.switchTab({ url: '/pages/index/index' });
  }
});
