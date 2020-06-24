package planit007.planit.home.planit007;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.mobile.Adapters.EventAdapter;
import com.planit.mobile.Adapters.MemberPreferenceAdapter;
import com.planit.mobile.Adapters.MemberQualificationAdapter;
import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.Contracts.MemberContract;
import com.planit.mobile.data.DbHelpers.MemberDbHelper;
import com.planit.mobile.data.Useful;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit007.Adapters.EventAdapter;
import com.planit007.Adapters.MemberPreferenceAdapter;
import com.planit007.Adapters.MemberQualificationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.planit007.data.Contracts.EventContract;
import com.planit007.data.Contracts.MemberContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.DbHelpers.MemberDbHelper;
import com.planit007.data.Useful;

import planit007.planit.home.planit007.data.Contracts.EventContract;
import planit007.planit.home.planit007.data.Contracts.MemberContract;
import planit007.planit.home.planit007.data.Useful;

public class MemberDetailsActivity extends AppCompatActivity implements MemberPreferenceAdapter.MemberPreferenceAdapterOnClickHandler, MemberQualificationAdapter.MemberQualificationAdapterOnClickHandler, EventAdapter.EventAdapterOnClickHandler {
    private static final String TAG = "MDA";
    boolean memberExists = false;

    String defaultName;
    int memberID;
    int groupID;

    TextView header;

    TextView tvName;

    String groupName;
    //Eventually will be extra from group details activity
    String members = "";

    private SQLiteDatabase mMemberDb;
    private SQLiteDatabase mEventsDb;

    private MemberQualificationAdapter memberQualificationAdapter;
    private MemberPreferenceAdapter memberPreferenceAdapter;
    private EventAdapter eventAdapter;

    private RecyclerView memQualList;
    private RecyclerView memPrefList;
    private RecyclerView eventList;

    private TextView txtNullQual;
    private TextView txtNullPref;
    private TextView txtNullEvent;

    private Cursor mCursor;
    private Cursor eventCursor;

    private List<String> quals = new ArrayList<String>();
    private List<String> prefs = new ArrayList<String>();

    private FloatingActionMenu famMDA;

    public Context mContext = this;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(planit.planit.home.planit003.R.layout.activity_member_details);

        mAdView = findViewById(planit.planit.home.planit003.R.id.md_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = getIntent();

        groupName = intent.getStringExtra("groupName");
        defaultName = intent.getStringExtra("name");
        memberID = intent.getIntExtra("memberID", -1);
        groupID = intent.getIntExtra("groupID", 0);

        if (memberID != -1) {

            memberExists = true;

            Log.d(TAG, "DEBUG MDA 88 MEMBER EXISTS");
        }
        Log.d(TAG, "DEBUG MDA 51 " + groupName);

        tvName = (TextView) findViewById(planit.planit.home.planit003.R.id.md_tv_name);

        tvName.setText(defaultName);

        memQualList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.md_rv_quals);
        memPrefList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.md_rv_prefs);
        eventList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.md_rv_events);

        LinearLayoutManager qualLM = new LinearLayoutManager(this);
        LinearLayoutManager prefLM = new LinearLayoutManager(this);
        LinearLayoutManager eventLM = new LinearLayoutManager(this);

        memQualList.setLayoutManager(qualLM);
        memPrefList.setLayoutManager(prefLM);
        eventList.setLayoutManager(eventLM);

        MemberDbHelper memberDbHelper = new MemberDbHelper(this);
        EventDbHelper eventDbHelper = new EventDbHelper(this);

        mMemberDb = memberDbHelper.getWritableDatabase();
        mEventsDb = eventDbHelper.getWritableDatabase();

        mCursor = getMember();
        eventCursor = getGroupEvents();

        if(!mCursor.moveToFirst()) {

            Log.d(TAG, "DEBUG MDA 115 Empty");
        }

        else {

            Log.d(TAG, "DEBUG MDA 136 " + mCursor.getString(mCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS)) +
                    mCursor.getString(mCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES)));

            quals = new LinkedList<String>(Arrays.asList(mCursor.getString(mCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS)).split("\\|\\|")));
            prefs = new LinkedList<String>(Arrays.asList(mCursor.getString(mCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES)).split("\\|\\|")));

            quals.remove("");
            prefs.remove("");
        }

        Useful.removeUnderscores(quals);
        Useful.removeUnderscores(prefs);

        memberQualificationAdapter = new MemberQualificationAdapter(this, quals, this);
        memberPreferenceAdapter = new MemberPreferenceAdapter(this, prefs, this);
        eventAdapter = new EventAdapter(this, eventCursor, this);

        txtNullQual = (TextView) findViewById(planit.planit.home.planit003.R.id.md_tv_qual_null);
        txtNullPref = (TextView) findViewById(planit.planit.home.planit003.R.id.md_tv_pref_null);
        txtNullEvent = (TextView) findViewById(planit.planit.home.planit003.R.id.md_tv_event_null);

        updateNullView(memberQualificationAdapter, txtNullQual);
        updateNullView(memberPreferenceAdapter, txtNullPref);
        updateNullView(eventAdapter, txtNullEvent);

        Log.d(TAG, "DEBUG MDA 150 prefs " + prefs + " GIVES " + memberPreferenceAdapter.getItemCount());
        Log.d(TAG, "DEBUG MDA 150 quals " + quals + " GIVES " + memberQualificationAdapter.getItemCount());

        memQualList.setAdapter(memberQualificationAdapter);
        memPrefList.setAdapter(memberPreferenceAdapter);
        eventList.setAdapter(eventAdapter);

        Log.d(TAG, "DEBUG MDA 160");

        famMDA = (FloatingActionMenu) findViewById(planit.planit.home.planit003.R.id.md_fam);

        findViewById(planit.planit.home.planit003.R.id.md_fab_add_qual).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final Useful.NameAlertDialog qualNAD = new Useful.NameAlertDialog("Qualification", "ADD", "CANCEL", "", new EditText(MemberDetailsActivity.this), MemberDetailsActivity.this);
                qualNAD.create();
                qualNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        qualNAD.setBtnColor();

                        Button btnPos = qualNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);
                        btnPos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String name = tvName.getText().toString();

                                if(quals.size() >= 1 && quals.get(quals.size() - 1).equals("")) {

                                    quals.set(quals.size() - 1, String.valueOf(qualNAD.et.getText()));
                                }

                                else {

                                    quals.add(String.valueOf(qualNAD.et.getText()));
                                }

                                if(!memberExists) {

                                    addNewMember(groupName, name);

                                    memberExists = true;
                                }

                                else {

                                    updateMember(groupName, name);
                                }

                                memberQualificationAdapter.notifyDataSetChanged();

                                updateNullView(memberQualificationAdapter, txtNullQual);

                                Log.d(TAG, "DEBUG MDA 188 " + quals);

                                qualNAD.ad.dismiss();

                                famMDA.close(true);
                            }
                        });
                    }
                });

                qualNAD.ad.show();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.md_fab_add_pref).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                final Useful.NameAlertDialog prefNAD = new Useful.NameAlertDialog("Preference", "ADD", "CANCEL", "", new EditText(MemberDetailsActivity.this), MemberDetailsActivity.this);
                prefNAD.create();
                prefNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        prefNAD.setBtnColor();

                        Button btnPos = prefNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE);
                        btnPos.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                String name = tvName.getText().toString();

                                if(prefs.size() >= 1 && prefs.get(prefs.size() - 1).equals("")) {

                                    prefs.set(prefs.size() - 1, String.valueOf(prefNAD.et.getText()));
                                }

                                else {

                                    prefs.add(String.valueOf(prefNAD.et.getText()));
                                }

                                if(!memberExists) {

                                    addNewMember(groupName, name);

                                    memberExists = true;
                                }

                                else {

                                    updateMember(groupName, name);
                                }

                                memberPreferenceAdapter.notifyDataSetChanged();

                                updateNullView(memberPreferenceAdapter, txtNullPref);

                                Log.d(TAG, "DEBUG MDA 242 " + prefs);

                                prefNAD.ad.dismiss();

                                famMDA.close(true);
                            }
                        });
                    }
                });

                prefNAD.ad.show();
            }
        });

        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.md_iv_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.md_iv_qual).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);

                TextView tv = new TextView(adb.getContext());
                tv.setText("Qualifications allow you to control which members are scheduled for certain jobs. " +
                        "Just add a qualification to a job, and add a qualification of the same name to one or more members, " +
                        "and only those members will be scheduled for the job. Be careful though, if no member with the " +
                        "qualification is available, you won't be able to create a schedule!");
                tv.setPadding(24, 0, 0, 0);

                adb.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                }).setTitle("Qualifications").setView(tv);

                final AlertDialog ad = adb.create();

                ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                    }
                });
                ad.show();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.md_iv_pref).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);

                TextView tv = new TextView(adb.getContext());
                tv.setText("Preferences allow you to influence which members are scheduled for certain jobs. " +
                        "Just add a preference to a job, and add a preference of the same name to one or more members, " +
                        "and those members will be prioritized when scheduling for that job. If no member with the " +
                        "preference is available, the schedule will continue with other members.");
                tv.setPadding(24, 16, 0, 0);

                adb.setPositiveButton("CLOSE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {


                    }
                }).setTitle("Preferences").setView(tv);

                final AlertDialog ad = adb.create();

                ad.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {

                        ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(getResources().getColor(planit.planit.home.planit003.R.color.colorPrimary));
                    }
                });
                ad.show();
            }
        });

        Log.d(TAG, "DEBUG MDA 306");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();

        inflater.inflate(planit.planit.home.planit003.R.menu.save_menu, menu);
        inflater.inflate(planit.planit.home.planit003.R.menu.delete_menu, menu);

        Log.d(TAG, "DEBUG MDA 302");

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case planit.planit.home.planit003.R.id.save_button:

                //returns whether valid  first & last name
                if (saveMember()) {
                    Intent intent = new Intent(MemberDetailsActivity.this, GroupDetailsActivity.class);

                    intent.putExtra("groupID", (int) groupID);

                    startActivity(intent);
                }

                return true;

            case planit.planit.home.planit003.R.id.delete_button:

                String name = tvName.getText().toString();

                memberDeletePrompt(name).show();

                return true;

            default:

                return super.onOptionsItemSelected(item);
        }

    }

    public long addNewMember(String groupName, String name) {

        ContentValues cv = new ContentValues();

        cv.put(MemberContract.MemberEntry.COLUMN_NAME, name);
        cv.put(MemberContract.MemberEntry.COLUMN_GROUP_ID, groupID);
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_AVAILABILITY, "");

        String qualString = "";
        String prefString = "";

        for(String s : quals) {

            qualString += s + "||";
        }

        if (quals.size() > 0)
            qualString = qualString.substring(0, qualString.length() - 2);

        for(String s : prefs) {

            prefString += s + "||";
        }

        if (prefs.size() > 0)
            prefString = prefString.substring(0, prefString.length() - 2);

        Log.d(TAG, "DEBUG MDA 426 " + qualString + " AND " + prefString);

        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS, qualString);
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES, prefString);

        Log.d(TAG, "DEBUG MDA 429 MEMBER ADDED");

        return mMemberDb.insertOrThrow(MemberContract.MemberEntry.TABLE_NAME, null, cv);
    }

    public long updateMember(String groupName, String name) {

        Log.d(TAG, "DEBUG MDA 179 " + name);

        Log.d(TAG, "DEBUG MDA 229 " + quals);

        ContentValues cv = new ContentValues();

        cv.put(MemberContract.MemberEntry.COLUMN_NAME, name);

        String qualString = "";
        String prefString = "";

        for(String s : quals) {

            qualString += s + "||";
        }

        if (quals.size() > 0)
            qualString = qualString.substring(0, qualString.length() - 2);

        for(String s : prefs) {

            prefString += s + "||";
        }

        if (prefs.size() > 0)
            prefString = prefString.substring(0, prefString.length() - 2);

        Log.d(TAG, "DEBUG MDA 471 " + qualString);

        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_QUALIFICATIONS, qualString);
        cv.put(MemberContract.MemberEntry.COLUMN_MEMBER_PREFERENCES, prefString);

        return mMemberDb.update(
                MemberContract.MemberEntry.TABLE_NAME,
                cv,
                MemberContract.MemberEntry._ID + " = " + memberID,
                null
        );
    }

    public AlertDialog memberDeletePrompt(final String name) {

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

                        removeMember(memberID);

                        Intent intent = new Intent(MemberDetailsActivity.this, GroupDetailsActivity.class);

                        Log.d(TAG, "DEBUG MDA 111 " + String.valueOf(groupName));

                        int id;

                        intent.putExtra("groupID", groupID);
                        Log.d(TAG, "DEBUG MDA 114 " + String.valueOf(groupID));

                        startActivity(intent);
                    }
                })
                .setTitle("Are you sure you would like to delete this member?");

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

    public boolean removeMember(int mID) {

        return mMemberDb.delete(MemberContract.MemberEntry.TABLE_NAME, MemberContract.MemberEntry._ID + "=" + mID, null) > 0;
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

    private Cursor getGroupEvents() {

        return mEventsDb.query(

                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry.COLUMN_GROUP_ID + "=" + groupID,
                null,
                null,
                null,
                EventContract.EventEntry.COLUMN_EVENT_NAME
        );
    }

    public boolean saveMember() {

        if(tvName.getText().toString().equals("") || tvName.getText().toString().equals(null)) {

            Toast t = Toast.makeText(MemberDetailsActivity.this, "Please enter a name", Toast.LENGTH_LONG);

            t.show();

            return false;
        }

        else if(tvName.getText().toString().contains("|")) {

            Toast t = Toast.makeText(MemberDetailsActivity.this, "Special characters such as '|' are not allowed", Toast.LENGTH_LONG);

            t.show();

            return false;
        }

        else {

            String name = tvName.getText().toString();

            Log.d(TAG, "DEBUG MDA 102 " + members);

            if(!memberExists) {

                addNewMember(groupName, name);

                memberExists = true;
            }

            else {

                updateMember(groupName, name);
            }

            return true;
        }
    }

    @Override
    public void onClickEvent(int event_id, int group_id) {

        saveMember();

        Intent i = new Intent(MemberDetailsActivity.this, MemberAvailabilityActivity.class);
        i.putExtra("eventID", event_id);
        i.putExtra("groupName", groupName);
        i.putExtra("groupID", groupID);
        i.putExtra("name", tvName.getText().toString());
        i.putExtra("memberID", memberID);

        Log.d(TAG, "DEBUG MDA 682 " + tvName.getText());

        startActivity(i);
    }

    @Override
    public void onClick(long preference_id, String isDelete) {

        if (isDelete.equals("true")) {

            updateNullView(memberPreferenceAdapter, txtNullPref);
        }

        else {

            //if change name functionality is desired
        }
    }

    @Override
    public void onClick(long qualification_id, boolean isDelete) {

        if (isDelete) {

            updateNullView(memberQualificationAdapter, txtNullQual);
        }

        else {

            //if change name functionality is desired
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

    public void nameOnClick() {

        famMDA.close(true);

        tvName.setPaintFlags(tvName.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        final Useful.NameAlertDialog memberNAD = new Useful.NameAlertDialog("Member", "DONE", "CANCEL", tvName.getText().toString(), new EditText(MemberDetailsActivity.this), MemberDetailsActivity.this);
        memberNAD.create();
        memberNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                memberNAD.setBtnColor();

                memberNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        defaultName = String.valueOf(memberNAD.et.getText());
                        tvName.setText(defaultName);

                        memberNAD.ad.dismiss();
                    }
                });
            }
        });

        memberNAD.ad.show();
    }
}
