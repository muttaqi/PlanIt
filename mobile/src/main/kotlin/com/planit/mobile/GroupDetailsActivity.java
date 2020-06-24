/*package com.planit.mobile;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit.mobile.Adapters.EventAdapter;
import com.planit.mobile.Adapters.MemberAdapter;
import com.planit.mobile.data.Contracts.MemberContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.DbHelpers.GroupDbHelper;
import com.planit.mobile.data.DbHelpers.MemberDbHelper;
import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.data.Useful;

public class GroupDetailsActivity extends AppCompatActivity implements MemberAdapter.MemberAdapterOnClickHandler, EventAdapter.EventAdapterOnClickHandler {
    Context context = this;
    private static final String TAG = "GroupDetailsActivity";

    int groupID;
    String defaultGroupName;
    String groupName;
    boolean firstMember;

    FloatingActionMenu famGDA;

    private TextView tvGroupName;

    private SQLiteDatabase mMemberDb;
    private SQLiteDatabase mEventDb;
    private SQLiteDatabase mGroupDb;

    private MemberAdapter mMemberAdapter;
    private EventAdapter mEventAdapter;

    private RecyclerView mMembersList;
    private RecyclerView mEventsList;

    private TextView titleMemberRv;
    private TextView titleEventRv;

    private TextView txtNullMember;
    private TextView txtNullEvent;

    private Cursor groupCursor;
    private Cursor eventCursor;
    private Cursor memberCursor;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details);

        mAdView = findViewById(R.id.gd_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        titleMemberRv = (TextView) findViewById(R.id.gd_tv_member_title);
        titleEventRv = (TextView) findViewById(R.id.gd_tv_event_title);

        Intent intent = getIntent();

        groupID = intent.getIntExtra("groupID", 0);

        mMembersList = (RecyclerView) findViewById(R.id.gd_rv_members);
        mEventsList = (RecyclerView) findViewById(R.id.gd_rv_events);

        LinearLayoutManager memberLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager eventLayoutManager = new LinearLayoutManager(this);

        mMembersList.setLayoutManager(memberLayoutManager);
        mEventsList.setLayoutManager(eventLayoutManager);

        MemberDbHelper memberDbHelper = new MemberDbHelper(this);
        EventDbHelper eventDbHelper = new EventDbHelper(this);
        GroupDbHelper groupDbHelper = new GroupDbHelper(this);

        mMemberDb = memberDbHelper.getReadableDatabase();
        mEventDb = eventDbHelper.getReadableDatabase();
        mGroupDb = groupDbHelper.getWritableDatabase();

        groupCursor = getGroupName(groupID);

        if(groupCursor.moveToFirst() && groupCursor != null) {

            groupName = groupCursor.getString(groupCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME));
            defaultGroupName = groupName;
        }

        tvGroupName = (TextView) findViewById(R.id.gd_tv_group_name);
        tvGroupName.setText(defaultGroupName);

        memberCursor = getMembers();
        eventCursor = getEvents();

        memberCursor.moveToFirst();
        eventCursor.moveToFirst();

        mMembersList.setHasFixedSize(true);
        mEventsList.setHasFixedSize(true);

        mMemberAdapter = new MemberAdapter(this, memberCursor, this);
        mEventAdapter = new EventAdapter(this, eventCursor, this);

        mMembersList.setAdapter(mMemberAdapter);
        mEventsList.setAdapter(mEventAdapter);

        titleMemberRv.setText("Members");
        titleEventRv.setText("Events");

        txtNullMember = (TextView) findViewById(R.id.gd_tv_member_null);
        txtNullEvent = (TextView) findViewById(R.id.gd_tv_event_null);

        updateNullView(mMemberAdapter, txtNullMember);
        updateNullView(mEventAdapter, txtNullEvent);

        firstMember = (mMemberAdapter.getItemCount() == 1);

        famGDA = (FloatingActionMenu) findViewById(R.id.gd_fam);

        findViewById(R.id.gd_fab_add_member).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final Useful.NameAlertDialog memberNAD = new Useful.NameAlertDialog("Member", "ADD", "CANCEL", "", new EditText(context), context);
                memberNAD.create();
                memberNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        memberNAD.setBtnColor();

                        Button btnPos = memberNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);

                        btnPos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                if(memberNAD.et.getText().toString().equals("") || memberNAD.et.getText().toString().equals(null)) {

                                    Toast t = Toast.makeText(getApplicationContext(), "Please enter a name", Toast.LENGTH_LONG);

                                    t.show();
                                }

                                else {

                                    Intent i = new Intent(GroupDetailsActivity.this, MemberDetailsActivity.class);

                                    i.putExtra("name", memberNAD.et.getText().toString());
                                    i.putExtra("groupID", groupID);
                                    i.putExtra("groupName", groupName);

                                    famGDA.close(true);

                                    memberNAD.ad.dismiss();

                                    saveGroupname();

                                    startActivity(i);
                                }
                            }
                        });
                    }
                });

                memberNAD.ad.show();
            }
        });

        findViewById(R.id.gd_fab_add_event).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            final Useful.NameAlertDialog eventNAD = new Useful.NameAlertDialog("Event", "CREATE", "CANCEL", "", new EditText(context), context);
            eventNAD.create();
            eventNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    Button btnPos = eventNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);

                    eventNAD.setBtnColor();

                    btnPos.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(eventNAD.et.getText().toString().equals("") || eventNAD.et.getText().toString().equals(null)) {

                                Toast t = Toast.makeText(getApplicationContext(), "Please enter an event name", Toast.LENGTH_LONG);

                                t.show();
                            }

                            else {

                                Intent i = new Intent(GroupDetailsActivity.this, EventDetailsActivity.class);

                                i.putExtra("eventName", eventNAD.et.getText().toString());
                                i.putExtra("groupID", groupID);
                                i.putExtra("isNewEvent", true);

                                famGDA.close(true);

                                eventNAD.ad.dismiss();

                                saveGroupname();

                                startActivity(i);
                            }
                        }
                    });
                }
            });

            eventNAD.ad.show();
            }
        });

        tvGroupName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        findViewById(R.id.gd_iv_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {

            Intent i = new Intent(GroupDetailsActivity.this, MainActivity.class);

            saveGroupname();
            startActivity(i);

            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(R.menu.save_delete_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.delete_button:

                //wouldn't work for some reason, test further
                groupDeletePrompt().show();

                return super.onOptionsItemSelected(item);

            case R.id.save_button:

                saveGroupname();

                Intent i = new Intent(GroupDetailsActivity.this, MainActivity.class);

                startActivity(i);

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Cursor getMembers() {

        return mMemberDb.query(
                MemberContract.MemberEntry.TABLE_NAME,
                null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID,
                null,
                null,
                null,
                MemberContract.MemberEntry.COLUMN_NAME
        );
    }

    private Cursor getEvents() {

        return mEventDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry.COLUMN_GROUP_ID + "=" + groupID,
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_DATE
        );
    }

    public AlertDialog groupDeletePrompt() {

        final AlertDialog.Builder adb = new AlertDialog.Builder(this);

        adb
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                })
                .setPositiveButton("CONTINUE", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        removeGroup(groupName);

                        Intent intent = new Intent(GroupDetailsActivity.this, MainActivity.class);
                        saveGroupname();
                        startActivity(intent);
                    }
                })
                .setTitle("Are you sure you would like to delete this group?");

        final AlertDialog retPrompt = adb.create();

        retPrompt.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                retPrompt.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
                retPrompt.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.colorPrimary));
            }
        });

        return retPrompt;
    }

    public boolean removeGroup(String groupName) {

        mMemberDb.delete(MemberContract.MemberEntry.TABLE_NAME, MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + groupID, null);
        mEventDb.delete(EventContract.EventEntry.TABLE_NAME, EventContract.EventEntry.COLUMN_GROUP_ID + "=" + groupID, null);
        return mGroupDb.delete(GroupContract.GroupEntry.TABLE_NAME, GroupContract.GroupEntry._ID + "=" + groupID, null) > 0;
    }

    public Cursor getGroupName(long groupID) {

        return mGroupDb.query(

                GroupContract.GroupEntry.TABLE_NAME,
                null,
                GroupContract.GroupEntry._ID + "=" + groupID,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onClick(String name, int memberID) {

        Intent intent = new Intent(GroupDetailsActivity.this, MemberDetailsActivity.class);

        intent.putExtra("name", name);

        intent.putExtra("groupID", groupID);
        intent.putExtra("groupName", groupName);
        intent.putExtra("firstMember", firstMember);
        intent.putExtra("memberID", memberID);

        saveGroupname();

        startActivity(intent);
    }

    public void saveGroupname() {

        groupName = String.valueOf(tvGroupName.getText());

        ContentValues cv1 = new ContentValues();
        cv1.put(GroupContract.GroupEntry.COLUMN_GROUP_NAME, groupName);

        mGroupDb.update(GroupContract.GroupEntry.TABLE_NAME,
                cv1,
                GroupContract.GroupEntry._ID + "=" + groupID + "",
                null);
    }

    @Override
    public void onClickEvent(int event_id, int group_id) {

        Intent i = new Intent(GroupDetailsActivity.this, EventDetailsActivity.class);

        //EVENT DB NOT NEEDED UNLIKE MAIN ACTIVITY BECAUSE GROUP ID IS ALREADY KNOWN (EVENT ID GIVEN AS PARAMETER)

        i.putExtra("eventID", event_id);
        i.putExtra("groupID", groupID);

        saveGroupname();

        startActivity(i);
    }

    @Override
    protected void onPause() {

        saveGroupname();

        super.onPause();
    }

    public void updateNullView(RecyclerView.Adapter adapter, TextView textView) {

        if (adapter.getItemCount() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }

    public void nameOnClick() {

        famGDA.close(true);

        tvGroupName.setPaintFlags(View.INVISIBLE);

        final Useful.NameAlertDialog groupNAD = new Useful.NameAlertDialog("Group", "DONE", "CANCEL", tvGroupName.getText().toString(), new EditText(context), context);
        groupNAD.create();
        groupNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialogInterface) {

                Button btnPos = groupNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);
                groupNAD.setBtnColor();

                btnPos.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        groupName = String.valueOf(groupNAD.et.getText());
                        tvGroupName.setText(groupName);

                        groupNAD.ad.dismiss();
                    }
                });
            }
        });

        groupNAD.ad.show();
    }
}
*/