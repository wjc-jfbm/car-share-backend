// app.js
const { request } = require('./utils/request');

App({
  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 检查是否有有效 token
    const token = wx.getStorageSync('token');
    if (!token) {
      // 静默登录：获取 code 但不阻塞启动
      wx.login({
        success: (res) => {
          if (res.code) {
            wx.setStorageSync('wxLoginCode', res.code);
          }
        }
      });
    }
  },
  globalData: {
    userInfo: null
  }
})
