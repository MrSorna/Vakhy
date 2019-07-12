
package com.example.mramirhosein.alarm.alert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.example.mramirhosein.alarm.Alarm;
import com.example.mramirhosein.alarm.AlarmActivity;
import com.example.mramirhosein.alarm.R;
import com.example.mramirhosein.alarm.customanalogclockview.CustomAnalogClock;
import com.example.mramirhosein.alarm.database.Database;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;


public class AlarmAlert extends Activity implements
        DecoratedBarcodeView.TorchListener {


    private MediaPlayer alarmTone;
    private MediaPlayer alarmFire;

    private Vibrator vibrator = null;
    private boolean alarmActive;
    private boolean previewing = true;
    private int volume;
    private int id = 0;
    private AudioManager audioManager;
    private long[] pattern = {1000, 200, 200, 200};

    private CaptureManager capture = null;
    private DecoratedBarcodeView barcodeScannerView;
    private ImageView switchFlashlightButton;
    private ImageView iv_status;
    private TextView lable;
    private TextView time;
    private static WindowManager wm = null;
    private static View mView = null;


    private int stateDelay = 60;
    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;


    private boolean barcodeScanned = false;
    public static Alarm alarm;
    private boolean tmp = false;

    private String barcodeData = "";
    private String mathLevel = "";

    private Typeface face;

    private ViewPager viewPager;
    private MainPagerAdapter pagerAdapter;
    ///math
    private TextView problemView;
    private TextView answerView;
    private String answerString;
    private StringBuilder answerBuilder = new StringBuilder();
    private MathProblem mathProblem;

    protected void onCreate(Bundle savedInstanceState) {
        Log.d("dbg", "onCreate");
        super.onCreate(savedInstanceState);
        try {
            if (!AlarmActivity.hasPermissions(this, AlarmActivity.PERMISSIONS)) {
                finish();
            }

            Database.init(AlarmAlert.this);
            final List<Alarm> alarms = Database.getAll();
            id = getIntent().getIntExtra("id", -1);
            for (Alarm a : alarms) {
                if (a.getAlarmActive()) {
                    if (a.getId() == id)
                        alarm = a;
                }
            }
            Log.d("dbg", "1");
            if (id == -1) {
                Log.d("dbg", "2");
                finish();
            }
            Log.d("dbg", "4");
            alarm.setFlag("false");
            Database.update(alarm);
            barcodeData = alarm.getBarcode();
            mathLevel = alarm.getMathLevel();
            face = Typeface.createFromAsset(getAssets(),
                    "Fonts/digital.ttf");
            LinearLayout mLinear = new LinearLayout(getApplicationContext()) {
                public void onCloseSystemDialogs(String reason) {
                    if ("globalactions".equals(reason)) {
                        Intent closeDialog = new Intent(
                                Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
                        getApplicationContext().sendBroadcast(closeDialog);
                    } else if ("homekey".equals(reason)) {
                    } else if ("recentapss".equals(reason)) {
                    }
                }

                @Override
                public boolean dispatchKeyEvent(KeyEvent event) {
                    if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                            || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                            || event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN
                            || event.getKeyCode() == KeyEvent.KEYCODE_CAMERA
                            || event.getKeyCode() == KeyEvent.KEYCODE_POWER) {
                        audioManager
                                .setStreamVolume(
                                        AudioManager.STREAM_ALARM,
                                        audioManager
                                                .getStreamMaxVolume(AudioManager.STREAM_ALARM) - 1,
                                        0);

//                        audioManager
//                                .setStreamVolume(
//                                        AudioManager.STREAM_ALARM,
//                                        0,
//                                        0);
                    }
                    return super.dispatchKeyEvent(event);
                }
            };
            mLinear.setFocusable(true);


            // params
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_FULLSCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;
            mView = LayoutInflater.from(getApplicationContext()).inflate(
                    R.layout.main_alarm_alert, mLinear);
            wm = (WindowManager) getApplicationContext()
                    .getSystemService(getApplicationContext().WINDOW_SERVICE);


            pagerAdapter = new MainPagerAdapter();
            viewPager = (ViewPager) mView.findViewById(R.id.viewPager);
            lable = (TextView) mView.findViewById(R.id.textView12);
            time = (TextView) mView.findViewById(R.id.tv_time);
            time.setTypeface(face);
            lable.setText(alarm.getAlarmName());

            CustomAnalogClock customAnalogClock = (CustomAnalogClock) mView.findViewById(R.id.analog_clock);
            customAnalogClock.setVisibility(customAnalogClock.VISIBLE);
            customAnalogClock.setAutoUpdate(true);


            viewPager.setAdapter(pagerAdapter);
            if (alarm.isSetBarcode()) {
                initBarcode(addView(R.layout.method_barcode));
            }
            if (alarm.isSetMathLevel()) {
                initMath(addView(R.layout.method_math));
            }

            pagerAdapter.notifyDataSetChanged();


            setCurrentPage(0);

            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    setCurrentPage(0);
                }

                @Override
                public void onPageSelected(int position) {

                }

                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });


            Log.d("dbg", String.valueOf(pagerAdapter.getCount()));

            mView.setSystemUiVisibility(flags);
            mView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                        mView.setSystemUiVisibility(flags);
                    }
                }
            });

            wm.addView(mView, params);


            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            volume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM) - 2,
                    0);
//            audioManager
//                    .setStreamVolume(
//                            AudioManager.STREAM_ALARM,
//                            0,
//                            0);
            alarmTone = new MediaPlayer();
            alarmFire = new MediaPlayer();


            if (AlarmAlert.alarm != null)
                if (AlarmAlert.alarm.getVibrate()) {
                    vibrator = (Vibrator)
                            getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(pattern, 0);
                }
            try {
                alarmTone.setVolume(1.0f, 1.0f);
                if(RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(alarm.getAlarmTonePath())).getTitle(getApplicationContext()).toLowerCase().trim().indexOf("default")!=-1){
                    alarmTone.setDataSource(this,
                            Uri.parse(alarm.getAlarmTonePath()));
                }else{
                    alarmTone.setDataSource(getRingtonePathFromContentUri(getApplicationContext(),
                            Uri.parse(AlarmAlert.alarm.getAlarmTonePath())));
                }
                alarmTone.setAudioStreamType(AudioManager.STREAM_ALARM);
                alarmTone.setLooping(true);
                alarmTone.prepare();
                alarmTone.start();
            } catch (Exception e) {
                alarmTone.release();
                alarmActive = false;
                Log.e("dbg","1AlarmAlert : "+e.getMessage(),e);
            }
            alarmActive = true;


            startDownTimer("Start State 2 : ", stateDelay);
            setStateDelay(new Runnable() {
                @Override
                public void run() {
                    try {
                        audioManager
                                .setStreamVolume(
                                        AudioManager.STREAM_ALARM,
                                        audioManager
                                                .getStreamMaxVolume(AudioManager.STREAM_ALARM),
                                        0);
//                        audioManager
//                                .setStreamVolume(
//                                        AudioManager.STREAM_ALARM,
//                                        0,
//                                        0);
                        alarmFire.setVolume(1.0f, 1.0f);

                        AssetFileDescriptor afd = getAssets().openFd("mp3/ambulancea.mp3");
                        alarmFire.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                        alarmFire.setAudioStreamType(AudioManager.STREAM_ALARM);
                        alarmFire.setLooping(true);
                        alarmFire.prepare();
                        alarmFire.start();
                        if (alarm.isSetPhonenumber()) {
                            startDownTimer("Make call " + alarm.getPhonenumber() + " ", stateDelay);
                            setStateDelay(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Intent intent = new Intent(Intent.ACTION_CALL);
                                        intent.setData(Uri.parse("tel:" + alarm.getPhonenumber()));
                                        startActivity(intent);
                                    } catch (SecurityException e) {
                                        Log.e("dbg","3AlarmAlert : "+e.getMessage(),e);
                                    }
                                }
                            }, stateDelay);
                        }
                    } catch (Exception e) {
                        alarmFire.release();
                        Log.e("dbg","2AlarmAlert : "+e.getMessage(),e);
                    }
                }
            }, stateDelay);

//            gotoAlarmActivity();
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);


            registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_ON));
            registerReceiver(mybroadcast, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        } catch (Exception e) {
            Log.e("dbg","4AlarmAlert : "+e.getMessage(),e);
        }
    }


    private BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            try {
                if (result.getText() != null) {
                    if (result.getText() != null) {
                        if (barcodeData.equals(result.getText())) {
//                            layouts.remove(0);
//                            adapter.notifyDataSetChanged();


//                            pagerAdapter.notifyDataSetChanged();
                            capture.onDestroy();
                            capture.onPause();
                            removeView(getCurrentPage());
                        } else {
                            if (!isError) {
                                isError = true;
                                startError(10);
                            }
                        }
                        barcodeScanned = true;
                    }
                }
            } catch (Exception e) {
                Log.e("dbg","5AlarmAlert : "+e.getMessage(),e);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };


    @Override
    protected void onResume() {
        Log.d("dbg", "onResume");
        StaticWakeLock.lockOn(getApplicationContext());
        super.onResume();
        if (capture != null)
            capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        StaticWakeLock.lockOff(getApplicationContext());
//        if(capture!=null)
//        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (capture != null)
            capture.onDestroy();

        unregisterReceiver(mybroadcast);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        if (capture != null)
            capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    /**
     * Check if the device's camera has a Flashlight.
     *
     * @return true if there is Flashlight, otherwise false.
     */
    private boolean hasFlash() {
        return getApplicationContext().getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    @Override
    public void onTorchOn() {
        switchFlashlightButton.setImageResource(R.drawable.flashlight_on);
    }

    @Override
    public void onTorchOff() {
        switchFlashlightButton.setImageResource(R.drawable.flashlight_off);

    }

    BroadcastReceiver mybroadcast = new BroadcastReceiver() {
        //When Event is published, onReceive method is called
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub


            if (AlarmAlert.alarm != null)
                if (AlarmAlert.alarm.getVibrate()) {
                    if (vibrator == null)
                        vibrator = (Vibrator)
                                getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(pattern, 0);
                }
        }
    };


    private void destroyView() {
        if (wm != null)
            if (mView != null)
                wm.removeView(mView);
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
        finish();
    }

    public void destroyAlarmAlert() {
        alarm.setFlag("true");
        Database.update(alarm);
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                volume, 0);
        try {
            if (vibrator != null)
                vibrator.cancel();
        } catch (Exception e) {
            Log.e("dbg","6AlarmAlert : "+e.getMessage(),e);
        }
        try {
            alarmTone.stop();
            alarmFire.stop();
        } catch (Exception e) {
            Log.e("dbg","7AlarmAlert : "+e.getMessage(),e);

        }
        try {
            alarmTone.release();
            alarmFire.release();
        } catch (Exception e) {
            Log.e("dbg","8AlarmAlert : "+e.getMessage(),e);

        }
        Database.deactivate();
    }


    public void setStateDelay(Runnable task, int seconds) {
        final Handler handler = new Handler();
        handler.postDelayed(task, seconds * 1000);
    }

    public void startDownTimer(final String lable, int seconds) {
        new CountDownTimer(seconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                time.setText(lable + String.valueOf(millisUntilFinished / 1000));
            }

            public void onFinish() {

                time.setText("No State");
            }
        }.start();
    }

//    private boolean Torch = false;
//    public void startFlashlight(int seconds) {
//        new CountDownTimer(seconds * 1000, 1000) {
//            public void onTick(long millisUntilFinished) {
//                if () {
//                    if (Torch) {
//                        barcodeScannerView.setTorchOff();
//                    } else {
//                        barcodeScannerView.setTorchOn();
//                    }
//                    Torch = !Torch;
//                }
//
//            }
//
//            public void onFinish() {
//                barcodeScannerView.setTorchOff();
//            }
//        }.start();
//    }


    private boolean isError = false;

    public void startError(int seconds) {
        new CountDownTimer(seconds * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                if (iv_status.getVisibility() == iv_status.VISIBLE) {
                    iv_status.setVisibility(iv_status.INVISIBLE);
                } else {
                    iv_status.setVisibility(iv_status.VISIBLE);
                    iv_status.setImageResource(R.drawable.wrong);
                }
            }

            public void onFinish() {
                iv_status.setVisibility(iv_status.INVISIBLE);
                isError = false;
            }
        }.start();
    }


    private void initBarcode(ViewGroup layout) {
        barcodeScannerView = (DecoratedBarcodeView) layout.findViewById(R.id.zxing_barcode_scanner);
        barcodeScannerView.setTorchListener(this);
        capture = new CaptureManager(this, barcodeScannerView);
        barcodeScannerView.decodeContinuous(callback);
        switchFlashlightButton = (ImageView) layout.findViewById(R.id.switch_flashlight);
        switchFlashlightButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tmp) {
                    barcodeScannerView.setTorchOff();
                } else {
                    barcodeScannerView.setTorchOn();
                }
                tmp = !tmp;
            }
        });

        iv_status = (ImageView) layout.findViewById(R.id.iv_status2);


        if (!hasFlash()) {
            switchFlashlightButton.setVisibility(View.GONE);
        }
        capture.onResume();
    }


    private View initVictory() {
        InputStream stream = null;
        try {
            stream = getAssets().open("raw/victory.mp4");
        } catch (IOException e) {
            Log.e("dbg","9AlarmAlert : "+e.getMessage(),e);
        }
        GifDecoderView view = new GifDecoderView(this, stream);
        return view;
    }

    private VideoView videoView;
    private void initVictory(ViewGroup layout) {
        videoView=(VideoView)layout.findViewById(R.id.videoView);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.victory;
        videoView.setVideoURI(Uri.parse(path));
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                videoView.start();
            }
        });
    }


    private void initMath(ViewGroup layout) {
        mathProblem = new MathProblem(Integer.parseInt(mathLevel));
        answerString = String.valueOf(mathProblem.getAnswer());

        if (answerString.endsWith(".0")) {
            answerString = answerString.substring(0, answerString.length() - 2);
        }
        problemView = (TextView) layout.findViewById(R.id.textView11);
        problemView.setText(mathProblem.toString());

        answerView = (TextView) layout.findViewById(R.id.textView21);
        answerView.setText("= ?");
        Log.d("dbg", "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA : " + answerString);

        ((Button) layout.findViewById(R.id.Button01)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button11)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button21)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button31)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button41)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button51)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button61)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button71)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button81)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button91)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button_clear1)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button_decimal1)).setOnClickListener(mathOnclick);
        ((Button) layout.findViewById(R.id.Button_minus1)).setOnClickListener(mathOnclick);
    }


    OnClickListener mathOnclick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            String button = (String) v.getTag();
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (button.equalsIgnoreCase("clear")) {
                if (answerBuilder.length() > 0) {
                    answerBuilder.setLength(answerBuilder.length() - 1);
                    answerView.setText(answerBuilder.toString());
                }
            } else if (button.equalsIgnoreCase(".")) {
                if (!answerBuilder.toString().contains(button)) {
                    if (answerBuilder.length() == 0)
                        answerBuilder.append(0);
                    answerBuilder.append(button);
                    answerView.setText(answerBuilder.toString());
                }
            } else if (button.equalsIgnoreCase("-")) {
                if (answerBuilder.length() == 0) {
                    answerBuilder.append(button);
                    answerView.setText(answerBuilder.toString());
                }
            } else {
                answerBuilder.append(button);
                answerView.setText(answerBuilder.toString());
                if (isAnswerCorrect()) {
                    removeView(getCurrentPage());
                }
            }
            if (answerView.getText().length() >= answerString.length()
                    && !isAnswerCorrect()) {
                answerView.setTextColor(Color.RED);
            } else {
                answerView.setTextColor(Color.BLUE);
            }
        }
    };


    public boolean isAnswerCorrect() {
        boolean correct = false;
        try {
            correct = mathProblem.getAnswer() == Float.parseFloat(answerBuilder
                    .toString());
        } catch (NumberFormatException e) {
            Log.e("dbg","10AlarmAlert : "+e.getMessage(),e);
            return false;
        } catch (Exception e) {
            Log.e("dbg","11AlarmAlert : "+e.getMessage(),e);
            return false;
        }
        return correct;
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


    public class MainPagerAdapter extends PagerAdapter {
        // This holds all the currently displayable views, in order from left to right.
        private ArrayList<View> views = new ArrayList<View>();

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  "Object" represents the page; tell the ViewPager where the
        // page should be displayed, from left-to-right.  If the page no longer exists,
        // return POSITION_NONE.
        @Override
        public int getItemPosition(Object object) {
            int index = views.indexOf(object);
            if (index == -1)
                return POSITION_NONE;
            else
                return index;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager needs a page to display; it is our job
        // to add the page to the container, which is normally the ViewPager itself.  Since
        // all our pages are persistent, we simply retrieve it from our "views" ArrayList.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View v = views.get(position);
            container.addView(v);
            return v;
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.  Called when ViewPager no longer needs a page to display; it
        // is our job to remove the page from the container, which is normally the
        // ViewPager itself.  Since all our pages are persistent, we do nothing to the
        // contents of our "views" ArrayList.
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(views.get(position));
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager; can be used by app as well.
        // Returns the total number of pages that the ViewPage can display.  This must
        // never be 0.
        @Override
        public int getCount() {
            return views.size();
        }

        //-----------------------------------------------------------------------------
        // Used by ViewPager.
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        //-----------------------------------------------------------------------------
        // Add "view" to right end of "views".
        // Returns the position of the new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView(View v) {
            return addView(v, views.size());
        }

        //-----------------------------------------------------------------------------
        // Add "view" at "position" to "views".
        // Returns position of new view.
        // The app should call this to add pages; not used by ViewPager.
        public int addView(View v, int position) {
            views.add(v);
            return position;
        }

        //-----------------------------------------------------------------------------
        // Removes "view" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
//        public int removeView(ViewPager pager, View v) {
//            Log.d("dbg","P1");
//
//            return removeView(pager, views.indexOf(v));
//        }

        //-----------------------------------------------------------------------------
        // Removes the "view" at "position" from "views".
        // Retuns position of removed view.
        // The app should call this to remove pages; not used by ViewPager.
        public int removeView(ViewPager pager, int position) {
            // ViewPager doesn't have a delete method; the closest is to set the adapter
            // again.  When doing so, it deletes all its views.  Then we can delete the view
            // from from the adapter and finally set the adapter to the pager again.  Note
            // that we set the adapter to null before removing the view from "views" - that's
            // because while ViewPager deletes all its views, it will call destroyItem which
            // will in turn cause a null pointer ref.
            Log.d("dbg", "P2");
            pager.setAdapter(null);
            Log.d("dbg", "P3");
            Log.d("dbg", "position   : " + String.valueOf(position));
            views.remove(position);
            Log.d("dbg", "P4");
            pager.setAdapter(this);
            Log.d("dbg", "P5");
            return position;
        }

        //-----------------------------------------------------------------------------
        // Returns the "view" at "position".
        // The app should call this to retrieve a view; not used by ViewPager.
        public View getView(int position) {
            return views.get(position);
        }

        // Other relevant methods:

        // finishUpdate - called by the ViewPager - we don't care about what pages the
        // pager is displaying so we don't use this method.
    }


    //-----------------------------------------------------------------------------
    // Here's what the app should do to add a view to the ViewPager.
    public ViewGroup addView(int rID) {
        LayoutInflater inflater = getLayoutInflater();
        ViewGroup newPage = (ViewGroup) inflater.inflate(rID, null);
        int pageIndex = pagerAdapter.addView(newPage);
        pagerAdapter.notifyDataSetChanged();
        // You might want to make "newPage" the currently displayed page:
        viewPager.setCurrentItem(pageIndex, true);
        return newPage;
    }

    public View addView(View view) {
        int pageIndex = pagerAdapter.addView(view);
        pagerAdapter.notifyDataSetChanged();
        // You might want to make "newPage" the currently displayed page:
        viewPager.setCurrentItem(pageIndex, true);
        return view;
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to remove a view from the ViewPager.
    public void removeView(int pageIndex) {
        pageIndex = pagerAdapter.removeView(viewPager, pageIndex);
        pagerAdapter.notifyDataSetChanged();
        // You might want to choose what page to display, if the current page was "defunctPage".
        Log.d("dbg", "pageIndex  +   " + String.valueOf(pageIndex));
        if (pageIndex == pagerAdapter.getCount())
            pageIndex--;
        viewPager.setCurrentItem(pageIndex);
        Log.d("dbg", "pageIndex  -   " + String.valueOf(pageIndex));
        if (pageIndex == -1) {
//            addView(initVictory());
            initVictory(addView(R.layout.victory));
            destroyAlarmAlert();
            pagerAdapter.notifyDataSetChanged();
            setStateDelay(new Runnable() {
                @Override
                public void run() {
                    destroyView();
                }
            }, 5);
        }
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to get the currently displayed page.
    public int getCurrentPage() {
        return viewPager.getCurrentItem();
    }

    //-----------------------------------------------------------------------------
    // Here's what the app should do to set the currently displayed page.  "pageToShow" must
    // currently be in the adapter, or this will crash.
    public void setCurrentPage(int pageToShow) {
        viewPager.setCurrentItem(pageToShow, true);
    }


}
