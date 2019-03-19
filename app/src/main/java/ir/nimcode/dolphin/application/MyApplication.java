package ir.nimcode.dolphin.application;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Typeface;
import android.os.Environment;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.facebook.stetho.Stetho;
import com.google.android.gms.common.Scopes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.FileAppender;
import es.dmoral.toasty.Toasty;
import io.fabric.sdk.android.Fabric;
import ir.nimcode.dolphin.R;
import ir.nimcode.dolphin.activity.SplashActivity;
import ir.nimcode.dolphin.database.AppDatabase;
import ir.nimcode.dolphin.database.DatabaseHelper;
import ir.nimcode.dolphin.util.LocaleHelper;
import ir.nimcode.dolphin.util.applock.core.LockManager;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by saeed on 11/10/17.
 */

public class MyApplication extends Application {

    public static final String APP_DIR = "/Payab/";

    public static File directory;
    public static SharedPref sp;
    public static AppDatabase database;
    public static DatabaseHelper databaseStudyArea;

    @Override
    public void onCreate() {
        super.onCreate();

//        configureLogbackDirectly();

        sp = new SharedPref(this);

        database = AppDatabase.getDatabase(this);
        databaseStudyArea = new DatabaseHelper(this, "databases/study_areas_db", "study_area_db");

        try {
            databaseStudyArea.createDataBase(false);
        } catch (IOException ioEx) {
            throw new Error("Unable to open database", ioEx);
        }

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(getString(R.string.fontNormal))
                .setFontAttrId(R.attr.fontPath)
                .build());

        directory = new ContextWrapper(this).getDir(Scopes.PROFILE, 0);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Stetho.initializeWithDefaults(this);


        Toasty.Config.getInstance()
                .setToastTypeface(Typeface.createFromAsset(getAssets(), getString(R.string.fontMedium)))
                .setTextSize(10)
                .apply();

        LockManager.getInstance().enableAppLock(this);
        LockManager.getInstance().getAppLock().addIgnoredActivity(SplashActivity.class);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

    }

//    private void configureLogbackDirectly() {
//        // reset the default context (which may already have been initialized)
//        // since we want to reconfigure it
//
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        lc.reset();
//
//        // setup FileAppender
//        PatternLayoutEncoder encoder1 = new PatternLayoutEncoder();
//        encoder1.setContext(lc);
//        encoder1.setPattern("%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
//        encoder1.start();
//
//        FileAppender<ILoggingEvent> fileAppender = new FileAppender<>();
//        fileAppender.setContext(lc);
////        fileAppender.setFile(this.getFileStreamPath("app.log").getAbsolutePath());
//        fileAppender.setFile(Environment.getExternalStorageDirectory().getAbsolutePath() + MyApplication.APP_DIR + "logs/app.log");
//        fileAppender.setEncoder(encoder1);
//        fileAppender.start();
//
//        // setup LogcatAppender
//        PatternLayoutEncoder encoder2 = new PatternLayoutEncoder();
//        encoder2.setContext(lc);
//        encoder2.setPattern("[%thread] %msg%n");
//        encoder2.start();
//
//        LogcatAppender logcatAppender = new LogcatAppender();
//        logcatAppender.setContext(lc);
//        logcatAppender.setEncoder(encoder2);
//        logcatAppender.start();
//
//        // add the newly created appenders to the root logger;
//        // qualify Logger to disambiguate from org.slf4j.Logger
//        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
//        root.addAppender(fileAppender);
//        root.addAppender(logcatAppender);
//    }

    protected void attachBaseContext(Context context) {
        Context newBase = LocaleHelper.setLocale(context, "fa");
        super.attachBaseContext(newBase);
        MultiDex.install(this);
    }
}
