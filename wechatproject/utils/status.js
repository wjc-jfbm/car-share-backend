/**
 * 拼车状态统一工具
 * 与后端 CarStatus 枚举保持一致：
 *   0=招募中  1=已满员  2=已结算  3=已发货  4=已完成  5=已取消
 */
var STATUS_MAP = {
  0: { text: '招募中', color: '#4CAF50', icon: '🟢' },
  1: { text: '已满员', color: '#FF9800', icon: '🟡' },
  2: { text: '已结算', color: '#2196F3', icon: '🔵' },
  3: { text: '已发货', color: '#9C27B0', icon: '🟣' },
  4: { text: '已完成', color: '#607D8B', icon: '✅' },
  5: { text: '已取消', color: '#F44336', icon: '❌' }
};

/**
 * 获取状态信息
 * @param {number|string} status - 状态码
 * @returns {{text: string, color: string, icon: string}}
 */
function getStatusInfo(status) {
  var code = parseInt(status);
  if (isNaN(code)) code = 0;
  return STATUS_MAP[code] || STATUS_MAP[0];
}

/**
 * 获取状态文本
 * @param {number|string} status
 * @returns {string}
 */
function getStatusText(status) {
  return getStatusInfo(status).text;
}

module.exports = {
  getStatusInfo: getStatusInfo,
  getStatusText: getStatusText,
  STATUS_MAP: STATUS_MAP
};
