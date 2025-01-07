package com.wldmedical.hotmeltprint;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.utils.Command;
import com.gprinter.io.BluetoothPort;
import com.gprinter.io.PortManager;

public class Printer {
    private static PortManager portManager = null;
    public static final PrinterDevices devices = null; // Changed to public static final

    private static final ExecutorService executorService = Executors.newCachedThreadPool();

    // 获取打印机管理类
    public static PortManager getPortManager() {
        return portManager;
    }

    // 获取连接状态
    public static boolean getConnectState() {
        return portManager != null && portManager.getConnectStatus();
    }

    // 链接设备
    public static void connect(PrinterDevices devices, Runnable callback) {
        executorService.execute(() -> {
            if (portManager != null) {
                portManager.closePort(); // 关闭上次链接
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 设备非空，判断是否蓝牙连接，如是，进行连接
            if (devices != null) {
                portManager = new BluetoothPort(devices);
                portManager.openPort();
                // 等待1秒后开始打印
                System.out.println("wswTEst 新版本V2");
                // 连接成功后调用回调函数
                callback.run();
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
    public static boolean sendDataToPrinter(byte[] vector) throws IOException {
        return portManager != null && portManager.writeDataImmediately(vector);
    }

    /**
     * 关闭连接
     *
     * @return
     */
    public static void close() {
        if(portManager != null) {
            portManager.closePort();
            portManager = null;
        }
    }
}