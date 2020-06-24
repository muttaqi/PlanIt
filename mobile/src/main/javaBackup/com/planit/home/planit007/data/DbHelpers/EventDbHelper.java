package planit007.planit.home.planit007.data.DbHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.planit.mobile.data.Contracts.EventContract;


/**
 * Created by Home on 2017-08-02.
 */

public class EventDbHelper extends SQLiteOpenHelper{

    String TAG = "EventDbHelper";

    static final String DATABASE_NAME = "events.db";

    static final int DATABASE_VERSION = 15;

    public EventDbHelper (Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_EVENT_TABLE = "CREATE TABLE " +
                EventContract.EventEntry.TABLE_NAME + " (" +
                EventContract.EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventContract.EventEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_EVENT_DATE + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOBS + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES + " TEXT NOT NULL" +
                ");";

        Log.d(TAG, "DEBUG EDH 41 " + EventContract.EventEntry.TABLE_NAME);

        sqLiteDatabase.execSQL(CREATE_EVENT_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventContract.EventEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}