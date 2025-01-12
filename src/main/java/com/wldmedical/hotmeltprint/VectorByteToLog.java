package com.wldmedical.hotmeltprint;

import android.util.Log;
import java.util.ArrayList;
import java.util.Vector;

public class VectorByteToLog {
    public static void logVectorByteDecimal(Vector<Byte> byteVector, String tag) {
        if (byteVector == null || byteVector.isEmpty()) {
            Log.d(tag, "Vector is empty or null.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < byteVector.size(); i++) {
            sb.append(byteVector.get(i).byteValue() & 0xFF); // & 0xFF 处理负数
            if (i < byteVector.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        Log.d(tag, "Decimal representation: " + sb.toString());
    }
    public static void logArrayListByteDecimal(ArrayList<Byte> byteArrayList, String tag) {
        if (byteArrayList == null || byteArrayList.isEmpty()) {
            Log.d(tag, "ArrayList is empty or null.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < byteArrayList.size(); i++) {
            sb.append(byteArrayList.get(i).byteValue() & 0xFF); // & 0xFF 处理负数
            if (i < byteArrayList.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        Log.d(tag, "Decimal representation: " + sb.toString());
    }

    public static void main(String[] args) {
        Vector<Byte> byteVector = new Vector<>();
        byteVector.add((byte) 1);
        byteVector.add((byte) 10);
        byteVector.add((byte) -1);
        logVectorByteDecimal(byteVector, "TestTag");

        ArrayList<Byte> byteArrayList = new ArrayList<>();
        byteArrayList.add((byte) 1);
        byteArrayList.add((byte) 10);
        byteArrayList.add((byte) -1);
        logArrayListByteDecimal(byteArrayList, "TestTag");
    }
}