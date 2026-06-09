package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.Goods;
import com.carshare.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public Result<?> getGoodsList(@RequestParam(defaultValue = "1") Integer page,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String type) {
        return Result.success(goodsService.getGoodsList(page, pageSize, keyword, type));
    }

    @GetMapping("/all")
    public Result<?> getAllGoods() {
        return Result.success(goodsService.getAllGoods());
    }

    @GetMapping("/detail/{id}")
    public Result<?> getGoodsDetail(@PathVariable Long id) {
        Goods goods = goodsService.getGoodsDetail(id);
        if (goods == null) {
            return Result.fail("商品不存在");
        }
        return Result.success(goods);
    }

    @PostMapping
    public Result<?> createGoods(@RequestBody Goods goods) {
        Long id = goodsService.createGoods(goods);
        return Result.success(id, "创建成功");
    }

    @PutMapping("/{id}")
    public Result<?> updateGoods(@PathVariable Long id, @RequestBody Goods goods) {
        boolean success = goodsService.updateGoods(id, goods);
        return success ? Result.success(null, "更新成功") : Result.fail("更新失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteGoods(@PathVariable Long id) {
        boolean success = goodsService.deleteGoods(id);
        return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
    }
}
