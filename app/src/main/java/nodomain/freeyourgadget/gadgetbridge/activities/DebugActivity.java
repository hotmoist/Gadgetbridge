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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.Widget;
import nodomain.freeyourgadget.gadgetbridge.adapter.GBDeviceAdapterv2;
import nodomain.freeyourgadget.gadgetbridge.devices.DeviceManager;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.WidgetPreferenceStorage;

import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;


import static android.content.Intent.EXTRA_SUBJECT;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_CHANNEL_ID;

public class DebugActivity extends AbstractGBActivity {


    private static final Logger LOG = LoggerFactory.getLogger(DebugActivity.class);

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
                    Thread.sleep(500);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (true);
        }
    }

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case 1:
//                    HRvalText.setText("HR: " + HuamiSupport.HEART_RATE + "bpm");
//                    StepText.setText("TOTAL STEP : " + HuamiSupport.TOTAL_STEP);
                    timePeriod.setText("경과 시간: " + (int) (HuamiSupport.STEP_TIMER / 60) + ":" + (HuamiSupport.STEP_TIMER) % 60+ " / "+ (HuamiSupport.RESET_TIME/60)+":00");
                    inTimeStep.setText("운동 횟수\n" + HuamiSupport.IN_TIME_STEP);
                    activationTimePeriod.setText("설정 활동 시간: " + newStartHour +":" + newStartMiunite + " ~ " + newEndHour +":" + newEndMiunite);
                    vibrationTimePeriod.setText("설정 주기 간격: " + (HuamiSupport.RESET_TIME/60));
                    if (HuamiSupport.CASES == HuamiSupport.NONE) {
                        currentCase.setText("Current case: NONE");
                    } else if (HuamiSupport.CASES == HuamiSupport.MUTABILITY) {
                        currentCase.setText("Current case: Mutability");
                    } else if (HuamiSupport.CASES == HuamiSupport.ONE_SECOND) {
                        currentCase.setText("Current case: One Second");
                    } else if (HuamiSupport.CASES == HuamiSupport.FIVE_SECOND) {
                        currentCase.setText("Current case: Five Second");
                    } else if (HuamiSupport.CASES == HuamiSupport.NONE_MUTABILITY){
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

    TextView vibrationTimePeriod;

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
            // t == 2일때 활동 시간 지정정            editor.putBoolean("SAVE_VIB_TIME", true);
            editor.putString("newStartHour", startHour.getText().toString().trim());
            editor.putString("newStartMinute", startminute.getText().toString().trim());
            editor.putString("newEndHour", endHour.getText().toString().trim());
            editor.putString("newEndMinute", endminute.getText().toString().trim());
        }
        editor.apply();
    }

    public void loadTime(){
        isSetVibrationTime = appData.getBoolean("SAVE_VIB_TIME", false);
        newStartHour = appData.getString("newStartHour", "");
        newStartMiunite = appData.getString("newStartMinute", "");
        newEndHour = appData.getString("newEndHour", "");
        newEndMiunite = appData.getString("newEndMinute", "");

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
        } else if (loadedCase.equals("NONE MUTABILITY")){
            HuamiSupport.CASES = HuamiSupport.NONE_MUTABILITY;
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
        String[] cases = {"NONE", "MUTABILITY", "ONE SECOND", "FIVE SECOND", "NONE MUTABILITY"};
        ArrayAdapter<String> caseSpinnerArrayAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, cases);
        sendCaseSpinner = findViewById(R.id.sendCaseSpinner);
        sendCaseSpinner.setAdapter(caseSpinnerArrayAdopter);

        // 진동 체크 주기 시간 spinner
        String[] timeCases = {"1", "2", "10", "20", "30","40","50","60"};
        ArrayAdapter<String> timePeriodSpinnerAdopter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeCases);
        sendVibPeriodSpinner = findViewById(R.id.sendVibPeriod);
        sendVibPeriodSpinner.setAdapter(timePeriodSpinnerAdopter);

//        HRvalText = (TextView) findViewById(R.id.realtimeHR);
//        StepText = (TextView) findViewById(R.id.realtimeSteps);
        timePeriod = (TextView) findViewById(R.id.timePeriod);
        inTimeStep = (TextView) findViewById(R.id.inTimeStep);
        currentCase = (TextView) findViewById(R.id.currentCase);

        startHour = findViewById(R.id.startHour);
        startminute = findViewById(R.id.startMinute);
        endHour = findViewById(R.id.endHour);
        endminute = findViewById(R.id.endMinute);
        setVibrationTime = findViewById(R.id.setVibrationTime);
        activationTimePeriod = findViewById(R.id.activationTimePeriod);

        vibrationTimePeriod = findViewById(R.id.vibrationTimePeriod);

        // 커서 부분 제거
        startHour.setCursorVisible(false);
        startminute.setCursorVisible(false);
        endHour.setCursorVisible(false);
        endminute.setCursorVisible(false);

        // 시작 시간과 종료시간이 지정된 경우
        appData = getSharedPreferences("appData", MODE_PRIVATE);
        loadTime();

        if (isSetVibrationTime) {
            // 이전에 시간이 저장된 경우가 있으면 세팅
            startHour.setText(newStartHour);
            startminute.setText(newStartMiunite);
            endHour.setText(newEndHour);
            endminute.setText(newEndMiunite);
        }


        setVibrationTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String startTime = null;
                String endTime = null;

                newStartHour = startHour.getText().toString();
                newStartMiunite = startminute.getText().toString();
                newEndHour = endHour.getText().toString();
                newEndMiunite = endminute.getText().toString();


                if (
                        (!newStartHour.equals("") && !newStartMiunite.equals("")) && (newStartHour.length() < 3 && newStartMiunite.length() < 3 && Integer.parseInt(newStartHour) < 24 && Integer.parseInt(newStartMiunite) < 60)
                                && (!newEndHour.equals("") && !newEndMiunite.equals("")) && (newEndHour.length() < 3 && newEndMiunite.length() < 3 && Integer.parseInt(newEndHour) < 24 && Integer.parseInt(newEndMiunite) < 60)
                                && (Integer.parseInt(newEndHour) > Integer.parseInt(newStartHour))
                ) {
                    if (startHour.getText().length() == 1) {
                        newStartHour = '0' + startHour.getText().toString();
                    }
                    if (startminute.getText().length() == 1) {
                        newStartMiunite = '0' + startminute.getText().toString();
                    }
                    startTime = newStartHour + newStartMiunite + "00";

                    if (endHour.getText().length() == 1) {
                        newEndHour = '0' + endHour.getText().toString();
                    }
                    if (endminute.getText().length() == 1) {
                        newEndMiunite = '0' + endminute.getText().toString();
                    }
                    endTime = newEndHour + newEndMiunite + "00";

                    GB.toast(newStartHour + "시" + newStartMiunite + "분 부터" +
                                    newEndHour + "시" + newEndMiunite + "분 까지로 설정되었습니다."

                            , Toast.LENGTH_SHORT, GB.INFO);
                    HuamiSupport.SET_START_TIME = Integer.parseInt(startTime);
                    HuamiSupport.SET_END_TIME = Integer.parseInt(endTime);
                    saveTime(2);
                } else {
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
                } else if (selectedState.equals("NONE MUTABILITY")){
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
            }
        });


        final boolean[] flag = {false};
        mHandler = new Handler();
        createNotificationChannel(DEFAULT, "default channel", NotificationManager.IMPORTANCE_HIGH);

        Intent intent = new Intent(this, ControlCenterv2.class);
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

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(GBApplication.getContext(), DiscoveryActivity.class));
            }
        });


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

}