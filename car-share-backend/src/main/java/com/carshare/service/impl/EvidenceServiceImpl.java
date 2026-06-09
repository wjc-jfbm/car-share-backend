package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.entity.Evidence;
import com.carshare.mapper.EvidenceMapper;
import com.carshare.service.EvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvidenceServiceImpl implements EvidenceService {

    @Autowired
    private EvidenceMapper evidenceMapper;

    @Override
    public Evidence getEvidenceDetail(Long evidenceId) {
        return evidenceMapper.selectById(evidenceId);
    }

    @Override
    public List<Evidence> adminList(Evidence evidence) {
        LambdaQueryWrapper<Evidence> wrapper = new LambdaQueryWrapper<>();
        if (evidence.getStatus() != null) {
            wrapper.eq(Evidence::getStatus, evidence.getStatus());
        }
        if (evidence.getType() != null) {
            wrapper.eq(Evidence::getType, evidence.getType());
        }
        if (evidence.getCarId() != null) {
            wrapper.eq(Evidence::getCarId, evidence.getCarId());
        }
        wrapper.orderByDesc(Evidence::getCreatedAt);
        return evidenceMapper.selectList(wrapper);
    }

    @Override
    public Page<Evidence> adminListPage(Page<Evidence> page, Evidence evidence) {
        LambdaQueryWrapper<Evidence> wrapper = new LambdaQueryWrapper<>();
        if (evidence.getStatus() != null) {
            wrapper.eq(Evidence::getStatus, evidence.getStatus());
        }
        if (evidence.getType() != null) {
            wrapper.eq(Evidence::getType, evidence.getType());
        }
        if (evidence.getCarId() != null) {
            wrapper.eq(Evidence::getCarId, evidence.getCarId());
        }
        wrapper.orderByDesc(Evidence::getCreatedAt);
        return evidenceMapper.selectPage(page, wrapper);
    }
}
