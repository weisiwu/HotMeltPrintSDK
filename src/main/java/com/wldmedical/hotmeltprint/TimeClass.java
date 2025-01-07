package com.wldmedical.hotmeltprint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimeClass {
    public String getCurrentFormattedDateTime() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }
}