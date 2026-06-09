package com.carshare.service;

import com.carshare.entity.CarFavorite;
import java.util.List;
import java.util.Map;

public interface CarFavoriteService {
    boolean addFavorite(Long carId, Long userId);
    boolean removeFavorite(Long carId, Long userId);
    boolean isFavorite(Long carId, Long userId);
    Map<String, Object> getMyFavorites(Long userId, Integer page, Integer pageSize);
}
