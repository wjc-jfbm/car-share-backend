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
import com.carshare.entity.Goods;
import com.carshare.service.GoodsService;

@RestController
@RequestMapping("/business/goods")
public class GoodsAdminController extends BaseController
{
    @Autowired
    private GoodsService goodsService;

    @PreAuthorize("@ss.hasPermi('business:goods:list')")
    @GetMapping("/list")
    public TableDataInfo list(Goods goods)
    {
        Page<Goods> page = PageUtils.startMpPage();
        Page<Goods> result = goodsService.adminListPage(page, goods);
        return getDataTable(result);
    }

    @PreAuthorize("@ss.hasPermi('business:goods:query')")
    @GetMapping("/{goodsId}")
    public AjaxResult getInfo(@PathVariable Long goodsId)
    {
        return success(goodsService.getGoodsDetail(goodsId));
    }

    @PreAuthorize("@ss.hasPermi('business:goods:edit')")
    @Log(title = "商品管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody Goods goods)
    {
        return toAjax(goodsService.updateGoods(goods));
    }

    @PreAuthorize("@ss.hasPermi('business:goods:remove')")
    @Log(title = "商品管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{goodsIds}")
    public AjaxResult remove(@PathVariable Long[] goodsIds)
    {
        return toAjax(goodsService.deleteGoods(goodsIds));
    }
}
