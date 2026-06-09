package com.carshare.admin.controller.system;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.carshare.common.config.RuoYiConfig;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.domain.entity.SysUser;
import com.carshare.common.utils.SecurityUtils;
import com.carshare.common.utils.StringUtils;
import com.carshare.system.service.ISysUserService;

@RestController
public class SysIndexController
{
    @Autowired
    private RuoYiConfig ruoyiConfig;

    @Autowired
    private ISysUserService userService;

    @GetMapping("/")
    public ResponseEntity<Resource> index()
    {
        File distFile = Paths.get("admin-ui/dist/index.html").toFile();
        if (distFile.exists())
        {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(new FileSystemResource(distFile));
        }
        ClassPathResource classPathResource = new ClassPathResource("static/index.html");
        if (classPathResource.exists())
        {
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .body(classPathResource);
        }
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(new org.springframework.core.io.ByteArrayResource(
                        StringUtils.format("欢迎使用{}后台管理框架，当前版本：v{}，请通过前端地址访问。",
                                ruoyiConfig.getName(), ruoyiConfig.getVersion()).getBytes()));
    }

    @PostMapping("/unlockscreen")
    public AjaxResult unlockScreen(@RequestBody Map<String, String> body)
    {
        String password = body.get("password");
        if (StringUtils.isEmpty(password))
        {
            return AjaxResult.error("密码不能为空");
        }
        String username = SecurityUtils.getUsername();
        SysUser user = userService.selectUserByUserName(username);
        if (user == null)
        {
            return AjaxResult.error("服务器超时，请重新登录");
        }
        if (!SecurityUtils.matchesPassword(password, user.getPassword()))
        {
            return AjaxResult.error("密码错误，请重新输入");
        }

        return AjaxResult.success("解锁成功");
    }
}
