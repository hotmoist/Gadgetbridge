package nodomain.freeyourgadget.gadgetbridge.activities;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;

import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;

import nodomain.freeyourgadget.gadgetbridge.GBApplication;
import nodomain.freeyourgadget.gadgetbridge.R;
import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;

public class MyService extends Service {

    private static final String TAG = "MyService";


    Handler mHandler;
    private final String DEFAULT = "DEFAULT";

    public MyService(){

    }

    @Override
    public void onCreate(){
        super.onCreate();
        // 서비스는 한번 실행되면 계속 실행된 상태로 있는다.
        // 따라서 서비스 특성상 intent를 받아서 처리하기에 적합하지않다.
        // intent에 대한 처리는 onStartCommand()에서 처리해준다.
        mHandler= new Handler();
    }

    /** 요놈이 중요
     * @return**/
    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        if (intent == null) {
            return Service.START_STICKY; //서비스가 종료되어도 자동으로 다시 실행시켜줘!
        } else {
            // intent가 null이 아니다.
            // 액티비티에서 intent를 통해 전달한 내용을 뽑아낸다.(if exists)
            notiTimer();

        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

    }

    @Override
    public IBinder onBind(Intent intent){
        throw new UnsupportedOperationException("Not yet Implemented"); //자동으로 작성되는 코드
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
    void destroyNotification(int id) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
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
}
