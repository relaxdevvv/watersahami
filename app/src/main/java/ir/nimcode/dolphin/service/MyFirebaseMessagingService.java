package ir.nimcode.dolphin.service;

import android.content.Intent;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONObject;

import java.util.Map;

import ir.nimcode.dolphin.activity.SplashActivity;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.util.Utilities;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String TAG = "Tag_Messaging";
    private static final int NOTICE_NOTIFICATION_ID = 2000;
    private static final int OTHER_NOTIFICATION_ID = 20002;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        try {
            Map<String, String> remoteMessageData = remoteMessage.getData();
            Log.d(TAG, "onMessageReceived: " + remoteMessageData);
            if (remoteMessageData.size() > 0) {
                final String type = remoteMessageData.get("type");
                switch (type) {
                    case "notice":
                        handleNoticeNotification(remoteMessageData);
                        break;
                    case "force_close":
                        handleForceCloseNotification(remoteMessageData);
                        break;

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleNoticeNotification(Map<String, String> remoteMessageData) {
        try {
//            if (Utilities.applicationInForeground(MyFirebaseMessagingService.this)) {
//                final JSONObject content = new JSONObject(remoteMessageData.get("content"));
//                final Intent dialogIntent = new Intent(this, DialogActivity.class);
//                dialogIntent.putExtra("title", content.getString("title"));
//                if (content.has("link")) {
//                    dialogIntent.putExtra("type", "dialogWebView");
//                    dialogIntent.putExtra("link", content.getString("link"));
//                } else {
//                    dialogIntent.putExtra("type", "dialog");
//                    dialogIntent.putExtra("message", content.getString("message"));
//                }
//                startActivity(dialogIntent);
//            } else {
            final JSONObject content = new JSONObject(remoteMessageData.get("content"));
            Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
            notificationIntent.putExtra("global_id", content.getLong("global_id"));
            Utilities.showStatusBarNotification(MyFirebaseMessagingService.this, notificationIntent, remoteMessageData, NOTICE_NOTIFICATION_ID);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void handleForceCloseNotification(Map<String, String> remoteMessageData) {

        String serverAddress = MyApplication.sp.getServerAddress();
        MyApplication.sp.clearAllCache();
        MyApplication.sp.setServerAddress(serverAddress);


        Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        Utilities.showStatusBarNotification(MyFirebaseMessagingService.this, notificationIntent, remoteMessageData, OTHER_NOTIFICATION_ID);

    }

}
