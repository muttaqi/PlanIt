package com.planit.mobile.data.DbHelpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

import com.planit.mobile.data.Contracts.EventContract


/**
 * Created by Home on 2017-08-02.
 */

class EventDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    internal var TAG = "EventDbHelper"

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

        val CREATE_EVENT_TABLE = "CREATE TABLE " +
                EventContract.EventEntry.TABLE_NAME + " (" +
                EventContract.EventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                EventContract.EventEntry.COLUMN_EVENT_NAME + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_EVENT_DATE + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_GROUP_ID + " INTEGER NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOBS + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES + " TEXT NOT NULL, " +
                EventContract.EventEntry.COLUMN_CALENDAR_ID + " TEXT " +
                ");"

        Log.d(TAG, "DEBUG EDH 41 " + EventContract.EventEntry.TABLE_NAME)

        sqLiteDatabase.execSQL(CREATE_EVENT_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + EventContract.EventEntry.TABLE_NAME)

        onCreate(sqLiteDatabase)
    }

    companion object {

        internal val DATABASE_NAME = "events.db"

        internal val DATABASE_VERSION = 18
    }
}