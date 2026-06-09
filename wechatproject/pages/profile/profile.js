const { request, uploadFile, getFullImageUrl, loadImage } = require('../../utils/request');

Page({
  data: {
    user: {
      nickname: '点击登录',
      avatar: '',
      creditScore: 0,
      creditLevel: 1,
      totalTransactions: 0,
      successTransactions: 0,
      phone: '',
      realName: ''
    },
    preference: {
      preferredArtists: '',
      preferredVersions: '',
      preferredCards: ''
    },
    isLoggedIn: false,
    menuList: [
      { icon: '🚗', text: '我发起的', color: '#FF6B9D', url: '/pages/order/order?tab=created' },
      { icon: '🙌', text: '我参与的', color: '#A78BFA', url: '/pages/order/order?tab=joined' },
      { icon: '📋', text: '我的模板', color: '#60A5FA', url: '', action: 'templates' },
      { icon: '🔔', text: '消息通知', color: '#FBBF24', url: '/pages/notification/notification' },
      { icon: '📊', text: '数据统计', color: '#34D399', url: '/pages/statistics/statistics' },
      { icon: '⭐', text: '信用评价', color: '#FBBF24', action: 'credit' },
      { icon: '❤️', text: '我的收藏', color: '#F87171', url: '', action: 'favorites' },
      { icon: 'ℹ️', text: '关于我们', color: '#9CA3AF', action: 'about' }
    ],
    unreadCount: 0
  },

  onShow() {
    this.loadUserInfo();
    this.loadUnreadCount();
    this.loadPreference();
  },

  async loadUserInfo() {
    var token = wx.getStorageSync('token');
    if (!token) {
      this.setData({ isLoggedIn: false });
      return;
    }

    try {
      var result = await request({
        url: '/user/profile',
        method: 'GET',
        loading: false
      });
      if (result.nickname) {
        result.avatarLetter = result.nickname.charAt(0);
      }
      if (result.avatar) {
        result.avatarFull = getFullImageUrl(result.avatar);
        var localPath = await loadImage(result.avatar);
        result.avatarLocal = localPath;
      }
      this.setData({
        user: result,
        isLoggedIn: true
      });
    } catch (err) {
      this.setData({ isLoggedIn: false });
    }
  },

  async loadPreference() {
    var token = wx.getStorageSync('token');
    if (!token) return;
    try {
      var result = await request({
        url: '/user/preference',
        method: 'GET',
        loading: false
      });
      if (result) {
        this.setData({ preference: result });
      }
    } catch (err) {}
  },

  async loadUnreadCount() {
    var token = wx.getStorageSync('token');
    if (!token) return;
    try {
      var result = await request({
        url: '/notification/unread-count',
        method: 'GET',
        loading: false
      });
      var count = result.unreadCount || 0;
      this.setData({ unreadCount: count });
      if (count > 0) {
        wx.setTabBarBadge({ index: 2, text: String(count) });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    } catch (err) {}
  },

  onLogin() {
    if (this.data.isLoggedIn) return;
    wx.navigateTo({ url: '/pages/auth/auth' });
  },

  onAvatarTap(e) {
    if (!this.data.isLoggedIn) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function (res) {
        var tempFilePath = res.tempFiles[0].tempFilePath;
        that.uploadAvatar(tempFilePath);
      }
    });
  },

  async uploadAvatar(filePath) {
    try {
      var result = await uploadFile({
        url: '/upload',
        filePath: filePath,
        name: 'file'
      });
      var avatarUrl = result;
      if (typeof result === 'object' && result.url) {
        avatarUrl = result.url;
      }
      await request({
        url: '/user/profile',
        method: 'PUT',
        data: { avatar: avatarUrl }
      });
      this.setData({ 'user.avatar': avatarUrl, 'user.avatarFull': getFullImageUrl(avatarUrl) });
      wx.showToast({ title: '头像更新成功', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: '头像更新失败', icon: 'none' });
    }
  },

  onEditNickname() {
    var that = this;
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入新昵称',
      content: that.data.user.nickname || '',
      success: function (res) {
        if (res.confirm && res.content && res.content.trim()) {
          that.updateProfile('nickname', res.content.trim());
        }
      }
    });
  },

  onEditPhone() {
    var that = this;
    wx.showModal({
      title: '修改手机号',
      editable: true,
      placeholderText: '请输入手机号',
      content: that.data.user.phone || '',
      success: function (res) {
        if (res.confirm && res.content && res.content.trim()) {
          var phone = res.content.trim();
          if (!/^1[3-9]\d{9}$/.test(phone)) {
            wx.showToast({ title: '手机号格式不正确', icon: 'none' });
            return;
          }
          that.updateProfile('phone', phone);
        }
      }
    });
  },

  onEditRealName() {
    var that = this;
    wx.showModal({
      title: '修改真实姓名',
      editable: true,
      placeholderText: '请输入真实姓名',
      content: that.data.user.realName || '',
      success: function (res) {
        if (res.confirm && res.content && res.content.trim()) {
          that.updateProfile('realName', res.content.trim());
        }
      }
    });
  },

  async updateProfile(field, value) {
    try {
      var data = {};
      data[field] = value;
      await request({
        url: '/user/profile',
        method: 'PUT',
        data: data
      });
      var updateData = {};
      updateData['user.' + field] = value;
      this.setData(updateData);
      wx.showToast({ title: '修改成功', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: '修改失败', icon: 'none' });
    }
  },

  onEditPreference(e) {
    var field = e.currentTarget.dataset.field;
    var labels = {
      preferredArtists: '偏好艺人',
      preferredVersions: '偏好版本',
      preferredCards: '偏好小卡'
    };
    var label = labels[field] || '偏好设置';
    var currentValue = this.data.preference[field] || '';
    var that = this;
    wx.showModal({
      title: '修改' + label,
      editable: true,
      placeholderText: '多个偏好用逗号分隔，如：成员A,成员B',
      content: currentValue,
      success: function (res) {
        if (res.confirm) {
          var newValue = res.content ? res.content.trim() : '';
          that.updatePreference(field, newValue);
        }
      }
    });
  },

  async updatePreference(field, value) {
    try {
      var pref = Object.assign({}, this.data.preference);
      pref[field] = value;
      await request({
        url: '/user/preference',
        method: 'POST',
        data: pref
      });
      var updateData = {};
      updateData['preference.' + field] = value;
      this.setData(updateData);
      wx.showToast({ title: '偏好已保存', icon: 'success' });
    } catch (err) {
      wx.showToast({ title: '保存失败', icon: 'none' });
    }
  },

  onMenuTap(e) {
    var item = e.currentTarget.dataset.item;
    if (!this.data.isLoggedIn) {
      wx.navigateTo({ url: '/pages/auth/auth' });
      return;
    }

    if (item.url) {
      wx.navigateTo({ url: item.url });
    } else if (item.action === 'templates') {
      this.showTemplates();
    } else if (item.action === 'favorites') {
      wx.navigateTo({ url: '/pages/favorites/favorites' });
    } else if (item.action === 'credit') {
      wx.navigateTo({ url: '/pages/statistics/statistics' });
    } else if (item.action === 'about') {
      wx.showModal({
        title: '关于我们',
        content: '🎯 拼车协作平台 v2.0\n\n' +
          '📌 面向粉丝团购场景的拼车协作与可信结算系统\n\n' +
          '🚀 核心功能：\n' +
          '发起拼车 · 参与拼车 · AI智能匹配\n' +
          '凭证管理 · 结算确认 · 物流追踪\n' +
          '信用评价 · 数据统计 · 拼车模板\n\n' +
          '🛠 技术架构：\n' +
          '前端：微信小程序 + WeUI\n' +
          '后端：Spring Boot 3.2 + MyBatis-Plus\n' +
          '数据库：MySQL + Redis\n' +
          '算法：AI多维度智能推荐引擎\n\n' +
          '© 2026 拼车协作平台',
        showCancel: false
      });
    }
  },

  showTemplates: function () {
    var that = this;
    request({
      url: '/template/my',
      method: 'GET',
      loading: false,
      showError: false
    }).then(function (result) {
      var list = result.list || [];
      if (list.length === 0) {
        wx.showToast({ title: '暂无模板，发布时可保存', icon: 'none' });
        return;
      }
      var items = list.map(function (t) {
        return t.name + '  ' + t.goodsName + ' · ' + t.totalCount + '人';
      });
      wx.showActionSheet({
        itemList: ['去发布页使用模板', '管理模板（长按删除）'],
        success: function (res) {
          if (res.tapIndex === 0) {
            wx.navigateTo({ url: '/pages/publish/publish' });
          }
        }
      });
    }).catch(function () {});
  },

  onLogout() {
    var that = this;
    wx.showModal({
      title: '提示',
      content: '确定要退出登录吗？',
      success: function (res) {
        if (res.confirm) {
          wx.removeStorageSync('token');
          wx.removeStorageSync('userId');
          that.setData({
            isLoggedIn: false,
            user: {
              nickname: '点击登录',
              creditScore: 0,
              creditLevel: 1
            }
          });
          wx.showToast({ title: '已退出登录', icon: 'success' });
        }
      }
    });
  }
});
