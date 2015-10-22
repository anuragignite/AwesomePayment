package com.example.anurag.awesomepayment.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class QRCodeManager {


    private static QRCodeManager mQRCodeManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;


    public static QRCodeManager getInstance() {
        if(mQRCodeManager == null){
            mQRCodeManager = new QRCodeManager();
        }
        return mQRCodeManager;
    }

    private QRCodeManager() {
    }

    public void initPayment(){

    }


    private void findOptimisePaymentMethod(){

    }

}
