package com.yinyuan.radarsdk.protocol;

import com.sirtrack.construct.Core;

import java.util.Arrays;
import java.util.List;

import static com.sirtrack.construct.Macros.*;

/**
 * 77G雷达私有协议
 * @author YD_Tao
 */
public class MmWave77 {

    /**协议中常量定义*/
    public static final String SYNCPATTERN = "0201040306050807";

    public static final int SYNC_PATTERN_LENGTH_IN_BYTES = SYNCPATTERN.length() << 1;

    public static final int FRAME_HEADER_LENGTH_IN_BYTES = 52;

    public static final int TLV_HEADER_LENGTH_IN_BYTES = 8;

    public static final int POINT_LENGTH_IN_BYTES = 16;

    public static final int TARGET_LENGTH_IN_BYTES = 68;

    public static final String FRAME_PACKETLENGTH = "packetLength";

    public static final String TLV_NUM = "numTLVs";

    public static final String TLV_HEADER_TYPE = "type";

    public static final String TLV_HEADER_LENGHT = "length";

    public static final List<String> TARGET_PROPERTIES = Arrays.asList("tid", "posX", "posY", "velX", "velY", "accX", "accY");

    public static Core.Construct frameHeaderStructType = Core.Struct(
            "frameHeaderStructType",
            Field("sync", 8),
            ULInt32("version"),
            ULInt32("platform"),
            ULInt32("timestamp"),
            ULInt32("packetLength"),
            ULInt32("frameNumber"),
            ULInt32("subframeNumber"),
            ULInt32("chirpMargin"),
            ULInt32("frameMargin"),
            ULInt32("uartSentTime"),
            ULInt32("trackProcessTime"),
            ULInt16("numTLVs"),
            ULInt16("checksum")
    );

    public static Core.Construct tlvHeaderStruct = Core.Struct(
            "tlvHeaderStruct",
            ULInt32("type"),
            ULInt32("length")
    );

    public static Core.Construct pointStruct2D = Core.Struct(
            "pointStruct2D",
            Field("range", 4),
            Field("azimuth", 4),
            Field("doppler", 4),
            Field("snr", 4)
    );

    public static Core.Construct targetStruct2D = Core.Struct(
            "targetStruct2D ",
            ULInt32("tid"),
            Field("posX", 4),
            Field("posY", 4),
            Field("velX", 4),
            Field("velY", 4),
            Field("accX", 4),
            Field("accY", 4),
            Array(9, Field("EC", 4)),
            Field("G", 4)
    );


    public static Core.Construct targetIndex = Core.Struct(
            "targetIndex",
            ULInt8("targetId")
    );
}
