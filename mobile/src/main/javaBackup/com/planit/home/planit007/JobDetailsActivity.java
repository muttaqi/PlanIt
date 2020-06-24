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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.mobile.Adapters.PreferenceAdapter;
import com.planit.mobile.Adapters.QualificationAdapter;
import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.EventContract;
import com.planit.mobile.data.DbHelpers.EventDbHelper;
import com.planit.mobile.data.Useful;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.planit007.Adapters.PreferenceAdapter;
import com.planit007.Adapters.QualificationAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.planit007.data.Contracts.EventContract;
import com.planit007.data.DbHelpers.EventDbHelper;
import com.planit007.data.Useful;

import planit007.planit.home.planit007.data.Contracts.EventContract;
import planit007.planit.home.planit007.data.Useful;

public class JobDetailsActivity extends AppCompatActivity {

    private String TAG = JobDetailsActivity.class.getSimpleName();

    private String eventName;
    private int eventID;
    private String job;
    private int jobID;
    private int groupID;

    private boolean newJob;

    private SQLiteDatabase mEventDb;

    private PreferenceAdapter mPrefAdapter;
    private QualificationAdapter mQualAdapter;

    private RecyclerView mPrefList;
    private RecyclerView mQualList;

    private TextView txtNullPref;
    private TextView txtNullQual;

    private Cursor mCursor;

    private List<String> prefs = new ArrayList<String>();
    private List<String> quals = new ArrayList<String>();

    private List<String> allQuals = new ArrayList<String>();
    private List<String> allPrefs = new ArrayList<String>();

    private FloatingActionMenu famJDA;

    private TextView tvJobName;

    public Context mContext = this;

    private AdView mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(planit.planit.home.planit003.R.layout.activity_job_details);

        mAdView = findViewById(planit.planit.home.planit003.R.id.jd_ad);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        Intent intent = this.getIntent();

        eventName = intent.getStringExtra("eventName");
        eventID = intent.getIntExtra("eventID", 0);

        job = intent.getStringExtra("jobName");
        jobID = intent.getIntExtra("jobID", 0);
        groupID = intent.getIntExtra("groupID", 0);

        newJob = true;

        Log.d(TAG, "DEBUG JDA 73 INTENT VALUES " + eventName + " " + job);

        tvJobName = (TextView) findViewById(planit.planit.home.planit003.R.id.jd_tv_job_name);
        tvJobName.setText(job);

        mPrefList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.jd_rv_prefs);
        mQualList = (RecyclerView) findViewById(planit.planit.home.planit003.R.id.jd_rv_quals);

        LinearLayoutManager prefLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager qualLayoutManager = new LinearLayoutManager(this);

        mPrefList.setLayoutManager(prefLayoutManager);
        mQualList.setLayoutManager(qualLayoutManager);

        EventDbHelper eventDbHelper = new EventDbHelper(this);

        mEventDb = eventDbHelper.getWritableDatabase();

        mCursor = getEventData(eventName);

        if (!mCursor.moveToFirst() || mCursor.equals(null)) {

            Log.d(TAG, "DEBUG JDA 104 shit makes no sense");
        }

        else {

            String jobString = mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS));
            List<String> jobs = new LinkedList<String>(Arrays.asList(jobString.split("\\|\\|")));

            Log.d(TAG, "DEBUG JDA 116 " + job);

            for(String s : jobs) {

                Log.d(TAG, "DEBUG JDA 114 " + s);

                if (s.equals(job)) {

                    newJob = false;
                }
            }
        }

        mPrefList.setHasFixedSize(true);
        mQualList.setHasFixedSize(true);

        getQualsAndPrefs(mCursor, jobID);

        Log.d(TAG, "DEBUG JDA 104 " + quals);

        Useful.removeUnderscores(quals);
        Useful.removeUnderscores(prefs);

        Log.d(TAG, "DEBUG JDA 109 " + quals);

        txtNullPref = (TextView) findViewById(planit.planit.home.planit003.R.id.jd_tv_pref_null);
        txtNullQual = (TextView) findViewById(planit.planit.home.planit003.R.id.jd_tv_qual_null);

        mQualAdapter = new QualificationAdapter(this, quals, txtNullQual);
        mPrefAdapter = new PreferenceAdapter(this, prefs, txtNullPref);

        mPrefList.setAdapter(mPrefAdapter);
        mQualList.setAdapter(mQualAdapter);

        updateNullView(mPrefAdapter, txtNullPref);
        updateNullView(mQualAdapter, txtNullQual);

        famJDA = (FloatingActionMenu) findViewById(planit.planit.home.planit003.R.id.jd_fam);

        findViewById(planit.planit.home.planit003.R.id.jd_fab_add_qual).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            final Useful.NameAlertDialog qualNAD = new Useful.NameAlertDialog("Qualification", "ADD", "CANCEL", "", new EditText(JobDetailsActivity.this), JobDetailsActivity.this);
            qualNAD.create();
            qualNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    qualNAD.setBtnColor();
                    qualNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(qualNAD.et.getText().toString().equals("") || qualNAD.et.getText().toString().equals(null)) {

                                Toast t = Toast.makeText(JobDetailsActivity.this, "Please enter a qualification name", Toast.LENGTH_LONG);

                                t.show();
                            }

                            else {

                                quals.add(String.valueOf(qualNAD.et.getText()));

                                storeJobData();

                                mQualAdapter.notifyDataSetChanged();

                                updateNullView(mQualAdapter, txtNullQual);

                                famJDA.close(true);

                                qualNAD.ad.dismiss();
                            }
                        }
                    });
                }
            });

            qualNAD.ad.show();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.jd_fab_add_pref).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

            final Useful.NameAlertDialog prefNAD = new Useful.NameAlertDialog("Preference", "ADD", "CANCEL", "", new EditText(JobDetailsActivity.this), JobDetailsActivity.this);
            prefNAD.create();
            prefNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialogInterface) {

                    prefNAD.setBtnColor();
                    prefNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if(prefNAD.et.getText().toString().equals("") || prefNAD.et.getText().toString().equals(null)) {

                                Toast t = Toast.makeText(JobDetailsActivity.this, "Please enter a preference name", Toast.LENGTH_LONG);

                                t.show();
                            }

                            else {

                                prefs.add(String.valueOf(prefNAD.et.getText()));

                                storeJobData();

                                mPrefAdapter.notifyDataSetChanged();

                                updateNullView(mPrefAdapter, txtNullPref);

                                famJDA.close(true);

                                prefNAD.ad.dismiss();
                            }
                        }
                    });
                }
            });

            prefNAD.ad.show();
            }
        });

        tvJobName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        findViewById(planit.planit.home.planit003.R.id.jd_iv_name).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                nameOnClick();
            }
        });

        tvJobName.setPaintFlags(tvJobName.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));

        findViewById(planit.planit.home.planit003.R.id.jd_iv_qual).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);

                TextView tv = new TextView(adb.getContext());
                tv.setText("Qualifications allow you to control which members are scheduled for certain jobs. " +
                        "Just add a qualification to a job, and add a qualification of the same name to one or more members, " +
                        "and only those members will be scheduled for the job. Be careful though, if no member with the " +
                        "qualification is available, you won't be able to create a schedule!");
                tv.setPadding(24, 16, 0, 0);

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

        findViewById(planit.planit.home.planit003.R.id.jd_iv_pref).setOnClickListener(new View.OnClickListener() {
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();

        menuInflater.inflate(planit.planit.home.planit003.R.menu.save_delete_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == planit.planit.home.planit003.R.id.delete_button) {

            deleteThenStoreJobData();

            Intent i = new Intent(JobDetailsActivity.this, EventDetailsActivity.class);

            i.putExtra("eventName", eventName);
            i.putExtra("groupID", groupID);
            i.putExtra("eventID", eventID);

            Log.d(TAG, "DEBUG JDA 173 " + eventID);

            startActivity(i);
        }

        else if (item.getItemId() == planit.planit.home.planit003.R.id.save_button) {

            storeJobData();

            Intent i = new Intent(JobDetailsActivity.this, EventDetailsActivity.class);

            i.putExtra("eventName", eventName);
            i.putExtra("groupID", groupID);
            i.putExtra("eventID", eventID);

            Log.d(TAG, "DEBUG JDA 173 " + eventID);

            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    public Cursor getEventData(String eventName) {

        return mEventDb.query(
                EventContract.EventEntry.TABLE_NAME,
                null,
                EventContract.EventEntry.COLUMN_EVENT_NAME + "='" + Useful.doubleSingleQuotations(eventName) + "'",
                null,
                null,
                null,
                null
        );
    }

    public void getQualsAndPrefs(Cursor mCursor, int jobID) {

        allQuals = new LinkedList<String>(Arrays.asList(mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS)).split("//")));
        allPrefs = new LinkedList<String>(Arrays.asList(mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES)).split("//")));

        Log.d(TAG, "DEBUG JDA 149 " + quals);

        do {

            if (jobID >= allQuals.size() || allQuals.size() == 0) {

                allQuals.add("");
            }

            if (jobID >= allPrefs.size() || allQuals.size() == 0) {

                allPrefs.add("");
            }

        } while (jobID >= allQuals.size() || jobID >= allPrefs.size() || allQuals.size() == 0 || allQuals.size() == 0);

        mCursor.moveToFirst();

        do {

            Log.d(TAG, "DEBUG JDA 221 " + mCursor.getInt(mCursor.getColumnIndex(EventContract.EventEntry._ID)));
        } while (mCursor.moveToNext());

        mCursor.moveToFirst();

        mCursor = getEventData(eventName);

        Log.d(TAG, "DEBUG JDA 228 id " + jobID + " BUT " + allQuals);

        quals = new LinkedList<String>(Arrays.asList(allQuals.get(jobID).split("\\|\\|")));
        prefs = new LinkedList<String>(Arrays.asList(allPrefs.get(jobID).split("\\|\\|")));

        Log.d(TAG, "DEBUG JDA 160 " + quals);
    }

    public void storeJobData() {

        quals = mQualAdapter.getQuals();
        prefs = mPrefAdapter.getPrefs();

        Log.d(TAG, "DEBUG JDA 495 allQuals: " + allQuals + " allPrefs: " + allPrefs);

        String qualJoined = "";

        Log.d(TAG, "DEBUG JDA 311 " + quals);

        for(String s : quals) {

            qualJoined += s + "||";
        }

        if (quals.size() > 0)
            qualJoined = qualJoined.substring(0, qualJoined.length() - 2);

        Log.d(TAG, "DEBUG JDA 324 " + qualJoined);

        allQuals.set(jobID, qualJoined);

        String prefJoined = "";

        for(String s : prefs) {

            prefJoined += s + "||";
        }

        if (prefs.size() > 0)
            prefJoined = prefJoined.substring(0, prefJoined.length() - 2);

        allPrefs.set(jobID, prefJoined);

        String allQualsJoined = "";

        Log.d(TAG, "DEBUG JDA 494 " + allQuals);

        for(String s : allQuals) {

            allQualsJoined = allQualsJoined + s + "//";
        }

        if (allQuals.size() > 0)
            allQualsJoined = allQualsJoined.substring(0, allQualsJoined.length() - 2);

        Log.d(TAG, "DEBUG JDA 374 " + allQualsJoined);

        String allPrefsJoined = "";

        for(String s : allPrefs) {

            allPrefsJoined += s + "//";
        }

        if (allPrefs.size() > 0)
            allPrefsJoined = allPrefsJoined.substring(0, allPrefsJoined.length() - 2);

        ContentValues cv = new ContentValues();
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, allQualsJoined);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, allPrefsJoined);

        String jobString = mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS));
        List<String> jobs = new LinkedList<String>(Arrays.asList(jobString.split("\\|\\|")));

        if (newJob) {

            jobs.add(tvJobName.getText().toString());

            jobString = "";

            for (String s : jobs) {

                jobString += s + "||";
            }

            jobString = jobString.substring(0, jobString.length() - 2);

            cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString);

        }

        else {

            jobs.set(jobID, String.valueOf(tvJobName.getText()));

            jobString = "";

            for (String s : jobs) {

                jobString += s + "||";
            }

            jobString = jobString.substring(0, jobString.length() - 2);

            cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString);
        }

        mEventDb.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry.COLUMN_EVENT_NAME + "='" + Useful.doubleSingleQuotations(eventName) + "'",
                null
        );
    }

    public void deleteThenStoreJobData() {

        quals = mQualAdapter.getQuals();
        prefs = mPrefAdapter.getPrefs();

        String qualJoined = "";

        Log.d(TAG, "DEBUG JDA 311 " + quals);

        for(String s : quals) {

            qualJoined += s + "||";
        }

        if (quals.size() > 0)
            qualJoined = qualJoined.substring(0, qualJoined.length() - 2);

        Log.d(TAG, "DEBUG JDA 324 " + qualJoined);

        allQuals.set(jobID, qualJoined);

        String prefJoined = "";

        for(String s : prefs) {

            prefJoined += s + "||";
        }

        if (prefs.size() > 0)
            prefJoined = prefJoined.substring(0, prefJoined.length() - 2);

        allPrefs.set(jobID, prefJoined);

        String allQualsJoined = "";

        for(String s : allQuals) {

            allQualsJoined += s + "//";
        }

        if (allQuals.size() > 0)
            allQualsJoined = allQualsJoined.substring(0, allQualsJoined.length() - 2);

        Log.d(TAG, "DEBUG JDA 374 " + allQualsJoined);

        String allPrefsJoined = "";

        for(String s : allPrefs) {

            allPrefsJoined += s + "//";
        }

        if (allPrefs.size() > 0)
            allPrefsJoined.substring(0, allPrefsJoined.length() - 2);

        Log.d(TAG, "DEBUG JDA 607 allqualsjoined " + allQualsJoined + " allprefsjoined " + allPrefsJoined);

        List<String> jobs = new LinkedList<String>(Arrays.asList(mCursor.getString(mCursor.getColumnIndex(EventContract.EventEntry.COLUMN_TABLE_JOBS)).split("\\|\\|")));

        jobs.remove(jobID);

        String jobString = "";

        for(String s : jobs) {

            jobString += s + "||";
        }

        if (jobs.size() > 0)
            jobString.substring(0, jobString.length() - 2);

        Log.d(TAG, "DEBUG JDA 584 " + jobString);

        ContentValues cv = new ContentValues();
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_QUALIFICATIONS, allQualsJoined);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOB_PREFERENCES, allPrefsJoined);
        cv.put(EventContract.EventEntry.COLUMN_TABLE_JOBS, jobString);

        mEventDb.update(
                EventContract.EventEntry.TABLE_NAME,
                cv,
                EventContract.EventEntry.COLUMN_EVENT_NAME + "='" + Useful.doubleSingleQuotations(eventName) + "'",
                null
        );

        mCursor = getEventData(eventName);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        setIntent(intent);
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

        famJDA.close(true);

        final Useful.NameAlertDialog jobNAD = new Useful.NameAlertDialog("Job", "DONE", "CANCEL", tvJobName.getText().toString(), new EditText(JobDetailsActivity.this), JobDetailsActivity.this);
        jobNAD.create();
        jobNAD.ad.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {

                jobNAD.setBtnColor();
                jobNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        tvJobName.setText(String.valueOf(jobNAD.et.getText()));
                        jobNAD.ad.dismiss();
                    }
                });
            }
        });

        jobNAD.ad.show();
    }
}
