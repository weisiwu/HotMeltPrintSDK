package com.wldmedical.hotmeltprint;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.gprinter.bean.PrinterDevices;
import com.gprinter.command.EscCommand;
import com.gprinter.utils.Command;
import com.gprinter.utils.ConnMethod;

import android.bluetooth.BluetoothAdapter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.content.Context;
import android.os.Build.VERSION;

public class HotmeltPinter extends AbstractHotmeltPinter {
    private final Printer printer = Printer.getInstance();
    private final EscCommand esc = new EscCommand();
    private static final long queryInterval = 5050;
    private String macAddress = "";
    TimeClass myObject = new TimeClass();
    String formattedDateTime = myObject.getCurrentFormattedDateTime();
    private static final String TAG = "WLDHotmeltPinter";
    // 使用线程安全的 ConcurrentLinkedQueue 存储数据，允许多线程添加
    private final ConcurrentLinkedQueue<Float> waveformData = new ConcurrentLinkedQueue<>();
    
    /**
     * 链接打印机
     * 1. 按照传入的mac地址，自动连接打印机设备
     */
    @Override
    public void connect(String mac, Context context, final boolean autoPrint) { // autoPrint 声明为 final
        try {
            BluetoothScanner scanner = new BluetoothScanner(context);
//            macAddress = scanner.scanForGPDevice();
            macAddress = "41:17:3A:F0:BF:9A";
            System.out.println("wswTest 开始链接打印机----->" + macAddress);
            PrinterDevices blueTooth = new PrinterDevices.Build()
                .setContext(context)
                .setConnMethod(ConnMethod.BLE_BLUETOOTH)
                .setMacAddress(macAddress)
                .setCommand(Command.ESC)
                .build();

            printer.connect(blueTooth, new Runnable() { // 转换为匿名内部类
                @Override
                public void run() {
                    if (autoPrint) {
                        print();
                    }
                }
            });
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
        clearWaveformData();
        headerPrint();
        wavePrintByTimer();
    }

    /**
     * 数据更新入口，C++中将获取的数据通过此函数传递过来
    */
    @Override
    public void update(float data) {
        waveformData.offer(data); // 使用 offer() 方法添加，线程安全
    }

    private void clearWaveformData(){
        waveformData.clear();
    }

    /**
     * 检查蓝牙打印机状态
     */
    private boolean checkDeviceStatus() {
        return Printer.getConnectState();
    }

    /**
     * 打印小票头部
     */
    private void headerPrint() {
        esc.addInitializePrinter();
        esc.addSetLineSpacing((byte) 100);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
         InputStream imageStream = classLoader.getResourceAsStream("images/logo.png");
         if (imageStream != null) {
             Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
             if (bitmap != null) {
                 // 打印图片,58打印机图片宽度最大为384dot  1mm=8dot 用尺子量取图片的宽度单位为Xmm  传入宽度值为 X*8
                 esc.drawImage(bitmap, 384);
             } else {
                 System.out.println("Failed to decode Bitmap");
             }
         } else {
             System.out.println("Failed to load resource");
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

        // 小票头部立刻打印
        if (printer.getPortManager() != null) {
            try {
                printer.getPortManager().writeDataImmediately(esc.getCommand());
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
        List<Float> dataToPrint = new ArrayList<>();
        // 从线程安全队列中取出数据，并限制数量
        int count = 0;
        while (count < 500 && !waveformData.isEmpty()) {
            Float data = waveformData.poll();//使用poll()方法，线程安全
            if(data != null) {
                dataToPrint.add(data);
                count++;
            }
        }
        if (!dataToPrint.isEmpty()) {
            Bitmap bitmap = generateWaveformBitmap(dataToPrint);
            if (esc != null) {
                esc.drawImage(bitmap);
                if (printer != null && printer.getPortManager() != null) {
                    try {
                        printer.getPortManager().writeDataImmediately(esc.getCommand());
                    } catch (IOException e) {
                        Log.e(TAG, "writeDataImmediately error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } else {
            System.out.println("No waveform data available, waiting...");
        }
    }

    /**
     * 打印波形，定时器调用
     */
    private void wavePrintByTimer() {
         // 使用 ScheduledExecutorService 模拟协程的定时执行
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                wavePrint();
            }
        }, 0, queryInterval, TimeUnit.MILLISECONDS);
    }

    private Bitmap generateWaveformBitmap(List<Float> data) {
        int width = 400;
        int height = 400;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2f);
        paint.setAntiAlias(true);

        float pointSpacing = (float) width / data.size();

        for (int i = 0; i < data.size() - 1; i++) {
            float startX = (float) (i * pointSpacing);
            float startY = data.get(i) * 5;
            float stopX = (float) ((i + 1) * pointSpacing);
            float stopY = data.get(i + 1) * 5;

            canvas.drawLine(startY, startX, stopY, stopX, paint);
        }

        return bitmap;
    }
}