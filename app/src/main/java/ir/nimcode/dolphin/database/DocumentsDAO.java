package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.Document;

/**
 * Created by saeed on 2/17/18.
 */

@Dao
public interface DocumentsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(Document document);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(Document... documents);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Document document);

    @Query("delete from documents where id = :documentId")
    void remove(long documentId);

    @Query("delete from documents")
    void removeAll();

    @Query("select * from documents where id = :documentId")
    Document get(long documentId);

    @Query("select * from documents where global_id = :globalId")
    Document getByGlobalId(long globalId);

    @Query("select * from documents order by form_id asc")
    List<Document> getAll();

    @Query("select * from documents order by last_seen_date desc limit 0,50")
    List<Document> getAllHistory();

    @Query("select * from documents " +
            "where latitude >= :latitude_min " +
            "  and latitude <= :latitude_max " +
            "  and longitude >= :longitude_min " +
            "  and longitude <= :longitude_max ")
    List<Document> getNearest(double latitude_min, double latitude_max, double longitude_min, double longitude_max);

    @Query("select * from documents " +
            "where form_id=:formId " +
            "  and latitude >= :latitude_min " +
            "  and latitude <= :latitude_max " +
            "  and longitude >= :longitude_min " +
            "  and longitude <= :longitude_max ")
    List<Document> getNearestWithFormId(long formId, double latitude_min, double latitude_max, double longitude_min, double longitude_max);

}
