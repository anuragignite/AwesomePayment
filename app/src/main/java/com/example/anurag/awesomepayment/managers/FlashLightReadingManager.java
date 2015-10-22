package com.example.anurag.awesomepayment.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class FlashLightReadingManager {


    private static FlashLightReadingManager mFlashLightReadingManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;

    public static FlashLightReadingManager getInstance() {
        if(mFlashLightReadingManager == null){
            mFlashLightReadingManager = new FlashLightReadingManager();
        }
        return mFlashLightReadingManager;
    }

    private FlashLightReadingManager() {
    }

    public void initPayment(){

    }


}
