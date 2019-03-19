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
@Entity(tableName = "forms_sheets",
        foreignKeys = {
                @ForeignKey(
                        entity = Form.class,
                        childColumns = "form_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Sheet.class,
                        childColumns = "sheet_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE),

        },
        indices = {
                @Index(value = "id"),
                @Index(value = "form_id"),
                @Index(value = "sheet_id")
        }
)
public class FormSheet implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long form_id;

    private long sheet_id;

    private int position;

    private boolean visibility;


    public FormSheet(long form_id, long sheet_id, int position, boolean visibility) {
        this.form_id = form_id;
        this.sheet_id = sheet_id;
        this.position = position;
        this.visibility = visibility;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getForm_id() {
        return form_id;
    }

    public void setForm_id(long form_id) {
        this.form_id = form_id;
    }

    public long getSheet_id() {
        return sheet_id;
    }

    public void setSheet_id(long sheet_id) {
        this.sheet_id = sheet_id;
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
