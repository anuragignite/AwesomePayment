package com.example.anurag.awesomepayment.managers;

import android.content.Context;

import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class WiFiDirectManager {

    private OnPaymentCompleted onPaymentCompleted;

    private static WiFiDirectManager mWIFIDirectManager;
    private Context mContext;

    public static WiFiDirectManager getInstance() {
        if(mWIFIDirectManager == null){
            mWIFIDirectManager = new WiFiDirectManager();
        }
        return mWIFIDirectManager;
    }

    private WiFiDirectManager() {
    }

    public void initPayment(){

    }

    private void findOptimisePaymentMethod(){

    }
}
