package com.koakh.jschapp.model;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.koakh.jschapp.app.Singleton;
import com.koakh.jschapp.util.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class FileInfo {

  /**
   * Fields
   */
  @SerializedName("UUID")
  private UUID uuid;
  @SerializedName("FileName")
  private String fileName;
  @SerializedName("SourceFilename")
  private String sourceFilename;
  @SerializedName("Info")
  private String info;
  @SerializedName("FileSize")
  private Long fileSize;
  @SerializedName("LastModified")
  private Long lastModified;
  @SerializedName("MD5")
  private String md5;
  @SerializedName("Uploaded")
  private Boolean uploaded;
  @SerializedName("UploadDate")
  private Date uploadDate;

  /**
   * Constructors
   */
  public FileInfo() {
  }

  public FileInfo(String pFileName, String pInfo) {
    File file = new File(pFileName);
    this.uuid = UUID.randomUUID();
    this.fileName = file.getName();
    this.sourceFilename = pFileName;
    this.fileSize = file.length();
    this.lastModified = file.lastModified();
    this.md5 = Utils.fileToMD5(pFileName);
    this.uploadDate = new Date(System.currentTimeMillis());
    this.info = pInfo;
  }

  /**
   * Getters and Setters
   */
  public UUID getUUID() {
    return uuid;
  }

  public String getName() {
    return fileName;
  }
  public void setName(String mName) {
    this.fileName = mName;
  }

  public String getSourceFilename() {
    return sourceFilename;
  }
  public void setSourceFilename(String mSourceFilename) {
    this.sourceFilename = mSourceFilename;
  }

  public String getInfo() {
    return info;
  }
  public void setInfo(String mInfo) {
    this.info = mInfo;
  }

  public Long getFileSize() {
    return fileSize;
  }
  public void setFileSize(Long mFileSize) {
    this.fileSize = mFileSize;
  }

  public Long getLastModified() {
    return lastModified;
  }
  public void setLastModified(Long mLastModified) {
    this.lastModified = mLastModified;
  }

  public String getMD5() {
    return md5;
  }
  public void setMD5(String mMD5) {
    this.md5 = mMD5;
  }

  public Boolean getUploaded() {
    return uploaded;
  }
  public void setUploaded(Boolean mUploaded) {
    this.uploaded = mUploaded;
  }

  public Date getUploadDate() {
    return uploadDate;
  }
  public void setUploadDate(Date mUploadDate) {
    this.uploadDate = mUploadDate;
  }

  public String toString() {
    return "FileInfo ["
            + "uuid=" + uuid
            + ", fileName=" + fileName
            + ", sourceFilename=" + sourceFilename
            + ", info=" + info
            + ", fileSize=" + fileSize
            + ", lastModified=" + lastModified
            + ", md5=" + md5
            + ", uploaded=" + uploaded
            + ", uploadDate=" + uploadDate
            + "]";
  }

  /**
   * Write Gson File
   * @param pFileName
   * @return
   */
  public Boolean writeGson(String pFileName) {
    Gson gson = new Gson();

    String json = gson.toJson(this);
    //Log.i(Singleton.getInstance().TAG, String.format("gson.toJson(this): %s", gson.toJson(this)));
    Log.i(Singleton.getInstance().TAG, String.format("writeGson: %s", this.fileName));

    try {
      return Utils.fileWrite(pFileName, json, false);
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * Read GSon
   * @param pFileName
   * @return
   */
  public static FileInfo readGson(String pFileName) {
    File file = new File(pFileName);
    if (file.exists()) {
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        Gson gson = new Gson();
        FileInfo fileInfo = gson.fromJson(bufferedReader, FileInfo.class);
        Log.i(Singleton.getInstance().TAG, String.format("readGson: %s", fileInfo.fileName));
        bufferedReader.close();

        return fileInfo;
      } catch (FileNotFoundException e) {
        e.printStackTrace();
        return null;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }
}
