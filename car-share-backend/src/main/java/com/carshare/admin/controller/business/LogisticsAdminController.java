package com.carshare.admin.controller.business;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.annotation.Log;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.enums.BusinessType;
import com.carshare.common.utils.PageUtils;
import com.carshare.entity.Logistics;
import com.carshare.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/business/logistics")
public class LogisticsAdminController extends BaseController
{
    @Autowired
    private LogisticsService logisticsService;

    @PreAuthorize("@ss.hasPermi('business:logistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(@RequestParam(required = false) Integer status,
                              @RequestParam(required = false) String expressNo)
    {
        Page<Logistics> page = PageUtils.startMpPage();
        Page<Logistics> result = logisticsService.adminListPage(page, status, expressNo);
        return getDataTable(result);
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id)
    {
        return success(logisticsService.getAdminDetail(id));
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:query')")
    @GetMapping("/car/{carId}")
    public AjaxResult getByCarId(@PathVariable Long carId)
    {
        return success(logisticsService.getLogisticsByCarId(carId));
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:ship')")
    @Log(title = "物流管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult ship(@RequestBody Logistics logistics)
    {
        return toAjax(logisticsService.adminCreateLogistics(logistics));
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:edit')")
    @Log(title = "物流管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Logistics logistics)
    {
        return toAjax(logisticsService.adminUpdateLogistics(logistics));
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:edit')")
    @Log(title = "物流管理", businessType = BusinessType.UPDATE)
    @PutMapping("/{id}/status")
    public AjaxResult updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> params)
    {
        return toAjax(logisticsService.adminUpdateStatus(id, params.get("status")));
    }

    @PreAuthorize("@ss.hasPermi('business:logistics:remove')")
    @Log(title = "物流管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(logisticsService.adminDeleteLogistics(ids));
    }
}
