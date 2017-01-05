package com.koakh.jschapp.model;

import java.util.UUID;

import com.google.gson.annotations.SerializedName;
import com.koakh.jschapp.util.FileExtensionFilter;

/**
 * Created by mario on 31/01/2015.
 */
public class ContentType {

  @SerializedName("UUID")
  private UUID uuid;
  @SerializedName("Name")
  private String fileName;
  @SerializedName("ExtensionFilter")
  private FileExtensionFilter fileExtensionFilter;
  @SerializedName("Info")
  private String info;

  public ContentType() {
    this.uuid = uuid.randomUUID();
  }

  public UUID getUuid() {
    return uuid;
  }

  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public FileExtensionFilter getFileExtensionFilter() {
    return fileExtensionFilter;
  }
  public void setFileExtensionFilter(FileExtensionFilter fileExtensionFilter) {
    this.fileExtensionFilter = fileExtensionFilter;
  }

  public String getInfo() {
    return info;
  }
  public void setInfo(String info) {
    this.info = info;
  }
}
