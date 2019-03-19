package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.GlobalSetting;

/**
 * Created by saeed on 2/17/18.
 */


@Dao
public interface GlobalSettingsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(GlobalSetting globalSetting);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(GlobalSetting... globalSettings);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(GlobalSetting globalSetting);

    @Query("delete from global_settings where `key` = :key")
    void remove(String key);

    @Query("delete from global_settings")
    void removeAll();

    @Query("select * from global_settings where `key` = :key")
    GlobalSetting get(String key);

    @Query("select * from global_settings")
    List<GlobalSetting> getAll();

}

