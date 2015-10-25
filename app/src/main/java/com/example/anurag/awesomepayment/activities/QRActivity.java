package com.example.anurag.awesomepayment.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.anurag.awesomepayment.R;
import com.example.anurag.awesomepayment.listeners.Constants;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;

import org.w3c.dom.Text;

public class QRActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listView;
    private EditText et_payment;
    private Button b_start_payment;
    private String[] paymentTypeArray = {"NFC", "WIFI-DIRECT", "QRCODE", "SMS", "INTERNET"};
    private boolean isPaymentInProgress;
    private TextView textView;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_activity_layout);

        textView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView.setOnClickListener(this);

        generateQRCode("This user has paid 1000 rs");

    }


    private void generateQRCode(String qrString) {
        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(qrString, BarcodeFormat.QR_CODE, 512, 512);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            imageView.setImageBitmap(bmp);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.textView:
//                IntentIntegrator integrator = new IntentIntegrator(QRActivity.this);
////                integrator.addExtra("SCAN_WIDTH", 640);
////                integrator.addExtra("SCAN_HEIGHT", 480);
//                integrator.addExtra("SCAN_MODE", "QR_CODE_MODE,PRODUCT_MODE");
//                //customize the prompt message before scanning
//                integrator.addExtra("PROMPT_MESSAGE", "Scanner Start!");
//                integrator.initiateScan(IntentIntegrator.PRODUCT_CODE_TYPES);

                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.putExtra("com.google.zxing.client.android.SCAN.SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
                break;
            default:
//
                break;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            String contents = intent.getStringExtra("SCAN_RESULT");
            String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
            textView.setText(contents);

            // Handle successful scan
        } else if (resultCode == RESULT_CANCELED) {
            // Handle cancel
            textView.setText("Nothing found");
        }
//        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
//        if (result != null) {
//            String contents = result.getContents();
//            if (contents != null) {
//                textView.setText(contents);
//            } else {
//                textView.setText("Nothing found");
//            }
//        }
    }
}
