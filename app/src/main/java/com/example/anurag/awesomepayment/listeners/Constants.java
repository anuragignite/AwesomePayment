package com.example.anurag.awesomepayment.listeners;

/**
 * Created by anurag on 10/20/15.
 */
public class Constants {
    enum PaymentMethods {
        NFC,
        QR,
        SMS,
        SOUND,
        FLASHLIGHT,
        INTERNET,
        WIFIDIRECT
    }

    public static final int ACTIVITY_FOR_RESULT_NFC = 300;
    public static final int ACTIVITY_FOR_RESULT_WIFIDIRECT = 301;
    public static final int ACTIVITY_FOR_RESULT_QRCODE = 302;
}
