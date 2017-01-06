package com.koakh.jschapp.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.koakh.jschapp.R;

//Android Internet Connection Status & Network Change Receiver Example
//http://viralpatel.net/blogs/android-internet-connection-status-network-change/
public class NetworkUtil {

  public static int TYPE_WIFI = 1;
  public static int TYPE_MOBILE = 2;
  public static int TYPE_NOT_CONNECTED = 0;

  public static int getConnectivityStatus(Context context) {

    ConnectivityManager cm = (ConnectivityManager) context
      .getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    if (activeNetwork != null) {
      if(activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
        return TYPE_WIFI;

      if(activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
        return TYPE_MOBILE;
    }

    return TYPE_NOT_CONNECTED;
  }

  public static String getConnectivityStatusString(Context context) {

    int conn = NetworkUtil.getConnectivityStatus(context);
    String status = getConnectivityStatusString(context, conn);

    return status;
  }

  public static String getConnectivityStatusString(Context context, int conn) {

    String status = null;
    if (conn == NetworkUtil.TYPE_WIFI) {
      status = context.getResources().getString(R.string.global_connectivity_status_wifi);//"Wifi enabled";
    } else if (conn == NetworkUtil.TYPE_MOBILE) {
      status = context.getResources().getString(R.string.global_connectivity_status_mobile);//"Mobile data enabled";
    } else if (conn == NetworkUtil.TYPE_NOT_CONNECTED) {
      status = context.getResources().getString(R.string.global_connectivity_status_not_connected);//"Not connected to Internet";
    }
    return status;
  }
}
