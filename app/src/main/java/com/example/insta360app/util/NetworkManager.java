package com.example.insta360app.util;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.widget.Toast;
import com.example.insta360app.R;

import com.arashivision.sdkcamera.camera.InstaCameraManager;
import com.example.insta360app.MyApp;

public class NetworkManager {

    private static class NetworkHolder {
        private static final NetworkManager instance = new NetworkManager();
    }

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        return NetworkHolder.instance;
    }

    private long mMobileNetId = -1;

    private ConnectivityManager.NetworkCallback mNetworkCallback = null;


    // Bind Mobile Network
    public void exchangeNetToMobile() {
        if (isBindingMobileNetwork()) {
            return;
        }

        ConnectivityManager connManager = (ConnectivityManager) MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        Network[] networks = connManager.getAllNetworks();
        for (Network network : networks) {
            NetworkInfo networkInfo = connManager.getNetworkInfo(network);
            if (networkInfo != null && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                // Need to set network Id of current wifi to camera
                InstaCameraManager.getInstance().setNetIdToCamera(getNetworkId(network));
            }
        }

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build();
        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                super.onAvailable(network);
                boolean bindSuccessful;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connManager.bindProcessToNetwork(null);
                    bindSuccessful = connManager.bindProcessToNetwork(network);
                } else {
                    ConnectivityManager.setProcessDefaultNetwork(null);
                    bindSuccessful = ConnectivityManager.setProcessDefaultNetwork(network);
                }
                // Record the bound mobile network ID
                mMobileNetId = getNetworkId(network);
                if (bindSuccessful) {
                    Toast.makeText(MyApp.getInstance(), R.string.live_toast_bind_mobile_network_successful, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MyApp.getInstance(), R.string.live_toast_bind_mobile_network_failed, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLost(Network network) {
                super.onLost(network);
                // The mobile network is suddenly unavailable, need to temporarily unbind and wait for the network to recover again
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    connManager.bindProcessToNetwork(null);
                } else {
                    ConnectivityManager.setProcessDefaultNetwork(null);
                }
                Toast.makeText(MyApp.getInstance(), R.string.live_toast_unbind_mobile_network_when_lost, Toast.LENGTH_SHORT).show();
            }
        };
        connManager.requestNetwork(request, mNetworkCallback);
    }

    private long getNetworkId(Network network) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return network.getNetworkHandle();
        }
        return Long.parseLong(network.toString());
    }


    // Unbind Mobile Network
    public void clearBindProcess() {
        ConnectivityManager connManager = (ConnectivityManager) MyApp.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            connManager.bindProcessToNetwork(null);
        } else {
            ConnectivityManager.setProcessDefaultNetwork(null);
        }
        if (mNetworkCallback != null) {
            // 注销callback，彻底解除绑定
            // Unregister Callback, Unbind completely
            connManager.unregisterNetworkCallback(mNetworkCallback);
            Toast.makeText(MyApp.getInstance(), R.string.live_toast_unbind_mobile_network, Toast.LENGTH_SHORT).show();
        }
        mNetworkCallback = null;
        mMobileNetId = -1;
        // 重置相机网络
        // Reset Camera Net Id
        InstaCameraManager.getInstance().setNetIdToCamera(-1);
    }

    public long getMobileNetId() {
        return mMobileNetId;
    }

    public boolean isBindingMobileNetwork() {
        return mNetworkCallback != null;
    }

}
