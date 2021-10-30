package com.yinyuan.radarsdk.selector;

import com.yinyuan.radarsdk.annotation.EnableRadar;
import com.yinyuan.radarsdk.configuration.Radar77gConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

/**
 * @author tyd
 * @date 2021/7/3
 */
public class EnableRadarImportSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        Map<String, Object> attributes = metadata.getAnnotationAttributes(EnableRadar.class.getName());
        assert attributes != null;
        String strategy = attributes.get("strategy").toString();
        switch (strategy){
            case "RADAR_77G":
                return new String[] {Radar77gConfiguration.class.getName()};
            default:
                return new String[] {Radar77gConfiguration.class.getName()};
        }
    }
}
