package planit007.planit.home.planit007.data.Contracts;

import android.provider.BaseColumns;

/**
 * Created by Home on 2017-08-02.
 */
public class EventContract {

    public class EventEntry implements BaseColumns {

        public static final String TABLE_NAME = "events";

        public static final String COLUMN_EVENT_NAME = "eventName";

        public static final String COLUMN_EVENT_DATE = "eventDate";

        public static final String COLUMN_GROUP_ID = "groupBelongedTo";

        public static final String COLUMN_TABLE_TIMESLOT_STARTTIMES = "tStartTimes";

        public static final String COLUMN_TABLE_TIMESLOT_ENDTIMES = "tEndtimes";

        public static final String COLUMN_TABLE_JOBS = "tJobs";

        public static final String COLUMN_TABLE_JOB_QUALIFICATIONS = "tQualifications";

        public static final String COLUMN_TABLE_JOB_PREFERENCES = "tPreferences";
    }
}