package com.koakh.jschapp.broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.koakh.jschapp.background.ServiceUpload;

/**
 * Created by mario on 07-01-2016.
 * http://stackoverflow.com/questions/15350998/determine-addaction-click-for-android-notifications
 */

public class NotificationReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {

    String action = intent.getAction();

    if (ServiceUpload.ACTION_INFO.equals(action)) {
      Toast.makeText(context, "ACTION_INFO", Toast.LENGTH_SHORT).show();
    }
    else if (ServiceUpload.ACTION_MEDIA.equals(action)) {
      Toast.makeText(context, "ACTION_MEDIA", Toast.LENGTH_SHORT).show();
    }
    else if (ServiceUpload.ACTION_SYNC.equals(action)) {
      Toast.makeText(context, "ACTION_SYNC", Toast.LENGTH_SHORT).show();
    }
  }
}
