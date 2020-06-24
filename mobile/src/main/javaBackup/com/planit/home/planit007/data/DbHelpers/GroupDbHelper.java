package planit007.planit.home.planit007.data.DbHelpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.planit.mobile.data.Contracts.GroupContract;

/**
 * Created by Home on 2017-08-02.
 */

public class GroupDbHelper extends SQLiteOpenHelper{

    static final String DATABASE_NAME = "groups.db";

    static final int DATABASE_VERSION = 15;

    public GroupDbHelper (Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String CREATE_GROUP_TABLE = "CREATE TABLE " +
                GroupContract.GroupEntry.TABLE_NAME + " (" +
                GroupContract.GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroupContract.GroupEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL " +
                ");";

        sqLiteDatabase.execSQL(CREATE_GROUP_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupContract.GroupEntry.TABLE_NAME);

        onCreate(sqLiteDatabase);
    }
}