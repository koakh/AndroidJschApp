package com.koakh.jschapp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.koakh.jschapp.util.NetworkUtil;

public class NetworkChangeReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(final Context context, final Intent intent) {
    String status = NetworkUtil.getConnectivityStatusString(context);
    Toast.makeText(context, status, Toast.LENGTH_SHORT).show();
  }
}