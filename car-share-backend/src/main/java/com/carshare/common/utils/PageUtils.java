package com.carshare.common.utils;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.carshare.common.core.page.PageDomain;
import com.carshare.common.core.page.TableSupport;
import com.carshare.common.utils.sql.SqlUtil;

public class PageUtils
{
    public static void startPage()
    {
    }

    public static <T> Page<T> startMpPage()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        Integer pageNum = pageDomain.getPageNum();
        Integer pageSize = pageDomain.getPageSize();
        String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
        Page<T> page = new Page<>(pageNum, pageSize);
        if (StringUtils.isNotEmpty(orderBy))
        {
            String[] orderItems = orderBy.split(",");
            for (String item : orderItems)
            {
                item = item.trim();
                if (item.toLowerCase().endsWith(" desc"))
                {
                    page.addOrder(OrderItem.desc(item.substring(0, item.length() - 5).trim()));
                }
                else if (item.toLowerCase().endsWith(" asc"))
                {
                    page.addOrder(OrderItem.asc(item.substring(0, item.length() - 4).trim()));
                }
                else
                {
                    page.addOrder(OrderItem.asc(item));
                }
            }
        }
        return page;
    }

    public static void clearPage()
    {
    }
}
