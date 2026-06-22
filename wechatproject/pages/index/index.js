var { request, getFullImageUrl, loadImage } = require('../../utils/request');
var { getStatusInfo } = require('../../utils/status');

Page({
  data: {
    carList: [],
    currentFilter: 'all',
    page: 1,
    pageSize: 10,
    loading: false,
    hasMore: true,
    keyword: '',
    searchFocus: false,
    refreshing: false,
    isLogin: false,
    nickname: '',
    greeting: '',
    unreadCount: 0,
    platformStats: null,
    loadError: false,
    errorMsg: '',
    isEmpty: false,
    // 筛选相关
    sortBy: 'time',          // time / price / hot / deadline
    priceMin: '',
    priceMax: '',
    showFilterPanel: false,
    avatarColors: [
      'linear-gradient(135deg, #667eea, #764ba2)',
      'linear-gradient(135deg, #f093fb, #f5576c)',
      'linear-gradient(135deg, #4facfe, #00f2fe)',
      'linear-gradient(135deg, #43e97b, #38f9d7)',
      'linear-gradient(135deg, #fa709a, #fee140)',
      'linear-gradient(135deg, #a18cd1, #fbc2eb)'
    ]
  },

  // 防重复加载标记
  _loadingLock: false,
  _isOffline: false,

  onNetworkChange: function (isConnected) {
    if (isConnected && this._isOffline) {
      this._isOffline = false;
      wx.showToast({ title: '网络已恢复', icon: 'success', duration: 2000 });
      this.onPullDownRefresh();
    } else if (!isConnected) {
      this._isOffline = true;
      wx.showToast({ title: '⚠️ 网络已断开', icon: 'none', duration: 3000 });
    }
  },

  onLoad: function () {
    this.setGreeting();
    this.checkLogin();
    this.loadCarList();
    this.loadPlatformStats();
  },

  onShow: function () {
    this.checkLogin();
    this.loadUnreadCount();
    // onShow 时不重置列表（避免重复加载），只有下拉刷新才重置
    if (this.data.carList.length === 0 && !this._loadingLock) {
      this.loadCarList();
    }
  },

  onPullDownRefresh: function () {
    var that = this;
    this.setData({
      page: 1,
      carList: [],
      hasMore: true,
      refreshing: true,
      loadError: false,
      isEmpty: false
    });
    Promise.all([
      that.loadCarList(),
      that.loadPlatformStats()
    ]).then(function () {
      that.setData({ refreshing: false });
      wx.stopPullDownRefresh();
    }).catch(function () {
      that.setData({ refreshing: false });
      wx.stopPullDownRefresh();
    });
  },

  setGreeting: function () {
    var hour = new Date().getHours();
    var greeting = '';
    if (hour < 6) greeting = '🌙 夜深了';
    else if (hour < 9) greeting = '🌅 早上好';
    else if (hour < 12) greeting = '☀️ 上午好';
    else if (hour < 14) greeting = '🍱 中午好';
    else if (hour < 18) greeting = '🌤️ 下午好';
    else if (hour < 22) greeting = '🌆 晚上好';
    else greeting = '🌙 夜深了';
    this.setData({ greeting: greeting });
  },

  checkLogin: function () {
    var token = wx.getStorageSync('token');
    var userInfo = wx.getStorageSync('userInfo');
    var nickname = '';
    if (userInfo) {
      try {
        var info = typeof userInfo === 'string' ? JSON.parse(userInfo) : userInfo;
        nickname = info.nickname || info.nickName || '';
      } catch (e) {
        nickname = '';
      }
    }
    this.setData({
      isLogin: !!token,
      nickname: nickname
    });
  },

  loadUnreadCount: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ unreadCount: 0 });
      return;
    }
    var that = this;
    request({
      url: '/notification/unread-count',
      method: 'GET',
      loading: false,
      showError: false  // 不显示错误 toast
    }).then(function (result) {
      that.setData({ unreadCount: result || 0 });
    }).catch(function () {
      that.setData({ unreadCount: 0 });
    });
  },

  loadPlatformStats: function () {
    var that = this;
    return request({
      url: '/statistics/platform',
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      that.setData({ platformStats: result });
    }).catch(function () {
      that.setData({ platformStats: null });
    });
  },

  loadCarList: function () {
    var that = this;

    // 防重复加载
    if (this._loadingLock) return Promise.resolve();
    if (this.data.loading || !this.data.hasMore) return Promise.resolve();

    this._loadingLock = true;
    this.setData({ loading: true });

    var params = {
      page: this.data.page,
      pageSize: this.data.pageSize,
      keyword: this.data.keyword,
      sortBy: this.data.sortBy
    };

    if (this.data.currentFilter === 'recruiting') {
      params.status = 0; // RECRUITING
    } else if (this.data.currentFilter === 'grouping') {
      params.status = 1; // CLOSED (已满员/成团中)
    }

    // 价格筛选
    if (this.data.priceMin) {
      params.priceMin = parseFloat(this.data.priceMin);
    }
    if (this.data.priceMax) {
      params.priceMax = parseFloat(this.data.priceMax);
    }

    return request({
      url: '/car/list',
      method: 'GET',
      data: params,
      loading: this.data.page === 1,
      showError: false  // 手动处理错误，不显示全局 toast
    }).then(function (result) {
      that._loadingLock = false;
      var newList = [];
      if (result && result.list) {
        newList = result.list;
      } else if (Array.isArray(result)) {
        newList = result;
      }

      // 处理列表数据
      for (var i = 0; i < newList.length; i++) {
        var statusInfo = getStatusInfo(newList[i].status);
        newList[i]._statusText = statusInfo.text;
        newList[i].timeLabel = that.formatTime(newList[i].createdAt || newList[i].created_at);
        newList[i]._successRate = that.calcSuccessRate(newList[i]);
        newList[i]._fav = false;  // 默认未收藏
      }

      // 检查收藏状态（登录用户）
      that.markFavorites(newList);

      // 异步加载图片（不阻塞渲染）
      that.loadListImages(newList);

      var carList = that.data.page === 1
        ? newList
        : that.data.carList.concat(newList);

      that.setData({
        carList: carList,
        hasMore: newList.length >= that.data.pageSize,
        loading: false,
        loadError: false,
        isEmpty: carList.length === 0
      });
    }).catch(function (err) {
      that._loadingLock = false;
      that.setData({
        loading: false,
        loadError: that.data.page === 1,
        errorMsg: err.message || '加载失败',
        isEmpty: that.data.carList.length === 0 && that.data.page === 1
      });
    });
  },

  // 标记收藏状态
  markFavorites: function (list) {
    var that = this;
    var token = wx.getStorageSync('token');
    if (!token || !list.length) return;

    request({
      url: '/favorite/my',
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      var favIds = {};
      if (result && result.list) {
        for (var i = 0; i < result.list.length; i++) {
          favIds[result.list[i].carId] = true;
        }
      }
      for (var i = 0; i < list.length; i++) {
        if (favIds[list[i].id]) {
          list[i]._fav = true;
        }
      }
      that.setData({ carList: that.data.carList });
    }).catch(function () {});
  },

  // 异步加载图片列表（失败不阻塞）
  loadListImages: function (list) {
    var imageTasks = [];
    for (var i = 0; i < list.length; i++) {
      var imgUrl = list[i].goodsImage || list[i].goods_image;
      if (!imgUrl) continue;
      imageTasks.push(
        loadImage(imgUrl).then(function (localPath) {
          // 不阻塞渲染，静默加载
        }).catch(function () {
          // 静默失败
        })
      );
    }
    Promise.all(imageTasks);
  },

  formatTime: function (dateStr) {
    if (!dateStr) return '刚刚';
    try {
      var date = new Date(dateStr);
      if (isNaN(date.getTime())) return '刚刚';
      var now = new Date();
      var diff = now - date;
      var minutes = Math.floor(diff / 60000);
      if (minutes < 1) return '刚刚';
      if (minutes < 60) return minutes + '分钟前';
      var hours = Math.floor(minutes / 60);
      if (hours < 24) return hours + '小时前';
      var days = Math.floor(hours / 24);
      if (days < 7) return days + '天前';
      return (date.getMonth() + 1) + '/' + date.getDate();
    } catch (e) {
      return '刚刚';
    }
  },

  calcSuccessRate: function (car) {
    if (!car.totalCount && !car.total_count) return null;
    var total = car.totalCount || car.total_count || 1;
    var current = car.currentCount || car.current_count || 0;
    return Math.round((current / total) * 100);
  },

  onSearchInput: function (e) {
    this.setData({ keyword: e.detail.value });
  },

  onSearchConfirm: function () {
    this.setData({
      page: 1,
      carList: [],
      hasMore: true,
      loadError: false,
      isEmpty: false
    });
    this.loadCarList();
  },

  onSearchFocus: function () {
    this.setData({ searchFocus: true });
  },

  onClearSearch: function () {
    this.setData({
      keyword: '',
      searchFocus: false,
      page: 1,
      carList: [],
      hasMore: true,
      loadError: false,
      isEmpty: false
    });
    this.loadCarList();
  },

  /* ========== 筛选排序 ========== */

  onToggleFilter: function () {
    this.setData({ showFilterPanel: !this.data.showFilterPanel });
  },

  onSortChange: function (e) {
    var sortBy = e.currentTarget.dataset.sort;
    if (sortBy === this.data.sortBy) return;
    this.setData({
      sortBy: sortBy,
      page: 1,
      carList: [],
      showFilterPanel: false
    });
    this.loadCarList();
  },

  onPriceMinInput: function (e) {
    this.setData({ priceMin: e.detail.value });
  },

  onPriceMaxInput: function (e) {
    this.setData({ priceMax: e.detail.value });
  },

  onPriceFilter: function () {
    this.setData({
      page: 1,
      carList: [],
      showFilterPanel: false
    });
    this.loadCarList();
  },

  onClearFilter: function () {
    this.setData({
      priceMin: '',
      priceMax: '',
      sortBy: 'time',
      page: 1,
      carList: [],
      showFilterPanel: false
    });
    this.loadCarList();
  },

  onFilterChange: function (e) {
    var filter = e.currentTarget.dataset.filter;
    if (filter === this.data.currentFilter) return;

    this.setData({
      currentFilter: filter,
      page: 1,
      carList: [],
      hasMore: true,
      loadError: false,
      isEmpty: false
    });
    this.loadCarList();
  },

  onLoadMore: function () {
    if (!this.data.hasMore || this.data.loading || this._loadingLock) return;
    this.setData({ page: this.data.page + 1 });
    this.loadCarList();
  },

  // 收藏切换
  toggleFav: function (e) {
    var that = this;
    var carId = e.currentTarget.dataset.id;
    var idx = e.currentTarget.dataset.idx;
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }

    var carList = this.data.carList.slice();
    var isFav = carList[idx]._fav;

    request({
      url: '/favorite/' + carId,
      method: isFav ? 'DELETE' : 'POST',
      loading: false,
      showError: false
    }).then(function () {
      carList[idx]._fav = !isFav;
      that.setData({ carList: carList });
      wx.showToast({ title: isFav ? '已取消收藏' : '收藏成功', icon: 'success' });
    }).catch(function () {});
  },

  goToDetail: function (e) {
    var carId = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + carId
    });
  },

  goToPublish: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    wx.navigateTo({ url: '/pages/publish/publish' });
  },

  goToRecommend: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    wx.navigateTo({ url: '/pages/recommend/recommend' });
  },

  goToOrder: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    wx.switchTab({ url: '/pages/order/order' });
  },

  goToStatistics: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    wx.navigateTo({ url: '/pages/statistics/statistics' });
  },

  goToNotification: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    wx.navigateTo({ url: '/pages/notification/notification' });
  },

  goToProfile: function () {
    wx.switchTab({ url: '/pages/profile/profile' });
  },

  // 错误重试
  onRetry: function () {
    this.setData({
      page: 1,
      carList: [],
      hasMore: true,
      loadError: false,
      isEmpty: false
    });
    this.loadCarList();
    this.loadPlatformStats();
  }
});
