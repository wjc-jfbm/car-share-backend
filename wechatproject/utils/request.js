var config = require('./config');

var baseUrl = config.BASE_URL || 'http://localhost:8081/api';
var serverUrl = config.SERVER_URL || 'http://localhost:8081';

// 真机调试时自动切换为局域网IP
var platform = 'devtools';
try {
  var deviceInfo = wx.getDeviceInfo ? wx.getDeviceInfo() : wx.getSystemInfoSync();
  platform = deviceInfo.platform;
} catch (e) {
  try {
    var sysInfo = wx.getSystemInfoSync();
    platform = sysInfo.platform;
  } catch (e2) {}
}
if (platform !== 'devtools') {
  var deviceIp = config.DEVTOOLS_IP || '192.168.245.125';
  baseUrl = 'http://' + deviceIp + ':8081/api';
  serverUrl = 'http://' + deviceIp + ':8081';
}

var isRedirecting = false;

function handleAuthExpired() {
  if (isRedirecting) return;
  isRedirecting = true;
  wx.removeStorageSync('token');
  wx.removeStorageSync('userId');
  wx.removeStorageSync('userInfo');
  wx.showToast({ title: '登录已过期，请重新登录', icon: 'none' });
  setTimeout(function () {
    wx.navigateTo({ url: '/pages/auth/auth' });
    isRedirecting = false;
  }, 1500);
}

function isAuthError(data) {
  if (!data) return false;
  var code = data.code;
  var msg = data.message || data.msg || '';
  if (code === 401) return true;
  if (msg === '登录已过期，请重新登录') return true;
  if (msg.indexOf('Missing request attribute') !== -1) return true;
  if (msg.indexOf('认证失败') !== -1) return true;
  return false;
}

/**
 * 通用请求方法
 * @param {Object} options
 * @param {string} options.url - 请求路径
 * @param {string} options.method - GET/POST/PUT/DELETE
 * @param {Object} options.data - 请求参数
 * @param {boolean} options.loading - 是否显示loading (默认true)
 * @param {string} options.loadingText - loading文字
 * @param {number} options.timeout - 超时时间ms (默认15000)
 * @param {boolean} options.showError - 失败时是否显示toast (默认true)
 */
// 网络状态监听（延迟执行避免页面注册时序问题）
var isOnline = true;

var request = function (options) {
  return new Promise(function (resolve, reject) {
    var token = wx.getStorageSync('token');
    var showLoading = options.loading !== false;
    var showError = options.showError !== false;
    var timeout = options.timeout || 15000;

    if (showLoading) {
      wx.showLoading({
        title: options.loadingText || '加载中...',
        mask: true
      });
    }

    wx.request({
      url: baseUrl + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: timeout,
      header: {
        'content-type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success: function (res) {
        if (showLoading) wx.hideLoading();

        if (res.statusCode === 401) {
          handleAuthExpired();
          reject({ message: '登录已过期', code: 401 });
          return;
        }

        if (res.statusCode === 200) {
          var data = res.data;
          if (!data) {
            resolve(null);
            return;
          }

          if (data.code === 0 || data.code === 200) {
            resolve(data.data);
            return;
          }

          if (isAuthError(data)) {
            handleAuthExpired();
            reject({ message: data.message || data.msg || '认证失败', code: data.code });
            return;
          }

          var msg = data.message || data.msg || '请求失败';
          if (msg.indexOf('Missing request attribute') !== -1 || msg.indexOf('userId') !== -1) {
            handleAuthExpired();
            reject({ message: msg, code: data.code });
            return;
          }

          // 只对关键操作显示错误提示
          if (showError) {
            wx.showToast({ title: msg, icon: 'none' });
          }
          reject({ message: msg, code: data.code });
        } else {
          var errMsg = '服务器错误(' + res.statusCode + ')';
          try {
            if (res.data) {
              errMsg = res.data.message || res.data.msg || res.data.error || errMsg;
            }
          } catch (e) {}
          if (showError) {
            wx.showToast({ title: errMsg, icon: 'none', duration: 3000 });
          }
          reject({ message: errMsg, code: res.statusCode });
        }
      },
      fail: function (err) {
        if (showLoading) wx.hideLoading();
        var errMsg = '网络连接失败，请检查网络';
        if (err.errno === 600000 || err.errno === 600001) {
          errMsg = '网络超时，请稍后重试';
        }
        if (showError) {
          wx.showToast({ title: errMsg, icon: 'none', duration: 3000 });
        }
        // 网络断开时全局标记
        isOnline = false;
        reject({ message: errMsg, code: -1, errno: err.errno });
      }
    });
  });
};

var uploadFile = function (options) {
  return new Promise(function (resolve, reject) {
    var token = wx.getStorageSync('token');
    var timeout = options.timeout || 30000;
    wx.uploadFile({
      url: baseUrl + options.url,
      filePath: options.filePath,
      name: options.name || 'file',
      timeout: timeout,
      header: {
        'Authorization': token ? 'Bearer ' + token : ''
      },
      formData: options.formData || {},
      success: function (res) {
        try {
          var data = JSON.parse(res.data);
          if (data.code === 0 || data.code === 200) {
            resolve(data.data);
          } else {
            wx.showToast({ title: data.message || '上传失败', icon: 'none' });
            reject(data);
          }
        } catch (e) {
          wx.showToast({ title: '上传响应解析失败', icon: 'none' });
          reject(e);
        }
      },
      fail: function (err) {
        wx.showToast({ title: '上传失败，请重试', icon: 'none' });
        reject(err);
      }
    });
  });
};

function getFullImageUrl(url) {
  if (!url) return '';
  if (url.startsWith('/uploads/')) {
    return baseUrl + '/file/' + url.replace('/uploads/', '');
  }
  if (url.startsWith('/api/file/')) {
    return baseUrl + url.replace('/api', '');
  }
  if (url.startsWith('http://') || url.startsWith('https://')) return url;
  if (url.startsWith('/')) {
    return serverUrl + url;
  }
  return serverUrl + '/' + url;
}

var imageCache = {};

/**
 * 加载单张图片（带超时和重试）
 */
function loadImage(url, retryCount) {
  retryCount = retryCount || 2;
  if (!url) return Promise.resolve('');
  var fullUrl = getFullImageUrl(url);
  if (imageCache[fullUrl]) return Promise.resolve(imageCache[fullUrl]);
  if (fullUrl.indexOf('http') !== 0) return Promise.resolve(fullUrl);

  var doLoad = function (attempt) {
    return new Promise(function (resolve) {
      wx.downloadFile({
        url: fullUrl,
        timeout: 8000,
        success: function (res) {
          if (res.statusCode === 200) {
            imageCache[fullUrl] = res.tempFilePath;
            resolve(res.tempFilePath);
          } else if (attempt < retryCount) {
            // 重试
            setTimeout(function () {
              doLoad(attempt + 1).then(resolve);
            }, 1000);
          } else {
            resolve(fullUrl); // 降级为原URL
          }
        },
        fail: function () {
          if (attempt < retryCount) {
            setTimeout(function () {
              doLoad(attempt + 1).then(resolve);
            }, 1000);
          } else {
            resolve(fullUrl); // 降级为原URL
          }
        }
      });
    });
  };

  return doLoad(0);
}

/**
 * 批量加载图片（控制并发数）
 */
function loadImages(urls) {
  if (!urls || !urls.length) return Promise.resolve([]);
  var results = [];
  var index = 0;

  return new Promise(function (resolve) {
    function next() {
      if (index >= urls.length) {
        resolve(results);
        return;
      }
      var i = index++;
      loadImage(urls[i]).then(function (localPath) {
        results[i] = localPath;
        next();
      });
    }
    // 限制并发为3
    for (var i = 0; i < 3 && i < urls.length; i++) {
      next();
    }
  });
}

module.exports = {
  request: request,
  uploadFile: uploadFile,
  baseUrl: baseUrl,
  serverUrl: serverUrl,
  getFullImageUrl: getFullImageUrl,
  loadImage: loadImage,
  loadImages: loadImages
};
