package com.carshare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.carshare.entity.*;
import com.carshare.mapper.*;
import com.carshare.service.FeeDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FeeDetailServiceImpl implements FeeDetailService {

    @Autowired
    private FeeDetailMapper feeDetailMapper;
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private CarMemberMapper carMemberMapper;

    @Override
    @Transactional
    public boolean calculateFee(Long carId, Integer shippingFeeType, BigDecimal shippingFee) {
        Car car = carMapper.selectById(carId);
        if (car == null) return false;

        LambdaQueryWrapper<CarMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CarMember::getCarId, carId);
        List<CarMember> members = carMemberMapper.selectList(wrapper);

        BigDecimal memberCount = BigDecimal.valueOf(members.size());
        BigDecimal shippingPerMember;

        switch (shippingFeeType) {
            case 2: // 车主承担
                shippingPerMember = BigDecimal.ZERO;
                break;
            case 1: // 按份数比例
                shippingPerMember = shippingFee.divide(memberCount, 2, RoundingMode.HALF_UP);
                break;
            default: // 均摊
                shippingPerMember = shippingFee.divide(memberCount, 2, RoundingMode.HALF_UP);
                break;
        }

        for (CarMember member : members) {
            LambdaQueryWrapper<FeeDetail> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(FeeDetail::getCarId, carId).eq(FeeDetail::getCarMemberId, member.getId());
            FeeDetail existing = feeDetailMapper.selectOne(existWrapper);

            BigDecimal goodsAmount = member.getAmount() != null ? member.getAmount() : car.getPricePer();
            BigDecimal totalAmount = goodsAmount.add(shippingPerMember);
            BigDecimal depositAmount = car.getDepositAmount() != null ? car.getDepositAmount() : BigDecimal.ZERO;
            BigDecimal balanceAmount = totalAmount.subtract(depositAmount);

            if (existing != null) {
                existing.setGoodsAmount(goodsAmount);
                existing.setShippingFee(shippingPerMember);
                existing.setTotalAmount(totalAmount);
                existing.setShippingFeeType(shippingFeeType);
                existing.setDepositAmount(depositAmount);
                existing.setBalanceAmount(balanceAmount);
                existing.setUpdatedAt(LocalDateTime.now());
                feeDetailMapper.updateById(existing);
            } else {
                FeeDetail fee = new FeeDetail();
                fee.setCarId(carId);
                fee.setCarMemberId(member.getId());
                fee.setUserId(member.getUserId());
                fee.setGoodsAmount(goodsAmount);
                fee.setShippingFee(shippingPerMember);
                fee.setTotalAmount(totalAmount);
                fee.setShippingFeeType(shippingFeeType);
                fee.setDepositAmount(depositAmount);
                fee.setBalanceAmount(balanceAmount);
                fee.setCreatedAt(LocalDateTime.now());
                fee.setUpdatedAt(LocalDateTime.now());
                feeDetailMapper.insert(fee);
            }
        }
        return true;
    }

    @Override
    public FeeDetail getMemberFee(Long carId, Long userId) {
        LambdaQueryWrapper<FeeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeDetail::getCarId, carId).eq(FeeDetail::getUserId, userId);
        return feeDetailMapper.selectOne(wrapper);
    }

    @Override
    public List<FeeDetail> getCarFees(Long carId) {
        LambdaQueryWrapper<FeeDetail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FeeDetail::getCarId, carId);
        return feeDetailMapper.selectList(wrapper);
    }

    @Override
    public boolean updateFeeDetail(FeeDetail feeDetail) {
        feeDetail.setUpdatedAt(LocalDateTime.now());
        return feeDetailMapper.updateById(feeDetail) > 0;
    }
}
