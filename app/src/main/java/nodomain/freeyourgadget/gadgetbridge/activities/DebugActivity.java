/*  Copyright (C) 2015-2020 Andreas Shimokawa, Carsten Pfeiffer, Daniele
    Gobbetti, Frank Slezak, ivanovlev, Kasha, Lem Dulfo, Pavel Elagin, Steffen
    Liebergeld, vanous
    This file is part of Gadgetbridge.
    Gadgetbridge is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    Gadgetbridge is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package nodomain.freeyourgadget.gadgetbridge.activities;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import de.cketti.library.changelog.ChangeLog;
import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.Widget;
import nodomain.freeyourgadget.gadgetbridge.adapter.GBDeviceAdapterv2;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.InsertDB;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.WidgetPreferenceStorage;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;


import static android.content.Intent.EXTRA_SUBJECT;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_CHANNEL_ID;

public class DebugActivity extends AbstractGBActivity {
//                (selectedState.equals("MUTABILITY"))
//                (selectedState.equals("ONE SECOND"))
//                (selectedState.equals("FIVE SECOND"))
//                (selectedState.equals("NON MUTABILITY"))
    public static boolean TIMER_UI = false;
    private static final Logger LOG = LoggerFactory.getLogger(DebugActivity.class);
    ImageView timerImage=null;
    Animation anim = null;
    public static String windowon = "화면켜짐";
    int imageX=0;
    int imageY=0;

    private static final String EXTRA_REPLY = "reply";
    private static final String ACTION_REPLY
            = "nodomain.freeyourgadget.gadgetbridge.DebugActivity.action.reply";
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (Objects.requireNonNull(intent.getAction())) {
                case ACTION_REPLY: {
                    Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
                    CharSequence reply = remoteInput.getCharSequence(EXTRA_REPLY);
                    LOG.info("got wearable reply: " + reply);
                    GB.toast(context, "got wearable reply: " + reply, Toast.LENGTH_SHORT, GB.INFO);
                    break;
                }
                case DeviceService.ACTION_REALTIME_SAMPLES:
                    handleRealtimeSample(intent.getSerializableExtra(DeviceService.EXTRA_REALTIME_SAMPLE));
                    break;
                default:
                    LOG.info("ignoring intent action " + intent.getAction());
                    break;
            }
        }
    };


    private Spinner sendCaseSpinner;
    private Spinner sendVibPeriodSpinner;
    private Spinner sendStartHourSpinner;
    private Spinner sendStartMinuteSpinner;
    private Spinner sendEndHourSpinner;
    private Spinner sendEndMinuteSpinner;

    private void handleRealtimeSample(Serializable extra) {  // void -> int 형으로 변환
        if (extra instanceof ActivitySample) {
            ActivitySample sample = (ActivitySample) extra;
        }
    }

    public static void notifi(Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Test")
                .setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GB.toast("pressed", GB.INFO, Toast.LENGTH_LONG);
                    }
                }).show();
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(999);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }
    TextView runMessage;
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
//                    HRvalText.setText("HR: " + HuamiSupport.HEART_RATE + "bpm");
//                    StepText.setText("TOTAL STEP : " + HuamiSupport.TOTAL_STEP);
                    if((HuamiSupport.STEP_TIMER) % 60 == -1){
                        timePeriod.setVisibility(View.GONE);
                        timePeriod.animate().alpha(0.0f);
                        deviceListView.animate().alpha(1.0f);
                        deviceListView.setVisibility(View.VISIBLE);
                        if(!HuamiSupport.IS_CONNECT){
                            runMessage.setText("워치와 연결이 끊겨있습니다");
                        }else if(HuamiSupport.IS_CONNECT && !HuamiSupport.IS_WEAR){
                            runMessage.setText("워치를 착용해주세요");
                        }
                        runMessage.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                    }else{
                        timePeriod.setText((int)((HuamiSupport.RESET_TIME/60-1)-(HuamiSupport.STEP_TIMER / 60)) + ":" + (60 - (HuamiSupport.STEP_TIMER) % 60));
                        timePeriod.setVisibility(View.VISIBLE);
                        timePeriod.animate().alpha(1.0f);
                        deviceListView.animate().alpha(0.0f);
                        deviceListView.setVisibility(View.GONE);
                        runMessage.setVisibility(View.GONE);
                        fab.setVisibility(View.GONE);
                    }
                    inTimeStep.setText("\n" + HuamiSupport.IN_TIME_STEP);
                    activationTimePeriod.setText("활동 시간\n\n" + newStartHour +":" + newStartMiunite + " - " + newEndHour +":" + newEndMiunite);
                    if(newStartHour.equals("0")&&newStartMiunite.equals("0")&&newEndHour.equals("0")&&newEndMiunite.equals("0")){
                        activationTimePeriod.setText("활동 시간\n\n항상 활성화");
                    }
                    vibrationTimePeriod.setText("설정 주기 간격: " + (HuamiSupport.RESET_TIME/60));
                    if (HuamiSupport.CASES == HuamiSupport.NONE) {
                        currentCase.setText("Current case: NONE");
                    } else if (HuamiSupport.CASES == HuamiSupport.MUTABILITY) {
                        currentCase.setText("Current case: Mutability");
                    } else if (HuamiSupport.CASES == HuamiSupport.ONE_SECOND) {
                        currentCase.setText("Current case: One Second");
                    } else if (HuamiSupport.CASES == HuamiSupport.FIVE_SECOND) {
                        currentCase.setText("Current case: Five Second");
                    }else if (HuamiSupport.CASES == HuamiSupport.NONE_MUTABILITY){
                        currentCase.setText("current case: None Mutability");
                    }
                    break;
            }
            return false;
        }
    });

    String selectedState;

    TextView HRvalText;
    TextView StepText;
    TextView timePeriod;
    TextView inTimeStep;
    TextView currentCase;
    TextView activationTimePeriod;
    EditText startHour;
    EditText startminute;
    EditText endHour;
    EditText endminute;
    Button setVibrationTime;
    Button timeShow;
    LinearLayout timelayout;

    TextView vibrationTimePeriod;
    //권한 부여
    private boolean pesterWithPermissions = true;
    private static PhoneStateListener fakeStateListener;

    // 활동 시간 관련
    private boolean isSetVibrationTime;
    String newStartHour;
    String newStartMiunite;
    String newEndHour;
    String newEndMiunite;
    private SharedPreferences appData;

    Handler mHandler;
    private final String DEFAULT = "DEFAULT";

    @RequiresApi(api = Build.VERSION_CODES.O)
    void createNotificationChannel(String channelID, String channelName, int importance) {
        if (Build.VERSION.SDK_INT >= (Build.VERSION_CODES.BASE - 1)) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(new NotificationChannel(channelID, channelName, importance));
        }
    }

    void createNotification(String channelID, int id, String title, String text, Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelID)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setOngoing(true)
//                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.action_quit), pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    void destroyNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

    private void saveTime(int t){
        SharedPreferences.Editor editor = appData.edit();
        if(t == 1) {
            // t == 1일때만 lab case 저장
            editor.putString("LabCase", selectedState);
        }

        else if(t ==2) {
            // t == 2일때 활동 시간 지정정
            editor.putBoolean("SAVE_VIB_TIME", true);
            editor.putString("newStartHour", sendStartHourSpinner.getSelectedItem().toString().trim());
            editor.putString("newStartMinute", sendStartMinuteSpinner.getSelectedItem().toString().trim());
            editor.putString("newEndHour", sendEndHourSpinner.getSelectedItem().toString().trim());
            editor.putString("newEndMinute", sendEndMinuteSpinner.getSelectedItem().toString().trim());
        }

        else if(t==3){
            // t == 3 일때 주기 불러오기
            editor.putInt("newVibPeriod", Integer.parseInt(sendVibPeriodSpinner.getSelectedItem().toString()));
        }
        editor.apply();
    }

    private void loadTime(){
        isSetVibrationTime = appData.getBoolean("SAVE_VIB_TIME", false);
        newStartHour = appData.getString("newStartHour", "");
        newStartMiunite = appData.getString("newStartMinute", "");
        newEndHour = appData.getString("newEndHour", "");
        newEndMiunite = appData.getString("newEndMinute", "");
        HuamiSupport.RESET_TIME=appData.getInt("newVibPeriod",10)*60;

        String loadedCase = appData.getString("LabCase", "NONE");
        if (loadedCase.equals("NONE")){
            HuamiSupport.CASES = HuamiSupport.NONE;
//            currentCase.setText("current case: NONE");
        }else if(loadedCase.equals("MUTABILITY")){
            HuamiSupport.CASES = HuamiSupport.MUTABILITY;
//            currentCase.setText("Current case: Mutability");
        }else if (loadedCase.equals("ONE SECOND")){
            HuamiSupport.CASES = HuamiSupport.ONE_SECOND;
//            currentCase.setText("Current case: One Second");
        }else if (loadedCase.equals("FIVE SECOND")){
            HuamiSupport.CASES = HuamiSupport.FIVE_SECOND;
//            currentCase.setText("Current case: Five Second");
        }
    }

    //추가부분
    private DeviceManager deviceManager;
    private GBDeviceAdapterv2 mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private FloatingActionButton fab;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPLY);
        filter.addAction(DeviceService.ACTION_REALTIME_SAMPLES);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
        registerReceiver(mReceiver, filter); // for ACTION_REPLY


        // Lab cases spinner add
        String[] cases = {"NONE", "MUTABILITY", "ONE SECOND", "FIVE SECOND", "NON MUTABILITY"};
        ArrayAdapter<String> caseSpinnerArrayAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cases);
        sendCaseSpinner = findViewById(R.id.sendCaseSpinner);
        sendCaseSpinner.setAdapter(caseSpinnerArrayAdopter);

        // 진동 체크 주기 시간 spinner
        String[] timeCases = {"1", "2", "3", "10", "20", "30","40","50","60"};
        ArrayAdapter<String> timePeriodSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeCases);
        sendVibPeriodSpinner = findViewById(R.id.sendVibPeriod);
        sendVibPeriodSpinner.setAdapter(timePeriodSpinnerAdopter);

        String[] hourCases = new String[24];
        String[] minuteCases = new String[60];

        for(int i = 0; i < hourCases.length; i++){
            hourCases[i] = i +"";
        }

        for(int i = 0; i <minuteCases.length; i++){
            minuteCases[i] = i + "";
        }
        Bitmap bitmap = Bitmap.createBitmap(800,800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);

        // time - start hour spinner
        ArrayAdapter<String> startHourSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hourCases);
        sendStartHourSpinner = findViewById(R.id.startHourSpinner);
        sendStartHourSpinner.setAdapter(startHourSpinnerAdopter);

        // time - start minute spinner
        ArrayAdapter<String> startMinuteSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, minuteCases);
        sendStartMinuteSpinner = findViewById(R.id.startMinuteSpinner);
        sendStartMinuteSpinner.setAdapter(startMinuteSpinnerAdopter);

        // time - end hour spinner
        ArrayAdapter<String> endHourSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, hourCases);
        sendEndHourSpinner = findViewById(R.id.endHourSpinner);
        sendEndHourSpinner.setAdapter(endHourSpinnerAdopter);

        // time - end minute spinner
        ArrayAdapter<String> endMinuteSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, minuteCases);
        sendEndMinuteSpinner = findViewById(R.id.endMinuteSpinner);
        sendEndMinuteSpinner.setAdapter(endMinuteSpinnerAdopter);


        timePeriod = (TextView) findViewById(R.id.timePeriod);
        inTimeStep = (TextView) findViewById(R.id.inTimeStep);
        currentCase = (TextView) findViewById(R.id.currentCase);

        setVibrationTime = findViewById(R.id.setVibrationTime);

        /**
         * 케이스 설정 화면 보이게/안보이기 설정
         */
        findViewById(R.id.setLabCaseLayer).setVisibility(View.GONE);

        activationTimePeriod = findViewById(R.id.activationTimePeriod);
        timeShow = findViewById(R.id.time_show);

        vibrationTimePeriod = findViewById(R.id.vibrationTimePeriod);
        runMessage = findViewById(R.id.run_message);

        // 시작 시간과 종료시간이 지정된 경우
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        loadTime();

        if (isSetVibrationTime) {
            // 이전에 시간이 저장된 경우가 있으면 세팅
            HuamiSupport.SET_START_TIME = Integer.parseInt(newStartHour + newStartMiunite + "000");
            HuamiSupport.SET_END_TIME = Integer.parseInt(newEndHour + newEndMiunite + "000");
        }
        findViewById(R.id.develop_layout).setVisibility(View.GONE);
        timelayout = findViewById(R.id.time_layout);
        timelayout.setVisibility(View.GONE);
        timePeriod.setVisibility(View.GONE);
        timeShow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    timelayout.animate().alpha(1.0f);
                    timelayout.setVisibility(View.VISIBLE);
                    timelayout.animate().translationY(-50);
                    timeShow.animate().alpha(0.0f);
                    timeShow.setVisibility(View.GONE);
            }
        });
        Button cancel_setVibrationTime = findViewById(R.id.cancel_setVibrationTime);
        cancel_setVibrationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                timelayout.setVisibility(View.GONE);
                timelayout.animate().alpha(0.0f);
                timelayout.setVisibility(View.GONE);
                timelayout.animate().translationY(50);
                timeShow.animate().alpha(1.0f);
                timeShow.setVisibility(View.VISIBLE);
            }
        });

        setVibrationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startTime = null;
                String endTime = null;
                newStartHour = (String) sendStartHourSpinner.getSelectedItem();
                newStartMiunite = (String) sendStartMinuteSpinner.getSelectedItem();
                newEndHour = (String) sendEndHourSpinner.getSelectedItem();
                newEndMiunite = (String) sendEndMinuteSpinner.getSelectedItem();

                if (
                        (!newStartHour.equals("") && !newStartMiunite.equals("")) && (newStartHour.length() < 3 && newStartMiunite.length() < 3 && Integer.parseInt(newStartHour) < 24 && Integer.parseInt(newStartMiunite) < 60)
                                && (!newEndHour.equals("") && !newEndMiunite.equals("")) && (newEndHour.length() < 3 && newEndMiunite.length() < 3 && Integer.parseInt(newEndHour) < 24 && Integer.parseInt(newEndMiunite) < 60)
                                && (Integer.parseInt(newEndHour) > Integer.parseInt(newStartHour)
                                || (newStartHour.equals("0") && newStartMiunite.equals("0") && newEndHour.equals("0") && newEndMiunite.equals("0")))
                ) {
                    if (newStartHour.length() == 1) {
                        newStartHour = '0' + newStartHour;
                    }
                    if (newStartMiunite.length() == 1) {
                        newStartMiunite = '0' + newStartMiunite;
                    }
                    startTime = newStartHour + newStartMiunite + "00";

                    if (newEndHour.length() == 1) {
                        newEndHour = '0' + newEndHour;
                    }
                    if (newEndMiunite.length() == 1) {
                        newEndMiunite = '0' + newEndMiunite;
                    }
                    endTime = newEndHour + newEndMiunite + "00";

                    GB.toast(newStartHour + "시" + newStartMiunite + "분 부터" +
                                    newEndHour + "시" + newEndMiunite + "분 까지로 설정되었습니다."

                            , Toast.LENGTH_SHORT, GB.INFO);
                    HuamiSupport.SET_START_TIME = Integer.parseInt(startTime);
                    HuamiSupport.SET_END_TIME = Integer.parseInt(endTime);
                    saveTime(2);

                    timelayout.animate().alpha(0.0f);
                    timelayout.setVisibility(View.GONE);
                    timelayout.animate().translationY(50);
                    timeShow.animate().alpha(1.0f);
                    timeShow.setVisibility(View.VISIBLE);

                    if(newStartHour.equals("0")&&newStartMiunite.equals("0")&&newEndHour.equals("0")&&newEndMiunite.equals("0")){
                        activationTimePeriod.setText("활동 시간\n\n항상 활성화");
                    }

                    InsertDB insert= new InsertDB(DebugActivity.this);
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                    String getTime = dateFormat.format(date);
                    insert.insertData(getTime + "", newStartHour+"", newStartMiunite+"", "to", newEndHour+"", newEndMiunite+"", option.getCase()+"");

                } else {
                    newStartHour = "00";
                    newStartMiunite = "00";
                    newEndHour = "00";
                    newEndMiunite = "00";
                    GB.toast("다시 입력하세요.", Toast.LENGTH_SHORT, GB.INFO);
                }
            }
        });

        new TimeThread().start();
        Button caseSetButton = findViewById(R.id.setLabCase);
        caseSetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedState = (String) sendCaseSpinner.getSelectedItem();
                if (selectedState.equals("NONE")) {
                    HuamiSupport.CASES = HuamiSupport.NONE;
                } else if (selectedState.equals("MUTABILITY")) {
                    HuamiSupport.CASES = HuamiSupport.MUTABILITY;
                } else if (selectedState.equals("ONE SECOND")) {
                    HuamiSupport.CASES = HuamiSupport.ONE_SECOND;
                } else if (selectedState.equals("FIVE SECOND")) {
                    HuamiSupport.CASES = HuamiSupport.FIVE_SECOND;
                }else if (selectedState.equals("NON MUTABILITY")){
                    HuamiSupport.CASES = HuamiSupport.NONE_MUTABILITY;
                }
                saveTime(1);

            }
        });
        Button setPeriodButton = findViewById(R.id.setVibPeriod);
        setPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedTime = (String) sendVibPeriodSpinner.getSelectedItem();
                HuamiSupport.RESET_TIME = Integer.parseInt(selectedTime) * 60;
                saveTime(3);
            }
        });


        final boolean[] flag = {false};
        mHandler = new Handler();
        createNotificationChannel(DEFAULT, "default channel", NotificationManager.IMPORTANCE_HIGH);

        Intent intent = new Intent(this, DebugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

//        Button sendEmail = findViewById(R.id.sendEmail);
//        sendEmail.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent email = new Intent(Intent.ACTION_SEND);
//                email.setType("plain/text");
//                String[] address = {"ljy9805@gmail.com"};
//                email.putExtra(Intent.EXTRA_EMAIL, address);
//                email.putExtra(Intent.EXTRA_SUBJECT, "Daily Report");
//                email.putExtra(Intent.EXTRA_TEXT, "하루동안 알람을 받은 횟수는 몇회입니까?\n1. 0~2회 \n2.3~4회\n5회이상\n\n실험을 하면서 기능적으로 문제가 되었던 부분이 있으면 작성해주세요.");
//                startActivity(email);
//            }
//        });

//        Button dataTest = findViewById(R.id.sendDataBase);
//        dataTest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                InsertDB insertDB = new InsertDB(DebugActivity.this);
//                insertDB.insertData("1", "1", "1", "1");
//            }
//        });


        deviceManager = ((GBApplication) getApplication()).getDeviceManager();

        deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setHasFixedSize(true);
        deviceListView.setLayoutManager(new LinearLayoutManager(this));

        List<GBDevice> deviceList = deviceManager.getDevices();
        mGBDeviceAdapter = new GBDeviceAdapterv2(this, deviceList);

        deviceListView.setAdapter(this.mGBDeviceAdapter);
        registerForContextMenu(deviceListView);

        IntentFilter filterLocal = new IntentFilter();
        filterLocal.addAction(GBApplication.ACTION_LANGUAGE_CHANGE);
        filterLocal.addAction(GBApplication.ACTION_QUIT);
        filterLocal.addAction(DeviceManager.ACTION_DEVICES_CHANGED);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filterLocal);

        refreshPairedDevices();
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GBApplication.getContext(), DiscoveryActivity.class));
            }
        });

        /*
         * Ask for permission to intercept notifications on first run.
         */
        Prefs prefs = GBApplication.getPrefs();
        pesterWithPermissions = prefs.getBoolean("permission_pestering", true);

        Set<String> set = NotificationManagerCompat.getEnabledListenerPackages(this);
        if (pesterWithPermissions) {
            if (!set.contains(this.getPackageName())) { // If notification listener access hasn't been granted
                Intent enableIntent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                startActivity(enableIntent);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermissions();
        }

        ChangeLog cl = createChangeLog();
        if (cl.isFirstRun()) {
            try {
                cl.getLogDialog().show();
            } catch (Exception ignored) {
                GB.toast(getBaseContext(), "Error showing Changelog", Toast.LENGTH_LONG, GB.ERROR);

            }
        }

        GBApplication.deviceService().start();

        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }




        new Thread(new Runnable() {

            @Override
            public void run() {
                notiTimer();
//                while (true) {
////                    if (HuamiSupport.CONNECTION == 1){
////                        // 연결이 감지 된 경우 연결 notifi 삭제
////                        destroyNotification(13009);
////                    }
//
//                    if (HuamiSupport.HEART_RATE > 0 ){
//                        destroyNotification(13009);
//                    }
//
//                    if (HuamiSupport.IS_NOTIFY) {
////                        LOG.debug("check Activity >> notify on");
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                createNotification(DEFAULT, 1259, "운동하세요", "어깨 돌리기 10회 이상 실시!", intent);
//                                HuamiSupport.IS_NOTIFY = false;
//                            }
//                        });
//                    } else if (HuamiSupport.WEAR_NOTIFY_TIMER % 60 == 0) {
//                        // 60초 마다 워치 착용 여부 감지
//                        LOG.debug("check Activity >> notify: " + HuamiSupport.WEAR_NOTIFY_TIMER);
//                        mHandler.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                createNotification(DEFAULT, 13009, "워치를 착용 및 연결해 주세요", "워치 착용(및 연결)이 감지되지 않았습니다. 워치를 착용해주세요", intent);
//                                HuamiSupport.WEAR_NOTIFY_TIMER = 1;
//
////                                if(!HuamiSupport.connect){
////                                    try {
////                                        Thread.sleep(2000);
////                                        System.exit(0);
////                                    } catch (InterruptedException e) {
////                                        e.printStackTrace();
////                                    }
////                                }
//                            }
//                        });
//                    } else {
////                        LOG.debug("check Activity >> notify pending");
//                        if (HuamiSupport.DESTROY_NOTIFICATION) {
//                            destroyNotification(1259);
//                            HuamiSupport.DESTROY_NOTIFICATION = false;
//                        }
//                    }
//                    try {
//                        Thread.sleep(100);
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        }).start();

        timerImage = findViewById(R.id.imageView1);

//                timerImage.clearAnimation();
//        anim.setDuration(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000);
//        timerImage.startAnimation(anim);
//        RotateAnimation anim =
//                new RotateAnimation(45+360*(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000)/HuamiSupport.STEP_TIMER*1000, 405,0.5f,0.5f);
//        anim.setDuration(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000);//에니메이션 지속시간
//
//        timerImage.startAnimation(anim);

/**
 * 세팅해주는 코드
 */
        HuamiSupport.CASES=option.getCase();
        HuamiSupport.RESET_TIME=option.getTime();


    }

    @Override
    /**
     * Todo
     * 계산식
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if(timelayout.getVisibility()==View.VISIBLE)
//            timelayout.setVisibility(View.GONE);
        if(HuamiSupport.STEP_TIMER>1){
            imageX = timerImage.getWidth();
            imageY = timerImage.getHeight();
            RotateAnimation anim =
                    new RotateAnimation(405-360*(HuamiSupport.RESET_TIME-HuamiSupport.STEP_TIMER)/HuamiSupport.RESET_TIME, 405,timerImage.getWidth()/2,timerImage.getHeight()/2);
            anim.setDuration(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000);//에니메이션 지속시간
            anim.setInterpolator(new LinearInterpolator());
            timerImage.startAnimation(anim);
        }
    }

    void notiTimer() {
        Timer notifyTimer = new Timer();
        TimerTask notifyTast = new TimerTask() {
            Intent intent = new Intent(GBApplication.getContext(), DebugActivity.class);
//            final ImageView timerImage = findViewById(R.id.imageView1);
//            Animation anim = AnimationUtils.loadAnimation(
//                    getApplicationContext(), // 현재 화면의 제어권자
//                    R.anim.rotate_anim);


            @Override
            public void run() {
                if(HuamiSupport.STEP_TIMER==1&&!TIMER_UI){
//                    timerImage.clearAnimation();
                    RotateAnimation anim =
                            new RotateAnimation(45, 405,timerImage.getWidth()/2,timerImage.getHeight()/2);
                    anim.setDuration(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000);//에니메이션 지속시간
                    anim.setInterpolator(new LinearInterpolator());
                    anim.setDuration(HuamiSupport.RESET_TIME*1000);
                    timerImage.startAnimation(anim);
                    TIMER_UI=true;
                }
                if (HuamiSupport.HEART_RATE > 0) {
                    destroyNotification(13009);
                }

                if (HuamiSupport.IS_NOTIFY) {
//                        LOG.debug("check Activity >> notify on");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            createNotification(DEFAULT, 1259, "운동하세요", "어깨 돌리기 10회 이상 실시!", intent);
                            HuamiSupport.IS_NOTIFY = false;
                        }
                    });
                } else if (HuamiSupport.WEAR_NOTIFY_TIMER % 60 == 0) {
                    // 60초 마다 워치 착용 여부 감지
                    LOG.debug("check Activity >> notify: " + HuamiSupport.WEAR_NOTIFY_TIMER);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            createNotification(DEFAULT, 13009, "워치를 착용 및 연결해 주세요", "워치 착용(및 연결)이 감지되지 않았습니다. 워치를 착용해주세요", intent);
                            HuamiSupport.WEAR_NOTIFY_TIMER = 1;

//                                if(!HuamiSupport.connect){
//                                    try {
//                                        Thread.sleep(2000);
//                                        System.exit(0);
//                                    } catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
//                                }
                        }
                    });
                } else {
//                        LOG.debug("check Activity >> notify pending");
                    if (HuamiSupport.DESTROY_NOTIFICATION) {
                        destroyNotification(1259);
                        HuamiSupport.DESTROY_NOTIFICATION = false;
                    }
                }

            }
        };
        notifyTimer.schedule(notifyTast,0,100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(HuamiSupport.STEP_TIMER>1){
            imageX = timerImage.getWidth();
            imageY = timerImage.getHeight();
            RotateAnimation anim =
                    new RotateAnimation(405-360*(HuamiSupport.RESET_TIME-HuamiSupport.STEP_TIMER)/HuamiSupport.RESET_TIME, 405,timerImage.getWidth()/2,timerImage.getHeight()/2);
            anim.setDuration(HuamiSupport.RESET_TIME*1000-HuamiSupport.STEP_TIMER*1000);//에니메이션 지속시간
            anim.setInterpolator(new LinearInterpolator());
            timerImage.startAnimation(anim);
        }
    }

    private void deleteWidgetsPrefs() {
        WidgetPreferenceStorage widgetPreferenceStorage = new WidgetPreferenceStorage();
        widgetPreferenceStorage.deleteWidgetsPrefs(DebugActivity.this);
        widgetPreferenceStorage.showAppWidgetsPrefs(DebugActivity.this);
    }

    private void showAppWidgetsPrefs() {
        WidgetPreferenceStorage widgetPreferenceStorage = new WidgetPreferenceStorage();
        widgetPreferenceStorage.showAppWidgetsPrefs(DebugActivity.this);

    }

    private void showAllRegisteredAppWidgets() {
        //https://stackoverflow.com/questions/17387191/check-if-a-widget-is-exists-on-homescreen-using-appwidgetid

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(DebugActivity.this);
        AppWidgetHost appWidgetHost = new AppWidgetHost(DebugActivity.this, 1); // for removing phantoms
        int[] appWidgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(DebugActivity.this, Widget.class));
        GB.toast("Number of registered app widgets: " + appWidgetIDs.length, Toast.LENGTH_SHORT, GB.INFO);
        for (int appWidgetID : appWidgetIDs) {
            GB.toast("Widget: " + appWidgetID, Toast.LENGTH_SHORT, GB.INFO);
        }
    }

    private void unregisterAllRegisteredAppWidgets() {
        //https://stackoverflow.com/questions/17387191/check-if-a-widget-is-exists-on-homescreen-using-appwidgetid

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(DebugActivity.this);
        AppWidgetHost appWidgetHost = new AppWidgetHost(DebugActivity.this, 1); // for removing phantoms
        int[] appWidgetIDs = appWidgetManager.getAppWidgetIds(new ComponentName(DebugActivity.this, Widget.class));
        GB.toast("Number of registered app widgets: " + appWidgetIDs.length, Toast.LENGTH_SHORT, GB.INFO);
        for (int appWidgetID : appWidgetIDs) {
            appWidgetHost.deleteAppWidgetId(appWidgetID);
            GB.toast("Removing widget: " + appWidgetID, Toast.LENGTH_SHORT, GB.INFO);
        }
    }

    private void showWarning() {
        new AlertDialog.Builder(this)
                .setCancelable(true)
                .setTitle(R.string.warning)
                .setMessage(R.string.share_log_warning)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        shareLog();
                    }
                })
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .show();
    }

    private void testNewFunctionality() {
        GBApplication.deviceService().onTestNewFunction();
    }

    private void shareLog() {
        String fileName = GBApplication.getLogPath();
        if (fileName != null && fileName.length() > 0) {
            File logFile = new File(fileName);
            if (!logFile.exists()) {
                GB.toast("File does not exist", Toast.LENGTH_LONG, GB.INFO);
                return;
            }

            Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
            emailIntent.setType("*/*");
            emailIntent.putExtra(EXTRA_SUBJECT, "Gadgetbridge log file");
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));
            startActivity(Intent.createChooser(emailIntent, "Share File"));
        }
    }

    private void testNotification() {
        Intent notificationIntent = new Intent(getApplicationContext(), DebugActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                notificationIntent, 0);

        RemoteInput remoteInput = new RemoteInput.Builder(EXTRA_REPLY)
                .build();

        Intent replyIntent = new Intent(ACTION_REPLY);

        PendingIntent replyPendingIntent = PendingIntent.getBroadcast(this, 0, replyIntent, 0);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_input_add, "Reply", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender().addAction(action);

        NotificationCompat.Builder ncomp = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(getString(R.string.test_notification))
                .setContentText(getString(R.string.this_is_a_test_notification_from_gadgetbridge))
                .setTicker(getString(R.string.this_is_a_test_notification_from_gadgetbridge))
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .extend(wearableExtender);

        GB.notify((int) System.currentTimeMillis(), ncomp.build(), this);
    }

    private void testPebbleKitNotification() {
        Intent pebbleKitIntent = new Intent("com.getpebble.action.SEND_NOTIFICATION");
        pebbleKitIntent.putExtra("messageType", "PEBBLE_ALERT");
        pebbleKitIntent.putExtra("notificationData", "[{\"title\":\"PebbleKitTest\",\"body\":\"sent from Gadgetbridge\"}]");
        getApplicationContext().sendBroadcast(pebbleKitIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        unregisterReceiver(mReceiver);
    }
    //add for research
    private void refreshPairedDevices() {
        mGBDeviceAdapter.notifyDataSetChanged();
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermissions() {
        List<String> wantedPermissions = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.BLUETOOTH_ADMIN);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CONTACTS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.CALL_PHONE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALL_LOG);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.PROCESS_OUTGOING_CALLS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.PROCESS_OUTGOING_CALLS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.RECEIVE_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.SEND_SMS);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.READ_CALENDAR);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED)
            wantedPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);

        try {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MEDIA_CONTENT_CONTROL) == PackageManager.PERMISSION_DENIED)
                wantedPermissions.add(Manifest.permission.MEDIA_CONTENT_CONTROL);
        } catch (Exception ignored) {
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (pesterWithPermissions) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ANSWER_PHONE_CALLS) == PackageManager.PERMISSION_DENIED) {
                    wantedPermissions.add(Manifest.permission.ANSWER_PHONE_CALLS);
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_DENIED) {
                wantedPermissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
            }
        }

        if (!wantedPermissions.isEmpty()) {
            Prefs prefs = GBApplication.getPrefs();
            // If this is not the first run, we can rely on
            // shouldShowRequestPermissionRationale(String permission)
            // and ignore permissions that shouldn't or can't be requested again
            if (prefs.getBoolean("permissions_asked", false)) {
                // Don't request permissions that we shouldn't show a prompt for
                // e.g. permissions that are "Never" granted by the user or never granted by the system
                Set<String> shouldNotAsk = new HashSet<>();
                for (String wantedPermission : wantedPermissions) {
                    if (!shouldShowRequestPermissionRationale(wantedPermission)) {
                        shouldNotAsk.add(wantedPermission);
                    }
                }
                wantedPermissions.removeAll(shouldNotAsk);
            } else {
                // Permissions have not been asked yet, but now will be
                prefs.getPreferences().edit().putBoolean("permissions_asked", true).apply();
            }

            if (!wantedPermissions.isEmpty()) {
                GB.toast(this, getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
                ActivityCompat.requestPermissions(this, wantedPermissions.toArray(new String[0]), 0);
                GB.toast(this, getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
            }
        }

        /* In order to be able to set ringer mode to silent in GB's PhoneCallReceiver
           the permission to access notifications is needed above Android M
           ACCESS_NOTIFICATION_POLICY is also needed in the manifest */
        if (pesterWithPermissions) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE)).isNotificationPolicyAccessGranted()) {
                    GB.toast(this, getString(R.string.permission_granting_mandatory), Toast.LENGTH_LONG, GB.ERROR);
                    startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                }
            }
        }

        // HACK: On Lineage we have to do this so that the permission dialog pops up
        if (fakeStateListener == null) {
            fakeStateListener = new PhoneStateListener();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_CALL_STATE);
            telephonyManager.listen(fakeStateListener, PhoneStateListener.LISTEN_NONE);
        }
    }
    private ChangeLog createChangeLog() {
        String css = ChangeLog.DEFAULT_CSS;
        css += "body { "
                + "color: " + AndroidUtils.getTextColorHex(getBaseContext()) + "; "
                + "background-color: " + AndroidUtils.getBackgroundColorHex(getBaseContext()) + ";" +
                "}";
        return new ChangeLog(this, css);
    }

    @Override
    protected void onStart() {
        super.onStart();
        windowon="on";
    }

    @Override
    protected void onStop() {
        super.onStop();
        windowon="close";
    }
}