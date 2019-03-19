package ir.nimcode.dolphin.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import java.io.Serializable;

/**
 * Created by saeed on 2/17/18.
 */

@Entity(tableName = "messages", indices = {@Index(value = "id")})
public class Message implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String message;

    private boolean visited;

    private int document_id;

    public Message(String message, boolean visited, int document_id) {
        this.message = message;
        this.visited = visited;
        this.document_id = document_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }
}
