package com.carshare.admin.controller.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.annotation.Log;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.enums.BusinessType;
import com.carshare.common.utils.PageUtils;
import com.carshare.entity.Car;
import com.carshare.service.CarService;

@RestController
@RequestMapping("/business/car")
public class CarAdminController extends BaseController
{
    @Autowired
    private CarService carService;

    @PreAuthorize("@ss.hasPermi('business:car:list')")
    @GetMapping("/list")
    public TableDataInfo list(Car car)
    {
        Page<Car> page = PageUtils.startMpPage();
        IPage<Car> result = carService.adminListPage(page, car);
        return getDataTable(result);
    }

    @PreAuthorize("@ss.hasPermi('business:car:query')")
    @GetMapping("/{carId}")
    public AjaxResult getInfo(@PathVariable Long carId)
    {
        return success(carService.getCarDetail(carId));
    }

    @PreAuthorize("@ss.hasPermi('business:car:edit')")
    @Log(title = "拼车管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Car car)
    {
        return toAjax(carService.updateCar(car));
    }

    @PreAuthorize("@ss.hasPermi('business:car:remove')")
    @Log(title = "拼车管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{carIds}")
    public AjaxResult remove(@PathVariable Long[] carIds)
    {
        return toAjax(carService.deleteCars(carIds));
    }
}
