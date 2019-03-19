package ir.nimcode.dolphin.application;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.model.DeviceInfo;
import ir.nimcode.dolphin.model.User;

public class SharedPref {

    public static final String IS_LOGIN = "_.IS_LOGIN";
    public static final String LAST_FORMS_UPDATED_TIME = "_.LAST_FORMS_UPDATED_TIME";
    public static final String LAST_DOCUMENTS_UPDATED_TIME = "_.LAST_DOCUMENTS_UPDATED_TIME";
    public static final String LAST_KMLS_UPDATED_TIME = "_.LAST_KMLS_UPDATED_TIME";
    public static final String USER_PROFILE = "_.USER_PROFILE_KEY";
    public static final String USER_PLAY_ID = "_.USER_PLAY_ID";
    public static final String DEVICE_INFO = "_.DEVICE_INFO";
    public static final String FILTER_FORM_ID = "_.FILTER_FORM_ID";
    // if change this keys , so you should update from pref_general.xml
    public static final String MAP_LOAD_RADIUS = "_.MAP_LOAD_RADIUS";
    public static final String SERVER_ADDRESS = "_.SERVER_ADDRESS";
    private Context context;
    private SharedPreferences sharedPreferences;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
//        sharedPreferences = context.getSharedPreferences("MAIN_PREF", Context.MODE_PRIVATE);
    }

    public void clearAllCache() {
        sharedPreferences.edit().clear().apply();
    }

    public void setIsLogin(boolean flag) {
        sharedPreferences.edit().putBoolean(IS_LOGIN, flag).apply();
    }

    public boolean isLogin() {
        return sharedPreferences.getBoolean(IS_LOGIN, false);
    }

    public String getServerAddress() {
        return sharedPreferences.getString(SERVER_ADDRESS, context.getString(R.string.server_address_default));
    }

    public void setServerAddress(String serverAddress) {
        sharedPreferences.edit().putString(SERVER_ADDRESS, serverAddress).apply();
    }

    public User setUser(User userProfile) {
        if (userProfile == null) return null;
        String json = new Gson().toJson(userProfile, User.class);
        sharedPreferences.edit().putString(USER_PROFILE, json).apply();
        return getUser();
    }

    public User getUser() {
        String data = sharedPreferences.getString(USER_PROFILE, null);
        if (data == null) return null;
        return new Gson().fromJson(data, User.class);
    }

    public String getAuthToken() {
        User user = getUser();
        if (user != null) {
            return user.token;
        }
        return null;
    }

    public String getPlayId() {
        return sharedPreferences.getString(USER_PLAY_ID, null);
    }

    public void setPlayId(String playId) {
        sharedPreferences.edit().putString(USER_PLAY_ID, playId).apply();

    }

    public DeviceInfo setDeviceInfo(DeviceInfo mobileInfo) {
        if (mobileInfo == null) return null;
        String json = new Gson().toJson(mobileInfo, DeviceInfo.class);
        sharedPreferences.edit().putString(DEVICE_INFO, json).apply();
        return getDeviceInfo();
    }

    public DeviceInfo getDeviceInfo() {
        String data = sharedPreferences.getString(DEVICE_INFO, null);
        if (data == null) return null;
        return new Gson().fromJson(data, DeviceInfo.class);
    }

    public long getLastFormsUpdatedTime() {
        return sharedPreferences.getLong(LAST_FORMS_UPDATED_TIME, 0L);
    }

    public void setLastFormsUpdatedTime(long time) {
        sharedPreferences.edit().putLong(LAST_FORMS_UPDATED_TIME, time).apply();
    }

    public long getLastDocumentsUpdatedTime() {
        return sharedPreferences.getLong(LAST_DOCUMENTS_UPDATED_TIME, 0L);
    }

    public void setLastDocumentsUpdatedTime(long time) {
        sharedPreferences.edit().putLong(LAST_DOCUMENTS_UPDATED_TIME, time).apply();
    }

    public long getLastKmlsUpdatedTime() {
        return sharedPreferences.getLong(LAST_KMLS_UPDATED_TIME, 0L);
    }

    public void setLastKmlsUpdatedTime(long time) {
        sharedPreferences.edit().putLong(LAST_KMLS_UPDATED_TIME, time).apply();
    }

    public long getFilterFormId() {
        return Long.parseLong(sharedPreferences.getString(FILTER_FORM_ID, "0"));
    }

    public void setFilterFormId(String formId) {
        sharedPreferences.edit().putString(FILTER_FORM_ID, formId).apply();
    }

    public int getMapLoadRadius() {
        return Integer.parseInt(sharedPreferences.getString(MAP_LOAD_RADIUS, "2000"));
    }

    public void setMapLoadRadius(String mapLoadRadius) {
        sharedPreferences.edit().putString(MAP_LOAD_RADIUS, mapLoadRadius).apply();
    }


}
