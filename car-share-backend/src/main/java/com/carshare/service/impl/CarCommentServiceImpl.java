package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.CarComment;
import com.carshare.mapper.CarCommentMapper;
import com.carshare.service.CarCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class CarCommentServiceImpl implements CarCommentService {

    @Autowired
    private CarCommentMapper carCommentMapper;

    @Override
    public boolean addComment(CarComment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        return carCommentMapper.insert(comment) > 0;
    }

    @Override
    public boolean deleteComment(Long id, Long userId) {
        CarComment comment = carCommentMapper.selectById(id);
        if (comment == null || !comment.getUserId().equals(userId)) return false;
        return carCommentMapper.deleteById(id) > 0;
    }

    @Override
    public Map<String, Object> getCarComments(Long carId, Integer page, Integer pageSize) {
        Page<CarComment> pageObj = new Page<>(page, pageSize);
        LambdaQueryWrapper<CarComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarComment::getCarId, carId).orderByAsc(CarComment::getCreatedAt);
        Page<CarComment> result = carCommentMapper.selectPage(pageObj, wrapper);

        Map<String, Object> map = new HashMap<>();
        map.put("list", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("pageSize", pageSize);
        return map;
    }
}
