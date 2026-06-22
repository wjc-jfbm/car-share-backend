// app.js
const { request } = require('./utils/request');

App({
  onLaunch() {
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 检查 token
    const token = wx.getStorageSync('token');
    if (!token) {
      wx.login({
        success: (res) => {
          if (res.code) {
            wx.setStorageSync('wxLoginCode', res.code);
          }
        }
      });
    }

    // 监听网络状态
    this.globalData.isOnline = true;
    wx.onNetworkStatusChange((res) => {
      this.globalData.isOnline = res.isConnected;
      // 触发全局事件
      var pages = getCurrentPages();
      var page = pages[pages.length - 1];
      if (page && page.onNetworkChange) {
        page.onNetworkChange(res.isConnected);
      }
    });
  },
  globalData: {
    userInfo: null,
    isOnline: true
  }
})
