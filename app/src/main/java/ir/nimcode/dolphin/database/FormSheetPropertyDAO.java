package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.FormSheetProperty;

/**
 * Created by saeed on 2/17/18.
 */


@Dao
public interface FormSheetPropertyDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(FormSheetProperty formSheetProperty);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(FormSheetProperty formSheetProperty);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(FormSheetProperty... formSheetProperties);

    @Query("delete from forms_sheets_properties where id = :formSheetPropertyId")
    void remove(long formSheetPropertyId);

    @Query("delete from forms_sheets_properties where form_sheet_id = :formSheetId and property_id = :propertyId")
    void remove(long formSheetId, long propertyId);

    @Query("delete from forms_sheets_properties")
    void removeAll();

    @Query("select * from forms_sheets_properties where id = :formSheetPropertyId and visibility=1")
    FormSheetProperty getById(long formSheetPropertyId);

    @Query("select * from forms_sheets_properties where form_sheet_id = :formSheetId and visibility=1 order by position")
    List<FormSheetProperty> get(long formSheetId);

    @Query("select * from forms_sheets_properties where  visibility=1 order by position")
    List<FormSheetProperty> getAll();

    @Query("select * from forms_sheets_properties where form_sheet_id = :formSheetId and visibility=1 and property_id in (:searchablePropertiesId) order by position")
    List<FormSheetProperty> getAllSearchable(long formSheetId, List<Long> searchablePropertiesId);

}