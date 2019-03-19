package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "documents",
        foreignKeys = {
                @ForeignKey(
                        entity = Form.class,
                        childColumns = "form_id",
                        parentColumns = "id",
                        onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "id"),
                @Index(value = "form_id"),
                @Index(value = "latitude"),
                @Index(value = "longitude"),
        }
)
@TypeConverters({DataTypeConverter.class})
public class Document implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long global_id;

    private long form_id;

    private int status;

    private long last_seen_date;

    private double latitude;

    private double longitude;

    @Ignore
    private List<DocumentSeries> document_series;

    public Document() {
    }

    public Document(long form_id, long global_id, status status, double latitude, double longitude) {
        this.form_id = form_id;
        this.global_id = global_id;
        this.status = status.getValue();
        this.latitude = latitude;
        this.longitude = longitude;
        this.last_seen_date = new Date().getTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getGlobal_id() {
        return global_id;
    }

    public void setGlobal_id(long global_id) {
        this.global_id = global_id;
    }

    public long getForm_id() {
        return form_id;
    }

    public void setForm_id(long form_id) {
        this.form_id = form_id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setStatus(status status) {
        this.status = status.getValue();
    }

    public long getLast_seen_date() {
        return last_seen_date;
    }

    public void setLast_seen_date(long last_seen_date) {
        this.last_seen_date = last_seen_date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public enum status {
        draft(0), pending(1), accept(2), reject(3);

        private int value;

        status(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    public List<DocumentSeries> getDocument_series() {
        return document_series;
    }

    public void setDocument_series(List<DocumentSeries> document_series) {
        this.document_series = document_series;
    }
}
