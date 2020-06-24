package com.planit.home.planit007;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;



import planit007.planit.home.planit007.Adapters.JobAdapter;
import com.planit.home.planit007.Adapters.TimeSlotAdapter;
import com.planit.home.planit007.R;
import com.planit.home.planit007.EventContract;
import com.planit.home.planit007.data.Contracts.GroupContract;
import com.planit.home.planit007.data.Contracts.MemberContract;
import com.planit.home.planit007.data.DbHelpers.EventDbHelper;
import com.planit.home.planit007.data.DbHelpers.GroupDbHelper;
import com.planit.home.planit007.data.DbHelpers.MemberDbHelper;
import com.planit.home.planit007.data.Useful;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit007.Adapters.JobAdapter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import com.planit007.Adapters.TimeSlotAdapter;
import com.planit007.data.Contracts.EventContract;
import com.planit007.data.Contracts.GroupContract;
import com.planit007.data.Contracts.MemberContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.DbHelpers.GroupDbHelper;
import com.planit007.data.DbHelpers.MemberDbHelper;
import com.planit007.data.Useful;

import planit007.planit.home.planit007.data.Contracts.EventContract;
import planit007.planit.home.planit007.data.Contracts.GroupContract;
import planit007.planit.home.planit007.data.Contracts.MemberContract;
import planit007.planit.home.planit007.data.Useful;

public class EventDetailsActivity extends AppCompatActivity implements TimeSlotAdapter.TimeSlotAdapterOnClickHandler,JobAdapter.JobAdapterOnClickHandler {
    String eventName;
    String eventDate;
    int groupID;
    String groupName;
    int eventID;

    private String TAG = EventDetailsActivity.class.getSimpleName();

    private SQLiteDatabase mEventsDb;
    private SQLiteDatabase mGroupsDb;
    private SQLiteDatabase mMembersDb;

    private TimeSlotAdapter mTimeSlotAdapter;
    private JobAdapter mJobAdapter;

    private RecyclerView mTimeSlotList;
    private RecyclerView mJobList;

    private Cursor eventCursor;
    private Cursor memberCursor;

    private String timeSlotStartString;
    private List<String> timeSlotStarts = new ArrayList<String>();

    private String timeSlotEndString;
    private List<String> timeSlotEnds = new ArrayList<String>();

    private FloatingActionMenu famEDA;

    private TextView tvEventName;
    private TextView tvEventDate;

    private TextView txtNullTimeslot;
    private TextView txtNullJob;

    DateFormat dateFormat;

    boolean newEvent;
    private List<List<String[]>> memberUnavailability = new ArrayList<List<String[]>>();
    private List<List<String>> tempList = new ArrayList<List<String>>();
    private List<Integer> memberIDs = new ArrayList<Integer>();

    boolean hasMembers = true;

    int day;
    int month;
    int year;

    int startHours = 0;
    int startMinutes = 0;

    TimePickerFragmentEndEdit timeDialogEndEdit;
    TimePickerFragmentEndNew timeDialogEndNew;

    TimeSlotAdapter.TimeSlotAdapterOnClickHandler tsaoch;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(planit.planit.home.planit003.R.layout.activity_event_details);

        mAdView = findViewById(planit.planit.home.planit003.R.id.ed_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        Intent intent = getIntent();

        eventID = intent.getIntExtra("eventID", 0);
        groupID = intent.getIntExtra("groupID", 0);
        newEvent = intent.getBooleanExtra("isNewEvent", false);

        Log.d(TAG, "DEBUG EDA 90 " + eventID + " " + newEvent);
        Log.d(TAG, "DEBUG EDA 90 " + eventID + " " + newEvent);

        mTimeSlotList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.ed_rv_timeSlots);
        mJobList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.ed_rv_jobs);

        LinearLayoutManager timeSlotLayoutManager = new TimeSlotLinearLayoutManager(this);
        LinearLayoutManager jobLayoutManager = new LinearLayoutManager(this);

        mTimeSlotList.setLayoutManager(timeSlotLayoutManager);
        mJobList.setLayoutManager(jobLayoutManager);

        EventDbHelper eventDbHelper = new EventDbHelper(this);
        GroupDbHelper groupDbHelper = new GroupDbHelper(this);
        MemberDbHelper memberDbHelper = new MemberDbHelper(this);

        mEventsDb = eventDbHelper.getWritableDatabase();
        mGroupsDb = groupDbHelper.getReadableDatabase();
        mMembersDb = memberDbHelper.getWritableDatabase();

        tvEventName = (TextView) findViewById(planit.planit.home.planit003.R.id.ed_tv_event_name);
        tvEventDate = (TextView) findViewById(planit.planit.home.planit003.R.id.ed_tv_event_date);

        eventCursor = getEvent(eventID);

        if (eventCursor.equals(null) || !eventCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG EDA 145 EVENT CURSOR NULL");
        }

        groupName = getGroup(groupID);

        if (!newEvent) {

            eventDate = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_DATE));

            Log.d(TAG, "DEBUG EDA 109 NOT NEW EVENT, EVENT DATE AT " + eventDate);

            eventName = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_NAME));

            tvEventDate.setText(eventDate);
            tvEventName.setText(eventName);

            getTimeSlots();
        } else {

            eventName = intent.getStringExtra("eventName");

            defaultEvent();

            if (!eventCursor.moveToFirst()) {

                Log.d(TAG, "DEBUG EDA 128 EVENT CURSOR EMPTY");
            } else {

                Log.d(TAG, "DEBUG EDA 133 NOT EMPTY");
            }

            Log.d(TAG, "DEBUG EDA 173 " + eventID);

            tvEventName.setText(eventName);

            Log.d(TAG, "DEBUG EDA 144");
            tvEventDate.setText(eventDate);
        }

        Log.d(TAG, "DEBUG EDA 120 " + eventCursor.getInt(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_GROUP_ID)));

        mTimeSlotList.setHasFixedSize(true);
        mJobList.setHasFixedSize(true);

        Log.d(TAG, "DEBUG EDA 166 " + eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS)));

        tsaoch = this;
        mTimeSlotAdapter = new TimeSlotAdapter(this, eventCursor, null, tsaoch, false);
        mJobAdapter = new JobAdapter(this, eventCursor, this);

        mTimeSlotList.setAdapter(mTimeSlotAdapter);
        mJobList.setAdapter(mJobAdapter);

        txtNullTimeslot = (TextView) findViewById(planit.planit.home.planit003.R.id.ed_tv_timeslot_null);
        txtNullJob = (TextView) findViewById(planit.planit.home.planit003.R.id.ed_tv_job_null);

        updateNullView(mTimeSlotAdapter, txtNullTimeslot);
        updateNullView(mJobAdapter, txtNullJob);

        famEDA = (FloatingActionMenu) findViewById(planit.planit.home.planit003.R.id.ed_fam);

        findViewById(planit.planit.home.planit003.R.id.ed_bt_create_schedule).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (hasMembers && mTimeSlotAdapter.getItemCount() > 0 && mJobAdapter.getItemCount() > 0) {

                    Intent i = new Intent(EventDetailsActivity.this, EventScheduleActivity.class);

                    i.putExtra("groupID", groupID);
                    i.putExtra("eventID", eventID);

                    startActivity(i);
                } else if (!hasMembers) {

                    Toast.makeText(EventDetailsActivity.this, "Your group needs a member", Toast.LENGTH_SHORT).show();
                }

                else if (mTimeSlotAdapter.getItemCount() == 0) {

                    Toast.makeText(EventDetailsActivity.this, "Your event needs a timeslot", Toast.LENGTH_SHORT).show();
                }

                else if (mJobAdapter.getItemCount() == 0) {

                    Toast.makeText(EventDetailsActivity.this, "Your event needs a job", Toast.LENGTH_SHORT).show();
                }
            }
        });

        //get member info [to handle time slot deletion and delete relevant inavailabilities
        try {
            memberCursor = getMembers(groupName);

            if (memberCursor.equals(null) || !memberCursor.moveToFirst()) {

                Log.d(TAG, "DEBUG EDA 84 MEMBER CURSOR EMPTY");
            }

            int l = 0;

            //essentially event handles members, members have unavailabilities, unavailabilities hold event id and timeslot id
            //templist keeps event id and timeslot id as a string instead of splitting into array

            do {

                memberUnavailability.add(l, new ArrayList<String[]>());

                memberIDs.add(memberCursor.getInt(memberCursor.getColumnIndex(MemberContract.MemberEntry._ID)));

                String avail = memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY));
                if (!(avail == null || avail.equals(""))) {

                    Log.d(TAG, "DEBUG EDA 101 " + l);

                    tempList.add(l, new LinkedList<String>(Arrays.asList(avail.split("//"))));
                    Log.d(TAG, Useful.debugID(TAG) + tempList);
                    for (int j = 0; j < tempList.get(l).size(); j++) {

                        Log.d(TAG, "DEBUG EDA 101 " + j);

                        String[] sAvail = tempList.get(l).get(j).split("\\s+");

                        //should be bypassed
                        if (sAvail.length > 1) {

                            Log.d(TAG, Useful.debugID(TAG) + sAvail[0] + " " + eventID);

                            if (sAvail[0].equals(String.valueOf(eventID))) {

                                memberUnavailability.get(l).add(sAvail);
                                tempList.get(l).remove(j);
                            }
                        }
                    }

                    Log.d(TAG, "DEBUG EDA 101" + avail);
                } else {

                    tempList.add(l, new ArrayList<String>());
                    tempList.get(l).add("");
                }

                l++;
            }

            while (memberCursor.moveToNext());
        } catch (CursorIndexOutOfBoundsException ciobe) {

            hasMembers = false;

            ciobe.printStackTrace();
        }

        tvEventName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.ed_iv_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        tvEventName.setPaintFlags(tvEventName.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        tvEventDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(EventDetailsActivity.this);

                final CalendarView cView = (CalendarView) getLayoutInflater().inflate(planit.planit.home.planit003.R.layout.calendar, null);

                if (cView == null) {
                    Log.d(TAG, "DEBUG EDA 198");
                }

                alertDialogBuilder.setView(cView);

                List<String> dateVals = new LinkedList<String>(Arrays.asList(eventDate.split("/")));

                Log.d(TAG, "DEBUG EDA 173 " + dateVals);

                if (dateVals.get(1).length() == 1) {

                    dateVals.set(1, "0" + dateVals.get(1));
                }

                if (dateVals.get(0).length() == 1) {

                    dateVals.set(0, "0" + dateVals.get(0));
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                formatter.setLenient(false);

                long dateMilis;

                String date = dateVals.get(2) + "/" + dateVals.get(1) + "/" + dateVals.get(0);

                try {

                    dateMilis = formatter.parse(date).getTime();
                    cView.setDate(dateMilis);
                } catch (java.text.ParseException e) {

                    Log.d(TAG, "DEBUG EDA 202 " + date);

                    e.printStackTrace();
                }

                cView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

                    @Override
                    public void onSelectedDayChange(CalendarView calendarView, int calYear, int calMonth, int calDayOfMonth) {

                        day = calDayOfMonth;
                        year = calYear;
                        month = calMonth + 1;

                        Log.d(TAG, "DEBUG EDA 216 " + month);
                    }
                });

                alertDialogBuilder
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                famEDA.close(true);
                            }
                        })
                        .setPositiveButton("SET DATE", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String sDay = String.valueOf(day);
                                String sMonth = String.valueOf(month);

                                if (sDay.length() == 1) {

                                    sDay = "0" + sDay;
                                }

                                if (sMonth.length() == 1) {

                                    sMonth = "0" + sMonth;
                                }

                                String date = String.valueOf(sDay + "/" + sMonth + "/" + year);

                                ContentValues cv = new ContentValues();

                                cv.put(EventContract.EventEntry.COLUMN_EVENT_DATE, date);

                                mEventsDb.update(
                                        EventContract.EventEntry.TABLE_NAME,
                                        cv,
                                        EventContract.EventEntry._ID + "= " + String.valueOf(eventID),
                                        null
                                );

                                Log.d(TAG, "DEBUG EDA 245 " + date);

                                eventCursor = getEvent(eventID);

                                if (!eventCursor.moveToFirst() && eventCursor.equals(null)) {

                                    Log.d(TAG, "DEBUG EDA 217 EVENT CURSOR NULL");
                                }

                                eventDate = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_DATE));

                                Log.d(TAG, "DEBUG EDA 215 " + eventDate);

                                tvEventDate.setText(eventDate);

                                famEDA.close(true);
                            }
                        })

                        .setCustomTitle(findViewById(planit.planit.home.planit003.R.id.calendarTitle));

                final AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button btnPos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                        btnPos.setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                        alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                    }
                });

                alertDialog.show();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.ed_fab_add_timeslot).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                addTimeslot(view);
            }
        });

        findViewById(planit.planit.home.planit003.R.id.ed_fab_add_job).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final Useful.NameAlertDialog jobNAD = new Useful.NameAlertDialog("Job", "CREATE", "CANCEL", "", new EditText(EventDetailsActivity.this), EventDetailsActivity.this);
                jobNAD.create();
                jobNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        Button btnPos = jobNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);

                        jobNAD.setBtnColor();

                        btnPos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if (jobNAD.et.getText().toString().equals("") || jobNAD.et.getText().toString().equals(null)) {

                                    Toast t = Toast.makeText(getApplicationContext(), "Please enter an job name", Toast.LENGTH_LONG);

                                    t.show();
                                } else {

                                    Intent intent = new Intent(EventDetailsActivity.this, JobDetailsActivity.class);

                                    intent.putExtra("eventName", eventName);
                                    intent.putExtra("jobName", String.valueOf(jobNAD.et.getText()));
                                    intent.putExtra("jobID", eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//").length);
                                    intent.putExtra("groupID", groupID);
                                    intent.putExtra("eventID", eventID);

                                    startActivity(intent);

                                    famEDA.close(true);
                                }
                            }
                        });
                    }
                });

                jobNAD.ad.show();
            }
        });
    }

    private static class TimeSlotLinearLayoutManager extends LinearLayoutManager {
        /**
         * Disable predictive animations. There is a bug in RecyclerView which causes views that
         * are being reloaded to pull invalid ViewHolders from the internal recycler stack if the
         * adapter size has decreased since the ViewHolder was recycled.
         */
        @Override
        public boolean supportsPredictiveItemAnimations() {
            return false;
        }

        public TimeSlotLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        public TimeSlotLinearLayoutManager(Context context) {

            super(context);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(planit.planit.home.planit003.R.menu.save_delete_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Log.d(TAG, "DEBUG EDA 368 " + item.getItemId() + " delete id is " + planit.planit.home.planit003.R.id.delete_button + " save id is " + planit.planit.home.planit003.R.id.save_button);

        if(item.getItemId() == planit.planit.home.planit003.R.id.delete_button) {

            AlertDialog ad = eventDeletePrompt();
            ad.show();

            Log.d(TAG, "DEBUG EDA 436");
        }

        else if (item.getItemId() == planit.planit.home.planit003.R.id.save_button) {
            if (eventCursor != null && eventCursor.moveToFirst()) {

                ContentValues cv = new ContentValues();

                eventName = String.valueOf(tvEventName.getText());

                Log.d(TAG, "DEBUG EDA 384 " + eventName + " ID " + eventID);

                cv.put(EventContract.EventEntry.COLUMN_EVENT_NAME, eventName);
                storeTimeSlots();

                mEventsDb.update(
                        EventContract.EventEntry.TABLE_NAME,
                        cv,
                        EventContract.EventEntry._ID + " = " + eventID,
                        null);


                Intent i = new Intent(EventDetailsActivity.this, MainActivity.class);

                startActivity(i);
            } else {

                Log.d(TAG, "DEBUG EDA 378 EVENT CURSOR NULL");
            }

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(String timeSlotStart, final String timeSlotEnd, final int timeSlotID, View view, CheckBox checkBox) {

        if (view.getId() == planit.planit.home.planit003.R.id.ib_item_timeslot) {

            Log.d(TAG, "DEBUG ONCLICK TSAD " + timeSlotID);

            //remove inavailabilities

            for (int i = 0; i < memberUnavailability.size(); i ++) {

                for (int j = 0; j < memberUnavailability.get(i).size(); j ++) {

                    if (memberUnavailability.get(i).get(j)[1].equals(String.valueOf(timeSlotID))) {

                        memberUnavailability.get(i).remove(j);

                        for (int k = 0; k < memberUnavailability.get(i).size(); k ++) {

                            String[] sa = memberUnavailability.get(i).get(k);

                            if (Integer.valueOf(sa[1]) > timeSlotID) {

                                int saInt = Integer.valueOf(sa[1]) - 1;
                                memberUnavailability.get(i).set(k, new String[]{sa[0], String.valueOf(saInt)});
                            }
                        }
                    }
                }
            }

            Toast.makeText(EventDetailsActivity.this, "Removed " + timeSlotStarts.get((int) (timeSlotID)) + " and " + timeSlotEnds.get((int) (timeSlotID)), Toast.LENGTH_SHORT).show();

            timeSlotStarts.remove(timeSlotID);
            timeSlotEnds.remove(timeSlotID);

            storeTimeSlots();

            eventCursor = getEvent(eventID);

            if (eventCursor.moveToFirst()) {
                Log.d(TAG, "DEBUG EDA 563 " + String.valueOf(eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES))));
            }

            mTimeSlotAdapter = new TimeSlotAdapter(EventDetailsActivity.this, eventCursor, null, EventDetailsActivity.this, false);

            mTimeSlotList.setAdapter(mTimeSlotAdapter);

            Log.d(TAG, "DEBUG");

            mTimeSlotAdapter.notifyItemRangeChanged(timeSlotID, timeSlotStarts.size());

            updateNullView(mTimeSlotAdapter, txtNullTimeslot);
        }

        else {

            TimePickerFragmentStartEdit timeDialogStart = new TimePickerFragmentStartEdit();
            timeDialogStart.show(getFragmentManager(), "Time Picker");
        }
    }

    @Override
    public void onClick(String job, int jobID) {

            Intent i = new Intent(EventDetailsActivity.this, JobDetailsActivity.class);

            i.putExtra("jobName", job);
            i.putExtra("jobID", jobID);
            i.putExtra("eventName", eventName);
            i.putExtra("eventID", eventID);
            i.putExtra("groupID", groupID);

            Log.d(TAG, "DEBUG EDA 338 " + jobID);

            storeTimeSlots();

            startActivity(i);
    }

    @Override
    protected void onStop() {

        Log.d(TAG, "DEBUG EDA 479");

        storeTimeSlots();

        super.onStop();
    }

    public void getTimeSlots() {

        timeSlotStartString = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES));
        timeSlotEndString = eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES));

        Log.d(TAG, "DEBUG EDA 766 " + timeSlotStartString);

        timeSlotStarts = new LinkedList<String>(Arrays.asList(timeSlotStartString.split("\\|\\|")));
        timeSlotEnds = new LinkedList<String>(Arrays.asList(timeSlotEndString.split("\\|\\|")));

        boolean tryAgain = false;

        do {

            tryAgain = false;

            for (int i = 0; i < timeSlotStarts.size() - 1; i ++) {

                Log.d(TAG, "DEBUG EDA 66 " + timeSlotStarts.size());

                if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2))) {

                    Log.d(TAG, "DEBUG EDA 70 " + i);

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

    public int storeTimeSlots() {

        timeSlotStarts.remove("");
        timeSlotEnds.remove("");

        Log.d(TAG, "DEBUG EDA 203 " + timeSlotStarts);

        timeSlotStartString = "";
        timeSlotEndString = "";

        boolean tryAgain = false;

        //sort
        do {

            tryAgain = false;

            for (int i = 0; i < timeSlotStarts.size() - 1; i ++) {

                Log.d(TAG, "DEBUG EDA 815 " + timeSlotStarts.size());

                if (Integer.valueOf(timeSlotStarts.get(i).substring(0, 2)) > Integer.valueOf(timeSlotStarts.get(i + 1).substring(0, 2))) {

                    Log.d(TAG, "DEBUG EDA 819 " + i);

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

        Log.d(TAG, "DEBUG EDA 825 " + timeSlotStarts.size());

        if (timeSlotStarts.size() > 0 && timeSlotEnds.size() > 0) {
            for (String s : timeSlotStarts) {

                timeSlotStartString += s + "||";
            }

            for (String s : timeSlotEnds) {

                timeSlotEndString += s + "||";
            }

            timeSlotStartString = timeSlotStartString.substring(0, timeSlotStartString.length() - 2);
            timeSlotEndString = timeSlotEndString.substring(0, timeSlotEndString.length() - 2);
        }

        ContentValues cv = new ContentValues();

        Log.d(TAG, "DEBUG EDA 516 " + timeSlotStartString + " END " + timeSlotEndString);

        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES, timeSlotStartString);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES, timeSlotEndString);

        return mEventsDb.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry._ID + "= " + String.valueOf(eventID),
                null
        );
    }

    public void defaultEvent() {

        ContentValues cv = new ContentValues();

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date = new Date();
        eventDate = formatter.format(date);

        cv.put(EventContract.EventEntry.COLUMN_EVENT_NAME, eventName);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, "Default Job");
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES, "10:30");
        cv.put(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_ENDTIMES, "12:30");
        cv.put(EventContract.EventEntry.COLUMN_EVENT_DATE, eventDate);
        cv.put(EventContract.EventEntry.COLUMN_GROUP_ID, groupID);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, "");
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, "");

        Log.d(TAG, "DEBUG EDA 933 " + cv.getAsString(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES));

        timeSlotStarts.add("10:30");
        timeSlotEnds.add("12:30");

        eventID = (int) mEventsDb.insertOrThrow(EventContract.EventEntry.TABLE_NAME, null, cv);

        eventCursor = mEventsDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry._ID + " = " + eventID,
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        );
    }

    public Cursor getEvent(int eventID) {

        Cursor cursor = mEventsDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry._ID + " = " + String.valueOf(eventID),
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        );

        if (cursor.equals(null) || !cursor.moveToFirst()) {

            Log.d(TAG, "DEBUG EDA 849");
        }

        return cursor;
    }

    public String getGroup(int groupID) {

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

            Log.d(TAG, "DEBUG EDA 463 CURSOR EMPTY WITH GROUP " + groupID);
        }

        Log.d(TAG, "DEBUG EDA 497 " + groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME)));

        return groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME));
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
    }

    public AlertDialog eventDeletePrompt() {

        Log.d(TAG, "DEBUG EDA 639");

        final AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Log.d(TAG, "DEBUG EDA 651");
                    }
                })
                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        deleteEventExitToMain();
                    }
                })
                .setTitle("Are you sure you would like to delete this event?");

        Log.d(TAG, "DEBUG EDA 668");

        final AlertDialog retPrompt = adb.create();

        retPrompt.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                retPrompt.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                retPrompt.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
            }
        });

        return retPrompt;
    }

    public void deleteEventExitToMain() {

        removeEvent(eventDate);

        Intent intent = new Intent(EventDetailsActivity.this, MainActivity.class);
        startActivity(intent);

        Log.d(TAG, "DEBUG EDA 665");
    }

    public boolean removeEvent(String eventDate) {

        return mEventsDb.delete(EventContract.EventEntry.TABLE_NAME, EventContract.EventEntry._ID + "=" + eventID, null) > 0;
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

    @Override
    protected void onPause() {

        super.onPause();

        //concat all info

        for (int i = 0; i < memberIDs.size(); i ++ ) {

            int memberID = memberIDs.get(i);

            String ma = "";

            for (String s : tempList.get(i)) {

                ma += s + "//";
            }

            for (String[] sa : memberUnavailability.get(i)) {

                ma += sa[0] + " " + sa[1] + "//";
            }

            ContentValues cv = new ContentValues();
            cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, ma);

            mMembersDb.update(
                    MemberContract.MemberEntry.TABLE_NAME,
                    cv,
                    MemberContract.MemberEntry._ID + " = " + memberID,
                    null
            );
        }
    }

    public void updateNullView(RecyclerView.Adapter adapter, TextView textView) {

        if (adapter.getItemCount() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }

    public void addTimeslot(View view) {

        final View v = view;

        startHours = 0;
        startMinutes = 0;

        TimePickerFragmentStartNew timeDialogStart = new TimePickerFragmentStartNew();
        timeDialogStart.show(getFragmentManager(), "Time Picker");
    }

    public static class TimePickerFragmentStartEdit extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        int minute = 0;
        int hour = 0;
        EventDetailsActivity EDA;

        TimePickerDialog tpd;

        int timeSlotID;

        public void setTimes(int hr, int min) {

            this.hour = hr;
            this.minute = min;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            EDA = ((EventDetailsActivity) getActivity());

        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = new TimePickerDialog(getActivity(),
                    planit.planit.home.planit003.R.style.Theme_Dialog,this,hour,minute,true);

            // Return the TimePickerDialog
            return tpd;
        }

        public void onTimeSet(TimePicker view, int hours, int minutes){

            Log.d(EDA.TAG, "DEBUG EDA 447a " + EDA.timeSlotStarts);

            EDA.timeDialogEndEdit = new TimePickerFragmentEndEdit();
            EDA.timeDialogEndEdit.setTimeSlotID(timeSlotID);
            EDA.timeDialogEndEdit.setStartHour(hours);
            EDA.timeDialogEndEdit.setStartMinute(minutes);

            EDA.timeDialogEndEdit.show(getFragmentManager(), "Time Picker");
        }
    }

    public static class TimePickerFragmentEndEdit extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        int minute = 0;
        int hour = 0;
        EventDetailsActivity EDA;

        TimePickerDialog tpd;

        int timeSlotID;

        int startHour;
        int startMinute;

        public void setTimes(int hr, int min) {

            this.hour = hr;
            this.minute = min;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            EDA = ((EventDetailsActivity) getActivity());

        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = new TimePickerDialog(getActivity(),
                    planit.planit.home.planit003.R.style.Theme_Dialog,this,hour,minute,true);

            // Return the TimePickerDialog
            return tpd;
        }

        public void setTimeSlotID(int timeSlotID) {
            this.timeSlotID = timeSlotID;
        }

        public void setStartHour(int startHour) {
            this.startHour = startHour;
        }

        public void setStartMinute(int startMinute) {
            this.startMinute = startMinute;
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute){

            if(hourOfDay < startHour || (hourOfDay == startHour && minute <= startMinute)) {

                Toast.makeText(EDA.getApplicationContext(), "The timeslot must end after it starts", Toast.LENGTH_SHORT).show();
            }

            else {
                //format hour:minute string
                String hoursString = String.valueOf(startHour);
                String minutesString = String.valueOf(startMinute);

                Log.d(EDA.TAG, "DEBUG EDA 435a " + startHour + " " + startMinute + " " + String.valueOf(startMinute).length());

                if (hoursString.length() == 1) {

                    hoursString = "0" + hoursString;
                }

                if (minutesString.length() == 1) {

                    minutesString = "0" + minutesString;
                }

                EDA.timeSlotStarts.set(timeSlotID, hoursString + ":" + minutesString);

                hoursString = String.valueOf(hourOfDay);
                minutesString = String.valueOf(minute);

                Log.d(EDA.TAG, "DEBUG EDA 435b " + hourOfDay + " " + String.valueOf(hourOfDay).length() + " " + minute + " " + String.valueOf(minute).length());

                if (hoursString.length() == 1) {

                    hoursString = "0" + hoursString;
                }

                if (minutesString.length() == 1) {

                    minutesString = "0" + minutesString;
                }

                Log.d(EDA.TAG, "DEBUG EDA 447b " + hoursString + " " + minutesString);

                //set on list and store
                EDA.timeSlotEnds.set(timeSlotID, hoursString + ":" + minutesString);

                Log.d(EDA.TAG, "DEBUG EDA 1187 " + EDA.timeSlotEnds);

                EDA.storeTimeSlots();

                EDA.eventCursor = EDA.getEvent(EDA.eventID);

                if (EDA.eventCursor.moveToFirst()) {
                    Log.d(EDA.TAG, "DEBUG EDA 442 " + String.valueOf(EDA.eventCursor.getString(EDA.eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES))));
                }

                //reset/update adapter and null view
                EDA.mTimeSlotAdapter = new TimeSlotAdapter(EDA.getApplicationContext(), EDA.eventCursor, null, EDA.tsaoch, false);

                EDA.mTimeSlotList.setAdapter(EDA.mTimeSlotAdapter);

                if (EDA.mTimeSlotList != null) {

                    EDA.mTimeSlotList.invalidate();
                }

                EDA.mTimeSlotAdapter.notifyDataSetChanged();

                EDA.updateNullView(EDA.mTimeSlotAdapter, EDA.txtNullTimeslot);
            }
        }
    }

    public static class TimePickerFragmentStartNew extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        int minute = 0;
        int hour = 0;
        EventDetailsActivity EDA;

        TimePickerDialog tpd;

        int timeSlotID;

        public void setTimes(int hr, int min) {

            this.hour = hr;
            this.minute = min;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            EDA = ((EventDetailsActivity) getActivity());

        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = new TimePickerDialog(getActivity(),
                    planit.planit.home.planit003.R.style.Theme_Dialog,this,hour,minute,true);

            // Return the TimePickerDialog
            return tpd;
        }

        public void onTimeSet(TimePicker view, int hours, int minutes){

            Log.d(EDA.TAG, "DEBUG EDA 447a " + EDA.timeSlotStarts);

            EDA.timeDialogEndNew = new TimePickerFragmentEndNew();
            EDA.timeDialogEndNew.setTimeSlotID(timeSlotID);
            EDA.timeDialogEndNew.setStartHour(hours);
            EDA.timeDialogEndNew.setStartMinute(minutes);

            EDA.timeDialogEndNew.show(getFragmentManager(), "Time Picker");
        }
    }

    public static class TimePickerFragmentEndNew extends DialogFragment implements TimePickerDialog.OnTimeSetListener{
        int minute = 0;
        int hour = 0;
        EventDetailsActivity EDA;

        TimePickerDialog tpd;

        int timeSlotID;

        int startHour;
        int startMinute;

        public void setTimes(int hr, int min) {

            this.hour = hr;
            this.minute = min;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){

            EDA = ((EventDetailsActivity) getActivity());

        /*
            Creates a new time picker dialog with the specified theme.

                TimePickerDialog(Context context, int themeResId,
                    TimePickerDialog.OnTimeSetListener listener,
                    int hourOfDay, int minute, boolean is24HourView)
         */

            // TimePickerDialog Theme : THEME_DEVICE_DEFAULT_LIGHT
            tpd = new TimePickerDialog(getActivity(),
                    planit.planit.home.planit003.R.style.Theme_Dialog,this,hour,minute,true);

            // Return the TimePickerDialog
            return tpd;
        }

        public void setTimeSlotID(int timeSlotID) {
            this.timeSlotID = timeSlotID;
        }

        public void setStartHour(int startHour) {
            this.startHour = startHour;
        }

        public void setStartMinute(int startMinute) {
            this.startMinute = startMinute;
        }

        public void onTimeSet(TimePicker view, int hours, int minutes){

            if (hours > startHour || (hours == startHour && minutes > startMinute)) {

                String startHoursString = String.valueOf(startHour);
                String startMinutesString = String.valueOf(startMinute);

                Log.d(EDA.TAG, "DEBUG EDA 435 " + hours + " " + String.valueOf(hours).length() + " " + minutes + " " + String.valueOf(minutes).length());

                if(startHoursString.length() == 1) {

                    startHoursString = "0" + startHoursString;
                }

                if(startMinutesString.length() == 1) {

                    startMinutesString = "0" + startMinutesString;
                }

                Log.d(EDA.TAG, "DEBUG EDA 447 " + startHoursString + " " + startMinutesString);

                EDA.timeSlotStarts.add(startHoursString + ":" + startMinutesString);

                String endHoursString = String.valueOf(hours);
                String endMinutesString = String.valueOf(minutes);

                Log.d(EDA.TAG, "DEBUG EDA 435 " + hours + " " + String.valueOf(hours).length() + " " + minutes + " " + String.valueOf(minutes).length());

                if(endHoursString.length() == 1) {

                    endHoursString = "0" + endHoursString;
                }

                if(endMinutesString.length() == 1) {

                    endMinutesString = "0" + endMinutesString;
                }

                Log.d(EDA.TAG, "DEBUG EDA 447 " + EDA.timeSlotStarts);

                EDA.timeSlotEnds.add(endHoursString + ":" + endMinutesString);

                Log.d(EDA.TAG, "DEBUG EDA 1356 " + EDA.timeSlotEnds);

                EDA.storeTimeSlots();

                EDA.eventCursor = EDA.getEvent(EDA.eventID);

                if (EDA.eventCursor.moveToFirst()) {
                    Log.d(EDA.TAG, "DEBUG EDA 442 " + String.valueOf(EDA.eventCursor.getString(EDA.eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_TIMESLOT_STARTTIMES))));
                }

                EDA.mTimeSlotAdapter = new TimeSlotAdapter(EDA.getApplicationContext(), EDA.eventCursor, null, EDA.tsaoch, false);

                EDA.mTimeSlotList.setAdapter(EDA.mTimeSlotAdapter);

                if (EDA.mTimeSlotList != null) {

                    EDA.mTimeSlotList.invalidate();
                }

                EDA.mTimeSlotAdapter.notifyDataSetChanged();

                EDA.updateNullView(EDA.mTimeSlotAdapter, EDA.txtNullTimeslot);

                EDA.famEDA.close(true);
            }

            else {

                Toast.makeText(EDA.getApplicationContext(), "The timeslot must end after it begins", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void nameOnClick() {

        famEDA.close(true);

        final Useful.NameAlertDialog eventNAD = new Useful.NameAlertDialog("Event", "DONE", "CANCEL", tvEventName.getText().toString(), new EditText(EventDetailsActivity.this), EventDetailsActivity.this);
        eventNAD.create();
        eventNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                eventNAD.setBtnColor();
                eventNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        eventNAD.ad.dismiss();

                        eventName = String.valueOf(eventNAD.et.getText());
                        tvEventName.setText(eventName);
                    }
                });
            }
        });

        eventNAD.ad.show();
    }
}
