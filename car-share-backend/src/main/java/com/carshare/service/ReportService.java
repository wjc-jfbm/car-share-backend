package com.carshare.service;

import com.carshare.entity.Report;
import java.util.Map;

public interface ReportService {
    boolean createReport(Report report);
    Map<String, Object> getMyReports(Long userId, Integer page, Integer pageSize);
    Map<String, Object> getPendingReports(Integer page, Integer pageSize);
    boolean handleReport(Long reportId, Long adminId, Integer status, String handleResult);
}
