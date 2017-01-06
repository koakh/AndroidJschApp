package com.koakh.jschapp.thread;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.network.Jsch;
import com.koakh.jschapp.util.Utils;

import java.util.ArrayList;

/**
 * Created by mario on 31/08/2014.
 * AsyncTask<Params, Progress, Result>
 */

// The three types used by an asynchronous task are the following:
// Params, the type of the parameters sent to the task upon execution.
// Progress, the type of the progress units published during the background computation.
// Result, the type of the result of the background computation.
public class JschAsyncTask extends AsyncTask<ArrayList<String>, Object, Boolean> {

  //Private Members
  private Singleton mApp;
  private Context mContext;
  private Jsch mJsch;
  //NotificationManager
  private NotificationManager mNotificationManager;
  //NotificationBuilder
  private Notification.Builder mNotificationBuilder;

  public JschAsyncTask(Jsch pJsch) {
    //Parameters
    mJsch = pJsch;
    //Get Singleton
    mApp = Singleton.getInstance();
    //Context
    mContext = mApp.getAppContext();
    //Add Reference to OutSide World, to publish progress from Outside
    mApp.setJschAsyncTask(this);
  }

  // onPreExecute(), invoked on the UI thread before the task is executed.
  // This step is normally used to setup the task, for instance by showing a progress bar
  // in the user interface.
  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    //Prepare PendingIntent, used to call activity when press notification
    //Intent notificationIntent = new Intent(mContext, MainActivity.class);
    //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    //PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, 0);

    //Initialize NotificationBuilder
    //mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    //mNotificationBuilder = new Notification.Builder(mContext);

    //mNotificationBuilder.setContentTitle(mContext.getString(R.string.global_file_upload))
    //  .setContentText(mContext.getString(R.string.global_upload_in_progress))
    //  .setSmallIcon(R.drawable.ic_download)
    //  .setContentIntent(pendingIntent);

    // Displays the progress bar for the first time.
    //mNotificationBuilder.setProgress(100, 0, false);
    //mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());

    //Enable Singleton AsyncTaskRunning
    mApp.setIsAsyncTaskUploadRunning(true);

    //Get From Singleton
    mNotificationManager = mApp.getNotificationManager();
    mNotificationBuilder = mApp.getNotificationBuilder();
  }

  // doInBackground(Params...), invoked on the background thread immediately after onPreExecute() finishes
  // executing. This step is used to perform background computation that can take a long time.
  // The parameters of the asynchronous task are passed to this step.
  // The result of the computation must be returned by this step and will be passed back to the last step.
  // This step can also use publishProgress(Progress...) to publish one or more units of progress. These values are published on the UI thread, in the onProgressUpdate(Progress...) step.
  @Override
  protected Boolean doInBackground(ArrayList<String>... pFileList) {

    Log.i(mApp.TAG, "doInBackground: Start Execute JschAsyncTask");

    try {
      //TODO: Remove HardCoded /tmp Path
      //required [0] to extract ArrayList from pFileList, debug pFileList to view
      mJsch.uploadFiles(pFileList[0], "/tmp/test");
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
    return true;
  }

  @Override
  protected void onProgressUpdate(Object... values) {
    super.onProgressUpdate(values);

    String percentage = (String) values[1];
    String speed = (String) values[2];
    Integer currentFileNo = (Integer) values[3];
    Integer totalNoOfFiles = (Integer) values[4];
    long totalTransferred = (Long) values[5];
    long totalFilesSize = (Long) values[6];

    String contentText = String.format(
      "%s%% : %s %s/%s : %s/%s : %s",
      percentage,
      mContext.getString(R.string.global_file),
      currentFileNo, totalNoOfFiles,
      Utils.bytesToHuman(totalTransferred),
      Utils.bytesToHuman(totalFilesSize),
      speed
    );


    //Send broadcast to update UI
    Intent intent = new Intent("my-event");
    // add data
    intent.putExtra("message", contentText);
    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);



    //Used to Debug Upload Service
    //Log.i(mApp.TAG, contentText);

    // Update progress
    mNotificationBuilder
      //Is Updated in AsyncTask
      //.setContentTitle(String.format("%s : %s", mContext.getString(R.string.global_file), values[0]))
      .setContentText(contentText)
      .setProgress((int) totalFilesSize, (int) totalTransferred, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());
  }

  @Override
  protected void onPostExecute(Boolean pBoolean) {
    super.onPostExecute(pBoolean);

    Log.i(mApp.TAG, "onPostExecute: Finished JschAsyncTask");

    //Can't create handler inside thread that has not called Looper.prepare()
    //Toast.makeText(mContext, "onPostExecute: Finished AsyncTask", Toast.LENGTH_SHORT).show();

    mNotificationBuilder.setContentTitle(mContext.getString(R.string.global_upload_complete));
    // Removes the progress bar/Currently we Keep it to user Look ate Final Details, like Speed/s etc
    //mNotificationBuilder.setProgress(0, 100, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());

    //Disable Singleton IsAsyncTaskRunning
    mApp.setIsAsyncTaskUploadRunning(false);
  }

  @Override
  protected void onCancelled(Boolean pBoolean) {
    super.onCancelled(pBoolean);

    Log.i(mApp.TAG, "onCancelled: Cancelled JschAsyncTask");

    mNotificationBuilder.setContentText(mContext.getString(R.string.global_upload_cancelled));
    //Removes the progress bar
    //mNotificationBuilder.setProgress(0, 0, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());

    //Disable Singleton IsAsyncTaskRunning
    mApp.setIsAsyncTaskUploadRunning(false);
  }

  /**
   * Used to send progress from outside/external classes
   *
   * @param pProgress
   */
  public void publishProgressFromOutside(Object[] pProgress) {
    publishProgress(pProgress);
  }
}
