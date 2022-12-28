package com.maknoon.quran;

import android.content.Context;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class DBHelper extends SQLiteOpenHelper
{
    final static String DB_ASSET = "quran.db";
    final static String DB_NAME = "app.db";
    final static int DB_VERSION = 1;
    final Context context;

    DBHelper(final Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        addDB(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE Quran");
        //db.execSQL("VACUUM FULL"); //take very long time 30 min without much improvement
        addDB(db);
    }

    void addDB(SQLiteDatabase db)
    {
        copyDataBase();
        db.execSQL(String.format("ATTACH DATABASE '%s' AS db", this.context.getDatabasePath(DB_ASSET).getAbsolutePath()));
        db.execSQL("CREATE TABLE Quran AS SELECT * FROM db.Quran");
        context.deleteDatabase(DB_ASSET);
    }

    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     */
    private void copyDataBase()
    {
        final String path = context.getDatabasePath(DB_ASSET).getAbsolutePath();

        final File f = new File(path);
        f.getParentFile().mkdirs();

        try
        {
            // Open your local db as the input stream
            final InputStream in = context.getAssets().open(DB_ASSET);

            // Open the empty db as the output stream
            final OutputStream out = new FileOutputStream(path);

            //transfer bytes from the inputfile to the outputfile
            final byte[] buffer = new byte[1024 * 3];
            int length;
            while ((length = in.read(buffer)) > 0)
                out.write(buffer, 0, length);

            out.close();
            in.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}