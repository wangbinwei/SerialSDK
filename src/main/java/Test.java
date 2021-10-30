/**
 * @author wbw
 * @date 2021/10/14 14:27
 */
import javax.print.DocFlavor;
import java.util.ArrayList;


import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import gnu.io.*;

//public class SerialPortDao implements Runnable, SerialPortEventListener {
public class Test {
    private static boolean isOpen=false;
    static Set<CommPortIdentifier> portList=new HashSet<CommPortIdentifier>();
    final static String appName="MyApp";
    private static InputStream is;
    private static OutputStream os;
    private static SerialPort serialPort;
    static byte[] readBuffer=new byte[1024];

    public static void main(String[] args) {
        System.out.printf("*****************\n");

        Set<CommPortIdentifier> ListTmp = getPortList();
        for (CommPortIdentifier portIp:ListTmp) {
            String strPort = portIp.getName();
            System.out.println(strPort);
            if (strPort.equals("COM8")) {
                if (openSerialPort(portIp, 1000)) {
                    System.out.printf("Opend %s Success.\n", strPort);
                    while (true) {
                        sendMessage("ATZ\r");
                        delay_ms(1000);
                        //uartReceiveDatafromSingleChipMachine(serialPort);
                        //serialEvent(serialPort);

                        try {
                            //获取data buffer数据长度
                            int bufferLength = is.available();
                            // System.out.println(bufferLength);
                            if (bufferLength > 0) {
                                is.read();
                                // delay_ms(100);
                            }
                        } catch (Exception e){

                        }

                    }
                } else {
                    System.out.printf("Error: Opend %s Failed!!!\n", strPort);
                }
            }
        }

        System.out.printf("----- End -----\n");
    }

    public static void delay_ms(int nms){
        try {
            Robot r = new Robot();
            r.delay(nms);
        } catch (Exception e) {
        }
    }

    public static Set<CommPortIdentifier> getPortList(){
        /*不带参数的getPortIdentifiers方法获得一个枚举对象，该对象又包含了系统中管理每个端口的CommPortIdentifier对象。
         * 注意这里的端口不仅仅是指串口，也包括并口。
         * 这个方法还可以带参数。
         * getPortIdentifiers(CommPort)获得与已经被应用程序打开的端口相对应的CommPortIdentifier对象。
         * getPortIdentifier(String portName)获取指定端口名（比如“COM1”）的CommPortIdentifier对象。
         */
        Enumeration tempPortList=CommPortIdentifier.getPortIdentifiers(); //枚举类
        while(tempPortList.hasMoreElements()){
            //在这里可以调用getPortType方法返回端口类型，串口为CommPortIdentifier.PORT_SERIAL
            CommPortIdentifier portIp = (CommPortIdentifier) tempPortList.nextElement();
            portList.add(portIp);
        }
        return portList;
    }

    public static boolean openSerialPort(CommPortIdentifier portIp,int delay){
        try {
            serialPort=(SerialPort) portIp.open(appName, delay);
            /* open方法打开通讯端口，获得一个CommPort对象。它使程序独占端口。
             * 如果端口正被其他应用程序占用，将使用 CommPortOwnershipListener事件机制，传递一个PORT_OWNERSHIP_REQUESTED事件。
             * 每个端口都关联一个 InputStream 和一个OutputStream。
             * 如果端口是用open方法打开的，那么任何的getInputStream都将返回相同的数据流对象，除非有close 被调用。
             * 有两个参数，第一个为应用程序名；第二个参数是在端口打开时阻塞等待的毫秒数。
             */
        } catch (PortInUseException e) {
            return false;
        }
        try {
            is=serialPort.getInputStream();/*获取端口的输入流对象*/
            os=serialPort.getOutputStream();/*获取端口的输出流对象*/
        } catch (IOException e) {
            return false;
        }
        //try {
        //serialPort.addEventListener(this);/*注册一个SerialPortEventListener事件来监听串口事件*/
        //} catch (TooManyListenersException e) {
        //    return false;
        // }
        serialPort.notifyOnDataAvailable(true);/*数据可用*/
        try {
            /*设置串口初始化参数，依次是波特率，数据位，停止位和校验*/
            serialPort.setSerialPortParams(38400, SerialPort.DATABITS_8,SerialPort.STOPBITS_1 , SerialPort.PARITY_NONE);
        } catch (UnsupportedCommOperationException e) {
            return false;
        }
        return true;
    }

    public static boolean closeSerialPort(){
        if(isOpen){
            try {
                is.close();
                os.close();
                serialPort.notifyOnDataAvailable(false);
                serialPort.removeEventListener();
                serialPort.close();
                isOpen = false;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    public static boolean sendMessage(String message){
        try {
            System.out.println(message);
            os.write(message.getBytes());
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * 从串口读取数据
     * @param serialPort 当前已建立连接的SerialPort对象
     * @return 读取到的数据
     * @throws ReadDataFromSerialPortFailure 从串口读取数据时出错
     * @throws SerialPortInputStreamCloseFailure 关闭串口对象输入流出错
     */
    public static byte[] readFromPort(SerialPort serialPort) {

        InputStream in = null;
        byte[] bytes = null;

        try {

            in = serialPort.getInputStream();
            int bufflenth = in.available();        //获取buffer里的数据长度

            while (bufflenth != 0) {
                bytes = new byte[bufflenth];    //初始化byte数组为buffer中数据的长度
                in.read(bytes);
                bufflenth = in.available();
            }
        } catch (IOException e) {
            //throw new ReadDataFromSerialPortFailure();
        } finally {
            try {
                if (in != null) {
                    in.close();
                    in = null;
                }
            } catch(IOException e) {
                //throw new SerialPortInputStreamCloseFailure();
            }

        }

        return bytes;

    }

    public void serialEvent(SerialPortEvent event) {
        /*
         * 此处省略一下事件，可酌情添加
         *  SerialPortEvent.BI:/*Break interrupt,通讯中断
         *  SerialPortEvent.OE:/*Overrun error，溢位错误
         *  SerialPortEvent.FE:/*Framing error，传帧错误
         *  SerialPortEvent.PE:/*Parity error，校验错误
         *  SerialPortEvent.CD:/*Carrier detect，载波检测
         *  SerialPortEvent.CTS:/*Clear to send，清除发送
         *  SerialPortEvent.DSR:/*Data set ready，数据设备就绪
         *  SerialPortEvent.RI:/*Ring indicator，响铃指示
         *  SerialPortEvent.OUTPUT_BUFFER_EMPTY:/*Output buffer is empty，输出缓冲区清空
         */
        System.out.println("eve");
        if(event.getEventType()==SerialPortEvent.DATA_AVAILABLE){
            /*Data available at the serial port，端口有可用数据。读到缓冲数组，输出到终端*/
            try {
                while(is.available()>0){
                    is.read(readBuffer);//收到的数据再此，可视情况处理
                    System.out.println(readBuffer);
                }
                //SPCommandDao.startDoMessage(new String (readBuffer));//这一句是我的自定义类，处理接受到的信息，可删除
            } catch (IOException e) {
            }
        }
    }

    /*
     * 上位机接收数据
     * 串口对象seriesPort
     * 接收数据buffer
     * 返回一个byte数组
     */
    public  static  byte[] uartReceiveDatafromSingleChipMachine(SerialPort serialPort)
    {
        byte[] receiveDataPackage=null;
        InputStream in=null;
        try
        {
            in=serialPort.getInputStream();
            //获取data buffer数据长度
            int bufferLength=in.available();
            System.out.println(bufferLength);
            //while(bufferLength!=0)
            if (bufferLength > 0)
            {
                //receiveDataPackage=new byte[bufferLength];
                //      in.read();
                //       in.read(receiveDataPackage);
                //       bufferLength=in.available();



            }
        }
        catch (IOException e)
        {
            // e.printStackTrace();
        }
        return receiveDataPackage;
    }

    public static void run() {
        try {
            Thread.sleep(50);//每次收发数据完毕后线程暂停50ms
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}