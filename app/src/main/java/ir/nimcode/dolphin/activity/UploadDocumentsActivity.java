package ir.nimcode.dolphin.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.StringRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.marcouberti.autofitbutton.AutoFitButton;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.database.AppDatabase;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.ImagePropertyValues;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.PropertyValues;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.Utilities;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static ir.nimcode.dolphin.model.Document.status.pending;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.MONTH;

public class UploadDocumentsActivity extends FullAppCompatActivity {

    public static final String TAG = "UploadDocuments_TAG";
    @BindView(R.id.start_date)
    MaterialEditText startDate;
    @BindView(R.id.clear_start_date)
    ImageView clearStartDate;
    @BindView(R.id.finish_date)
    MaterialEditText finishDate;
    @BindView(R.id.clear_finish_date)
    ImageView clearFinishDate;
    @BindView(R.id.upload)
    AutoFitButton upload;
    @BindView(R.id.re_upload)
    AutoFitButton reUpload;
    private Calendar now;
    private SweetAlertDialog progressDialog;
    private ProgressDialog progress;
    private AppDatabase database;
    private long serverRequestCount;
    private long firstServerRequestTime;

    private Logger log = LoggerFactory.getLogger(UploadDocumentsActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_documents);

        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.upload_data)));

        database = MyApplication.database;

        clearFinishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishDate.setText("");
                clearFinishDate.setVisibility(View.GONE);
            }
        });

        clearStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDate.setText("");
                clearStartDate.setVisibility(View.GONE);
            }
        });

        now = Calendar.getInstance();
        JalaliCalendar nowJalali = new JalaliCalendar(now);

        final PersianCalendar calendarPersianCalendar = new PersianCalendar(now.getTime().getTime());

        JalaliCalendar jalaliCalendar = new JalaliCalendar(now);
        jalaliCalendar.add(MONTH, -1);
        startDate.setText(jalaliCalendar.dateToString());
        clearStartDate.setVisibility(View.VISIBLE);
        startDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersianCalendar persianCalendar;
                if (!startDate.getText().toString().isEmpty()) {
                    persianCalendar = new PersianCalendar(new JalaliCalendar(startDate.getText().toString()).getGregorianCalendar().getTime().getTime());
                    persianCalendar.add(HOUR, 24);
                } else {
                    persianCalendar = calendarPersianCalendar;
                }
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
                                clearStartDate.setVisibility(View.VISIBLE);
                                startDate.setText(String.format(new Locale("en"), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                            }
                        },
                        persianCalendar.getPersianYear(),
                        persianCalendar.getPersianMonth(),
                        persianCalendar.getPersianDay()
                );
                datePickerDialog.show(getFragmentManager(), "DatePicker");
            }
        });
        startDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    startDate.callOnClick();
                }
            }
        });

        finishDate.setText(nowJalali.dateToString());
        clearFinishDate.setVisibility(View.VISIBLE);
        finishDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PersianCalendar persianCalendar;
                if (!finishDate.getText().toString().isEmpty()) {
                    persianCalendar = new PersianCalendar(new JalaliCalendar(finishDate.getText().toString()).getGregorianCalendar().getTime().getTime());
                    persianCalendar.add(HOUR, 24);
                } else {
                    persianCalendar = calendarPersianCalendar;
                }
                DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
                                clearFinishDate.setVisibility(View.VISIBLE);
                                finishDate.setText(String.format(new Locale("en"), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                            }
                        },
                        persianCalendar.getPersianYear(),
                        persianCalendar.getPersianMonth(),
                        persianCalendar.getPersianDay()
                );
                datePickerDialog.show(getFragmentManager(), "DatePicker");
            }
        });
        finishDate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    finishDate.callOnClick();
                }
            }
        });


        progress = new ProgressDialog(this);
        progress.setMessage(getString(R.string.sending));
        progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progress.setIndeterminate(true);

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                progressDialog = new SweetAlertDialog(UploadDocumentsActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
                progressDialog.setCancelable(false);
                progressDialog.setTitleText(getString(R.string.calculating));
                progressDialog.show();

                upload(false, startDate.getText().toString(), finishDate.getText().toString());
            }
        });
        reUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                progressDialog = new SweetAlertDialog(UploadDocumentsActivity.this, SweetAlertDialog.PROGRESS_TYPE);
//                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
                progressDialog.setCancelable(false);
                progressDialog.setTitleText(getString(R.string.calculating));
                progressDialog.show();

                upload(true, startDate.getText().toString(), finishDate.getText().toString());
            }
        });
    }


    private void upload(final boolean reUpload, String startDate, String finishDate) {

        if (!checkInternet()) {
            return;
        }

        final List<DocumentSeries> documentSeries;

        Date startDa = !startDate.isEmpty() ? new JalaliCalendar(startDate).getGregorianCalendar().getTime() : now.getTime();
        Date finishDa = !finishDate.isEmpty() ? new JalaliCalendar(finishDate).getGregorianCalendar().getTime() : now.getTime();
        finishDa.setHours(23);
        finishDa.setMinutes(59);
        finishDa.setSeconds(59);

        if (!reUpload) {
            if (!startDate.isEmpty() || !finishDate.isEmpty()) {

                if (finishDate.isEmpty()) {
                    documentSeries = database.documentSeriesDAO().getAllGreaterNotUpload(startDa.getTime());
                } else if (startDate.isEmpty()) {
                    documentSeries = database.documentSeriesDAO().getAllLesserNotUpload(finishDa.getTime());
                } else {
                    documentSeries = database.documentSeriesDAO().getAllNotUpload(startDa.getTime(), finishDa.getTime());
                }
            } else {
                documentSeries = database.documentSeriesDAO().getAllNotUpload();
            }
        } else {
            if (!startDate.isEmpty() || !finishDate.isEmpty()) {

                if (finishDate.isEmpty()) {
                    documentSeries = database.documentSeriesDAO().getAllGreater(startDa.getTime());
                } else if (startDate.isEmpty()) {
                    documentSeries = database.documentSeriesDAO().getAllLesser(finishDa.getTime());
                } else {
                    documentSeries = database.documentSeriesDAO().getAll(startDa.getTime(), finishDa.getTime());
                }
            } else {
                documentSeries = database.documentSeriesDAO().getAllNonZeroDate();
            }
        }


        if (documentSeries.size() == 0) {
            showDialog(R.string.error, R.string.upload_documents_no_data_fail, SweetAlertDialog.ERROR_TYPE);
            return;
        }

        progressDialog.setTitle(getString(R.string.sending));

        firstServerRequestTime = System.currentTimeMillis();

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < documentSeries.size(); i++) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setContentText(String.format("%d %s %d", finalI + 1, getString(R.string.from), documentSeries.size()));
                        }
                    });
                    Document document = database.documentDAO().get(documentSeries.get(i).getDocument_id());
                    if (!upload(reUpload, document)) {
                        if (!progressDialog.isShowing()) {
                            showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                        }
                        return;
                    }

                }
                if (progressDialog.getAlerType() == SweetAlertDialog.PROGRESS_TYPE) {
                    showDialog(R.string.alert, R.string.upload_information_success, SweetAlertDialog.SUCCESS_TYPE);
                }
            }
        }).start();

    }

    public void showDialog(@StringRes final int title, @StringRes final int message, final int alertType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                progressDialog.setTitleText(getString(title))
                        .setContentText(getString(message))
                        .setConfirmText(getString(R.string.ok))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                            }
                        })
                        .changeAlertType(alertType);
            }
        });
    }

    public boolean upload(final boolean reUpload, final Document document) {

        // if document not yet send , first send document to get global id
        log.info(TAG+ "document.getGlobal_id(): " + document.getGlobal_id());
//        if (document.getGlobal_id() == -1) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("document", document);
            Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").saveDocument("Bearer " + MyApplication.sp.getAuthToken(), body);
            sleepRequestToServer();
            Response<ResponseBody> response = call.execute();
            if (response.code() == 200) {
                ResponseBody resp = response.body();
                JSONObject jsonObject = new JSONObject(resp.string());
                int status_code = jsonObject.getInt("status_code");
                if (status_code == 0) {
                    document.setGlobal_id(jsonObject.getLong("global_id"));
                    database.documentDAO().update(document);

                }
            } else {
                showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                log.error(response.errorBody().string());
                if (BuildConfig.DEBUG) {
                    Intent intent = new Intent(UploadDocumentsActivity.this, ShowErrorActivity.class);
                    intent.putExtra("errorBody", response.errorBody().string());
                    startActivity(intent);
                }
                return false;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
            showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
            log.error(e.getMessage(),e);
            e.printStackTrace();
            return false;
        }
//        }

        // for each document series of document that not yet uploaded , first upload images and
        // get server link address for each image then send document series with global_id of document and position of document series

        List<DocumentSeries> documentSeriesList = database.documentSeriesDAO().getAll(document.getId());
        for (int position = 0; position < documentSeriesList.size(); position++) {
            Log.d(TAG, "position: " + position);
            DocumentSeries documentSeries = documentSeriesList.get(position);
            if (!reUpload && documentSeries.isUpload()) {
                continue;
            }
            List<Property> imageProperties = database.propertyDAO().getAllType(Property.type.image.getValue());
            for (Property p : imageProperties) {
                PropertyValues propertyValues = documentSeries.getMapValues().get(p.getId());
                if (propertyValues != null && propertyValues.getVal() != null && !propertyValues.getVal().isEmpty()) {
                    ImagePropertyValues values = new Gson().fromJson(propertyValues.getVal(), ImagePropertyValues.class);
                    // if image not yet uploaded , upload image and getVisible server link
                    if (!values.isUpload) {
                        try {
                            File file = new File(values.localLink);
                            MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                            Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").upload("Bearer " + MyApplication.sp.getAuthToken(), filePart);
                            sleepRequestToServer();
                            Response<ResponseBody> response = call.execute();
                            if (response.code() == 200) {
                                ResponseBody resp = response.body();
                                JSONObject jsonObject = new JSONObject(resp.string());
                                int status_code = jsonObject.getInt("status_code");
                                if (status_code == 0) {
                                    values.isUpload = true;
                                    values.serverLink = jsonObject.getString("link");
                                    propertyValues.setVal(new Gson().toJson(values));
                                    database.documentSeriesDAO().update(documentSeries);
                                }
                            } else {
                                showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                                log.error(response.errorBody().string());
                                if (BuildConfig.DEBUG) {
                                    Intent intent = new Intent(UploadDocumentsActivity.this, ShowErrorActivity.class);
                                    intent.putExtra("errorBody", response.errorBody().string());
                                    startActivity(intent);
                                }
                                return false;
                            }
                        } catch (Exception e) {
                            Crashlytics.logException(e);
                            showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                            log.error(e.getMessage(),e);
                            e.printStackTrace();
                            return false;
                        }
                    }
                }
            }

            // send document series
            try {
                List<Object> valuesJsonArray = new ArrayList<>();
                Iterator<Map.Entry<Long, PropertyValues>> it = documentSeries.getMapValues().entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<Long, PropertyValues> e = it.next();
                    HashMap<String, Object> object = new HashMap<>();
                    object.put("pid", e.getKey());
                    object.put("chk", e.getValue().getChk());
                    object.put("val", e.getValue().getVal());
                    valuesJsonArray.add(object);
                }
                Map<String, Object> body = new HashMap<>();
                body.put("values", valuesJsonArray);
                body.put("global_id", document.getGlobal_id());
                body.put("position", documentSeries.getPosition());
                Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").saveDocumentSeries("Bearer " + MyApplication.sp.getAuthToken(), body);
                sleepRequestToServer();
                Response<ResponseBody> response = call.execute();
                if (response.code() == 200) {
                    ResponseBody resp = response.body();
                    JSONObject jsonObject = new JSONObject(resp.string());
                    int status_code = jsonObject.getInt("status_code");
                    if (status_code == 0) {
                        documentSeries.setUpload(true);
                        database.documentSeriesDAO().update(documentSeries);
                        if (jsonObject.has("updated_at")) {
                            MyApplication.sp.setLastDocumentsUpdatedTime(jsonObject.getLong("updated_at"));
                        }
                    }
                } else {
                    showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                    log.error(response.errorBody().string());
                    if (BuildConfig.DEBUG) {
                        Intent intent = new Intent(UploadDocumentsActivity.this, ShowErrorActivity.class);
                        intent.putExtra("errorBody", response.errorBody().string());
                        startActivity(intent);
                    }
                    return false;
                }
            } catch (Exception e) {
                Crashlytics.logException(e);
                showDialog(R.string.error, R.string.upload_information_fail, SweetAlertDialog.ERROR_TYPE);
                log.error(e.getMessage(),e);
                e.printStackTrace();
                return false;
            }
        }

        document.setStatus(pending);
        database.documentDAO().update(document);

        return true;
    }

    private void sleepRequestToServer() throws InterruptedException {
        if ((++serverRequestCount) % 60 == 0) {
            serverRequestCount = 0;
            Log.d(TAG, "upload: " + 60000 + " - " + System.currentTimeMillis() + " - " + firstServerRequestTime + " = " + (60000 - (System.currentTimeMillis() - firstServerRequestTime)));
            long sleep = 60000 - (System.currentTimeMillis() - firstServerRequestTime);
            if (sleep > 0) {
                Thread.sleep(sleep);
            }
            firstServerRequestTime = System.currentTimeMillis();
        } else {
            Thread.sleep(1000);
        }
    }

    public boolean checkInternet() {
        if (Utilities.isAvailableNetwork(UploadDocumentsActivity.this)) {
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
}