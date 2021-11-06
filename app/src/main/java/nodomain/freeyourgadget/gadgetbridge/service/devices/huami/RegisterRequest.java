package nodomain.freeyourgadget.gadgetbridge.service.devices.huami;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

import nodomain.freeyourgadget.gadgetbridge.activities.option;

public class RegisterRequest extends StringRequest {
    final static private String URL= option.getUrl();
    private Map<String,String> parameters;


    public RegisterRequest(String time, String heartrate, String totalstep, String realtimestep, String intimestep, String vibrationtag, String windowon, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("time",time);
        parameters.put("heartrate",heartrate);
        parameters.put("totalstep",totalstep);
        parameters.put("realtimestep",realtimestep);
        parameters.put("intimestep",intimestep);
        parameters.put("vibrationtag",vibrationtag);
        parameters.put("windowon",windowon);
    }

    @Override
    public Map<String, String> getParams(){
        return parameters;
    }
}