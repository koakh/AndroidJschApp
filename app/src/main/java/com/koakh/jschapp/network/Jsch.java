package com.koakh.jschapp.network;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.koakh.jschapp.R;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.model.FileInfo;
import com.koakh.jschapp.ui.activity.MainActivity;
import com.koakh.jschapp.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mario on 04/10/2014.
 */
public class Jsch {

  //Constants ChannelSftp
  public static final int RESUME = 1;

  //Parameters
  private String mServer;
  private String mUsername;
  private String mPassword;
  private int mPort;

  //Private Members
  private Singleton mApp;
  private Context mContext;
  //Files
  private String mRsaKnownHostsFile;

  public Jsch(String pServer, int pPort, String pUsername, String pPassword) {

    //Parameters
    mServer = pServer;
    mPort = pPort;
    mUsername = pUsername;
    mPassword = pPassword;

    //Get Singleton
    mApp = Singleton.getInstance();
    //Context
    mContext = mApp.getAppContext();
    //dataDir
    mRsaKnownHostsFile = String.format("%s/known_hosts", mApp.getDataDir());

    //NEW - REQUIRED to create fake file, else gives problems when file is missing, ex on re-install, sintoms, can comunicate, and dont output any errors
    //mRsaKnownHostsFile = String.format("%s/known_hosts", mContext.getFilesDir());
    //TODO: ONLY CREATE IF not exists
    Utils.fileWrite(mRsaKnownHostsFile, "", true);
  }

  public Session getSession() {

    //Local vars
    Session session = null;

    try {
      JSch ssh = new JSch();
      ssh.setKnownHosts(mRsaKnownHostsFile);
      session = ssh.getSession(mUsername, mServer, mPort);

      UserInfo ui = new MyUserInfo();
      session.setUserInfo(ui);
      session.setPassword(mPassword.getBytes());

      try {
        session.connect();
      } catch (final JSchException e) {
        e.printStackTrace();
        //Can't create handler inside thread that has not called Looper.prepare()
        //If you try to show anything on UI thread without calling runOnUiThread
        mApp.getAppContext().runOnUiThread(new Runnable() {
          public void run() {
            Toast.makeText(mApp.getAppContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
          }
        });
        Log.e(mApp.TAG, e.getCause().getLocalizedMessage());
        return null;
      }
    } catch (JSchException e1) {
      //InException because dont have known_hosts it creates it
      e1.printStackTrace();
      addHost(session.getHostKey().getKey());
    }

    return session;
  }

  public void uploadFiles(ArrayList<String> fileList, String pathDestination) {

    //Local vars
    Session session = getSession();

    if (session == null) return;

    Channel channel;

    try {
      channel = session.openChannel("sftp");
      channel.connect();
      ChannelSftp sftp = (ChannelSftp) channel;

      if (sftp.isConnected()) {

        Log.i(mApp.TAG, "SFTP is Connected. Start Task");

        /*
        String pathSource = "/home/mario/Dropbox/Markdown";

        try {
          sftp.cd(pathSource);
        } catch (SftpException e) {
          e.printStackTrace();
        }
        */

        //Prepare to Upload : Get Total Files to Process
        int currentFileIndex = -1;
        File file;
        String fileNameGSON = null;
        int totalNoOfFiles = 0;
        List<FileInfo> fileInfo = new ArrayList<FileInfo>();
        long totalFilesSize = (long) 0;
        String remoteCmd = null;
        String resultCmd = null;
        String[] resultCmdArray;
        String localMD5 = null;
        String remoteMD5 = null;

        //Loop FileList ArrayList
        for (String fileName : fileList) {
          currentFileIndex++;
          file = new File(fileName);
          if (file.exists()) {
            totalNoOfFiles += 1;
            totalFilesSize += file.length();

            //MD5 Stuff
            fileNameGSON = String.format("%s.%s", Utils.stripFileExtension(fileName), "gson");
            File fileGSON = new File(fileNameGSON);

            //Generate FileInfo
            if (!fileGSON.exists()) {
              mApp.getNotificationBuilder()
                .setProgress(0, 0, true)//indeterminate
                .setContentTitle(mContext.getString(R.string.global_generating_local_md5))
                .setContentText(String.format("%s: %s", mContext.getString(R.string.global_file), file.getName()));

              mApp.getNotificationManager().notify(mApp.NOTIFICATION_UNIQUE_ID, mApp.getNotificationBuilder().build());

              //Add new FileInfo to ArrayList
              fileInfo.add(new FileInfo(fileList.get(currentFileIndex), "Info"));
              fileInfo.get(currentFileIndex).writeGson(fileNameGSON);
              //ReUse already created FileInfo
            } else {
              fileInfo.add(FileInfo.readGson(fileNameGSON));
            }
          }
        }

        JschProgressMonitor progressMonitor = new JschProgressMonitor(
          Thread.currentThread(),
          new File(fileList.get(0)).getName(),
          //Assign 0 values file on Loop
          0, totalNoOfFiles,
          0, totalFilesSize
        );

        //Reset
        currentFileIndex = -1;
        long totalTransferredProgress = 0;

        //Start Upload
        for (String fileName : fileList) {
          currentFileIndex++;
          file = new File(fileName);
          fileNameGSON = String.format("%s.%s", Utils.stripFileExtension(fileName), "gson");

          if (file.exists()) {
            //Prepare Notification Builder
            mApp.getNotificationBuilder().setContentTitle(String.format("%s: %s", mContext.getString(R.string.global_uploading), file.getName()));
            //Notify NotifyManager
            mApp.getNotificationManager().notify(mApp.NOTIFICATION_UNIQUE_ID, mApp.getNotificationBuilder().build());

            //Update Progress Monitor
            progressMonitor.setCurrentFileNo(currentFileIndex + 1);
            progressMonitor.setCurrentFileSize(file.length());

            //Start Upload
            try {
              Log.i(mApp.TAG, String.format("Start Upload file: %s MD5: %s", fileName, fileInfo.get(currentFileIndex).getMD5()));
              sftp.put(fileName, pathDestination, progressMonitor, RESUME);
              sftp.put(fileNameGSON, pathDestination, progressMonitor, RESUME);
            } catch (SftpException e) {
              e.printStackTrace();
              //Toggle Buttons
//mApp.getAppContext().toggleMenuItemsJschAsyncTask(false);
mApp.setIsAsyncTaskUploadRunning(false);
            }
          } else {
            Log.i(mApp.TAG, String.format("File %s dont exists, please check filename", file.getName()));
          }

          //Prepare Notification Builder
          //mApp.getNotificationBuilder().setContentTitle(String.format("%s %s", mContext.getString(R.string.global_generating_remote_md5), file.getName()));
          //Notify NotifyManager
          //mApp.getNotificationManager().notify(mApp.NOTIFICATION_UNIQUE_ID, mApp.getNotificationBuilder().build());

          //Check Remote MD5
          try {
            //Get Result Command
            remoteCmd = String.format("md5sum %s/%s", pathDestination, file.getName());
            resultCmd = executeRemoteCommand(session, remoteCmd).trim();
            //Get Remote MD5
            resultCmdArray = resultCmd.split("  ");
            //Log.i(mApp.TAG, String.format("remoteMD5: [%s]", remoteMD5));
            //Upper MD5s
            localMD5 = fileInfo.get(currentFileIndex).getMD5().toUpperCase();
            remoteMD5 = resultCmdArray[0].toUpperCase();

            //Compare Local / Remote MD5
            if (localMD5.equals(remoteMD5)) {
              Log.i(mApp.TAG, String.format("Valid Remote MD5: %s", localMD5));
            } else {
              Log.i(mApp.TAG, String.format("InValid MD5: Local:[%s] <> Remote:[%s]", localMD5, remoteMD5));
            }
          } catch (Exception e) {
            e.printStackTrace();
          }

          //Force Update After Put, if Files are already in Server, This way we always have Progress for already Upload files
          totalTransferredProgress += file.length();
          //Update Progress Monitor Member to be Sync
          progressMonitor.setTotalTransferredProgress(totalTransferredProgress);
          String percentage = Utils.decimalFormat(((long) 100 * totalTransferredProgress) / totalFilesSize, "0");
          Object[] progress = {file.getName(), percentage, "??%", currentFileIndex + 1, totalNoOfFiles, totalTransferredProgress, totalFilesSize};
          mApp.getJschAsyncTask().publishProgressFromOutside(progress);
        }
        try {
          sftp.disconnect();
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        Log.i(mApp.TAG, "SFTP is Not Connected");
      }
    } catch (JSchException e) {
      e.printStackTrace();
    }
  }

  /**
   * Add Host RSA File (known_hosts)
   *
   * @param key Host Key
   */
  private void addHost(String key) {
    String ip = Utils.getLocalIpAddress();
    if (ip != null) {
      String rsaLine = ip + " ssh-rsa " + key + "\n";
      Utils.fileWrite(mRsaKnownHostsFile, rsaLine, true);
    }
  }

  public String executeRemoteCommand(Session pSession, String pCommand) throws Exception {
    // SSH Channel
    ChannelExec channelSsh = (ChannelExec) pSession.openChannel("exec");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    channelSsh.setOutputStream(baos);

    // Execute command
    channelSsh.setCommand(pCommand);
    channelSsh.connect();

    while (!channelSsh.isClosed()) {
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    channelSsh.disconnect();

    return baos.toString();
  }

  //REQUIRE IMPLEMENT THIS ELSE Gives com.jcraft.jsch.JSchException: Auth fail
  public static class MyUserInfo implements UserInfo, UIKeyboardInteractive {

    private String password;

    public String getPassword() {
      return password;
    }

    public boolean promptYesNo(String str) {
      return true;
    }

    public String getPassphrase() {
      return null;
    }

    public boolean promptPassphrase(String message) {
      return true;
    }

    public boolean promptPassword(String message) {

      password = MainActivity.getServerUsername();
      return true;
    }

    public void showMessage(String message) {
      System.out.println("message = " + message);
    }

    public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt, boolean[] echo) {

      if (prompt[0].equals("Password: ")) {
        String[] response = new String[1];
        response[0] = MainActivity.getServerUsername();
        return response;
      }

      return null;
    }
  }

  /*
  //Todo Put this Method to Work
  public String executeRemoteCommand(String pHostname, int pPort, String pUsername, String pPassword) throws Exception {
    JSch jsch = new JSch();
    Session session = jsch.getSession(pUsername, pHostname, pPort);
    //session.setPassword(pPassword);

    UserInfo ui = new MyUserInfo();
    session.setUserInfo(ui);
    //session.setPassword("PASS".getBytes());

    //session.connect(30000);
    //Log.i(mApp.TAG, "getHostKey:" + session.getHostKey());

    // Avoid asking for key confirmation
    Properties prop = new Properties();
    prop.put("StrictHostKeyChecking", "no");
    session.setConfig(prop);
    //session.getHostKey();

    ChannelSftp channel = (ChannelSftp) session.openChannel("sftp");

    session.connect();
    Log.i(mApp.TAG, "isConnected:" + session.isConnected());

    // SSH Channel
    ChannelExec channelssh = (ChannelExec) session.openChannel("exec");
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    channelssh.setOutputStream(baos);

    // Execute command
    channelssh.setCommand("ls");
    channelssh.connect();
    channelssh.disconnect();

    return baos.toString();
  }
  */
}
