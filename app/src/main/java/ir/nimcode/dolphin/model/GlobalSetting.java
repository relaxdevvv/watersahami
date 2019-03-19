package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by saeed on 5/14/18.
 */

@Entity(tableName = "global_settings",
        indices = {
                @Index(value = "id"),
                @Index(value = "key"),
        }
)
public class GlobalSetting implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String key;

    private String value;

    public GlobalSetting(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
