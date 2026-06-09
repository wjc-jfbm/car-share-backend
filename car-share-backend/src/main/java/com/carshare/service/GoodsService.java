package com.carshare.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Goods;

import java.util.List;
import java.util.Map;

public interface GoodsService {
    Map<String, Object> getGoodsList(Integer page, Integer pageSize, String keyword, String type);
    Goods getGoodsDetail(Long id);
    Long createGoods(Goods goods);
    boolean updateGoods(Long id, Goods goods);
    boolean updateGoods(Goods goods);
    boolean deleteGoods(Long id);
    boolean deleteGoods(Long[] ids);
    List<Goods> getAllGoods();
    List<Goods> adminList(Goods goods);
    Page<Goods> adminListPage(Page<Goods> page, Goods goods);
}
