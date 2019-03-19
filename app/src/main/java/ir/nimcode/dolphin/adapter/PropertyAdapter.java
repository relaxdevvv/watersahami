package ir.nimcode.dolphin.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.google.android.gms.maps.model.LatLng;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SheetActivity;
import ir.nimcode.dolphin.application.MyApplication;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.Sheet;
import ir.nimcode.dolphin.model.StudyArea;
import ir.nimcode.dolphin.model.User;
import ir.nimcode.dolphin.util.JalaliCalendar;
import ir.nimcode.dolphin.util.Utilities;

import static java.util.Calendar.HOUR;

/**
 * Created by saeed on 11/26/17.
 */

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {


    private Context context;
    private List<Property> list;
    private int sheetPosition;

    public PropertyAdapter(Context context, List<Property> list, int sheetPosition) {
        this.context = context;
        this.list = list;
        this.sheetPosition = sheetPosition;
    }

    @Override
    public PropertyAdapter.PropertyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PropertyViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_row_property, parent, false));

    }


    @Override
    public void onBindViewHolder(final PropertyAdapter.PropertyViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final Property property = list.get(position);
        Property rel1 = null;
        Property rel2 = null;
        final String relationTag = property.getRelationTag();
        if (relationTag != null && !relationTag.isEmpty()) {
            String[] relsId = property.getRelation().split(",");
            rel1 = SheetActivity.allProperties.get(Long.parseLong(relsId[0]));
            if (relsId.length > 1) {
                rel2 = SheetActivity.allProperties.get(Long.parseLong(relsId[1]));
            }
        }
        final Property finalRel1 = rel1;
        final Property finalRel2 = rel2;

        property.setSheetPosition(sheetPosition - 1);
        final Sheet sheet = SheetActivity.sheets.get(property.getSheetPosition());

        TextWatcher watcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (finalRel1 != null && !(relationTag.equals(">") || relationTag.equals("<"))) {
                    if (holder.editText.getTag() == null || !holder.editText.getTag().equals("relation")) {
                        boolean flag = false;
                        if (s.toString().length() > 0) {
                            flag = true;
                        }
                        s.clear();
                        if (flag) {
                            holder.editText.setError("این یک داده محاسباتی است و شما قادر به تغییر آن نیستید");
                        }
                    } else if (property.getValue() != null && !s.toString().equals(property.getValue())) {
                        holder.editText.setText(property.getValue());
                        holder.editText.setError("این یک داده محاسباتی است و شما قادر به تغییر آن نیستید");
                    }
                } else {
                    if (property.isHasError()) {
                        holder.editText.setError(null);
                    }
                    if (property.getType() == Property.type.date.getValue() && property.getValue() != null && property.getValue().contains("-")) {
                        String[] dates = s.toString().split("-");
                        JalaliCalendar jalaliCalendar = new JalaliCalendar(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]), Integer.parseInt(dates[2]));
                        property.setValue(jalaliCalendar.getGregorianCalendar().getTimeInMillis() + "");
                    } else {
                        property.setValue(s.toString());
                    }
                }
                if (property.isHasError()) {
                    property.setHasError(false);
                    sheet.setProperties_error_count(sheet.getProperties_error_count() - 1);
                }
            }
        };

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.editText.setOnClickListener(null);
        holder.editText.setOnFocusChangeListener(null);
        holder.editText.removeTextChangedListener(watcher);
        holder.spinner.removeTextChangedListener(watcher);

        holder.spinner.setHint(property.getName_fa());
        holder.editText.setHint(property.getName_fa());
        holder.editText.setFloatingLabelText(property.getName_fa());
        holder.spinner.setFloatingLabelText(property.getName_fa());

        holder.spinner.setVisibility(View.GONE);
        holder.editText.setVisibility(View.GONE);

        holder.editText.setTag(null);
        if (property.getType() == Property.type.date.getValue() && property.getValue() != null && !property.getValue().isEmpty() && !property.getValue().contains("-")) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(Long.parseLong(property.getValue()));
            holder.editText.setText(new JalaliCalendar(cal).dateToString());
        } else {
            holder.editText.setText(property.getValue());
        }

        holder.spinner.setText(property.getValue());

        holder.checkBox.setEnabled(property.isEnabled());
        holder.editText.setEnabled(!property.isChecked() && property.isEnabled());
        holder.spinner.setEnabled(!property.isChecked() && property.isEnabled());

        holder.checkBox.setChecked(property.isChecked());

        if (property.getType() == Property.type.spinner.getValue()) {

            String[] list = property.getSpinner_value().split("-");
            holder.spinner.setAdapter(new ArrayAdapter<>(context, R.layout.adapter_row_spinner, list));

            holder.spinner.setVisibility(View.VISIBLE);
            holder.spinner.addTextChangedListener(watcher);
        } else {
            if (property.getType() == Property.type.date.getValue()) {

                if (property.isEnabled()) {
                    holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                holder.editText.callOnClick();
                            }
                        }
                    });
                    JalaliCalendar jalaliCalendar = new JalaliCalendar(Calendar.getInstance());
                    final PersianCalendar calendarPersianCalendar = new PersianCalendar(jalaliCalendar.getGregorianCalendar().getTime().getTime());
                    holder.editText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(holder.editText.getWindowToken(), 0);
                            holder.editText.setInputType(InputType.TYPE_NULL);

                            PersianCalendar persianCalendar;
                            if (!holder.editText.getText().toString().isEmpty()) {
                                persianCalendar = new PersianCalendar(new JalaliCalendar(holder.editText.getText().toString()).getGregorianCalendar().getTime().getTime());
                                persianCalendar.add(HOUR, 24);
                            } else {
                                persianCalendar = calendarPersianCalendar;
                            }
                            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                                    new DatePickerDialog.OnDateSetListener() {
                                        @Override
                                        public void onDateSet(DatePickerDialog view, final int year, final int monthOfYear, final int dayOfMonth) {
                                            holder.editText.setText(String.format(new Locale("en"), "%d-%02d-%02d", year, monthOfYear + 1, dayOfMonth));
                                        }
                                    },
                                    persianCalendar.getPersianYear(),
                                    persianCalendar.getPersianMonth(),
                                    persianCalendar.getPersianDay()
                            );
                            datePickerDialog.show(((Activity) context).getFragmentManager(), "DatePicker");
                        }
                    });

                }
            } else {

                holder.editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {

                        //  check relations
                        if (finalRel1 != null) {
                            if (relationTag.equals(">") || relationTag.equals("<")) {
                                if (!hasFocus) {
                                    checkComparableRelations(holder.editText, property, finalRel1, true, sheet);
                                }
                            } else {
                                holder.editText.setError(null);
                                if (hasFocus) {
                                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(holder.editText.getWindowToken(), 0);
                                    holder.editText.setInputType(InputType.TYPE_NULL);
                                    checkComputableRelations(holder.editText, property, finalRel1, finalRel2, !property.isHasError(), sheet);
                                }
                            }
                        }

                        if (!hasFocus) {

                            // max , min
                            if (property.getMin() != 0 || property.getMax() != 0) {
                                int min = Integer.MIN_VALUE;
                                int max = Integer.MAX_VALUE;
                                if (property.getMin() != 0) {
                                    min = property.getMin();
                                }
                                if (property.getMax() != 0) {
                                    max = property.getMax();
                                }
                                if (property.getValue() != null && !property.getValue().isEmpty() && (property.getType() == Property.type.number_int.getValue() || property.getType() == Property.type.number_float.getValue())) {
                                    float val = Float.parseFloat(property.getValue());
                                    if (val < min || val > max) {
                                        holder.editText.setError(String.format("مقدار این داده باید بین %d و %d باشد ", min, max));
                                        if (!property.isHasError()) {
                                            property.setHasError(true);
                                            sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                                        }
                                    }
                                }
                            }

                        }
                    }
                });

                if (property.getType() == Property.type.number_int.getValue()) {
                    holder.editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (property.getType() == Property.type.number_float.getValue()) {
                    holder.editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                } else if (property.getType() == Property.type.text.getValue()) {
                    holder.editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
                    holder.editText.setHorizontallyScrolling(false);
                    holder.editText.setMaxLines(10);
                }
            }

            if (property.getTag() != null) {
                if (property.getTag().equals("today")) {
                    JalaliCalendar jalaliCalendar = new JalaliCalendar(Calendar.getInstance());
                    holder.editText.setText(jalaliCalendar.dateToString());
                    property.setValue(jalaliCalendar.getGregorianCalendar().getTimeInMillis()+"");
                } else if (property.getTag().startsWith("user")) {
                    User user = MyApplication.sp.getUser();
                    if (property.getTag().equals("user_full_name")) {
                        holder.editText.setText(user.fullname);
                        property.setValue(user.fullname);
                    } else if (property.getTag().equals("user_supervisor")) {
                        holder.editText.setText(user.supervisor.fullname);
                        property.setValue(user.supervisor.id + "");
                    }
                }
            }

            holder.editText.setVisibility(View.VISIBLE);
            holder.editText.addTextChangedListener(watcher);
        }


        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (finalRel1 != null) {
                    if (relationTag.equals(">") || relationTag.equals("<")) {
                        checkComparableRelations(holder.editText, property, finalRel1, !property.isHasError(), sheet);
                    }
                }

                if (property.isHasError()) {
                    buttonView.setChecked(false);
                    holder.editText.setEnabled(true);
                    return;
                }
                sheet.setProperties_checked_count(isChecked ? sheet.getProperties_checked_count() + 1 : sheet.getProperties_checked_count() - 1);
                holder.spinner.setEnabled(!isChecked);
                holder.editText.setEnabled(!isChecked);
                property.setChecked(isChecked);
                if (property.getValue() != null && !property.getValue().isEmpty() && isChecked && property.getTag() != null && property.getTag().startsWith("utm")) {

                    Utilities.UTM utm = new Utilities.UTM(0, 0, 0, 'N');

                    if (property.getTag().equals("utm_zone")) {
                        utm.setZone(Integer.parseInt(property.getValue()));
                    } else if (property.getTag().equals("utm_x")) {
                        utm.setX(Double.parseDouble(property.getValue()));
                    } else if (property.getTag().equals("utm_y")) {
                        utm.setY(Double.parseDouble(property.getValue()));
                    }

                    if (utm.getX() != 0 && utm.getY() != 0 && utm.getZone() != 0) {
                        LatLng latLng = Utilities.UTMToLatLng(utm);
                        StudyArea studyArea = MyApplication.databaseStudyArea.getStudyAreaFromPoint("GeomFromText('POINT(" + latLng.latitude + " " + latLng.longitude + ")')");
                        if (studyArea != null) {
                            Property studyAreaCode = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("study_area_code").getId());
                            Property studyAreaName = SheetActivity.allProperties.get(MyApplication.database.propertyDAO().getByTag("study_area_name").getId());
                            if (studyAreaCode.getValue() == null || !studyAreaCode.getValue().equals(studyArea.getCode() + "")) {
                                studyAreaCode.setValue(studyArea.getCode() + "");
                                studyAreaName.setValue(studyArea.getName());
                                notifyDataSetChanged();
                            }
                        }
                    }
                }
            }
        });


        // check relation
        if (finalRel1 != null) {
            if (relationTag.equals(">") || relationTag.equals("<")) {
                checkComparableRelations(holder.editText, property, finalRel1, !property.isHasError(), sheet);
            } else {
                checkComputableRelations(holder.editText, property, finalRel1, finalRel2, !property.isHasError(), sheet);
            }
        }

        // max , min
        if (property.getMin() != 0 || property.getMax() != 0) {
            int min = Integer.MIN_VALUE;
            int max = Integer.MAX_VALUE;
            if (property.getMin() != 0) {
                min = property.getMin();
            }
            if (property.getMax() != 0) {
                max = property.getMax();
            }
            Log.d("TEST", "onBindViewHolder: " + max);
            Log.d("TEST", "onBindViewHolder: " + min);
            if (property.getValue() != null && !property.getValue().isEmpty() && (property.getType() == Property.type.number_int.getValue() || property.getType() == Property.type.number_float.getValue())) {
                float val = Float.parseFloat(property.getValue());
                if (val < min || val > max) {
                    holder.editText.setError(String.format("مقدار این داده باید بین %d تا %d باشد ", min, max));
                    if (!property.isHasError()) {
                        property.setHasError(true);
                        sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                    }
                }

            }
        }

        // length
        if (property.getLength() != 0) {
            holder.editText.setMaxCharacters(property.getLength());
        }

        if (!property.isVisibility()) {
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));
        }

    }

    private void checkComparableRelations(MaterialEditText editText, Property property, Property rel, boolean count, Sheet sheet) {
        if (!editText.getText().toString().isEmpty()) {
            double val = Double.parseDouble(property.getValue());
            if (rel.getValue() != null && !rel.getValue().isEmpty()) {
                double val2 = Double.parseDouble(rel.getValue());
                if (property.getRelationTag().equals(">") && val < val2) {
                    editText.setError("این داده باید از  " + rel.getName_fa() + " بزرگتر باشد");
                    property.setHasError(true);
                    if (count) {
                        sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                    }
                } else if (property.getRelationTag().equals("<") && val > val2) {
                    editText.setError("این داده باید از  " + rel.getName_fa() + " کوچکتر باشد");
                    property.setHasError(true);
                    if (count) {
                        sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                    }
                }
            }
        }
    }

    private void checkComputableRelations(MaterialEditText editText, Property property, Property rel1, Property rel2, boolean count, Sheet sheet) {
        boolean flag = false;
        if (rel1 == null) {
            Log.d("TEST", "onBindViewHolderG: rel1 is null ");
            flag = true;
        }
        if (rel2 == null) {
            Log.d("TEST", "onBindViewHolderG: rel2 is null ");
            flag = true;
        }
        if (flag) {
            return;
        }
        if (rel1.getValue() != null && !rel1.getValue().isEmpty() && rel2.getValue() != null && !rel2.getValue().isEmpty()) {
            double val1 = Double.parseDouble(rel1.getValue());
            double val2 = Double.parseDouble(rel2.getValue());
            editText.setTag("relation");
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
                    Log.d("TEST", "checkComputableRelations: " + val2);
                    if (val2 == 0) {
                        editText.setError("بدلیل محسابه تقسیم ، مقدار داده ی " + rel2.getName_fa() + " نباید صفر باشد");
                        if (count) {
                            sheet.setProperties_error_count(sheet.getProperties_error_count() + 1);
                        }
                    } else {
                        property.setValue((val1 / val2) + "");
                    }
                    break;
                }
            }
            if (property.getValue() != null) {
                editText.setText(property.getValue());
            }
        }

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public class PropertyViewHolder extends RecyclerView.ViewHolder {

        private MaterialEditText editText;
        private MaterialBetterSpinner spinner;
        private CheckBox checkBox;

        public PropertyViewHolder(View itemView) {
            super(itemView);

            this.editText = itemView.findViewById(R.id.edit_text);
            this.checkBox = itemView.findViewById(R.id.check_box);
            this.spinner = itemView.findViewById(R.id.spinner);
            this.spinner.setAdapter(new ArrayAdapter<>(itemView.getContext(), R.layout.adapter_row_spinner, new String[0]));
        }
    }
}
