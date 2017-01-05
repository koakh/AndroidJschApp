package com.koakh.jschapp.util;

import android.content.Context;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.Enumeration;

/**
 * Created by mario on 05/09/2014.
 */
public class Utils {

  /**
   * @return Required Permissions
   * <uses-permission android:name="android.permission.INTERNET" />
   * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
   */
  public static String getLocalIpAddress() {
    try {
      for (Enumeration<NetworkInterface> en = NetworkInterface
        .getNetworkInterfaces(); en.hasMoreElements(); ) {
        NetworkInterface intf = en.nextElement();
        for (Enumeration<InetAddress> enumIpAddr = intf
          .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
          InetAddress inetAddress = enumIpAddr.nextElement();
          //System.out.println("ip1--:" + inetAddress);
          //System.out.println("ip2--:" + inetAddress.getHostAddress());

          // for getting IPV4 format
          String ipv4;
          if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipv4 = inetAddress.getHostAddress())) {
            // return inetAddress.getHostAddress().toString();
            return ipv4;
          }
        }
      }
    } catch (Exception ex) {
      Log.e("IP Address", ex.toString());
    }
    return null;
  }

  public static String internetSpeed(long msecs, long bytes) {

    if (msecs > 1000 && bytes > 0) {
      long secs = msecs / 1000;
      long bits = bytes * 8;
      float speed = bits / secs;

      long Kbit = 1024;
      long Mbit = Kbit * 1024;
      long Gbit = Mbit * 1024;

      if (speed < Kbit) return String.valueOf(decimalFormat(speed)) + " bit/sec";
      if (speed > Kbit && speed < Mbit)
        return String.valueOf(decimalFormat(speed / Kbit)) + " Kbit/sec";
      if (speed > Mbit && speed < Gbit)
        return String.valueOf(decimalFormat(speed / Mbit)) + " Mbit/sec";
      if (speed > Gbit) return String.valueOf(decimalFormat(speed / Gbit)) + " Gbit/sec";
    }

    return "???";
  }

  //http://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
  public static String decimalFormat(double pDecimal) {
    return new DecimalFormat("#.0").format(pDecimal);
  }

  public static String decimalFormat(double pDecimal, String pFormat) {
    return new DecimalFormat(pFormat).format(pDecimal);
  }

  public static String bytesToHuman(long size) {
    long Kb = 1 * 1024;
    long Mb = Kb * 1024;
    long Gb = Mb * 1024;
    long Tb = Gb * 1024;
    long Pb = Tb * 1024;
    long Eb = Pb * 1024;

    if (size < Kb) return decimalFormat(size) + " byte";
    if (size >= Kb && size < Mb) return decimalFormat((double) size / Kb) + " Kb";
    if (size >= Mb && size < Gb) return decimalFormat((double) size / Mb) + " Mb";
    if (size >= Gb && size < Tb) return decimalFormat((double) size / Gb) + " Gb";
    if (size >= Tb && size < Pb) return decimalFormat((double) size / Tb) + " Tb";
    if (size >= Pb && size < Eb) return decimalFormat((double) size / Pb) + " Pb";
    if (size >= Eb) return decimalFormat((double) size / Eb) + " Eb";

    return "???";
  }

  /**
   * getDataDir : For current Android application package:
   *
   * @param context
   * @return
   * @throws Exception
   */
  public static String getDataDir(Context context) throws Exception {
    return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).applicationInfo.dataDir;
  }

  /**
   * getDataDir : For any package:
   *
   * @param context
   * @param packageName
   * @return
   * @throws Exception
   */
  public static String getAnyDataDir(Context context, String packageName) throws Exception {
    return context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.dataDir;
  }

  //http://stackoverflow.com/questions/304268/getting-a-files-md5-checksum-in-java
  public static String fileToMD5(String filePath) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(filePath);
      byte[] buffer = new byte[1024];
      MessageDigest digest = MessageDigest.getInstance("MD5");
      int numRead = 0;
      while (numRead != -1) {
        numRead = inputStream.read(buffer);
        if (numRead > 0)
          digest.update(buffer, 0, numRead);
      }
      byte[] md5Bytes = digest.digest();
      return convertHashToString(md5Bytes);
    } catch (Exception e) {
      return null;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (Exception e) {
        }
      }
    }
  }

  private static String convertHashToString(byte[] md5Bytes) {
    String returnVal = "";
    for (int i = 0; i < md5Bytes.length; i++) {
      returnVal += Integer.toString((md5Bytes[i] & 0xff) + 0x100, 16).substring(1);
    }
    return returnVal.toUpperCase();
  }

  public static BufferedReader fileRead(File pFileName) {

    //File yourFile = pContext.getFileStreamPath(pFileName);
    File fileName = pFileName;

    if (fileName.exists()) {
      try {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
        bufferedReader.close();
        return bufferedReader;
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return null;
  }

  public static Boolean fileWrite(String pFileName, String pContent, Boolean pAppend) {
    try {
      FileWriter fileWriter = new FileWriter(pFileName, true);
      if (pAppend) {
        fileWriter.append(pContent);
      } else {
        fileWriter.write(pContent);
      }
      fileWriter.flush();
      fileWriter.close();
      return true;
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  public static String stripFileExtension(String pInput) {
    return pInput != null && pInput.lastIndexOf(".") > 0 ? pInput.substring(0, pInput.lastIndexOf(".")) : pInput;
  }

  //http://stackoverflow.com/questions/5351483/calculate-date-time-difference-in-java
  public String friendlyTimeDiff(long timeDifferenceMilliseconds) {
    long diffSeconds = timeDifferenceMilliseconds / 1000;
    long diffMinutes = timeDifferenceMilliseconds / (60 * 1000);
    long diffHours = timeDifferenceMilliseconds / (60 * 60 * 1000);
    long diffDays = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24);
    long diffWeeks = timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 7);
    long diffMonths = (long) (timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 30.41666666));
    long diffYears = (long) (timeDifferenceMilliseconds / (60 * 60 * 1000 * 24 * 365));

    if (diffSeconds < 1) {
      return "less than a second";
    } else if (diffMinutes < 1) {
      return diffSeconds + " seconds";
    } else if (diffHours < 1) {
      return diffMinutes + " minutes";
    } else if (diffDays < 1) {
      return diffHours + " hours";
    } else if (diffWeeks < 1) {
      return diffDays + " days";
    } else if (diffMonths < 1) {
      return diffWeeks + " weeks";
    } else if (diffYears < 1) {
      return diffMonths + " months";
    } else {
      return diffYears + " years";
    }
  }

  /**
   * Get Files from File Type (File/Dir)
   * @param dir
   * @param extensionFilter
   * @return
   */
  public static File[] getFiles(String dir, FileExtensionFilter extensionFilter) {
    File[] listFiles = new File[0];
    try {
      File file = new File(dir);
      listFiles = file.listFiles(extensionFilter);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return listFiles;
  }

  //TODO : Test this Implementation
  public void writeJSON() {
    JSONObject object = new JSONObject();
    try {
      object.put("name", "Jack Hack");
      object.put("score", new Integer(200));
      object.put("current", new Double(152.32));
      object.put("nickname", "Hacker");
    } catch (JSONException e) {
      e.printStackTrace();
    }
    System.out.println(object);
  }

  //http://stackoverflow.com/questions/2625419/check-if-directory-exist-on-androids-sdcard
  public static boolean dirExists(String pDirPath)
  {
    boolean result = false;
    File dir = new File(pDirPath);
    if(dir.exists() && dir.isDirectory())
      result = true;
    return result;
  }

}
