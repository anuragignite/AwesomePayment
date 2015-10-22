package com.example.anurag.awesomepayment.managers;

import android.content.Context;

import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class SMSReadingManager {


    private static SMSReadingManager mSMSReadingManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;

    public static SMSReadingManager getInstance() {
        if(mSMSReadingManager == null){
            mSMSReadingManager = new SMSReadingManager();
        }
        return mSMSReadingManager;
    }

    private SMSReadingManager() {
    }

    public void initPayment(){

    }


    private void findOptimisePaymentMethod(){

    }
}
