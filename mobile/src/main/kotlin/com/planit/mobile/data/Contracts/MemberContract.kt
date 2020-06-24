package com.planit.mobile.data.Contracts

import android.provider.BaseColumns

/**
 * Created by Home on 2017-08-02.
 */

class MemberContract {

    class MemberEntry : BaseColumns {
        companion object {

            val _ID = "_id"

            val TABLE_NAME = "members"

            val COLUMN_NAME = "name"

            val COLUMN_GROUP_ID = "groupBelongedTo"

            val COLUMN_TIMESLOT_MEMBER_AVAILABILITY = "tMemberAvailability"

            val COLUMN_MEMBER_QUALIFICATIONS = "tMemberQualifications"

            val COLUMN_MEMBER_PREFERENCES = "tMemberPreferences"

            val COLUMN_MEMBER_AVAILABILITY = "tMemberAvailability"
        }
    }
}
