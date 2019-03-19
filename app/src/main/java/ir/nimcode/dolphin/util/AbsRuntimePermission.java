package ir.nimcode.dolphin.util;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.Arrays;

import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.R;

public abstract class AbsRuntimePermission extends FullAppCompatActivity {

    private SweetAlertDialog pDialog;
    private SweetAlertDialog pDialog1;

    private String[] requestedPermissions;
    private int requestCode;
    private boolean isFirst = true;
    private boolean isSettingsShow = false;

    public static final int REQUEST_PERMISSION = 1001;

    public static final String TAG = "TAG_PermissionsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        pDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.warning))
                .setContentText("برای اجرای صحیح برنامه لطفا اجازه به دسترسی های خواسته شده را صادر کنید")
                .setConfirmText(getString(R.string.see))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        pDialog.dismiss();
                        ActivityCompat.requestPermissions(AbsRuntimePermission.this, requestedPermissions, requestCode);
                    }
                })
                .setCancelText(getString(R.string.exit))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        pDialog.dismiss();
                        Utilities.finishApplication(AbsRuntimePermission.this);
                    }
                })
        ;
        pDialog.setCancelable(false);
        pDialog1 = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText(getString(R.string.warning))
                .setContentText(getString(R.string.request_permissions2_message))
                .setConfirmText(getString(R.string.settings))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        isSettingsShow = true;
                        pDialog1.dismiss();
                        Intent i = new Intent();
                        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        i.addCategory(Intent.CATEGORY_DEFAULT);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                        startActivity(i);
                    }
                })
                .setCancelText(getString(R.string.exit))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        pDialog1.dismiss();
                        Utilities.finishApplication(AbsRuntimePermission.this);
                    }
                });
        pDialog1.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isSettingsShow) {
            requestAppPermissions(requestedPermissions, requestCode);
        }
    }

    public abstract void onPermissionsGranted(int requestCode);

    public void requestAppPermissions(final String[] requestedPermissions, final int requestCode) {
        this.requestedPermissions = requestedPermissions;
        this.requestCode = requestCode;
        int permissionCheck = 0;
        boolean showRequestPermissions = false;
        for (String permission : requestedPermissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            showRequestPermissions = showRequestPermissions || ActivityCompat.shouldShowRequestPermissionRationale(this, permission);
        }


        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            onPermissionsGranted(requestCode);
            Log.d(TAG, "PERMISSION_GRANTED");
        } else {
            if (showRequestPermissions || isFirst) {
                Log.d(TAG, "showRequestPermissions :pDialog");
                pDialog.show();
                isFirst = false;
            } else {
                Log.d(TAG, "showRequestPermissions :pDialog1");
                pDialog1.show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        int permissionCheck = 0;
        for (String permission : permissions) {
            permissionCheck = permissionCheck + ContextCompat.checkSelfPermission(this, permission);
            Log.d(TAG, "onRequestPermissionsResult: " + permission);
        }

        if ((grantResults.length > 0) && PackageManager.PERMISSION_GRANTED == permissionCheck) {
            onPermissionsGranted(requestCode);
            Log.d(TAG, "onPermissionsGranted");
        } else {
            requestAppPermissions(permissions, requestCode);
            Log.d(TAG, "requestAppPermissions" + Arrays.toString(permissions));
        }
    }
}
