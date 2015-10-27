package com.example.anurag.awesomepayment.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anurag.awesomepayment.R;
import com.example.anurag.awesomepayment.receivers.WiFiDirectBroadcastReceiver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class WiFiDirectActivity extends AppCompatActivity implements View.OnClickListener, WifiP2pManager.ChannelListener, WifiP2pManager.PeerListListener, WifiP2pManager.ConnectionInfoListener {

    private ListView listView;
    private EditText et_payment;
    private TextView userText;
    private Button b_start_payment;
    private boolean isPaymentInProgress;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    ProgressDialog progressDialog = null;
    private WifiP2pDevice device;
    public static final String TAG = "wifidirectdemo";

    private WifiP2pManager manager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private WifiP2pInfo info;
    private Socket client = null;
    private InputReaderThread inputReaderThread;
    private OutputWriterThread outputWriterThread;
    private boolean isHostType;


    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_direct_activity_layout);

        listView = (ListView) findViewById(R.id.listView);
        et_payment = (EditText) findViewById(R.id.et_payment);
        b_start_payment = (Button) findViewById(R.id.b_start_payment);
        userText = (TextView) findViewById(R.id.userText);
        b_start_payment.setVisibility(View.GONE);
        b_start_payment.setOnClickListener(this);

        et_payment.clearFocus();
        et_payment.setVisibility(View.GONE);
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(et_payment.getWindowToken(), 0);
        b_start_payment.requestFocus();

        addIntentFiltersAndInitManager();
        onInitiateDiscovery();
        addListAdapter();
    }

    private void addIntentFiltersAndInitManager() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
    }

    public void onInitiateDiscovery() {
//        if (!isWifiP2pEnabled) {
//            Toast.makeText(WiFiDirectActivity.this, R.string.p2p_off_warning,
//                    Toast.LENGTH_SHORT).show();
//            if (manager != null && channel != null) {
//                // Since this is the system wireless settings activity, it's
//                // not going to send us a result. We will be notified by
//                // WiFiDeviceBroadcastReceiver instead.
//                startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
//            } else {
//                Log.e(TAG, "channel or manager is null");
//            }
//
//        } else {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(WiFiDirectActivity.this, "Press back to cancel", "finding peers", true,
                true, new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                });
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
            }
        });
//        }
    }

    private void addListAdapter() {
        WiFiPeerListAdapter paymentMethodAdapter = new WiFiPeerListAdapter();
        listView.setAdapter(paymentMethodAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }


    @Override
    public void onPeersAvailable(WifiP2pDeviceList peerList) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        peers.clear();
        peers.addAll(peerList.getDeviceList());
        ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d(WiFiDirectActivity.TAG, "No devices found");
            return;
        }

    }

    private static String getDeviceStatus(int deviceStatus) {
        Log.d(WiFiDirectActivity.TAG, "Peer status :" + deviceStatus);
        switch (deviceStatus) {
            case WifiP2pDevice.AVAILABLE:
                return "Available";
            case WifiP2pDevice.INVITED:
                return "Invited";
            case WifiP2pDevice.CONNECTED:
                return "Connected";
            case WifiP2pDevice.FAILED:
                return "Failed";
            case WifiP2pDevice.UNAVAILABLE:
                return "Unavailable";
            default:
                return "Unknown";
        }
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_start_payment:
                if (client != null && client.isConnected()) {
                    if (!TextUtils.isEmpty(et_payment.getText().toString())) {
                        showDialogMerchantInitPayment();
                    } else {
                        Toast.makeText(WiFiDirectActivity.this, "Please enter money in Rs.", Toast.LENGTH_LONG).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (client != null) {
            if (client.isConnected()) {
                try {
                    client.close();
                } catch (IOException e) {
                    // Give up
                    e.printStackTrace();
                }
            }
        }
    }

    public void resetData() {
        peers.clear();
        ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
    }


    private class WiFiPeerListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return peers.size();
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
                LayoutInflater vi = (LayoutInflater) WiFiDirectActivity.this.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                convertView = vi.inflate(R.layout.peerlist_items, null);
            }
            WifiP2pDevice deviceLocal = peers.get(position);
            if (deviceLocal != null) {
                TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
                TextView device_address = (TextView) convertView.findViewById(R.id.device_address);
                TextView device_status = (TextView) convertView.findViewById(R.id.device_status);
                Button connect_button = (Button) convertView.findViewById(R.id.connect_button);

                device_name.setText(deviceLocal.deviceName);
                device_address.setText(deviceLocal.deviceAddress);
                device_status.setText(getDeviceStatus(deviceLocal.status));

                if (deviceLocal.status == WifiP2pDevice.AVAILABLE) {
                    connect_button.setVisibility(View.VISIBLE);
                    connect_button.setText("Connect");
                    connect_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToDevice(position);
                        }
                    });
                } else if (deviceLocal.status == WifiP2pDevice.CONNECTED) {
                    connect_button.setVisibility(View.VISIBLE);
                    connect_button.setText("Disconnect");
                    connect_button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            disconnectDevice();
                        }
                    });
                } else {
                    connect_button.setVisibility(View.GONE);
                }
            }
            if (device != null && deviceLocal.deviceName.equalsIgnoreCase(device.deviceName)) {
                convertView.setBackgroundColor(Color.GREEN);
            } else {
                convertView.setBackgroundColor(Color.WHITE);
            }
            return convertView;

        }
    }

    private void connectToDevice(int position) {
        WifiP2pConfig config = new WifiP2pConfig();
        device = peers.get(position);
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        progressDialog = ProgressDialog.show(WiFiDirectActivity.this, "Press back to cancel",
                "Connecting to :" + device.deviceName, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
        );

        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void disconnectDevice() {
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {

            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);
            }

            @Override
            public void onSuccess() {
                ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
            }
        });
    }


    public void updateThisDevice(WifiP2pDevice device) {
//        if(device != null){
//            for (int i = 0; i < peers.size(); i++) {
//                if(peers.get(i).deviceAddress.equalsIgnoreCase(device.deviceAddress)){
//                    peers.set(i, device);
//                    ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
//                    break;
//                }
//            }
//        }
//        this.device = device;

//        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
//        view.setText(device.deviceName);
//        view = (TextView) mContentView.findViewById(R.id.my_status);
//        view.setText(getDeviceStatus(device.status));
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
//        ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();

//        this.getView().setVisibility(View.VISIBLE);
//
//        // The owner IP is now known.
//        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
//        view.setText(getResources().getString(R.string.group_owner_text)
//                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
//                : getResources().getString(R.string.no)));
//
//        // InetAddress from WifiP2pInfo struct.
//        view = (TextView) mContentView.findViewById(R.id.device_info);
//        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
//
//        // After the group negotiation, we assign the group owner as the file
//        // server. The file server is single threaded, single connection server
//        // socket.
        if (info.groupFormed && info.isGroupOwner) {

            isHostType = true;

            ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
            et_payment.setVisibility(View.GONE);

            b_start_payment.setClickable(false);
            b_start_payment.setVisibility(View.GONE);

            ConnectSocketAsyncTask connectSocketAsyncTask = new ConnectSocketAsyncTask(WiFiDirectActivity.this, true, "N/A", 0);
            connectSocketAsyncTask.execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            et_payment.setVisibility(View.VISIBLE);
            b_start_payment.setVisibility(View.VISIBLE);
            b_start_payment.setClickable(true);

            if (device != null) {
                b_start_payment.setText("Ask for payment from " + device.deviceName);
            }
            ConnectSocketAsyncTask connectSocketAsyncTask = new ConnectSocketAsyncTask(WiFiDirectActivity.this, false, info.groupOwnerAddress.getHostAddress(), 8988);
            connectSocketAsyncTask.execute();
        }
    }

    private void customerSocketListener() {
        if (inputReaderThread != null && inputReaderThread.isAlive()) {
            inputReaderThread.interrupt();
        }
        inputReaderThread = new InputReaderThread();
        inputReaderThread.start();
    }

    private void merchantSocketListener() {
        if (inputReaderThread != null && inputReaderThread.isAlive()) {
            inputReaderThread.interrupt();
        }
        inputReaderThread = new InputReaderThread();
        inputReaderThread.start();

        if (outputWriterThread != null && outputWriterThread.isAlive()) {
            outputWriterThread.interrupt();
        }
        outputWriterThread = new OutputWriterThread("action-initpayment, money-" + et_payment.getText().toString());
        outputWriterThread.start();
    }


    private class InputReaderThread extends Thread {
        @Override
        public void run() {
            if (client != null && client.isConnected()) {
                try {
                    InputStream is = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuilder sb = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        if (line.equals("~~/START/~~"))
                            sb = new StringBuilder();
                        else if (line.equals("~~/END/~~")) {
                            processInputData(sb.toString());
                            sb.delete(0, sb.length());
                        } else
                            sb.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    private void processInputData(final String input) {
        WiFiDirectActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(WiFiDirectActivity.this, input, Toast.LENGTH_LONG).show();
                if (isHostType) {
                    if (input.contains("action-initpayment")) {
                        showDialogCustomerInitPayment(input);
                    }
                } else {
                    if (input.contains("action-confirmpayment")) {
                        showDialogConfirmPayment(input);
                    }
                }
            }
        });
    }


    private class OutputWriterThread extends Thread {
        String msg;

        public OutputWriterThread(String msg) {
            this.msg = "~~/START/~~\n" + msg + "\n~~/END/~~\n";
        }

        @Override
        public void run() {
            if (client != null && client.isConnected()) {
                try {
                    OutputStream os = client.getOutputStream();
                    transferMoney(new ByteArrayInputStream(msg.getBytes(Charset.forName("UTF-8"))), os);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void showDialogMerchantInitPayment() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                WiFiDirectActivity.this);
        alertDialogBuilder.setTitle("Initiate payment...");
        alertDialogBuilder
                .setMessage("Payment of " + et_payment.getText().toString() + " Rs will be asked from user to pay. Would you like to continue?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        merchantSocketListener();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDialogCustomerInitPayment(String result) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                WiFiDirectActivity.this);
        alertDialogBuilder.setTitle("Please pay to merchant...");
        String digits = result.replaceAll("[^0-9.]", "");
        alertDialogBuilder
                .setMessage("Payment of " + digits + " Rs will be cut from your account. Would you like to procceed?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (outputWriterThread != null && outputWriterThread.isAlive()) {
                            outputWriterThread.interrupt();
                        }
                        outputWriterThread = new OutputWriterThread("action-confirmpayment");
                        outputWriterThread.start();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void showDialogConfirmPayment(String result) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                WiFiDirectActivity.this);
        alertDialogBuilder.setTitle("Payment successful... :)");
        String digits = result.replaceAll("[^0-9.]", "");
        alertDialogBuilder
                .setMessage("Payment of " + digits + " Rs is added to your account :)")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
//                .setNegativeButton("No", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.cancel();
//                    }
//                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public class ConnectSocketAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private boolean isHost;
        private String host = "";
        private int port = 0;

        public ConnectSocketAsyncTask(Context context, boolean isHost, String host, int port) {
            this.context = context;
            this.isHost = isHost;
            this.host = host;
            this.port = port;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                if (client == null || !client.isConnected()) {
                    if (isHost) {
                        ServerSocket serverSocket = new ServerSocket(8988);
                        Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                        client = serverSocket.accept();
                        client.setKeepAlive(true);
                        Log.d(WiFiDirectActivity.TAG, "Server: connection done");
                    } else {
                        Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                        client = new Socket();
                        client.bind(null);
                        client.connect((new InetSocketAddress(host, port)), 5000);
                        client.setKeepAlive(true);
                        Log.d(WiFiDirectActivity.TAG, "Client socket - " + client.isConnected());
                    }
                }
                return "";
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Toast.makeText(context, "Socket is open now", Toast.LENGTH_LONG).show();
            if (isHost) {
                customerSocketListener();
            }
        }
    }

    public static boolean transferMoney(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.flush();
//            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirectActivity.TAG, e.toString());
            return false;
        }
        return true;
    }

    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}
