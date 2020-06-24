package planit007.planit.home.planit007;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.evrencoskun.tableview.TableView;
import com.planit.mobile.Adapters.ScheduleTableAdapter;
import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.data.Contracts.MemberContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.DbHelpers.GroupDbHelper;
import com.planit.mobile.data.DbHelpers.MemberDbHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.planit007.Adapters.ScheduleTableAdapter;
import com.planit007.data.Contracts.EventContract;
import com.planit007.data.Contracts.GroupContract;
import com.planit007.data.Contracts.MemberContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.DbHelpers.GroupDbHelper;
import com.planit007.data.DbHelpers.MemberDbHelper;

import planit007.planit.home.planit007.data.Contracts.EventContract;
import planit007.planit.home.planit007.data.Contracts.GroupContract;
import planit007.planit.home.planit007.data.Contracts.MemberContract;
import planit007.planit.home.planit007.data.DbHelpers.GroupDbHelper;
import planit007.planit.home.planit007.data.DbHelpers.MemberDbHelper;

public class EventScheduleActivity extends AppCompatActivity {

    private String TAG = EventScheduleActivity.class.getSimpleName();

    private SQLiteDatabase mEventsDb;
    private SQLiteDatabase mGroupsDb;
    private SQLiteDatabase mMembersDb;

    private List<String> Jobs = new ArrayList<String>();
    private List<String> TimeSlots = new ArrayList<String>();

    private List<Integer> jobIDs = new ArrayList<>();

    private String timeSlotStartString;
    private List<String> timeSlotStarts = new ArrayList<String>();

    private String timeSlotEndString;
    private List<String> timeSlotEnds = new ArrayList<String>();

    private String jobString;
    private List<String> jobNames = new ArrayList<>();
    private List<Job> jobs = new ArrayList<>();

    //this
    private List<String> allQuals;
    private List<String> allPrefs;
    //becomes this
    private List<String> jobQuals;
    private List<String> jobPrefs;
    //which becomes this
    /*private List<List<String>> allJobQuals = new ArrayList<List<String>>();
    private List<List<String>> allJobPrefs = new ArrayList<List<String>>();*/

    private List<Integer> numQuals = new ArrayList<>();

    private int eventID;
    private int groupID;
    private Cursor eventCursor;
    private Cursor groupCursor;

    private TableView mScheduleTable;

    List<Member> members = new ArrayList<>();

    List<List<String>> tempList = new ArrayList<List<String>>();
    //list.get(member)(specific unavailability)[specific item*] *(event id : [0], etc.)

    private AdView mAdView;

    public static class Job {

        private List<String> quals = new ArrayList<>();
        private List<String> prefs = new ArrayList<>();

        private int id;
        private String name;

        public Job (String name) {

            this.setName(name);
        }

        public Job (Job j) {

            this.id = j.id;
            this.name = j.name;
            this.prefs = j.prefs;
            this.quals = j.quals;
        }

        public List<String> getQuals() {
            return quals;
        }

        public void setQuals(List<String> quals) {
            this.quals = quals;
        }

        public List<String> getPrefs() {
            return prefs;
        }

        public void setPrefs(List<String> prefs) {
            this.prefs = prefs;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Member {

        //stores unavailabilities based on timeslot ids
        private List<Integer> unavailability = new ArrayList<>();

        private String name;
        private int id = 0;

        private List<String> quals = new ArrayList<>();
        private List<String> prefs = new ArrayList<>();

        //updates as schedule is being created (set to false when member is put on a shift, resets after every timeslot)
        private boolean available = true;
        //keeps track of total shifts worked in event (IMPLEMENT HOUR CALCULATION IN FUTURE!!!)
        private int business = 0;

        public Member(String name) {

            this.setName(name);
        }

        public List<Integer> getUnavailability() {
            return unavailability;
        }

        public void setUnavailability(List<Integer> unavailability) {
            this.unavailability = unavailability;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public List<String> getQuals() {
            return quals;
        }

        public void setQuals(List<String> quals) {
            this.quals = quals;
        }

        public List<String> getPrefs() {
            return prefs;
        }

        public void setPrefs(List<String> prefs) {
            this.prefs = prefs;
        }

        public boolean isAvailable() {
            return available;
        }

        public void setAvailable(boolean available) {
            this.available = available;
        }

        public int getBusiness() {
            return business;
        }

        public void setBusiness(int business) {
            this.business = business;
        }
    }

    public static class sortJobsByNumQualsDescending implements Comparator<Job> {
        @Override
        public int compare(Job j1, Job j2) {
            return j2.getQuals().size() - j1.getQuals().size();
        }
    };

    public static class sortMembersByBusinessAscending implements Comparator<Member> {
        @Override
        public int compare(Member m1, Member m2) {
            return m1.getBusiness() - m2.getBusiness();
        }
    };

    Job noQualJob = new Job("");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(planit.planit.home.planit003.R.layout.activity_event_schedule);

        mAdView = findViewById(planit.planit.home.planit003.R.id.es_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent i = getIntent();

        eventID = i.getIntExtra("eventID", 0);
        groupID = i.getIntExtra("groupID", 0);

        EventDbHelper eventDbHelper = new EventDbHelper(this);
        GroupDbHelper groupDbHelper = new GroupDbHelper(this);
        MemberDbHelper memberDbHelper = new MemberDbHelper(this);

        mEventsDb = eventDbHelper.getWritableDatabase();
        mGroupsDb = groupDbHelper.getReadableDatabase();
        mMembersDb = memberDbHelper.getReadableDatabase();

        eventCursor = getEvent(eventID);
        groupCursor = getGroup(groupID);

        String groupName = groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME));

        Cursor memberCursor = getMembers(groupName);

        if(memberCursor.equals(null) || !memberCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG ESA 84 MEMBER CURSOR EMPTY");
        }

        int l = 0;

        //this shit too complicated to comment; essentially event handles members, members have unavailabilities, unavailabilities hold event id and timeslot id

        do {
            Member m = new Member(memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_NAME)));

            Log.d(TAG, "DEBUG ESA 131 " + memberCursor.getInt(memberCursor.getColumnIndex(MemberContract.MemberEntry._ID)));
            m.setId(memberCursor.getInt(memberCursor.getColumnIndex(MemberContract.MemberEntry._ID)));

            List<Integer> memberUnavailability = new ArrayList<>();

            m.setQuals(new LinkedList<String>(Arrays.asList(memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS)).split("\\|\\|"))));
            m.setPrefs(new LinkedList<String>(Arrays.asList(memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES)).split("\\|\\|"))));

            String avail = memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY));
            if (!(avail == null || avail.equals(""))) {

                tempList.add(l, new LinkedList<String>(Arrays.asList(avail.split("//"))));
                Log.d(TAG, "DEBUG ESA 123a " + tempList.get(l));
                for (int j = 0; j < tempList.get(l).size();) {

                    String[] sAvail = tempList.get(l).get(j).split("\\s+");

                    Log.d(TAG, "DEBUG ESA 123b " + sAvail[0] + " " + eventID);

                    if (sAvail.length > 1) {

                        if (sAvail[0].equals(String.valueOf(eventID))) {

                            memberUnavailability.add(Integer.valueOf(sAvail[1]));
                            tempList.get(l).remove(j);
                        }

                        else {

                            j++;
                        }
                    }

                    else {

                        j++;
                    }
                }

                Log.d(TAG, "DEBUG ESA 101 " + avail);
            }

            else {

                tempList.add(l, new ArrayList<String>());
                tempList.get(l).add("");
            }

            Log.d(TAG, "DEBUG ESA 132 " + l + " " + memberUnavailability.size());

            m.setUnavailability(memberUnavailability);

            members.add(m);

            l++;
        }

        while (memberCursor.moveToNext());

        if (eventCursor.equals(null) || !eventCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG ESA 70");
        }

        if (groupCursor.equals(null) || !groupCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG ESA 75");
        }

        mScheduleTable = (TableView) findViewById(planit.planit.home.planit003.R.id.es_table_schedule);

        ScheduleTableAdapter adapter = new ScheduleTableAdapter(this);

        mScheduleTable.setAdapter(adapter);

        Log.d(TAG, "DEBUG ESA 201 " + allQuals);

        getTimeSlots();

        for (int j = 0; j < timeSlotStarts.size(); j ++) {

            TimeSlots.add(timeSlotStarts.get(j) + "-" + timeSlotEnds.get(j));
        }

        jobString = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS));
        jobNames = new LinkedList<String>(Arrays.asList(jobString.split("\\|\\|")));

        for (int j = 0; j < jobNames.size(); j ++) {

            if (jobNames.get(j).equals("")) {

                jobNames.remove(j);
            }
        }

        Jobs = jobNames;

        allQuals = new LinkedList<String>(Arrays.asList(eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//")));
        allPrefs= new LinkedList<String>(Arrays.asList(eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES)).split("//")));

        for(int j = 0; j < jobNames.size(); j ++) {

            Job job = new Job(jobNames.get(j));
            job.setId(j);

            getJobQualsAndPrefs(j);
            job.setQuals(jobQuals);
            job.setPrefs(jobPrefs);

            jobs.add(job);
        }

        Log.d(TAG, "DEBUG ESA 227 " + allQuals);

        //Creation of actual schedule; members placed in a two dimensional array list based on their positioning in the lost

        String[][] memberArray = new String[timeSlotStarts.size()][jobs.size()];

        Collections.sort(jobs, new sortJobsByNumQualsDescending());

        for (int t = 0; t < timeSlotStarts.size(); t ++) {

            //reset memberAvailable to be true
            for (Member m : members) {

                m.setAvailable(true);
            }

            Log.d(TAG, "DEBUG ESA 253 " + jobs);

            for (int j = 0; j < jobs.size(); j ++) {

                Log.d(TAG, "DEBUG ESA 214a " + jobs.get(j).getName() + "/" + jobs.size());

                //sort to minimize overworking
                Collections.sort(members, new sortMembersByBusinessAscending());

                Log.d(TAG, "DEBUG ESA 166" + members);

                boolean done = false;

                for(int m = 0; m < members.size(); m ++) {

                    Log.d(TAG, "DEBUG ESA 245 " + jobs.size());
                    //p is used to check if anyone has all preferences and then all - 1, etc.
                    for(int p = jobs.get(j).getPrefs().size(); p >= 0; p -= 1) {

                        Log.d(TAG, "DEBUG ESA 248 " + done);
                        if (!done) {

                            Log.d(TAG, "DEBUG ESA 168 " + members.get(m).isAvailable());

                            Log.d(TAG, "DEBUG ESA 179a availability? : " + members.get(m).isAvailable());
                            Log.d(TAG, "DEBUG ESA 179b check? : " + timeSlotEnds.get(t) + " " + done);
                            Log.d(TAG, "DEBUG EsA 232 " + m + "/" + members.size());

                            boolean unavailable = false;

                            Log.d(TAG, "DEBUG ESA 370a " + t + " " + m + " " + members.get(m).getName() + " " + members.get(m).getUnavailability().size());

                            //check timeslot availability
                            for (int tsID : members.get(m).getUnavailability()) {

                                Log.d(TAG, "DEBUG ESA 370b " + tsID + " " + t);

                                if (tsID == t) {

                                    unavailable = true;
                                    Log.d(TAG, "DEBUG ESA 370c " + t + " " + members.get(m) + " " + unavailable);
                                }
                            }

                            //then check qualification and ability
                            List<Boolean> hasQual = new ArrayList<Boolean>();
                            for (String jQ : jobs.get(j).getQuals()) {

                                if(!(jQ.equals("_") || jQ.equals(null) || jQ.equals(""))){

                                    Log.d(TAG, "DEBUG ESA 328 " + jQ);
                                    hasQual.add(false);
                                    for (String mQ : members.get(m).getQuals()) {

                                        if (mQ.toLowerCase().equals(jQ.toLowerCase())) {

                                            hasQual.set(hasQual.size() - 1, true);
                                        }
                                    }
                                }
                            }

                            for (int q = 0; q < hasQual.size(); q ++) {

                                Boolean b = hasQual.get(q);
                                if (!b) {

                                    Log.d(TAG, "DEBUG ESA 343 " + unavailable);
                                    unavailable = true;

                                    if (noQualJob.getName().equals("")) {

                                        noQualJob = new Job(jobs.get(j));

                                        List<String> theQual = new ArrayList<String>();
                                        theQual.add(jobs.get(j).getQuals().get(q));
                                        noQualJob.setQuals(theQual);
                                    }
                                }
                            }

                            //then preferences
                            int prefCount = 0;
                            for (String jP : jobs.get(j).getPrefs()) {

                                if(!(jP.equals("_") || jP.equals(null) || jP.equals(""))) {

                                    for (String mP : members.get(m).getPrefs()) {

                                        if (mP.equals(jP)) {

                                            prefCount++;
                                        }
                                    }
                                }
                            }

                            if(prefCount < p) {

                                Log.d(TAG, "DEBUG ESA 308 " + prefCount + " < " + p);

                                unavailable = true;
                            }

                            Log.d(TAG, "DEBUG ESA 370d " + t + " " + members.get(m).getName() + " " + unavailable);

                            if (!unavailable && members.get(m).isAvailable()) {

                                Log.d(TAG, "DEBUG ESA 192 " + timeSlotStarts.get(t) + " " + timeSlotEnds.get(t));

                                memberArray[t][j] = members.get(m).getName();
                                int mb = members.get(m).getBusiness();
                                members.get(m).setBusiness(mb + 1);
                                members.get(m).setAvailable(false);

                                Log.d(TAG, "DEBUG ESA 370e " + t + " " + members.get(m) + " " + unavailable + " " + t + " " + j + " " + members.get(m).getBusiness());

                                done = true;
                            }
                        }
                    }
                }
            }
        }

        for(String[] s : memberArray) {

            for (String st : s) {

                Log.d(TAG, "DEBUG ESA 174 " + st);
            }
        }

        List<List<String>> Members = new ArrayList<List<String>>();

        for (int j = 0; j < timeSlotStarts.size(); j ++) {

            List<String> list = new ArrayList<String>();

            for (int k = 0; k < jobs.size(); k ++) {

                list.add(memberArray[j][k]);
            }

            Members.add(list);
        }

        Log.d(TAG, "DEBUG ESA 234" + TimeSlots);

        adapter.setAllItems(Jobs, TimeSlots, Members);

        boolean done = false;
        for (int j = 0; j < Members.size(); j ++) {

            for (int k = 0; k < Members.get(j).size(); k ++) {

                if (Members.get(j).get(k) == (null) || Members.get(j).get(k).equals("")) {

                    final AlertDialog ad;
                    AlertDialog.Builder adb = new AlertDialog.Builder(this);

                    final TextView tv = new TextView(this);

                    if (!noQualJob.getName().equals("")) {

                        tv.setText("You don't have enough members with the qualification '" + noQualJob.getQuals().get(0) + "'!");
                    }

                    else {

                        tv.setText("You don't have enough members to handle this event!");
                    }
                    tv.setPadding(50, 0, 15, 0);

                    adb.setView(tv).setTitle("Error").setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {


                        }
                    });

                    ad = adb.create();

                    ad.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                        }
                    });

                    ad.show();

                    done = true;
                    break;
                }

                if (done) {

                    break;
                }
            }

            if (done) {

                break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater mi = getMenuInflater();
        mi.inflate(planit.planit.home.planit003.R.menu.back_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case planit.planit.home.planit003.R.id.back_button:

                Intent i = new Intent(EventScheduleActivity.this, EventDetailsActivity.class);
                i.putExtra("eventID", eventID);
                i.putExtra("groupID", groupID);

                startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public Cursor getEvent(int eventID) {

        return mEventsDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry._ID + " = " + String.valueOf(eventID),
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        );
    }

    public Cursor getGroup(int groupID) {

        Cursor groupCursor = mGroupsDb.query(
                GroupContract.GroupEntry.TABLE_NAME,
                null,
                GroupContract.GroupEntry._ID + " = " + String.valueOf(groupID),
                null,
                null,
                null,
                null
        );

        if(!groupCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG ESA 463 CURSOR EMPTY WITH GROUP " + groupID);
        }

        Log.d(TAG, "DEBUG ESA 497 " + groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME)));

        return groupCursor;
    }

    public void getTimeSlots() {

        timeSlotStartString = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES));
        timeSlotEndString = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES));

        timeSlotStarts = new LinkedList<String>(Arrays.asList(timeSlotStartString.split("\\|\\|")));
        timeSlotEnds = new LinkedList<String>(Arrays.asList(timeSlotEndString.split("\\|\\|")));

        Log.d(TAG, "DEBUG ESA 146 " + timeSlotStarts);

        boolean tryAgain = false;

        do {

            tryAgain = false;

            for (int i = 0; i < timeSlotStarts.size() - 1; i ++) {

                Log.d(TAG, "DEBUG ESA 66 " + timeSlotStarts.size());

                if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2))) {

                    Log.d(TAG, "DEBUG ESA 70 " + i);

                    String a = timeSlotStarts.get(i);
                    String b = timeSlotStarts.get(i + 1);

                    String c = timeSlotEnds.get(i);
                    String d = timeSlotEnds.get(i + 1);

                    timeSlotStarts.set(i, b);
                    timeSlotStarts.set(i + 1, a);

                    timeSlotEnds.set(i, d);
                    timeSlotEnds.set(i + 1, c);

                    tryAgain = true;
                }

                else if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) == Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2)) && Integer.valueOf(timeSlotStarts.get(i).substring(3, 5)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(3, 5))) {

                    String a = timeSlotStarts.get(i);
                    String b = timeSlotStarts.get(i + 1);

                    String c = timeSlotEnds.get(i);
                    String d = timeSlotEnds.get(i + 1);

                    timeSlotStarts.set(i, b);
                    timeSlotStarts.set(i + 1, a);

                    timeSlotEnds.set(i, d);
                    timeSlotEnds.set(i + 1, c);

                    tryAgain = true;
                }
            }
        } while (tryAgain);
    }

    private Cursor getMembers(String groupName) {

        return mMembersDb.query(
                MemberContract.MemberEntry.TABLE_NAME,
                null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID,
                null,
                null,
                null,
                MemberContract.MemberEntry.COLUMN_NAME
        );
    }

    public void getJobQualsAndPrefs(int jobID) {

        do {

            if (jobID >= allQuals.size() || allQuals.size() == 0) {

                allQuals.add("_");
            }

            if (jobID >= allPrefs.size() || allQuals.size() == 0) {

                allPrefs.add("_");
            }

        } while (jobID >= allQuals.size() || jobID >= allPrefs.size() || allQuals.size() == 0 || allQuals.size() == 0);

        Log.d(TAG, "DEBUG JDA 228 id " + jobID + " BUT " + allQuals);

        jobQuals = new LinkedList<String>(Arrays.asList(allQuals.get(jobID).split("\\|\\|")));
        jobPrefs = new LinkedList<String>(Arrays.asList(allPrefs.get(jobID).split("\\|\\|")));

        Log.d(TAG, "DEBUG JDA 160 " + jobPrefs);
    }
}
