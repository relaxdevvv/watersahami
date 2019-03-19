package ir.nimcode.dolphin.activity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.ogaclejapan.smarttablayout.SmartTabLayout;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItemAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItems;

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
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.database.AppDatabase;
import ir.nimcode.dolphin.fragment.ImageFragment;
import ir.nimcode.dolphin.fragment.LocationFragment;
import ir.nimcode.dolphin.fragment.PropertyFragment;
import ir.nimcode.dolphin.fragment.SheetFragment;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.FormSheet;
import ir.nimcode.dolphin.model.FormSheetProperty;
import ir.nimcode.dolphin.model.ImagePropertyValues;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.PropertyValues;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.Utilities;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

import static ir.nimcode.dolphin.model.Document.status.accept;
import static ir.nimcode.dolphin.model.Document.status.draft;
import static ir.nimcode.dolphin.model.Document.status.pending;
import static ir.nimcode.dolphin.model.Document.status.reject;

public class SheetActivity extends FullAppCompatActivity {

    public final static String TAG = "TAG_SheetActivity";
    public static List<Sheet> sheets;
    public static Map<Long, Property> allProperties;
    public static List<List<Property>> sheetsProperties;
    public static List<DocumentSeries> documentDocumentSeries;
    public static Document document;
    public DocumentSeries documentSeries;
    //    @BindView(R.id.action_save)
//    FloatingActionButton actionSave;
    @BindView(R.id.view_pager_tab)
    SmartTabLayout viewPagerTab;
    private long form_id;
    private long document_id;
    private boolean new_document_series;
    private boolean isUpload;
    private boolean oldDocumentSeries;
    private SweetAlertDialog progressDialog;
    private AppDatabase database;
    private MenuItem actionSave;
    private MenuItem actionSeries;
    private MenuItem actionSendDocument;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.document_status)
    TextView documentStatus;

    boolean isSaved = false;

    private Logger log = LoggerFactory.getLogger(SheetActivity.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheet);

        ButterKnife.bind(this);

        sheets = null;
        allProperties = null;
        sheetsProperties = null;
        documentDocumentSeries = null;
        document = null;

        database = MyApplication.database;

        Intent intent = getIntent();

        form_id = intent.getLongExtra("form_id", -1);
        document_id = intent.getLongExtra("document_id", -1);
        isSaved = intent.getBooleanExtra("isSaved", false);
        new_document_series = intent.getBooleanExtra("new_document_series", false);
        int document_series_position = intent.getIntExtra("document_series_position", -1);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, database.formDAO().get(form_id).getName_fa()));

        if (document_id != -1) {
            document = database.documentDAO().get(document_id);
            documentDocumentSeries = database.documentSeriesDAO().getAll(document_id);

            if (document_series_position != -1 && document_series_position != documentDocumentSeries.size() - 1) {
//                setActionSaveVisibility(false);
                documentSeries = documentDocumentSeries.get(document_series_position);
                oldDocumentSeries = true;
            } else {
//                setActionSaveVisibility(true);
                if (documentDocumentSeries.size() > 0) {
                    documentSeries = documentDocumentSeries.get(documentDocumentSeries.size() - 1);
                } else {
                    HashMap<Long, PropertyValues> values = new HashMap<>();
                    Utilities.UTM utm = Utilities.LatLngToUTM(new LatLng(document.getLatitude(), document.getLongitude()));
                    values.put(database.propertyDAO().getByTag("utm_zone").getId(), new PropertyValues(utm.getZone() + "", true));
                    values.put(database.propertyDAO().getByTag("utm_x").getId(), new PropertyValues((int) utm.getX() + "", true));
                    values.put(database.propertyDAO().getByTag("utm_y").getId(), new PropertyValues((int) utm.getY() + "", true));
                    documentSeries = new DocumentSeries(document_id, form_id, new Date().getTime(), false, values, 0);
                    database.documentSeriesDAO().add(documentSeries);
                }
            }

            if (draft.getValue() == document.getStatus()) {
                documentStatus.setText(R.string.draft_document);
                documentStatus.setTextColor(getResources().getColor(R.color.md_light_blue_A700));
            } else if (accept.getValue() == document.getStatus()) {
                documentStatus.setText(R.string.accept_document);
                documentStatus.setTextColor(getResources().getColor(R.color.md_light_green_A700));
            } else if (pending.getValue() == document.getStatus()) {
                documentStatus.setText(R.string.pending_document);
                documentStatus.setTextColor(getResources().getColor(R.color.md_amber_A200));
            } else if (reject.getValue() == document.getStatus()) {
                documentStatus.setText(R.string.reject_document);
                documentStatus.setTextColor(getResources().getColor(R.color.md_red_700));
            }

        } else {
            documentStatus.setText(R.string.new_document);
        }

        Bundle args = new Bundle();
        args.putAll(intent.getExtras());

        if (document_id == -1 || new_document_series) {
            args.putBoolean("new_document", true);
        }

        FragmentPagerItems pages = new FragmentPagerItems(this);
        pages.add(0, FragmentPagerItem.of(getString(R.string.summary), SheetFragment.class, args));

        sheets = new ArrayList<>();
        sheetsProperties = new ArrayList<>();
        allProperties = new HashMap<>();

        // get form sheets with form_id
        List<FormSheet> formSheets = database.formSheetDAO().get(form_id);

        // load sheet info for each formSheets
        for (int i = 0; i < formSheets.size(); i++) {

            FormSheet formSheet = formSheets.get(i);
            Sheet sheet = database.sheetDAO().get(formSheet.getSheet_id());
            if (sheet == null) {
                continue;
            }
            // load properties for each sheet
            List<FormSheetProperty> formSheetProperties = database.formSheetPropertyDAO().get(formSheet.getId());
            int propertiesCheckedCount = 0;

            Map<Long, PropertyValues> documentSeriesValues = null;
            if (documentSeries != null) {
                documentSeriesValues = documentSeries.getMapValues();
            }

            // load properties info
            List<Property> properties = new ArrayList<>();
            for (int j = 0; j < formSheetProperties.size(); j++) {

                Property property = database.propertyDAO().get(formSheetProperties.get(j).getProperty_id());

                // load property value
                if (documentSeries != null) {
                    PropertyValues propertyValues = documentSeriesValues.get(property.getId());
                    if (propertyValues != null) {
                        property.setValue(propertyValues.getVal());
                        property.setChecked(propertyValues.isChk());
                    }
                }

                if (document_series_position != -1 && document_series_position != documentDocumentSeries.size() - 1) {
                    property.setEnabled(false);
                }

                // calculate checked property
                if (property.isChecked()) {
                    propertiesCheckedCount++;
                }

                allProperties.put(property.getId(), property);
                properties.add(property);
            }

            sheetsProperties.add(properties);

            sheet.setProperties_count(formSheetProperties.size());
            sheet.setProperties_checked_count(propertiesCheckedCount);
            sheets.add(sheet);

            if (sheet.isVisibility()) {
                if (sheet.getType() == Sheet.type.property.getValue()) {
                    pages.add(FragmentPagerItem.of(sheet.getName_fa(), PropertyFragment.class));
                } else if (sheet.getType() == Sheet.type.location.getValue()) {
                    pages.add(FragmentPagerItem.of(sheet.getName_fa(), LocationFragment.class));
                } else if (sheet.getType() == Sheet.type.camera.getValue()) {
                    pages.add(FragmentPagerItem.of(sheet.getName_fa(), ImageFragment.class));
                } else if (sheet.getType() == Sheet.type.user.getValue()) {
                    pages.add(FragmentPagerItem.of(sheet.getName_fa(), PropertyFragment.class));
                }
            }
        }
        FragmentPagerItemAdapter adapter = new FragmentPagerItemAdapter(getSupportFragmentManager(), pages);

        viewPager = findViewById(R.id.view_pager);
        viewPager.setRotationY(180);
        viewPager.setOffscreenPageLimit(pages.size());
        viewPager.setAdapter(adapter);

        SmartTabLayout viewPagerTab = findViewById(R.id.view_pager_tab);
        viewPagerTab.setViewPager(viewPager);

        progressDialog = new SweetAlertDialog(SheetActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
        progressDialog.setCancelable(false);

//        actionSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                save(false);
//            }
//        });


    }

    @Override
    public void onBackPressed() {
        if (isSaved) {
            Intent intent = new Intent(SheetActivity.this, MainActivity.class);
            if (document != null) {
                intent.putExtra("lat", document.getLatitude());
                intent.putExtra("lon", document.getLongitude());
            }
            clearStaticVariables();
            startActivity(intent);
//        overridePendingTransition(R.anim.left_in, R.anim.left_out);
            finish();
        } else {
            progressDialog.setTitleText(getString(R.string.warning))
                    .showContentText(true)
                    .setContentText(getString(R.string.document_not_saved))
                    .setConfirmText(getString(R.string.yes))
                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            save(false);
                        }
                    })
                    .setCancelText(getString(R.string.no))
                    .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                        @Override
                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                            sweetAlertDialog.dismiss();
                            isSaved = true;
                            onBackPressed();
                        }
                    })
                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            progressDialog.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.document_menu, menu);
        actionSeries = menu.findItem(R.id.action_document_series);
        actionSave = menu.findItem(R.id.action_save);
        actionSendDocument = menu.findItem(R.id.action_send_document);

        if (document_id == -1 || new_document_series) {
            actionSeries.setEnabled(false);
        }

        if (oldDocumentSeries) {
            actionSave.setEnabled(false);
            actionSendDocument.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_document_series: {
                Intent intent = new Intent(SheetActivity.this, DocumentSeriesActivity.class);
                intent.putExtra("form_id", form_id);
                intent.putExtra("document_id", document_id);
                startActivity(intent);
                finish();
                break;
            }

            case R.id.action_save: {

                save(false);
                break;
            }

            case R.id.action_send_document: {

                save(true);
                break;
            }


        }
        return super.onOptionsItemSelected(item);
    }


    public void setCurrentItem(int itemPosition) {
        viewPager.setCurrentItem(itemPosition);
    }

    public void save(boolean upload) {

        viewPager.setCurrentItem(0);
        isUpload = upload;

        if (progressDialog.isShowing()){
            progressDialog.dismiss();
        }
        progressDialog.setTitleText(getString(R.string.saving));
        progressDialog.showContentText(false);

        Button confirmButton = progressDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM);
        if (confirmButton != null)
            confirmButton.setVisibility(View.GONE);
        Button cancelButton = progressDialog.getButton(SweetAlertDialog.BUTTON_CANCEL);
        if (cancelButton != null)
            cancelButton.setVisibility(View.GONE);

        progressDialog.show();

        Utilities.UTM utm = new Utilities.UTM(0, 0, 0, 'N');

        Map<Long, PropertyValues> documentDataProperties = new HashMap<>();
        for (Property property : allProperties.values()) {

            String relationTag = property.getRelationTag();
            if (relationTag != null && !relationTag.isEmpty()) {
                Log.d(TAG, "save: " + relationTag);
                Log.d(TAG, "save: " + property.getRelation());
                String[] relsId = property.getRelation().split(",");
                Property rel1 = allProperties.get(Long.parseLong(relsId[0]));

                if (relationTag.equals(">") || relationTag.equals("<")) {
                    checkComprableRelations(property, rel1, !property.isHasError(), sheets.get(property.getSheetPosition()));
                } else {
                    Property rel2 = allProperties.get(Long.parseLong(relsId[1]));
                    checkComputableRelations(property, rel1, rel2, !property.isHasError(), sheets.get(property.getSheetPosition()));
                }

            }
            if (property.isHasError()) {
                showFailedSaveDialog(getString(R.string.save_information_failed_has_property_error));
                return;
            }
            documentDataProperties.put(property.getId(), new PropertyValues(property.getValue(), property.isChecked()));
            if (property.getTag() != null && property.getValue() != null && property.getTag().startsWith("utm")) {
                switch (property.getTag()) {
                    case "utm_zone": {
                        utm.setZone(Integer.parseInt(property.getValue()));
                        break;
                    }
                    case "utm_x": {
                        utm.setX(Double.parseDouble(property.getValue()));
                        break;
                    }
                    case "utm_y": {
                        utm.setY(Double.parseDouble(property.getValue()));
                        break;
                    }
                }
            }
        }
        if (utm.getZone() == 0 || utm.getX() == 0 || utm.getY() == 0) {
            showFailedSaveDialog(getString(R.string.save_information_failed_no_location));
            return;
        }
        LatLng location = Utilities.UTMToLatLng(utm);
        if (document == null) {
            document_id = database.documentDAO().add(new Document(form_id, -1, draft, location.latitude, location.longitude));
            database.documentSeriesDAO().add(new DocumentSeries(document_id, form_id, new Date().getTime(), false, documentDataProperties, 0));
            document = database.documentDAO().get(document_id);
        } else {
            document.setLatitude(location.latitude);
            document.setLongitude(location.longitude);
            document.setStatus(draft);
            database.documentDAO().update(document);
            Calendar calendar = Calendar.getInstance();
            documentDataProperties.put(MyApplication.database.propertyDAO().getByTag("today").getId(), new PropertyValues(new JalaliCalendar(calendar).getGregorianCalendar().getTimeInMillis() + "", true));
            if (new_document_series) {

                database.documentSeriesDAO().add(new DocumentSeries(document_id, form_id, calendar.getTime().getTime(), false, documentDataProperties, database.documentSeriesDAO().getAll(document_id).size()));
            } else {
                documentSeries.setMapValues(documentDataProperties);
                documentSeries.setDate(calendar.getTime().getTime());
                documentSeries.setUpload(false);
                database.documentSeriesDAO().update(documentSeries);
            }
        }

        if (upload) {
            upload();
        } else {
            isSaved = true;
            showSuccessSaveDialog();
        }


    }


    private void checkComprableRelations(Property property, Property rel, boolean count, Sheet sheet) {
        if (property.getValue() != null && !property.getValue().isEmpty()) {
            double val = Double.parseDouble(property.getValue());
            if (rel.getValue() != null && !rel.getValue().isEmpty()) {
                double val2 = Double.parseDouble(rel.getValue());
                if (property.getRelationTag().equals(">") && val < val2) {
                    property.setHasError(true);
                    if (count) {
                        sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                    }
                } else if (property.getRelationTag().equals("<") && val > val2) {
                    property.setHasError(true);
                    if (count) {
                        sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                    }
                }
            }
        }
    }

    private void checkComputableRelations(Property property, Property rel1, Property rel2, boolean count, Sheet sheet) {

        if (rel1.getValue() != null && !rel1.getValue().isEmpty() && rel2.getValue() != null && !rel2.getValue().isEmpty()) {
            double val1 = Double.parseDouble(rel1.getValue());
            double val2 = Double.parseDouble(rel2.getValue());
            switch (property.getRelationTag()) {
                case "*": {
                    property.setValue((val1 * val2) + "");
                    break;
                }
                case "-": {
                    property.setValue((val1 - val2) + "");
                    break;
                }
                case "+": {
                    property.setValue((val1 + val2) + "");
                    break;
                }
                case "/": {
                    if (val2 == 0) {
                        if (count) {
                            sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                        }
                    } else {
                        property.setValue((val1 / val2) + "");
                    }
                    break;
                }
            }
        }
    }

    public void upload() {

        if (!checkInternet()) {
            return;
        }

        progressDialog.dismiss();
        progressDialog.setTitleText(getString(R.string.sending));
        progressDialog.showContentText(false);
        Button confirmButton = progressDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM);
        if (confirmButton != null)
            confirmButton.setVisibility(View.GONE);
        Button cancelButton = progressDialog.getButton(SweetAlertDialog.BUTTON_CANCEL);
        if (cancelButton != null)
            cancelButton.setVisibility(View.GONE);
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {


                // if document not yet send , first send document to get global id
                Log.d(TAG, "document.getGlobal_id(): " + document.getGlobal_id());
//                if (document.getGlobal_id() == -1 ) {
                try {
                    Map<String, Object> body = new HashMap<>();
                    body.put("document", document);
                    Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").saveDocument("Bearer " + MyApplication.sp.getAuthToken(), body);
                    Response<ResponseBody> response = call.execute();
                    if (response.code() == 200) {
                        ResponseBody resp = response.body();
                        JSONObject jsonObject = new JSONObject(resp.string());
                        int status_code = jsonObject.getInt("status_code");
                        if (status_code == 0) {
                            document.setGlobal_id(jsonObject.getLong("global_id"));
                            database.documentDAO().update(document);
                        } else {
                            showFailedUploadDialog();
                            return;
                        }
                    } else {
                        showFailedUploadDialog();
                        log.error(response.errorBody().string());
                        if (BuildConfig.DEBUG) {
                            Intent intent = new Intent(SheetActivity.this, ShowErrorActivity.class);
                            intent.putExtra("errorBody", response.errorBody().string());
                            startActivity(intent);
                        }
                        return;
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                    showFailedUploadDialog();
                    log.error(e.getMessage(),e);
                    e.printStackTrace();
                    return;
                }
//                }

                // for each document series of document that not yet uploaded , first upload images and
                // get server link address for each image then send document series with global_id of document and position of document series

                List<DocumentSeries> documentSeriesList = database.documentSeriesDAO().getAll(document_id);
                for (DocumentSeries documentSeries : documentSeriesList) {
                    if (documentSeries.isUpload()) {
                        continue;
                    }
                    List<Property> imageProperties = database.propertyDAO().getAllType(Property.type.image.getValue());
                    for (Property p : imageProperties) {
                        PropertyValues propertyValues = documentSeries.getMapValues().get(p.getId());
                        if (propertyValues != null && propertyValues.getVal() != null && !propertyValues.getVal().isEmpty()) {
                            ImagePropertyValues values = new Gson().fromJson(propertyValues.getVal(), ImagePropertyValues.class);
                            // if image not yet uploaded , upload image and get server link
                            if (!values.isUpload) {
                                try {
                                    File file = new File(values.localLink);
                                    MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
                                    Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").upload("Bearer " + MyApplication.sp.getAuthToken(), filePart);
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
                                        showFailedUploadDialog();
                                        log.error(response.errorBody().string());
                                        if (BuildConfig.DEBUG) {
                                            Intent intent = new Intent(SheetActivity.this, ShowErrorActivity.class);
                                            intent.putExtra("errorBody", response.errorBody().string());
                                            startActivity(intent);
                                        }
                                        return;
                                    }
                                } catch (Exception e) {
                                    Crashlytics.logException(e);
                                    showFailedUploadDialog();
                                    log.error(e.getMessage(),e);
                                    e.printStackTrace();
                                    return;
                                }
                            }


                        }
                    }

                    // send document series
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
                    try {
                        Response<ResponseBody> response = call.execute();
                        if (response.code() == 200) {
                            ResponseBody resp = response.body();
                            JSONObject jsonObject = new JSONObject(resp.string());
                            int status_code = jsonObject.getInt("status_code");
                            if (status_code == 0) {
                                documentSeries.setUpload(true);
                                database.documentSeriesDAO().update(documentSeries);
                            }
                        } else {
                            showFailedUploadDialog();
                            log.error(response.errorBody().string());
                            if (BuildConfig.DEBUG) {
                                Intent intent = new Intent(SheetActivity.this, ShowErrorActivity.class);
                                intent.putExtra("errorBody", response.errorBody().string());
                                startActivity(intent);
                            }
                            return;
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        showFailedUploadDialog();
                        log.error(e.getMessage(),e);
                        e.printStackTrace();
                        return;
                    }
                }
                document.setStatus(pending);
                database.documentDAO().update(document);

                isSaved = true;

                showSuccessUploadDialog();

            }
        }).start();
    }

    public void clearStaticVariables() {
        sheets = null;
        allProperties = null;
        sheetsProperties = null;
        documentDocumentSeries = null;
        documentSeries = null;
        document = null;
    }

    private void showSuccessSaveDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitleText(getString(R.string.alert))
                        .setContentText(getString(R.string.save_information_success))
                        .setConfirmText(getString(R.string.ok))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(SheetActivity.this, SheetActivity.class);
                                intent.putExtra("document_id", document.getId());
                                intent.putExtra("form_id", document.getForm_id());
                                intent.putExtra("isSaved", true);
                                clearStaticVariables();
                                startActivity(intent);
                                finish();
                            }
                        })
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
            }
        });
    }

    private void showFailedSaveDialog(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitleText(getString(R.string.error))
                        .setContentText(message)
                        .setConfirmText(getString(R.string.close))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                            }
                        })
                        .setCancelText(getString(R.string.retry))
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                save(isUpload);
                            }
                        })
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            }
        });
    }

    private void showSuccessUploadDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitleText(getString(R.string.alert))
                        .setContentText(getString(R.string.upload_information_success))
                        .setConfirmText(getString(R.string.ok))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(SheetActivity.this, SheetActivity.class);
                                intent.putExtra("document_id", document.getId());
                                intent.putExtra("form_id", document.getForm_id());
                                intent.putExtra("isSaved", true);
                                clearStaticVariables();
                                startActivity(intent);
                                finish();
                            }
                        })
                        .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
            }
        });
    }


    private void showFailedUploadDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitleText(getString(R.string.error))
                        .setContentText(getString(R.string.upload_information_fail))
                        .setConfirmText(getString(R.string.close))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(SheetActivity.this, SheetActivity.class);
                                intent.putExtra("document_id", document.getId());
                                intent.putExtra("form_id", document.getForm_id());
                                clearStaticVariables();
                                startActivity(intent);
                                finish();
                            }
                        })
                        .setCancelText(getString(R.string.retry))
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                upload();
                            }
                        })
                        .changeAlertType(SweetAlertDialog.ERROR_TYPE);
            }
        });
    }

    public boolean checkInternet() {
        if (Utilities.isAvailableNetwork(SheetActivity.this)) {
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
