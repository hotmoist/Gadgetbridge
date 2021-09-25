package nodomain.freeyourgadget.gadgetbridge.service.devices.huami;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {
    static private String URL= "https://ljy897.cafe24.com/UserRegister1.php";
    private Map<String,String> parameters;
    private Map<String,String> oldParameters;


    public RegisterRequest(String time, String heartrate, String totalstep, String realtimestep, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("time",time);
        parameters.put("heartrate",heartrate);
        parameters.put("totalstep",totalstep);
        parameters.put("realtimestep",realtimestep);
    }
    @Override
    public Map<String, String> getParams(){
        oldParameters=parameters;
        parameters.clear();
        return oldParameters;
    }
}