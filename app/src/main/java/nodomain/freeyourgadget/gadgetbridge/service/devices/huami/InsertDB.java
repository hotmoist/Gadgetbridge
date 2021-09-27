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
    Context context;


    InsertDB(String time, String heartrate, String totalstep, String realtimestep, Context context) {
        this.time = time;
        this.heartrate = heartrate;
        this.totalstep = totalstep;
        this.realtimestep = realtimestep;
        this.context = context;
    }

    InsertDB(Context context) {
        this.context=context;
    }

    public void insertData(String time, String heartrate, String totalstep, String realtimestep) {
        this.time = time;
        this.heartrate = heartrate;
        this.totalstep = totalstep;
        this.realtimestep = realtimestep;
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

            RegisterRequest registerRequest = new RegisterRequest(time, heartrate, totalstep, realtimestep, responseListener);
//            registerRequest.setShouldCache(true);
            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(registerRequest);
            queue.getCache().invalidate("https://ljy897.cafe24.com/UserRegister1.php",true);
            registerRequest.setShouldCache(false);
            registerRequest.wait(1);
            registerRequest.cancel();
            queue.stop();

        }catch (Exception e){

        }
    }


}