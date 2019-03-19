package ir.nimcode.dolphin.activity;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.User;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;
import ir.nimcode.dolphin.util.applock.core.AppLock;
import ir.nimcode.dolphin.util.applock.core.AppLockActivity;
import ir.nimcode.dolphin.util.applock.core.LockManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends FullAppCompatActivity {

    public static final String TAG = "TAG_ProfileActivity";
    private final int PICK_IMAGE_REQUEST = 1;
    @BindView(R.id.profile_photo)
    CircleImageView profilePhoto;
    @BindView(R.id.change_photo)
    FloatingActionButton change_photo;
    @BindView(R.id.email)
    TextView email;
    @BindView(R.id.full_name)
    TextView fullName;
    @BindView(R.id.supervisor)
    TextView supervisor;
    @BindView(R.id.mobile_number)
    TextView mobileNumber;
    @BindView(R.id.change_password)
    LinearLayout changePassword;
    @BindView(R.id.change_two_passcode)
    LinearLayout changeTwoPasscode;
    @BindView(R.id.edit_profile)
    LinearLayout editProfile;
    @BindView(R.id.switch_two_passcode)
    SwitchCompat switchTwoPasscode;
    private SweetAlertDialog progressDialog;
    private SweetAlertDialog noInternetDialog;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.profile)));

        user = MyApplication.sp.getUser();

        fullName.setText(user.fullname);
        supervisor.setText(user.supervisor.fullname);
        mobileNumber.setText(user.mobile);
        email.setText(user.email);

        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editProfile();
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        switchTwoPasscode.setChecked(LockManager.getInstance().getAppLock().isPasscodeSet());
        changeTwoPasscode.setEnabled(LockManager.getInstance().getAppLock().isPasscodeSet());

        switchTwoPasscode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                changeTwoPasscode.setEnabled(isChecked);
                if (LockManager.getInstance().getAppLock().isPasscodeSet() != isChecked) {
                    int type = !isChecked ? AppLock.DISABLE_PASSLOCK : AppLock.ENABLE_PASSLOCK;
                    Intent intent = new Intent(ProfileActivity.this, AppLockActivity.class);
                    intent.putExtra(AppLock.TYPE, type);
                    startActivityForResult(intent, type);
                }
            }
        });
        changeTwoPasscode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, AppLockActivity.class);
                intent.putExtra(AppLock.TYPE, AppLock.CHANGE_PASSWORD);
                intent.putExtra(AppLock.MESSAGE, getString(R.string.enter_old_passcode));
                startActivityForResult(intent, AppLock.CHANGE_PASSWORD);
            }
        });
        if (MyApplication.directory.list().length == 0) {
            downloadProfilePhoto();
        } else {
            setProfilePhoto();
        }

        progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
        progressDialog.setCancelable(false);

        noInternetDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        noInternetDialog.setTitleText(getString(R.string.error));
        noInternetDialog.setContentText(getString(R.string.no_internet_message));
        noInternetDialog.setConfirmText(getString(R.string.settings));
        noInternetDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
            }
        });
        noInternetDialog.setCancelText(getString(R.string.retry));
        noInternetDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                noInternetDialog.dismiss();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (switchTwoPasscode != null) {
            switchTwoPasscode.setChecked(LockManager.getInstance().getAppLock().isPasscodeSet());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            try {
                profilePhoto.setImageBitmap(Utilities.getResizedBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData()), 500));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void changePassword() {
        final Dialog dialogChangePassword = new Dialog(ProfileActivity.this);
        dialogChangePassword.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogChangePassword.setContentView(R.layout.dialog_change_password);
        final MaterialEditText currentPassword = dialogChangePassword.findViewById(R.id.current_password);
        final MaterialEditText newPassword = dialogChangePassword.findViewById(R.id.new_password);
        final MaterialEditText newPasswordRepeat = dialogChangePassword.findViewById(R.id.confirm_new_password);
        Button save = dialogChangePassword.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                //validate fields
                if (checkPasswordFields(currentPassword, newPassword, newPasswordRepeat)) {

                    //check network is available
                    if (Utilities.isAvailableNetwork(ProfileActivity.this)) {

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitleText(getString(R.string.sending))
                                        .showContentText(false)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                progressDialog.show();
                            }
                        });

                        Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").changePassword("Bearer " + MyApplication.sp.getAuthToken(), currentPassword.getText().toString(), newPassword.getText().toString());
                        call.enqueue(new Callback<ResponseBody>() {

                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                try {
                                    if (response.code() == 200) {
                                        String json = response.body().string();
                                        JSONObject responseData = new JSONObject(json);
                                        int statusCode = responseData.getInt("status_code");

                                        if (statusCode == 0) {
                                            progressDialog.setTitleText("")
                                                    .setContentText(getString(R.string.change_password_success))
                                                    .showContentText(true)
                                                    .setConfirmText(getString(R.string.ok))
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            progressDialog.dismiss();
                                                            dialogChangePassword.dismiss();

                                                        }
                                                    })
                                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                        } else {
                                            progressDialog.setTitleText(getString(R.string.error))
                                                    .setContentText(getString(R.string.change_password_failed))
                                                    .showContentText(true)
                                                    .setConfirmText(getString(R.string.ok))
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            progressDialog.dismiss();

                                                        }
                                                    })
                                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        }
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            String json = response.errorBody().string();
                                            Intent intent = new Intent(ProfileActivity.this, ShowErrorActivity.class);
                                            intent.putExtra("errorBody", json);
                                            startActivity(intent);
                                        } else {
                                            progressDialog.setTitleText(getString(R.string.error))
                                                    .setContentText(getString(R.string.change_password_failed))
                                                    .showContentText(true)
                                                    .setConfirmText(getString(R.string.ok))
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            progressDialog.dismiss();

                                                        }
                                                    })
                                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                                Toasty.error(ProfileActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_LONG, true).show();
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        noInternetDialog.show();
                    }
                }
            }
        });
        dialogChangePassword.show();
    }

    public boolean checkPasswordFields(MaterialEditText currentPassword, MaterialEditText newPassword, MaterialEditText confirmNewPassword) {
        boolean flag = true;

        if (currentPassword.getText().toString().length() == 0) {
            currentPassword.setError(getString(R.string.password_error_empty));
            flag = false;
        } else if (currentPassword.getText().toString().length() < 4) {
            currentPassword.setError(getString(R.string.password_error_invalid));
            flag = false;
        }
        if (newPassword.getText().toString().length() == 0) {
            newPassword.setError(getString(R.string.password_error_empty));
            flag = false;
        } else if (newPassword.getText().toString().length() < 4) {
            newPassword.setError(getString(R.string.password_error_invalid));
            flag = false;
        }
        if (confirmNewPassword.getText().toString().length() == 0) {
            confirmNewPassword.setError(getString(R.string.confirm_password_error_empty));
            flag = false;
        } else if (!newPassword.getText().toString().equals(confirmNewPassword.getText().toString())) {
            confirmNewPassword.setError(getString(R.string.confirm_password_error_invalid));
            flag = false;
        }
        return flag;
    }

    private void editProfile() {

        final Dialog dialogEditProfile = new Dialog(ProfileActivity.this);
        dialogEditProfile.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogEditProfile.setContentView(R.layout.dialog_edit_profile);
        Button btnSave = dialogEditProfile.findViewById(R.id.save);
        final MaterialEditText fullNameDialog = dialogEditProfile.findViewById(R.id.full_name);
        final MaterialEditText mobileNumberDialog = dialogEditProfile.findViewById(R.id.mobile_number);
        final MaterialEditText emailDialog = dialogEditProfile.findViewById(R.id.email);

        fullNameDialog.setText(user.fullname);
        mobileNumberDialog.setText(user.mobile);
        emailDialog.setText(user.email);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //validate fields
                if (checkProfileFields(fullNameDialog, mobileNumberDialog, emailDialog)) {

                    //check network is available
                    if (Utilities.isAvailableNetwork(ProfileActivity.this)) {

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog.setTitleText(getString(R.string.sending))
                                        .showContentText(false)
                                        .setConfirmClickListener(null)
                                        .changeAlertType(SweetAlertDialog.PROGRESS_TYPE);
                                progressDialog.show();
                            }
                        });

                        Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").changeProfileInfo("Bearer " + MyApplication.sp.getAuthToken(), fullNameDialog.getText().toString(), mobileNumberDialog.getText().toString(), emailDialog.getText().toString());
                        call.enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                try {
                                    if (response.code() == 200) {
                                        String json = response.body().string();
                                        JSONObject responseData = new JSONObject(json);
                                        int statusCode = responseData.getInt("status_code");

                                        if (statusCode == 0) {

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    user.fullname = fullNameDialog.getText().toString();
                                                    user.mobile = mobileNumberDialog.getText().toString();
                                                    user.email = emailDialog.getText().toString();
                                                    user = MyApplication.sp.setUser(user);

                                                    fullName.setText(user.fullname);
                                                    mobileNumber.setText(user.mobile);
                                                    email.setText(user.email);

                                                    MainActivity.profileDrawerItem.withName(user.fullname);
                                                    MainActivity.headerResult.updateProfile(MainActivity.profileDrawerItem);

                                                    progressDialog.setTitleText("")
                                                            .setContentText(getString(R.string.edit_profile_success))
                                                            .showContentText(true)
                                                            .setConfirmText(getString(R.string.ok))
                                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                                @Override
                                                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                                    progressDialog.dismiss();
                                                                    dialogEditProfile.dismiss();

                                                                }
                                                            })
                                                            .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                                }
                                            }, 1000);

                                        } else {
                                            progressDialog.setTitleText(getString(R.string.error))
                                                    .setContentText(getString(R.string.edit_profile_failed))
                                                    .showContentText(true)
                                                    .setConfirmText(getString(R.string.ok))
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            progressDialog.dismiss();

                                                        }
                                                    })
                                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                        }
                                    } else {
                                        if (BuildConfig.DEBUG) {
                                            String json = response.errorBody().string();
                                            Intent intent = new Intent(ProfileActivity.this, ShowErrorActivity.class);
                                            intent.putExtra("errorBody", json);
                                            startActivity(intent);
                                        }
                                        progressDialog.setTitleText(getString(R.string.error))
                                                .setContentText(getString(R.string.edit_profile_failed))
                                                .showContentText(true)
                                                .setConfirmText(getString(R.string.ok))
                                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                    @Override
                                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                        progressDialog.dismiss();

                                                    }
                                                })
                                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable throwable) {
                                Toasty.error(ProfileActivity.this, getString(R.string.server_connection_error), Toast.LENGTH_SHORT, true).show();
                                progressDialog.dismiss();
                            }
                        });
                    } else {
                        noInternetDialog.show();
                    }
                }
            }
        });
        dialogEditProfile.show();
    }

    private boolean checkProfileFields(MaterialEditText fullName, MaterialEditText mobileNumber, MaterialEditText email) {
        boolean flag = true;
        if (fullName.getText().toString().length() == 0) {
            fullName.setError(getString(R.string.full_name_error_empty));
            flag = false;
        }
        if (email.getText().toString().length() > 0 && !Utilities.isValidEmail(email.getText().toString())) {
            email.setError(getString(R.string.email_error_invalid));
            flag = false;
        }

        if (mobileNumber.getText().toString().length() > 0 && !mobileNumber.getText().toString().startsWith("09")) {
            email.setError(getString(R.string.mobile_number_error_invalid));
            flag = false;
        }
        return flag;
    }

    public void setProfilePhoto() {
        try {
            this.profilePhoto.setImageBitmap(Utilities.getResizedBitmap(MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(new File(MyApplication.directory, "thumbnail.png"))), 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void downloadProfilePhoto() {

        //TODO: downloadProfilePhoto
//        Target target = new Target() {
//            @Override
//            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
//                runOnUiThread(new Runnable() {
//                    public void run() {
//                        try {
//                            FileOutputStream fos = new FileOutputStream(new File(MyApplication.directory, "thumbnail.png"));
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
//                            fos.close();
//                            setProfilePhoto();
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//            }
//
//            @Override
//            public void onBitmapFailed(Drawable errorDrawable) {
//            }
//
//            @Override
//            public void onPrepareLoad(Drawable placeHolderDrawable) {
//            }
//        };
//        Picasso.with(ProfileActivity.this).load("http://91.222.196.139:7070/storage/" + MyApplication.sp.getString("photo", "")).into(target);
//        this.profilePhoto.setTag(target);
    }
}
