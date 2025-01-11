package com.wldmedical.hotmeltprint;

import java.io.IOException;
import java.io.InputStream;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.command.EscCommand;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import androidx.annotation.Keep;
import android.util.Log;

public class HotmeltPinter implements HotmeltPinterInterface {
    private final Printer printer = new Printer(); // 获取管理对象
    private final EscCommand esc = new EscCommand();
    private long queryInterval = 5050;
    private String macAddress = "";
    TimeClass myObject = new TimeClass();
    String formattedDateTime = myObject.getCurrentFormattedDateTime();
    private static final String TAG = "wswTestTAG Printer"; // 定义一个 TAG，方便在 Logcat 中过滤日志

    /**
     * 链接打印机
     * 1. 按照传入的mac地址，自动连接打印机设备
     */
    @Override
    public void connect(String mac, boolean autoPrint) {
        try {
            macAddress = mac;
            PrinterDevices blueTooth = new PrinterDevices.Build()
                    .setConnMethod(ConnMethod.BLUETOOTH)
                    .setMacAddress(mac)
                    .setCommand(Command.ESC)
                    .build();
            System.out.println("wswTest【HotmeltPinter】 开始链接设备===> " + mac);
            Printer.connect(blueTooth, () -> { //Lambda 表达式
                if (autoPrint) {
                    System.out.println("wswTest【HotmeltPinter】 开始自动打印V2");
                    print();
                }
            });
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Connect failed (IllegalArgument): " + e.getMessage(), e); // 输出详细日志
            throw new RuntimeException("Connect failed (IllegalArgument): " + e.getMessage()); // 重新抛出 RuntimeException
        } catch (Exception e) { // 捕获其他类型的异常
            Log.e(TAG, "Connect failed (Other): " + e.getMessage(), e); // 输出详细日志
            throw new RuntimeException("Connect failed (Other): " + e.getMessage()); // 重新抛出 RuntimeException
        }
    }

    /**
     * 打印数据，入口
     */
    @Override
    public void print() {
        System.out.println("wswTest【HotmeltPinter】 进入打印流程");
        if (!checkDeviceStatus()) {
            System.out.println("wswTest【HotmeltPinter】 设备状态检验失败");
            return;
        }

        headerPrint();
        wavePrint();
    }

    /**
     * 检查蓝牙打印机状态
     */
    private boolean checkDeviceStatus() {
        System.out.println("wswTest【HotmeltPinter】 " + Printer.getConnectState());
        return Printer.getConnectState();
    }

    /**
     * 打印小票头部
     */
    private void headerPrint() {
        System.out.println("wswTest【HotmeltPinter】 开始打印发票头部");
        esc.addInitializePrinter();
        esc.addSetLineSpacing((byte) 100);
        System.out.println("wswTest【HotmeltPinter】 打印设置搞定");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream imageStream = classLoader.getResourceAsStream("images/logo.png");
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
        esc.addText("电话: 13501129344\n");
        esc.addText("地址: 北京市北京经济技术开发区宏达南路3号院2号楼3层301室\n");
        esc.addText("网址: https://www.wldyq.com\n");
        esc.addSelectJustification(EscCommand.JUSTIFICATION.CENTER);
        // 设置纠错等级
        esc.addSelectErrorCorrectionLevelForQRCode((byte) 0x31);
        // 设置qrcode模块大小
        esc.addSelectSizeOfModuleForQRCode((byte) 4);
        // 设置qrcode内容
        esc.addStoreQRCodeData("https://www.wldyq.com");
        // 打印QRCode
        esc.addPrintQRCode();
        esc.addText("\n");
        esc.addSelectJustification(EscCommand.JUSTIFICATION.LEFT);
        esc.addText("********************************\n");
        esc.addText("时间: " + formattedDateTime + "\n");
        esc.addText("设备: CapnoEasy\n");
        esc.addText("序号: " + macAddress + "\n");
        esc.addText("********************************\n");
        esc.addText("\n");
        esc.addCutPaper();
        if (Printer.getPortManager() != null) {
            try {
                Printer.getPortManager().writeDataImmediately(esc.getCommand());
            } catch (IOException e) {
                e.printStackTrace();
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