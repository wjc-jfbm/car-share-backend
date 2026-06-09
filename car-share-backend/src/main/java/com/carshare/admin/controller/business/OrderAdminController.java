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
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.annotation.Log;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.enums.BusinessType;
import com.carshare.common.utils.PageUtils;
import com.carshare.entity.CarOrder;
import com.carshare.service.CarOrderService;

@RestController
@RequestMapping("/business/order")
public class OrderAdminController extends BaseController
{
    @Autowired
    private CarOrderService carOrderService;

    @PreAuthorize("@ss.hasPermi('business:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(CarOrder order)
    {
        Page<CarOrder> page = PageUtils.startMpPage();
        Page<CarOrder> result = carOrderService.adminListPage(page, order);
        return getDataTable(result);
    }

    @PreAuthorize("@ss.hasPermi('business:order:query')")
    @GetMapping("/{orderId}")
    public AjaxResult getInfo(@PathVariable Long orderId)
    {
        return success(carOrderService.getOrderDetail(orderId));
    }

    @PreAuthorize("@ss.hasPermi('business:order:edit')")
    @Log(title = "订单管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody CarOrder order)
    {
        return toAjax(carOrderService.updateOrder(order));
    }

    @PreAuthorize("@ss.hasPermi('business:order:remove')")
    @Log(title = "订单管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(carOrderService.deleteOrders(orderIds));
    }
}
