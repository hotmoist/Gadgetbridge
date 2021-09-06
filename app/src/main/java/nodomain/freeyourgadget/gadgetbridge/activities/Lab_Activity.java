//package nodomain.freeyourgadget.gadgetbridge.activities;
//
//import android.app.NotificationManager;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Bundle;
//import android.widget.Toast;
//
//import androidx.core.app.RemoteInput;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.Serializable;
//import java.util.Objects;
//
//import nodomain.freeyourgadget.gadgetbridge.Logging;
//import nodomain.freeyourgadget.gadgetbridge.model.ActivitySample;
//import nodomain.freeyourgadget.gadgetbridge.model.DeviceService;
//import nodomain.freeyourgadget.gadgetbridge.service.btle.AbstractBTLEDeviceSupport;
//import nodomain.freeyourgadget.gadgetbridge.service.devices.huami.HuamiSupport;
//import nodomain.freeyourgadget.gadgetbridge.service.devices.miband.RealtimeSamplesSupport;
//import nodomain.freeyourgadget.gadgetbridge.util.GB;
//
//public class Lab_Activity extends AbstractGBActivity{
//    Thread end_noti = new Thread(new Runnable() {
//        @Override
//        public void run() {
//            destroyNotification(20189);
//            HuamiSupport.test = 2;
//            HuamiSupport.temp = true;
//        }
//    });
//
//    void destroyNotification(int id) {
//        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        notificationManager.cancel(id);
//    }
//    @Override
//    protected void onCreate(Bundle savedInstanceState){
//        super.onCreate(savedInstanceState);
//        destroyNotification(20189);
//    }
//}
