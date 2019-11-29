package com.nlazzos.cuantoponemos.BDHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

import com.nlazzos.cuantoponemos.BuildConfig;
import com.nlazzos.cuantoponemos.DAOs.DAOPersona;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nico on 12/12/2017.
 */
public class BDHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "cuantoponemos.sqlite";
    private static final String DB_PATH = "data/" + BuildConfig.APPLICATION_ID + "/databases/";
    private static final int DB_SCHEME_VERSION = 1;

    public BDHelper(Context context) {
        super(context, DB_NAME, null, DB_SCHEME_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DAOPersona.CREATE_TABLE);
        db.execSQL(DAOPersona.INSERT1);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void exportDB() throws IOException {
        File data = Environment.getDataDirectory();
        File sdPath = Environment.getExternalStorageDirectory();
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Date now = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String backupFile  = "Cuanto_Ponemos_" + dateFormat.format(now) + ".sqlite";
            File currentDB = new File(data, DB_PATH + DB_NAME);
            File backupDB = new File(sdPath, backupFile);

            if(currentDB.exists()) {
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
            }
        }
    }

    public void importDB(String rutaBackup) throws IOException {
        File data = Environment.getDataDirectory();
        File currentDB = new File(data, DB_PATH + DB_NAME);

        OutputStream databaseOutputStream = new FileOutputStream(currentDB);
        InputStream databaseInputStream;

        byte[] buffer = new byte[1024];
        int length;

        databaseInputStream = new FileInputStream(rutaBackup);

        while ((length = databaseInputStream.read(buffer)) > 0) {
            databaseOutputStream.write(buffer);
        }

        databaseInputStream.close();
        databaseOutputStream.flush();
        databaseOutputStream.close();
    }

}
