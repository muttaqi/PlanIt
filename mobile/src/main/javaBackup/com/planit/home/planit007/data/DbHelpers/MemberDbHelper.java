package planit007.planit.home.planit007.data.DbHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.planit.mobile.data.Contracts.MemberContract;

/**
 * Created by Home on 2017-08-02.
 */

public class MemberDbHelper extends SQLiteOpenHelper{

    static final String TAG = MemberDbHelper.class.getSimpleName();

    static final String DATABASE_NAME = "members.db";

    static final int DATABASE_VERSION = 15;

    public MemberDbHelper (Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_MEMBER_TABLE = "CREATE TABLE " + MemberContract.MemberEntry.TABLE_NAME + " (" +
                MemberContract.MemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MemberContract.MemberEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MemberContract.MemberEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL,  " +
                MemberContract.MemberEntry.COLUMN_TIMESLOT_MEMBER_AVAILABILITY + " TEXT,  " +
                MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS + " TEXT NOT NULL,  " +
                MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES + " TEXT NOT NULL" +
                ");";

        sqLiteDatabase.execSQL(CREATE_MEMBER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        Log.d(TAG, "DEBUG MDBHELPER 44 MEMBER TABLE DROPPED");

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MemberContract.MemberEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}