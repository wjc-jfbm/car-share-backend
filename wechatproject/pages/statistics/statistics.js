const { request } = require('../../utils/request');

Page({
  data: {
    userStats: {},
    creditTrend: [],
    statusDistribution: [],
    monthlyTrend: [],
    loading: true
  },

  onShow() {
    this.loadAllStatistics();
  },

  async loadAllStatistics() {
    this.setData({ loading: true });
    try {
      var results = await Promise.all([
        request({ url: '/statistics/user', method: 'GET', loading: false }),
        request({ url: '/statistics/credit-trend', method: 'GET', loading: false }),
        request({ url: '/statistics/car-status-distribution', method: 'GET', loading: false }),
        request({ url: '/statistics/monthly-trend', method: 'GET', loading: false })
      ]);

      this.setData({
        userStats: results[0],
        creditTrend: results[1].trend || [],
        statusDistribution: results[2].distribution || [],
        monthlyTrend: results[3].trend || [],
        loading: false
      });

      this.drawCreditTrendChart();
      this.drawStatusPieChart();
      this.drawMonthlyTrendChart();
    } catch (err) {
      this.setData({ loading: false });
    }
  },

  drawCreditTrendChart() {
    var trend = this.data.creditTrend;
    if (!trend || trend.length === 0) return;

    var query = wx.createSelectorQuery();
    query.select('#creditCanvas').fields({ node: true, size: true }).exec(function (res) {
      if (!res[0]) return;
      var canvas = res[0].node;
      var ctx = canvas.getContext('2d');
      var dpr = wx.getWindowInfo().pixelRatio;
      canvas.width = res[0].width * dpr;
      canvas.height = res[0].height * dpr;
      ctx.scale(dpr, dpr);

      var w = res[0].width;
      var h = res[0].height;
      var padding = 40;
      var chartW = w - padding * 2;
      var chartH = h - padding * 2;

      var scores = trend.map(function (t) { return t.score; });
      var minScore = Math.min.apply(null, scores) - 5;
      var maxScore = Math.max.apply(null, scores) + 5;
      var scoreRange = maxScore - minScore || 1;

      ctx.strokeStyle = '#eee';
      ctx.lineWidth = 1;
      for (var i = 0; i <= 4; i++) {
        var y = padding + chartH * i / 4;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(w - padding, y);
        ctx.stroke();

        ctx.fillStyle = '#999';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxScore - (scoreRange * i / 4)), padding - 5, y + 3);
      }

      ctx.strokeStyle = '#FF6B9D';
      ctx.lineWidth = 2;
      ctx.beginPath();
      for (var i = 0; i < trend.length; i++) {
        var x = padding + chartW * i / (trend.length - 1);
        var y = padding + chartH * (1 - (scores[i] - minScore) / scoreRange);
        if (i === 0) ctx.moveTo(x, y);
        else ctx.lineTo(x, y);
      }
      ctx.stroke();

      ctx.fillStyle = '#FF6B9D';
      for (var i = 0; i < trend.length; i++) {
        var x = padding + chartW * i / (trend.length - 1);
        var y = padding + chartH * (1 - (scores[i] - minScore) / scoreRange);
        ctx.beginPath();
        ctx.arc(x, y, 3, 0, Math.PI * 2);
        ctx.fill();
      }

      ctx.fillStyle = '#999';
      ctx.font = '10px sans-serif';
      ctx.textAlign = 'center';
      for (var i = 0; i < trend.length; i++) {
        var x = padding + chartW * i / (trend.length - 1);
        var label = trend[i].date.substring(5);
        ctx.fillText(label, x, h - 10);
      }
    });
  },

  drawStatusPieChart() {
    var distribution = this.data.statusDistribution;
    if (!distribution || distribution.length === 0) return;

    var query = wx.createSelectorQuery();
    query.select('#statusCanvas').fields({ node: true, size: true }).exec(function (res) {
      if (!res[0]) return;
      var canvas = res[0].node;
      var ctx = canvas.getContext('2d');
      var dpr = wx.getWindowInfo().pixelRatio;
      canvas.width = res[0].width * dpr;
      canvas.height = res[0].height * dpr;
      ctx.scale(dpr, dpr);

      var w = res[0].width;
      var h = res[0].height;
      var centerX = w * 0.35;
      var centerY = h / 2;
      var radius = Math.min(centerX, centerY) - 20;

      var total = 0;
      for (var i = 0; i < distribution.length; i++) {
        total += distribution[i].value;
      }

      if (total === 0) {
        ctx.fillStyle = '#ccc';
        ctx.font = '14px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText('暂无数据', centerX, centerY);
        return;
      }

      var colors = ['#FF6B9D', '#4FC3F7', '#81C784', '#FFB74D'];
      var startAngle = -Math.PI / 2;

      for (var i = 0; i < distribution.length; i++) {
        var sliceAngle = (distribution[i].value / total) * Math.PI * 2;
        ctx.beginPath();
        ctx.moveTo(centerX, centerY);
        ctx.arc(centerX, centerY, radius, startAngle, startAngle + sliceAngle);
        ctx.closePath();
        ctx.fillStyle = colors[i % colors.length];
        ctx.fill();
        startAngle += sliceAngle;
      }

      ctx.beginPath();
      ctx.arc(centerX, centerY, radius * 0.5, 0, Math.PI * 2);
      ctx.fillStyle = 'white';
      ctx.fill();

      ctx.fillStyle = '#333';
      ctx.font = 'bold 16px sans-serif';
      ctx.textAlign = 'center';
      ctx.fillText(total, centerX, centerY + 5);

      var legendX = w * 0.65;
      var legendY = 30;
      for (var i = 0; i < distribution.length; i++) {
        ctx.fillStyle = colors[i % colors.length];
        ctx.fillRect(legendX, legendY + i * 30, 14, 14);

        ctx.fillStyle = '#333';
        ctx.font = '12px sans-serif';
        ctx.textAlign = 'left';
        ctx.fillText(distribution[i].name + ' ' + distribution[i].value, legendX + 20, legendY + i * 30 + 12);
      }
    });
  },

  drawMonthlyTrendChart() {
    var trend = this.data.monthlyTrend;
    if (!trend || trend.length === 0) return;

    var query = wx.createSelectorQuery();
    query.select('#monthlyCanvas').fields({ node: true, size: true }).exec(function (res) {
      if (!res[0]) return;
      var canvas = res[0].node;
      var ctx = canvas.getContext('2d');
      var dpr = wx.getWindowInfo().pixelRatio;
      canvas.width = res[0].width * dpr;
      canvas.height = res[0].height * dpr;
      ctx.scale(dpr, dpr);

      var w = res[0].width;
      var h = res[0].height;
      var padding = 40;
      var chartW = w - padding * 2;
      var chartH = h - padding * 2;

      var counts = trend.map(function (t) { return t.count; });
      var maxCount = Math.max.apply(null, counts);
      if (maxCount === 0) maxCount = 1;

      ctx.strokeStyle = '#eee';
      ctx.lineWidth = 1;
      for (var i = 0; i <= 4; i++) {
        var y = padding + chartH * i / 4;
        ctx.beginPath();
        ctx.moveTo(padding, y);
        ctx.lineTo(w - padding, y);
        ctx.stroke();

        ctx.fillStyle = '#999';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'right';
        ctx.fillText(Math.round(maxCount * (4 - i) / 4), padding - 5, y + 3);
      }

      var barWidth = chartW / trend.length * 0.6;
      var gap = chartW / trend.length;

      for (var i = 0; i < trend.length; i++) {
        var x = padding + gap * i + (gap - barWidth) / 2;
        var barH = chartH * counts[i] / maxCount;
        var y = padding + chartH - barH;

        ctx.fillStyle = '#4FC3F7';
        ctx.beginPath();
        ctx.moveTo(x + 3, y);
        ctx.lineTo(x + barWidth - 3, y);
        ctx.quadraticCurveTo(x + barWidth, y, x + barWidth, y + 3);
        ctx.lineTo(x + barWidth, padding + chartH);
        ctx.lineTo(x, padding + chartH);
        ctx.lineTo(x, y + 3);
        ctx.quadraticCurveTo(x, y, x + 3, y);
        ctx.fill();

        ctx.fillStyle = '#999';
        ctx.font = '10px sans-serif';
        ctx.textAlign = 'center';
        ctx.fillText(trend[i].month.substring(5), x + barWidth / 2, h - 10);

        if (counts[i] > 0) {
          ctx.fillStyle = '#333';
          ctx.font = '10px sans-serif';
          ctx.fillText(counts[i], x + barWidth / 2, y - 5);
        }
      }
    });
  }
});
