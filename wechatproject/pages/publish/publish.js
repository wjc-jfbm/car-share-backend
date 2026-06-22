var { request, uploadFile, getFullImageUrl, loadImage } = require('../../utils/request');

function getToday() {
  var d = new Date();
  var year = d.getFullYear();
  var month = (d.getMonth() + 1).toString().padStart(2, '0');
  var day = d.getDate().toString().padStart(2, '0');
  return year + '-' + month + '-' + day;
}

Page({
  data: {
    formData: {
      title: '',
      goodsName: '',
      goodsImage: '',
      description: '',
      versions: '',
      cards: '',
      priceTotal: '',
      totalCount: '',
      distributionType: 0,
      deadline: ''
    },
    calculatedPrice: '0.00',
    today: getToday(),
    submitting: false,
    // 模板相关
    showTemplates: false,
    templateList: [],
    saveAsTemplate: false,
    templateName: '',
    templateId: '',  // 编辑模板时使用
    editId: ''       // 编辑拼车时使用
  },

  onLoad: function (options) {
    this.loadTemplates();

    // 编辑模式：加载已有拼车数据
    if (options && options.editId) {
      this.setData({ editId: options.editId, templateId: options.editId });
      this.loadCarForEdit(options.editId);
      return;
    }

    // 恢复草稿
    var draft = wx.getStorageSync('publishDraft');
    if (draft && draft.title && draft._saveTime) {
      var that = this;
      wx.showModal({
        title: '恢复草稿',
        content: '检测到上次未发布的草稿（' + draft._saveTime + '），是否恢复？',
        success: function (res) {
          if (res.confirm) {
            delete draft._saveTime;
            that.setData({ formData: draft });
            that.calculatePrice();
          } else {
            wx.removeStorageSync('publishDraft');
          }
        }
      });
    }
  },

  // 自动保存草稿
  _autoSaveDraft: function () {
    var data = this.data.formData;
    if (data.title || data.goodsName) {
      data._saveTime = new Date().toLocaleString('zh-CN');
      wx.setStorageSync('publishDraft', data);
    }
  },

  /* ========== 编辑功能 ========== */

  loadCarForEdit: function (carId) {
    var that = this;
    request({
      url: '/car/detail/' + carId,
      method: 'GET',
      loading: true,
      showError: false
    }).then(function (result) {
      if (!result) {
        wx.showToast({ title: '拼车不存在', icon: 'none' });
        return;
      }
      var versions = '';
      var cards = '';
      if (result.goods) {
        try {
          var v = typeof result.goods.versions === 'string' ? JSON.parse(result.goods.versions) : (result.goods.versions || []);
          versions = Array.isArray(v) ? v.join('，') : '';
        } catch (e) {}
        try {
          var c = typeof result.goods.cards === 'string' ? JSON.parse(result.goods.cards) : (result.goods.cards || []);
          cards = Array.isArray(c) ? c.join('，') : '';
        } catch (e) {}
      }
      that.setData({
        formData: {
          title: result.title || '',
          goodsName: result.goodsName || result.goods_name || '',
          goodsImage: result.goodsImage || result.goods_image || '',
          description: result.description || '',
          versions: versions,
          cards: cards,
          priceTotal: result.priceTotal ? String(result.priceTotal) : '',
          totalCount: result.totalCount ? String(result.totalCount) : '',
          distributionType: result.distributionType || 0,
          deadline: result.deadline ? result.deadline.substring(0, 10) : ''
        },
        calculatedPrice: result.pricePer || '0.00'
      });
    }).catch(function () {
      wx.showToast({ title: '加载失败', icon: 'none' });
    });
  },

  /* ========== 模板功能 ========== */

  loadTemplates: function () {
    var that = this;
    request({
      url: '/template/my',
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      if (result && result.list) {
        that.setData({ templateList: result.list });
      }
    }).catch(function () {});
  },

  onToggleTemplates: function () {
    this.setData({ showTemplates: !this.data.showTemplates });
  },

  onSelectTemplate: function (e) {
    var tpl = e.currentTarget.dataset.template;
    if (!tpl) return;

    // 解析模板中的数据填充表单
    var versions = '';
    var cards = '';
    try {
      if (tpl.versions) {
        var v = typeof tpl.versions === 'string' ? JSON.parse(tpl.versions) : tpl.versions;
        versions = Array.isArray(v) ? v.join('，') : v;
      }
    } catch (e) {}
    try {
      if (tpl.cards) {
        var c = typeof tpl.cards === 'string' ? JSON.parse(tpl.cards) : tpl.cards;
        cards = Array.isArray(c) ? c.join('，') : c;
      }
    } catch (e) {}

    this.setData({
      formData: {
        title: tpl.name || '',
        goodsName: tpl.goodsName || '',
        goodsImage: tpl.goodsImage || '',
        description: tpl.description || '',
        versions: versions,
        cards: cards,
        priceTotal: tpl.priceTotal ? String(tpl.priceTotal) : '',
        totalCount: tpl.totalCount ? String(tpl.totalCount) : '',
        distributionType: tpl.distributionType || 0,
        deadline: ''
      },
      templateId: tpl.id,
      showTemplates: false,
      saveAsTemplate: false
    });
    this.calculatePrice();
    wx.showToast({ title: '已加载模板', icon: 'success' });
  },

  onToggleSaveTemplate: function () {
    this.setData({ saveAsTemplate: !this.data.saveAsTemplate, templateName: '' });
  },

  onTemplateNameInput: function (e) {
    this.setData({ templateName: e.detail.value });
  },

  /* ========== 表单操作 ========== */

  onTitleInput: function (e) {
    this.setData({ 'formData.title': e.detail.value });
    this._autoSaveDraft();
  },

  onGoodsNameInput: function (e) {
    this.setData({ 'formData.goodsName': e.detail.value });
    this._autoSaveDraft();
  },

  onDescInput: function (e) {
    this.setData({ 'formData.description': e.detail.value });
    this._autoSaveDraft();
  },

  onPriceInput: function (e) {
    this.setData({ 'formData.priceTotal': e.detail.value });
    this.calculatePrice();
    this._autoSaveDraft();
  },

  onCountInput: function (e) {
    this.setData({ 'formData.totalCount': e.detail.value });
    this.calculatePrice();
    this._autoSaveDraft();
  },

  calculatePrice: function () {
    var p = parseFloat(this.data.formData.priceTotal) || 0;
    var c = parseInt(this.data.formData.totalCount) || 1;
    if (c < 1) c = 1;
    this.setData({
      calculatedPrice: (p / c).toFixed(2)
    });
  },

  onDistributionChange: function (e) {
    this.setData({
      'formData.distributionType': parseInt(e.detail.value)
    });
  },

  onDateChange: function (e) {
    this.setData({
      'formData.deadline': e.detail.value
    });
  },

  onChooseGoodsImage: function () {
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function (res) {
        var tempPath = res.tempFiles[0].tempFilePath;
        wx.showLoading({ title: '上传中...' });
        uploadFile({
          url: '/upload',
          filePath: tempPath,
          name: 'file'
        }).then(function (result) {
          wx.hideLoading();
          var rawUrl = result.url || result;
          loadImage(rawUrl).then(function (localPath) {
            that.setData({ 'formData.goodsImage': rawUrl, 'formData.goodsImageFull': localPath });
          });
        }).catch(function () {
          wx.hideLoading();
          wx.showToast({ title: '上传失败', icon: 'none' });
        });
      }
    });
  },

  onRemoveGoodsImage: function () {
    this.setData({ 'formData.goodsImage': '' });
  },

  /* ========== 提交 ========== */

  onSubmit: function (e) {
    if (this.data.submitting) return;

    var formData = e.detail.value;

    if (!formData.title || !formData.title.trim()) {
      wx.showToast({ title: '请填写拼车标题', icon: 'none' });
      return;
    }
    if (!formData.goodsName || !formData.goodsName.trim()) {
      wx.showToast({ title: '请填写商品名称', icon: 'none' });
      return;
    }
    if (!formData.priceTotal || parseFloat(formData.priceTotal) <= 0) {
      wx.showToast({ title: '请输入有效的总价', icon: 'none' });
      return;
    }
    if (!formData.totalCount || parseInt(formData.totalCount) < 2) {
      wx.showToast({ title: '总人数至少2人', icon: 'none' });
      return;
    }

    var priceTotal = parseFloat(formData.priceTotal);
    var totalCount = parseInt(formData.totalCount);
    var pricePer = parseFloat((priceTotal / totalCount).toFixed(2));

    var versionsList = [];
    if (formData.versions && formData.versions.trim()) {
      versionsList = formData.versions.split(/[,，]/).map(function (v) { return v.trim(); }).filter(function (v) { return v; });
    }
    var cardsList = [];
    if (formData.cards && formData.cards.trim()) {
      cardsList = formData.cards.split(/[,，]/).map(function (v) { return v.trim(); }).filter(function (v) { return v; });
    }

    var submitData = {
      title: formData.title.trim(),
      goodsName: formData.goodsName.trim(),
      description: (formData.description || '').trim(),
      priceTotal: priceTotal,
      totalCount: totalCount,
      pricePer: pricePer,
      distributionType: parseInt(formData.distributionType) || 0,
      goodsImage: this.data.formData.goodsImage || ''
    };

    var extraData = {};
    if (versionsList.length > 0) {
      extraData.versions = versionsList;
    }
    if (cardsList.length > 0) {
      extraData.cards = cardsList;
    }
    if (Object.keys(extraData).length > 0) {
      submitData.tags = JSON.stringify(extraData);
    }

    if (this.data.formData.deadline) {
      submitData.deadline = this.data.formData.deadline + ' 23:59:59';
    }

    var that = this;
    this.setData({ submitting: true });

    // 编辑模式走 PUT，否则 POST
    var method = that.data.editId ? 'PUT' : 'POST';
    var url = that.data.editId ? '/car/' + that.data.editId : '/car';

    request({
      url: url,
      method: method,
      data: submitData
    }).then(function (carResult) {
      // 保存为模板
      if (that.data.saveAsTemplate && that.data.templateName.trim()) {
        var tplData = {
          name: that.data.templateName.trim(),
          goodsName: submitData.goodsName,
          goodsImage: submitData.goodsImage,
          description: submitData.description,
          totalCount: submitData.totalCount,
          priceTotal: submitData.priceTotal,
          pricePer: submitData.pricePer,
          distributionType: submitData.distributionType,
          versions: JSON.stringify(versionsList),
          cards: JSON.stringify(cardsList)
        };
        request({
          url: '/template/save',
          method: 'POST',
          data: tplData,
          loading: false,
          showError: false
        }).catch(function () {});
      }

      // 清除草稿
      wx.removeStorageSync('publishDraft');
      that.setData({ submitting: false });
      wx.showToast({ title: '发布成功', icon: 'success' });
      setTimeout(function () {
        wx.navigateBack();
      }, 1500);
    }).catch(function (err) {
      that.setData({ submitting: false });
      wx.showToast({ title: err.message || '发布失败，请重试', icon: 'none' });
    });
  }
});
