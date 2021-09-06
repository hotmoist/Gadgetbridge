package nodomain.freeyourgadget.gadgetbridge.activities;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class InsertDB {
    private androidx.appcompat.app.AlertDialog dialog;
    String userID = "test1";
    String userPassword = "test";
    String userGender = "test";
    String userEmail = "test";
    Context context;


    InsertDB(String userID, String userPassword, String userGender, String userEmail, Context context) {
        this.userID = userID;
        this.userPassword = userPassword;
        this.userGender = userGender;
        this.userEmail = userEmail;
        this.context = context;
    }

    InsertDB(Context context) {
        this.context = context;
    }

    public void insertData(String userID, String userPassword, String userGender, String userEmail) {
        Response.Listener<String> responseListener = new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    if (success) {

                        return;
                    } else {

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        RegisterRequest registerRequest = new RegisterRequest(userID, userPassword, userGender, userEmail, responseListener);
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(registerRequest);
    }
}