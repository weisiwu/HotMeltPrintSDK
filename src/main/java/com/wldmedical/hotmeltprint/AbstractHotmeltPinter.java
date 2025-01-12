package com.wldmedical.hotmeltprint;

import android.content.Context;

public abstract class AbstractHotmeltPinter implements HotmeltPinterInterface {
    @Override
    public void connect(String mac, Context context, boolean autoPrint) {
        System.out.println("Default connect implementation in Java: mac=" + mac + ", autoPrint=" + autoPrint);
    }

    @Override
    public void print() {
        System.out.println("Default print implementation in Java");
    }
}