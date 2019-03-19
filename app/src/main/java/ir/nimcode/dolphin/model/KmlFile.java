package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by saeed on 3/8/18.
 */

@Entity(tableName = "kml_files", indices = {@Index(value = "id")})
public class KmlFile {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;

    private boolean visibility;

    private String url;

    public KmlFile(String name, boolean visibility, String url) {
        this.name = name;
        this.visibility = visibility;
        this.url = url;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
