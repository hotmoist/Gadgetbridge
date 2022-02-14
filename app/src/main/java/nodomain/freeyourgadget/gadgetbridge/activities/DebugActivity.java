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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.os.AsyncTask;
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
import nodomain.freeyourgadget.gadgetbridge.service.DeviceCommunicationService;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.InsertDB;
import nodomain.freeyourgadget.gadgetbridge.util.AndroidUtils;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.Prefs;
import nodomain.freeyourgadget.gadgetbridge.util.WidgetPreferenceStorage;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;


import static android.content.Intent.EXTRA_SUBJECT;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_CHANNEL_ID;

public class DebugActivity extends AbstractGBActivity {
    private static final Logger LOG = LoggerFactory.getLogger(DebugActivity.class);
    public static boolean TIMER_UI = false;

    private static final String EXTRA_REPLY = "reply";
    private static final String ACTION_REPLY
            = "nodomain.freeyourgadget.gadgetbridge.DebugActivity.action.reply";

    /**
     * ui 세팅 선
     */
    public static ImageView timerImage = null;
    public static String windowon = "화면켜짐";
    int imageX = 0;
    int imageY = 0;
    Intent background;
    TimerTask Task = new TimerTask() {
        @Override
        public void run() {
            Message msg = new Message();
            msg.what = 1;
            handler.sendMessage(msg);
        }
    };


    private Spinner sendCaseSpinner;
    private Spinner sendVibPeriodSpinner;
    private Spinner sendStartHourSpinner;
    private Spinner sendStartMinuteSpinner;
    private Spinner sendEndHourSpinner;
    private Spinner sendEndMinuteSpinner;

    LinearLayout setLabCaseLayer;
    LinearLayout developLayout;
    LinearLayout timelayout;
    TextView runMessage;
    TextView vibrationTimePeriod;
    TextView timePeriod;
    TextView inTimeStep;
    TextView currentCase;
    TextView activationTimePeriod;
    Button setVibrationTime;
    Button timeShow;
    Button cancel_setVibrationTime;
    Button caseSetButton;
    Button setPeriodButton;
    String selectedState;
    //권한 부여
    private boolean pesterWithPermissions = true;
    private static PhoneStateListener fakeStateListener;
    /**
     * 활동 시간
     */
    private boolean isSetVibrationTime;
    String newStartHour;
    String newStartMiunite;
    String newEndHour;
    String newEndMiunite;
    Handler mHandler;
    /**
     * 진동 상태 세팅
     */
    private final String DEFAULT = "DEFAULT";
    private SharedPreferences appData;


    //추가부분
    private DeviceManager deviceManager;
    private GBDeviceAdapterv2 mGBDeviceAdapter;
    private RecyclerView deviceListView;
    private FloatingActionButton fab;


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = "";
            switch (Objects.requireNonNull(action = intent.getAction())) {
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
    private final BroadcastReceiver receiverUI = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("Background")) {
                Message msg = new Message();
                msg.what = 1;
                handler.sendMessage(msg);
                LOG.info("Background");
            }
        }
    };

    /**
     * TODO : braocastReciever을 이용하여 알림 구현
     * -> 블루투스 연결이 끊긴 경우 ( : 이 부분은 외부에서 발생하는 이벤트로 감지)
     * 외부에서 브로드케스트 감지 -> 알림으로 알림
     */
//    private final BroadcastReceiver bleConnectionReceiver = new BroadcastReceiver() {
//        class Task extends AsyncTask<String, Integer, String> {
//            private final PendingResult pendingResult;
//            private final Intent intent;
//
//            Task(PendingResult _pendingResult, Intent _intent) {
//                this.pendingResult = _pendingResult;
//                this.intent = _intent;
//            }
//
//            @Override
//            protected String doInBackground(String... strings) {
                // notification 백그라운드로 이동
//                String action = intent.getAction();
//                if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
//                if(!action.equals(BluetoothDevice.ACTION_ACL_CONNECTED) || !intent.hasExtra(BluetoothDevice.EXTRA_DEVICE)){
//                if(BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)){
                    // 연결 확인을 위한 test code
//                    LOG.debug("/*****  before assert disconnection detected! notify! *********/");
//                    assert(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED));
//                    LOG.debug("/*****  disconnection detected! notify! *********/");
//                    createNotification(DEFAULT, 13009, "disconnect test", "test", intent);
//                }
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(String s) {
//                super.onPostExecute(s);
//                pendingResult.finish();
//            }
//        }
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            final PendingResult pendingResult = goAsync();
//            try {
//                Task asyncTask = new Task(pendingResult, intent);
//                asyncTask.execute();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    };


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        /***** ble disconnection  receiver register *****/
        // 브로드캐스트를 사용하기 위한 필터
        // Android manifest에 등록이 되어있어서 따로 등록 x (뭔가 어디서 등록된거지 ?)
//        IntentFilter disconnectionFilter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        this.registerReceiver(bleConnectionReceiver, disconnectionFilter);

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
        String[] timeCases = {"1", "2", "3", "10", "20", "30", "40", "50", "60"};
        ArrayAdapter<String> timePeriodSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeCases);
        sendVibPeriodSpinner = findViewById(R.id.sendVibPeriod);
        sendVibPeriodSpinner.setAdapter(timePeriodSpinnerAdopter);

        String[] hourCases = new String[24];
        String[] minuteCases = new String[60];
        for (int i = 0; i < hourCases.length; i++) {
            hourCases[i] = i + "";
        }
        for (int i = 0; i < minuteCases.length; i++) {
            minuteCases[i] = i + "";
        }
        Bitmap bitmap = Bitmap.createBitmap(800, 800, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.BLACK);

        /**
         * 시간 설정 스피너
         */
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

        inTimeStep = (TextView) findViewById(R.id.inTimeStep);
        currentCase = (TextView) findViewById(R.id.currentCase);

        /**
         * 케이스 설정 화면, ui 설정
         */
        setLabCaseLayer = findViewById(R.id.setLabCaseLayer);
        setLabCaseLayer.setVisibility(View.GONE);
        developLayout = findViewById(R.id.develop_layout);
        developLayout.setVisibility(View.GONE);
        timelayout = findViewById(R.id.time_layout);
        timelayout.setVisibility(View.GONE);
        timePeriod = findViewById(R.id.timePeriod);
        timePeriod.setVisibility(View.GONE);

        activationTimePeriod = findViewById(R.id.activationTimePeriod);
        vibrationTimePeriod = findViewById(R.id.vibrationTimePeriod);
        runMessage = findViewById(R.id.run_message);
        timerImage = findViewById(R.id.imageView1);

        /**
         * 핸들러 사용
         */
        mHandler = new Handler();

        /**
         * 시간 세팅 메뉴창 켜기
         */
        timeShow = findViewById(R.id.time_show);
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

        /**
         * 시간 세팅 메뉴창 취
         */
        cancel_setVibrationTime = findViewById(R.id.cancel_setVibrationTime);
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

        /**
         * 시간 세팅 메뉴창 설정 완료
         */
        setVibrationTime = findViewById(R.id.setVibrationTime);
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

                    if (newStartHour.equals("0") && newStartMiunite.equals("0") && newEndHour.equals("0") && newEndMiunite.equals("0")) {
                        activationTimePeriod.setText("활동 시간\n\n항상 활성화");
                    }

                    InsertDB insert = new InsertDB(DebugActivity.this);
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                    String getTime = dateFormat.format(date);
                    insert.insertData(getTime + "", newStartHour + "", newStartMiunite + "", "to", newEndHour + "", newEndMiunite + "", option.getCase() + "");

                } else {
                    newStartHour = "00";
                    newStartMiunite = "00";
                    newEndHour = "00";
                    newEndMiunite = "00";
                    GB.toast("다시 입력하세요.", Toast.LENGTH_SHORT, GB.INFO);
                }
            }
        });

        /**
         * 디바이스 추가 메뉴
         */
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GBApplication.getContext(), DiscoveryActivity.class));
            }
        });


        /**
         * 테스트 실험용
         */
        caseSetButton = findViewById(R.id.setLabCase);
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
                } else if (selectedState.equals("NON MUTABILITY")) {
                    HuamiSupport.CASES = HuamiSupport.NONE_MUTABILITY;
                }
                saveTime(1);

            }
        });
        setPeriodButton = findViewById(R.id.setVibPeriod);
        setPeriodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selectedTime = (String) sendVibPeriodSpinner.getSelectedItem();
                HuamiSupport.RESET_TIME = Integer.parseInt(selectedTime) * 60;
                saveTime(3);
            }
        });

        /**
         * 디바이스 연결
         */
        createNotificationChannel(DEFAULT, "default channel", NotificationManager.IMPORTANCE_HIGH);
        Intent intent = new Intent(this, DebugActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
        if (GB.isBluetoothEnabled() && deviceList.isEmpty() && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startActivity(new Intent(this, DiscoveryActivity.class));
        } else {
            GBApplication.deviceService().requestDeviceInfo();
        }


        /**
         * UI 세팅
         */


        /**
         * 세팅해주는 코드
         */
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        loadTime();
        if (isSetVibrationTime) {
            // 이전에 시간이 저장된 경우가 있으면 세팅
            HuamiSupport.SET_START_TIME = Integer.parseInt(newStartHour + newStartMiunite + "000");
            HuamiSupport.SET_END_TIME = Integer.parseInt(newEndHour + newEndMiunite + "000");
        }


//        BroadcastReceiver br = receiverUI;
//        IntentFilter UiFilter = new IntentFilter();
//        UiFilter.addAction("Background");
//        registerReceiver(br, UiFilter);
//        background = new Intent(getApplicationContext(), MyService.class);
//        startService(background);


        GBApplication.deviceService().start();
//        timer = new Timer();
//        timer.schedule(Task,0,1000);
        HuamiSupport.CASES = option.getCase();
        HuamiSupport.RESET_TIME = option.getTime();
    }


    /**
     * ui 및 여러가지 세팅 Handler
     */
    public Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
//                    HRvalText.setText("HR: " + HuamiSupport.HEART_RATE + "bpm");
//                    StepText.setText("TOTAL STEP : " + HuamiSupport.TOTAL_STEP);
                    if (HuamiSupport.STEP_TIMER == 1 && !TIMER_UI) {
//                    timerImage.clearAnimation();
                        RotateAnimation anim =
                                new RotateAnimation(45, 405, timerImage.getWidth() / 2, timerImage.getHeight() / 2);
                        anim.setDuration(HuamiSupport.RESET_TIME * 1000 - HuamiSupport.STEP_TIMER * 1000);//에니메이션 지속시간
                        anim.setInterpolator(new LinearInterpolator());
                        anim.setDuration(HuamiSupport.RESET_TIME * 1000);
                        timerImage.startAnimation(anim);
                        TIMER_UI = true;
                    }
                    if ((HuamiSupport.STEP_TIMER) % 60 == -1) {
                        timePeriod.setVisibility(View.GONE);
                        timePeriod.animate().alpha(0.0f);
                        deviceListView.animate().alpha(1.0f);
                        deviceListView.setVisibility(View.VISIBLE);
                        if (!HuamiSupport.IS_CONNECT) {
                            runMessage.setText("워치와 연결이 끊겨있습니다");
                        } else if (HuamiSupport.IS_CONNECT && !HuamiSupport.IS_WEAR) {
                            runMessage.setText("워치를 착용해주세요");
                        }
                        runMessage.setVisibility(View.VISIBLE);
                        fab.setVisibility(View.VISIBLE);
                    } else {
                        timePeriod.setText((int) ((HuamiSupport.RESET_TIME / 60 - 1) - (HuamiSupport.STEP_TIMER / 60)) + ":" + (60 - (HuamiSupport.STEP_TIMER) % 60));
                        timePeriod.setVisibility(View.VISIBLE);
                        timePeriod.animate().alpha(1.0f);
                        deviceListView.animate().alpha(0.0f);
                        deviceListView.setVisibility(View.GONE);
                        runMessage.setVisibility(View.GONE);
                        fab.setVisibility(View.GONE);
                    }
                    inTimeStep.setText("\n" + HuamiSupport.IN_TIME_STEP);
                    activationTimePeriod.setText("활동 시간\n\n" + newStartHour + ":" + newStartMiunite + " - " + newEndHour + ":" + newEndMiunite);
                    if (newStartHour.equals("0") && newStartMiunite.equals("0") && newEndHour.equals("0") && newEndMiunite.equals("0")) {
                        activationTimePeriod.setText("활동 시간\n\n항상 활성화");
                    }
                    vibrationTimePeriod.setText("설정 주기 간격: " + (HuamiSupport.RESET_TIME / 60));
                    if (HuamiSupport.CASES == HuamiSupport.NONE) {
                        currentCase.setText("Current case: NONE");
                    } else if (HuamiSupport.CASES == HuamiSupport.MUTABILITY) {
                        currentCase.setText("Current case: Mutability");
                    } else if (HuamiSupport.CASES == HuamiSupport.ONE_SECOND) {
                        currentCase.setText("Current case: One Second");
                    } else if (HuamiSupport.CASES == HuamiSupport.FIVE_SECOND) {
                        currentCase.setText("Current case: Five Second");
                    } else if (HuamiSupport.CASES == HuamiSupport.NONE_MUTABILITY) {
                        currentCase.setText("current case: None Mutability");
                    }
                    break;
            }
            return false;
        }
    });

    private void handleRealtimeSample(Serializable extra) {  // void -> int 형으로 변환
        if (extra instanceof ActivitySample) {
            ActivitySample sample = (ActivitySample) extra;
        }
    }


    /**
     * TODO : Broadcast Reciever을 이용한 notification 구현
     * 외부 event : 블루투스 연결 끊김
     */

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

    /**
     * 시간 저장
     *
     * @param t
     */
    private void saveTime(int t) {
        SharedPreferences.Editor editor = appData.edit();
        if (t == 1) {
            // t == 1일때만 lab case 저장
            editor.putString("LabCase", selectedState);
        } else if (t == 2) {
            // t == 2일때 활동 시간 지정정
            editor.putBoolean("SAVE_VIB_TIME", true);
            editor.putString("newStartHour", sendStartHourSpinner.getSelectedItem().toString().trim());
            editor.putString("newStartMinute", sendStartMinuteSpinner.getSelectedItem().toString().trim());
            editor.putString("newEndHour", sendEndHourSpinner.getSelectedItem().toString().trim());
            editor.putString("newEndMinute", sendEndMinuteSpinner.getSelectedItem().toString().trim());
        } else if (t == 3) {
            // t == 3 일때 주기 불러오기
            editor.putInt("newVibPeriod", Integer.parseInt(sendVibPeriodSpinner.getSelectedItem().toString()));
        }
        editor.apply();
    }

    /**
     * 시간 불러오기
     */
    private void loadTime() {
        isSetVibrationTime = appData.getBoolean("SAVE_VIB_TIME", false);
        newStartHour = appData.getString("newStartHour", "");
        newStartMiunite = appData.getString("newStartMinute", "");
        newEndHour = appData.getString("newEndHour", "");
        newEndMiunite = appData.getString("newEndMinute", "");
        HuamiSupport.RESET_TIME = appData.getInt("newVibPeriod", 10) * 60;

        String loadedCase = appData.getString("LabCase", "NONE");
        if (loadedCase.equals("NONE")) {
            HuamiSupport.CASES = HuamiSupport.NONE;
//            currentCase.setText("current case: NONE");
        } else if (loadedCase.equals("MUTABILITY")) {
            HuamiSupport.CASES = HuamiSupport.MUTABILITY;
//            currentCase.setText("Current case: Mutability");
        } else if (loadedCase.equals("ONE SECOND")) {
            HuamiSupport.CASES = HuamiSupport.ONE_SECOND;
//            currentCase.setText("Current case: One Second");
        } else if (loadedCase.equals("FIVE SECOND")) {
            HuamiSupport.CASES = HuamiSupport.FIVE_SECOND;
//            currentCase.setText("Current case: Five Second");
        }
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

    /**
     * 어플 ui 관련
     */
    @Override
    protected void onStart() {
        super.onStart();
        windowon = "on";
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (HuamiSupport.STEP_TIMER > 1) {
            imageX = timerImage.getWidth();
            imageY = timerImage.getHeight();
            RotateAnimation anim =
                    new RotateAnimation(405 - 360 * (HuamiSupport.RESET_TIME - HuamiSupport.STEP_TIMER) / HuamiSupport.RESET_TIME, 405, timerImage.getWidth() / 2, timerImage.getHeight() / 2);
            anim.setDuration(HuamiSupport.RESET_TIME * 1000 - HuamiSupport.STEP_TIMER * 1000);//에니메이션 지속시간
            anim.setInterpolator(new LinearInterpolator());
            timerImage.startAnimation(anim);
        }
    }

    /**
     * 화면 변화에 따라 ui 변경
     */
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (HuamiSupport.STEP_TIMER > 1) {
            imageX = timerImage.getWidth();
            imageY = timerImage.getHeight();
            RotateAnimation anim =
                    new RotateAnimation(405 - 360 * (HuamiSupport.RESET_TIME - HuamiSupport.STEP_TIMER) / HuamiSupport.RESET_TIME, 405, timerImage.getWidth() / 2, timerImage.getHeight() / 2);
            anim.setDuration(HuamiSupport.RESET_TIME * 1000 - HuamiSupport.STEP_TIMER * 1000);//에니메이션 지속시간
            anim.setInterpolator(new LinearInterpolator());
            timerImage.startAnimation(anim);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        windowon = "close";
//        stopService(background);
    }
}