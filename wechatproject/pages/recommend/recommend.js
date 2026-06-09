var { request } = require('../../utils/request');

Page({
  data: {
    list: [],
    total: 0,
    page: 1,
    pageSize: 10,
    loading: false,
    hasMore: true,
    avatarColors: [
      'linear-gradient(135deg, #FF6B9D, #A78BFA)',
      'linear-gradient(135deg, #60A5FA, #34D399)',
      'linear-gradient(135deg, #FBBF24, #FF6B9D)',
      'linear-gradient(135deg, #A78BFA, #60A5FA)',
      'linear-gradient(135deg, #34D399, #FBBF24)',
      'linear-gradient(135deg, #F87171, #A78BFA)'
    ]
  },

  _loadingLock: false,

  onLoad: function () {
    this.loadRecommend();
  },

  onShow: function () {
    if (this.data.list.length === 0 && !this._loadingLock) {
      this.loadRecommend();
    }
  },

  loadRecommend: function () {
    if (this._loadingLock || this.data.loading || !this.data.hasMore) return;

    this._loadingLock = true;
    this.setData({ loading: true });

    var that = this;
    request({
      url: '/car/recommend',
      method: 'GET',
      data: {
        page: this.data.page,
        pageSize: this.data.pageSize
      },
      loading: this.data.page === 1,
      showError: false
    }).then(function (result) {
      that._loadingLock = false;
      var newList = result.list || [];

      // 增强匹配数据：处理AI评分和标签
      for (var i = 0; i < newList.length; i++) {
        var item = newList[i];
        var score = item.matchScore || 0;
        item._matchLevel = that.getMatchLevel(score);
        item._matchLabel = that.getMatchLabel(score);
        item._matchColor = that.getMatchColor(score);
        item._matchAngle = Math.round(score * 360);
      }

      var list = that.data.page === 1 ? newList : that.data.list.concat(newList);

      that.setData({
        list: list,
        total: result.total || 0,
        hasMore: newList.length >= that.data.pageSize,
        loading: false
      });
    }).catch(function () {
      that._loadingLock = false;
      that.setData({ loading: false });
    });
  },

  goToDetail: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?id=' + id
    });
  },

  goToProfile: function () {
    wx.switchTab({ url: '/pages/profile/profile' });
  },

  onReachBottom: function () {
    if (!this.data.hasMore || this.data.loading) return;
    this.setData({ page: this.data.page + 1 });
    this.loadRecommend();
  },

  /* AI 匹配度辅助函数 */
  getMatchLevel: function (score) {
    if (score >= 0.7) return 'high';
    if (score >= 0.4) return 'mid';
    return 'low';
  },

  getMatchLabel: function (score) {
    if (score >= 0.8) return '🔥 非常匹配';
    if (score >= 0.6) return '✅ 高度匹配';
    if (score >= 0.4) return '📌 适合你';
    if (score >= 0.2) return '👀 可以看看';
    return '💡 一般推荐';
  },

  getMatchColor: function (score) {
    if (score >= 0.6) return '#34D399';
    if (score >= 0.3) return '#FBBF24';
    return '#F87171';
  }
});
