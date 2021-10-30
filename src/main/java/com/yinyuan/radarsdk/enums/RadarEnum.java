package com.yinyuan.radarsdk.enums;

import lombok.Getter;

/**
 * 雷达类型枚举
 * @author YD_Tao
 */
@Getter
public enum RadarEnum {

    /**24G雷达*/
    Radar_24G(24, "RadarServerPort 24G"),

    /**77G雷达*/
    Radar_77G(77, "RadarServerPort 77G");

    /**
     * 雷达型号
     */
    private int type;

    /**
     * 相关描述
     */
    private String desc;

    RadarEnum(int type, String desc){
        this.type = type;
        this.desc = desc;
    }
}
