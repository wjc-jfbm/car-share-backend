const { request } = require('../../utils/request');

Page({
  data: {
    notifications: [],
    page: 1,
    pageSize: 10,
    total: 0,
    loading: false,
    hasMore: true,
    unreadCount: 0
  },

  onShow() {
    this.setData({ page: 1, notifications: [], hasMore: true });
    this.loadNotifications();
    this.loadUnreadCount();
  },

  onPullDownRefresh() {
    this.setData({ page: 1, notifications: [], hasMore: true });
    this.loadNotifications().then(function () {
      wx.stopPullDownRefresh();
    });
  },

  onReachBottom() {
    if (this.data.hasMore && !this.data.loading) {
      this.loadNotifications();
    }
  },

  async loadNotifications() {
    if (this.data.loading) return;
    this.setData({ loading: true });

    try {
      var result = await request({
        url: '/notification/list?page=' + this.data.page + '&pageSize=' + this.data.pageSize,
        method: 'GET',
        loading: false
      });

      var newList = result.list || [];
      for (var i = 0; i < newList.length; i++) {
        newList[i].typeLabel = this.getTypeLabel(newList[i].type);
        newList[i].timeLabel = this.formatTime(newList[i].createdAt);
      }
      var notifications = this.data.page === 1 ? newList : this.data.notifications.concat(newList);

      this.setData({
        notifications: notifications,
        total: result.total,
        hasMore: notifications.length < result.total,
        page: this.data.page + 1,
        loading: false
      });
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  async loadUnreadCount() {
    try {
      var result = await request({
        url: '/notification/unread-count',
        method: 'GET',
        loading: false
      });
      this.setData({ unreadCount: result.unreadCount || 0 });

      if (result.unreadCount > 0) {
        wx.setTabBarBadge({ index: 2, text: String(result.unreadCount) });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    } catch (err) {}
  },

  async onNotificationTap(e) {
    var item = e.currentTarget.dataset.item;

    if (item.isRead === 0) {
      try {
        await request({
          url: '/notification/' + item.id + '/read',
          method: 'PUT',
          loading: false
        });

        var notifications = this.data.notifications;
        for (var i = 0; i < notifications.length; i++) {
          if (notifications[i].id === item.id) {
            notifications[i].isRead = 1;
            break;
          }
        }
        this.setData({ notifications: notifications });
        this.loadUnreadCount();
      } catch (err) {}
    }

    if (item.carId) {
      wx.navigateTo({ url: '/pages/detail/detail?id=' + item.carId });
    }
  },

  async onMarkAllRead() {
    try {
      await request({
        url: '/notification/read-all',
        method: 'PUT'
      });

      var notifications = this.data.notifications;
      for (var i = 0; i < notifications.length; i++) {
        notifications[i].isRead = 1;
      }
      this.setData({
        notifications: notifications,
        unreadCount: 0
      });
      wx.removeTabBarBadge({ index: 2 });
      wx.showToast({ title: '全部已读', icon: 'success' });
    } catch (err) {}
  },

  async onDeleteNotification(e) {
    var id = e.currentTarget.dataset.id;
    try {
      await request({
        url: '/notification/' + id,
        method: 'DELETE'
      });

      var notifications = this.data.notifications.filter(function (n) {
        return n.id !== id;
      });
      this.setData({ notifications: notifications });
      this.loadUnreadCount();
      wx.showToast({ title: '已删除', icon: 'success' });
    } catch (err) {}
  },

  getTypeLabel(type) {
    var labels = {
      0: '系统通知',
      1: '成员动态',
      2: '成团通知',
      3: '分配通知',
      4: '凭证审核',
      5: '结算通知',
      6: '超时提醒'
    };
    return labels[type] || '通知';
  },

  formatTime(timeStr) {
    if (!timeStr) return '';
    var date = new Date(timeStr);
    var now = new Date();
    var diff = now - date;
    if (diff < 60000) return '刚刚';
    if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前';
    if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前';
    if (diff < 604800000) return Math.floor(diff / 86400000) + '天前';
    return date.getMonth() + 1 + '月' + date.getDate() + '日';
  }
});
