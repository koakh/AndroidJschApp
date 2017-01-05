package com.koakh.jschapp.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.background.ServiceExample;
import com.koakh.jschapp.background.ServiceUpload;
import com.koakh.jschapp.model.FileInfo;
import com.koakh.jschapp.util.FileExtensionFilter;
import com.koakh.jschapp.util.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * @author mario
 */

public class MainActivity extends Activity {

  private Singleton mApp;
  //Menu
  private MenuItem mMenuItemTestProgressBar;
  private MenuItem mMenuItemStartService;
  private MenuItem mMenuItemStopService;
  private MenuItem mMenuItemStartScan;
  private MenuItem mMenuItemStopScan;
  private MenuItem mMenuItemStartUpload;
  private MenuItem mMenuItemStopUpload;
  private MenuItem mMenuItemServiceGetRandomNumber;
  //Intent
  private Intent mIntentServiceUpload;
  private Intent mIntentServiceExample;
  //JSch
  private static final String mPropertiesFileLocation = "/sdcard/Temp/credentials.properties";
  private static String mServer;
  private static int mServerPort;
  private static String mServerUsername;
  private static String mServerPassword;
  //Service Bind Vars
  private ServiceUpload mService;
  private boolean mBound = false;
  //UI
  private TextView mTextView;

  //Preferences
  //Default
  //private final static String MEDIA_PATH = "/sdcard/DCIM/";
  //Galaxy5
  //private final static String MEDIA_PATH = "/storage/extSdCard/DCIM/";
  //LG G3
  private final static String MEDIA_PATH = "/storage/external_SD/DCIM/";
  //Android:File - http://developer.android.com/reference/java/io/File.html
  //Android:FilenameFilter - http://developer.android.com/reference/java/io/FilenameFilter.html
  private final static FileExtensionFilter MEDIA_FILTER = new FileExtensionFilter(false, ".jpg", ".mp4", ".tgz", ".deb", ".tar.gz");
  ArrayList<String> mFileList = new ArrayList<String>();
  //TC Filter: PRT_IP.map*;Kod*;*.map;hd_wallpaper*;*.jpg;*.nfs;*.gson
  /*
  private String[] mFileList = {
          "/mnt/sdcard/NDrive/maps/PRT_IP.map",
          "/mnt/sdcard/Navigon/map/Spain.map",
          "/mnt/sdcard/MP3/Mantras/KodoishHymn.mp3",
          "/mnt/sdcard/Navigon/map/Portugal.map",
          "/mnt/sdcard/Navigon/data/MapDrawer.nfs",
          "/mnt/sdcard/Navigon/data/RealityView.nfs",
          "/mnt/sdcard/Navigon/data/Satellite.nfs",
          "/mnt/sdcard/Navigon/data/TMC.nfs",
          "/mnt/sdcard/DCIM/Camera/20140823_144353.jpg",
          "/mnt/sdcard/DCIM/Camera/20140823_144354.jpg",
          "/mnt/sdcard/WallpapersHD/hd_wallpaper_12468.jpg"
  };
  */

  //Todo: Store in Singleton
  public static String getServer() { return mServer; }
  public static int getServerPort() { return mServerPort; }
  public static String getServerUsername() { return mServerUsername; }
  public static String getServerPassword() { return mServerPassword; }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //LiveCycle

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    //get UI Objects References
    mTextView = (TextView)findViewById(R.id.textview_main);

    //Get Application Singleton
    mApp = Singleton.getInstance();
    mApp = ((Singleton) this.getApplicationContext());

    //Assign Context
    mApp.setAppContext(this);

    //Assign DataDir to Singleton
    try {
      mApp.setDataDir(Utils.getDataDir(this));
    } catch (Exception e) {
      e.printStackTrace();
    }

    //TODO: Implement Adapter
    //WIP : Work With ArrayAdapter
    //    ListView listView = (ListView) findViewById(R.id.listview_content);
    //    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
    //      android.R.layout.list_content_, contentMap);
    //
    //    listView.setAdapter(adapter);

    Properties properties = new Properties();
    try {
      properties.load(new FileInputStream(mPropertiesFileLocation));
    } catch (IOException e) {
      e.printStackTrace();
    }

    //Assign form Environment
    mServer = properties.getProperty("SERVER_SSH_HOST");
    mServerPort = Integer.parseInt(properties.getProperty("SERVER_SSH_PORT"));
    mServerUsername = properties.getProperty("SERVER_SSH_USER");
    mServerPassword = properties.getProperty("SERVER_SSH_PASS");

    //Service Intent ServiceExample
    mIntentServiceExample = new Intent(getBaseContext(), ServiceExample.class);
    //Service Intent ServiceUpload
    mIntentServiceUpload = new Intent(getBaseContext(), ServiceUpload.class);
    mIntentServiceUpload.putExtra("Server", mServer);
    mIntentServiceUpload.putExtra("Port", mServerPort);
    mIntentServiceUpload.putExtra("Username", mServerUsername);
    mIntentServiceUpload.putExtra("Password", mServerPassword);

    Toast.makeText(this, "Start " + getString(R.string.app_name), Toast.LENGTH_LONG).show();
  }

  @Override
  protected void onStart() {
    super.onStart();
    //If Service Running Bind to Service
    if (mApp.isServiceUploadRunning()) {
      bindService(mIntentServiceUpload, mConnection, Context.BIND_AUTO_CREATE);
    }
  }

  @Override
  protected void onStop() {
    super.onStop();

    // Unbind from the service
    if (mApp.isServiceUploadRunning() && mBound) {
      unbindService(mConnection);
      mBound = false;
    }
  }

  @Override
  protected void onResume() {
    super.onResume();

    // Register mMessageReceiver to receive messages.
    //  Required com.android.support:support-vx
    LocalBroadcastManager
      .getInstance(this)
      .registerReceiver(mMessageReceiver, new IntentFilter("my-event"));
  }

  @Override
  protected void onPause() {
    super.onPause();

    // Unregister since the activity is not visible
    LocalBroadcastManager
      .getInstance(this)
      .unregisterReceiver(mMessageReceiver);
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //Menu

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);

    //Get References to MenuItems here
    mMenuItemTestProgressBar = menu.findItem(R.id.action_menu_test_progressbar);
    mMenuItemStartService = menu.findItem(R.id.action_menu_service_start);
    mMenuItemStopService = menu.findItem(R.id.action_menu_service_stop);
    mMenuItemStartScan = menu.findItem(R.id.action_menu_scan_content_start);
    mMenuItemStopScan = menu.findItem(R.id.action_menu_scan_content_stop);
    mMenuItemStartUpload = menu.findItem(R.id.action_menu_upload_content_start);
    mMenuItemStopUpload = menu.findItem(R.id.action_menu_upload_content_stop);
    mMenuItemServiceGetRandomNumber = menu.findItem(R.id.action_menu_service_get_random_number);

    return true;
  }

  /**
   * @param menu
   * @return
   * @see http://developer.android.com/guide/topics/ui/menus.html
   * If you want to modify the options menu based on events that occur during the activity lifecycle, you can do so in the onPrepareOptionsMenu() method. This method passes you the Menu object as it currently exists so you can modify it, such as add, remove, or disable items.
   */
  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {

    if (mApp.isServiceUploadRunning()) {
      mMenuItemStartService.setEnabled(false);
      mMenuItemStopService.setEnabled(true);
      mMenuItemStartScan.setEnabled(
        !mApp.isIsAsyncTaskScanContentRunning()
      );
      mMenuItemStopScan.setEnabled(
        mApp.isIsAsyncTaskScanContentRunning()
      );
      mMenuItemStartUpload.setEnabled(
        !mApp.isAsyncTaskUploadRunning() && !mApp.getContentMap().isEmpty()
      );
      mMenuItemStopUpload.setEnabled(mApp.isAsyncTaskUploadRunning());
    } else {
      mMenuItemStartService.setEnabled(true);
      mMenuItemStopService.setEnabled(false);
      mMenuItemStartScan.setEnabled(false);
      mMenuItemStartUpload.setEnabled(false);
      mMenuItemStopUpload.setEnabled(false);
    }
    mMenuItemServiceGetRandomNumber.setEnabled(mApp.isServiceUploadRunning() && mBound);

    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {

    Log.i(mApp.TAG, getString(menuItem.getItemId()));

    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    switch (menuItem.getItemId()) {

      //DISABLED
      case R.id.action_menu_test_progressbar:
        //testProgressNotification();
        return true;

      //DISABLED
      case R.id.action_menu_fileinfo_write_json:
        try {
          String fileName = String.format("%s/known_hosts", mApp.getDataDir());
          FileInfo fileInfo = new FileInfo(fileName, "Info");
          fileInfo.writeGson(String.format("%s.%s", fileName, "gson"));
        } catch (Exception e) {
          e.printStackTrace();
        }
        return true;

      //DISABLED
      case R.id.action_menu_fileinfo_read_json:
        try {
          String fileName = String.format("%s/known_hosts", mApp.getDataDir());
          FileInfo fileInfo = (FileInfo.readGson(String.format("%s.%s", fileName, "gson")));
          Log.i(mApp.TAG, String.format("MD5: %s FileSize: %s", fileInfo.getMD5(), fileInfo.getFileSize()));
        } catch (Exception e) {
          e.printStackTrace();
        }
        return true;

      case R.id.action_menu_service_start:
        //startServiceExample();
        startServiceUpload();
        return true;

      case R.id.action_menu_service_stop:
        //stopServiceExample();
        stopServiceUpload();
        return true;

      case R.id.action_menu_scan_content_start:
        //startScanContent();
        ArrayList<String> directories = new ArrayList<String>();
        directories.add(MEDIA_PATH);
//TODO: Receive Media Object (Dir,Desc,Filter etc)
mService.asyncTaskScanContentAsyncStart(directories);
        return true;

      case R.id.action_menu_scan_content_stop:
        mService.asyncTaskScanContentAsyncStop();
        return true;

      case R.id.action_menu_upload_content_start:
//asyncTaskJschStart(mFileList);
//TODO: Change to mApp.getAppContext()
mFileList = mApp.getFileList();
        mService.asyncTaskJschStart(mFileList, mServer, mServerPort, mServerUsername, mServerPassword);
        return true;

      case R.id.action_menu_upload_content_stop:
//menuItem.setEnabled(asyncTaskJschStop());
        mService.asyncTaskJschStop();
        return true;

      case R.id.action_menu_service_get_random_number:
        mService.getRandomNumber();
        return true;

      case R.id.action_menu_quit:
        finish();
        return true;

      default:
        return super.onOptionsItemSelected(menuItem);
    }
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //Services

  public void startServiceExample() {
    mIntentServiceExample.putExtra("ImageFileURL", "http://www.codeproject.com/App_Themes/CodeProject/Img/logo125x125.gif");
    mIntentServiceExample.putExtra("FileName", "bob.gif");
    mIntentServiceExample.putExtra("FileList", mFileList);
    startService(mIntentServiceExample);
  }

  public void stopServiceExample() {
    stopService(mIntentServiceExample);
  }

  //TODO: Remove This Function : Removed Now is Inside of Service
  public void startServiceUpload() {
    try {
      //TODO: Change mFileList to contentMap
      //TODO: Remove FileList From Here
      mIntentServiceUpload.putExtra("FileList", mFileList);
      //Start Service Without Bind
      startService(mIntentServiceUpload);
      //Start Service with Bind to LocalService
      //http://developer.android.com/reference/android/content/Context.html
      bindService(mIntentServiceUpload, mConnection, Context.BIND_AUTO_CREATE);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
    }
  }

  public void stopServiceUpload() {
    try {
      if (mBound) unbindService(mConnection);
      stopService(mIntentServiceUpload);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
    }
  }

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //Services Bind

  /**
   * Defines callbacks for service binding, passed to bindService()
   */
  private ServiceConnection mConnection = new ServiceConnection() {

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
      // We've bound to LocalService, cast the IBinder and get LocalService instance
      ServiceUpload.LocalBinder binder = (ServiceUpload.LocalBinder) service;
      mService = binder.getService();
      mBound = true;
      Log.i(mApp.TAG, "ServiceConnected");
    }

    @Override
    public void onServiceDisconnected(ComponentName arg0) {
      mBound = false;
      Log.i(mApp.TAG, "ServiceDisconnected");
    }
  };

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //LocalBroadcastManager

  // handler for received Intents for the "my-event" event
  private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      // Extract data included in the Intent
      String message = intent.getStringExtra("message");
      Log.d("receiver", "Got message: " + message);

      mTextView.setText(message);
    }
  };

  //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
  //Deprecated

  /**
   * Start Scan Content
   */
  /*
  //TODO: Remove This Function : Removed Now is Inside of Service
  public void startScanContent() {
    mMenuItemStartScan.setEnabled(false);

    try {
      //TODO: Move to Method to get contentMap files
      //TODO: Move to Service
      //Check if MEDIA_PATH Exists
      if (Utils.dirExists(MEDIA_PATH)) {
        //Create the HasMap to house content Model Pojos
        //Map contentMap = new HashMap();
        //Get ContentMap from Singleton, Create Local Reference
        ContentMap contentMap = mApp.getContentMap();

        Content content;
        File[] listFiles = Utils.getFiles(MEDIA_PATH, MEDIA_FILTER);
        String fileNameGSON;
        FileInfo fileInfo;
        if (listFiles != null) {
          for (File file : listFiles) {
            //Create the ArrayList to send to Upload
            mFileList.add(file.getPath());
            //Create content object to add to contentMap
            content = new Content();
            //Prepare Gson FileName
            fileNameGSON = String.format("%s.%s", Utils.stripFileExtension(file.getAbsoluteFile().toString()), "gson");
            File fileGSON = new File(fileNameGSON);
            //Load Existing Gson File
            if (fileGSON.exists()) {
              fileInfo = FileInfo.readGson(fileNameGSON);
            }
            //Save Gson File
            else {
              fileInfo = new FileInfo(file.getAbsolutePath(), "Description");
              fileInfo.writeGson(fileNameGSON);
            }
            //Set content fileInfo
            content.setFileInfo(fileInfo);
            //Add to ContentMap
            contentMap.put(file.getAbsolutePath(), content);
          }
        } else {
          Toast.makeText(this, String.format(getString(R.string.global_directory_no_files_found, MEDIA_PATH), MEDIA_PATH), Toast.LENGTH_LONG).show();
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
        Toast.makeText(this, String.format(getString(R.string.global_directory_finish_scan), MEDIA_PATH, contentMap.size()), Toast.LENGTH_LONG).show();
      } else {
        Toast.makeText(this, String.format(getString(R.string.global_directory_invalid_directory), MEDIA_PATH), Toast.LENGTH_LONG).show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      mMenuItemStartScan.setEnabled(true);
    }
  }
  */

  /**
   * Start JschAsyncTask AsyncTask
   *
   * @param file File to Process
   */
  //TODO: Remove This Function : Removed Now is Inside of Service
  /*
  public void asyncTaskJschStart(String file) {
    //Send a ArrayList with the parameter File
    ArrayList<String> fileList = new ArrayList<String>();
    fileList.add(file);
    asyncTaskJschStart(fileList);
  }
  */

  /**
   * Start JschAsyncTask
   *
   * @param fileList Array of Files to Process
   */
  /*
  //TODO: Remove This Function : Removed Now is Inside of Service
  public void asyncTaskJschStart(ArrayList<String> fileList) {
    try {
      mMenuItemStartUpload.setEnabled(false);
      mMenuItemStopUpload.setEnabled(true);

      int status = NetworkUtil.getConnectivityStatus(this);
      if (status != NetworkUtil.TYPE_NOT_CONNECTED) {
        //Toast.makeText(this, "Start JSch AsyncTask", Toast.LENGTH_SHORT).show();
        Jsch jSch = new Jsch("HOST", PORT, "USER", "PASS");
        mJschAsyncTask = new JschAsyncTask(jSch);
        mJschAsyncTask.execute(fileList);
      } else {
        Toast.makeText(this, String.format("Please connect to Network first. Current Network Status %s", status), Toast.LENGTH_SHORT).show();
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      mMenuItemStartUpload.setEnabled(true);
    }
  }
  */

  /**
   * @return
   */
  //TODO: Remove This Function : Removed Now is Inside of Service
  /*
  public boolean asyncTaskJschStop() {
    boolean result = mJschAsyncTask.cancel(true);
    if (result) {
      mMenuItemStartUpload.setEnabled(true);
      mMenuItemStopUpload.setEnabled(false);
    }
    return result;
  }
  */
}
