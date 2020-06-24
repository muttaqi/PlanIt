package com.planit.mobile.data.Contracts

import android.provider.BaseColumns

/**
 * Created by Home on 2017-08-02.
 */

class GroupContract {

    class GroupEntry : BaseColumns {
        companion object {

            val _ID = "_id"

            val TABLE_NAME = "groups"

            val COLUMN_GROUP_NAME = "groupName"
        }
    }
}
