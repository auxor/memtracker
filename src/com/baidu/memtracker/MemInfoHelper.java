
package com.baidu.memtracker;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.baidu.memtracker.util.LogHelper;

import android.app.ActivityManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Debug.MemoryInfo;
import android.os.Environment;
import android.widget.Toast;

public class MemInfoHelper {
    Context mContext;
    ActivityManager mActivityManager;
    MemDbHelper mdbHelper;

    public MemInfoHelper(Context context) {
        mContext = context;
        mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        mdbHelper = new MemDbHelper(mContext);
    }

    public void snapCurrentMeminfo() {
        LogHelper.i("TAG", "snapCurrentMeminfo");
        List<ActivityManager.RunningAppProcessInfo> processes = mActivityManager
                .getRunningAppProcesses();
        final int NP = processes != null ? processes.size() : 0;
        int[] pids = new int[NP];
        for (int i = 0; i < NP; i++) {
            ActivityManager.RunningAppProcessInfo pi = processes.get(i);
            pids[i] = pi.pid;
        }

        MemoryInfo[] mem = mActivityManager.getProcessMemoryInfo(pids);
        long avaMem = getTotalUsedSize();
        long snaptime = System.currentTimeMillis();
        SQLiteDatabase db = mdbHelper.getWritableDatabase();

        // put the total used memory
        ContentValues values = new ContentValues();
        values.put(MemDbHelper.TABLE_CLOUMN_NAME, MemDbHelper.TABLE_ROW_SYSTEM);
        values.put(MemDbHelper.TABLE_CLOUMN_TIME, snaptime);
        values.put(MemDbHelper.TABLE_CLOUMN_MEM, avaMem);
        db.insert(MemDbHelper.TABLE_NAME, null, values);

        for (int i = 0; i < NP; i++) {
            ActivityManager.RunningAppProcessInfo info = processes.get(i);
            MemoryInfo memInfo = mem[i];
            LogHelper.i("TAG", "" + info.processName + " " + memInfo.getTotalPss());
            values.put(MemDbHelper.TABLE_CLOUMN_NAME, info.processName);
            values.put(MemDbHelper.TABLE_CLOUMN_TIME, snaptime);
            values.put(MemDbHelper.TABLE_CLOUMN_MEM, memInfo.getTotalPss());
            db.insert(MemDbHelper.TABLE_NAME, null, values);
        }
        db.close();
    }

    public long getTotalUsedSize() {
        MemInfoReader mMemInfoReader = new MemInfoReader();
        mMemInfoReader.readMemInfo();
        long freeMem = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize();
        long totalMem = mMemInfoReader.getTotalSize();
        return (totalMem - freeMem)/1024;
    }

    public void exportResult() {
        long curTime = System.currentTimeMillis();
        SQLiteDatabase db = mdbHelper.getReadableDatabase();

        // first get the system's total memory
        Cursor c = db.query(MemDbHelper.TABLE_NAME, MemDbHelper.ALL_CLOUMN,
                MemDbHelper.TABLE_CLOUMN_TIME + " < " + curTime + " and "
                        + MemDbHelper.TABLE_CLOUMN_NAME + " = '" + MemDbHelper.TABLE_ROW_SYSTEM
                        + "'", null, null, null, MemDbHelper.TABLE_CLOUMN_TIME);
        if (c == null) {
            db.close();
            return;
        }

        int count = 0;
        long timeIntervals[] = null;
        String timeIntervalsStr = "TIME";
        String memStr = "TOTAL";
        count = c.getCount();
        if (count > 0) {
            timeIntervals = new long[count];
        }
        int i = 0;
        while (c.moveToNext()) {
            timeIntervals[i] = c.getLong(2);
            timeIntervalsStr += "," + formatTime(timeIntervals[i]);
            memStr += "," + formatMem(c.getLong(1));
            i++;
        }

        File sdcardDir = null;
        String outputDirName = null;
        String outputFileName = null;
        FileWriter fw = null;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            sdcardDir = Environment.getExternalStorageDirectory();
            outputDirName = sdcardDir.getAbsolutePath() + "/memtracker/";
            outputFileName = outputDirName + curTime + ".csv";
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
                    fw.write(timeIntervalsStr);
                    fw.write("\n");
                    fw.write(memStr);
                    fw.write("\n");

                    // read the all items
                    Cursor all = db.query(MemDbHelper.TABLE_NAME, MemDbHelper.ALL_CLOUMN,
                            MemDbHelper.TABLE_CLOUMN_TIME + " < " + curTime + " and "
                                    + MemDbHelper.TABLE_CLOUMN_NAME + " <> '"
                                    + MemDbHelper.TABLE_ROW_SYSTEM + "'", null, null, null,
                            MemDbHelper.TABLE_CLOUMN_NAME + "," + MemDbHelper.TABLE_CLOUMN_TIME);
                    if (all == null) {
                        db.close();
                        return;
                    }
                    String procName = null;
                    long procTime = 0;
                    String procMem = null;
                    String preProcName = null;
                    String outStr = null;
                    int j = 0;
                    while (all.moveToNext()) {
                        procName = all.getString(0);
                        procMem = formatMem(all.getLong(1));
                        procTime = all.getLong(2);
                        LogHelper.i("TAG", "export name,mem,time " + procName + "," + procMem + ","
                                + procTime);
                        if (preProcName == null) {
                            preProcName = procName;
                            outStr = procName;
                        }
                        // read one process finish
                        if (!preProcName.equals(procName)) {
                          //skip intervals after
                            while (j < count) {
                                outStr += ",0";
                                j++;
                            }
                            fw.write(outStr);
                            fw.write("\n");

                            // new process start
                            preProcName = procName;
                            outStr = procName;
                            j = 0;
                        }
                        //skip intervals before
                        while (j < count && procTime != timeIntervals[j]) {
                            outStr += ",0";
                            j++;
                        }
                        outStr += "," + procMem;
                        j++;
                    }
                    if (outStr != null) {
                        fw.write(outStr);
                        fw.write("\n");
                    }
                    // toast success
                    Toast.makeText(mContext, "Has exported to " + outputFileName, Toast.LENGTH_LONG)
                            .show();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    try {
                        if (fw != null) {
                            fw.flush();
                            fw.close();
                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        } else {
            // toast
            Toast.makeText(mContext, "Cannot write to sdcard.", Toast.LENGTH_LONG).show();
        }

        c.close();
        db.close();
    }

    private String formatMem(long kb) {
        float mb = (float) kb / 1024;
        return String.format("%.02f", mb);
    }

    private String formatTime(long millSeconds) {
        Date date = new Date(millSeconds);
        return String.format("%02d:%02d:%02d", date.getHours(), date.getMinutes(), date.getSeconds());
    }
}
