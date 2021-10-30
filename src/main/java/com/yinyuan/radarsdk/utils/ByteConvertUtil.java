package com.yinyuan.radarsdk.utils;

/**
 * 字节转换工具类
 * @author YD_Tao
 */
public class ByteConvertUtil {

    /**
     * 字节数组转为浮点数float32
     * @param b 字节数组至少4字节
     * @param index 起始下标
     * @return 32位浮点数
     * */
    public static float byte2float(byte[] b, int index) {
        int l;
        l = b[index];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return Float.intBitsToFloat(l);
    }


}
