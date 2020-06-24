package com.planit.mobile.data.DbHelpers

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.planit.mobile.data.Contracts.GroupContract

/**
 * Created by Home on 2017-08-02.
 */

class GroupDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {

        val CREATE_GROUP_TABLE = "CREATE TABLE " +
                GroupContract.GroupEntry.TABLE_NAME + " (" +
                GroupContract.GroupEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                GroupContract.GroupEntry.COLUMN_GROUP_NAME + " TEXT NOT NULL " +
                ");"

        sqLiteDatabase.execSQL(CREATE_GROUP_TABLE)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + GroupContract.GroupEntry.TABLE_NAME)

        onCreate(sqLiteDatabase)
    }

    companion object {

        internal val DATABASE_NAME = "groups.db"

        internal val DATABASE_VERSION = 16
    }
}