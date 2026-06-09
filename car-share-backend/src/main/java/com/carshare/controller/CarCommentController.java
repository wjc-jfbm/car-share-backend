package com.carshare.controller;

import com.carshare.common.Result;
import com.carshare.entity.CarComment;
import com.carshare.service.CarCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comment")
public class CarCommentController {

    @Autowired
    private CarCommentService carCommentService;

    @PostMapping("/add")
    public Result<?> addComment(@RequestBody CarComment comment,
                                @RequestAttribute("userId") Long userId) {
        comment.setUserId(userId);
        boolean success = carCommentService.addComment(comment);
        return success ? Result.success(null, "评论成功") : Result.fail("评论失败");
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteComment(@PathVariable Long id,
                                   @RequestAttribute("userId") Long userId) {
        boolean success = carCommentService.deleteComment(id, userId);
        return success ? Result.success(null, "删除成功") : Result.fail("删除失败");
    }

    @GetMapping("/car/{carId}")
    public Result<Map<String, Object>> getCarComments(
            @PathVariable Long carId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(carCommentService.getCarComments(carId, page, pageSize));
    }
}
