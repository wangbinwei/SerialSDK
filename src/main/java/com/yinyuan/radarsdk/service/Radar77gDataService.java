package com.yinyuan.radarsdk.service;
import com.sirtrack.construct.lib.Containers;

import com.yinyuan.radarsdk.manager.SerialPortManager;
import com.yinyuan.radarsdk.pojo.PeopleCount;
import com.yinyuan.radarsdk.protocol.MmWave77;
import com.yinyuan.radarsdk.service.impl.PeopleCountServiceImpl;
import com.yinyuan.radarsdk.utils.ByteConvertUtil;
import com.yinyuan.radarsdk.utils.ByteUtils;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sirtrack.construct.lib.Binary.byteArrayToHexString;
import static com.sirtrack.construct.lib.Binary.hexStringToByteArray;
import static com.yinyuan.radarsdk.protocol.MmWave77.TARGET_LENGTH_IN_BYTES;
import static com.yinyuan.radarsdk.protocol.MmWave77.TARGET_PROPERTIES;
import static com.yinyuan.radarsdk.protocol.MmWave77.*;
/**
 * @author wbw
 * @date 2021/10/14 15:38
 */
@Slf4j
public class Radar77gDataService {
    //final int receive_temp_size = 8192;
    final int receive_temp_size = 30000;
    int frame_head_index = 0;
    int frame_tail_index = 0;

    @Autowired
    private PeopleCountService peopleCountService;

    public SerialPort openSerialPort(int baudrate, String commName) {
        // 串口对象
        SerialPort mSerialport = null;
        if (commName == null || commName.equals("")) {
            log.info("没有有效串口！");
            return null;
        }else{
            try {
                mSerialport = SerialPortManager.openPort(commName, baudrate);
            } catch (PortInUseException e) {
                log.info("串口被占用");
                return null;
            }
        }

        // 添加串口监听
        SerialPort finalMSerialport = mSerialport;
        SerialPortManager.addListener(mSerialport, new SerialPortManager.DataAvailableListener() {

            @Override
            public void dataAvailable() {
                byte[] data = null;
                try {
                    if (finalMSerialport == null) {
                        //ShowUtils.errorMessage("串口对象为空，监听失败！");
                        log.info("串口对象为空，监听失败！");
                        return ;
                    } else {
                        // 读取串口数据
                        data = SerialPortManager.readFromPort(finalMSerialport);
                        int length = data.length;
                        //System.out.println("length:"+length);
                        //System.out.println(ByteUtils.byteArrayToHexString(data) + "\r\n");
                        //第一种方法：SDK方法
                        /*if(length > receive_temp_size){
                            log.info("ERROR! received data out of buffer range\n");
                            return ;
                        }
                        else if(length < 52 && length != 0){
                            log.info("data too short");
                            return ;
                        }
                        else if (length >=52 && length <= receive_temp_size) {
                            //todo 数据接收处理
                            StringBuilder content = new StringBuilder(byteArrayToHexString(data));
                            int frameStartIndex = content.indexOf(SYNCPATTERN);
                            int frameEndIndex = content.indexOf(SYNCPATTERN, frameStartIndex + SYNC_PATTERN_LENGTH_IN_BYTES >> 1);
                            if (frameStartIndex != -1) {
                                while (frameEndIndex != -1) {
                                    String frame = content.substring(frameStartIndex, frameEndIndex);
                                    content.delete(frameStartIndex, frameEndIndex);
                                    frameEndIndex = content.indexOf(SYNCPATTERN, frameStartIndex + SYNC_PATTERN_LENGTH_IN_BYTES >> 1);
                                    parseOneFrame(frame, finalMSerialport.getName());
                                }
                                String frame = content.toString();
                                parseOneFrame(frame, finalMSerialport.getName());
                            }
                        }*/


                        //第二种：嵌入式方法
                        if(length > receive_temp_size){
                            log.info("ERROR! received data out of buffer range;Data Length: {}\n",length);
                            return ;
                        }
                        else if(length < 52 && length != 0){
                            log.info("data too short");
                            return ;
                        }
                        else if (length >=52 && length <= receive_temp_size) {
                            //byte[] receive_temp = comByteMap.get(finalMSerialport.getName());
                            sendFrames(data,length,finalMSerialport.getName());
                        }
                        else{ //length == 0,情况不会发生

                            return ;
                        }


                    }
                } catch (Exception e) {
                    log.error("串口读取错误");
                    //ShowUtils.errorMessage(e.toString());
                    // 发生读取错误时显示错误信息后退出系统
                    //System.exit(0);
                }
            }
        });
        //返回端口
        return mSerialport;
    }

    /**
     * 解析一帧数据
     * @param frame
     * @param sn
     */
    private void parseOneFrame(String frame, String sn){
        if ((FRAME_HEADER_LENGTH_IN_BYTES << 1) > frame.length()) {
            //log.warn("Radar 77G : The length of the data part of the frame is wrong, this data analysis failed and skipped(1)!");
            return;
        }
        //处理帧头
        String frameHeaderContent = frame.substring(0, FRAME_HEADER_LENGTH_IN_BYTES << 1);
        Containers.Container frameHeader = MmWave77.frameHeaderStructType.parse(hexStringToByteArray(frameHeaderContent));
        //数据部分长度(按字节) = 包长 - 帧头长度
        int dataLength = (int) frameHeader.get(FRAME_PACKETLENGTH) - FRAME_HEADER_LENGTH_IN_BYTES;
        //偏移量，左移是因为两位十六进制为一个字节(目前是十六进制数据)
        int frameOffset = FRAME_HEADER_LENGTH_IN_BYTES << 1;
        //一帧的数据部分(十六进制)
        String frameData = frame.substring(frameOffset);
        //数据长度效验
        if (dataLength <= 0 || dataLength << 1 != frameData.length()){
            log.info("{HeartBeat dataType: Radar_77G, sn: {}}", sn);
            //log.warn("Radar 77G : The length of the data part of the frame is wrong, this data analysis failed and skipped(2)!");
            return;
        }
        //该帧包含的tlv个数
        int tlvCount = frameHeader.get(TLV_NUM);
        //解析数据部分
        parseDataOfFrame(frameData, tlvCount, sn);
    }

    /**
     * @param frameData
     * @param tlvCount
     * @param sn
     */
    private void parseDataOfFrame(String frameData, int tlvCount, String sn){
        //目标点list
        List<Map<String, Object>> targetList = new ArrayList<>();
        //结果集
        Map<String, Object> result = new HashMap<>(16);

        //偏移量
        int offset = 0;
        //处理给一个tlv
        for (int i = 0; i < tlvCount; i++) {
            //获取tlv头
            Containers.Container tlvHeader = MmWave77.tlvHeaderStruct.parse(
                    hexStringToByteArray(frameData.substring(offset, offset + 16))
            );
            //获取tlv信息
            int tlvType = tlvHeader.get(TLV_HEADER_TYPE);
            int tlvLength = tlvHeader.get(TLV_HEADER_LENGHT);
            //长度效验
            if (((tlvLength << 1 )) + offset > frameData.length()) {
                log.warn("Radar 77G : The length of the data part of the frame is wrong, this data analysis failed and skipped(3)!");
                return;
            }
            //处理tlv体
            offset += (TLV_HEADER_LENGTH_IN_BYTES << 1);
            int valueLength = tlvLength - TLV_HEADER_LENGTH_IN_BYTES;
            switch (tlvType) {
                case 6:
                    //数据封装,tlv类型6为心跳数据
//                    result.put("dataType", RadarEnum.Radar_77G);
                    result.put("sn", sn);
                    result.put("data", null);
                    //发送数据
//                    if (callback != null){
//                        JSONObject jsonResult = new JSONObject(result);
//                        callback.notify(jsonResult.toJSONString());
//                    }
                    offset += (valueLength << 1);
                    break;
                case 7:
                    //tlv类型6为目标点数据
                    try {
                        //处理目标点，一个tlv包含多个目标点
                        int targetCount = valueLength / TARGET_LENGTH_IN_BYTES;
                        for (int j = 0; j < targetCount; j++) {
                            //解析一个目标点
                            Containers.Container targetStruct = MmWave77.targetStruct2D.parse(
                                    hexStringToByteArray(
                                            frameData.substring(offset, offset + (TARGET_LENGTH_IN_BYTES << 1))
                                    )
                            );
                            //目标点
                            Map<String, Object> target = new HashMap<>(16);
                            //封装tid, posX, posY, velX, velY, accX, accY
                            TARGET_PROPERTIES.forEach((key)-> {
                                Object value = targetStruct.get(key);
                                if (value instanceof byte[]){
                                    target.put(key, ByteConvertUtil.byte2float(targetStruct.get(key), 0));
                                } else {
                                    target.put(key, value);
                                }
                            });
                            //添加到目标点list
                            log.info("{posX : {}}", target.get("posX").toString());
                            //目标点的框在x:[-1.5,1.5], y:[0.8,1.7]范围
                            if(-1.5 < Double.parseDouble(target.get("posX").toString()) && 1.5 > Double.parseDouble(target.get("posX").toString()) &&
                                    0.8 < Double.parseDouble(target.get("posY").toString()) && 1.7 > Double.parseDouble(target.get("posY").toString())){
                                //添加到目标点list
                                targetList.add(target);
                            }else{
                                //log.info("Radar 77G : Out of boundary of Box");
                            }
                            offset += (TARGET_LENGTH_IN_BYTES << 1);
                        }
                        //数据封装
//                        result.put("dataType", RadarEnum.Radar_77G);
                        result.put("sn", sn);
                        result.put("data", targetList);
                        log.info("Radar 77G true PeopleNum:{}",targetList.size());
                        log.info("Radar 77G PeopleNum:{}",targetCount);
                        //发送
//                        if (callback != null){
//                            JSONObject jsonResult = new JSONObject(result);
//                            callback.notify(jsonResult.toJSONString());
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    break;
                default:
                    offset += (valueLength << 1);
                    break;
            }
        }
    }

    void sendFrames(byte[] receive_temp_data, int length, String serial_port_Name){
        // DEBUG_RADAR("Length: %d\n", length);
        List<Map<String, Object>> targetList = new ArrayList<>();
        int[] receive_temp = new int[receive_temp_data.length];
        for(int i =0;i<receive_temp_data.length;i++){
            receive_temp[i] = receive_temp_data[i] & 0xff;
        }
        int state = 0;
        for(int i = 0; i < length; i++){
            switch (state)
            {
                case 0: // 寻找帧头，是否以02开头
                    if(receive_temp[i] == 0x02){
                        frame_head_index = i;
                        state = 1;
                    }
                    break;
                case 1: // 寻找帧头，02的下一个字节是否为01
                    if(receive_temp[i] == 0x01){
                        state = 2;
                    }else{
                        state = 0; //不是帧头，重新开始
                    }
                    break;
                case 2: // 寻找帧头，01的下一个字节是否为04
                    if(receive_temp[i] == 0x04){
                        state = 3;
                    }else{
                        state = 0; //不是帧头，重新开始
                    }
                    break;
                case 3: // 找到帧头，接收数据
                    if(receive_temp[i] == 0x02){
                        state = 4;
                    }
                    break;
                case 4: // 找到帧头，接收数据
                    if(receive_temp[i] == 0x01){
                        state = 5;
                    }
                    else{ // 否则退回上一状态3
                        state = 3;
                    }
                    break;
                case 5: // 疑似找到下一个帧头
                    if(receive_temp[i] == 0x04){
                        // 喂狗
//                        ESP.wdtFeed();
                        // 抽取数据帧
                        frame_tail_index = i - 2; // 指向帧尾下一个元素
                        int frame_length = frame_tail_index - frame_head_index;
                        int[] frame = new int[frame_length];
                        byte[] frameByte = new byte[frame_length];
                        // length实发长度
                        //memcpy, (frame, receive_temp + frame_head_index, frame_length);
                        System.arraycopy(receive_temp, frame_head_index, frame, 0, frame_length);
                        System.arraycopy(receive_temp_data, frame_head_index, frameByte, 0, frame_length); //一帧的字节数组
                        // real_frame_length 应发长度
                        int real_frame_length = frame[20] + (frame[21] << 8) + (frame[22] << 16) + (frame[23] << 24);
                        //log.info("This time send frame length: {}", real_frame_length);
                        int this_frame_length=0;
                        if (real_frame_length > length)
                        {
                            return ;
                        }
                        int index  = 52 ;
                        int num_TLVs = frame[48] + (frame[49] << 8);
                        //log.info("This time num_TLVs: {}", num_TLVs);
                        int targetnums = 0;
                        for(int j = 0; j < num_TLVs;j++){ //遍历TLV类型数量

                            //log.info("This TLV type is: {}", frame[index]);
                            if (frame[index] == 0x06)
                            {
                                // DEBUG_RADAR("This TLV is 6 \n");
                                index = index + 4;
                                this_frame_length = frame[index] + (frame[index+1] << 8) + (frame[index+2] << 16) + (frame[index+3] << 24);

                                // index += this_frame_length;
                                index = index + this_frame_length - 4;
                                //DEBUG_RADAR("This TLV length is :%d and index:%d\n", this_frame_length, index);
                            }
                            else if (frame[index] == 0x08)
                            {
                                // DEBUG_RADAR("This TLV is 8 \n");
                                index = index + 4;
                                this_frame_length = frame[index] + (frame[index+1] << 8) + (frame[index+2] << 16) + (frame[index+3] << 24);
                                // index += this_frame_length;
                                index = index + this_frame_length - 4;
                                //DEBUG_RADAR("This TLV length is :%d and index:%d\n", this_frame_length, index);
                            }
                            else if (frame[index] == 0x07)
                            {
                                //4bytes标识,4bytes长度
                                index = index + 4;
                                this_frame_length = frame[index] + (frame[index+1] << 8) + (frame[index+2] << 16) + (frame[index+3] << 24);
                                int targetCount = (this_frame_length - 8) / 68;
                                int peopleNum = 0;
                                index = index + 4;
                                int offset = index;
                                for (int k = 0; k < targetCount; k++) { //解析每个目标点
                                    //目标点
                                    //解析一个目标点
                                    byte[]  targetByte = new byte[TARGET_LENGTH_IN_BYTES];
                                    System.arraycopy(frameByte, offset, targetByte,0,TARGET_LENGTH_IN_BYTES);
                                    Containers.Container targetStruct = MmWave77.targetStruct2D.parse(targetByte);

                                    Map<String, Object> target = new HashMap<>(16);
                                    //封装tid, posX, posY, velX, velY, accX, accY
                                    TARGET_PROPERTIES.forEach((key)-> {
                                        Object value = targetStruct.get(key);
                                        if (value instanceof byte[]){
                                            target.put(key, ByteConvertUtil.byte2float(targetStruct.get(key), 0));
                                        } else {
                                            target.put(key, value);
                                        }
                                    });

                                    //log.info("{posX : {}, posY : {}}", target.get("posX").toString(),target.get("posY").toString());

                                    //目标点的框在x:[-1.5,1.5], y:[0.8,1.7]范围
                                   if(-1.5 < Double.parseDouble(target.get("posX").toString()) && 1.5 > Double.parseDouble(target.get("posX").toString()) &&
                                            0.8 < Double.parseDouble(target.get("posY").toString()) && 1.7 > Double.parseDouble(target.get("posY").toString())){
                                        //添加到目标点list
                                        targetList.add(target);
                                        peopleNum++;
                                    }else{
                                        //log.info("Radar 77G : Out of boundary of Box");
                                    }
                                    offset += TARGET_LENGTH_IN_BYTES ;
                                }
                                if(PeopleCountServiceImpl.ON_START == 1) {
                                    peopleCountService.insertData2List(new PeopleCount(targetList.size(),PeopleCountServiceImpl.START_TIME,serial_port_Name, LocalDateTime.now()));
                                }
                                targetnums = peopleNum;
                                //targetnums = targetCount;
                                index = index + this_frame_length - 4;
                            }
                        }
                        if(targetnums != 0) {
                            log.info("This time target nums: {} ,from port:{}", targetnums, serial_port_Name);
                        }
                        //todo 人数结果发送
                        frame_head_index = frame_tail_index;
                        state = 3; //下一次直接从状态3开始
                    }else{
                        state = 3; //不是下一个帧头，继续接收数据
                    }
                    break;
                default:
                    log.error("ERROR! invalidate status in sendFrames\n");
                    break;
            }
        }
    }

    public static void main(String[] args) {
        Radar77gDataService radar77gDataService = new Radar77gDataService();
        ArrayList<String> ports = SerialPortManager.findPorts();
        for (String port : ports) {
            System.out.println(port);
        }
        SerialPort serialPort1 = radar77gDataService.openSerialPort(115200, "COM9");
        SerialPort serialPort2 = radar77gDataService.openSerialPort(115200,"COM10");
//        System.out.println(serialPort1.toString());
//        System.out.println(serialPort2.toString());
    }
}
