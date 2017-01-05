package com.koakh.jschapp.background;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.network.Jsch;
import com.koakh.jschapp.thread.JschAsyncTask;
import com.koakh.jschapp.thread.ScanContentAsyncTask;
import com.koakh.jschapp.ui.activity.MainActivity;
import com.koakh.jschapp.util.NetworkUtil;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by mario on 29/11/2014.
 * http://www.codeproject.com/Articles/820238/Article-Beginners-Guide-to-Android-Services
 */

public class ServiceUpload extends Service {

  //Constants
  public final static String ACTION_INFO = "com.koakh.jschapp.ACTION_INFO";
  public final static String ACTION_MEDIA = "com.koakh.jschapp.ACTION_MEDIA";
  public final static String ACTION_SYNC = "com.koakh.jschapp.ACTION_SYNC";
  //Members
  private Singleton mApp;
  private Context mContext;
  private JschAsyncTask mJschAsyncTask;
  private ScanContentAsyncTask mScanContentAsyncTask;

  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();
  // Random number generator
  private final Random mGenerator = new Random();

  @Override
  public void onCreate() {
    super.onCreate();

    //Get Application Singleton
    mApp = Singleton.getInstance();
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {

    Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();

    //Get Singleton References
    mContext = mApp.getAppContext();
    //Enable Singleton IsServiceUploadRunning
    mApp.setIsServiceUploadRunning(true);

    //Get Bundle Extras
    Bundle extras = intent.getExtras();
    ArrayList<String> fileList = extras.getStringArrayList("FileList");
    String server = intent.getStringExtra("Server");
    int serverPort = intent.getIntExtra("Port", 22);
    String serverUsername = intent.getStringExtra("Username");
    String serverPassword = intent.getStringExtra("Password");

    //Prepare PendingIntent to Call MainActivity, used to call activity when press notification
    Intent notificationIntent = new Intent(mContext, MainActivity.class);
    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

    //Trick is use getBroadcast and not getService or getActivity, else it dont send the PendingItent to BroadcastReceiver
    //http://developer.android.com/reference/android/app/PendingIntent.html#getBroadcast%28android.content.Context,%20int,%20android.content.Intent,%20int%29

    //TODO
    //Try getService to communication with service

    //Private request code for the sender
    int requestCode = 12345;
    //Action Info Intent/PendingIntents
    Intent infoIntent = new Intent().setAction(ServiceUpload.ACTION_INFO);
    PendingIntent infoPendingIntent = PendingIntent.getBroadcast(this, requestCode, infoIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    //Action Sync Intent/PendingIntents
    Intent syncIntent = new Intent().setAction(ServiceUpload.ACTION_SYNC);
    PendingIntent syncPendingIntent = PendingIntent.getBroadcast(this, requestCode, syncIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    //Action Media Intent/PendingIntents
    Intent mediaIntent = new Intent().setAction(ServiceUpload.ACTION_MEDIA);
    PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, requestCode, mediaIntent, PendingIntent.FLAG_UPDATE_CURRENT);

    //Initialize NotificationBuilder
    NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    Notification.Builder notificationBuilder = new Notification.Builder(mContext);
    //Assign local members to Application Singleton
    mApp.setNotificationManager(notificationManager);
    mApp.setNotificationBuilder(notificationBuilder);

    //Initialize Notification
    notificationBuilder
      .setPriority(Notification.PRIORITY_HIGH)
      .setContentTitle(mContext.getString(R.string.app_name))
      .setContentText(mContext.getString(R.string.global_service_running))
      .setSmallIcon(R.drawable.ic_download)
      .setContentIntent(pendingIntent)
      // Add media control buttons that invoke intents in your media service
      .addAction(R.drawable.ic_info, getString(R.string.global_service_action_info), infoPendingIntent)
      .addAction(R.drawable.ic_media, getString(R.string.global_service_action_media), mediaPendingIntent)
      .addAction(R.drawable.ic_cloud, getString(R.string.global_service_action_sync), syncPendingIntent)
    ;

    //Build Notification Object to Assign to OnBind
    Notification notification = notificationBuilder.build();

    //TODO
    //Launch AsyncTask
    //asyncTaskJschStart(fileList, server, serverPort, serverUsername, serverPassword);

    //Start Service in ForeGround with OnGoing Notification
    startForeground(mApp.NOTIFICATION_UNIQUE_ID, notification);

    // We want this service to continue running until it is explicitly
    // stopped, so return sticky.
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

    asyncTaskJschStop();

    //Disable Singleton IsServiceUploadRunning
    mApp.setIsServiceUploadRunning(false);
  }

  @Override
  public IBinder onBind(Intent intent) {

    Toast.makeText(this, "Service Bound", Toast.LENGTH_LONG).show();

    return mBinder;
  }

  @Override
  public boolean onUnbind(Intent intent) {

    Toast.makeText(this, "Service UnBind", Toast.LENGTH_LONG).show();

    return super.onUnbind(intent);
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);

    Toast.makeText(this, "Service OnReBind", Toast.LENGTH_LONG).show();
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  /**
   * Class used for the client Binder.  Because we know this service always
   * runs in the same process as its clients, we don't need to deal with IPC.
   * <p/>
   * The LocalBinder provides the getService() method for clients to retrieve the current instance
   * of LocalService. This allows clients to call public methods in the service.
   * For example, clients can call getRandomNumber() from the service.
   */
  public class LocalBinder extends Binder {
    public ServiceUpload getService() {
      // Return this instance of LocalService so clients can call public methods
      return ServiceUpload.this;
    }
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  /**
   * Start JschAsyncTask
   *
   * @param pFileList Array of Files to Process
   */
  public void asyncTaskJschStart(ArrayList<String> pFileList, String pServer, int pServerPort, String pUsername, String pPassword) {
    try {
      int status = NetworkUtil.getConnectivityStatus(this);
      if (status != NetworkUtil.TYPE_NOT_CONNECTED) {
        Jsch jSch = new Jsch(pServer, pServerPort, pUsername, pPassword);
        mJschAsyncTask = new JschAsyncTask(jSch);
        mJschAsyncTask.execute(pFileList);
      } else {
        Toast.makeText(this, String.format("Please connect to Network first. Current Network Status %s", status), Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void asyncTaskJschStop() {
    //Stop AsyncTask
    if (mJschAsyncTask != null) mJschAsyncTask.cancel(true);
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

  /**
   * Start/Stop ScanContentAsyncTask
   */
  public void asyncTaskScanContentAsyncStart(ArrayList<String> pDirectories) {
    try {
      mScanContentAsyncTask = new ScanContentAsyncTask();
      mScanContentAsyncTask.execute(pDirectories);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void asyncTaskScanContentAsyncStop() {
    //Stop AsyncTask
    if (mScanContentAsyncTask != null) mScanContentAsyncTask.cancel(true);
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //Client Methods/ Called with bound Components

  public int getRandomNumber() {
    return mGenerator.nextInt(100);
  }
}

