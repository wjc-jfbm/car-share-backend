package com.carshare.common.utils;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页结果工具类
 * 统一分页返回值格式，消除重复的 HashMap 拼接代码
 */
public class PageResult {

    /**
     * 从 MyBatis-Plus IPage 构建分页结果
     */
    public static Map<String, Object> of(IPage<?> page) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", page.getRecords());
        result.put("total", page.getTotal());
        result.put("page", (int) page.getCurrent());
        result.put("pageSize", (int) page.getSize());
        return result;
    }

    /**
     * 自定义列表和总数
     */
    public static Map<String, Object> of(List<?> list, long total, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * 空分页结果
     */
    public static Map<String, Object> empty(int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("list", List.of());
        result.put("total", 0L);
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }
}
