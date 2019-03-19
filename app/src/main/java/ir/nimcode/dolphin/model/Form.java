package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "forms", indices = {@Index(value = "id")})
public class Form implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long parent_id;

    private String name_en;

    private String name_fa;

    private String map_pin_symbol;

    private int position;

    private boolean visibility;

    public Form(String name_en, String name_fa, String map_pin_symbol, long parent_id, int position, boolean visibility) {
        this.name_en = name_en;
        this.name_fa = name_fa;
        this.map_pin_symbol = map_pin_symbol;
        this.position = position;
        this.parent_id = parent_id;
        this.visibility = visibility;
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

    public String getMap_pin_symbol() {
        return map_pin_symbol;
    }

    public void setMap_pin_symbol(String map_pin_symbol) {
        this.map_pin_symbol = map_pin_symbol;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public long getParent_id() {
        return parent_id;
    }

    public void setParent_id(long parent_id) {
        this.parent_id = parent_id;
    }

    public boolean isVisibility() {
        return visibility;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }
}
