package com.carshare.service;

import com.carshare.entity.CarComment;
import java.util.Map;

public interface CarCommentService {
    boolean addComment(CarComment comment);
    boolean deleteComment(Long id, Long userId);
    Map<String, Object> getCarComments(Long carId, Integer page, Integer pageSize);
}
