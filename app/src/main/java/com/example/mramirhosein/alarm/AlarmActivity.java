/* Copyright 2014 Sheldon Neilson www.neilson.co.za
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.example.mramirhosein.alarm;

import java.util.List;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Handler;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DigitalClock;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mramirhosein.alarm.alert.AlarmAlert;
import com.example.mramirhosein.alarm.alert.MathProblem;
import com.example.mramirhosein.alarm.customanalogclockview.CustomAnalogClock;
import com.example.mramirhosein.alarm.database.Database;
import com.example.mramirhosein.alarm.preferences.AlarmPreferencesActivity;

public class AlarmActivity extends BaseActivity {

    ImageButton newButton;
    ListView mathAlarmListView;
    AlarmListAdapter alarmListAdapter;
    public final static int PERMISSION_ALL = 1;
    public final static String[] PERMISSIONS = {
            Manifest.permission.VIBRATE,
            Manifest.permission.RECEIVE_BOOT_COMPLETED,
            Manifest.permission.EXPAND_STATUS_BAR,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.CAMERA,
            Manifest.permission.VIBRATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);
        Typeface face = Typeface.createFromAsset(getAssets(),
                "Fonts/digital.ttf");
        DigitalClock dClock = (DigitalClock) findViewById(R.id.dClock);
        dClock.setTypeface(face);

        try {


            Database.init(AlarmActivity.this);




            mathAlarmListView = (ListView) findViewById(R.id.list_alarms);
            mathAlarmListView.setLongClickable(true);
            mathAlarmListView
                    .setOnItemLongClickListener(new OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> adapterView,
                                                       View view, int position, long id) {
                            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                            final Alarm alarm = (Alarm) alarmListAdapter
                                    .getItem(position);
                            Builder dialog = new AlertDialog.Builder(
                                    AlarmActivity.this);
                            dialog.setTitle("Delete");
                            dialog.setMessage("Delete this alarm?");
                            dialog.setPositiveButton("Ok", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog,
                                                    int which) {

                                    Database.init(AlarmActivity.this);
                                    Database.deleteEntry(alarm);
                                    AlarmActivity.this
                                            .callMathAlarmScheduleService();

                                    updateAlarmList();
                                }
                            });
                            dialog.setNegativeButton("Cancel",
                                    new OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            dialog.dismiss();
                                        }
                                    });

                            dialog.show();

                            return true;
                        }
                    });

            callMathAlarmScheduleService();

            alarmListAdapter = new AlarmListAdapter(this);
            this.mathAlarmListView.setAdapter(alarmListAdapter);
            mathAlarmListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> arg0, View v, int position,
                                        long id) {
//                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
//                    Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
//                    Intent intent = new Intent(AlarmActivity.this,
//                            AlarmPreferencesActivity.class);
//                    intent.putExtra("alarm", alarm);
//                    startActivity(intent);


                    v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                    Alarm alarm = (Alarm) alarmListAdapter.getItem(position);
                    Intent intent = new Intent(AlarmActivity.this,
                            customAlarmDialog.class);
                    intent.putExtra("alarm", alarm);
                    startActivity(intent);

                }

            });

            mathAlarmListView.setSelector(R.drawable.transparent_selector);
            if (!hasPermissions(this, PERMISSIONS)) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
            }


        } catch (Exception e) {
            Log.e("dbg","1AlarmActivity : "+e.getMessage(),e);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.findItem(R.id.menu_item_save1).setVisible(false);
        menu.findItem(R.id.menu_item_delete1).setVisible(false);
        return result;
    }

    @Override
    protected void onPause() {
        // setListAdapter(null);
        Database.deactivate();
        super.onPause();
    }

    @Override
    protected void onResume() {
        checkDrawOverlayPermission();
        super.onResume();
        updateAlarmList();
    }

    public void updateAlarmList() {
        try {
            Database.init(AlarmActivity.this);
            final List<Alarm> alarms = Database.getAll();
            alarmListAdapter.setMathAlarms(alarms);
            runOnUiThread(new Runnable() {
                public void run() {
                    // reload content
                    AlarmActivity.this.alarmListAdapter.notifyDataSetChanged();
                    if (alarms.size() > 0) {
                        findViewById(R.id.empty).setVisibility(View.INVISIBLE);
                    } else {
                        findViewById(R.id.empty).setVisibility(View.VISIBLE);
                    }
                }
            });
        } catch (Exception e) {
            Log.e("dbg","2AlarmActivity : "+e.getMessage(),e);

        }

    }

    public void onClickNew(View view) {
        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
//        Intent newAlarmIntent = new Intent(this,
//                AlarmPreferencesActivity.class);
//        startActivity(newAlarmIntent);

//        customAlarmDialog fragment1 = new customAlarmDialog();
//        fragment1.mListener = this;
//        fragment1.text = "sd";
//        fragment1.show(getFragmentManager(), "");

        Intent newAlarmIntent = new Intent(this,
                customAlarmDialog.class);
        startActivity(newAlarmIntent);

    }

//    @SuppressLint("NewApi")
//    public void checkDrawOverlayPermission() {
//        /** check if we already  have permission to draw over other apps */
//        if (!Settings.canDrawOverlays(getApplicationContext())) {
//            /** if not construct intent to request permission */
//            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                    Uri.parse("package:" + getPackageName()));
//            /** request permission via start activity for result */
//            startActivityForResult(intent, REQUEST_CODE);
//        }
//    }
//
//    @SuppressLint("NewApi")
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        /** check if received result code
//         is equal our requested code for draw permission  */
//        if (requestCode == REQUEST_CODE) {
//            if (Settings.canDrawOverlays(this)) {
//                // continue here - permission was granted
//                finish();
//            }
//        }
//    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    private Alarm mAlarm ;
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.checkBox_alarm_active1:
                Database.init(AlarmActivity.this);
                CheckBox checkBox = (CheckBox) v;
                mAlarm = (Alarm) alarmListAdapter.getItem((Integer) checkBox
                        .getTag());
                mAlarm.setAlarmActive(checkBox.isChecked());
                Database.update(mAlarm);
                AlarmActivity.this.callMathAlarmScheduleService();
                if (checkBox.isChecked()) {
                    Toast.makeText(AlarmActivity.this,
                            mAlarm.getTimeUntilNextAlarmMessage(), Toast.LENGTH_LONG)
                            .show();
                }
                break;

            case R.id.imageView_alarm_delete:
                ImageView imageView=(ImageView)v;
                mAlarm = (Alarm) alarmListAdapter.getItem((Integer) imageView
                        .getTag());
                Builder dialog = new AlertDialog.Builder(
                        AlarmActivity.this);
                dialog.setTitle("Delete");
                dialog.setMessage("Delete this alarm?");
                dialog.setPositiveButton("Ok", new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,
                                        int which) {

                        Database.init(AlarmActivity.this);

                        Database.deleteEntry(mAlarm);
                        AlarmActivity.this
                                .callMathAlarmScheduleService();

                        updateAlarmList();
                    }
                });
                dialog.setNegativeButton("Cancel",
                        new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.dismiss();
                            }
                        });

                dialog.show();


                break;

        }


    }

    private static Boolean exit = false;

    @Override
    public void onBackPressed() {
        if (exit) {
            finish(); // finish activity
        } else {
            Toast.makeText(getApplicationContext(), "Press Back again to Exit.",
                    Toast.LENGTH_SHORT).show();
            exit = true;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    exit = false;
                }
            }, 3 * 1000);

        }
    }

    public final static int REQUEST_CODE = -1010101;

    @SuppressLint("NewApi")
    public void checkDrawOverlayPermission() {

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /** check if we already  have permission to draw over other apps */
            if (!Settings.canDrawOverlays(getApplicationContext())) {
                /** if not construct intent to request permission */
                Toast.makeText(
                        getApplicationContext(), "Hi\nPlz, Check  Permission", Toast.LENGTH_LONG)
                        .show();
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                /** request permission via start activity for result */
                startActivityForResult(intent, REQUEST_CODE);
            }
        }
    }


}