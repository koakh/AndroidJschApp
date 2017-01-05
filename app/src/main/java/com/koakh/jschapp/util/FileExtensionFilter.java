package com.koakh.jschapp.util;

import java.io.File;
import java.io.FileFilter;

/**
 * Created by mario on 31/01/2015.
 * http://stackoverflow.com/questions/865059/is-there-any-better-way-to-do-filefilter-for-many-ext
 * @param usage: File files[] = directory.listFiles(new FileExtensionFilter(".zip", ".jar", ".z", ".tar"));
 *
 * Other
 * http://www.codota.com/java/core/scenarios/54932dc4da0ae7d82e85fc72/javax.swing.filechooser.FileNameExtensionFilter?tag=jdk8_1
 * http://www.avajava.com/tutorials/lessons/how-do-i-use-a-filenamefilter-to-display-a-subset-of-files-in-a-directory.html
 * http://www.avajava.com/tutorials/lessons/how-do-i-recursively-display-all-files-and-directories-in-a-directory.html;jsessionid=E6C39DED012406B93235CB0E993EF08A
 */
public class FileExtensionFilter implements FileFilter {

  private final String[] sValidExtensions;
  private final boolean sAcceptDirectories;

  public FileExtensionFilter(boolean pAcceptDirectories, String... pValidExtensions) {
    this.sValidExtensions = pValidExtensions;
    this.sAcceptDirectories = pAcceptDirectories;
  }

  public boolean accept(File pathname) {

    if (this.sAcceptDirectories && pathname.isDirectory()) {
      return true;
    }

    String name = pathname.getName().toLowerCase();

    for (String ext : sValidExtensions) {
      if (name.endsWith(ext)) {
        return true;
      }
    }
    return false;
  }
}