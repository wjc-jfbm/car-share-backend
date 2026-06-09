package com.carshare.service;

import com.carshare.entity.CarTemplate;
import java.util.List;
import java.util.Map;

public interface CarTemplateService {
    boolean saveTemplate(CarTemplate template);
    boolean updateTemplate(CarTemplate template);
    boolean deleteTemplate(Long id, Long userId);
    boolean setTop(Long id, Long userId);
    Map<String, Object> getMyTemplates(Long userId, Integer page, Integer pageSize);
    CarTemplate getTemplateById(Long id, Long userId);
}
