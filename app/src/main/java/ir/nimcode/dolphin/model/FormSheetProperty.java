package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "forms_sheets_properties",
        foreignKeys = {
                @ForeignKey(
                        entity = FormSheet.class,
                        childColumns = "form_sheet_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Property.class,
                        childColumns = "property_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "id"),
                @Index(value = "form_sheet_id"),
                @Index(value = "property_id")
        }
)
public class FormSheetProperty implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long form_sheet_id;

    private long property_id;

    private int position;

    private boolean visibility;

    public FormSheetProperty(long form_sheet_id, long property_id, int position, boolean visibility) {
        this.form_sheet_id = form_sheet_id;
        this.property_id = property_id;
        this.position = position;
        this.visibility = visibility;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getForm_sheet_id() {
        return form_sheet_id;
    }

    public void setForm_sheet_id(long form_sheet_id) {
        this.form_sheet_id = form_sheet_id;
    }

    public long getProperty_id() {
        return property_id;
    }

    public void setProperty_id(long property_id) {
        this.property_id = property_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

}
