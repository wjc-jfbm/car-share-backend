package com.carshare.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.constant.HttpStatus;
import com.carshare.common.core.domain.AjaxResult;
import com.carshare.common.core.domain.model.LoginUser;
import com.carshare.common.core.page.PageDomain;
import com.carshare.common.core.page.TableDataInfo;
import com.carshare.common.core.page.TableSupport;
import com.carshare.common.utils.DateUtils;
import com.carshare.common.utils.PageUtils;
import com.carshare.common.utils.SecurityUtils;
import com.carshare.common.utils.StringUtils;

public class BaseController
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    protected void startPage()
    {
    }

    protected void startOrderBy()
    {
    }

    protected void clearPage()
    {
    }

    protected TableDataInfo getDataTable(List<?> list)
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        int total = list.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        List<?> pageList;
        if (fromIndex < total)
        {
            pageList = list.subList(fromIndex, toIndex);
        }
        else
        {
            pageList = List.of();
        }
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(pageList);
        rspData.setTotal(total);
        return rspData;
    }

    protected <T> TableDataInfo getDataTable(Page<T> page)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    protected <T> TableDataInfo getDataTable(IPage<T> page)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }

    public AjaxResult success()
    {
        return AjaxResult.success();
    }

    public AjaxResult error()
    {
        return AjaxResult.error();
    }

    public AjaxResult success(String message)
    {
        return AjaxResult.success(message);
    }

    public AjaxResult success(Object data)
    {
        return AjaxResult.success(data);
    }

    public AjaxResult error(String message)
    {
        return AjaxResult.error(message);
    }

    public AjaxResult warn(String message)
    {
        return AjaxResult.warn(message);
    }

    protected AjaxResult toAjax(int rows)
    {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    protected AjaxResult toAjax(boolean result)
    {
        return result ? success() : error();
    }

    public String redirect(String url)
    {
        return StringUtils.format("redirect:{}", url);
    }

    public LoginUser getLoginUser()
    {
        return SecurityUtils.getLoginUser();
    }

    public Long getUserId()
    {
        return getLoginUser().getUserId();
    }

    public Long getDeptId()
    {
        return getLoginUser().getDeptId();
    }

    public String getUsername()
    {
        return getLoginUser().getUsername();
    }
}
