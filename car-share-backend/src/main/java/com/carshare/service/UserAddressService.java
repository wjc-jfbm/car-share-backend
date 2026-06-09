package com.carshare.service;

import com.carshare.entity.UserAddress;
import java.util.List;

public interface UserAddressService {
    List<UserAddress> getMyAddresses(Long userId);
    UserAddress getDefaultAddress(Long userId);
    boolean addAddress(UserAddress address);
    boolean updateAddress(UserAddress address);
    boolean deleteAddress(Long id, Long userId);
    boolean setDefault(Long id, Long userId);
}
