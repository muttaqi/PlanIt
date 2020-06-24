package com.planit.home.planit007;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit.home.planit007.Adapters.GroupAdapter;
import com.planit.home.planit007.R;
import com.planit.home.planit007.data.Contracts.GroupContract;
import com.planit.home.planit007.data.DbHelpers.EventDbHelper;
import com.planit.home.planit007.data.DbHelpers.GroupDbHelper;

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
        setContentView(planit.planit.home.planit003.R.layout.activity_choose_group);

        mAdView = findViewById(planit.planit.home.planit003.R.id.cg_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent i = getIntent();
        eventName = i.getStringExtra("eventName");

        mGroupsList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.cg_rv_groups);

       final LinearLayoutManager groupLayoutManager = new LinearLayoutManager(this);

        if(!groupLayoutManager.equals(null)) {

            Log.d(TAG, "DEBUG CGA 36 NOT NULL EVENT NAME IS " + eventName );
        }

        mGroupsList.setLayoutManager(groupLayoutManager);

        GroupDbHelper groupDbHelper = new GroupDbHelper(this);
        EventDbHelper eventDbHelper = new EventDbHelper(this);

        mGroupDb = groupDbHelper.getReadableDatabase();
        mEventDb = eventDbHelper.getReadableDatabase();

        Cursor groupCursor = getAllGroups();

        mGroupsList.setHasFixedSize(true);

        mGroupAdapter = new GroupAdapter(this, groupCursor, this);

        txtNullGroup = (TextView) findViewById(planit.planit.home.planit003.R.id.cg_tv_group_null);
        updateNullView(mGroupAdapter, txtNullGroup);

        mGroupsList.setAdapter(mGroupAdapter);
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
    public void onClick(int group_id) {

        Intent i = new Intent(ChooseGroupActivity.this, EventDetailsActivity.class);
        i.putExtra("eventName", eventName);
        i.putExtra("groupID", (int) (group_id));
        i.putExtra("isNewEvent", true);

        Log.d(TAG, "DEBUG CGA 82 " + group_id);

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
