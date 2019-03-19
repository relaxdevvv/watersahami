package ir.nimcode.dolphin.activity;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.marcouberti.autofitbutton.AutoFitButton;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.pedant.SweetAlert.SweetAlertDialog;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.database.AppDatabase;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.Form;
import ir.nimcode.dolphin.model.FormSheet;
import ir.nimcode.dolphin.model.FormSheetProperty;
import ir.nimcode.dolphin.model.ImagePropertyValues;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.PropertyValues;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.Utilities;

import static java.util.Calendar.HOUR;
import static java.util.Calendar.MONTH;

public class ReportActivity extends FullAppCompatActivity {

    @BindView(R.id.start_date)
    MaterialEditText startDate;
    @BindView(R.id.clear_start_date)
    ImageView clearStartDate;
    @BindView(R.id.finish_date)
    MaterialEditText finishDate;
    @BindView(R.id.clear_finish_date)
    ImageView clearFinishDate;
    @BindView(R.id.report)
    AutoFitButton report;
    private JalaliCalendar nowJalali;
    private Calendar now;
    private SweetAlertDialog progressDialog;
    private AppDatabase database;
    private Workbook workbook;
    private Map<Long, Integer> documentSeriesCount;
    private Sheet sheet;
    private Row rowHeader;
    private Row row;
    private Cell cHeader;
    private Cell cRow;
    private CellStyle cellHeaderStyle;
    private CellStyle cellStyle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.report)));

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
        nowJalali = new JalaliCalendar(now);

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
        report.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                progressDialog = new SweetAlertDialog(ReportActivity.this, SweetAlertDialog.PROGRESS_TYPE);
//                progressDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                progressDialog.getProgressHelper().setBarColor(ReportActivity.this.getResources().getColor(R.color.colorAccent));
                progressDialog.setTitleText(getString(R.string.saving));
                progressDialog.setCancelable(false);
                progressDialog.show();

                saveExcelFile(startDate.getText().toString(), finishDate.getText().toString());

//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loadPreDataFromJsonFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MyApplication.APP_DIR + "obs.json"),
//                                2,
//                                new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20});
//
//                        loadPreDataFromJsonFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), MyApplication.APP_DIR + "kami.json"),
//                                3,
//                                new long[]{1, 2, 3, 4, 5, 6, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28});
//
//                        loadPreDataFromJsonFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "kifi.json"),
//                                4,
//                                new long[]{1, 3, 2, 21, 4, 5, 6, 27, 10, 11, 9, 12, 13, 14, 14, 15, 16, 17, 18, 19, 20, 25, 26, 29});
//                        loadPreDataFromJsonFile(new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "kamiVakifi.json"),
//                                5,
//                                new long[]{1, 2, 3, 4, 5, 6, 27, 21, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 22, 23, 24, 25, 26, 28, 29});
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                progressDialog.setTitleText("");
//                                progressDialog.setContentText(getString(R.string.save_information_success));
//                                progressDialog.changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
//                                progressDialog.setConfirmText(getString(R.string.ok));
//                                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                    @Override
//                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                        progressDialog.dismiss();
//                                    }
//                                });
//                            }
//                        });
//                    }
//                }).start();
            }
        });
    }

    private void loadPreDataFromJsonFile(File file, int form_id, long[] ids) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext()) {
                stringBuilder.append(scanner.nextLine());
            }
            JSONArray jsonArray = new JSONArray(stringBuilder.toString());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                HashMap<Long, PropertyValues> valuesHashMap = new HashMap<>();

                // check date
                Date date;
                JalaliCalendar jalaliCalendar;
                String jalaliDateString = jsonObject.getString("" + 9);
                if (!jalaliDateString.isEmpty()) {
                    jalaliCalendar = new JalaliCalendar(jalaliDateString);
                    date = jalaliCalendar.getGregorianCalendar().getTime();
                } else {
                    date = new Date();
                    jalaliCalendar = nowJalali;
                }


                LatLng latLng = Utilities.UTMToLatLng(new Utilities.UTM(jsonObject.getInt("" + 5), jsonObject.getInt("" + 6), jsonObject.getInt("" + 4), 'N'));
                for (int j = 0; j < ids.length; j++) {
                    if (ids[j] == 9) {
                        String value = jalaliCalendar.dateToString();
                        valuesHashMap.put(ids[j], new PropertyValues(value, true));
                    } else {
                        String value = jsonObject.getString("" + ids[j]);
                        valuesHashMap.put(ids[j], new PropertyValues(value.isEmpty() ? null : value, !value.isEmpty() || ids[j] == 14 || ids[j] == 15));
                    }
                }

                long document_id = database.documentDAO().add(new Document(form_id, -1, Document.status.draft, latLng.latitude, latLng.longitude));
                database.documentSeriesDAO().add(new DocumentSeries(document_id, form_id, date.getTime(), false, valuesHashMap, 0));
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void saveExcelFile(final String startDate, final String finishDate) {


        final List<DocumentSeries> documentSeries;

        final String fileName;


        if (!startDate.isEmpty() || !finishDate.isEmpty()) {

            Date startDa = !startDate.isEmpty() ? new JalaliCalendar(startDate).getGregorianCalendar().getTime() : now.getTime();
            Date finishDa = !finishDate.isEmpty() ? new JalaliCalendar(finishDate).getGregorianCalendar().getTime() : now.getTime();
            finishDa.setHours(23);
            finishDa.setMinutes(59);
            finishDa.setSeconds(59);

            if (finishDate.isEmpty()) {
                documentSeries = database.documentSeriesDAO().getAllGreater(startDa.getTime());
                fileName = "Greater_" + startDate;
            } else if (startDate.isEmpty()) {
                documentSeries = database.documentSeriesDAO().getAllLesser(finishDa.getTime());
                fileName = "Lesser_" + finishDate;
            } else {
                documentSeries = database.documentSeriesDAO().getAll(startDa.getTime(), finishDa.getTime());
                fileName = startDate + "_" + finishDate;
            }
        } else {
            documentSeries = database.documentSeriesDAO().getAllNonZeroDate();
            fileName = "all_documents";
        }

        if (documentSeries.size() == 0) {
            showDialog(getString(R.string.error), getString(R.string.save_information_no_data_fail), SweetAlertDialog.ERROR_TYPE);
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {

                workbook = new HSSFWorkbook();

                cellHeaderStyle = workbook.createCellStyle();
                cellHeaderStyle.setBorderTop((short) 1);
                cellHeaderStyle.setBorderBottom((short) 1);
                cellHeaderStyle.setBorderLeft((short) 1);
                cellHeaderStyle.setBorderRight((short) 1);
//                cellHeaderStyle.setFillForegroundColor(IndexedColors.LIME.getIndex());
//                cellHeaderStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

                cellStyle = workbook.createCellStyle();
                cellStyle.setBorderTop((short) 1);
                cellStyle.setBorderBottom((short) 1);
                cellStyle.setBorderLeft((short) 1);
                cellStyle.setBorderRight((short) 1);
//                cellStyle.setFillPattern(CellStyle.ALIGN_CENTER);

                Set<Long> formIds = new HashSet<>();
                DocumentSeries documentSeriesValue = documentSeries.get(0);
                documentSeriesCount = new HashMap<>();
                formIds.add(database.documentDAO().get(documentSeriesValue.getDocument_id()).getForm_id());
                createSheetAndHeaderAndFirstRow(documentSeriesValue);
                for (int i = 1; i < documentSeries.size(); i++) {
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog.setContentText(String.format("%d %s %d", finalI, getString(R.string.from), documentSeries.size()));
                        }
                    });
                    documentSeriesValue = documentSeries.get(i);
                    long formId = database.documentDAO().get(documentSeriesValue.getDocument_id()).getForm_id();
                    if (!formIds.contains(formId)) {
                        formIds.add(formId);
                        createSheetAndHeaderAndFirstRow(documentSeriesValue);
                    } else {
                        createSheetRow(documentSeriesValue);
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "Reports/" + nowJalali.dateToString());
                if (!root.exists()) {
                    root.mkdirs();
                }
                File file = new File(root, fileName + "_" + System.currentTimeMillis() + ".xlsx");
                FileOutputStream os = null;

                try {
                    os = new FileOutputStream(file);
                    workbook.write(os);
                    showDialog("", getString(R.string.save_information_success), SweetAlertDialog.SUCCESS_TYPE);
                } catch (Exception e) {
                    showDialog(getString(R.string.error), getString(R.string.save_information_fail), SweetAlertDialog.ERROR_TYPE);
                    e.printStackTrace();
                } finally {
                    try {
                        if (null != os)
                            os.close();
                    } catch (IOException e) {
                        showDialog(getString(R.string.error), getString(R.string.save_information_fail), SweetAlertDialog.ERROR_TYPE);
                        e.printStackTrace();
                    }
                }
            }

        }).start();
    }

    public void showDialog(final String title, final String message, final int alertType) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                progressDialog.setTitleText(title);
                progressDialog.setContentText(message);
                progressDialog.changeAlertType(alertType);

                progressDialog.setConfirmText(getString(R.string.ok));
                progressDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }


    public void createSheetAndHeaderAndFirstRow(DocumentSeries documentSeries) {

        Map<Long, PropertyValues> valuesMap = documentSeries.getMapValues();

        if (documentSeriesCount.containsKey(documentSeries.getDocument_id())) {
            documentSeriesCount.put(documentSeries.getDocument_id(), documentSeriesCount.get(documentSeries.getDocument_id()) + 1);
        } else {
            documentSeriesCount.put(documentSeries.getDocument_id(), 1);
        }
        Document document = database.documentDAO().get(documentSeries.getDocument_id());
        Form form = database.formDAO().get(document.getForm_id());
        sheet = workbook.createSheet(database.formDAO().get(form.getParent_id()).getName_fa() + "_" + form.getName_fa());
        rowHeader = sheet.createRow(0);
        row = sheet.createRow(1);

        createHeaderCell(0, "شماره سند");
        createRowCell(0, documentSeries.getDocument_id() + "");


        createHeaderCell(1, "شماره سری");
        createRowCell(1, documentSeriesCount.get(documentSeries.getDocument_id()) + "");

        int j = 2;
        List<FormSheet> formSheetList = database.formSheetDAO().get(documentSeries.getForm_id());
        for (FormSheet formSheet : formSheetList) {
            for (FormSheetProperty formSheetProperty : database.formSheetPropertyDAO().get(formSheet.getId())) {
                Property property = database.propertyDAO().get(formSheetProperty.getProperty_id());

                createHeaderCell(j, property.getName_fa());

                String value = null;
                if (valuesMap != null && valuesMap.get(property.getId()) != null) {
                    value = valuesMap.get(property.getId()).getVal();
                }
                if (value == null) {
                    value = " ";
                }

                if (property.getType() == Property.type.image.getValue()) {

                    if (value.length() > 1) {
                        ImagePropertyValues values = new Gson().fromJson(value, ImagePropertyValues.class);

                        createRowCell(j, values.serverLink);

                        j++;
                        createHeaderCell(j, "مکان عکس برداری");
                        createRowCell(j, String.format("%s , %s", values.latitude + "", values.longitude + ""));

                        j++;
                        createHeaderCell(j, "فاصله تا منبع");
                        createRowCell(j, values.distanceFromSource + "");
                    } else {
                        createRowCell(j, value);

                        j++;
                        createHeaderCell(j, "مکان عکس برداری");
                        createRowCell(j, value);

                        j++;
                        createHeaderCell(j, "فاصله تا منبع");
                        createRowCell(j, value);
                    }

                } else {
                    createRowCell(j, value);
                }

                j++;
            }
        }

    }

    public void createSheetRow(DocumentSeries documentSeries) {

        Map<Long, PropertyValues> valuesMap = documentSeries.getMapValues();

        if (documentSeriesCount.containsKey(documentSeries.getDocument_id())) {
            documentSeriesCount.put(documentSeries.getDocument_id(), documentSeriesCount.get(documentSeries.getDocument_id()) + 1);
        } else {
            documentSeriesCount.put(documentSeries.getDocument_id(), 1);
        }

        row = sheet.createRow(row.getRowNum() + 1);

        createRowCell(0, documentSeries.getDocument_id() + "");

        createRowCell(1, documentSeriesCount.get(documentSeries.getDocument_id()) + "");

        int j = 2;
        List<FormSheet> formSheetList = database.formSheetDAO().get(documentSeries.getForm_id());
        for (FormSheet formSheet : formSheetList) {
            for (FormSheetProperty formSheetProperty : database.formSheetPropertyDAO().get(formSheet.getId())) {
                Property property = database.propertyDAO().get(formSheetProperty.getProperty_id());
                String value = null;
                if (valuesMap != null && valuesMap.get(property.getId()) != null) {
                    value = valuesMap.get(property.getId()).getVal();
                }
                if (value == null) {
                    value = " ";
                }

                if (property.getType() == Property.type.image.getValue()) {
                    if (value.length() > 1) {
                        ImagePropertyValues values = new Gson().fromJson(value, ImagePropertyValues.class);

                        createRowCell(j, values.serverLink);

                        j++;
                        createRowCell(j, String.format("%s , %s", values.latitude + "", values.longitude + ""));

                        j++;
                        createRowCell(j, values.distanceFromSource + "");
                    } else {
                        createRowCell(j, value);
                        j++;
                        createRowCell(j, value);
                        j++;
                        createRowCell(j, value);
                    }
                } else {
                    createRowCell(j, value);
                }
                j++;
            }
        }
    }

    public void createHeaderCell(int index, String value) {
        cHeader = rowHeader.createCell(index);
        cHeader.setCellStyle(cellHeaderStyle);
        cHeader.setCellValue(value);
    }

    public void createRowCell(int index, String value) {
        cRow = row.createCell(index);
        cRow.setCellStyle(cellStyle);
        cRow.setCellValue(value);
    }


}
