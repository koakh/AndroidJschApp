package com.koakh.jschapp.thread;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.model.Content;
import com.koakh.jschapp.model.ContentMap;
import com.koakh.jschapp.model.FileInfo;
import com.koakh.jschapp.util.FileExtensionFilter;
import com.koakh.jschapp.util.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by mario on 08-01-2016.
 * AsyncTask<Params, Progress, Result>
 */
public class ScanContentAsyncTask extends AsyncTask<ArrayList<String>, Object, Long> {

  //TODO: Replace MEDIA_PATH and MEDIA_FILTER por ArrayList of Object <Path,Filter,Description,....>
  private final static String MEDIA_PATH = "/storage/external_SD/DCIM/";
  private final static FileExtensionFilter MEDIA_FILTER = new FileExtensionFilter(false, ".jpg", ".mp4", ".tgz", ".deb", ".tar.gz");

  //Private Members
  private Singleton mApp;
  private Context mContext;
  //NotificationManager
  private NotificationManager mNotificationManager;
  //NotificationBuilder
  private Notification.Builder mNotificationBuilder;

  public ScanContentAsyncTask() {

    //Get Singleton
    mApp = Singleton.getInstance();
    //Context
    mContext = mApp.getAppContext();
  }

  @Override
  protected void onPreExecute() {
    super.onPreExecute();

    //Enable Singleton AsyncTaskRunning
    mApp.setIsAsyncTaskScanContentRunning(true);

    //Get From Singleton
    mNotificationManager = mApp.getNotificationManager();
    mNotificationBuilder = mApp.getNotificationBuilder();
  }

  @Override
  protected Long doInBackground(ArrayList<String>... pDirectories) {

    long result = 0;

    Log.i(mApp.TAG, "doInBackground: Start Execute ScanContentAsyncTask");
    //required [0] to extract ArrayList from pFileList, debug pFileList to view
    startScanContent(pDirectories[0]);

    return result;
  }

  @Override
  protected void onProgressUpdate(Object... values) {
    super.onProgressUpdate(values);

    String fileName = (String) values[0];
    String info = (String) values[1];
    int position = (Integer) values[2];
    int max = (Integer) values[3];

    // Update progress
    mNotificationBuilder
      .setContentTitle(String.format("%s %d/%d : %s", mContext.getString(R.string.global_file), position, max, fileName))
      .setContentText(info)
      .setProgress(max, position, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());
  }

  @Override
  protected void onPostExecute(Long result) {
    super.onPostExecute(result);

    Log.i(mApp.TAG, "onPostExecute: Finished ScanContentAsyncTask");

    mNotificationBuilder.setContentTitle(mContext.getString(R.string.global_scan_complete));
    // Removes the progress bar/Currently we Keep it to user Look ate Final Details, like Speed/s etc
    //mNotificationBuilder.setProgress(0, 100, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());

    //Disable Singleton AsyncTaskRunning
    mApp.setIsAsyncTaskScanContentRunning(false);
  }

  @Override
  protected void onCancelled(Long result) {
    super.onCancelled(result);

    Log.i(mApp.TAG, "onCancelled: Cancelled ScanContentAsyncTask");

    mNotificationBuilder.setContentText(mContext.getString(R.string.global_scan_cancelled));
    //Removes the progress bar
    //mNotificationBuilder.setProgress(0, 0, false);
    mNotificationManager.notify(mApp.NOTIFICATION_UNIQUE_ID, mNotificationBuilder.build());

    //Disable Singleton AsyncTaskRunning
    mApp.setIsAsyncTaskScanContentRunning(false);
  }

  /**
   * Used to send progress from outside/external classes
   *
   * @param pProgress
   */
  //public void publishProgressFromOutside(int pProgress) {
  //  publishProgress(pProgress);
  //}

  public ContentMap startScanContent(ArrayList<String> pDirectories) {

    //Get ContentMap from Singleton, Create Local Reference
    ContentMap contentMap = new ContentMap();
    ArrayList<String> fileList = new ArrayList<String>();
    //Update Singleton References
    mApp.setContentMap(contentMap);
    mApp.setFileList(fileList);
    //Initialize variables
    int currentFileIndex = 0;
    String notificationText;

    try {
      //TODO: Move to Method to get contentMap files
      //Check if MEDIA_PATH Exists
      if (Utils.dirExists(MEDIA_PATH)) {
        //Create the HasMap to house content Model Pojos
        //Map contentMap = new HashMap();
        //Get ContentMap from Singleton, Create Local Reference
        //ContentMap contentMap = mApp.getContentMap();

        Content content;
        File[] listFiles = Utils.getFiles(MEDIA_PATH, MEDIA_FILTER);
        String fileNameGSON;
        FileInfo fileInfo;
        if (listFiles != null) {
          for (File file : listFiles) {
            //Create the ArrayList to send to Upload
            fileList.add(file.getPath());
            //Create content object to add to contentMap
            content = new Content();
            //Prepare Gson FileName
            fileNameGSON = String.format("%s.%s", Utils.stripFileExtension(file.getAbsoluteFile().toString()), "gson");
            File fileGSON = new File(fileNameGSON);
            //Load Existing Gson File
            if (fileGSON.exists()) {
              //Prepare Progress and Send Progress
              notificationText = String.format("%s : %s %s", mContext.getString(R.string.global_used_local_md5), mContext.getString(R.string.global_file_size), Utils.bytesToHuman(file.length()));
              Object[] progress = {file.getName(), notificationText, ++currentFileIndex, listFiles.length};
              publishProgress(progress);
              //get FileInfo
              fileInfo = FileInfo.readGson(fileNameGSON);
            }
            //Save Gson File
            else {
              //Prepare Progress and Send Progress
              notificationText = String.format("%s : %s %s", mContext.getString(R.string.global_generating_local_md5), mContext.getString(R.string.global_file_size), Utils.bytesToHuman(file.length()));
              Object[] progress = {file.getName(), notificationText, ++currentFileIndex, listFiles.length};
              publishProgress(progress);
              //get FileInfo
              fileInfo = new FileInfo(file.getAbsolutePath(), "Description");
              fileInfo.writeGson(fileNameGSON);
            }
            //Set content fileInfo
            content.setFileInfo(fileInfo);
            //Add to ContentMap
            contentMap.put(file.getAbsolutePath(), content);
          }
        } else {
          //Can't create handler inside thread that has not called Looper.prepare()
          //Toast.makeText(mContext, String.format(mContext.getString(R.string.global_directory_no_files_found, MEDIA_PATH), MEDIA_PATH), Toast.LENGTH_LONG).show();
        }

        // Iterate over all contentMap, using the keySet method.
        if (!contentMap.isEmpty()) {
          for (Object key : contentMap.keySet()) {
            Log.i(mApp.TAG, String.format("contentMap: %s : %s", key, contentMap.get(key)));
          }
          //Save ContentMap Gson
          contentMap.writeGson(String.format("%scontentMap.gson", MEDIA_PATH));
        }
        //Finish Scan
        //Can't create handler inside thread that has not called Looper.prepare()
        //Toast.makeText(mContext, String.format(mContext.getString(R.string.global_directory_finish_scan), MEDIA_PATH, contentMap.size()), Toast.LENGTH_LONG).show();
      } else {
        //Can't create handler inside thread that has not called Looper.prepare()
        //Toast.makeText(mContext, String.format(mContext.getString(R.string.global_directory_invalid_directory), MEDIA_PATH), Toast.LENGTH_LONG).show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
    }

    return contentMap;
  }
}
