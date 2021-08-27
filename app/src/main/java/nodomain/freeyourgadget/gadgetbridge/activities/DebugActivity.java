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
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NavUtils;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.Widget;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.MiBandPreferencesActivity;
import nodomain.freeyourgadget.gadgetbridge.devices.miband.VibrationProfile;
import nodomain.freeyourgadget.gadgetbridge.devices.pebble.PebbleColor;
import nodomain.freeyourgadget.gadgetbridge.devices.pebble.PebbleIconID;
import nodomain.freeyourgadget.gadgetbridge.entities.Device;
import nodomain.freeyourgadget.gadgetbridge.impl.GBDevice;
import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
import nodomain.freeyourgadget.gadgetbridge.model.CallSpec;
import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
import nodomain.freeyourgadget.gadgetbridge.model.MusicSpec;
import nodomain.freeyourgadget.gadgetbridge.model.MusicStateSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationSpec;
import nodomain.freeyourgadget.gadgetbridge.model.NotificationType;
import nodomain.freeyourgadget.gadgetbridge.model.RecordedDataTypes;
import nodomain.freeyourgadget.gadgetbridge.service.btle.TransactionBuilder;
import nodomain.freeyourgadget.gadgetbridge.service.serial.GBDeviceProtocol;
import nodomain.freeyourgadget.gadgetbridge.util.GB;
import nodomain.freeyourgadget.gadgetbridge.util.WidgetPreferenceStorage;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.amazfitgts2.AmazfitGTS2MiniSupport;

import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.RealtimeSamplesSupport;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;


import static android.content.Intent.EXTRA_SUBJECT;
import static nodomain.freeyourgadget.gadgetbridge.service.btle.GattCharacteristic.UUID_CHARACTERISTIC_ALERT_LEVEL;
import static nodomain.freeyourgadget.gadgetbridge.util.GB.NOTIFICATION_CHANNEL_ID;

public class DebugActivity extends AbstractGBActivity {


    private static final Logger LOG = LoggerFactory.getLogger(DebugActivity.class);

    private int heartRate = 0;  // realtime heart rate
    private int steps = 0;      // realtime steps data
    private int total_steps = 0;
    private int prev_total_steps = -1;
    private int step_count = 0;

    private boolean flag = true;

    private RealtimeSamplesSupport realtimeSamplesSupport;

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


    private Spinner sendTypeSpinner;
    private EditText editContent;

    private int handleRealtimeSample(Serializable extra) {  // void -> int 형으로 변환
        int t = 0;  // 심박수 저장

        if (extra instanceof ActivitySample) {
            ActivitySample sample = (ActivitySample) extra;
            heartRate = sample.getHeartRate();  // 심박수 측정 메소드. int형 반환
            steps = sample.getSteps();

            if (steps > -1) {
                prev_total_steps = total_steps;
                total_steps = steps;
            }
        }
        return t;
    }

    public static void notifi(Context context) {
        new AlertDialog.Builder(context)
                .setMessage("Test")
                .setPositiveButton("dismiss", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        GB.toast("pressed", GB.INFO, Toast.LENGTH_LONG);
//                                        flag = false;
                    }
                }).show();
    }

    public class TimeThread extends Thread {
        @Override
        public void run() {
            super.run();
            do {
                try {
                    Thread.sleep(1000);
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
                    HRvalText.setText("Heart rate: " + HuamiSupport.HEART_RATE + "bpm");
                    StepText.setText("Steps:" + HuamiSupport.TOTAL_STEPS);
                    break;
            }
            return false;
        }
    });

    TextView HRvalText;
    TextView StepText;

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
//                .addAction(R.drawable.ic_launcher_foreground, getString(R.string.action_quit), pendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(id, builder.build());
    }

    void destroyNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }

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

        editContent = findViewById(R.id.editContent);

        ArrayList<String> spinnerArray = new ArrayList<>();
        for (NotificationType notificationType : NotificationType.values()) {
            spinnerArray.add(notificationType.name());
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerArray);
        sendTypeSpinner = findViewById(R.id.sendTypeSpinner);
        sendTypeSpinner.setAdapter(spinnerArrayAdapter);

        HRvalText = (TextView) findViewById(R.id.realtimeHR);
        StepText = (TextView) findViewById(R.id.realtimeSteps);
        new TimeThread().start();

        Button vibrate_test = findViewById(R.id.vibrationButton);
        vibrate_test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MiBandPreferencesActivity activity = new MiBandPreferencesActivity();
                NotificationSpec spec = new NotificationSpec();
//                spec.type = new NotificationType(1, (byte)0x01 );
                spec.type = NotificationType.UNKNOWN;
                activity.tryVibration(spec.type);
            }
        });

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotificationSpec notificationSpec = new NotificationSpec();
                String testString = editContent.getText().toString();
                notificationSpec.phoneNumber = testString;
                notificationSpec.body = testString;
                notificationSpec.sender = testString;
                notificationSpec.subject = testString;
                notificationSpec.type = NotificationType.values()[sendTypeSpinner.getSelectedItemPosition()];
                notificationSpec.pebbleColor = notificationSpec.type.color;
                GBApplication.deviceService().onNotification(notificationSpec);
            }
        });

        Button testSensorButton = findViewById(R.id.sensorButton);
        {
            testSensorButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    testNewFunctionality();
                }
            });
        }

        // mutable debug setting
        final boolean[] flag = {false};
        mHandler = new Handler();
        createNotificationChannel(DEFAULT, "default channel", NotificationManager.IMPORTANCE_HIGH);

        Intent intent = new Intent(this, ControlCenterv2.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        Button testMutableButton = findViewById(R.id.testMutable);
        {
            testMutableButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                                LOG.debug("debug activity test heart: " + HuamiSupport.HEART_RATE);
                                LOG.debug("debug activity test step: " + HuamiSupport.STEP);
                            try {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        createNotification(DEFAULT, 20189, "운동하세요", "어깨 돌리기 10회 이상 실시!", intent);
                                    }
                                });
                                while(true) {
                                    if (HuamiSupport.STEP >= 10) {
                                        destroyNotification(20189);
                                        break;
                                    }
                                    Thread.sleep(100);
                                }
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            });
        }

        // one second debug setting
        Button testOneSecondButton = findViewById(R.id.testOneSecond);
        {
            testOneSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(UUID_CHARACTERISTIC_ALERT_LEVEL, BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE);
                    try {
                        TransactionBuilder builder = new TransactionBuilder("vibrate once");
                        builder.write(characteristic, new byte[]{3});
                        builder.write(characteristic, new byte[]{3});
//                        builder.queue(getQueue());
                    } catch (Exception e){
                        LOG.error("error in static vibrate once", e);
                    }
                }
            });
        }

        // five second debug setting
        Button testFiveSecondButton = findViewById(R.id.testFiveSecond);
        {
            testFiveSecondButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

//        Button incomingCallButton = findViewById(R.id.incomingCallButton);
//        incomingCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CallSpec callSpec = new CallSpec();
//                callSpec.command = CallSpec.CALL_INCOMING;
//                callSpec.number = editContent.getText().toString();
//                GBApplication.deviceService().onSetCallState(callSpec);
//
//            }
//        });
//        realtimeHR.addte
//        }


//        Button outgoingCallButton = findViewById(R.id.outgoingCallButton);
//        outgoingCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CallSpec callSpec = new CallSpec();
//                callSpec.command = CallSpec.CALL_OUTGOING;
//                callSpec.number = editContent.getText().toString();
//                GBApplication.deviceService().onSetCallState(callSpec);
//            }
//        });
//
//        Button startCallButton = findViewById(R.id.startCallButton);
//        startCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CallSpec callSpec = new CallSpec();
//                callSpec.command = CallSpec.CALL_START;
//                GBApplication.deviceService().onSetCallState(callSpec);
//            }
//        });
//        Button endCallButton = findViewById(R.id.endCallButton);
//        endCallButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                CallSpec callSpec = new CallSpec();
//                callSpec.command = CallSpec.CALL_END;
//                GBApplication.deviceService().onSetCallState(callSpec);
//            }
//        });
//
//        Button rebootButton = findViewById(R.id.rebootButton);
//        rebootButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GBApplication.deviceService().onReset(GBDeviceProtocol.RESET_FLAGS_REBOOT);
//            }
//        });
//
//        Button factoryResetButton = findViewById(R.id.factoryResetButton);
//        factoryResetButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new AlertDialog.Builder(DebugActivity.this)
//                        .setCancelable(true)
//                        .setTitle(R.string.debugactivity_really_factoryreset_title)
//                        .setMessage(R.string.debugactivity_really_factoryreset)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                GBApplication.deviceService().onReset(GBDeviceProtocol.RESET_FLAGS_FACTORY_RESET);
//                            }
//                        })
//                        .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        })
//                        .show();
//            }
//        });
//
//        Button heartRateButton = findViewById(R.id.HeartRateButton);
//        heartRateButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GB.toast("Measuring heart rate, please wait...", Toast.LENGTH_LONG, GB.INFO);
//                GBApplication.deviceService().onHeartRateTest();
////                new AmazfitGTS2MiniSupport().onEnableRealtimeHeartRateMeasurement(true);
//            }
//        });
//
//        Button setFetchTimeButton = findViewById(R.id.SetFetchTimeButton);
//        setFetchTimeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final Calendar currentDate = Calendar.getInstance();
//                Context context = getApplicationContext();
//
//                if (context instanceof GBApplication) {
//                    GBApplication gbApp = (GBApplication) context;
//                    final GBDevice device = gbApp.getDeviceManager().getSelectedDevice();
//                    if (device != null) {
//                        new DatePickerDialog(DebugActivity.this, new DatePickerDialog.OnDateSetListener() {
//                            @Override
//                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                                Calendar date = Calendar.getInstance();
//                                date.set(year, monthOfYear, dayOfMonth);
//
//                                long timestamp = date.getTimeInMillis() - 1000;
//                                GB.toast("Setting lastSyncTimeMillis: " + timestamp, Toast.LENGTH_LONG, GB.INFO);
//
//                                SharedPreferences.Editor editor = GBApplication.getDeviceSpecificSharedPrefs(device.getAddress()).edit();
//                                editor.remove("lastSyncTimeMillis"); //FIXME: key reconstruction is BAD
//                                editor.putLong("lastSyncTimeMillis", timestamp);
//                                editor.apply();
//                            }
//                        }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DATE)).show();
//                    } else {
//                        GB.toast("Device not selected/connected", Toast.LENGTH_LONG, GB.INFO);
//                    }
//                }
//
//
//            }
//        });


//        Button setMusicInfoButton = findViewById(R.id.setMusicInfoButton);
//        setMusicInfoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                MusicSpec musicSpec = new MusicSpec();
//                String testString = editContent.getText().toString();
//                musicSpec.artist = testString + "(artist)";
//                musicSpec.album = testString + "(album)";
//                musicSpec.track = testString + "(track)";
//                musicSpec.duration = 10;
//                musicSpec.trackCount = 5;
//                musicSpec.trackNr = 2;
//
//                GBApplication.deviceService().onSetMusicInfo(musicSpec);
//
//                MusicStateSpec stateSpec = new MusicStateSpec();
//                stateSpec.position = 0;
//                stateSpec.state = 0x01; // playing
//                stateSpec.playRate = 100;
//                stateSpec.repeat = 1;
//                stateSpec.shuffle = 1;
//
//                GBApplication.deviceService().onSetMusicState(stateSpec);
//            }
//        });
//
//
        // vibration test
//        Button vibration = findViewById(R.id.vibration);
//        vibration.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //                vibration_timer(3,3);
//            }
//        });


//        vibration.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                test();
//                NotificationSpec spec = new NotificationSpec();
////                spec.type = new NotificationType(1, (byte)0x01 );
//                GBApplication.deviceService().onNotification(spec);
//            }
//        });
//
//
//        Button setTimeButton = findViewById(R.id.setTimeButton);
//        setTimeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GBApplication.deviceService().onSetTime();
//            }
//        });
//
//        Button testNotificationButton = findViewById(R.id.testNotificationButton);
//        testNotificationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                testNotification();
//            }
//        });
//
//        Button testPebbleKitNotificationButton = findViewById(R.id.testPebbleKitNotificationButton);
//        testPebbleKitNotificationButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                testPebbleKitNotification();
//            }
//        });
//
//        Button fetchDebugLogsButton = findViewById(R.id.fetchDebugLogsButton);
//        fetchDebugLogsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                GBApplication.deviceService().onFetchRecordedData(RecordedDataTypes.TYPE_DEBUGLOGS);
//            }
//        });
//
        Button testNewFunctionalityButton = findViewById(R.id.testNewFunctionality);
        testNewFunctionalityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testNewFunctionality();
            }
        });
//
//        Button shareLogButton = findViewById(R.id.shareLog);
//        shareLogButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showWarning();
//            }
//        });
//
//        Button showWidgetsButton = findViewById(R.id.showWidgetsButton);
//        showWidgetsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAllRegisteredAppWidgets();
//            }
//        });

//        Button unregisterWidgetsButton = findViewById(R.id.deleteWidgets);
//        unregisterWidgetsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                unregisterAllRegisteredAppWidgets();
//            }
//        });
//
//        Button showWidgetsPrefsButton = findViewById(R.id.showWidgetsPrefs);
//        showWidgetsPrefsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showAppWidgetsPrefs();
//            }
//        });
//
//        Button deleteWidgetsPrefsButton = findViewById(R.id.deleteWidgetsPrefs);
//        deleteWidgetsPrefsButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                deleteWidgetsPrefs();
//            }
//        });

//        CheckBox activity_list_debug_extra_time_range = findViewById(R.id.activity_list_debug_extra_time_range);
//        activity_list_debug_extra_time_range.setAllCaps(true);
//        boolean activity_list_debug_extra_time_range_value = GBApplication.getPrefs().getPreferences().getBoolean("activity_list_debug_extra_time_range", false);
//        activity_list_debug_extra_time_range.setChecked(activity_list_debug_extra_time_range_value);
//
//        activity_list_debug_extra_time_range.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
//                GBApplication.getPrefs().getPreferences().getBoolean("activity_list_debug_extra_time_range", false);
//                SharedPreferences.Editor editor = GBApplication.getPrefs().getPreferences().edit();
//                editor.putBoolean("activity_list_debug_extra_time_range", b).apply();
//            }
//        });
//
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

    //set vibration timer for mi band
    private void vibrationTimer(){

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