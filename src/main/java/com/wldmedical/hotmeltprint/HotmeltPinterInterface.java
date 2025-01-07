package com.wldmedical.hotmeltprint;

public interface HotmeltPinterInterface {

    default void connect(String mac, boolean autoPrint) {
        // 默认实现（可选）
        System.out.println("Default connect implementation in Java: mac=" + mac + ", autoPrint=" + autoPrint);
    }

    default void print() {
        // 默认实现（可选）
        System.out.println("Default print implementation in Java");
    }
}