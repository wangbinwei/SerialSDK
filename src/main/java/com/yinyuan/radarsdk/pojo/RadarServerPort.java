package com.yinyuan.radarsdk.pojo;

import lombok.Data;

/**
 * 接服务端的服务端端口
 * @author YD_Tao
 */
@Data
public class RadarServerPort {

    /**
     * 连接服务端的服务端端口
     */
    private String serialPort;

    public RadarServerPort(Integer port){
        this.serialPort = serialPort;
    }
}
