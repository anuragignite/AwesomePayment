package com.example.anurag.awesomepayment.managers;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.example.anurag.awesomepayment.managers.PaymentManager.OnPaymentCompleted;

/**
 * Created by anurag on 10/20/15.
 */
public class SoundReadingManager {


    private static SoundReadingManager mSoundReadingManager;
    private Context mContext;
    private OnPaymentCompleted onPaymentCompleted;


    public static SoundReadingManager getInstance() {
        if(mSoundReadingManager == null){
            mSoundReadingManager = new SoundReadingManager();
        }
        return mSoundReadingManager;
    }

    private SoundReadingManager() {
    }

    public void initPayment(){

    }


    private void findOptimisePaymentMethod(){

    }

}
