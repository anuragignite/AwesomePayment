package com.example.anurag.awesomepayment.managers;

import android.content.Context;

import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class InternetPaymentManager {


    private static InternetPaymentManager mInternetPaymentManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;

    public static InternetPaymentManager getInstance() {
        if(mInternetPaymentManager == null){
            mInternetPaymentManager = new InternetPaymentManager();
        }
        return mInternetPaymentManager;
    }

    private InternetPaymentManager() {
    }

    public void initPayment(){

    }


    private void findOptimisePaymentMethod(){

    }
}
