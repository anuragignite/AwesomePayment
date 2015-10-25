package com.example.anurag.awesomepayment.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.anurag.awesomepayment.R;
import com.example.anurag.awesomepayment.listeners.Constants;
import com.example.anurag.awesomepayment.receivers.WiFiDirectBroadcastReceiver;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
    private String[] paymentTypeArray = {"NFC", "WIFI-DIRECT", "QRCODE", "SMS", "INTERNET"};
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
        b_start_payment.setOnClickListener(this);

        et_payment.clearFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(et_payment.getWindowToken(), 0);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);


        onInitiateDiscovery();

        WiFiPeerListAdapter paymentMethodAdapter = new WiFiPeerListAdapter();
        listView.setAdapter(paymentMethodAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(TextUtils.isEmpty(et_payment.getText().toString())){
                    Toast.makeText(WiFiDirectActivity.this, "Please input money", Toast.LENGTH_LONG).show();
                    return;
                }

                WifiP2pConfig config = new WifiP2pConfig();
                device = peers.get(position);
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(WiFiDirectActivity.this, "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
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
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.b_start_payment:
//                isPaymentInProgress = true;
//                b_start_payment.setText("Payment in progress...");
//
                break;
            default:
//
                break;
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


    public void resetData() {
        peers.clear();
        ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
    }


    public void updateThisDevice(WifiP2pDevice device) {
        if(device != null){
            for (int i = 0; i < peers.size(); i++) {
                if(peers.get(i).deviceAddress.equalsIgnoreCase(device.deviceAddress)){
                    peers.set(i, device);
                    ((WiFiPeerListAdapter) listView.getAdapter()).notifyDataSetChanged();
                    break;
                }
            }
        }
//        TextView view = (TextView) mContentView.findViewById(R.id.my_name);
//        view.setText(device.deviceName);
//        view = (TextView) mContentView.findViewById(R.id.my_status);
//        view.setText(getDeviceStatus(device.status));
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
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) WiFiDirectActivity.this.getSystemService(
                        Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(android.R.layout.simple_list_item_1, null);
            }
            WifiP2pDevice device = peers.get(position);
            if (device != null) {
                TextView top = (TextView) v.findViewById(android.R.id.text1);
//                TextView bottom = (TextView) v.findViewById(R.id.device_details);
                if (top != null) {
                    top.setText(device.deviceName);
                }
//                if (bottom != null) {
//                    bottom.setText(getDeviceStatus(device.status));
//                }
            }

            return v;

        }
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

    public void onInitiateDiscovery() {
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


    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
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
//            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
//                    .execute();
            b_start_payment.setVisibility(View.GONE);
            if(info != null){
                SendDataAsyncTask sendDataAsyncTask = new SendDataAsyncTask("We request payment of 1000", info.groupOwnerAddress.getHostAddress(), 8988);
                sendDataAsyncTask.execute();
            }


        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            b_start_payment.setVisibility(View.GONE);
            ReceiveDataAsyncTask receiveDataAsyncTask = new ReceiveDataAsyncTask();
            receiveDataAsyncTask.execute();

//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
        }
//
//        // hide the connect button
//        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }




    public class ReceiveDataAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context = WiFiDirectActivity.this;

//        public ReceiveDataAsyncTask(Context context, View statusText) {
//            this.context = context;
//        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");
//                final File f = new File(Environment.getExternalStorageDirectory() + "/"
//                        + context.getPackageName() + "/wifip2pshared-" + System.currentTimeMillis()
//                        + ".jpg");

//                File dirs = new File(f.getParent());
//                if (!dirs.exists())
//                    dirs.mkdirs();
//                f.createNewFile();

//                Log.d(WiFiDirectActivity.TAG, "server: copying files " + f.toString());
                InputStream inputstream = client.getInputStream();
                String response = convertStreamToString(inputstream);
//                copyFile(inputstream, new FileOutputStream(f));
                serverSocket.close();
//                return f.getAbsolutePath();
                return response;
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
                return null;
            }
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
//                statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Please pay to merchant...");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Payment of 1000 Rs will be cut from your account. Would you like to procceed?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing


                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing

                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
//            statusText.setText("Opening a server socket");
        }

    }


    public class SendDataAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context = WiFiDirectActivity.this;
        private String message = "";
        private String host = "";
        private int port = 0;

        /**
         * @param context
         * @param statusText
         */
        public SendDataAsyncTask(String message, String host, int port) {
            this.message = message;
            this.host = host;
            this.port = port;
        }

        @Override
        protected String doInBackground(Void... params) {
            Socket socket = new Socket();

            try {
                Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), 5000);

                Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
//                try {
//                    is = cr.openInputStream(Uri.parse(fileUri));
//                } catch (FileNotFoundException e) {
//                    Log.d(WiFiDirectActivity.TAG, e.toString());
//                }
                is = new ByteArrayInputStream("User has authorised payment of 1000".getBytes(Charset.forName("UTF-8")));

                transferMoney(is, stream);
                Log.d(WiFiDirectActivity.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirectActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
            return "";

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
//                statusText.setText("File copied - " + result);
//                Intent intent = new Intent();
//                intent.setAction(android.content.Intent.ACTION_VIEW);
//                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
//                context.startActivity(intent);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set title
                alertDialogBuilder.setTitle("Accept Payment from user..");

                // set dialog message
                alertDialogBuilder
                        .setMessage("Payment of 1000 Rs will be received in your account. Would you like to procceed?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing


                                dialog.cancel();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing

                                dialog.cancel();
                            }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }

        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
//            statusText.setText("Opening a server socket");
        }

    }

    public static boolean transferMoney(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
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
