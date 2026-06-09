var { request, uploadFile, getFullImageUrl, loadImage } = require('../../utils/request');

Page({
  data: {
    car: {},
    carId: '',
    hasJoined: false,
    hasClaimed: false,
    hasPaid: false,
    hasEvidence: false,
    evidenceStatus: null,
    isOwner: false,
    isFavorited: false,     // 收藏状态
    myMemberId: null,
    memberCount: 0,
    showClaimModal: false,
    versionList: [],
    cardList: [],
    showJoinModal: false,
    joinVersions: [],
    joinCards: [],
    joinPhone: '',
    joinAddress: '',
    reviews: [],            // 评价列表
    showReviewModal: false,
    reviewRating: 5,
    reviewContent: '',
    evidenceStatusMap: {
      0: { text: '待审核', color: '#FF9800' },
      1: { text: '已通过', color: '#4CAF50' },
      2: { text: '已驳回', color: '#F44336' }
    }
  },

  onLoad: function (options) {
    if (options.id) {
      this.setData({ carId: options.id });
      this.loadCarDetail(options.id);
    }
  },

  onShow: function () {
    if (this.data.carId) {
      this.loadCarDetail(this.data.carId);
    }
  },

  loadCarDetail: function (carId) {
    var that = this;

    // 防重复加载
    if (this._loadingDetail) return;
    this._loadingDetail = true;

    request({
      url: '/car/detail/' + carId,
      method: 'GET',
      showError: false
    }).then(function (result) {
      that._loadingDetail = false;

      if (!result) {
        wx.showToast({ title: '拼车不存在', icon: 'none' });
        return;
      }

      var members = result.members || [];
      var nickname = result.userNickname || result.user_nickname || '';
      if (nickname) {
        result.avatarLetter = nickname.charAt(0);
      }
      for (var i = 0; i < members.length; i++) {
        var m = members[i];
        var mNickname = m.nickname || m.user_nickname || '';
        if (mNickname) {
          m.avatarLetter = mNickname.charAt(0);
        }
      }

      // 解析商品版本/小卡（JSON字符串解析）
      if (result.goods) {
        try {
          result.goods.versionList = typeof result.goods.versions === 'string'
            ? JSON.parse(result.goods.versions) : (result.goods.versions || []);
        } catch (e) {
          result.goods.versionList = [];
        }
        try {
          result.goods.cardList = typeof result.goods.cards === 'string'
            ? JSON.parse(result.goods.cards) : (result.goods.cards || []);
        } catch (e) {
          result.goods.cardList = [];
        }
      }

      // 构建图片URL
      if (result.goodsImage || result.goods_image) {
        result.goodsImageFull = getFullImageUrl(result.goodsImage || result.goods_image);
      }
      if (result.userAvatar || result.user_avatar) {
        result.userAvatarFull = getFullImageUrl(result.userAvatar || result.user_avatar);
      }

      // 先渲染页面，再异步加载图片（图片失败不阻塞）
      result.status = parseInt(result.status);
      that.setData({
        car: result,
        memberCount: members.length
      });
      that.checkUserStatus(result);

      // 预处理评价数据（WXML不支持方法调用）
      that.loadReviews(carId, function (reviews) {
        var processed = [];
        for (var i = 0; i < reviews.length; i++) {
          processed.push({
            id: reviews[i].id,
            rating: reviews[i].rating || 5,
            content: reviews[i].content || '',
            // 预生成星标字符串
            stars: '★★★★★'.substring(0, 5 - Math.max(1, Math.min(5, reviews[i].rating || 5))) + '★'.repeat(Math.max(1, Math.min(5, reviews[i].rating || 5))),
            // 预格式化日期
            dateLabel: that.formatReviewDate(reviews[i].createdAt)
          });
        }
        that.setData({ reviews: processed });
      });

      // 检查收藏状态
      that.checkFavorite(carId);
      // 加载评价
      that.loadReviews(carId);

      // 静默加载图片，失败不阻塞
      if (result.goodsImage || result.goods_image) {
        loadImage(result.goodsImage || result.goods_image).then(function (p) {
          that.setData({ 'car.goodsImageLocal': p });
        }).catch(function () {});
      }
      if (result.userAvatar || result.user_avatar) {
        loadImage(result.userAvatar || result.user_avatar).then(function (p) {
          that.setData({ 'car.userAvatarLocal': p });
        }).catch(function () {});
      }
    }).catch(function (err) {
      that._loadingDetail = false;
      wx.showToast({ title: err.message || '加载失败', icon: 'none' });
    });
  },

  checkUserStatus: function (car) {
    var userId = wx.getStorageSync('userId');
    if (!userId) return;

    var members = car.members || [];
    var myMember = null;
    for (var i = 0; i < members.length; i++) {
      if (members[i].userId == userId || members[i].user_id == userId) {
        myMember = members[i];
        break;
      }
    }

    if (myMember) {
      var evidenceUrl = myMember.evidenceUrl || myMember.evidence_url;
      var evidenceStatus = myMember.evidenceStatus != null ? myMember.evidenceStatus : (myMember.evidence_status != null ? myMember.evidence_status : null);
      this.setData({
        hasJoined: true,
        hasClaimed: !!(myMember.claimedVersion || myMember.claimed_version || myMember.claimedCard || myMember.claimed_card),
        hasPaid: myMember.payStatus === 1 || myMember.pay_status === 1 || myMember.pay_status === 2,
        hasEvidence: !!(evidenceUrl),
        evidenceStatus: evidenceStatus,
        isOwner: myMember.isOwner === 1 || myMember.is_owner === 1,
        myMemberId: myMember.id
      });
    }

    if (car.userId == userId || car.user_id == userId) {
      this.setData({ isOwner: true });
    }
  },

  onJoin: function () {
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }

    var car = this.data.car;
    var versions = [];
    var cards = [];

    try {
      if (car.goods && car.goods.versions) {
        versions = typeof car.goods.versions === 'string' ? JSON.parse(car.goods.versions) : car.goods.versions;
      }
    } catch (e) { }

    try {
      if (car.goods && car.goods.cards) {
        cards = typeof car.goods.cards === 'string' ? JSON.parse(car.goods.cards) : car.goods.cards;
      }
    } catch (e) { }

    // 添加 _jsel 标记（WXML不支持indexOf）
    versions = (versions || []).map(function(v) { return {_jsel: false, v: v}; });
    cards = (cards || []).map(function(c) { return {_jsel: false, c: c}; });

    this.setData({
      showJoinModal: true,
      joinVersions: versions,
      joinCards: cards,
      joinPhone: '',
      joinAddress: ''
    });
  },

  onJoinVersionSelect: function (e) {
    var idx = e.currentTarget.dataset.index;
    var list = this.data.joinVersions.slice();
    list[idx]._jsel = !list[idx]._jsel;
    this.setData({ joinVersions: list });
  },

  onJoinCardSelect: function (e) {
    var idx = e.currentTarget.dataset.index;
    var list = this.data.joinCards.slice();
    list[idx]._jsel = !list[idx]._jsel;
    this.setData({ joinCards: list });
  },

  onJoinPhoneInput: function (e) {
    this.setData({ joinPhone: e.detail.value });
  },

  onJoinAddressInput: function (e) {
    this.setData({ joinAddress: e.detail.value });
  },

  onJoinCancel: function () {
    this.setData({ showJoinModal: false });
  },

  onJoinSubmit: function () {
    var that = this;
    var carId = this.data.carId;
    var joinData = {};

    // 从带标记的列表中提取选中的值
    var selVersions = this.data.joinVersions.filter(function(v) { return v._jsel; }).map(function(v) { return v.v; });
    var selCards = this.data.joinCards.filter(function(c) { return c._jsel; }).map(function(c) { return c.c; });

    if (selVersions.length > 0) {
      joinData.prefVersions = selVersions.join(',');
    }
    if (selCards.length > 0) {
      joinData.prefCards = selCards.join(',');
    }
    if (this.data.joinPhone.trim()) {
      joinData.phone = this.data.joinPhone.trim();
    }
    if (this.data.joinAddress.trim()) {
      joinData.address = this.data.joinAddress.trim();
    }

    request({
      url: '/car/' + carId + '/join',
      method: 'POST',
      data: joinData
    }).then(function () {
      wx.showToast({ title: '参与成功', icon: 'success' });
      that.setData({ showJoinModal: false });
      that.loadCarDetail(carId);
    }).catch(function (err) {
      var msg = '参与失败';
      if (err && err.data && err.data.message) {
        msg = err.data.message;
      }
      wx.showToast({ title: msg, icon: 'none' });
    });
  },

  onClaimPreference: function () {
    var car = this.data.car;
    var versions = [];
    var cards = [];

    try {
      if (car.goods && car.goods.versions) {
        versions = typeof car.goods.versions === 'string' ? JSON.parse(car.goods.versions) : car.goods.versions;
      }
    } catch (e) { }

    try {
      if (car.goods && car.goods.cards) {
        cards = typeof car.goods.cards === 'string' ? JSON.parse(car.goods.cards) : car.goods.cards;
      }
    } catch (e) { }

    // 添加 _sel 标记（WXML不支持indexOf）
    versions = (versions || []).map(function(v) { return {_sel: false, v: v}; });
    cards = (cards || []).map(function(c) { return {_sel: false, c: c}; });

    this.setData({
      showClaimModal: true,
      versionList: versions,
      cardList: cards
    });
  },

  onVersionSelect: function (e) {
    var idx = e.currentTarget.dataset.index;
    var list = this.data.versionList.slice();
    list[idx]._sel = !list[idx]._sel;
    this.setData({ versionList: list });
  },

  onCardSelect: function (e) {
    var idx = e.currentTarget.dataset.index;
    var list = this.data.cardList.slice();
    list[idx]._sel = !list[idx]._sel;
    this.setData({ cardList: list });
  },

  onClaimSubmit: function () {
    var that = this;
    var carId = this.data.carId;

    // 从带标记的列表中提取选中的值
    var selVersions = this.data.versionList.filter(function(v) { return v._sel; }).map(function(v) { return v.v; });
    var selCards = this.data.cardList.filter(function(c) { return c._sel; }).map(function(c) { return c.c; });

    request({
      url: '/car/' + carId + '/claim',
      method: 'POST',
      data: {
        claimedVersion: selVersions.length > 0 ? selVersions[0] : '',
        claimedCard: selCards.length > 0 ? selCards[0] : ''
      }
    }).then(function () {
      wx.showToast({ title: '认领成功', icon: 'success' });
      that.setData({ showClaimModal: false });
      that.loadCarDetail(carId);
    }).catch(function (err) {
      wx.showToast({ title: '认领失败', icon: 'none' });
    });
  },

  onClaimCancel: function () {
    this.setData({ showClaimModal: false });
  },

  onPay: function () {
    var that = this;
    var car = this.data.car;
    wx.showModal({
      title: '确认付款',
      content: '需支付 ¥' + (car.pricePer || car.price_per || 0) + '，请向车主转账后上传付款凭证',
      confirmText: '去付款',
      success: function (res) {
        if (res.confirm) {
          that.onUploadEvidence();
        }
      }
    });
  },

  onUploadEvidence: function () {
    var that = this;
    wx.chooseImage({
      count: 6,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: function (res) {
        var tempFilePaths = res.tempFilePaths;
        that.doUploadEvidence(tempFilePaths);
      }
    });
  },

  doUploadEvidence: function (filePaths) {
    var that = this;
    var carId = this.data.carId;
    wx.showLoading({ title: '上传中...' });

    var uploadPromises = filePaths.map(function (path) {
      return uploadFile({
        url: '/upload/evidence',
        filePath: path,
        name: 'file',
        formData: {
          carId: carId,
          type: '0'
        }
      });
    });

    Promise.all(uploadPromises).then(function (results) {
      var urls = results.map(function (r) { return r.url || r; });
      return request({
        url: '/settle/evidence',
        method: 'POST',
        data: {
          carMemberId: that.data.myMemberId,
          carId: carId,
          type: 0,
          imageUrl: urls.length === 1 ? urls[0] : JSON.stringify(urls),
          remark: ''
        },
        loading: false
      });
    }).then(function () {
      wx.hideLoading();
      wx.showToast({ title: '凭证上传成功', icon: 'success' });
      that.loadCarDetail(carId);
    }).catch(function (err) {
      wx.hideLoading();
      wx.showToast({ title: '上传失败', icon: 'none' });
    });
  },

  goToLogistics: function () {
    var autoShip = this.data.isOwner && this.data.car.status == 2 ? '&autoShip=1' : '';
    wx.navigateTo({
      url: '/pages/logistics/logistics?carId=' + this.data.carId + autoShip
    });
  },

  onCloseCar: function () {
    var that = this;
    wx.showModal({
      title: '确认截止报名',
      content: '截止后其他用户将无法再加入，确认截止？',
      success: function (res) {
        if (res.confirm) {
          request({
            url: '/car/' + that.data.carId + '/close',
            method: 'POST'
          }).then(function () {
            wx.showToast({ title: '已截止报名', icon: 'success' });
            that.loadCarDetail(that.data.carId);
          }).catch(function () {
            wx.showToast({ title: '操作失败', icon: 'none' });
          });
        }
      }
    });
  },

  goToSettle: function () {
    wx.navigateTo({
      url: '/pages/settle/settle?carId=' + this.data.carId
    });
  },

  onPreviewEvidence: function (e) {
    var url = e.currentTarget.dataset.url;
    var urls = [url];
    try {
      var parsed = JSON.parse(url);
      if (Array.isArray(parsed)) {
        urls = parsed;
      }
    } catch (err) {}
    wx.previewImage({ current: urls[0], urls: urls });
  },

  /* ========== 收藏功能 ========== */

  checkFavorite: function (carId) {
    var that = this;
    var token = wx.getStorageSync('token');
    if (!token) return;
    request({
      url: '/favorite/check/' + carId,
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      that.setData({ isFavorited: !!result });
    }).catch(function () {});
  },

  onToggleFavorite: function () {
    var that = this;
    var token = wx.getStorageSync('token');
    if (!token) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }

    var carId = this.data.carId;
    var isFav = this.data.isFavorited;

    if (isFav) {
      request({
        url: '/favorite/' + carId,
        method: 'DELETE',
        loading: false,
        showError: false
      }).then(function () {
        that.setData({ isFavorited: false });
        wx.showToast({ title: '已取消收藏', icon: 'success' });
      }).catch(function () {});
    } else {
      request({
        url: '/favorite/' + carId,
        method: 'POST',
        loading: false,
        showError: false
      }).then(function () {
        that.setData({ isFavorited: true });
        wx.showToast({ title: '收藏成功', icon: 'success' });
      }).catch(function () {});
    }
  },

  /* ========== 评价功能 ========== */

  loadReviews: function (carId, callback) {
    var that = this;
    request({
      url: '/review/car/' + carId,
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      var list = Array.isArray(result) ? result : [];
      if (callback) callback(list);
      else that.setData({ reviews: list });
    }).catch(function () {
      if (callback) callback([]);
      else that.setData({ reviews: [] });
    });
  },

  formatReviewDate: function (dateStr) {
    if (!dateStr) return '';
    try { return dateStr.substring(0, 10); }
    catch (e) { return ''; }
  },

  onShowReviewModal: function () {
    var token = wx.getStorageSync('token');
    if (!token) { wx.navigateTo({ url: '/pages/auth/auth' }); return; }
    this.setData({
      showReviewModal: true,
      reviewRating: 5,
      reviewContent: ''
    });
  },

  onCloseReviewModal: function () {
    this.setData({ showReviewModal: false });
  },

  onReviewRatingTap: function (e) {
    this.setData({ reviewRating: parseInt(e.currentTarget.dataset.rating) });
  },

  onReviewContentInput: function (e) {
    this.setData({ reviewContent: e.detail.value });
  },

  onSubmitReview: function () {
    var that = this;
    var car = this.data.car;
    // 找被评价人（如果是车主评价，评价第一个非车主的成员；如果是成员，评价车主）
    var toUserId = car.userId;
    var type = 0;
    // 简单：评价车主
    request({
      url: '/review',
      method: 'POST',
      data: {
        carId: this.data.carId,
        toUserId: toUserId,
        type: type,
        rating: this.data.reviewRating,
        content: this.data.reviewContent.trim()
      },
      showError: false
    }).then(function () {
      wx.showToast({ title: '评价成功 🌟', icon: 'success' });
      that.setData({ showReviewModal: false });
      that.loadReviews(that.data.carId);
    }).catch(function (err) {
      wx.showToast({ title: err.message || '评价失败', icon: 'none' });
    });
  },

  onShareAppMessage: function () {
    return {
      title: this.data.car.title || '拼车协作',
      path: '/pages/detail/detail?id=' + this.data.carId
    };
  }
});
