package planit007.planit.home.planit007;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.home.planit007.Adapters.EventAdapter;
import com.planit.home.planit007.Adapters.GroupAdapter;
import com.planit.home.planit007.data.Contracts.EventContract;
import com.planit.home.planit007.DbHelpers.EventDbHelper;
import com.planit.home.planit007.Contracts.GroupContract;
import com.planit.home.planit007.DbHelpers.GroupDbHelper;
import com.planit.home.planit007.Useful;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.planit007.Adapters.EventAdapter;
import com.planit007.Adapters.GroupAdapter;

import com.planit007.data.Contracts.EventContract;
import com.planit007.data.Contracts.GroupContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.DbHelpers.GroupDbHelper;
import com.planit007.data.Useful;

import planit007.planit.home.planit007.data.Contracts.EventContract;
import planit007.planit.home.planit007.data.Contracts.GroupContract;
import planit007.planit.home.planit007.data.Useful;


//7.04 ONWARDS NEEDS TO BE DONE FOR EVENTS

public class MainActivity extends AppCompatActivity implements GroupAdapter.GroupAdapterOnClickHandler, EventAdapter.EventAdapterOnClickHandler {
    Context context = this;

    FloatingActionMenu famMain;

    private SQLiteDatabase mGroupDb;
    private SQLiteDatabase mEventDb;

    private GroupAdapter mGroupAdapter;
    private EventAdapter mEventAdapter;

    private RecyclerView mGroupsList;
    private RecyclerView mEventsList;

    private Cursor groupCursor;
    private Cursor eventCursor;

    private TextView txtAddGroup;
    private TextView txtAddEvent;

    private TextView txtNullGroup;
    private TextView txtNullEvent;

    private boolean famIsOpen = false;

    String TAG = "MainActivity";

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(planit.planit.home.planit003.R.layout.activity_main);

        mAdView = findViewById(planit.planit.home.planit003.R.id.m_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        MobileAds.initialize(this, "ca-app-pub-1144529456090164~2412831884");

        mGroupsList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.m_rv_groups);
        mEventsList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.m_rv_events);

        LinearLayoutManager groupLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager eventLayoutManager = new LinearLayoutManager(this);

        mGroupsList.setLayoutManager(groupLayoutManager);
        mEventsList.setLayoutManager(eventLayoutManager);

        GroupDbHelper groupDbHelper = new GroupDbHelper(this);
        EventDbHelper eventDbHelper = new EventDbHelper(this);

        mGroupDb = groupDbHelper.getReadableDatabase();
        mEventDb = eventDbHelper.getReadableDatabase();

        groupCursor = getAllGroups();
        eventCursor = getAllEvents();

        txtNullGroup = (TextView) findViewById(planit.planit.home.planit003.R.id.m_tv_groups_null);
        txtNullEvent = (TextView) findViewById(planit.planit.home.planit003.R.id.m_tv_events_null);

        if(!eventCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG MA 77 EVENT CURSOR NULL");
        }

        mGroupsList.setHasFixedSize(true);
        mEventsList.setHasFixedSize(true);

        mGroupAdapter = new GroupAdapter(this, groupCursor, this);
        mEventAdapter = new EventAdapter(this, eventCursor, this);

        mGroupsList.setAdapter(mGroupAdapter);
        mEventsList.setAdapter(mEventAdapter);

        updateNullView(mGroupAdapter, txtNullGroup);
        updateNullView(mEventAdapter, txtNullEvent);

        famMain = (FloatingActionMenu) findViewById(planit.planit.home.planit003.R.id.m_fam);

        findViewById(planit.planit.home.planit003.R.id.m_fab_add_group).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final Useful.NameAlertDialog groupNAD = new Useful.NameAlertDialog("Group", "CREATE", "CANCEL", "", new EditText(context), context);
                groupNAD.create();
                groupNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        groupNAD.setBtnColor();
                        groupNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(groupNAD.et.getText().toString().equals("") || groupNAD.et.getText().toString().equals(null)) {

                                    Toast t = Toast.makeText(getApplicationContext(), "Please enter a group name", Toast.LENGTH_LONG);

                                    t.show();
                                }

                                else {

                                    Intent i = new Intent(MainActivity.this, GroupDetailsActivity.class);

                                    famMain.close(true);

                                    groupNAD.ad.dismiss();

                                    i.putExtra("groupName", groupNAD.et.getText().toString());
                                    i.putExtra("groupID", (int) (addNewGroup(groupNAD.et.getText().toString())));

                                    startActivity(i);
                                }
                            }
                        });
                    }
                });

                groupNAD.ad.show();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.m_fab_add_event).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if(mGroupAdapter.getItemCount() == 0) {

                    Toast t = new Toast(context);

                    t.makeText(context, "Please create a group before creating an event", Toast.LENGTH_SHORT).show();
                }

                else {

                    final Useful.NameAlertDialog eventNAD = new Useful.NameAlertDialog("Event", "CREATE", "CANCEL", "", new EditText(context), context);
                    eventNAD.create();
                    eventNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                        @Override
                        public void onShow(DialogInterface dialogInterface) {

                            eventNAD.setBtnColor();
                            eventNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    if(eventNAD.et.getText().toString().equals("") || eventNAD.et.getText().toString().equals(null)) {

                                        Toast t = Toast.makeText(getApplicationContext(), "Please enter an event name", Toast.LENGTH_LONG);

                                        t.show();
                                    }

                                    else {

                                        Intent i = new Intent(MainActivity.this, ChooseGroupActivity.class);

                                        i.putExtra("eventName", eventNAD.et.getText().toString());

                                        famMain.close(true);

                                        eventNAD.ad.dismiss();

                                        startActivity(i);
                                    }
                                }
                            });
                        }
                    });

                    eventNAD.ad.show();
                }
            }
        });
    }

    private Cursor getAllGroups() {

        return mGroupDb.query(

                GroupContract.GroupEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                GroupContract.GroupEntry.COLUMN_GROUP_NAME
        );
    }

    private Cursor getAllEvents() {

        return mEventDb.query(

                EventContract.EventEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_NAME
        );
    }

    @Override
    public void onClick(int group_id) {

        Intent intent = new Intent(this, GroupDetailsActivity.class);

        intent.putExtra("groupID", group_id);

        Log.d(TAG, "DEBUG MA 202 " + group_id);

        startActivity(intent);
    }

    @Override
    public void onClickEvent(int event_id, int group_id) {

        Log.d(TAG, "DEBUG MA 369 ONCLICKEVENT " + event_id);

        Intent i = new Intent(MainActivity.this, EventDetailsActivity.class);

        Cursor cursor = mEventDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry._ID + " = " + event_id,
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_DATE,
                null
        );

        i.putExtra("eventID", event_id);
        i.putExtra("groupID", group_id);

        Log.d(TAG, "DEBUG MA 372 " + group_id);

        startActivity(i);
    }

    public void updateNullView(RecyclerView.Adapter adapter, TextView textView) {

        Log.d(TAG, "DEBUG MA 427 " + adapter.getItemCount());

        if (adapter.getItemCount() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }

    public long addNewGroup(String groupName) {

        Log.d(TAG, "DEBUG MDA 197 GROUP IS ADDED");

        ContentValues cv = new ContentValues();

        cv.put(GroupContract.GroupEntry.COLUMN_GROUP_NAME, groupName);

        return mGroupDb.insertOrThrow(GroupContract.GroupEntry.TABLE_NAME, null, cv);
    }
}
