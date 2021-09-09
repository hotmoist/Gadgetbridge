package nodomain.freeyourgadget.gadgetbridge.service.devices.huami;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

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
//                                Toast.makeText(context, "성공",Toast.LENGTH_SHORT);

                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        RegisterRequest registerRequest = new RegisterRequest(time, heartrate, totalstep, realtimestep, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(registerRequest);
    }
}