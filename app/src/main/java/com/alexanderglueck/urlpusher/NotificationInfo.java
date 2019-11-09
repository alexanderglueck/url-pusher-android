package com.alexanderglueck.urlpusher;

import java.io.Serializable;

public class NotificationInfo implements Serializable {
    public String message;

    public NotificationInfo(String message) {
        this.message = message;
    }
}