package com.example.mramirhosein.alarm;

/**
 * Created by Mr.Amirhosein on 11/24/2016.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.*;

import com.example.mramirhosein.alarm.alert.MathProblem;
import com.example.mramirhosein.alarm.barcode.BarCode;
import com.example.mramirhosein.alarm.database.Database;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.Calendar;

public class customAlarmDialog extends BaseActivity {

    private TextView textView_alarm_dialog_time;
    private CheckBox checkBox_alarm_dialog_repeat;
    private LinearLayout linearLayout_alarm_dialog_day;
    private CheckBox[] checkBox_days = new CheckBox[7];
    private TextView textView_alarm_dialog_ringTone;
    private EditText editText_alarm_dialog_label;
    private CheckBox checkBox_alarm_dialog_vibrate;
    private CheckBox checkBox_alarm_dialog_flashlight;
//    private Button button_alarm_dialog_close;
//    private Button button_alarm_dialog_save;


    private CheckBox checkBox_alarm_dialog_barcode;
    private CheckBox checkBox_alarm_dialog_math;
    private CheckBox checkBox_alarm_dialog_phonenumber;


    private final String[] alarmMethods = {"Barcode", "MathProblem", "PhoneNumber"}; //
    private Alarm alarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.custom_alarm_dialog);

        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);


        Bundle bundle = getIntent().getExtras();
        if (bundle != null && bundle.containsKey("alarm")) {
            alarm = (Alarm) bundle.getSerializable("alarm");
        } else {
            alarm = new Alarm();
        }
        Typeface face = Typeface.createFromAsset(getAssets(),
                "Fonts/digital.ttf");

        textView_alarm_dialog_time = (TextView) findViewById(R.id.textView_alarm_dialog_time);
        checkBox_days[0] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_sun);
        checkBox_days[1] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_mon);
        checkBox_days[2] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_tue);
        checkBox_days[3] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_wed);
        checkBox_days[4] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_thu);
        checkBox_days[5] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_fri);
        checkBox_days[6] = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_sat);
        checkBox_alarm_dialog_repeat = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_repeat);
        linearLayout_alarm_dialog_day = (LinearLayout) findViewById(R.id.linearLayout_alarm_dialog_day);
        textView_alarm_dialog_ringTone = (TextView) findViewById(R.id.textView_alarm_dialog_ringTone);
        editText_alarm_dialog_label = (EditText) findViewById(R.id.editText_alarm_dialog_label);
        checkBox_alarm_dialog_vibrate = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_vibrate);
        checkBox_alarm_dialog_flashlight = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_flashlight);
        checkBox_alarm_dialog_barcode = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_barcode);
        checkBox_alarm_dialog_math = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_math);
        checkBox_alarm_dialog_phonenumber = (CheckBox) findViewById(R.id.checkBox_alarm_dialog_phonenumber);

//        button_alarm_dialog_close = (Button) findViewById(R.id.button_alarm_dialog_close);
//        button_alarm_dialog_save = (Button) findViewById(R.id.button_alarm_dialog_save);

        textView_alarm_dialog_time.setTypeface(face);

        textView_alarm_dialog_time.setText(alarm.getAlarmTimeString());

        for (Alarm.Day day : alarm.getDays()) {
            checkBox_days[day.ordinal()].setChecked(true);
            checkBox_alarm_dialog_repeat.setChecked(true);
        }
        Uri alarmToneUri = Uri.parse(alarm.getAlarmTonePath());
        Ringtone alarmTone = RingtoneManager.getRingtone(getApplicationContext(), alarmToneUri);
        alarm.setAlarmTonePath(alarm.getAlarmTonePath());
        textView_alarm_dialog_ringTone.setText("Ringtone(" + alarmTone.getTitle(getApplicationContext()) + ")");
        editText_alarm_dialog_label.setText(alarm.getAlarmName());
        checkBox_alarm_dialog_vibrate.setChecked(alarm.getVibrate());


        Log.d("dbg","CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCVVVVV                "+ Settings.System.DEFAULT_ALARM_ALERT_URI.getPath());


        setViewMethods();

        Repeat(checkBox_alarm_dialog_repeat);




    }


    @Override
    public void onDestroy() {
        Database.deactivate();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Save?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onClickSave();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onClickClose() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to Save?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onClickSave();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }


    public void onCheckedVibrator() {
        alarm.setVibrate(checkBox_alarm_dialog_vibrate.isChecked());
        if (checkBox_alarm_dialog_vibrate.isChecked()) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(1000);
        }
    }


    public void onClickPhonenumber() {
        if (checkBox_alarm_dialog_phonenumber.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            final EditText input = new EditText(
                    customAlarmDialog.this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            input.setText(alarm.getPhonenumber());
            builder.setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alarm.setPhonenumber(input
                                    .getText().toString());
                            setViewMethods();
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setViewMethods();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setView(input);
            alert.show();
        } else {
            alarm.notSetPhonenumber();
        }
    }


    public void onClickMathProblem() {
        if (checkBox_alarm_dialog_math.isChecked()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LinearLayout layout = new LinearLayout(customAlarmDialog.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            final TextView tv_seekBar = new TextView(
                    customAlarmDialog.this);
            final SeekBar input = new SeekBar(
                    customAlarmDialog.this);
            input.setMax(10);
            input.setMax(7);
            if (alarm.isSetMathLevel())
                input.setProgress(Integer.parseInt(alarm.getMathLevel()));

            tv_seekBar.setText(alarm.getMathLevel());
            layout.addView(tv_seekBar);
            layout.addView(input);

            input.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    // TODO Auto-generated method stub
                    tv_seekBar.setText("Level " + String.valueOf(progress) + "\n" + new MathProblem(progress + 3).toString());
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // TODO Auto-generated method stub
                }
            });


            builder.setCancelable(false)
                    .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            alarm.setMathLevel(String.valueOf(input.getProgress() + 3));
                            setViewMethods();
                        }
                    })
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            setViewMethods();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.setView(layout);
            alert.show();
        } else {
            alarm.notSetMathLevel();
        }
    }

    public void onClickBarcode() {
        if (checkBox_alarm_dialog_barcode.isChecked()) {
            try {
                if (!AlarmActivity.hasPermissions(getApplicationContext(), AlarmActivity.PERMISSIONS)) {
                    Toast.makeText(
                            getApplicationContext(), "PERMISSIONS NOT FOUND", Toast.LENGTH_LONG)
                            .show();
                    finish();
                }
                new IntentIntegrator(customAlarmDialog.this).setOrientationLocked(true).setCaptureActivity(BarCode.class).initiateScan();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext().getApplicationContext(),
                        e.getMessage(), Toast.LENGTH_LONG)
                        .show();
            }
        } else {
            alarm.notSetBarcode();
        }
    }


    public void onClickTime() {
        //R.style.DialogTheme,
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                customAlarmDialog.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(
                            TimePicker timePicker, int hours,
                            int minutes) {
                        Calendar newAlarmTime = Calendar
                                .getInstance();
                        newAlarmTime.set(Calendar.HOUR_OF_DAY,
                                hours);
                        newAlarmTime.set(Calendar.MINUTE,
                                minutes);
                        newAlarmTime.set(Calendar.SECOND, 0);
                        alarm.setAlarmTime(newAlarmTime);
                        textView_alarm_dialog_time.setText(alarm.getAlarmTimeString());
                    }
                }, alarm.getAlarmTime().get(
                Calendar.HOUR_OF_DAY), alarm
                .getAlarmTime().get(Calendar.MINUTE),
                true);
        timePickerDialog.show();
    }

    private void setDay(int i) {
        if (checkBox_alarm_dialog_repeat.isChecked())
            if (checkBox_days[i].isChecked())
                alarm.addDay(Alarm.Day.values()[i]);
            else
                alarm.removeDay(Alarm.Day.values()[i]);
        else
            alarm.removeDay(Alarm.Day.values()[i]);
    }

    public void days(View view) {
        switch (view.getId()) {
            case R.id.checkBox_alarm_dialog_sun:
                setDay(0);
                break;
            case R.id.checkBox_alarm_dialog_mon:
                setDay(1);
                break;
            case R.id.checkBox_alarm_dialog_tue:
                setDay(2);
                break;
            case R.id.checkBox_alarm_dialog_wed:
                setDay(3);
                break;
            case R.id.checkBox_alarm_dialog_thu:
                setDay(4);
                break;
            case R.id.checkBox_alarm_dialog_fri:
                setDay(5);
                break;
            case R.id.checkBox_alarm_dialog_sat:
                setDay(6);
                break;
        }
    }

    public void Repeat(View view) {
        if (checkBox_alarm_dialog_repeat.isChecked()) {
            linearLayout_alarm_dialog_day.setVisibility(View.VISIBLE);
        } else {
            linearLayout_alarm_dialog_day.setVisibility(View.GONE);
        }
        for (int i = 0; i < checkBox_days.length; i++)
            setDay(i);
    }

    public void onClickSave() {
        if (alarm.isSetBarcode()||alarm.isSetMathLevel()||alarm.isSetPhonenumber()) {
            alarm.setAlarmName(editText_alarm_dialog_label.getText().toString());
            alarm.setDifficulty(Alarm.Difficulty.values()[0]);
            alarm.setAlarmActive(true);
            Database.init(getApplicationContext());
            if (alarm.getId() < 1) {
                Database.create(alarm);
            } else {
                Database.update(alarm);
            }
            Database.deactivate();
            callMathAlarmScheduleService();
            Toast.makeText(getApplicationContext(),
                    alarm.getTimeUntilNextAlarmMessage(),
                    Toast.LENGTH_LONG).show();
            finish();

        } else {
            Toast.makeText(getApplicationContext(),
                    "Method not set ... ",
                    Toast.LENGTH_LONG).show();
        }
    }

    private AlertDialog.Builder alertMethods;

//    public void onClickMethods() {
//        alertMethods = new AlertDialog.Builder(
//                customAlarmDialog.this);
//        CharSequence[] items = new CharSequence[alarmMethods.length];
//        for (int i = 0; i < items.length; i++)
//            items[i] = alarmMethods[i];
//
//        boolean[] checkedItems = new boolean[]{alarm.isSetBarcode(),alarm.isSetMathLevel(),alarm.isSetPhonenumber()};
//        alertMethods.setMultiChoiceItems(items, checkedItems,
//                new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(
//                            final DialogInterface dialog,
//                            int which, boolean isChecked) {
//                        if(isChecked)
//                        switch (which) {
//                            case 0: //barcode
//                                Log.d("dbg","s1");
//                                onClickBarcode();
//                                Log.d("dbg","s2");
//
//                                break;
//
//                            case 1:
//                                Log.d("dbg","s3");
//                                onClickMathProblem();
//                                Log.d("dbg","s4");
//                                break;
//
//                            case 2: //phonenumber
//                                Log.d("dbg","s5");
//                                onClickPhonenumber();
//                                Log.d("dbg","s6");
//                                break;
//                        }
//
//                    }
//                });
//        alertMethods.show();
//    }

    private CountDownTimer alarmToneTimer;
    private Uri currenturi = null;

    public void onClickRingTone() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select ringtone for Alarm:");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currenturi);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM);
        startActivityForResult(intent, 999);
    }


    private void msg(Object msg) {
        Toast.makeText(getApplicationContext(), String.valueOf(msg),
                Toast.LENGTH_SHORT).show();
    }





    public static String getRingtonePathFromContentUri(Context context,
                                                       Uri contentUri) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Cursor ringtoneCursor = context.getContentResolver().query(contentUri,
                proj, null, null, null);
        ringtoneCursor.moveToFirst();
        String path = ringtoneCursor.getString(ringtoneCursor
                .getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));

        ringtoneCursor.close();
        return path;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(requestCode==999){
            if(data!=null){
                currenturi=data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                alarm.setAlarmTonePath(String.valueOf(currenturi));
                Ringtone alarmTone = RingtoneManager.getRingtone(getApplicationContext(), currenturi);
                if (alarmTone instanceof Ringtone && !alarm.getAlarmTonePath().equalsIgnoreCase("")) {
                    textView_alarm_dialog_ringTone.setText("Ringtone(" + alarmTone.getTitle(getApplicationContext()) + ")");
                    Log.d("dbg","CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCVVVVV                "+getRingtonePathFromContentUri(getApplicationContext(),currenturi));
                }
            }
        }
        if (result != null) {
            if (result.getContents() == null) {
                msg("Cancelled");
            } else {
                alarm.setBarcode(result.getContents().trim());
                msg("Scanned: " + result.getContents().trim());
            }
            setViewMethods();

        } else {
            // This is important, otherwise the result will not be passed to the fragment
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onPause() {
        Log.d("dbg", "onPause");
        super.onPause();
    }

    @Override
    public void onClick(View v) {

        try {
            switch (v.getId()) {
                case R.id.textView_alarm_dialog_time:
                    onClickTime();
                    break;
                case R.id.textView_alarm_dialog_ringTone:
                    onClickRingTone();
                    break;
//                case R.id.button_alarm_dialog_save:
//                    onClickSave();
//                    break;

//                case R.id.button_alarm_dialog_close:
//                    onClickClose();
//                    break;
                case R.id.checkBox_alarm_dialog_barcode:
                    onClickBarcode();
                    break;

                case R.id.checkBox_alarm_dialog_math:
                    onClickMathProblem();
                    break;

                case R.id.checkBox_alarm_dialog_phonenumber:
                    onClickPhonenumber();
                    break;
                case R.id.checkBox_alarm_dialog_vibrate:
                    onCheckedVibrator();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            msg("Error : " + e.getMessage());
        }
    }


    private void setViewMethods() {
        checkBox_alarm_dialog_barcode.setChecked(alarm.isSetBarcode());
        checkBox_alarm_dialog_math.setChecked(alarm.isSetMathLevel());
        checkBox_alarm_dialog_phonenumber.setChecked(alarm.isSetPhonenumber());
    }


}