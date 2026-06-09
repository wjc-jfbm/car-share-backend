package com.carshare.common.utils.http;

import jakarta.servlet.http.HttpServletRequest;

public class UserAgentUtils
{
    public static String getBrowser(HttpServletRequest request)
    {
        return getBrowser(request.getHeader("User-Agent"));
    }

    public static String getBrowser(String userAgent)
    {
        if (userAgent == null)
        {
            return "Unknown";
        }
        if (userAgent.contains("Edg"))
        {
            return "Edge";
        }
        else if (userAgent.contains("Chrome"))
        {
            return "Chrome";
        }
        else if (userAgent.contains("Firefox"))
        {
            return "Firefox";
        }
        else if (userAgent.contains("Safari"))
        {
            return "Safari";
        }
        return "Unknown";
    }

    public static String getOperatingSystem(HttpServletRequest request)
    {
        return getOperatingSystem(request.getHeader("User-Agent"));
    }

    public static String getOperatingSystem(String userAgent)
    {
        if (userAgent == null)
        {
            return "Unknown";
        }
        if (userAgent.contains("Windows"))
        {
            return "Windows";
        }
        else if (userAgent.contains("Mac"))
        {
            return "Mac";
        }
        else if (userAgent.contains("Linux"))
        {
            return "Linux";
        }
        else if (userAgent.contains("Android"))
        {
            return "Android";
        }
        else if (userAgent.contains("iPhone") || userAgent.contains("iPad"))
        {
            return "iOS";
        }
        return "Unknown";
    }
}
