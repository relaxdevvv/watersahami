package ir.nimcode.dolphin.activity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import com.codekidlabs.storagechooser.Content;
import com.codekidlabs.storagechooser.StorageChooser;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.marcouberti.autofitbutton.AutoFitButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.BuildConfig;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.adapter.KmlFilesAdapter;
import ir.nimcode.dolphin.api.APIBaseCreator;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.KmlFile;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class KmlFilesActivity extends FullAppCompatActivity {

    private static final int PICK_FILE_REQUEST = 1001;
    @BindView(R.id.noting_found_error_layout)
    LinearLayout notingFoundErrorLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.add_new)
    FloatingActionMenu addNew;
    @BindView(R.id.server)
    FloatingActionButton server;
    @BindView(R.id.storage)
    FloatingActionButton storage;
    private KmlFilesAdapter kmlFilesAdapter;
    private List<KmlFile> kmlFiles;
    private InputStream source;
    private SweetAlertDialog progressDialog;
    private AutoFitButton save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kml_files);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.kml_files)));

        kmlFiles = MyApplication.database.kmlLayerDAO().getAll();
        kmlFilesAdapter = new KmlFilesAdapter(KmlFilesActivity.this, kmlFiles);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(KmlFilesActivity.this));
        recyclerView.setAdapter(kmlFilesAdapter);
        kmlFilesAdapter.notifyDataSetChanged();
        showNotingFoundErrorLayout(kmlFiles.isEmpty());

        progressDialog = new SweetAlertDialog(KmlFilesActivity.this, SweetAlertDialog.PROGRESS_TYPE);
        progressDialog.getProgressHelper().setBarColor(getResources().getColor(R.color.colorAccent));
        progressDialog.setTitleText(getString(R.string.loading));
        progressDialog.setCancelable(false);

        addNew.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {
                if (addNew.isOpened()) {
                    addNew.setMenuButtonColorNormal(getResources().getColor(R.color.md_red_500));
                } else {
                    addNew.setMenuButtonColorNormal(getResources().getColor(R.color.accent));
                }

            }
        });

        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNew.setMenuButtonColorNormal(getResources().getColor(R.color.accent));
                addNew.close(true);
                getKmlsUpdate();
            }
        });

        storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNew.setMenuButtonColorNormal(getResources().getColor(R.color.accent));
                addNew.close(true);
                showKmlFileDialog();
            }
        });

//        try {
//            XmlPullParserFactory pullParserFactory = XmlPullParserFactory.newInstance();
//            XmlPullParser parser = pullParserFactory.newPullParser();
//            InputStream in_s = getApplicationContext().getAssets().open("kmls/study_areas.kml");
//            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
//            parser.setInput(in_s, null);
//            parseXML(parser);
//        } catch (XmlPullParserException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void getKmlsUpdate() {

        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Call<ResponseBody> call = APIBaseCreator.getAPIAdapter("server").getKmlsUpdate("Bearer " + MyApplication.sp.getAuthToken(), MyApplication.sp.getLastKmlsUpdatedTime());
                    Response<ResponseBody> response = call.execute();

                    if (response.code() == 200) {
                        ResponseBody resp = response.body();
                        JSONObject jsonObject = new JSONObject(resp.string());
                        int status_code = jsonObject.getInt("status_code");

                        if (status_code == 0) {

                            downloadKmlFiles(jsonObject.getJSONArray("kmls"));

                            MyApplication.sp.setLastKmlsUpdatedTime(System.currentTimeMillis());

                            showProgressDialog("", getString(R.string.save_information_success), SweetAlertDialog.SUCCESS_TYPE);

                        } else {
                            showProgressDialog(getString(R.string.warning), getString(R.string.get_kmls_update_error_no_data), SweetAlertDialog.WARNING_TYPE);
                        }
                    } else {
                        if (BuildConfig.DEBUG) {
                            Intent intent = new Intent(KmlFilesActivity.this, ShowErrorActivity.class);
                            intent.putExtra("errorBody", response.errorBody().string());
                            startActivity(intent);
                        }
                        showProgressDialog(getString(R.string.error), getString(R.string.save_information_fail), SweetAlertDialog.ERROR_TYPE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showProgressDialog(getString(R.string.error), getString(R.string.save_information_fail), SweetAlertDialog.ERROR_TYPE);
                }

            }
        }).start();

    }

    private void showProgressDialog(final String title, final String message, final int Type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressDialog.setTitleText(title)
                        .setContentText(message)
                        .setConfirmText(getString(R.string.close))
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                progressDialog.dismiss();
                                showNotingFoundErrorLayout(kmlFiles.isEmpty());
                                kmlFilesAdapter.notifyDataSetChanged();
                            }
                        })
                        .changeAlertType(Type);
            }
        });

    }


    private void downloadKmlFiles(JSONArray jsonArray) throws JSONException, IOException {

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String name = jsonObject.getString("title");
            String url = jsonObject.getString("url");
            String source = "http://" + MyApplication.sp.getServerAddress() + "/storage/" + url;
            String destination = Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "KmlFiles/";
            String fileName = System.currentTimeMillis() + ".kml";
            String kmlFileUrl = Utilities.downloadFile(source, destination, fileName);

            final KmlFile kmlFile = new KmlFile(name, true, kmlFileUrl);
            MyApplication.database.kmlLayerDAO().add(kmlFile);
            kmlFiles.add(kmlFile);

        }
    }


    private void showKmlFileDialog() {

        final Dialog dialog = new Dialog(KmlFilesActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_new_kml_file);

        final MaterialEditText name = dialog.findViewById(R.id.name);

        AutoFitButton selectFile = dialog.findViewById(R.id.select_file);
        selectFile.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Content content = new Content();
                content.setInternalStorageText("حافظه داخلی");
                content.setCancelLabel(getString(R.string.cancel));
                content.setSelectLabel(getString(R.string.select));
                content.setOverviewHeading(getString(R.string.choose_file));
                StorageChooser chooser = new StorageChooser.Builder()
                        .withActivity(KmlFilesActivity.this)
                        .withContent(content)
                        .allowCustomPath(true)
                        .setType(StorageChooser.FILE_PICKER)
                        .withFragmentManager(getFragmentManager())
                        .withMemoryBar(true)
                        .build();
                chooser.show();

                chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
                    @Override
                    public void onSelect(String path) {

                        try {
                            if (path != null) {
                                File f = new File(path);
                                source = new FileInputStream(f);
                                if (save != null) {
                                    save.setBackground(getResources().getDrawable(R.drawable.selector_accent_rounded_10dp));
                                    save.setEnabled(true);
                                }
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                });

            }

        });

        save = dialog.findViewById(R.id.save);
        save.setEnabled(false);
        save.setBackground(getResources().getDrawable(R.drawable.selector_disabled_rounded_10dp));
        save.setOnClickListener(new View.OnClickListener() {


            public void onClick(View view) {

                if (name.getText().toString().isEmpty()) {
                    name.setError(getString(R.string.kml_file_name_empty_error));
                    return;
                }
                progressDialog.show();
                if (source != null) {
                    String kmlFileUrl = Utilities.copyFile(source, new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "KmlFiles"), System.currentTimeMillis() + ".kml");
                    final KmlFile kmlFile = new KmlFile(name.getText().toString(), true, kmlFileUrl);
                    MyApplication.database.kmlLayerDAO().add(kmlFile);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setTitleText("")
                                    .setContentText(getString(R.string.save_information_success))
                                    .setConfirmText(getString(R.string.ok))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            dialog.dismiss();
                                            progressDialog.dismiss();
                                            kmlFiles.add(kmlFile);
                                            showNotingFoundErrorLayout(kmlFiles.isEmpty());
                                            kmlFilesAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                        }
                    }, 1000);
                } else {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setTitleText(getString(R.string.error))
                                    .setContentText(getString(R.string.save_information_fail))
                                    .setConfirmText(getString(R.string.retry))
                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                        @Override
                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                            progressDialog.dismiss();
                                        }
                                    })
                                    .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                        }
                    }, 1000);
                }
            }
        });
        dialog.show();
    }

    public void showNotingFoundErrorLayout(boolean visibility) {
        notingFoundErrorLayout.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

//    private void parseXML(XmlPullParser parser) throws XmlPullParserException, IOException {
//        int eventType = parser.getEventType();
//        boolean flag = false;
//        a:
//        while (eventType != XmlPullParser.END_DOCUMENT) {
//            String name;
//            switch (eventType) {
//
//                case XmlPullParser.START_DOCUMENT: {
//                    break;
//                }
//
//                case XmlPullParser.START_TAG: {
//                    name = parser.getName();
//                    if (name.equals("Placemark")) {
//                        long studyAreaCode = 0;
//                        String studyAreaName;
//                        while (true) {
//                            eventType = parser.next();
//                            if (eventType == XmlPullParser.START_TAG) {
//                                name = parser.getName();
//                                if (name.equals("SimpleData")) {
//                                    if (parser.getAttributeValue(null, "name").equals("ST_Code")) {
//                                        studyAreaCode = Long.parseLong(parser.nextText());
//                                    } else if (parser.getAttributeValue(null, "name").equals("ST_Name")) {
//                                        studyAreaName = parser.nextText();
//                                        break;
//                                    }
//                                }
//                            }
//                        }
//                        ArrayList<String> coordinates = new ArrayList<>();
//                        while (true) {
//                            eventType = parser.next();
//                            if (eventType == XmlPullParser.START_TAG) {
//                                name = parser.getName();
//                                if (name.equals("coordinates")) {
//                                    coordinates.add(parser.nextText());
//                                } else if (name.equals("Placemark")) {
//                                    MyApplication.databaseStudyArea.insertStudyArea(studyAreaCode, studyAreaName, getPolygons(coordinates));
//                                    flag = true;
//                                    break;
//                                }
//                            } else if (eventType == XmlPullParser.END_DOCUMENT) {
//                                MyApplication.databaseStudyArea.insertStudyArea(studyAreaCode, studyAreaName, getPolygons(coordinates));
//                                break a;
//                            }
//                        }
//                    }
//                    break;
//                }
//
//                case XmlPullParser.END_TAG: {
//                    break;
//                }
//            }
//            if (!flag)
//                eventType = parser.next();
//            else
//                flag = false;
//        }
//    }

//    public String getPolygons(ArrayList<String> coordinates) {
//        StringBuilder polygons = new StringBuilder("GeomFromText('MULTIPOLYGON(");
//        int iMax = coordinates.size() - 1;
//        for (int i = 0; ; i++) {
//            polygons.append("((");
//            String coordinate = coordinates.get(i);
//            String[] points = coordinate.split(" ");
//            int jMax = points.length - 1;
//            for (int j = 0; ; j++) {
//                String[] point = points[j].split(",");
//                polygons.append(point[1] + " " + point[0]);
//                if (j == jMax) {
//                    break;
//                }
//                polygons.append(", ");
//            }
//            if (i == iMax) {
//                polygons.append("))");
//                break;
//            }
//            polygons.append(")), ");
//        }
//        return polygons.append(")')").toString();
//    }
}
