package com.planit.mobile.data.DbHelpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.planit.mobile.data.Contracts.MemberContract

/**
 * Created by Home on 2017-08-02.
 */

class MemberDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

        val CREATE_MEMBER_TABLE = "CREATE TABLE " + MemberContract.MemberEntry.TABLE_NAME + " (" +
                MemberContract.MemberEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MemberContract.MemberEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                MemberContract.MemberEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL,  " +
                MemberContract.MemberEntry.COLUMN_TIMESLOT_MEMBER_AVAILABILITY + " TEXT,  " +
                MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS + " TEXT NOT NULL,  " +
                MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES + " TEXT NOT NULL" +
                ");"

        sqLiteDatabase.execSQL(CREATE_MEMBER_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

        Log.d(TAG, "DEBUG MDBHELPER 44 MEMBER TABLE DROPPED")

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MemberContract.MemberEntry.TABLE_NAME)

        onCreate(sqLiteDatabase)
    }

    companion object {

        internal val TAG = MemberDbHelper::class.java.simpleName

        internal val DATABASE_NAME = "members.db"

        internal val DATABASE_VERSION = 16
    }
}