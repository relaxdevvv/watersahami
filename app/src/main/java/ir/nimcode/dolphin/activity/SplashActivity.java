package ir.nimcode.dolphin.activity;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.FileProvider;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import io.fabric.sdk.android.Fabric;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.api.ResponseBase;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.CheckUpdate;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.util.AbsRuntimePermission;
import ir.nimcode.dolphin.util.Utilities;
import retrofit2.Call;
import retrofit2.Response;

public class SplashActivity extends AbsRuntimePermission {


    public static final int REQUEST_CODE_NO_INTERNET = 1003;
    public static final int REQUEST_CODE_INSTALL_PACKAGE = 1004;
    public static final int UPDATE_NOTIFICATION_ID = 1005;
    public static final int SPLASH_TIME_OUT = 3000;
    public static final String TAG = "TAG_SplashActivity";
    @BindView(R.id.logo_image)
    ImageView logoImage;
    @BindView(R.id.welcome_text)
    TextView welcomeText;
    @BindView(R.id.loading_layout)
    LinearLayout loadingLayout;
    @BindView(R.id.version_name)
    TextView versionName;
    @BindView(R.id.server_name)
    TextView serverName;
    private String newVersionName;
    private String newFeatures;
    private String downloadLink;
    private boolean isPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
        if (MyApplication.sp.getAuthToken() != null && !MyApplication.sp.getAuthToken().isEmpty()) {
            Crashlytics.setUserIdentifier(MyApplication.sp.getAuthToken());
            Crashlytics.setUserName(MyApplication.sp.getUser().username);
            Crashlytics.setUserName(MyApplication.sp.getUser().email);
        }

        setContentView(R.layout.activity_splash);

        ButterKnife.bind(this);

        versionName.setText(String.format("%s %s", getString(R.string.edition_label), Utilities.getVersionName(SplashActivity.this)));
        if (Build.VERSION.SDK_INT >= 23) {
            requestPermissions();
        } else {
            connect();
        }
    }

    public void requestPermissions() {
        requestAppPermissions(
                new String[]{
                        Manifest.permission.ACCESS_NETWORK_STATE,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, AbsRuntimePermission.REQUEST_PERMISSION);
    }

    @Override
    public void onPermissionsGranted(int requestCode) {
        connect();
    }

    private void connect() {

        //start animations
        final Animation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        logoImage.setVisibility(View.VISIBLE);
        logoImage.startAnimation(fadeIn);
        logoImage.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                welcomeText.setVisibility(View.VISIBLE);
                versionName.setVisibility(View.VISIBLE);
                serverName.setVisibility(View.VISIBLE);
                loadingLayout.setVisibility(View.VISIBLE);
                welcomeText.startAnimation(fadeIn);
                versionName.startAnimation(fadeIn);
                serverName.startAnimation(fadeIn);
                loadingLayout.startAnimation(fadeIn);

                if (!isGooglePlayServicesAvailable()) {
                    new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText(getString(R.string.google_play_service_error_tittle))
                            .setContentText(getString(R.string.google_play_service_error_message))
                            .setConfirmText(getString(R.string.skip))
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    checkUpdate();
                                }
                            })
                            .setCancelText(getString(R.string.exit))
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    Utilities.finishApplication(SplashActivity.this);
                                }
                            }).show();
                } else {
                    updatePlayId();

                    subscribeToFirebaseTopic();

                    checkUpdate();

                }

            }
        }, 1200);

    }

    public void checkUpdate() {


        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Utilities.isAvailableNetwork(SplashActivity.this)) {
                    try {
                        Call<ResponseBase<CheckUpdate>> call = APIBaseCreator.getAPIAdapter("server").checkUpdate(Utilities.getVersionCode(SplashActivity.this));
                        Response<ResponseBase<CheckUpdate>> response = call.execute();
                        if (response.code() == 200) {

                            ResponseBase<CheckUpdate> res = response.body();

                            if (res!=null && res.getStatusCode() == 0) {

                                CheckUpdate data=res.getData();
                                newVersionName = data.getNewVersionName();
                                downloadLink = data.getDownloadLink();
                                newFeatures = data.getNewFeatures();

                                //create update notification
                                createNotification();

                                // show update dialog
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        new SweetAlertDialog(SplashActivity.this, SweetAlertDialog.WARNING_TYPE)
                                                .setTitleText(getString(R.string.update))
                                                .setContentText(getString(R.string.new_features) + "\n" + Html.fromHtml(newFeatures))
                                                .setConfirmText(getString(R.string.install))
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sDialog) {
                                                        updateProgram(newVersionName, downloadLink);
                                                    }
                                                })
                                                .setCancelText(getString(R.string.exit))
                                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        Utilities.finishApplication(SplashActivity.this);
                                                    }
                                                }).show();
                                    }
                                });

                            } else {
                                Intent intent;
                                if (MyApplication.sp.isLogin()) {
                                    Log.d(TAG, "startOnlineMode Main Activity");
                                    intent = new Intent(SplashActivity.this, MainActivity.class);
                                    Intent in = getIntent();
                                    if (in != null && in.getLongExtra("global_id", -1L) != -1L) {
                                        Document document = MyApplication.database.documentDAO().getByGlobalId(in.getLongExtra("global_id", -1L));
                                        Log.d(TAG, "startOnline: " + new Gson().toJson(document));
                                        if (document != null) {
                                            intent.putExtra("document_id", document.getId());
                                            intent.putExtra("form_id", document.getForm_id());
                                        }
                                    }
                                } else {
                                    intent = new Intent(SplashActivity.this, LoginActivity.class);
                                }
                                startActivity(intent);
                                finish();

                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(SplashActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
                                }
                            });


                            if (BuildConfig.DEBUG) {
                                Log.e(TAG, "checkUpdateError");
                                String json = response.errorBody().string();
                                Intent intent = new Intent(SplashActivity.this, ShowErrorActivity.class);
                                intent.putExtra("errorBody", json);
                                startActivity(intent);
                            }
                        }
                    } catch (Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(SplashActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
                            }
                        });

                        e.printStackTrace();
                    }

                } else if (MyApplication.sp.isLogin()) {
                    Log.d(TAG, "startOfflineMode Main Activity");
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    Intent in = getIntent();
                    if (in != null && in.getLongExtra("global_id", -1L) != -1L) {
                        Document document = MyApplication.database.documentDAO().getByGlobalId(in.getLongExtra("global_id", -1L));
                        if (document != null) {
                            intent.putExtra("document_id", document.getId());
                            intent.putExtra("form_id", document.getForm_id());
                        }
                    }
                    startActivity(intent);
                    finish();
                } else {
                    startActivityForResult(new Intent(SplashActivity.this, NoInternetActivity.class), REQUEST_CODE_NO_INTERNET);
                }

            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult: ");
        switch (requestCode) {
            case REQUEST_CODE_NO_INTERNET:
                if (resultCode == RESULT_OK) {
                    checkUpdate();
                } else {
                    Utilities.finishApplication(SplashActivity.this);
                }
                break;
        }
    }


    public void updateProgram(String updateVersion, String downloadLink) {

        File newVersionFile = createNewVersionFile(updateVersion);
        final Uri uri = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) ?
                FileProvider.getUriForFile(SplashActivity.this, BuildConfig.APPLICATION_ID + ".provider", newVersionFile) :
                Uri.fromFile(newVersionFile);

        if (!newVersionFile.exists()) {

            // show downloading dialog
            final SweetAlertDialog pDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
            pDialog.setTitleText(getString(R.string.update));
            pDialog.setCancelable(false);
            pDialog.show();

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadLink));
            request.setDescription(getString(R.string.downloading_new_version));
            request.setTitle(getString(R.string.app_name));
            request.setDestinationUri(Uri.fromFile(newVersionFile));
            final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            final long downloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    pDialog.dismiss();
                    Intent install = new Intent(Intent.ACTION_VIEW);
                    install.setDataAndType(uri, manager.getMimeTypeForDownloadedFile(downloadId));
                    install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(install, REQUEST_CODE_INSTALL_PACKAGE);

                    unregisterReceiver(this);
                }
            };

            //register receiver for when .apk download is compete
            registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        } else {
            Intent install = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            install.setDataAndType(uri, "application/vnd.android.package-archive");
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(install, REQUEST_CODE_INSTALL_PACKAGE);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel("UPDATE", UPDATE_NOTIFICATION_ID);
    }

    @NonNull
    private File createNewVersionFile(String updateVersion) {
        return new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/", getString(R.string.app_name) + "-" + updateVersion + ".apk");
    }

    public void createNotification() {

        Intent notificationIntent = new Intent(getApplicationContext(), SplashActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addNextIntent(notificationIntent);
        PendingIntent notificationPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(getString(R.string.update_message));
        bigText.setBigContentTitle(getString(R.string.update));

        Notification notification = new NotificationCompat.Builder(SplashActivity.this)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setColor(getResources().getColor(R.color.colorAccent))
                .setContentTitle(getString(R.string.update))
                .setContentText(getString(R.string.update_message))
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(notificationPendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setWhen(System.currentTimeMillis())
                .setTicker(getString(R.string.update_message))
                .setStyle(bigText)
                .setAutoCancel(true)
                .setVibrate(new long[]{100, 200, 300, 500})
                .build();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify("UPDATE", UPDATE_NOTIFICATION_ID, notification);
        }
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        Integer resultCode = googleApiAvailability.isGooglePlayServicesAvailable(SplashActivity.this);
        return resultCode == ConnectionResult.SUCCESS;
    }

    private void updatePlayId() {
        MyApplication.sp.setPlayId(FirebaseInstanceId.getInstance().getToken());
        Log.d(TAG, "userPlayId: " + MyApplication.sp.getPlayId());
    }

    private void subscribeToFirebaseTopic() {
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        Log.d(TAG, "Subscribed to all topic");
    }
}
