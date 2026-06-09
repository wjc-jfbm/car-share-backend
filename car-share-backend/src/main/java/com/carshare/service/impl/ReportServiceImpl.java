package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Report;
import com.carshare.mapper.ReportMapper;
import com.carshare.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public boolean createReport(Report report) {
        report.setStatus(0);
        report.setCreatedAt(LocalDateTime.now());
        return reportMapper.insert(report) > 0;
    }

    @Override
    public Map<String, Object> getMyReports(Long userId, Integer page, Integer pageSize) {
        Page<Report> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getUserId, userId).orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    public Map<String, Object> getPendingReports(Integer page, Integer pageSize) {
        Page<Report> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getStatus, 0).orderByDesc(Report::getCreatedAt);
        Page<Report> result = reportMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    public boolean handleReport(Long reportId, Long adminId, Integer status, String handleResult) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) return false;
        report.setStatus(status);
        report.setHandleResult(handleResult);
        report.setHandledBy(adminId);
        report.setHandledAt(LocalDateTime.now());
        return reportMapper.updateById(report) > 0;
    }
}
