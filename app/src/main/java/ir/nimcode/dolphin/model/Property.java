package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

import static ir.nimcode.dolphin.model.Property.type.date;
import static ir.nimcode.dolphin.model.Property.type.image;
import static ir.nimcode.dolphin.model.Property.type.number_float;
import static ir.nimcode.dolphin.model.Property.type.number_int;
import static ir.nimcode.dolphin.model.Property.type.spinner;
import static ir.nimcode.dolphin.model.Property.type.text;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "properties", indices = {@Index(value = "id")})
public class Property implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;
    private String name_en;
    private String name_fa;
    private int type;
    private String tag;
    private String relationTag;
    private String relation;
    private String value;
    private String spinner_value;
    @Ignore
    private boolean isHasError;
    @Ignore
    private int sheetPosition;
    private int max;
    private int min;
    private int length;
    private boolean isChecked;
    private boolean isEnabled;
    private boolean visibility;
    private boolean isSearchable;

    public Property() {
    }

    public Property(String name_en, String name_fa, type type, String tag, String relationTag, String relation, String value, boolean isChecked, String spinner_value, int max, int min, int length, boolean isEnabled, boolean visibility, boolean isSearchable) {
        this.name_en = name_en;
        this.name_fa = name_fa;
        this.type = type.getValue();
        this.tag = tag;
        this.relationTag = relationTag;
        this.relation = relation;
        this.value = value;
        this.spinner_value = spinner_value;
        this.max = max;
        this.min = min;
        this.length = length;
        this.isChecked = isChecked;
        this.isEnabled = isEnabled;
        this.visibility = visibility;
        this.isSearchable = isSearchable;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
    }

    public String getName_fa() {
        return name_fa;
    }

    public void setName_fa(String name_fa) {
        this.name_fa = name_fa;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setType(type type) {
        this.type = type.getValue();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getRelationTag() {
        return relationTag;
    }

    public void setRelationTag(String relationTag) {
        this.relationTag = relationTag;
    }

    public String getRelation() {
        return relation;
    }

    public void setRelation(String relation) {
        this.relation = relation;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getSpinner_value() {
        return spinner_value;
    }

    public void setSpinner_value(String spinner_value) {
        this.spinner_value = spinner_value;
    }

    public boolean isHasError() {
        return isHasError;
    }

    public void setHasError(boolean hasError) {
        isHasError = hasError;
    }

    public int getSheetPosition() {
        return sheetPosition;
    }

    public void setSheetPosition(int sheetPosition) {
        this.sheetPosition = sheetPosition;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public boolean isSearchable() {
        return isSearchable;
    }

    public void setSearchable(boolean searchable) {
        isSearchable = searchable;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean equals(Object obj) {
        Property p = (Property) obj;
        return this.id == p.id;
    }

    public enum type {
        text(0), number_int(1), number_float(2), spinner(3), date(4), image(5);

        private int value;

        type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
