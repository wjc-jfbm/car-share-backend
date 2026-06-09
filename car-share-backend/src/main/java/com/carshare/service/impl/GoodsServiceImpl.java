package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Goods;
import com.carshare.mapper.GoodsMapper;
import com.carshare.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Override
    public Map<String, Object> getGoodsList(Integer page, Integer pageSize, String keyword, String type) {
        Page<Goods> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Goods::getName, keyword);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Goods::getType, type);
        }
        wrapper.eq(Goods::getStatus, 1);
        wrapper.orderByDesc(Goods::getCreatedAt);

        Page<Goods> result = goodsMapper.selectPage(pageObj, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }

    @Override
    public Goods getGoodsDetail(Long id) {
        return goodsMapper.selectById(id);
    }

    @Override
    public Long createGoods(Goods goods) {
        goods.setStatus(1);
        goodsMapper.insert(goods);
        return goods.getId();
    }

    @Override
    public boolean updateGoods(Long id, Goods goods) {
        goods.setId(id);
        return goodsMapper.updateById(goods) > 0;
    }

    @Override
    public boolean deleteGoods(Long id) {
        Goods goods = new Goods();
        goods.setId(id);
        goods.setStatus(0);
        return goodsMapper.updateById(goods) > 0;
    }

    @Override
    public List<Goods> getAllGoods() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1);
        wrapper.orderByDesc(Goods::getCreatedAt);
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public boolean updateGoods(Goods goods) {
        return goodsMapper.updateById(goods) > 0;
    }

    @Override
    public boolean deleteGoods(Long[] ids) {
        return goodsMapper.deleteBatchIds(Arrays.asList(ids)) > 0;
    }

    @Override
    public List<Goods> adminList(Goods goods) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (goods.getName() != null && !goods.getName().isEmpty()) {
            wrapper.like(Goods::getName, goods.getName());
        }
        if (goods.getType() != null && !goods.getType().isEmpty()) {
            wrapper.eq(Goods::getType, goods.getType());
        }
        if (goods.getStatus() != null) {
            wrapper.eq(Goods::getStatus, goods.getStatus());
        }
        wrapper.orderByDesc(Goods::getCreatedAt);
        return goodsMapper.selectList(wrapper);
    }

    @Override
    public Page<Goods> adminListPage(Page<Goods> page, Goods goods) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (goods.getName() != null && !goods.getName().isEmpty()) {
            wrapper.like(Goods::getName, goods.getName());
        }
        if (goods.getType() != null && !goods.getType().isEmpty()) {
            wrapper.eq(Goods::getType, goods.getType());
        }
        if (goods.getStatus() != null) {
            wrapper.eq(Goods::getStatus, goods.getStatus());
        }
        wrapper.orderByDesc(Goods::getCreatedAt);
        return goodsMapper.selectPage(page, wrapper);
    }
}
