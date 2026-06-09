const { request } = require('../../utils/request');

Page({
  data: {
    isLoading: false,
    loginMode: 'wechat',
    isRegister: false,
    username: '',
    password: '',
    confirmPassword: '',
    nickname: '',
    showPassword: false
  },

  onModeSwitch: function (e) {
    var mode = e.currentTarget.dataset.mode;
    this.setData({ loginMode: mode, isRegister: false });
  },

  onSwitchRegister: function () {
    this.setData({ isRegister: true });
  },

  onSwitchLogin: function () {
    this.setData({ isRegister: false });
  },

  onUsernameInput: function (e) {
    this.setData({ username: e.detail.value });
  },

  onPasswordInput: function (e) {
    this.setData({ password: e.detail.value });
  },

  onConfirmPasswordInput: function (e) {
    this.setData({ confirmPassword: e.detail.value });
  },

  onNicknameInput: function (e) {
    this.setData({ nickname: e.detail.value });
  },

  onTogglePassword: function () {
    this.setData({ showPassword: !this.data.showPassword });
  },

  async onWechatLogin() {
    var that = this;
    if (this.data.isLoading) return;
    this.setData({ isLoading: true });

    wx.login({
      success: async function (res) {
        if (res.code) {
          try {
            var result = await request({
              url: '/user/login',
              method: 'POST',
              data: { code: res.code },
              loading: false
            });
            that.handleLoginSuccess(result);
          } catch (err) {
            wx.showToast({ title: err.message || '登录失败', icon: 'none' });
            that.setData({ isLoading: false });
          }
        } else {
          wx.showToast({ title: '微信登录失败', icon: 'none' });
          that.setData({ isLoading: false });
        }
      },
      fail: function () {
        wx.showToast({ title: '微信登录不可用', icon: 'none' });
        that.setData({ isLoading: false });
      }
    });
  },

  async onAccountSubmit() {
    var that = this;
    var username = this.data.username;
    var password = this.data.password;

    if (!username || username.trim().length === 0) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (!password || password.length < 6) {
      wx.showToast({ title: '密码长度不能少于6位', icon: 'none' });
      return;
    }

    if (this.data.isRegister) {
      var confirmPassword = this.data.confirmPassword;
      if (password !== confirmPassword) {
        wx.showToast({ title: '两次密码不一致', icon: 'none' });
        return;
      }

      if (this.data.isLoading) return;
      this.setData({ isLoading: true });

      try {
        console.log('注册请求发送:', { username: username.trim(), password: '***', nickname: this.data.nickname.trim() });
        var result = await request({
          url: '/user/register',
          method: 'POST',
          data: {
            username: username.trim(),
            password: password,
            nickname: this.data.nickname.trim()
          }
        });
        console.log('注册成功:', result);
        that.handleLoginSuccess(result);
      } catch (err) {
        console.error('注册失败:', err);
        wx.showToast({ title: err.message || '注册失败', icon: 'none' });
        that.setData({ isLoading: false });
      }
    } else {
      if (this.data.isLoading) return;
      this.setData({ isLoading: true });

      try {
        console.log('登录请求发送:', { username: username.trim(), password: '***' });
        var result = await request({
          url: '/user/phone-login',
          method: 'POST',
          data: {
            username: username.trim(),
            password: password
          }
        });
        console.log('登录成功:', result);
        that.handleLoginSuccess(result);
      } catch (err) {
        console.error('登录失败:', err);
        wx.showToast({ title: err.message || '登录失败', icon: 'none' });
        that.setData({ isLoading: false });
      }
    }
  },

  handleLoginSuccess: function (result) {
    wx.setStorageSync('token', result.token);
    if (result.userId) {
      wx.setStorageSync('userId', result.userId);
    }
    var userInfo = {
      nickname: result.nickname || '',
      userId: result.userId || '',
      phone: result.phone || ''
    };
    wx.setStorageSync('userInfo', JSON.stringify(userInfo));

    var that = this;
    that.setData({ isLoading: false });
    wx.showToast({ title: that.data.isRegister ? '注册成功' : '登录成功', icon: 'success' });
    setTimeout(function () {
      wx.navigateBack();
    }, 1500);
  },

  onBack() {
    wx.navigateBack();
  }
});
