package com.example.anurag.awesomepayment.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.anurag.awesomepayment.R;
import com.example.anurag.awesomepayment.listeners.Constants;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private EditText et_payment;
    private Button b_start_payment;
    private String[] paymentTypeArray = {"NFC", "WIFI-DIRECT", "QRCODE", "SMS", "INTERNET"};
    private boolean isPaymentInProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        et_payment = (EditText) findViewById(R.id.et_payment);
        b_start_payment = (Button) findViewById(R.id.b_start_payment);
        b_start_payment.setOnClickListener(this);


        PaymentMethodAdapter paymentMethodAdapter = new PaymentMethodAdapter();
        listView.setAdapter(paymentMethodAdapter);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_start_payment:
                isPaymentInProgress = true;
                b_start_payment.setText("Payment in progress...");
//
                break;
            default:
//
                break;
        }
    }


    private class PaymentMethodAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return paymentTypeArray.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MainActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
            textView.setText(paymentTypeArray[position]);

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleClickOfListView(paymentTypeArray[position]);
                }
            });

            return convertView;
        }
    }

    private void handleClickOfListView(String type) {
        if(type.equalsIgnoreCase("NFC")){

        } else if(type.equalsIgnoreCase("WIFI-DIRECT")) {
            Intent intent = new Intent(MainActivity.this, WiFiDirectActivity.class);
            startActivity(intent);
        } else if(type.equalsIgnoreCase("QRCODE")) {

        } else if(type.equalsIgnoreCase("SMS")) {

        } else if(type.equalsIgnoreCase("INTERNET")) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case Constants.ACTIVITY_FOR_RESULT_NFC:
                    break;
                case Constants.ACTIVITY_FOR_RESULT_QRCODE:
                    break;
                case Constants.ACTIVITY_FOR_RESULT_WIFIDIRECT:
                    break;
                default:
                    break;
            }
        }
    }
}
