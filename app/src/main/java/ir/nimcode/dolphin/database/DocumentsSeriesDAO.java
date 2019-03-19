package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.DocumentSeries;

/**
 * Created by saeed on 3/13/18.
 */

@Dao
public interface DocumentsSeriesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(DocumentSeries documentSeries);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(DocumentSeries... documentSeries);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(DocumentSeries documentSeries);

    @Query("delete from documents_series where id = :documentSeriesId")
    void remove(long documentSeriesId);

    @Query("delete from documents_series")
    void removeAll();

    @Query("select * from documents_series where id = :documentSeriesId")
    DocumentSeries get(long documentSeriesId);

    @Query("select * from documents_series where document_id = :documentId and position = :position")
    DocumentSeries get(long documentId, int position);

    @Query("select * from documents_series where :query_conditions")
    List<DocumentSeries> search(String query_conditions);

    @Query("select * from documents_series order by form_id,document_id,position asc")
    List<DocumentSeries> getAll();

    @Query("select * from documents_series where document_id = :documentId order by form_id,position asc")
    List<DocumentSeries> getAll(long documentId);

    @Query("select * from documents_series where date!=0 order by form_id,document_id,position asc")
    List<DocumentSeries> getAllNonZeroDate();

    @Query("select * from documents_series where date >= :startDate and date!=0 order by form_id,document_id,position asc")
    List<DocumentSeries> getAllGreater(long startDate);

    @Query("select * from documents_series where date <= :finishDate and date!=0 order by form_id,document_id,position asc")
    List<DocumentSeries> getAllLesser(long finishDate);

    @Query("select * from documents_series" +
            " where date between :startDate and :finishDate and date!=0" +
            " order by form_id,document_id,position asc ")
    List<DocumentSeries> getAll(long startDate, long finishDate);

    @Query("select * from documents_series where isUpload = 0 and  date >= :startDate and date!=0 group by document_id order by form_id,document_id,position asc")
    List<DocumentSeries> getAllGreaterNotUpload(long startDate);

    @Query("select * from documents_series where isUpload = 0 and date <= :finishDate and date!=0 group by document_id order by form_id,document_id,position asc")
    List<DocumentSeries> getAllLesserNotUpload(long finishDate);

    @Query("select * from documents_series" +
            " where isUpload = 0 and date between :startDate and :finishDate and date!=0" +
            " group by document_id order by form_id,document_id,position asc ")
    List<DocumentSeries> getAllNotUpload(long startDate, long finishDate);

    @Query("select * from documents_series where isUpload = 0 and date!=0 group by document_id order by form_id,document_id,position asc")
    List<DocumentSeries> getAllNotUpload();

}