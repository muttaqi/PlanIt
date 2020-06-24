package com.planit.mobile.data.Contracts

import android.provider.BaseColumns

/**
 * Created by Home on 2017-08-02.
 */
class EventContract {

    class EventEntry : BaseColumns {
        companion object {

            val _ID = "_id"

            val TABLE_NAME = "events"

            val COLUMN_EVENT_NAME = "eventName"

            val COLUMN_EVENT_DATE = "eventDate"

            val COLUMN_GROUP_ID = "groupBelongedTo"

            val COLUMN_TABLE_TIMESLOT_STARTTIMES = "tStartTimes"

            val COLUMN_TABLE_TIMESLOT_ENDTIMES = "tEndtimes"

            val COLUMN_TABLE_JOBS = "tJobs"

            val COLUMN_TABLE_JOB_QUALIFICATIONS = "tQualifications"

            val COLUMN_TABLE_JOB_PREFERENCES = "tPreferences"

            val COLUMN_CALENDAR_ID = "calendarID"
        }
    }
}