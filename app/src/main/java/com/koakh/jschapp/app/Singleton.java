package com.koakh.jschapp.app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;

import com.koakh.jschapp.model.ContentMap;
import com.koakh.jschapp.thread.JschAsyncTask;
import com.koakh.jschapp.ui.activity.MainActivity;

import java.util.ArrayList;

public class Singleton extends Application {

  //Constants
  public final static String TAG = "JschApp";
  public final static int NOTIFICATION_UNIQUE_ID = 2800;

  //Singleton
  private static Singleton mOurInstance;// = new Singleton();
  //Request Codes
  //Map<String, String> mRequestCode = new HashMap<String, String>();
  //App Context
  private MainActivity mAppContext;
  private JschAsyncTask mJschAsyncTask;
  //Paths
  private String mDataDir;
  //NotificationManager
  private NotificationManager mNotificationManager;
  //NotificationBuilder
  private Notification.Builder mNotificationBuilder;
  //FileList
  private ArrayList<String> mFileList;
  //ContentMap
  private ContentMap mContentMap = new ContentMap();
  //Service
  private boolean mIsServiceUploadRunning = false;
  private boolean mIsAsyncTaskUploadRunning = false;
  private boolean mIsAsyncTaskScanContentRunning = false;

  @Override
  public void onCreate() {
    super.onCreate();
    mOurInstance = this;
  }

  public static Singleton getInstance() {
    return mOurInstance;
  }

  public MainActivity getAppContext() {
    return mAppContext;
  }

  public void setAppContext(MainActivity pAppContext) {
    mAppContext = pAppContext;
  }

  public JschAsyncTask getJschAsyncTask() {
    return mJschAsyncTask;
  }

  public void setJschAsyncTask(JschAsyncTask pJschAsyncTask) {
    mJschAsyncTask = pJschAsyncTask;
  }

  public String getDataDir() {
    return mDataDir;
  }

  public void setDataDir(String pDataDir) {
    mDataDir = pDataDir;
  }

  public NotificationManager getNotificationManager() {
    return mNotificationManager;
  }

  public void setNotificationManager(NotificationManager pNotificationManager) {
    mNotificationManager = pNotificationManager;
  }

  public Notification.Builder getNotificationBuilder() {
    return mNotificationBuilder;
  }

  public void setNotificationBuilder(Notification.Builder pNotificationBuilder) {
    mNotificationBuilder = pNotificationBuilder;
  }

  public ContentMap getContentMap() {
    return mContentMap;
  }

  public void setContentMap(ContentMap mContentMap) {
    this.mContentMap = mContentMap;
  }

  public ArrayList<String> getFileList() {
    return mFileList;
  }

  public void setFileList(ArrayList<String> mFileList) {
    this.mFileList = mFileList;
  }

  public boolean isServiceUploadRunning() {
    return mIsServiceUploadRunning;
  }

  public void setIsServiceUploadRunning(boolean mIsServiceUploadRunning) {
    this.mIsServiceUploadRunning = mIsServiceUploadRunning;
  }

  public boolean isAsyncTaskUploadRunning() {
    return mIsAsyncTaskUploadRunning;
  }

  public void setIsAsyncTaskUploadRunning(boolean mIsAsyncTaskRunning) {
    this.mIsAsyncTaskUploadRunning = mIsAsyncTaskRunning;
  }

  public boolean isIsAsyncTaskScanContentRunning() {
    return mIsAsyncTaskScanContentRunning;
  }

  public void setIsAsyncTaskScanContentRunning(boolean mIsAsyncTaskScanContentRunning) {
    this.mIsAsyncTaskScanContentRunning = mIsAsyncTaskScanContentRunning;
  }
}
