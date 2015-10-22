package com.example.anurag.awesomepayment.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by anurag on 10/20/15.
 */
public class PaymentManager {


    private static PaymentManager mPaymentManager;
    private Context mContext;

    public static PaymentManager getInstance() {
        if(mPaymentManager == null){
            mPaymentManager = new PaymentManager();
        }
        return mPaymentManager;
    }

    private PaymentManager() {
    }

    public void initPayment(){

    }


    private void findOptimisePaymentMethod(){

    }


    public interface OnPaymentCompleted{
        public void onPaymentSuccessful();
        public void onPaymentFailed(String reason);
    }


    private boolean checkForInternetConnectivity(){
        ConnectivityManager cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
