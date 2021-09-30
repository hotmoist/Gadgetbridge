package nodomain.freeyourgadget.gadgetbridge.service.devices.huami;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.net.URI;
import java.util.Calendar;

public class InsertDB {
    private androidx.appcompat.app.AlertDialog dialog;
    String time = "test1";
    String heartrate = "test";
    String totalstep = "test";
    String realtimestep = "test";
    String intimestep = "test";
    Context context;


    InsertDB(String time, String heartrate, String totalstep, String realtimestep, String intimestep , Context context) {
        this.time = time;
        this.heartrate = heartrate;
        this.totalstep = totalstep;
        this.realtimestep = realtimestep;
        this.intimestep = intimestep;
        this.context = context;
    }

    InsertDB(Context context) {
        this.context=context;
    }

    public void insertData(String time, String heartrate, String totalstep, String realtimestep, String intimestep) {
        this.time = time;
        this.heartrate = heartrate;
        this.totalstep = totalstep;
        this.realtimestep = realtimestep;
        this.intimestep = intimestep;

        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {
                                Toast.makeText(context, "성공", Toast.LENGTH_SHORT);

                        return;
                    }else{

                        Toast.makeText(context, "실패", Toast.LENGTH_SHORT);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        try {

            RegisterRequest registerRequest = new RegisterRequest(time, heartrate, totalstep, realtimestep, intimestep, responseListener);
   
//            registerRequest.setShouldCache(true);
            registerRequest.setShouldCache(false);
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(registerRequest);
//            registerRequest.wait(100);
            Thread.sleep(100);
            queue.getCache().invalidate("https://ljy897.cafe24.com/UserRegister3.php",true);
            registerRequest.cancel();
            queue.stop();

        }catch (Exception e){
            e.printStackTrace();
        }
    }


}