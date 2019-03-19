package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.KmlFile;

/**
 * Created by saeed on 3/8/18.
 */

@Dao
public interface KmlFilesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(KmlFile kmlFile);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(KmlFile... kmlFiles);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(KmlFile kmlFile);

    @Query("delete from kml_files where id = :kmlFileId")
    void remove(long kmlFileId);

    @Query("delete from kml_files")
    void removeAll();

    @Query("select * from kml_files where id = :kmlFileId")
    KmlFile get(long kmlFileId);

    @Query("select * from kml_files")
    List<KmlFile> getAll();

}
