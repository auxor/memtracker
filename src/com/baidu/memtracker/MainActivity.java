
package com.baidu.memtracker;

import java.io.FileWriter;
import java.io.IOException;

import com.baidu.memtracker.util.ShellHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends Activity {

    private Button mSet;
    private Button mExport;
    private Button mGet;
    private Spinner mSpinner;

    private ArrayAdapter<String> adapter;
    private int mInterval = 0;
    private int mSelectInterval = 30;
    private int DEFAULT_SELECT = 2; //30s

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    public void initUI() {
        mSet = (Button) this.findViewById(R.id.setButton);
        mExport = (Button) this.findViewById(R.id.exportButton);
        mSpinner = (Spinner) this.findViewById(R.id.spinner);
        mGet = (Button) this.findViewById(R.id.getButton);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, this.getResources().getStringArray(R.array.snap_interval));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        mSpinner.setSelection(DEFAULT_SELECT);

        mSet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectInterval != mInterval) {
                    // start the service
                    mInterval = mSelectInterval;
                    Intent intent = new Intent();
                    intent.setClass(MainActivity.this.getApplicationContext(), MemSnapService.class);
                    intent.putExtra("SECOND", mInterval);
                    MainActivity.this.startService(intent);
                }
            }

        });

        mExport.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MemInfoHelper mMemInfoHelper = new MemInfoHelper(MainActivity.this
                        .getApplicationContext());
                mMemInfoHelper.exportResult();
            }

        });

        mGet.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new GetInfoTask().execute();
                mGet.setEnabled(false);
            }

        });
    }

    private class GetInfoTask extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... params) {
            ShellHelper shell = new ShellHelper();
            FileWriter fw = shell.getFileWriter();
            if (fw != null) {
                shell.exeShellWithRoot(new String[] {
                        "dumpsys activity", "dumpsys meminfo", "top -n 1 -d 1 -m 30 -t"
                }, fw);
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return 1;
            } else {
                return -1;
            }
        }

        protected void onPostExecute(Integer result) {
            if (result != -1) {
                // toast success
                Toast.makeText(MainActivity.this, "Has exported to sdcard/memtracker/*.log",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Can not exported to sdcard/memtracker/*.log",
                        Toast.LENGTH_LONG).show();
            }
            mGet.setEnabled(true);
        }
    }

    class SpinnerSelectedListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            switch (arg2) {
                case 0:
                    mSelectInterval = 15;
                    break;
                case 1:
                    mSelectInterval = 30;
                    break;
                case 2:
                    mSelectInterval = 60;
                    break;
                case 3:
                    mSelectInterval = 2*60;
                case 4:
                    mSelectInterval = 5*60;
                case 5:
                    mSelectInterval = 10*60;
                case 6:
                    mSelectInterval = 30*60;
                    break;
                default:
                    mSelectInterval = 30;
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub
        }
    }

}
