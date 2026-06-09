package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.CarTemplate;
import com.carshare.mapper.CarTemplateMapper;
import com.carshare.service.CarTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarTemplateServiceImpl implements CarTemplateService {

    @Autowired
    private CarTemplateMapper carTemplateMapper;

    @Override
    public boolean saveTemplate(CarTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        if (template.getIsTop() == null) template.setIsTop(0);
        return carTemplateMapper.insert(template) > 0;
    }

    @Override
    public boolean updateTemplate(CarTemplate template) {
        CarTemplate existing = carTemplateMapper.selectById(template.getId());
        if (existing == null || !existing.getUserId().equals(template.getUserId())) return false;
        template.setUpdatedAt(LocalDateTime.now());
        return carTemplateMapper.updateById(template) > 0;
    }

    @Override
    public boolean deleteTemplate(Long id, Long userId) {
        CarTemplate existing = carTemplateMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) return false;
        return carTemplateMapper.deleteById(id) > 0;
    }

    @Override
    public boolean setTop(Long id, Long userId) {
        CarTemplate existing = carTemplateMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) return false;
        existing.setIsTop(existing.getIsTop() == 1 ? 0 : 1);
        existing.setUpdatedAt(LocalDateTime.now());
        return carTemplateMapper.updateById(existing) > 0;
    }

    @Override
    public Map<String, Object> getMyTemplates(Long userId, Integer page, Integer pageSize) {
        Page<CarTemplate> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CarTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarTemplate::getUserId, userId).orderByDesc(CarTemplate::getIsTop).orderByDesc(CarTemplate::getCreatedAt);
        Page<CarTemplate> result = carTemplateMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    public CarTemplate getTemplateById(Long id, Long userId) {
        CarTemplate template = carTemplateMapper.selectById(id);
        if (template != null && template.getUserId().equals(userId)) return template;
        return null;
    }
}
