package ir.nimcode.dolphin.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rengwuxian.materialedittext.MaterialEditText;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.api.ResponseBase;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.DeviceInfo;
import ir.nimcode.dolphin.model.User;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;
import ir.nimcode.dolphin.util.applock.core.LockManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends FullAppCompatActivity {

    public static final String TAG = "TAG_LoginActivity";
    @BindView(R.id.user_name)
    MaterialEditText userName;
    @BindView(R.id.password)
    MaterialEditText password;
    @BindView(R.id.login)
    Button login;
    @BindView(R.id.exit)
    Button exit;
    @BindView(R.id.forget_password)
    TextView forgetPassword;
    @BindView(R.id.call_support)
    TextView callSupport;
    @BindView(R.id.change_server_address)
    FloatingActionButton changeServerAddress;
    private SweetAlertDialog progressDialog;
    private long lastBackButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ButterKnife.bind(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                login();
                return true;
            }
        });

        forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                forgetPassword();
            }
        });

        callSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(Intent.ACTION_DIAL);
                in.setData(Uri.parse(getString(R.string.support_tel)));
                startActivity(in);
            }
        });

        changeServerAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeServerAddress();
            }
        });

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utilities.finishApplication(LoginActivity.this);
            }
        });

        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.setCancelable(false);
        progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));

    }

    private void updatePlayId() {
        MyApplication.sp.setPlayId(FirebaseInstanceId.getInstance().getToken());
        Log.d(TAG, "userPlayId: " + MyApplication.sp.getPlayId());
    }

    private void login() {

        // validate mobile number_int and password text fields
        if (!validateUserName(userName) || !validatePassword(password)) {
            return;
        }

        // check internet is available
        if (!checkInternet()) {
            return;
        }

        updatePlayId();

        //get mobile
        setMobileDetails();

        progressDialog.setTitleText(getString(R.string.sending))
                .showContentText(false)
                .showCancelButton(false)
                .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.show();

        Call<ResponseBase<User>> call = APIBaseCreator.getAPIAdapter("server").login(
                MyApplication.sp.getPlayId(),
                userName.getText().toString(),
                password.getText().toString()
        );
        call.enqueue(new Callback<ResponseBase<User>>() {

            @Override
            public void onResponse(@NonNull Call<ResponseBase<User>> call, @NonNull Response<ResponseBase<User>> response) {
                try {
                    if (response.code() == 200) {
                        ResponseBase<User> resp = response.body();
                        if (resp.getStatusCode() == 0) {
                            MyApplication.sp.setUser(resp.getData());
                            MyApplication.sp.setIsLogin(true);
                            showSuccessLoginDialog();
                        } else {
                            showFailedLoginDialog();
                        }
                    } else {
                        if (BuildConfig.DEBUG) {
                            Intent intent = new Intent(LoginActivity.this, ShowErrorActivity.class);
                            intent.putExtra("errorBody", response.errorBody().string());
                            startActivity(intent);
                        } else {
                            showFailedLoginDialog();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBase<User>> call, @NonNull Throwable throwable) {
                Toasty.error(LoginActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
                progressDialog.dismiss();
            }
        });

    }

    private void showSuccessLoginDialog() {
        progressDialog.setTitleText(getString(R.string.greeting))
                .setContentText(getString(R.string.login_success))
                .showContentText(true)
                .setConfirmText(getString(R.string.enter))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        LockManager.getInstance().getAppLock().setPasscode(getString(R.string.default_passcode));
                        finish();
                    }
                })
                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);


    }

    private void showFailedLoginDialog() {
        progressDialog.setTitleText(getString(R.string.error))
                .setContentText(getString(R.string.login_failed))
                .showContentText(true)
                .setConfirmText(getString(R.string.forget_password))
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        forgetPassword();
                        progressDialog.dismiss();
                    }
                })
                .setCancelText(getString(R.string.retry))
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                    }
                })
                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
    }

    private void forgetPassword() {

        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_forget_password);

        final MaterialEditText mobileNumber = dialog.findViewById(R.id.mobile_number);

        Button sendPassword = dialog.findViewById(R.id.send_password);

        sendPassword.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                //validate mobile number_int text field
                if (validateMobileNumber(mobileNumber)) {

                    //TODO: change forget password api

                    // check internet is available
                    if (!checkInternet()) {
                        return;
                    }

//                        Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").forgetPassword(MyApplication.FORGET_PASSWORD_TOKEN, MyApplication.USER_TYPE, mobileNumber.getText().toString().trim());
//                        call.enqueue(new Callback<ResponseBody>() {
//
//                            @Override
//                            public void onResponse(Call<ResponseBody> call, ResponseList<ResponseBody> response) {
//                                try {
//                                    if (response.code() == 200) {
//                                        String json = response.body().string();
//                                        JSONObject responseDate = new JSONObject(json);
//                                        int statusCode = responseDate.getInt("status_code");
//                                        if (statusCode == 0) {
//                                            Log.d(TAG, "forgetPassword: " + json);
//                        Toasty.success(LoginActivity.this, getString(R.string.new_password_sent_success), Toast.LENGTH_SHORT, true).show();
//                        forgetPassword.setEnabled(false);
//                        forgetPassword.setTextColor(getResources().getColor(R.color.md_grey_100));
//                        callSupport.setVisibility(View.VISIBLE);
//                        dialog.dismiss();
//                        new CountDownTimer(120000, 1000) {
//                            public void onTick(long millisUntilFinished) {
//                                long t = (millisUntilFinished / 1000);
//                                String time = (t / 60) + ":" + (t % 60 < 10 ? "0" + (t % 60) : t % 60);
//                                forgetPassword.setText(String.format("%s %s", getString(R.string.waiting_time_for_sms), time));
//                            }
//
//                            public void onFinish() {
//                                forgetPassword.setEnabled(true);
//                                forgetPassword.setTextColor(getResources().getColor(R.color.md_white_1000));
//                                forgetPassword.setText(getString(R.string.forget_password));
//                                callSupport.setVisibility(View.INVISIBLE);
//                            }
//                        }.start();
//                                        } else {
//                                            Log.e(TAG, "forgetPasswordFailed: " + json);
//                                            Toasty.warning(LoginActivity.this, getString(R.string.new_password_sent_failed), Toast.LENGTH_SHORT, true).show();
//                                        }
//                                    } else {
//                                        if (BuildConfig.DEBUG) {
//                                            Log.e(TAG, "forgetPasswordError");
//                                            String json = response.errorBody().string();
//                                            Intent intent = new Intent(LoginActivity.this, ShowErrorActivity.class);
//                                            intent.putExtra("errorBody", json);
//                                            startActivity(intent);
//                                        }
//                                    }
//                                } catch (Exception e) {
//                                    e.printStackTrace();
//                                }
//                            }
//
//                            @Override
//                            public void onFailure(Call<ResponseBody> call, Throwable throwable) {
//                                Toasty.error(LoginActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
//                            }
//                        });
                }
            }
        });
        dialog.show();
    }

    public void changeServerAddress() {
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_change_server_address);

        final MaterialEditText serverAddress = dialog.findViewById(R.id.server_address);
        serverAddress.setText(MyApplication.sp.getServerAddress());
        Button save = dialog.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {
                MyApplication.sp.setServerAddress(serverAddress.getText().toString());
                dialog.dismiss();
                Toasty.success(LoginActivity.this, getString(R.string.save_information_success), Toast.LENGTH_LONG, true).show();
            }
        });
        dialog.show();
    }

    public boolean checkInternet() {
        if (Utilities.isAvailableNetwork(LoginActivity.this)) {
            return true;
        } else {
            progressDialog.setTitleText(getString(R.string.error))
                    .showContentText(true)
                    .setContentText(getString(R.string.no_internet_message))
                    .setConfirmText(getString(R.string.settings))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            Intent intent = new Intent();
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction(Settings.ACTION_DATA_ROAMING_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setCancelText(getString(R.string.retry))
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            progressDialog.dismiss();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            progressDialog.show();
            return false;
        }
    }

    public void setMobileDetails() {

        MyApplication.sp.setDeviceInfo(
                new DeviceInfo(
                        "Android",
                        Utilities.getVersionName(LoginActivity.this),
                        Utilities.getDeviceInfo(),
                        Utilities.getDeviceId(LoginActivity.this)
                )
        );
    }

    private boolean validateUserName(MaterialEditText userName) {
        String mobileText = userName.getText().toString();
        if (mobileText.isEmpty()) {
            userName.setError(getString(R.string.username_empty_error));
            return false;
        }
        return true;
    }

    private boolean validateMobileNumber(MaterialEditText mobile) {
        String mobileText = mobile.getText().toString();
        if (mobileText.isEmpty()) {
            mobile.setError(getString(R.string.mobile_number_error_empty));
            return false;
        }

        if (mobileText.length() != 11 || !mobileText.startsWith("09")) {
            mobile.setError(getString(R.string.mobile_number_error_invalid));
            return false;
        }
        return true;
    }

    private boolean validatePassword(MaterialEditText password) {
        String passwordText = password.getText().toString();
        if (passwordText.isEmpty()) {
            password.setError(getString(R.string.password_error_empty));
            return false;
        }

        if (passwordText.length() < 4) {
            password.setError(getString(R.string.password_error_invalid));
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {

        if (lastBackButtonPressed + 2000 > System.currentTimeMillis()) {
            Utilities.finishApplication(LoginActivity.this);
        } else {
            Toasty.info(LoginActivity.this, getString(R.string.exit_message), Toast.LENGTH_LONG, true).show();
        }
        lastBackButtonPressed = System.currentTimeMillis();

    }

}
