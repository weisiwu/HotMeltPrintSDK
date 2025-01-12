package com.wldmedical.hotmeltprint;

import android.bluetooth.BluetoothAdapter;

import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.io.IOException;
import java.util.Vector;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.io.BluetoothPort;
import com.gprinter.io.BleBlueToothPort;
import com.gprinter.utils.Command;

public class Printer {
    public static Printer printer=null;
    public static BluetoothPort portManager = null;
    public static final PrinterDevices devices = null;
    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    public Printer(){
    }

    public static Printer getInstance(){
       if (printer==null){
           printer=new Printer();
       }
       return printer;
    }

    // 获取打印机管理类
    public static BluetoothPort getPortManager() {
        return portManager;
    }

    // 获取连接状态
    public static boolean getConnectState() {
         System.out.println("wswTest【HotmeltPinter】检查链接状态的portManager " + portManager);
         if (portManager != null) {
             System.out.println("wswTest【HotmeltPinter】检查链接状态的portManager.getConnectStatus() " + portManager.getConnectStatus());
         }
         return portManager != null && portManager.getConnectStatus();
    }

    // 链接设备
    public static void connect(final PrinterDevices devices, final Runnable callback) {
        // callback 需要声明为 final
        executorService.execute(new Runnable() { // 使用匿名内部类
            @Override
            public void run() {
                System.out.println("wswTest【HotmeltPinter】开始检查portManager");
                if (portManager != null) {
                    System.out.println("wswTest【HotmeltPinter】portManager非空");
                    portManager.closePort();
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    System.out.println("wswTest【HotmeltPinter】sleep error");
                    e.printStackTrace();
                }
                System.out.println("wswTest【HotmeltPinter】检查设备");
                if (devices != null) {
                    System.out.println("wswTest【HotmeltPinter】准备打开portManager");
                    portManager = new BluetoothPort(devices);
                    Boolean tdd = portManager.openPort();
                    System.out.println("wswTest【HotmeltPinter】链接后portManager" + BluetoothAdapter.checkBluetoothAddress("41:17:3A:F0:BF:9A"));
                    System.out.println("wswTest【HotmeltPinter】是否能成功打开端口" + tdd);
                    // 连接成功后调用回调函数
                    callback.run();
                }
            }
        });
    }

    /**
     * 获取打印机状态
     *
     * @param printerCommand 打印机命令 ESC为小票，TSC为标签 ，CPCL为面单
     * @return 返回值常见文档说明
     * @throws IOException
     */
    public static int getPrinterState(Command printerCommand, long delayMillis) throws IOException {
        return portManager.getPrinterStatus(printerCommand);
    }

    /**
     * 获取打印机电量
     *
     * @return
     * @throws IOException
     */
    public static int getPower() throws IOException {
        return portManager.getPower();
    }

    /**
     * 获取打印机指令
     *
     * @return
     */
    public static Command getPrinterCommand() {
        return portManager.getCommand();
    }

    /**
     * 设置使用指令
     *
     * @param printerCommand
     */
    public static void setPrinterCommand(Command printerCommand) {
        if(portManager != null) {
            portManager.setCommand(printerCommand);
        }
    }

    /**
     * 发送数据到打印机 指令集合内容
     *
     * @param vector
     * @return true发送成功 false 发送失败
     * 打印机连接异常或断开发送时会抛异常，可以捕获异常进行处理
     */
    public static boolean sendDataToPrinter(Vector<Byte> vector) throws IOException {
        return portManager != null && portManager.writeDataImmediately(vector);
    }

    /**
     * 关闭连接
     *
     * @return
     */
    public static void close() {
        System.out.println("wswTest【HotmeltPinter】 准备关闭");
        if(portManager != null) {
            portManager.closePort();
            System.out.println("wswTest【HotmeltPinter】 成功关闭！！！");
            portManager = null;
        }
    }
}