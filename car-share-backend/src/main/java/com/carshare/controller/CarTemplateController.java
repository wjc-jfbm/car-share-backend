package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.CarTemplate;
import com.carshare.service.CarTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/template")
public class CarTemplateController {

    @Autowired
    private CarTemplateService carTemplateService;

    @PostMapping("/save")
    public Result<?> saveTemplate(@RequestBody CarTemplate template,
                                  @RequestAttribute("userId") Long userId) {
        template.setUserId(userId);
        boolean success = carTemplateService.saveTemplate(template);
        return success ? Result.success(null, "模板保存成功") : Result.fail("模板保存失败");
    }

    @PutMapping("/update")
    public Result<?> updateTemplate(@RequestBody CarTemplate template,
                                    @RequestAttribute("userId") Long userId) {
        template.setUserId(userId);
        boolean success = carTemplateService.updateTemplate(template);
        return success ? Result.success(null, "模板更新成功") : Result.fail("模板更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteTemplate(@PathVariable Long id,
                                    @RequestAttribute("userId") Long userId) {
        boolean success = carTemplateService.deleteTemplate(id, userId);
        return success ? Result.success(null, "模板删除成功") : Result.fail("模板删除失败");
    }

    @PutMapping("/{id}/top")
    public Result<?> setTop(@PathVariable Long id,
                            @RequestAttribute("userId") Long userId) {
        boolean success = carTemplateService.setTop(id, userId);
        return success ? Result.success(null, "操作成功") : Result.fail("操作失败");
    }

    @GetMapping("/my")
    public Result<Map<String, Object>> getMyTemplates(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carTemplateService.getMyTemplates(userId, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<CarTemplate> getTemplateById(@PathVariable Long id,
                                                @RequestAttribute("userId") Long userId) {
        CarTemplate template = carTemplateService.getTemplateById(id, userId);
        return template != null ? Result.success(template) : Result.fail("模板不存在");
    }
}
