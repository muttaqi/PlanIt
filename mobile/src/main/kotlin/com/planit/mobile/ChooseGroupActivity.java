/*package com.planit.mobile;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit.mobile.Adapters.GroupAdapter;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.DbHelpers.GroupDbHelper;

public class ChooseGroupActivity extends AppCompatActivity implements GroupAdapter.GroupAdapterOnClickHandler {

    private SQLiteDatabase mGroupDb;
    private SQLiteDatabase mEventDb;

    private GroupAdapter mGroupAdapter;
    private RecyclerView mGroupsList;

    private TextView txtNullGroup;

    String TAG = "CGA";

    String eventName;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_group);

        mAdView = findViewById(R.id.cg_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent i = getIntent();
        eventName = i.getStringExtra("eventName");

        mGroupsList = (RecyclerView) findViewById(R.id.cg_rv_groups);

       final LinearLayoutManager groupLayoutManager = new LinearLayoutManager(this);

        mGroupsList.setLayoutManager(groupLayoutManager);

        GroupDbHelper groupDbHelper = new GroupDbHelper(this);
        EventDbHelper eventDbHelper = new EventDbHelper(this);

        mGroupDb = groupDbHelper.getReadableDatabase();
        mEventDb = eventDbHelper.getReadableDatabase();

        Cursor groupCursor = getAllGroups();

        groupCursor.moveToFirst();

        mGroupsList.setHasFixedSize(true);

        mGroupAdapter = new GroupAdapter(this, groupCursor, this);

        txtNullGroup = (TextView) findViewById(R.id.cg_tv_group_null);
        updateNullView(mGroupAdapter, txtNullGroup);

        mGroupsList.setAdapter(mGroupAdapter);
    }

    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
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

    @Override
    public void onClick(int group_id, RecyclerView memberList, RecyclerView eventList, TextView memberNull,
                        TextView eventNull, LinearLayout llContent, TextView listGroupNameView) {

        Intent i = new Intent(ChooseGroupActivity.this, EventDetailsActivity.class);
        i.putExtra("eventName", eventName);
        i.putExtra("groupID", (int) (group_id));
        i.putExtra("isNewEvent", true);

        startActivity(i);
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
*/