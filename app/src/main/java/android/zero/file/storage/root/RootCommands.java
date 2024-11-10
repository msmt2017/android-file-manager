/*
 * Copyright (C) 2014 Simple Explorer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package android.zero.file.storage.root;

import android.util.Log;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootTools.RootTools;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootCommands {

    private static final Logger logger = LoggerFactory.getLogger(RootCommands.class);

  private static final String UNIX_ESCAPE_EXPRESSION =
      "(\\(|\\)|\\[|\\]|\\s|\'|\"|`|\\{|\\}|&|\\\\|\\?)";
  private static SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

  private static String getCommandLineString(String input) {
    return input.replaceAll(UNIX_ESCAPE_EXPRESSION, "\\\\$1");
  }

    
   

public static InputStream getFile(String path) {
    
    try (InputStream in = openFile("cat " + getCommandLineString(path))) {
        // 如果需要立即返回 InputStream，可以在这里进行处理
        return in;
    } catch (Exception e) {
        logger.error("An error occurred while getting the file from path: {}", path, e);
        return null; // 或者抛出自定义异常
    }
}
    
    
//  public static InputStream getFile(String path) {
//    InputStream in = null;
//
//    try {
//      in = openFile("cat " + getCommandLineString(path));
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//    return in;
//  }

  public static InputStream putFile(String path, String text) {
   // InputStream in = null;

    try {
     InputStream  in = openFile("echo \"" + text + "\" > " + getCommandLineString(path));
    } catch (Exception e) {
logger.error("An error occurred while getting the file from path: {}", path, e);
    }

    return in;
  }

  public static ArrayList<String> listFiles(String path) {
    ArrayList<String> listFiles = new ArrayList<>();

    try {
      listFiles = execute("ls -l " + getCommandLineString(path));
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", path, e);
    }

    return listFiles;
  }

  public static ArrayList<String> listFiles(String path, boolean showhidden) {
    ArrayList<String> mDirContent = new ArrayList<>();

    ArrayList<String> listFiles = execute("ls -a " + getCommandLineString(path));
    for (String line : listFiles) {
      if (!showhidden) {
        if (line.charAt(0) != '.') mDirContent.add(path + "/" + line);
      } else {
        mDirContent.add(path + "/" + line);
      }
    }

    return mDirContent;
  }

  public static ArrayList<String> findFiles(String path, String query) {
    ArrayList<String> listFiles = new ArrayList<>();

    try {
      listFiles =
          execute(
              "find "
                  + getCommandLineString(path)
                  + " -type f -iname "
                  + '*'
                  + getCommandLineString(query)
                  + '*'
                  + " -exec ls -ls {} \\;");
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", path, e);
    }

    return listFiles;
  }

  public static ArrayList<String> findFile(String path, String query) {
    ArrayList<String> mDirContent = new ArrayList<>();

    try {
      mDirContent =
          execute(
              "find "
                  + getCommandLineString(path)
                  + " -type f -iname "
                  + '*'
                  + getCommandLineString(query)
                  + '*'
                  + " -exec ls -a {} \\;");
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", path, e);
    }

    return mDirContent;
  }

  // Create Directory with root
  public static boolean createRootdir(String parentPath, String name) {
    File dir = new File(parentPath + File.separator + name);
    if (dir.exists()) return false;

    try {
      if (!readReadWriteFile()) RootTools.remount(parentPath, "rw");

      execute("mkdir " + getCommandLineString(dir.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", path, e);
    }

    return false;
  }

  // Create file with root
  public static boolean createRootFile(String parentPath, String name) {
    File dir = new File(parentPath + File.separator + name);

    if (dir.exists()) return false;

    try {
      if (!readReadWriteFile()) RootTools.remount(parentPath, "rw");

      execute("touch " + getCommandLineString(dir.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", parentPath, e);
    }

    return false;
  }

  // Move or Copy with Root Access using RootTools library
  public static boolean moveCopyRoot(String old, String newDir) {
    try {
      if (!readReadWriteFile()) RootTools.remount(newDir, "rw");

      execute("cp -fr " + getCommandLineString(old) + " " + getCommandLineString(newDir));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }
    return false;
  }

  // path = currentDir
  // oldName = currentDir + "/" + selected Item
  // name = new name
  public static boolean renameRootTarget(String path, String oldname, String name) {
    File file = new File(path + File.separator + oldname);
    File newf = new File(path + File.separator + name);

    if (name.length() < 1) return false;

    try {
      if (!readReadWriteFile()) RootTools.remount(path, "rw");

      execute(
          "mv "
              + getCommandLineString(file.getAbsolutePath())
              + " "
              + getCommandLineString(newf.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }

    return false;
  }

  // path = currentDir
  // oldName = currentDir + "/" + selected Item
  // name = new name
  public static boolean renameRootTarget(RootFile before, RootFile after) {
    File file = new File(before.getParent() + File.separator + before.getName());
    File newf = new File(after.getParent() + File.separator + after.getName());

    if (after.getName().length() < 1) return false;

    try {
      if (!readReadWriteFile()) RootTools.remount(before.getPath(), "rw");

      execute(
          "mv "
              + getCommandLineString(file.getAbsolutePath())
              + " "
              + getCommandLineString(newf.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }

    return false;
  }

  // Delete file with root
  public static boolean deleteFileRoot(String path) {
    try {
      if (!readReadWriteFile()) RootTools.remount(path, "rw");

      if (new File(path).isDirectory()) {
        execute("rm -f -r " + getCommandLineString(path));
      } else {
        execute("rm -r " + getCommandLineString(path));
      }
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }
    return false;
  }

  // Check if system is mounted
  /*  private static boolean readReadWriteFile() {
      File mountFile = new File("/proc/mounts");
      StringBuilder procData = new StringBuilder();
      if (mountFile.exists()) {
          try {
              FileInputStream fis = new FileInputStream(mountFile.toString());
              DataInputStream dis = new DataInputStream(fis);
              BufferedReader br = new BufferedReader(new InputStreamReader(
                      dis));
              String data;
              while ((data = br.readLine()) != null) {
                  procData.append(data).append("\n");
              }

              br.close();
          } catch (Exception e) {
              e.printStackTrace();
              return false;
          }

          String[] tmp = procData.toString().split("\n");
          for (String aTmp : tmp) {
              // Kept simple here on purpose different devices have
              // different blocks
              if (aTmp.contains("/dev/block")
                      && aTmp.contains("/system")) {
                  if (aTmp.contains("rw")) {
                      // system is rw
                      return true;
                  } else if (aTmp.contains("ro")) {
                      // system is ro
                      return false;
                  } else {
                      return false;
                  }
              }
          }
      }
      return false;
  }
  */

  private static boolean readReadWriteFile() {
    File mountFile = new File("/proc/mounts");
    if (!mountFile.exists()) {
      return false;
    }

    List<String> lines = readLinesFromFile(mountFile);
    return checkSystemPartition(lines);
  }

  private static List<String> readLinesFromFile(File file) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      while ((line = br.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }
    return lines;
  }

  private static boolean checkSystemPartition(List<String> lines) {
    for (String line : lines) {
      if (isSystemPartition(line)) {
        return isReadWrite(line);
      }
    }
    return false;
  }

  private static boolean isSystemPartition(String line) {
    return line.contains("/dev/block") && line.contains("/system");
  }

  private static boolean isReadWrite(String line) {
    return line.contains("rw");
  }

  private static boolean containsIllegals2(String toExamine) {
    // checks for "+" sign so the program doesn't throw an error when its
    // not erroring.
    Pattern pattern = Pattern.compile("[+]");
    Matcher matcher = pattern.matcher(toExamine);
    return matcher.find();
  }

  private static synchronized ArrayList<String> execute(String cmd) {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final ArrayList<String> list = new ArrayList<>();
    final AtomicReference<ArrayList<String>> resultRef = new AtomicReference<>();
    Command command =
        new Command(0, cmd) {
          @Override
          public void commandOutput(int id, String line) {
            super.commandOutput(id, line);
            list.add(line);
          }

          @Override
          public void commandTerminated(int id, String reason) {
            super.commandTerminated(id, reason);
          }

          @Override
          public void commandCompleted(int id, int exitcode) {
            super.commandCompleted(id, exitcode);
            resultRef.set(list);
            countDownLatch.countDown();
          }
        };
    try {
      RootTools.getShell(true).add(command);
      countDownLatch.await();
    } catch (IOException | RootDeniedException | TimeoutException | InterruptedException e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }
    return resultRef.get();
  }

//  private static InputStream openFile(String cmd) {
//    InputStream inputStream;
//    try {
//      Process process = Runtime.getRuntime().exec("su");
//      DataOutputStream os = new DataOutputStream(process.getOutputStream());
//      os.writeBytes(cmd + "\n");
//      os.writeBytes("exit\n");
//      inputStream = process.getInputStream();
//      String err = (new BufferedReader(new InputStreamReader(process.getErrorStream()))).readLine();
//      os.flush();
//
//      if (process.waitFor() != 0 || (!"".equals(err) && null != err) && !containsIllegals(err)) {
//        Log.e("Root Error, cmd: " + cmd, err);
//        return null;
//      }
//      return inputStream;
//    } catch (Exception e) {
//      logger.error("An error occurred while getting the file from path: {}", e);
//    }
//    return null;
//  }

    
        public static InputStream openFile(String cmd) {
        if (cmd == null || cmd.trim().isEmpty()) {
            throw new IllegalArgumentException("Command cannot be null or empty");
        }

        try (Process process = Runtime.getRuntime().exec("su");
             DataOutputStream os = new DataOutputStream(process.getOutputStream());
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            // Write the command and exit to the process
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

            // Read the error stream
            StringBuilder errorOutput = new StringBuilder();
            String line;
            while ((line = errorReader.readLine()) != null) {
                errorOutput.append(line).append("\n");
            }

            // Wait for the process to complete with a timeout
            if (!process.waitFor(10, TimeUnit.SECONDS)) { // 10 seconds timeout
                process.destroyForcibly(); // Terminate the process if it exceeds the timeout
                logger.error("Command execution timed out: " + cmd);
                return null;
            }

            int exitCode = process.exitValue();
            String err = errorOutput.toString().trim();

            if (exitCode != 0 || (!err.isEmpty() && !containsIllegals(err))) {
                logger.error( "Root Error, cmd: " + cmd + ", error: " + err);
                return null;
            }

            return process.getInputStream();
        } catch (IOException | InterruptedException e) {
            logger.error("An error occurred while executing the command: " + cmd, e);
        }
        return null;
    }

 private static boolean containsIllegals(String err) {
    if (err == null) {
        return false;
    }

    // List of sensitive keywords to filter out
    String[] sensitiveKeywords = {
        "password",
        "secret",
        "key",
        "token",
        "credential"
    };

    // Check if the error message contains any of the sensitive keywords
    for (String keyword : sensitiveKeywords) {
        if (err.toLowerCase().contains(keyword)) {
            return true;
        }
    }

    return false;
}

    
  public static boolean changeGroupOwner(File file, String owner, String group) {
    try {
      if (!readReadWriteFile()) RootTools.remount(file.getAbsolutePath(), "rw");

      execute("chown " + owner + "." + group + " " + getCommandLineString(file.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }

    return false;
  }

  public static boolean applyPermissions(File file, Permissions permissions) {
    try {
      if (!readReadWriteFile()) RootTools.remount(file.getAbsolutePath(), "rw");

      execute(
          "chmod "
              + Permissions.toOctalPermission(permissions)
              + " "
              + getCommandLineString(file.getAbsolutePath()));
      return true;
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }

    return false;
  }

  public static String[] getFileProperties(File file) {
    String[] info = null;

    try {
      ArrayList<String> listFiles =
          execute("ls -l " + getCommandLineString(file.getAbsolutePath()));
      for (String line : listFiles) {
        info = getAttrs(line);
      }
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }

    return info;
  }

  private static String[] getAttrs(String string) {
    if (string.length() < 44) {
      throw new IllegalArgumentException("Bad ls -l output: " + string);
    }

    final char[] chars = string.toCharArray();
    final String[] results = new String[11];
    final StringBuilder current = new StringBuilder();

    for (int i = 0, ind = 0; i < chars.length; i++) {
      if (handleChar(chars[i], current, results, ind)) {
        ind++;
        if (ind == 10) {
          results[ind] = string.substring(i).trim();
          return results;
        }
      }
    }

    // If we reach here, it means the input string did not contain enough fields
    throw new IllegalArgumentException("Bad ls -l output: " + string);
  }

  private static boolean handleChar(char c, StringBuilder current, String[] results, int ind) {
    if (c == ' ' || c == '\t') {
      if (current.length() > 0) {
        results[ind] = current.toString();
        current.setLength(0);
        return true;
      }
    } else {
      current.append(c);
    }
    return false;
  }

  //    private static String[] getAttrs(String string) {
  //        if (string.length() < 44) {
  //            throw new IllegalArgumentException("Bad ls -l output: " + string);
  //        }
  //        final char[] chars = string.toCharArray();
  //
  //        final String[] results = new String[11];
  //        int ind = 0;
  //        final StringBuilder current = new StringBuilder();
  //
  //        Loop:
  //        for (int i = 0; i < chars.length; i++) {
  //            switch (chars[i]) {
  //                case ' ':
  //                case '\t':
  //                    if (current.length() != 0) {
  //                        results[ind] = current.toString();
  //                        ind++;
  //                        current.setLength(0);
  //                        if (ind == 10) {
  //                            results[ind] = string.substring(i).trim();
  //                            break Loop;
  //                        }
  //                    }
  //                    break;
  //
  //                default:
  //                    current.append(chars[i]);
  //                    break;
  //            }
  //        }
  //
  //        return results;
  //    }

  public static long getTimeinMillis(String date) {
    long timeInMillis = 0;
    try {
      timeInMillis = simpledateformat.parse(date).getTime();
    } catch (Exception e) {
      logger.error("An error occurred while getting the file from path: {}", e);
    }
    return timeInMillis;
  }
}
