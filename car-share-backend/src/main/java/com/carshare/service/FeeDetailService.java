package com.carshare.service;

import com.carshare.entity.FeeDetail;
import java.math.BigDecimal;
import java.util.List;

public interface FeeDetailService {
    boolean calculateFee(Long carId, Integer shippingFeeType, BigDecimal shippingFee);
    FeeDetail getMemberFee(Long carId, Long userId);
    List<FeeDetail> getCarFees(Long carId);
    boolean updateFeeDetail(FeeDetail feeDetail);
}
