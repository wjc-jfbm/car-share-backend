package com.carshare.admin.controller.business;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.annotation.Log;
import com.carshare.common.core.controller.BaseController;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.enums.BusinessType;
import com.carshare.common.utils.PageUtils;
import com.carshare.entity.User;
import com.carshare.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 小程序用户管理Controller
 */
@RestController
@RequestMapping("/business/wxuser")
public class WxUserAdminController extends BaseController {

    @Autowired
    private UserMapper userMapper;

    /**
     * 查询小程序用户列表
     */
    @PreAuthorize("@ss.hasPermi('business:wxuser:list')")
    @GetMapping("/list")
    public TableDataInfo list(User user) {
        Page<User> page = PageUtils.startMpPage();

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (user.getNickname() != null && !user.getNickname().isEmpty()) {
            wrapper.like("nickname", user.getNickname());
        }
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            wrapper.like("phone", user.getPhone());
        }
        if (user.getStatus() != null) {
            wrapper.eq("status", user.getStatus());
        }
        if (user.getRole() != null) {
            wrapper.eq("role", user.getRole());
        }
        wrapper.orderByDesc("created_at");

        Page<User> result = userMapper.selectPage(page, wrapper);
        // 隐藏敏感信息
        result.getRecords().forEach(u -> {
            u.setOpenid(null);
            u.setUnionid(null);
            u.setSessionKey(null);
            u.setPassword(null);
        });
        return getDataTable(result);
    }

    /**
     * 获取小程序用户详细信息
     */
    @PreAuthorize("@ss.hasPermi('business:wxuser:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setOpenid(null);
            user.setUnionid(null);
            user.setSessionKey(null);
            user.setPassword(null);
        }
        return success(user);
    }

    /**
     * 修改小程序用户状态
     */
    @PreAuthorize("@ss.hasPermi('business:wxuser:edit')")
    @Log(title = "小程序用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody User user) {
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setStatus(user.getStatus());
        updateUser.setRole(user.getRole());
        updateUser.setRealName(user.getRealName());
        updateUser.setPhone(user.getPhone());
        return toAjax(userMapper.updateById(updateUser));
    }

    /**
     * 删除小程序用户
     */
    @PreAuthorize("@ss.hasPermi('business:wxuser:remove')")
    @Log(title = "小程序用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(userMapper.deleteBatchIds(Arrays.asList(ids)));
    }
}
