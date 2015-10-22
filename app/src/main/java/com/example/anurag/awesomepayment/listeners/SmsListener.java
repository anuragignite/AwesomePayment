package com.example.anurag.awesomepayment.listeners;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

/**
 * Created by anurag on 10/20/15.
 */
public class SmsListener extends BroadcastReceiver {

    private SharedPreferences preferences;

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub

        if(intent.getAction().equals(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)){
            Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
            SmsMessage[] msgs = null;
            SmsMessage smsMessage = null;
            String msg_from;


            if(Build.VERSION.SDK_INT >= 19) { //KITKAT
                msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                smsMessage = msgs[0];

            } else {
                Object pdus[] = (Object[]) bundle.get("pdus");
                smsMessage = SmsMessage.createFromPdu((byte[]) pdus[0]);
            }




//            if (bundle != null){
//                //---retrieve the SMS message received---
//                try{
//                    Object[] pdus = (Object[]) bundle.get("pdus");
//                    msgs = new SmsMessage[pdus.length];
//                    for(int i=0; i<msgs.length; i++){
//                        msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);
//                        msg_from = msgs[i].getOriginatingAddress();
//                        String msgBody = msgs[i].getMessageBody();
//                    }
//                }catch(Exception e){
////                            Log.d("Exception caught",e.getMessage());
//                }
//            }
        }
    }
}
