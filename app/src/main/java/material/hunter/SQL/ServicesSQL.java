package material.hunter.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;

import material.hunter.MainActivity;
import material.hunter.models.ServicesModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class ServicesSQL extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ServicesFragment";
    private static final String TAG = "ServicesSQL";
    private static final String TABLE_NAME = DATABASE_NAME;
    private static final String[][] ServicesData = {
        {"1", "SSH", "service ssh start", "service ssh stop", "sshd", "0"}
    };
    private static final ArrayList<String> COLUMNS = new ArrayList<>();
    private static ServicesSQL instance;

    private ServicesSQL(Context context) {
        super(context, DATABASE_NAME, null, 3);
        // Add your default column here;
        COLUMNS.add("id");
        COLUMNS.add("label");
        COLUMNS.add("cmd_start");
        COLUMNS.add("cmd_stop");
        COLUMNS.add("cmd_check");
        COLUMNS.add("receive");
    }

    public static synchronized ServicesSQL getInstance(Context context) {
        if (instance == null) {
            instance = new ServicesSQL(MainActivity.context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE "
                        + TABLE_NAME
                        + " ("
                        + COLUMNS.get(0)
                        + " INTEGER, "
                        + COLUMNS.get(1)
                        + " TEXT, "
                        + COLUMNS.get(2)
                        + " TEXT, "
                        + COLUMNS.get(3)
                        + " TEXT, "
                        + COLUMNS.get(4)
                        + " TEXT, "
                        + COLUMNS.get(5)
                        + " INTEGER)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data : ServicesData) {
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
            initialValues.put(COLUMNS.get(5), data[5]);
            db.insert(TABLE_NAME, null, initialValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        this.onCreate(db);
    }

    public ArrayList<ServicesModel> bindData(ArrayList<ServicesModel> servicesModelArrayList) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
        while (cursor.moveToNext()) {
            servicesModelArrayList.add(
                    new ServicesModel(
                            cursor.getString(cursor.getColumnIndex(COLUMNS.get(1))),
                            cursor.getString(cursor.getColumnIndex(COLUMNS.get(2))),
                            cursor.getString(cursor.getColumnIndex(COLUMNS.get(3))),
                            cursor.getString(cursor.getColumnIndex(COLUMNS.get(4))),
                            cursor.getString(cursor.getColumnIndex(COLUMNS.get(5))),
                            "[-] Service isn't running"));
        }
        cursor.close();
        db.close();
        return servicesModelArrayList;
    }

    public void addData(int targetPositionId, ArrayList<String> Data) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues initialValues = new ContentValues();
        db.execSQL(
                "UPDATE "
                        + TABLE_NAME
                        + " SET "
                        + COLUMNS.get(0)
                        + " = "
                        + COLUMNS.get(0)
                        + " + 1 WHERE "
                        + COLUMNS.get(0)
                        + " >= "
                        + targetPositionId
                        + ";");
        initialValues.put(COLUMNS.get(0), targetPositionId);
        initialValues.put(COLUMNS.get(1), Data.get(0));
        initialValues.put(COLUMNS.get(2), Data.get(1));
        initialValues.put(COLUMNS.get(3), Data.get(2));
        initialValues.put(COLUMNS.get(4), Data.get(3));
        initialValues.put(COLUMNS.get(5), Data.get(4));
        db.beginTransaction();
        db.insert(TABLE_NAME, null, initialValues);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void deleteData(ArrayList<Integer> selectedTargetIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "DELETE FROM "
                        + TABLE_NAME
                        + " WHERE "
                        + COLUMNS.get(0)
                        + " in ("
                        + TextUtils.join(",", selectedTargetIds)
                        + ");");
        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);

        while (cursor.moveToNext()) {
            db.execSQL(
                    "UPDATE "
                            + TABLE_NAME
                            + " SET "
                            + COLUMNS.get(0)
                            + " = "
                            + cursor.getPosition()
                            + " + 1 WHERE "
                            + COLUMNS.get(0)
                            + " = "
                            + cursor.getInt(0)
                            + ";");
        }
        cursor.close();
        db.close();
    }

    public void moveData(Integer originalPosition, Integer targetPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE "
                        + TABLE_NAME
                        + " SET "
                        + COLUMNS.get(0)
                        + " = 0 - 1 WHERE "
                        + COLUMNS.get(0)
                        + " = "
                        + (originalPosition + 1)
                        + ";");
        if (originalPosition < targetPosition) {
            db.execSQL(
                    "UPDATE "
                            + TABLE_NAME
                            + " SET "
                            + COLUMNS.get(0)
                            + " = "
                            + COLUMNS.get(0)
                            + " - 1 WHERE "
                            + COLUMNS.get(0)
                            + " > "
                            + (originalPosition + 1)
                            + " AND "
                            + COLUMNS.get(0)
                            + " < "
                            + (targetPosition + 2)
                            + ";");
        } else {
            db.execSQL(
                    "UPDATE "
                            + TABLE_NAME
                            + " SET "
                            + COLUMNS.get(0)
                            + " = "
                            + COLUMNS.get(0)
                            + " + 1 WHERE "
                            + COLUMNS.get(0)
                            + " > "
                            + targetPosition
                            + " AND "
                            + COLUMNS.get(0)
                            + " < "
                            + (originalPosition + 1)
                            + ";");
        }
        db.execSQL(
                "UPDATE "
                        + TABLE_NAME
                        + " SET "
                        + COLUMNS.get(0)
                        + " = "
                        + (targetPosition + 1)
                        + " WHERE "
                        + COLUMNS.get(0)
                        + " = -1;");
        db.close();
    }

    public void editData(Integer targetPosition, ArrayList<String> editData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(
                "UPDATE "
                        + TABLE_NAME
                        + " SET "
                        + COLUMNS.get(1)
                        + " = '"
                        + editData.get(0).replace("'", "''")
                        + "', "
                        + COLUMNS.get(2)
                        + " = '"
                        + editData.get(1).replace("'", "''")
                        + "', "
                        + COLUMNS.get(3)
                        + " = '"
                        + editData.get(2).replace("'", "''")
                        + "', "
                        + COLUMNS.get(4)
                        + " = '"
                        + editData.get(3).replace("'", "''")
                        + "', "
                        + COLUMNS.get(5)
                        + " = '"
                        + editData.get(4).replace("'", "''")
                        + "'"
                        + " WHERE "
                        + COLUMNS.get(0)
                        + " = "
                        + (targetPosition + 1));
        db.close();
    }

    public void resetData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL(
                "CREATE TABLE "
                        + TABLE_NAME
                        + " ("
                        + COLUMNS.get(0)
                        + " INTEGER, "
                        + COLUMNS.get(1)
                        + " TEXT, "
                        + COLUMNS.get(2)
                        + " TEXT, "
                        + COLUMNS.get(3)
                        + " TEXT, "
                        + COLUMNS.get(4)
                        + " TEXT, "
                        + COLUMNS.get(5)
                        + " INTEGER)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data : ServicesData) {
            initialValues.put(COLUMNS.get(0), data[0]);
            initialValues.put(COLUMNS.get(1), data[1]);
            initialValues.put(COLUMNS.get(2), data[2]);
            initialValues.put(COLUMNS.get(3), data[3]);
            initialValues.put(COLUMNS.get(4), data[4]);
            initialValues.put(COLUMNS.get(5), data[5]);
            db.insert(TABLE_NAME, null, initialValues);
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public String backupData(String storedDBpath) {
        try {
            String currentDBPath =
                    Environment.getDataDirectory()
                            + "/data/material.hunter/databases/"
                            + getDatabaseName();
            if (Environment.getExternalStorageDirectory().canWrite()) {
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);
                if (currentDB.exists()) {
                    FileChannel src = new FileInputStream(currentDB).getChannel();
                    FileChannel dst = new FileOutputStream(backupDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return null;
    }

    public String restoreData(String storedDBpath) {
        if (!new File(storedDBpath).exists()) {
            return "db file not found.";
        }
        try {
            String currentDBPath =
                    Environment.getDataDirectory()
                            + "/data/material.hunter/databases/"
                            + getDatabaseName();
            if (Environment.getExternalStorageDirectory().canWrite()) {
                File currentDB = new File(currentDBPath);
                File backupDB = new File(storedDBpath);
                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB).getChannel();
                    FileChannel dst = new FileOutputStream(currentDB).getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
        return null;
    }
}