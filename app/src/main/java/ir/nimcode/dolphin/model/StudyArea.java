package ir.nimcode.dolphin.model;

/**
 * Created by saeed on 4/10/18.
 */

public class StudyArea {

    public static final String TABLE_NAME = "study_areas";

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CODE = "code";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_POLYGON = "polygon";
    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_CODE + " INTEGER,"
                    + COLUMN_NAME + " TEXT, "
                    + COLUMN_POLYGON + " GEOMETRY "
                    + ")";
    private long id;
    private long code;
    private String name;

    public StudyArea(long id, long code, String name) {
        this.id = id;
        this.code = code;
        this.name = name;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCode() {
        return code;
    }

    public void setCode(long code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
