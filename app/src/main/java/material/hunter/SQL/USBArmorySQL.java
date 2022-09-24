package material.hunter.SQL;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import material.hunter.MainActivity;
import material.hunter.models.USBArmoryUSBSwitchModel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class USBArmorySQL extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "USBArmoryFragment";
    private static final String TAG = "USBArmorySQL";
    private static final String USBSWITCH_TABLE_NAME = "USBSwitch";
    private static final ArrayList<String> COLUMNS_USBSWITCH = new ArrayList<>();
    private static USBArmorySQL instance;

    private USBArmorySQL(Context context) {
        super(context, DATABASE_NAME, null, 3);
        // Add your default column here;
        COLUMNS_USBSWITCH.add("target");
        COLUMNS_USBSWITCH.add("functions");
        COLUMNS_USBSWITCH.add("idVendor");
        COLUMNS_USBSWITCH.add("idProduct");
        COLUMNS_USBSWITCH.add("manufacturer");
        COLUMNS_USBSWITCH.add("product");
        COLUMNS_USBSWITCH.add("serialnumber");
    }

    public static synchronized USBArmorySQL getInstance(Context context) {
        if (instance == null) {
            instance = new USBArmorySQL(MainActivity.context);
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String[][] USBSwitchData = {
            {"Windows", "reset", "0x1234", "0x5678", "", "", ""},
            {"Windows", "hid", "0x046d", "0xc316", "", "", ""},
            {"Windows", "hid,adb", "0x046d", "0xc317", "", "", ""},
            {"Windows", "mass_storage", "0x9051", "0x168a", "", "", ""},
            {"Windows", "mass_storage,adb", "0x9051", "0x168b", "", "", ""},
            {"Windows", "rndis", "0x0525", "0xa4a2", "", "", ""},
            {"Windows", "rndis,adb", "0x0525", "0xa4a3", "", "", ""},
            {"Windows", "hid,mass_storage", "0x046d", "0xc318", "", "", ""},
            {"Windows", "hid,mass_storage,adb", "0x046d", "0xc319", "", "", ""},
            {"Windows", "rndis,hid", "0x0525", "0xa4a6", "", "", ""},
            {"Windows", "rndis,hid,adb", "0x0525", "0xa4a7", "", "", ""},
            {"Windows", "rndis,mass_storage", "0x0525", "0xa4a4", "", "", ""},
            {"Windows", "rndis,mass_storage,adb", "0x0525", "0xa4a5", "", "", ""},
            {"Windows", "rndis,hid,mass_storage", "0x0525", "0xa4a8", "", "", ""},
            {"Windows", "rndis,hid,mass_storage,adb", "0x0525", "0xa4a9", "", "", ""},
            {"Linux", "reset", "0x1234", "0x5678", "", "", ""},
            {"Linux", "hid", "0x046d", "0xc316", "", "", ""},
            {"Linux", "hid,adb", "0x046d", "0xc317", "", "", ""},
            {"Linux", "mass_storage", "0x9051", "0x168a", "", "", ""},
            {"Linux", "mass_storage,adb", "0x9051", "0x168b", "", "", ""},
            {"Linux", "rndis", "0x0525", "0xa4a2", "", "", ""},
            {"Linux", "rndis,adb", "0x0525", "0xa4a3", "", "", ""},
            {"Linux", "hid,mass_storage", "0x046d", "0xc318", "", "", ""},
            {"Linux", "hid,mass_storage,adb", "0x046d", "0xc319", "", "", ""},
            {"Linux", "rndis,hid", "0x0525", "0xa4a6", "", "", ""},
            {"Linux", "rndis,hid,adb", "0x0525", "0xa4a7", "", "", ""},
            {"Linux", "rndis,mass_storage", "0x0525", "0xa4a4", "", "", ""},
            {"Linux", "rndis,mass_storage,adb", "0x0525", "0xa4a5", "", "", ""},
            {"Linux", "rndis,hid,mass_storage", "0x0525", "0xa4a8", "", "", ""},
            {"Linux", "rndis,hid,mass_storage,adb", "0x0525", "0xa4a9", "", "", ""},
            {"Mac OS", "reset", "0x1234", "0x5678", "", "", ""},
            {"Mac OS", "hid", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "hid,adb", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "mass_storage", "0x0930", "0x6545", "", "", ""},
            {"Mac OS", "mass_storage,adb", "0x0930", "0x6545", "", "", ""},
            {"Mac OS", "acm,ecm", "0x1d6b", "0x0105", "", "", ""},
            {"Mac OS", "acm,ecm,adb", "0x1d6b", "0x0105", "", "", ""},
            {"Mac OS", "hid,mass_storage", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "hid,mass_storage,adb", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "acm,ecm,hid", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "acm,ecm,hid,adb", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "acm,ecm,mass_storage", "0x1d6b", "0x0105", "", "", ""},
            {"Mac OS", "acm,ecm,mass_storage,adb", "0x1d6b", "0x0105", "", "", ""},
            {"Mac OS", "acm,ecm,hid,mass_storage", "0x05ac", "0x0201", "", "", ""},
            {"Mac OS", "acm,ecm,hid,mass_storage,adb", "0x05ac", "0x0201", "", "", ""}
        };
        db.execSQL(
                "CREATE TABLE "
                        + USBSWITCH_TABLE_NAME
                        + " ("
                        + COLUMNS_USBSWITCH.get(0)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(1)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(2)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(3)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(4)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(5)
                        + " TEXT, "
                        + COLUMNS_USBSWITCH.get(6)
                        + " TEXT)");
        ContentValues initialValues = new ContentValues();
        db.beginTransaction();
        for (String[] data : USBSwitchData) {
            initialValues.put(COLUMNS_USBSWITCH.get(0), data[0]);
            initialValues.put(COLUMNS_USBSWITCH.get(1), data[1]);
            initialValues.put(COLUMNS_USBSWITCH.get(2), data[2]);
            initialValues.put(COLUMNS_USBSWITCH.get(3), data[3]);
            initialValues.put(COLUMNS_USBSWITCH.get(4), data[4]);
            initialValues.put(COLUMNS_USBSWITCH.get(5), data[5]);
            initialValues.put(COLUMNS_USBSWITCH.get(6), data[6]);
            db.insert(USBSWITCH_TABLE_NAME, null, initialValues);
            initialValues.clear();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public USBArmoryUSBSwitchModel getUSBSwitchColumnData(
            String targetOSName, String functionName) {
        USBArmoryUSBSwitchModel usbArmoryUSBSwitchModel = new USBArmoryUSBSwitchModel();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor =
                db.rawQuery(
                        "SELECT * FROM "
                                + USBSWITCH_TABLE_NAME
                                + " WHERE "
                                + COLUMNS_USBSWITCH.get(0)
                                + "='"
                                + targetOSName
                                + "'"
                                + " AND "
                                + COLUMNS_USBSWITCH.get(1)
                                + "='"
                                + functionName
                                + "';",
                        null);
        if (cursor.moveToFirst()) {
            usbArmoryUSBSwitchModel.setidVendor(
                    cursor.getString(cursor.getColumnIndex(COLUMNS_USBSWITCH.get(2))));
            usbArmoryUSBSwitchModel.setidProduct(
                    cursor.getString(cursor.getColumnIndex(COLUMNS_USBSWITCH.get(3))));
            usbArmoryUSBSwitchModel.setmanufacturer(
                    cursor.getString(cursor.getColumnIndex(COLUMNS_USBSWITCH.get(4))));
            usbArmoryUSBSwitchModel.setproduct(
                    cursor.getString(cursor.getColumnIndex(COLUMNS_USBSWITCH.get(5))));
            usbArmoryUSBSwitchModel.setserialnumber(
                    cursor.getString(cursor.getColumnIndex(COLUMNS_USBSWITCH.get(6))));
        }
        cursor.close();
        return usbArmoryUSBSwitchModel;
    }

    public boolean setUSBSwitchColumnData(
            String functionName, int targetColumnIndex, String targetOSName, String content) {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL(
                    "UPDATE "
                            + USBSWITCH_TABLE_NAME
                            + " SET "
                            + COLUMNS_USBSWITCH.get(targetColumnIndex)
                            + " = '"
                            + content
                            + "'"
                            + " WHERE "
                            + COLUMNS_USBSWITCH.get(0)
                            + " = '"
                            + targetOSName
                            + "'"
                            + " AND "
                            + COLUMNS_USBSWITCH.get(1)
                            + " = '"
                            + functionName
                            + "';");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean resetData() {
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + USBSWITCH_TABLE_NAME);
            this.onCreate(db);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
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

        SQLiteDatabase tempDB =
                SQLiteDatabase.openDatabase(storedDBpath, null, SQLiteDatabase.OPEN_READONLY);
        if (tempDB.getVersion() > this.getReadableDatabase().getVersion()) {
            tempDB.close();
            return "db cannot be restored.\n"
                       + "Reason: the db version of your backup db is newer than the current db"
                       + " version.";
        }
        tempDB.close();

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
            Log.e(TAG, e.toString());
            return e.toString();
        }
        return null;
    }

    private boolean ifTableExists(SQLiteDatabase tempDB, String tableName) {
        boolean tableExists = false;
        try {
            Cursor c =
                    tempDB.rawQuery(
                            "SELECT name FROM sqlite_master WHERE type='table' AND name='"
                                    + tableName
                                    + "'",
                            null);
            if (c.getCount() == 1) {
                tableExists = true;
            }
            c.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return tableExists;
    }
}
