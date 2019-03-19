package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.Sheet;

/**
 * Created by saeed on 2/17/18.
 */


@Dao
public interface SheetsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(Sheet sheet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(Sheet... sheets);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Sheet sheet);

    @Query("delete from sheets where id = :sheetId and visibility=1")
    void remove(long sheetId);

    @Query("delete from sheets")
    void removeAll();

    @Query("select * from sheets where id = :sheetId and visibility=1")
    Sheet get(long sheetId);

    @Query("select * from sheets where visibility=1")
    List<Sheet> getAll();

}

