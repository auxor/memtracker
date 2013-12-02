
package com.baidu.memtracker;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MemSnapService extends Service {
    MemInfoHelper mMeminfo;
    private int mSecond = 1 * 60; // 1 minute

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        mMeminfo = new MemInfoHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("TAG", "onStartCommand");
        if (intent != null) {
            mSecond = intent.getIntExtra("SECOND", 1 * 60);
        }
        startTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopTimer();
    }

    private class SnapMemTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object... params) {
            mMeminfo.snapCurrentMeminfo();
            return null;
        }
    }

    Handler mTimerhandler = new Handler();
    Runnable mTimerRunnable = new Runnable() {
        @Override
        public void run() {
            mTimerhandler.postDelayed(this, mSecond * 1000);
            (new SnapMemTask()).execute(null);
        }
    };

    private void startTimer() {
        mTimerhandler.removeCallbacks(mTimerRunnable);
        mTimerhandler.postDelayed(mTimerRunnable, mSecond * 1000);
    }

    private void stopTimer() {
        mTimerhandler.removeCallbacks(mTimerRunnable);
    }
}
