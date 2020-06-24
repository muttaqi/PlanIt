package planit007.planit.home.planit007;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.CheckBox;
import android.widget.TextView;

import com.planit.mobile.Adapters.JobAdapter;
import com.planit.mobile.Adapters.TimeSlotAdapter;
import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.Contracts.MemberContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.DbHelpers.MemberDbHelper;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit007.Adapters.JobAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.planit007.Adapters.TimeSlotAdapter;
import com.planit007.data.Contracts.EventContract;
import com.planit007.data.Contracts.MemberContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.DbHelpers.MemberDbHelper;

public class MemberAvailabilityActivity extends AppCompatActivity implements TimeSlotAdapter.TimeSlotAdapterOnClickHandler,JobAdapter.JobAdapterOnClickHandler {

    int eventID;
    int groupID;
    String groupName;
    String name;
    int memberID;

    SQLiteDatabase mEventsDb;
    SQLiteDatabase mMemberDb;
    Cursor memberCursor;
    Cursor eventCursor;

    private TimeSlotAdapter mTimeSlotAdapter;

    RecyclerView mTimeSlotList;

    TextView txtNullTimeSlot;

    List<String[]> memberUnavailability = new ArrayList<String[]>();
    List<String> tempList = new ArrayList<String>();

    String TAG = MemberAvailabilityActivity.class.getSimpleName();

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(planit.planit.home.planit003.R.layout.activity_member_availability);

        mAdView = findViewById(planit.planit.home.planit003.R.id.ma_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent i = getIntent();

        eventID = i.getIntExtra("eventID", 0);
        groupName = i.getStringExtra("groupName");
        groupID = i.getIntExtra("groupID", 0);
        name = i.getStringExtra("name");
        memberID = i.getIntExtra("memberID", 0);

        Log.d(TAG, "DEBUG MAA 128 " + name);

        TextView title = (TextView) findViewById(planit.planit.home.planit003.R.id.ma_tv_timeslot_title);

        mTimeSlotList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.ed_rv_timeSlots);

        EventDbHelper eventDbHelper = new EventDbHelper(this);
        MemberDbHelper memberDbHelper = new MemberDbHelper(this);

        mEventsDb = eventDbHelper.getWritableDatabase();
        mMemberDb = memberDbHelper.getWritableDatabase();

        eventCursor = getEvent(eventID);
        memberCursor = getMember();

        title.setText(name + "'s Availability For " + eventCursor.getString(eventCursor.getColumnIndex(EventContract.EventEntry.COLUMN_EVENT_NAME)));

        String avail = "";

        if (memberCursor.moveToFirst() && memberCursor != null) {

            avail = memberCursor.getString(memberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY));

            Log.d(TAG, "DEBUG MAA 89 " + avail);
        }

        else {

            Log.d(TAG, "DEBUG MAA 88 MEMBER CURSOR IS EMPTY CHECK GETMEMBER()");
        }

        Log.d(TAG, "DEBUG MAA 115 " + avail);

        if (!(avail == null || avail.equals(""))) {

            tempList = new ArrayList<String>(Arrays.asList(avail.split("//")));

            Log.d(TAG,"DEBUGE MAA 104 " + tempList.toString());
            for (int j = 0; j < tempList.size(); j++) {

                String[] sAvail = tempList.get(j).split("\\s+");

                Log.d(TAG, "DEBUG MAA 101 ");

                if (sAvail.length > 1) {

                    Log.d(TAG, "DEBUG MAA 112 " + sAvail[0] + " " + eventID);

                    if (sAvail[0].equals(String.valueOf(eventID))) {

                        memberUnavailability.add(sAvail);
                        tempList.remove(j);

                        j--;
                    }
                }
            }
        }

        else {

            tempList.add(0, "");
        }

        Log.d(TAG, "DEBUG MAA 176 " + memberUnavailability.size() + " " + tempList.size());

        LinearLayoutManager timeSlotLayoutManager = new TimeSlotLinearLayoutManager(this);
        LinearLayoutManager jobLayoutManager = new LinearLayoutManager(this);

        mTimeSlotList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.ma_rv_timeSlots);

        mTimeSlotList.setLayoutManager(timeSlotLayoutManager);

        mTimeSlotAdapter = new TimeSlotAdapter(this, eventCursor, avail, this, true);

        txtNullTimeSlot = (TextView) findViewById(planit.planit.home.planit003.R.id.ma_tv_timeslot_null);
        updateNullView(mTimeSlotAdapter, txtNullTimeSlot);

        mTimeSlotList.setAdapter(mTimeSlotAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(planit.planit.home.planit003.R.menu.save_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case planit.planit.home.planit003.R.id.save_button:

                Intent i = new Intent(MemberAvailabilityActivity.this, MemberDetailsActivity.class);
                i.putExtra("groupID", groupID);
                i.putExtra("groupName", groupName);
                i.putExtra("name", name);
                i.putExtra("memberID", memberID);

                startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(String job, int jobID) {


    }

    @Override
    public void onClick(String timeSlotStart, String timeSlotEnd, int timeSlotID, View view, CheckBox checkBox) {

        if(view.getId() != checkBox.getId()) {checkBox.toggle();}

        Log.d(TAG, "DEBUG MAA 170 " + checkBox.isChecked());

        String ma = "";

        if (checkBox.isChecked()) {

            for (int i = 0; i < memberUnavailability.size(); i ++) {

                Log.d(TAG, "DEBUG MAA 174 " + memberUnavailability.get(i)[1] + " " + timeSlotID);

                if (memberUnavailability.get(i)[1].equals(String.valueOf(timeSlotID))) {

                    Log.d(TAG, "DEBUG MAA 198");

                    memberUnavailability.remove(i);
                }
            }

            for(String s : tempList) {

                ma += s + "//";
            }

            for(String[] sa : memberUnavailability) {

                ma += sa[0] + " " + sa[1] + "//";
            }
        }

        else {

            //concat all info

            for(String s : tempList) {

                ma += s + "//";
            }

            for(String[] sa : memberUnavailability) {

                ma += sa[0] + " " + sa[1] + "//";
            }

            ma += String.valueOf(eventID) + " " + String.valueOf(timeSlotID);

            memberUnavailability.add(new String[]{String.valueOf(eventID), String.valueOf(timeSlotID)});
        }

        Log.d(TAG, "DEBUG MAA 197 " + ma);

        ContentValues cv = new ContentValues();
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, ma);

        mMemberDb.update(
                MemberContract.MemberEntry.TABLE_NAME,
                cv,
                MemberContract.MemberEntry._ID + " = " + memberID,
                null
        );
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

            Log.d(TAG, "DEBUG MAA 849");
        }

        return cursor;
    }

    public Cursor getMember() {

        return mMemberDb.query(
                MemberContract.MemberEntry.TABLE_NAME,
                null,
                MemberContract.MemberEntry._ID + " = " + memberID,
                null,
                null,
                null,
                null
        );
    }

    public void saveAvailability() {


    }

    public void updateNullView(RecyclerView.Adapter adapter, TextView textView) {

        if (adapter.getItemCount() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }
}
