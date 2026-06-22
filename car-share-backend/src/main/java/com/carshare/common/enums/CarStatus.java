package com.carshare.common.enums;

import lombok.Getter;

/**
 * 拼车状态枚举
 * 统一管理拼车生命周期中的所有状态码
 */
@Getter
public enum CarStatus {
    /** 招募中 */
    RECRUITING(0, "招募中"),
    /** 已满员（手动截止或自动满员） */
    CLOSED(1, "已满员"),
    /** 已结算（车主确认所有成员付款） */
    SETTLED(2, "已结算"),
    /** 已发货（物流已发出） */
    SHIPPED(3, "已发货"),
    /** 已完成（所有成员确认收货） */
    COMPLETED(4, "已完成"),
    /** 已取消（车主取消或系统自动过期关闭） */
    CANCELLED(5, "已取消");

    private final int code;
    private final String label;

    CarStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * 根据状态码获取枚举值
     */
    public static CarStatus fromCode(int code) {
        for (CarStatus s : values()) {
            if (s.code == code) return s;
        }
        return RECRUITING;
    }

    /**
     * 根据状态码获取中文标签
     */
    public static String getLabel(int code) {
        return fromCode(code).getLabel();
    }
}
