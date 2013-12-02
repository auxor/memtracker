
package com.baidu.memtracker.util;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import android.os.Environment;
import android.util.Log;

public class ShellHelper {
    public void exeShell(String cmd, FileWriter fw) {
        Log.i("exeShell", "cmd " + cmd);
        try {
            fw.write("Exec Shell :" + cmd + "\n");
            Process p = Runtime.getRuntime().exec(cmd);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                Log.i("exeShell", line);
                fw.write(line);
                fw.write("\n");
            }
            fw.flush();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public void exeShellWithRoot(String[] cmds, FileWriter fw) {
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : cmds) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = null;
            while ((line = in.readLine()) != null) {
                Log.i("exeShell", line);
                fw.write(line);
                fw.write("\n");
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public FileWriter getFileWriter() {
        long curTime = System.currentTimeMillis();
        File sdcardDir = null;
        String outputDirName = null;
        String outputFileName = null;
        FileWriter fw = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            sdcardDir = Environment.getExternalStorageDirectory();
            outputDirName = sdcardDir.getAbsolutePath() + "/memtracker/";
            outputFileName = outputDirName + "getinfo" + curTime + ".log";
            if (sdcardDir.exists()) {
                try {
                    File dir = new File(outputDirName);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
                    File file = new File(outputFileName);
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    fw = new FileWriter(outputFileName, true);
                    return fw;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                }
            }
        } else {
            return null;
        }
        return null;
    }
}
