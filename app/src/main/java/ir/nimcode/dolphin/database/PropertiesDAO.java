package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.Property;

/**
 * Created by saeed on 2/17/18.
 */


@Dao
public interface PropertiesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(Property property);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(Property... properties);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Property property);

    @Query("delete from properties where id = :propertyId")
    void remove(long propertyId);

    @Query("delete from properties")
    void removeAll();

    @Query("select * from properties where id = :propertyId and visibility=1")
    Property get(long propertyId);

    @Query("select * from properties where tag = :tag and visibility=1")
    Property getByTag(String tag);

    @Query("select * from properties where visibility=1")
    List<Property> getAll();

    @Query("select * from properties where type = :type and visibility=1")
    List<Property> getAllType(int type);

    @Query("select id from properties where isSearchable = 1 and visibility=1")
    List<Long> getAllSearchable();

}

