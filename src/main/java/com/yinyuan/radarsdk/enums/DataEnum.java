package com.yinyuan.radarsdk.enums;

import lombok.Getter;

/**
 * 雷达数据类型枚举
 * @author YD_Tao
 */
@Getter
public enum DataEnum {

    /**设备心跳数据*/
    DEVICE_HEARTBEAT(10000, "RadarServerPort 77G Heartbeat Data"),

    /**目标点数据*/
    TARGET_DATA(10001, "RadarServerPort 77G Target Point Data"),

    /**点云数据*/
    POINT_CLOUD(10002, "RadarServerPort 77G Point Cloud Data");

    /**
     * 数据类型
     */
    private int type;

    /**
     * 相关描述
     */
    private String desc;

    DataEnum(int type, String desc){
        this.type = type;
        this.desc = desc;
    }
}