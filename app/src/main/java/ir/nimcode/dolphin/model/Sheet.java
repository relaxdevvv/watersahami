package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import static ir.nimcode.dolphin.model.Sheet.type.camera;
import static ir.nimcode.dolphin.model.Sheet.type.location;
import static ir.nimcode.dolphin.model.Sheet.type.property;
import static ir.nimcode.dolphin.model.Sheet.type.user;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "sheets", indices = {@Index(value = "id")})
public class Sheet {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name_en;

    private String name_fa;

    private int type;

    private boolean visibility;

    @Ignore
    private int properties_error_count;
    @Ignore
    private int properties_count;
    @Ignore
    private int properties_checked_count;

    public Sheet() {
    }

    public Sheet(String name_en, String name_fa, type type, boolean visibility) {
        this.name_en = name_en;
        this.name_fa = name_fa;
        this.type = type.getValue();
        this.visibility = visibility;
    }

    public Sheet(long id, String name_en, String name_fa, int properties_count, int properties_checked_count) {
        this.id = id;
        this.name_en = name_en;
        this.name_fa = name_fa;
        this.properties_count = properties_count;
        this.properties_checked_count = properties_checked_count;
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

    public void setType(Property.type type) {
        this.type = type.getValue();
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public int getProperties_error_count() {
        return properties_error_count;
    }

    public void setProperties_error_count(int properties_error_count) {
        this.properties_error_count = properties_error_count;
    }

    public int getProperties_count() {
        return properties_count;
    }

    public void setProperties_count(int properties_count) {
        this.properties_count = properties_count;
    }

    public int getProperties_checked_count() {
        return properties_checked_count;
    }

    public void setProperties_checked_count(int properties_checked_count) {
        this.properties_checked_count = properties_checked_count;
    }

    public enum type {
        property(1), location(2), camera(3), user(4);

        private int value;

        type(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}
