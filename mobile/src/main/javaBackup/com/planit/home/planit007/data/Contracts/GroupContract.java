package planit007.planit.home.planit007.data.Contracts;

import android.provider.BaseColumns;

/**
 * Created by Home on 2017-08-02.
 */

public class GroupContract {

    public class GroupEntry implements BaseColumns {

        public static final String TABLE_NAME = "groups";

        public static final String COLUMN_GROUP_NAME = "groupName";
    }
}
