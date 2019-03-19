package ir.nimcode.dolphin.api;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.application.MyApplication;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static ir.nimcode.dolphin.api.APIBaseCreator.bodyToString;

/**
 * Created by saeed on 11/15/17.
 */

public class APIBaseCreator {

    private Logger log = LoggerFactory.getLogger(APIBaseCreator.class);

    public static APIBaseAdapter getAPIAdapter(String type) {

        String url;
        if (type.equalsIgnoreCase("google")) {
            url = "https://maps.googleapis.com/";
        } else {
            url = "http://" + MyApplication.sp.getServerAddress() + "/api/";
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS)
                .writeTimeout(0, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .cache(null);


        if (BuildConfig.DEBUG) {
            builder.addInterceptor(new HeaderInterceptor());
            builder.addNetworkInterceptor(new StethoInterceptor());
        }
        OkHttpClient httpClient = builder.build();


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
                .enableComplexMapKeySerialization()
                .setDateFormat(DateFormat.LONG)
                .setPrettyPrinting()
                .setVersion(1.0)
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .client(httpClient)
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();


        return retrofit.create(APIBaseAdapter.class);
    }


    static String bodyToString(final okhttp3.RequestBody request) {
        try {
            final Buffer buffer = new Buffer();
            if (request != null)
                request.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return "did not work";
        }
    }


}

class HeaderInterceptor implements Interceptor {

    private Logger log = LoggerFactory.getLogger(APIBaseCreator.class);

    @Override
    public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
//        builder.addHeader("OS", "Android");
//        builder.addHeader("DeviceId", FirebaseInstanceId.getInstance().getId());
//        builder.addHeader("DeviceName", Build.MODEL);
//        builder.addHeader("Token", TokenManager.getInstance().getToken(context));
//        builder.addHeader("clientId", context.getString(R.string.client_id));
//        builder.addHeader("clientSecret", context.getString(R.string.client_secret));
        builder.addHeader("Content-Type", "application/json");
        Request request = builder.build();
        log.info("API_TAG_REQUEST_URL "+ new Gson().toJson(request.url()));
        log.info("API_TAG_REQUEST_HEADER "+ new Gson().toJson(request.headers()));
        log.info("API_TAG_REQUEST_BODY "+ bodyToString(request.body()));

        Response proceed = chain.proceed(request);
        try {

            BufferedSource source = proceed.body().source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            String responseBodyString = buffer.clone().readString(Charset.forName("UTF-8"));
            log.info("API_TAG_RESPONSE "+ responseBodyString);
        }catch (Exception e){
            Crashlytics.logException(e);
            log.error(e.getMessage(),e);
            e.printStackTrace();
        }


        return proceed;

    }
}

class BooleanTypeAdapter implements JsonDeserializer<Boolean> {
    public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        int code = json.getAsInt();
        return code > 0;
    }
}
