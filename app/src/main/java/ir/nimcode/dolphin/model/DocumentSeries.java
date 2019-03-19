package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by saeed on 3/13/18.
 */

@Entity(tableName = "documents_series",
        foreignKeys = {
                @ForeignKey(
                        entity = Document.class,
                        childColumns = "document_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(
                        entity = Form.class,
                        childColumns = "form_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "id"),
                @Index(value = "document_id"),
                @Index(value = "form_id"),
                @Index(value = "date"),
        }
)
@TypeConverters({DataTypeConverter.class})
public class DocumentSeries implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long document_id;

    private long form_id;

    private int position;

    private long date;

    private boolean isUpload;

    private Map<Long, PropertyValues> mapValues;

    @Ignore
    private List<PropertyValues> values;

    public DocumentSeries(long document_id, long form_id, long date, boolean isUpload, Map<Long, PropertyValues> mapValues, int position) {
        this.document_id = document_id;
        this.form_id = form_id;
        this.date = date;
        this.mapValues = mapValues;
        this.isUpload = isUpload;
        this.position = position;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDocument_id() {
        return document_id;
    }

    public void setDocument_id(long document_id) {
        this.document_id = document_id;
    }

    public long getForm_id() {
        return form_id;
    }

    public void setForm_id(long form_id) {
        this.form_id = form_id;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isUpload() {
        return isUpload;
    }

    public void setUpload(boolean upload) {
        isUpload = upload;
    }

    public Map<Long, PropertyValues> getMapValues() {
        return mapValues;
    }

    public void setMapValues(Map<Long, PropertyValues> mapValues) {
        this.mapValues = mapValues;
    }

    public List<PropertyValues> getValues() {
        return values;
    }

    public void setValues(List<PropertyValues> values) {
        this.values = values;
    }
}
