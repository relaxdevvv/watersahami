package ir.nimcode.dolphin.service;

import android.util.Log;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.application.MyApplication;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    public static final String TAG = "Tag_InstanceId";

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        try {
            MyApplication.sp.setPlayId(FirebaseInstanceId.getInstance().getToken());
            Log.d(TAG, "onTokenRefresh: " + MyApplication.sp.getPlayId());

            Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").updatePlayId(MyApplication.sp.getAuthToken(), MyApplication.sp.getPlayId());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.code() == 200) {
                            String json = response.body().string();
                            JSONObject responseData = new JSONObject(json);
                            int statusCode = responseData.getInt("status_code");
                            if (statusCode == 0) {
                                Log.d(TAG, "updatePlayId: " + json);
                            } else {
                                Log.d(TAG, "updatePlayIdFailed: " + json);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable throwable) {
                    Toasty.error(MyFirebaseInstanceIDService.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT, true).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
