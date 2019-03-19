package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.FormSheet;

/**
 * Created by saeed on 2/17/18.
 */

@Dao
public interface FormsSheetsDAO {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(FormSheet formSheet);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FormSheet formSheet);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(FormSheet... formSheets);

    @Query("delete from forms_sheets where id = :formSheetId" )
    void remove(long formSheetId);

    @Query("delete from forms_sheets where form_id = :formId and sheet_id = :sheetId")
    void remove(long formId, long sheetId);

    @Query("delete from forms_sheets")
    void removeAll();

    @Query("select * from forms_sheets where id = :formSheetId and visibility=1")
    FormSheet getById(long formSheetId);

    @Query("select * from forms_sheets where form_id = :formId and visibility=1 order by position asc")
    List<FormSheet> get(long formId);

    @Query("select * from forms_sheets where visibility=1 order by position asc")
    List<FormSheet> getAll();

}