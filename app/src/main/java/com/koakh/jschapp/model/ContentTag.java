package com.koakh.jschapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.UUID;

/**
 * Created by mario on 31/01/2015.
 */
public class ContentTag {
  @SerializedName("UUID")
  private UUID uuid;
  @SerializedName("Name")
  private String fileName;
  @SerializedName("Info")
  private String info;

  public ContentTag() {
    this.uuid = UUID.randomUUID();
  }

  public UUID getUuid() {
    return uuid;
  }
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getFileName() {
    return fileName;
  }
  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getInfo() {
    return info;
  }
  public void setInfo(String info) {
    this.info = info;
  }
}
