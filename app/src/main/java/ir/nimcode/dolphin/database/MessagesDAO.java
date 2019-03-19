package ir.nimcode.dolphin.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import ir.nimcode.dolphin.model.Message;

/**
 * Created by saeed on 2/17/18.
 */

@Dao
public interface MessagesDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long add(Message message);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] addAll(Message... messages);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void update(Message message);

    @Query("delete from messages where id = :messageId")
    void remove(long messageId);

    @Query("delete from messages")
    void removeAll();

    @Query("select * from messages where id = :messageId")
    Message get(long messageId);

    @Query("select * from messages")
    List<Message> getAll();

}
