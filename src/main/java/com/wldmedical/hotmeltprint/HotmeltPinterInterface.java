package com.wldmedical.hotmeltprint;

import android.content.Context;

public interface HotmeltPinterInterface {
    void connect(String mac, boolean autoPrint);

    void print();

    void update(float data);
}
