package com.example.anurag.awesomepayment.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class NFCManager {


    private static NFCManager mNFCManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;

    public static NFCManager getInstance() {
        if(mNFCManager == null){
            mNFCManager = new NFCManager();
        }
        return mNFCManager;
    }

    private NFCManager() {
    }

    public void initPayment(){

    }

    private void findOptimisePaymentMethod(){

    }

}
