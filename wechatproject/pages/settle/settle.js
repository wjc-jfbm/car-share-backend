const { request, uploadFile, getFullImageUrl, loadImage, loadImages, baseUrl, serverUrl } = require('../../utils/request');

function toRawPath(url) {
  if (!url) return '';
  if (url.startsWith(baseUrl)) {
    return url.replace(baseUrl, '/api');
  }
  if (url.startsWith(serverUrl)) {
    return url.replace(serverUrl, '');
  }
  return url;
}

Page({
  data: {
    carId: '',
    car: {},
    settleInfo: null,
    evidenceList: [],
    evidenceRawList: [],
    evidenceUrlList: [],
    isSubmitting: false,
    isOwner: false,
    carSettled: false,
    showReviewModal: false,
    reviewingMember: null,
    reviewStatus: 1,
    rejectReason: '',
    showPreviewModal: false,
    previewImages: [],
    previewCurrent: 0,
    evidenceStatusMap: {
      0: { text: '待审核', color: '#FF9800' },
      1: { text: '已通过', color: '#4CAF50' },
      2: { text: '已驳回', color: '#F44336' }
    },
    payStatusMap: {
      0: { text: '未付款', color: '#999' },
      1: { text: '已付款', color: '#4CAF50' }
    }
  },

  onLoad(options) {
    if (options && options.carId) {
      this.setData({ carId: options.carId });
      this.loadSettleInfo(options.carId);
    }
  },

  onShow() {
    if (this.data.carId) {
      this.loadSettleInfo(this.data.carId);
    }
  },

  async loadSettleInfo(carId) {
    try {
      var result = await request({
        url: '/settle/' + carId,
        method: 'GET'
      });

      var userId = wx.getStorageSync('userId');
      var car = result.car || {};
      var isOwner = result.is_owner === true || result.is_owner === 'true';
      if (!isOwner && car.userId && userId) {
        isOwner = String(car.userId) === String(userId);
      }
      if (!isOwner && car.user_id && userId) {
        isOwner = String(car.user_id) === String(userId);
      }

      var carStatus = parseInt(car.status);
      if (isNaN(carStatus)) carStatus = 0;
      var carSettled = (carStatus === 2 || car.status === '2' || car.status == 2);

      var members = result.members || [];

      var evidenceList = members.filter(function (m) {
        return m.evidenceUrl && m.evidenceUrl.length > 0;
      });

      for (var i = 0; i < members.length; i++) {
        var m = members[i];
        var mNickname = m.nickname || '';
        if (mNickname) {
          m.avatarLetter = mNickname.charAt(0);
        }
        if (m.evidenceUrl) {
          try {
            var parsed = JSON.parse(m.evidenceUrl);
            if (Array.isArray(parsed)) {
              m.evidenceRawList = parsed.slice();
              m.evidenceImageList = parsed.map(function(u) { return getFullImageUrl(u); });
            } else {
              m.evidenceRawList = [m.evidenceUrl];
              m.evidenceImageList = [getFullImageUrl(m.evidenceUrl)];
            }
          } catch (e) {
            m.evidenceRawList = [m.evidenceUrl];
            m.evidenceImageList = [getFullImageUrl(m.evidenceUrl)];
          }
        } else {
          m.evidenceRawList = [];
          m.evidenceImageList = [];
        }
      }

      var allImgPromises = [];
      for (var j = 0; j < members.length; j++) {
        var member = members[j];
        if (member.evidenceRawList && member.evidenceRawList.length > 0) {
          (function (mem) {
            allImgPromises.push(
              loadImages(mem.evidenceRawList).then(function (localPaths) {
                mem.evidenceImageLocalList = localPaths;
              })
            );
          })(member);
        }
      }

      var that = this;
      Promise.all(allImgPromises).then(function () {
        that.setData({
          settleInfo: result,
          car: car,
          isOwner: isOwner,
          evidenceList: evidenceList,
          carSettled: carSettled
        });
      });
    } catch (err) {
      wx.showToast({ title: '加载失败', icon: 'none' });
    }
  },

  chooseEvidence() {
    var that = this;
    var currentRawList = this.data.evidenceRawList;
    var currentDisplayList = this.data.evidenceUrlList;
    if (currentDisplayList.length >= 6) {
      wx.showToast({ title: '最多上传6张凭证', icon: 'none' });
      return;
    }

    wx.chooseMedia({
      count: 6 - currentDisplayList.length,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: async function (res) {
        wx.showLoading({ title: '上传中...' });
        var newRawUrls = [];
        var newFullUrls = [];
        for (var i = 0; i < res.tempFiles.length; i++) {
          try {
            var uploadResult = await uploadFile({
              url: '/upload/evidence',
              filePath: res.tempFiles[i].tempFilePath,
              name: 'file'
            });
            var rawUrl = uploadResult.url || uploadResult;
            newRawUrls.push(rawUrl);
            newFullUrls.push(getFullImageUrl(rawUrl));
          } catch (err) {
            console.error('上传失败:', err);
          }
        }
        wx.hideLoading();
        if (newRawUrls.length > 0) {
          loadImages(newRawUrls).then(function (localPaths) {
            that.setData({
              evidenceRawList: currentRawList.concat(newRawUrls),
              evidenceUrlList: currentDisplayList.concat(localPaths)
            });
          });
          wx.showToast({ title: '上传成功', icon: 'success' });
        }
      }
    });
  },

  removeEvidence(e) {
    var idx = e.currentTarget.dataset.index;
    var rawList = this.data.evidenceRawList.slice();
    var displayList = this.data.evidenceUrlList.slice();
    rawList.splice(idx, 1);
    displayList.splice(idx, 1);
    this.setData({ evidenceRawList: rawList, evidenceUrlList: displayList });
  },

  previewEvidence(e) {
    var idx = e.currentTarget.dataset.index;
    this.setData({
      showPreviewModal: true,
      previewImages: this.data.evidenceUrlList,
      previewCurrent: idx
    });
  },

  closePreview() {
    this.setData({ showPreviewModal: false });
  },

  async onSubmit() {
    if (this.data.evidenceRawList.length === 0) {
      wx.showToast({ title: '请先上传凭证', icon: 'none' });
      return;
    }

    this.setData({ isSubmitting: true });
    try {
      // 查找当前用户的成员记录
      var members = this.data.settleInfo && this.data.settleInfo.members || [];
      var userId = wx.getStorageSync('userId');
      var myMemberId = this.data.myMemberId;
      if (!myMemberId) {
        for (var i = 0; i < members.length; i++) {
          if (String(members[i].userId) === String(userId)) {
            myMemberId = members[i].id;
            break;
          }
        }
      }

      await request({
        url: '/settle/evidence',
        method: 'POST',
        data: {
          carMemberId: myMemberId,
          carId: this.data.carId,
          type: 0,
          imageUrl: this.data.evidenceRawList.length === 1 ? this.data.evidenceRawList[0] : JSON.stringify(this.data.evidenceRawList),
          remark: ''
        }
      });
      wx.showToast({ title: '提交成功', icon: 'success' });
      this.setData({ evidenceRawList: [], evidenceUrlList: [] });
      this.loadSettleInfo(this.data.carId);
    } catch (err) {
      wx.showToast({ title: '提交失败', icon: 'none' });
    } finally {
      this.setData({ isSubmitting: false });
    }
  },

  onReviewEvidence(e) {
    var member = e.currentTarget.dataset.member;
    this.setData({
      showReviewModal: true,
      reviewingMember: member,
      reviewStatus: 1,
      rejectReason: ''
    });
  },

  onReviewStatusChange(e) {
    this.setData({ reviewStatus: parseInt(e.currentTarget.dataset.status) });
  },

  onRejectReasonInput(e) {
    this.setData({ rejectReason: e.detail.value });
  },

  async onSubmitReview() {
    var that = this;
    var member = this.data.reviewingMember;
    var status = this.data.reviewStatus;

    if (status === 2 && !this.data.rejectReason.trim()) {
      wx.showToast({ title: '请填写驳回原因', icon: 'none' });
      return;
    }

    try {
      // 查找该成员的待审核凭证ID
      var evidences = await request({
        url: '/settle/' + that.data.carId + '/evidences',
        method: 'GET'
      });
      var evidenceId = null;
      if (evidences && evidences.length > 0) {
        for (var i = 0; i < evidences.length; i++) {
          if (evidences[i].carMemberId == member.id || evidences[i].car_member_id == member.id) {
            evidenceId = evidences[i].id;
            break;
          }
        }
      }

      if (!evidenceId) {
        wx.showToast({ title: '未找到凭证记录', icon: 'none' });
        return;
      }

      await request({
        url: '/settle/evidence/' + evidenceId + '/review',
        method: 'POST',
        data: {
          status: status,
          reject_reason: status === 2 ? that.data.rejectReason : ''
        }
      });
      wx.showToast({ title: '审核完成', icon: 'success' });
      that.setData({ showReviewModal: false });
      that.loadSettleInfo(that.data.carId);
    } catch (err) {
      wx.showToast({ title: '审核失败', icon: 'none' });
    }
  },

  onCancelReview() {
    this.setData({ showReviewModal: false });
  },

  onPreviewMemberEvidence(e) {
    var member = e.currentTarget.dataset.member;
    if (!member || !member.evidenceImageList || member.evidenceImageList.length === 0) {
      return;
    }
    wx.previewImage({
      current: member.evidenceImageList[0],
      urls: member.evidenceImageList
    });
  },

  async onConfirmSettle() {
    var that = this;
    var members = this.data.settleInfo && this.data.settleInfo.members || [];
    var unpaid = members.filter(function (m) { return m.isOwner !== 1 && m.payStatus !== 1; });

    if (unpaid.length > 0) {
      wx.showModal({
        title: '无法结算',
        content: '还有 ' + unpaid.length + ' 位成员未完成付款审核，请先审核所有付款凭证',
        showCancel: false
      });
      return;
    }

    wx.showModal({
      title: '确认结算',
      content: '确认所有成员已完成付款？结算后将进入物流发货阶段。',
      success: async function (res) {
        if (res.confirm) {
          try {
            await request({
              url: '/settle/' + that.data.carId + '/confirm',
              method: 'POST'
            });
            wx.showToast({ title: '结算确认成功', icon: 'success' });
            that.setData({ carSettled: true, 'car.status': 2 });
            setTimeout(function() {
              that.loadSettleInfo(that.data.carId);
            }, 500);
          } catch (err) {
            wx.showToast({ title: err.message || '确认失败，请确保所有成员已完成付款', icon: 'none', duration: 3000 });
          }
        }
      }
    });
  },

  goToShip() {
    var autoShip = this.data.isOwner ? '&autoShip=1' : '';
    wx.navigateTo({
      url: '/pages/logistics/logistics?carId=' + this.data.carId + autoShip
    });
  }
});
