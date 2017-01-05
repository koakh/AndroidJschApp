package com.koakh.jschapp.network;

import android.util.Log;

import com.jcraft.jsch.SftpProgressMonitor;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.util.Utils;

/**
 * Created by mario on 04/10/2014.
 */

public class JschProgressMonitor implements SftpProgressMonitor {

  //PacketSize
  private final int SSH_PACKET_SIZE = 32673;
  //Thread
  private final int THREAD_SLEEP_MSECS = 10;

  //Parameters Members
  private Thread mThread;
  private String  mCurrentFileName;
  private int mCurrentFileNo;
  private int mTotalNoOfFiles;
  private long mCurrentFileSize;
  private long mTotalFilesSize;

  //Private Members
  private Singleton mApp;
  //Time
  private long mTaskTimeStart;
  //Stores the Resumed Initial File
  //private long mTotalResumedFileSize = 0;
  private long mTotalTransferredInTask = 0;
  private long mTotalTransferredProgress = 0;

  public JschProgressMonitor(Thread pThread, String pFileName, int pCurrentFileNo, int pTotalNoOfFiles, long pCurrentFileSize, long pTotalFilesSize) {

    //Params to local Vars
    mThread = pThread;
    mCurrentFileName = pFileName;
    mCurrentFileNo = pCurrentFileNo;
    mTotalNoOfFiles = pTotalNoOfFiles;
    mCurrentFileSize = pCurrentFileSize;
    mTotalFilesSize = pTotalFilesSize;

    //Get Singleton
    mApp = Singleton.getInstance();

    //Store StartTime before Start Upload Process, and After Generate MD5
    mTaskTimeStart = System.currentTimeMillis();
  }

  //Public Setters Members
  public void setCurrentFileNo(int mCurrentFileNo) {
    this.mCurrentFileNo = mCurrentFileNo;
  }

  public void setCurrentFileSize(long mCurrentFileSize) {
    this.mCurrentFileSize = mCurrentFileSize;
  }

  public void setTotalTransferredProgress(long mTotalTransferredProgress) {
    this.mTotalTransferredProgress = mTotalTransferredProgress;
  }

  //SftpProgressMonitor Methods

  public void init(int op, java.lang.String src, java.lang.String dest, long max) {
    //System.out.println("STARTING: " + op + " " + src + " -> " + dest + " total: " + max);
  }

  public boolean count(long pCountBytes) {
    // Notes: In resume the First Count has File Size of the Resume + SSH_PACKET_SIZE,
    // Ideal to get current resumeFileSize if > SSH_PACKET_SIZE, next counters add SSH_PACKET_SIZE without the Resume Size

    //If file already have File Size Great than SSH_PACKET_SIZE (RESUMED) Store Current Resume File Size (First CountBytes - SSH_PACKET_SIZE)
    //Currently not used, may be useful in future
    //if (pCountBytes > 32673) {
    //  mTotalResumedFileSize = pCountBytes - SSH_PACKET_SIZE;
    //}

    //Normal Packet Size Upload
    mTotalTransferredInTask += SSH_PACKET_SIZE;
    //Always Add to Global Transfer Amount
    mTotalTransferredProgress += pCountBytes;

    long msecsElapsed = (System.currentTimeMillis() - mTaskTimeStart);
    String percentage = Utils.decimalFormat(((long) 100 * mTotalTransferredProgress) / mTotalFilesSize, "0");
    String speed = Utils.internetSpeed(msecsElapsed, mTotalTransferredInTask);

    Object[] progress = {mCurrentFileName, percentage, speed, mCurrentFileNo, mTotalNoOfFiles, mTotalTransferredProgress, mTotalFilesSize};
    mApp.getJschAsyncTask().publishProgressFromOutside(progress);

    // Return false from its count method whenever you want to cancel the file transfer.
    if (mTotalTransferredProgress == mTotalFilesSize) {
      return false;
    }

    //User Cancel
    if (mApp.getJschAsyncTask().isCancelled()) {
      Log.i(mApp.TAG, "AsyncTask Cancelled by User");
      return false;
    }

    //It Works at Last.....
    try {
      mThread.sleep(THREAD_SLEEP_MSECS);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return true;
  }

  //Finish Transfer File
  public void end() {
    Log.i(mApp.TAG, String.format("JschProgressMonitor Finished Transfer File %s of %s", mCurrentFileNo, mTotalNoOfFiles));
  }
}