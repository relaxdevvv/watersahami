package ir.nimcode.dolphin.activity;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.ldoublem.loadingviewlib.view.LVNews;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Form;
import ir.nimcode.dolphin.model.FormSheet;
import ir.nimcode.dolphin.model.FormSheetProperty;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.util.FullAppCompatActivity;
import ir.nimcode.dolphin.util.Utilities;

public class SearchActivity extends FullAppCompatActivity {

    public static final int SEARCH_RESULT_REQUEST_CODE = 3001;
    @BindView(R.id.root_view)
    RelativeLayout rootView;
    @BindView(R.id.progress)
    LVNews progress;
    @BindView(R.id.search_component_layout)
    LinearLayout searchComponentLayout;
    @BindView(R.id.form_type)
    MaterialBetterSpinner formType;
    @BindView(R.id.search)
    Button search;
    private ArrayAdapter<String> adapterFormType;
    private Typeface font;
    private List<Form> forms;
    private Map<Long, EditText> searchableComponents;
    private Map<Integer, List<Property>> formSearchableComponents;
    private int selectedFromIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        Utilities.setupCustomActivityToolbarWithBack(Utilities.setToolbar(this, getString(R.string.search)));

        font = Typeface.createFromAsset(getAssets(), getString(R.string.fontNormal));

        progress.setViewColor(getResources().getColor(R.color.md_grey_800));

        adapterFormType = new ArrayAdapter<>(this, R.layout.adapter_row_spinner, new String[0]);
        formType.setAdapter(adapterFormType);

        loadSearchableComponent();

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                List<Property> properties = formSearchableComponents.get(selectedFromIndex);
                StringBuilder sqlQuery = new StringBuilder("SELECT * FROM documents_series WHERE ");
                if (selectedFromIndex != 0) {
                    sqlQuery
                            .append("form_id = ")
                            .append(forms.get(selectedFromIndex - 1).getId())
                            .append(" AND ");
                }
                for (Property property : properties) {
                    String value = searchableComponents.get(property.getId()).getText().toString().trim();

                    if (!value.isEmpty()) {
                        sqlQuery
                                .append("(( `mapValues` GLOB '*\"")
                                .append(property.getId())
                                .append("\":{\"chk\":?,\"val\":\"")
                                .append(value)
                                .append("*\"}*' ) OR ( `mapValues` GLOB '*\"")
                                .append(property.getId())
                                .append("\":{\"val\":\"")
                                .append(value)
                                .append("*\",\"chk\":?}*' )");

//                    if (value.isEmpty()) {
//                        sqlQuery
//                                .append(" OR `values` GLOB '*\"")
//                                .append(property.getId())
//                                .append("\":{\"chk\":?}*' ");
//                    }
                        sqlQuery.append(") AND ");
                    }

                }
                sqlQuery.append("1 LIMIT 0,50 ;");
                SupportSQLiteQuery query = new SimpleSQLiteQuery(sqlQuery.toString());
                Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                intent.putExtra("query", query.getSql());
                startActivityForResult(intent, SEARCH_RESULT_REQUEST_CODE);
                overridePendingTransition(R.anim.right_in, R.anim.right_out);
            }
        });
    }

    public void loadSearchableComponent() {

        progress.setVisibility(View.VISIBLE);
        progress.startAnim(1000);

        formSearchableComponents = new HashMap<>();
        searchableComponents = new HashMap<>();
        List<Long> searchablePropertiesId = MyApplication.database.propertyDAO().getAllSearchable();
        List<String> formsNamesList = new ArrayList<>();
        List<Property> CommonSearchableProperties = new ArrayList<>();
        formsNamesList.add("همه");
        formType.setText("همه");
        selectedFromIndex = 0;
        long filterFormId = MyApplication.sp.getFilterFormId();
        if (filterFormId != 0) {
            Form form = MyApplication.database.formDAO().get(filterFormId);
            forms = MyApplication.database.formDAO().getAll(form.getId());
        } else {
            forms = MyApplication.database.formDAO().getAllSubForms();
        }
        for (int i = 0; i < forms.size(); i++) {
            Form form = forms.get(i);
            if (filterFormId != 0) {
                formsNamesList.add(form.getName_fa());
            } else if (form.getParent_id() != 0L) {
                Log.d("TEST", "loadSearchableComponent: " + form.getParent_id());
                Form parentForm = MyApplication.database.formDAO().get(form.getParent_id());
                if (parentForm != null) {
                    formsNamesList.add(parentForm.getName_fa() + " > " + form.getName_fa());
                } else {
                    formsNamesList.add(form.getName_fa());
                }
            }
            List<Property> formSearchableProperties = new ArrayList<>();
            List<FormSheet> formSheets = MyApplication.database.formSheetDAO().get(form.getId());
            for (FormSheet formSheet : formSheets) {
                List<FormSheetProperty> formSheetProperties = MyApplication.database.formSheetPropertyDAO().getAllSearchable(formSheet.getId(), searchablePropertiesId);
                for (FormSheetProperty formSheetProperty : formSheetProperties) {
                    Property property = MyApplication.database.propertyDAO().get(formSheetProperty.getProperty_id());
                    if (!formSearchableProperties.contains(property)) {
                        formSearchableProperties.add(property);
                    }
                }
            }
            if (CommonSearchableProperties.isEmpty()) {
                CommonSearchableProperties.addAll(formSearchableProperties);
            } else {
                CommonSearchableProperties.retainAll(formSearchableProperties);
            }
            formSearchableComponents.put(i + 1, formSearchableProperties);
        }
        formSearchableComponents.put(0, CommonSearchableProperties);
        adapterFormType = new ArrayAdapter<>(this, R.layout.adapter_row_spinner, formsNamesList);
        formType.setAdapter(adapterFormType);
        formType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedFromIndex = position;
                rootView.setVisibility(View.INVISIBLE);
                searchComponentLayout.removeAllViews();
                searchableComponents.clear();
                progress.setVisibility(View.VISIBLE);
                progress.startAnim(1000);
                for (Property property : formSearchableComponents.get(position)) {
                    if (property.getType() == Property.type.spinner.getValue()) {
                        addNewSpinner(property);
                    } else {
                        addNewEditText(property);
                    }
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        rootView.setVisibility(View.VISIBLE);
                        progress.stopAnim();
                        progress.setVisibility(View.INVISIBLE);
                    }
                }, 1000);
            }
        });
        for (Property property : formSearchableComponents.get(0)) {
            if (property.getType() == Property.type.spinner.getValue()) {
                addNewSpinner(property);
            } else {
                addNewEditText(property);
            }
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rootView.setVisibility(View.VISIBLE);
                progress.stopAnim();
                progress.setVisibility(View.INVISIBLE);
            }
        }, 1000);
    }

    private void addNewEditText(Property property) {
        MaterialEditText editText = new MaterialEditText(SearchActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        editText.setLayoutParams(params);
        if (property.getType() == Property.type.number_float.getValue()) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (property.getType() == Property.type.number_int.getValue()) {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        }
        editText.setAccentTypeface(font);
        editText.setTypeface(font);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        editText.setFloatingLabelText(property.getName_fa());
        editText.setFloatingLabelTextSize(Utilities.convertDpToPixel(10, SearchActivity.this));
        editText.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
        editText.setPrimaryColor(getResources().getColor(R.color.accent));
        editText.setMetHintTextColor(getResources().getColor(R.color.md_grey_600));
        editText.setSingleLineEllipsis(true);
        editText.setEms(10);
        editText.setMaxLines(1);
        editText.setHint(property.getName_fa());
        int margin = Utilities.convertDpToPixel(10, SearchActivity.this);
        params.setMargins(margin, margin, margin, margin * -1);
        searchComponentLayout.addView(editText, searchComponentLayout.getChildCount());
        searchableComponents.put(property.getId(), editText);
    }

    private void addNewSpinner(Property property) {
        MaterialBetterSpinner spinner = new MaterialBetterSpinner(SearchActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinner.setLayoutParams(params);
        spinner.setAccentTypeface(font);
        spinner.setTypeface(font);
        spinner.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        spinner.setFloatingLabelText(property.getName_fa());
        spinner.setFloatingLabelTextSize(Utilities.convertDpToPixel(10, SearchActivity.this));
        spinner.setFloatingLabel(MaterialEditText.FLOATING_LABEL_HIGHLIGHT);
        spinner.setPrimaryColor(getResources().getColor(R.color.accent));
        spinner.setMetHintTextColor(getResources().getColor(R.color.md_grey_600));
        spinner.setSingleLineEllipsis(true);
        spinner.setEms(10);
        spinner.setMaxLines(1);
        spinner.setHint(property.getName_fa());
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.adapter_row_spinner, property.getSpinner_value().split("-"));
        spinner.setAdapter(adapter);
        int margin = Utilities.convertDpToPixel(10, SearchActivity.this);
        params.setMargins(margin, margin, margin, margin * -1);
        searchComponentLayout.addView(spinner, searchComponentLayout.getChildCount());
        searchableComponents.put(property.getId(), spinner);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_RESULT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Intent intent = getIntent();
                setResult(RESULT_OK, intent);
                intent.putExtra("latitude", data.getDoubleExtra("latitude", 35.754969));
                intent.putExtra("longitude", data.getDoubleExtra("longitude", 51.420301));
                finish();
            }
        }
    }
}
