var { request } = require('../../utils/request');

Page({
  data: {
    list: [],
    total: 0,
    loading: false,
    page: 1,
    pageSize: 20,
    hasMore: true
  },

  onLoad: function () {
    this.loadFavorites();
  },

  onShow: function () {
    if (this.data.list.length === 0) {
      this.loadFavorites();
    }
  },

  loadFavorites: function () {
    var that = this;
    this.setData({ loading: true });

    request({
      url: '/favorite/my',
      method: 'GET',
      data: { page: this.data.page, pageSize: this.data.pageSize },
      loading: false,
      showError: false
    }).then(function (result) {
      var newList = result.list || [];
      // 格式化时间
      for (var i = 0; i < newList.length; i++) {
        newList[i].timeLabel = that.formatTime(newList[i].createdAt);
      }
      that.setData({
        list: that.data.page === 1 ? newList : that.data.list.concat(newList),
        total: result.total || 0,
        hasMore: newList.length >= that.data.pageSize,
        loading: false
      });
    }).catch(function () {
      that.setData({ loading: false });
    });
  },

  removeFav: function (e) {
    var that = this;
    var carId = e.currentTarget.dataset.id;
    var index = e.currentTarget.dataset.index;

    wx.showModal({
      title: '取消收藏',
      content: '确定取消收藏该拼车？',
      success: function (res) {
        if (res.confirm) {
          request({
            url: '/favorite/' + carId,
            method: 'DELETE',
            loading: false,
            showError: true
          }).then(function () {
            var list = that.data.list.slice();
            list.splice(index, 1);
            that.setData({
              list: list,
              total: that.data.total - 1
            });
            wx.showToast({ title: '已取消收藏', icon: 'success' });
          }).catch(function () {
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  goToDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/detail/detail?id=' + id });
  },

  goToIndex: function () {
    wx.switchTab({ url: '/pages/index/index' });
  },

  formatTime: function (dateStr) {
    if (!dateStr) return '';
    try {
      var date = new Date(dateStr);
      if (isNaN(date.getTime())) return '';
      var now = new Date();
      var diff = now - date;
      var days = Math.floor(diff / 86400000);
      if (days === 0) return '今天';
      if (days === 1) return '昨天';
      if (days < 7) return days + '天前';
      return (date.getMonth() + 1) + '/' + date.getDate();
    } catch (e) {
      return '';
    }
  }
});
