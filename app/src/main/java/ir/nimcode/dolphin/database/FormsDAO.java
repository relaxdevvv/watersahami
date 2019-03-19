package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.Form;

/**
 * Created by saeed on 2/17/18.
 */


@Dao
public interface FormsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(Form form);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(Form... forms);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Form form);

    @Query("delete from Forms where id = :formId")
    void remove(long formId);

    @Query("delete from forms")
    void removeAll();

    @Query("select * from forms where id = :formId")
    Form get(long formId);

    @Query("select * from forms where parent_id = :parentId and visibility=1 order by position")
    List<Form> getAll(long parentId);

    @Query("select * from forms where visibility=1 order by position")
    List<Form> getAll();

    @Query("select * from forms where parent_id != 0 and visibility=1 order by position")
    List<Form> getAllSubForms();

}

