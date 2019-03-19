package ir.nimcode.dolphin.database;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

import ir.nimcode.dolphin.model.DataTypeConverter;
import ir.nimcode.dolphin.model.Document;
import ir.nimcode.dolphin.model.DocumentSeries;
import ir.nimcode.dolphin.model.Form;
import ir.nimcode.dolphin.model.FormSheet;
import ir.nimcode.dolphin.model.FormSheetProperty;
import ir.nimcode.dolphin.model.GlobalSetting;
import ir.nimcode.dolphin.model.KmlFile;
import ir.nimcode.dolphin.model.Message;
import ir.nimcode.dolphin.model.Property;
import ir.nimcode.dolphin.model.Sheet;

/**
 * Created by saeed on 12/17/17.
 */

@Database(
        entities =
                {
                        KmlFile.class,
                        Message.class,
                        Form.class,
                        Sheet.class,
                        FormSheet.class,
                        Property.class,
                        FormSheetProperty.class,
                        Document.class,
                        DocumentSeries.class,
                        GlobalSetting.class
                },
        version = 1
        ,
        exportSchema = false
)
@TypeConverters({DataTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {


    // Database Name
    public static final String DATABASE_NAME = "app_db";


    public static AppDatabase INSTANCE;

    private static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {

//            // Create the new table
//            database.execSQL("CREATE TABLE forms_new (id INTEGER NOT NULL, parent_id INTEGER NOT NULL, name_en TEXT, name_fa TEXT, map_pin_symbol TEXT, position INTEGER NOT NULL, visibility INTEGER NOT NULL, PRIMARY KEY(id))");
//
//            // Copy the data
//            database.execSQL("INSERT INTO forms_new (id, parent_id, name_en, name_fa, map_pin_symbol, position, visibility) SELECT id, parent_id, name_en, name_fa, map_pin_symbol, position, visibility FROM forms");
//
//            // Remove the old table
//            database.execSQL("DROP TABLE forms");
//
//            // Change the table name to the correct one
//            database.execSQL("ALTER TABLE forms_new RENAME TO forms");
//
//            database.execSQL("CREATE INDEX index_forms_id ON forms (id)");

//            database.execSQL("ALTER TABLE forms ADD COLUMN map_pin_symbol TEXT");
//            database.execSQL("DELETE FROM forms");
//            database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'forms'");
//
//            database.execSQL("DELETE FROM sheets");
//            database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'sheets'");
//
//            database.execSQL("DELETE FROM forms_sheets");
//            database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'forms_sheets'");
//
//            database.execSQL("DELETE FROM properties");
//            database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'properties'");
//
//            database.execSQL("DELETE FROM forms_sheets_properties");
//            database.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = 'forms_sheets_properties'");
//
//            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
//                @Override
//                public void run() {
//                    INSTANCE.formDAO().addAll(Form.populateData());
//                    INSTANCE.sheetDAO().addAll(Sheet.populateData());
//                    INSTANCE.formSheetDAO().addAll(FormSheet.populateData());
//                    INSTANCE.propertyDAO().addAll(Property.populateData());
//                    INSTANCE.formSheetPropertyDAO().addAll(FormSheetProperty.populateData());
//
//                }
//            });
        }
    };

    public synchronized static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room
//            INSTANCE = RoomAsset
                    .databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
//                    .addCallback(new Callback() {
//                        @Override
//                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
//                            super.onCreate(db);
//                            Executors.newSingleThreadScheduledExecutor().execute(new Runnable() {
//                                @Override
//                                public void run() {
//                                    getDatabase(context).formDAO().addAll(Form.populateData());
//                                    getDatabase(context).sheetDAO().addAll(Sheet.populateData());
//                                    getDatabase(context).formSheetDAO().addAll(FormSheet.populateData());
//                                    getDatabase(context).propertyDAO().addAll(Property.populateData());
//                                    getDatabase(context).formSheetPropertyDAO().addAll(FormSheetProperty.populateData());
//                                }
//                            });
//                        }
//                    })
                    .allowMainThreadQueries()
//                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    public abstract KmlFilesDAO kmlLayerDAO();

    public abstract MessagesDAO messageDAO();

    public abstract FormsDAO formDAO();

    public abstract SheetsDAO sheetDAO();

    public abstract FormsSheetsDAO formSheetDAO();

    public abstract PropertiesDAO propertyDAO();

    public abstract FormSheetPropertyDAO formSheetPropertyDAO();

    public abstract DocumentsDAO documentDAO();

    public abstract DocumentsSeriesDAO documentSeriesDAO();

    public abstract GlobalSettingsDAO globalSettingsDAO();

}