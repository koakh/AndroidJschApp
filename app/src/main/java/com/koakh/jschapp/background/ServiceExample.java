package com.koakh.jschapp.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;

/**
 * Created by mario on 09/10/2014.
 * http://www.codeproject.com/Articles/825693/Article-Beginners-Guide-to-Android-Services
 */
public class ServiceExample extends Service {

  boolean mAllowRebind;
  private Singleton mApp;

  @Override
  public void onCreate() {
    super.onCreate();

    //Get Singleton
    mApp = Singleton.getInstance();

    Log.d(mApp.TAG, "Service Created");
    Toast.makeText(this, "Service Created", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Log.d(mApp.TAG, "Service Destroyed");
    Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();

    Log.d(mApp.TAG, "Service Low Memory");
    Toast.makeText(this, "Service Low Memory", Toast.LENGTH_SHORT).show();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.d(mApp.TAG, "Service Start Command");
    Toast.makeText(this, "Service Start Command", Toast.LENGTH_SHORT).show();

    Bundle extras = intent.getExtras();
    String imageURL = extras.getString("ImageFileURL");
    String fileName = extras.getString("FileName");
    String[] fileList = extras.getStringArray("FileList");

    Log.d(mApp.TAG, String.format("Bundle: %s, %s", imageURL, fileName));
    for (int i = 0; i < fileList.length; i++) {
      Log.d(mApp.TAG, String.format("file: %s", fileList[i]));
    }

    //Launch a lengthy operation
    testProgressNotification();

    return super.onStartCommand(intent, flags, startId);
  }

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public boolean onUnbind(Intent intent) {
    return mAllowRebind;
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);
  }

  /**
   * Test ProgressBar Notification
   * Displaying Progress in a Notification
   * http://developer.android.com/training/notify-user/display-progress.html
   * http://javatechig.com/android/progress-notification-in-android-example
   */
  public void testProgressNotification() {

    final NotificationManager notifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    final Notification.Builder notificationBuilder = (new Notification.Builder(this));

    notificationBuilder.setContentTitle("testProgressNotification")
      .setContentText("test in progress...")
      .setSmallIcon(R.drawable.ic_download);

    // Start a lengthy operation in a background thread
    new Thread(
      new Runnable() {
        @Override
        public void run() {
          int incr;
          // Do the "lengthy" operation 20 times/progress steps
          for (incr = 0; incr <= 100; incr += 5) {
            // Sets the progress indicator to a max value, the
            // current completion percentage, and "determinate"
            // state
            notificationBuilder.setProgress(100, incr, false);
            // Displays the progress bar for the first time.
            notifyManager.notify(mApp.NOTIFICATION_UNIQUE_ID, notificationBuilder.build());
            // Sleeps the thread, simulating an operation
            // that takes time
            try {
              // Sleep for 1 seconds
              Thread.sleep(1 * 2000);
            } catch (InterruptedException e) {
              Log.d(mApp.TAG, "Sleep failure");
            }
          }
          // When the loop is finished, updates the notification
          notificationBuilder.setContentText("Download complete")
            // Removes the progress bar
            .setProgress(0, 0, false);
          notifyManager.notify(mApp.NOTIFICATION_UNIQUE_ID, notificationBuilder.build());
        }
      }
      // Starts the thread by calling the run() method in its Runnable
    ).start();
  }
}