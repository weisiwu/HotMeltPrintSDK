package com.wldmedical.hotmeltprint;

import android.content.Context;

public interface HotmeltPinterInterface {

    void connect(String mac, Context context, boolean autoPrint);

    void print();
}
