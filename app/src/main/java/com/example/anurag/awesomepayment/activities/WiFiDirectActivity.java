package com.example.anurag.awesomepayment.activities;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (ListView) findViewById(R.id.listView);
        et_payment = (EditText) findViewById(R.id.et_payment);
        b_start_payment = (Button) findViewById(R.id.b_start_payment);
        userText = (TextView) findViewById(R.id.userText);
        b_start_payment.setOnClickListener(this);


        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);


        WiFiPeerListAdapter paymentMethodAdapter = new WiFiPeerListAdapter();
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

    @Override
    public void onChannelDisconnected() {

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
//        this.info = info;
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
//        if (info.groupFormed && info.isGroupOwner) {
//            new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text))
//                    .execute();
//        } else if (info.groupFormed) {
//            // The other device acts as the client. In this case, we enable the
//            // get file button.
//            mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
//            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
//                    .getString(R.string.client_text));
//        }
//
//        // hide the connect button
//        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

}
