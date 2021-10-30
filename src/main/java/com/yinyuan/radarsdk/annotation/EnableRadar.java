package com.yinyuan.radarsdk.annotation;

import com.yinyuan.radarsdk.enums.RadarOpenStrategy;
import com.yinyuan.radarsdk.selector.EnableRadarImportSelector;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 开启雷达SDK
 * @author YD_Tao
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import(EnableRadarImportSelector.class)
public @interface EnableRadar {

    /**
     * 雷达SDK的启动策略
     */
    RadarOpenStrategy strategy();
}
