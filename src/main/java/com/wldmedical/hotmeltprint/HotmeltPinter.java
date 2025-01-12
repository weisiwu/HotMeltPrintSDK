package com.wldmedical.hotmeltprint;

import java.io.IOException;
import java.io.InputStream;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.command.EscCommand;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.content.Context;
import android.os.Build.VERSION;

public class HotmeltPinter extends AbstractHotmeltPinter {
    private final Printer printer = Printer.getInstance();
    private final EscCommand esc = new EscCommand();
    private long queryInterval = 5050;
    private String macAddress = "";
    TimeClass myObject = new TimeClass();
    String formattedDateTime = myObject.getCurrentFormattedDateTime();
    private static final String TAG = "wswTestTAG Printer";

    /**
     * 链接打印机
     * 1. 按照传入的mac地址，自动连接打印机设备
     */
    @Override
    public void connect(String mac, Context context, final boolean autoPrint) { // autoPrint 声明为 final
        try {
            macAddress = mac;
            System.out.println("wswTest【开始输出context】 开始自动打印V2" + context);
            PrinterDevices blueTooth = new PrinterDevices.Build()
                    .setContext(context)
                    .setConnMethod(ConnMethod.BLUETOOTH)
                    .setMacAddress(mac)
                    .setCommand(Command.ESC)
                    .build();
            System.out.println("wswTest【HotmeltPinter】 开始链接设备===> " + mac);
            if (BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                System.out.println("wswTest【HotmeltPinter】 蓝牙是开启的123131231121 "  + BluetoothAdapter.getDefaultAdapter().getRemoteDevice("41:42:3A:F0:BF:9A").getName());
            }
            System.out.println("wswTest【HotmeltPinter】 设备版本是多少===> " + VERSION.SDK_INT);

            printer.connect(blueTooth, new Runnable() { // 转换为匿名内部类
                @Override
                public void run() {
                System.out.println("wswTest 执行到 Printer.connect 里面");
                    try {
                        Thread.sleep(2000);
                        System.out.println("wswTest【HotmeltPinter】是不是先睡觉，睡了5秒" );
//                        System.out.println("wswTest【HotmeltPinter】获取的所有service是什么~~~" + printer.portManager.getSupportedGattServices());
                        if (autoPrint) {
                            System.out.println("wswTest【HotmeltPinter】 已经准备自动打印了");
                            print();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            System.out.println("wswTest【HotmeltPinter】 执行到Printer.connect后面 ");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Connect failed (IllegalArgument): " + e.getMessage(), e);
            throw new RuntimeException("Connect failed (IllegalArgument): " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Connect failed (Other): " + e.getMessage(), e);
            throw new RuntimeException("Connect failed (Other): " + e.getMessage());
        }
    }

    /**
     * 打印数据，入口
     */
    @Override
    public void print() {
        System.out.println("wswTest【HotmeltPinter】 进入打印流程");

        // if (!checkDeviceStatus()) {
        //     System.out.println("wswTest【HotmeltPinter】 设备状态检验失败");
        //     return;
        // }

        System.out.println("wswTest【HotmeltPinter】 准备开始打印头部");
        headerPrint();
        System.out.println("wswTest【HotmeltPinter】 打印完毕头部，开始打印波形");
        wavePrint();
        System.out.println("wswTest【HotmeltPinter】 打印完毕头部，波形打印结束");
    }

    /**
     * 检查蓝牙打印机状态
     */
    private boolean checkDeviceStatus() {
        System.out.println("wswTest【HotmeltPinter】检查状态 " + Printer.getConnectState());
        return Printer.getConnectState();
    }

    /**
     * 打印小票头部
     */
    private void headerPrint() {
        System.out.println("wswTest【HotmeltPinter】 开始打印发票头部");
        esc.addInitializePrinter();
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出1");
        esc.addSetLineSpacing((byte) 100);
        System.out.println("wswTest【HotmeltPinter】 打印设置搞定");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream imageStream = classLoader.getResourceAsStream("images/logo.png");
         System.out.println("wswTest【HotmeltPinter】 这是要打印的图片" + imageStream);
         if (imageStream != null) {
             Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
             if (bitmap != null) {
                 System.out.println("wswTest Bitmap loaded with width: " + bitmap.getWidth() + ", height: " + bitmap.getHeight());
                 // 打印图片,58打印机图片宽度最大为384dot  1mm=8dot 用尺子量取图片的宽度单位为Xmm  传入宽度值为 X*8
                 esc.drawImage(bitmap, 384);
             } else {
                 System.out.println("wswTest Failed to decode Bitmap");
             }
         } else {
             System.out.println("wswTest Failed to load resource");
         }
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出2");
        esc.addText("电话: 13501129344\n");
        esc.addText("地址: 北京市北京经济技术开发区宏达南路3号院2号楼3层301室\n");
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出3");
        esc.addText("网址: https://www.wldyq.com\n");
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出4");
        // 设置纠错等级
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31);
        // 设置qrcode模块大小
        esc.addSelectSizeOfModuleForQRCode((byte) 4);
        // 设置qrcode内容
        esc.addStoreQRCodeData("https://www.wldyq.com");
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出5");
        // 打印QRCode
        esc.addPrintQRCode();
        esc.addText("\n");
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("********************************\n");
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出6");
        esc.addText("时间: " + formattedDateTime + "\n");
        esc.addText("设备: CapnoEasy\n");
        esc.addText("序号: " + macAddress + "\n");
        esc.addText("********************************\n");
        esc.addText("\n");
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出7");
        VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试命令输出8");
        System.out.println("wswTest 开始准备发送命令接受的Port" + printer.getPortManager());
        if (printer.getPortManager() != null) {
            try {
                System.out.println("wswTest 将要开始打印的命令" + esc.getCommand());
                VectorByteToLog.logVectorByteDecimal(esc.getCommand(), "wswTEst测试输出VectorByteToLog");
                printer.getPortManager().writeDataImmediately(esc.getCommand());
                System.out.println("wswTest 打印结束，看下发送函数===");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("wswTest 打印出现错误===");
            }
        }
    }

    /**
     * 打印波形
     * 启动一个协程，不停地从队列中处理数据（打印等）
     */
    private void wavePrint() {
        // 定时查收指定地址数据，如有新数据，统一绘制
        // GlobalScope.launch {
        //     while (true) {
        //         delay(queryInterval) // 每隔 1 秒处理
    }
}