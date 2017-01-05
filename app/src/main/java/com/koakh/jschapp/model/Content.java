package com.koakh.jschapp.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.UUID;

/**
 * Created by mario on 31/01/2015.
 */

public class Content {
  @SerializedName("UUID")
  private UUID uuid;
  @SerializedName("fileInfo")
  private FileInfo fileInfo;
  @SerializedName("Type")
  private ContentType contentType;
  @SerializedName("Tag")
  private List<ContentTag> contentTags;

  public Content() {
    this.uuid = uuid.randomUUID();
  }

  public List<ContentTag> getContentTags() {
    return contentTags;
  }
  public void setContentTags(List<ContentTag> contentTags) {
    this.contentTags = contentTags;
  }

  public ContentType getContentType() {
    return contentType;
  }
  public void setContentType(ContentType contentType) {
    this.contentType = contentType;
  }

  public FileInfo getFileInfo() {
    return fileInfo;
  }
  public void setFileInfo(FileInfo fileInfo) {
    this.fileInfo = fileInfo;
  }

  public UUID getUuid() {
    return uuid;
  }
  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }
}
