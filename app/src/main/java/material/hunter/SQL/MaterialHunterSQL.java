package material.hunter.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import material.hunter.AppNavHomeActivity;
import material.hunter.models.MaterialHunterModel;
import material.hunter.utils.NhPaths;

/*
   SQLiteOpenHelper class for materialhunter fragment.
*/
public class MaterialHunterSQL extends SQLiteOpenHelper {
  private static final String DATABASE_NAME = "MaterialHunterFragment";
  private static final String TAG = "MaterialHunterSQL";
  private static final String TABLE_NAME = DATABASE_NAME;
  private static final String[][] materialhunterData = {
    {"1", "Kernel info", "uname -a", "1"},
    {"2", "Device Codename", "getprop ro.product.device", "1"},
    {"3", "HID Status", "ls /dev/hidg* || echo \"HID interface not found.\"", "1"},
    {
      "4",
      "SAR",
      "grep ' / ' /proc/mounts | grep -qv 'rootfs' || grep -q ' /system_root ' /proc/mounts && echo"
          + " True || echo False",
      "1"
    },
    {"5", "Network Interface Status", "ip -o addr show | awk '{print $2, $3, $4}'", "1"},
    {
      "6",
      "External IP",
      "curl ifconfig.me || echo \"No internet connection or busybox isn't installed.\"",
      "0"
    }
  };
  private static final ArrayList<String> COLUMNS = new ArrayList<>();
  private static MaterialHunterSQL instance;

  private MaterialHunterSQL(Context context) {
    super(context, DATABASE_NAME, null, 1);
    COLUMNS.add("id");
    COLUMNS.add("TitleName");
    COLUMNS.add("CommandforResult");
    COLUMNS.add("RunOnCreate");
  }

  public static synchronized MaterialHunterSQL getInstance(Context context) {
    if (instance == null) {
      instance = new MaterialHunterSQL(AppNavHomeActivity.context);
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
            + " INTEGER, "
            + COLUMNS.get(3)
            + " TEXT)");
    ContentValues initialValues = new ContentValues();
    db.beginTransaction();
    for (String[] data : materialhunterData) {
      initialValues.put(COLUMNS.get(0), data[0]);
      initialValues.put(COLUMNS.get(1), data[1]);
      initialValues.put(COLUMNS.get(2), data[2]);
      initialValues.put(COLUMNS.get(3), data[3]);
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

  public ArrayList<MaterialHunterModel> bindData(
      ArrayList<MaterialHunterModel> materialhunterModelArrayList) {
    SQLiteDatabase db = getWritableDatabase();
    Cursor cursor =
        db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);
    while (cursor.moveToNext()) {
      materialhunterModelArrayList.add(
          new MaterialHunterModel(
              cursor.getString(cursor.getColumnIndex(COLUMNS.get(1))),
              cursor.getString(cursor.getColumnIndex(COLUMNS.get(2))),
              cursor.getString(cursor.getColumnIndex(COLUMNS.get(3))),
              ""));
    }
    cursor.close();
    db.close();
    return materialhunterModelArrayList;
  }

  public void addData(int targetPositionId, ArrayList<String> addData) {
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
    initialValues.put(COLUMNS.get(1), addData.get(0));
    initialValues.put(COLUMNS.get(2), addData.get(1));
    initialValues.put(COLUMNS.get(3), addData.get(2));
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
        db.rawQuery("SELECT * FROM " + TABLE_NAME + " ORDER BY " + COLUMNS.get(0) + ";", null);

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
            + "' "
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
            + " INTEGER, "
            + COLUMNS.get(3)
            + " TEXT)");
    ContentValues initialValues = new ContentValues();
    db.beginTransaction();
    for (String[] data : materialhunterData) {
      initialValues.put(COLUMNS.get(0), data[0]);
      initialValues.put(COLUMNS.get(1), data[1]);
      initialValues.put(COLUMNS.get(2), data[2]);
      initialValues.put(COLUMNS.get(3), data[3]);
      db.insert(TABLE_NAME, null, initialValues);
    }
    db.setTransactionSuccessful();
    db.endTransaction();
    db.close();
  }

  public String backupData(String storedDBpath) {
    try {
      String currentDBPath =
          Environment.getDataDirectory() + "/data/material.hunter/databases/" + getDatabaseName();
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
    if (!verifyDB(storedDBpath)) {
      return "invalid columns format.";
    }
    try {
      String currentDBPath =
          Environment.getDataDirectory() + "/data/material.hunter/databases/" + getDatabaseName();
      if (Environment.getExternalStorageDirectory().canWrite()) {
        File currentDB = new File(currentDBPath);
        File backupDB = new File(storedDBpath);
        if (backupDB.exists()) {
          FileChannel src = new FileInputStream(backupDB).getChannel();
          FileChannel dst = new FileOutputStream(currentDB).getChannel();
          dst.transferFrom(src, 0, src.size());
          src.close();
          dst.close();
          // NhPaths.showMessage(context, "db is successfully restored to " + currentDBPath);
          // return "db is successfully restored to " + currentDBPath;
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      return e.toString();
    }
    return null;
  }

  private boolean verifyDB(String storedDBpath) {
    SQLiteDatabase tempDB =
        SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READWRITE);
    Cursor c =
        tempDB.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name='" + TABLE_NAME + "'",
            null);
    if (c.getCount() == 1) {
      c.close();
      c = tempDB.query(TABLE_NAME, null, null, null, null, null, null);
      String[] tempColumnNames = c.getColumnNames();
      c.close();
      if (tempColumnNames.length != COLUMNS.size()) {
        tempDB.close();
        return false;
      }
      for (int i = 0; i < tempColumnNames.length; i++) {
        if (!tempColumnNames[i].equals(COLUMNS.get(i))) {
          tempDB.close();
          return false;
        }
      }
      tempDB.close();
      return true;
    }
    tempDB.close();
    return false;
  }
}