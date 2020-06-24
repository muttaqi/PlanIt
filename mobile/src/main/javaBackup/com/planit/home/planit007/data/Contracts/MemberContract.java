package planit007.planit.home.planit007.data.Contracts;

import android.provider.BaseColumns;

/**
 * Created by Home on 2017-08-02.
 */

public class MemberContract {

    public class MemberEntry implements BaseColumns {

        public static final String TABLE_NAME = "members";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_GROUP_ID = "groupBelongedTo";

        public static final String COLUMN_TIMESLOT_MEMBER_AVAILABILITY = "tMemberAvailability";

        public static final String COLUMN_MEMBER_QUALIFICATIONS = "tMemberQualifications";

        public static final String COLUMN_MEMBER_PREFERENCES = "tMemberPreferences";

        public static final String COLUMN_MEMBER_AVAILABILITY = "tMemberAvailability";
    }
}
