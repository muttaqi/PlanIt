package com.planit.mobile

import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.FragmentActivity
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.Toast.makeText

import com.github.clans.fab.FloatingActionButton
import com.github.clans.fab.FloatingActionMenu
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.planit.mobile.Adapters.GroupAdapter
import com.planit.mobile.Adapters.MemberAdapter
import com.planit.mobile.data.Contracts.EventContract
import com.planit.mobile.data.Contracts.GroupContract
import com.planit.mobile.data.Contracts.MemberContract
import com.planit.mobile.data.DbHelpers.MemberDbHelper
import com.planit.mobile.data.Useful
import com.planit.mobile.Adapters.EventAdapter
import com.planit.mobile.data.DbHelpers.EventDbHelper
import com.planit.mobile.data.DbHelpers.GroupDbHelper
import com.planit.mobile.R

import org.apache.commons.logging.LogFactory
import org.mortbay.jetty.Main
import org.w3c.dom.Text


//7.04 ONWARDS NEEDS TO BE DONE FOR EVENTS

class MainActivity : FragmentActivity(), GroupAdapter.GroupAdapterOnClickHandler, EventAdapter.EventAdapterOnClickHandler, MemberAdapter.MemberAdapterOnClickHandler {
    internal var context: Context = this

    private var eventDB: SQLiteDatabase? = null
    private var memberDB: SQLiteDatabase? = null

    private var mEventAdapter: EventAdapter? = null
    private var mMemberAdapter: MemberAdapter? = null

    private var mEventsList: RecyclerView? = null
    private var mMembersList: RecyclerView? = null

    private var eventCursor: Cursor? = null
    private var memberCursor: Cursor? = null
    internal lateinit var fabGroup: FloatingActionButton
    internal lateinit var fabDelete: FloatingActionButton

    private var mGroupDb: SQLiteDatabase? = null

    private var mGroupAdapter: GroupAdapter? = null

    private var mGroupsList: RecyclerView? = null

    private var groupCursor: Cursor? = null

    private var txtNullGroup: TextView? = null

    internal var TAG = "MainActivity"

    private var mAdView: AdView? = null

    private var expandedGroupId = -1

    internal var doubleBackToExitPressedOnce = false

    private val allGroups: Cursor
        get() = mGroupDb!!.query(

                GroupContract.GroupEntry.TABLE_NAME, null, null, null, null, null,
                GroupContract.GroupEntry.COLUMN_GROUP_NAME
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        mAdView = findViewById(R.id.m_ad)
        val adRequest = AdRequest.Builder().build()
        mAdView!!.loadAd(adRequest)

        MobileAds.initialize(this, "ca-app-pub-1144529456090164~2412831884")

        mGroupsList = findViewById<View>(R.id.m_rv_groups) as RecyclerView
        mGroupsList!!.isFocusable = false

        val groupLayoutManager = LinearLayoutManager(this)

        mGroupsList!!.layoutManager = groupLayoutManager

        val groupDbHelper = GroupDbHelper(this)

        mGroupDb = groupDbHelper.readableDatabase

        groupCursor = allGroups

        groupCursor!!.moveToFirst()

        txtNullGroup = findViewById<View>(R.id.m_tv_groups_null) as TextView

        mGroupsList!!.setHasFixedSize(true)

        mGroupAdapter = GroupAdapter(this, groupCursor, this)

        mGroupsList!!.adapter = mGroupAdapter

        updateNullView(mGroupAdapter!!, txtNullGroup)

        (findViewById<View>(R.id.m_fam_label) as FloatingActionMenu).open(true)

        fabGroup = findViewById(R.id.m_fab_add_group)
        fabDelete = findViewById(R.id.m_fab_delete)
        fabDelete.hide(false)

        fabDelete.setOnClickListener {
            if (expandedGroupId >= 0) {

                removeGroup()

                fabDelete.hide(true)
                famMain.close(true)

                groupCursor = allGroups
                groupCursor!!.moveToFirst()

                mGroupAdapter!!.refreshCursor(groupCursor)

                mGroupAdapter!!.notifyDataSetChanged()

                updateNullView(mGroupAdapter!!, txtNullGroup)

                expandedGroupId = -1
            }
        }

        famMain = findViewById<View>(R.id.m_fam) as FloatingActionMenu
        //famMain.setVisibility(View.GONE);

        fabGroup.setOnClickListener {
            Log.d(TAG, "DEBUG MA 137")

            val groupNAD = Useful.NameAlertDialog("Group", "CREATE", "CANCEL", "", EditText(context), context)
            groupNAD.create()
            groupNAD.ad.setOnShowListener {
                groupNAD.setBtnColor()
                groupNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    if (groupNAD.et.text.toString() == "" || groupNAD.et.text.toString() == null) {

                        val t = Toast.makeText(applicationContext, "Please enter a group name", Toast.LENGTH_LONG)

                        t.show()
                    } else {

                        addNewGroup(groupNAD.et.text.toString())

                        groupCursor = allGroups
                        groupCursor!!.moveToFirst()

                        mGroupAdapter!!.refreshCursor(groupCursor)

                        mGroupAdapter!!.notifyDataSetChanged()

                        updateNullView(mGroupAdapter!!, txtNullGroup)

                        expandedGroupId = -1

                        famMain.close(true)

                        groupNAD.ad.dismiss()
                    }
                }
            }

            groupNAD.ad.show()
        }

        findViewById<View>(R.id.m_fab_add_event).setOnClickListener {
            if (mGroupAdapter!!.itemCount == 0) {

                makeText(context, "Please create a group before creating an event", Toast.LENGTH_SHORT).show()
            } else if (expandedGroupId >= 0) {

                val eventNAD = Useful.NameAlertDialog("Event", "CREATE", "CANCEL", "", EditText(context), context)
                eventNAD.create()
                eventNAD.ad.setOnShowListener {
                    eventNAD.setBtnColor()
                    eventNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                        if (eventNAD.et.text.toString() == "" || eventNAD.et.text.toString() == null) {

                            val t = Toast.makeText(applicationContext, "Please enter an event name", Toast.LENGTH_LONG)

                            t.show()
                        } else {

                            val i = Intent(this@MainActivity, EventDetailsActivity::class.java)
                            i.putExtra("eventName", eventNAD.et.text.toString())
                            i.putExtra("groupID", expandedGroupId)
                            i.putExtra("isNewEvent", true)

                            startActivity(i)

                            famMain.close(true)

                            eventNAD.ad.dismiss()

                            startActivity(i)
                        }
                    }
                }

                eventNAD.ad.show()
            }
        }

        findViewById<View>(R.id.m_fab_add_member).setOnClickListener {
            if (expandedGroupId >= 0) {

                val memberNAD = Useful.NameAlertDialog("Member", "ADD", "CANCEL", "", EditText(context), context)
                memberNAD.create()
                memberNAD.ad.setOnShowListener {
                    memberNAD.setBtnColor()

                    val btnPos = memberNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE)

                    btnPos.setOnClickListener {
                        if (memberNAD.et.text.toString() == "" || memberNAD.et.text.toString() == null) {

                            val t = Toast.makeText(applicationContext, "Please enter a name", Toast.LENGTH_LONG)

                            t.show()
                        } else {

                            val i = Intent(this@MainActivity, MemberDetailsActivity::class.java)

                            i.putExtra("name", memberNAD.et.text.toString())
                            i.putExtra("groupID", expandedGroupId)

                            famMain.close(true)

                            memberNAD.ad.dismiss()

                            //saveGroupname();

                            startActivity(i)
                        }
                    }
                }

                memberNAD.ad.show()
            }
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {

            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return
        }

        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()

        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onClick(group_id: Int, memberList: RecyclerView, eventList: RecyclerView, memberNull: TextView,
                         eventNull: TextView, llContent: LinearLayout, listGroupNameView: TextView, arrow: ImageView) {

        Log.d(TAG, "DEBUG MA 359")
        if (llContent.visibility != View.VISIBLE) {

            famMain.visibility = View.VISIBLE
            mMembersList = memberList
            mEventsList = eventList

            val memberLayoutManager = LinearLayoutManager(context)
            val eventLayoutManager = LinearLayoutManager(context)

            mMembersList!!.layoutManager = memberLayoutManager
            mEventsList!!.layoutManager = eventLayoutManager

            val memberDbHelper = MemberDbHelper(context)
            val eventDbHelper = EventDbHelper(context)

            memberDB = memberDbHelper.readableDatabase
            eventDB = eventDbHelper.readableDatabase

            memberCursor = getMembers(group_id)
            eventCursor = getEvents(group_id)

            memberCursor!!.moveToFirst()
            eventCursor!!.moveToFirst()

            mMembersList!!.setHasFixedSize(true)
            mEventsList!!.setHasFixedSize(true)

            mMemberAdapter = MemberAdapter(this, memberCursor, this, group_id)
            mEventAdapter = EventAdapter(this, eventCursor, this)

            mMembersList!!.adapter = mMemberAdapter
            mEventsList!!.adapter = mEventAdapter

            updateNullView(mMemberAdapter!!, memberNull)
            updateNullView(mEventAdapter!!, eventNull)

            //llContent.setVisibility(View.VISIBLE);

            if (expandedGroupId >= 0) {

                closingCollateral = true
                mGroupAdapter!!.hideExpandedGroup(expandedGroupId)
            }

            Log.d(TAG, "DEBUG MA 404")
            Useful.expand(llContent)

            //listGroupNameView.bringToFront();

            expandedGroupId = group_id

            Useful.rotate_down(this@MainActivity, arrow)

            Log.d(TAG, "DEBUG 363")
            famMain.open(true)
            fabDelete.show(true)
        } else {

            //llContent.setVisibility(View.GONE);

            //listGroupNameView.bringToFront();\

            Log.d(TAG, "DEBUG MA 377")

            if (!closingCollateral) {

                Useful.collapse(llContent, true)
                famMain.close(true)
                fabDelete.hide(true)
                //famMain.setVisibility(View.GONE);

                expandedGroupId = -1
            } else {

                Useful.collapse(llContent, false)
                closingCollateral = false
            }

            Useful.rotate_up(this@MainActivity, arrow)
        }
    }

    fun updateNullView(adapter: RecyclerView.Adapter<*>, textView: TextView?) {

        if (adapter.itemCount == 0) {

            textView!!.visibility = View.VISIBLE
        } else {

            textView!!.visibility = View.GONE
        }
    }

    fun addNewGroup(groupName: String): Long {

        val cv = ContentValues()

        cv.put(GroupContract.GroupEntry.COLUMN_GROUP_NAME, groupName)

        return mGroupDb!!.insertOrThrow(GroupContract.GroupEntry.TABLE_NAME, null, cv)
    }

    fun getMembers(groupID: Int): Cursor {

        //String[] selection = {MemberContract.MemberEntry.COLUMN_NAME};

        return memberDB!!.query(
                MemberContract.MemberEntry.TABLE_NAME, null,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "='" + groupID + "'", null, null, null, null
        )
    }

    fun getEvents(groupID: Int): Cursor {

        //String[] selection = {EventContract.EventEntry.COLUMN_EVENT_NAME};

        return eventDB!!.query(
                EventContract.EventEntry.TABLE_NAME, null,
                EventContract.EventEntry.COLUMN_GROUP_ID + "='" + groupID + "'", null, null, null, null
        )
    }

    override fun onClickEvent(event_id: Int, group_id: Int) {

        val i = Intent(context, EventDetailsActivity::class.java)

        //EVENT DB NOT NEEDED UNLIKE MAIN ACTIVITY BECAUSE GROUP ID IS ALREADY KNOWN (EVENT ID GIVEN AS PARAMETER)

        i.putExtra("eventID", event_id)
        i.putExtra("groupID", group_id)

        //saveGroupname();

        startActivity(i)
    }

    override fun onClick(name: String, memberID: Int, groupId: Int) {

        val intent = Intent(context, MemberDetailsActivity::class.java)

        intent.putExtra("name", name)

        intent.putExtra("groupID", groupId)
        intent.putExtra("firstMember", false)
        intent.putExtra("memberID", memberID)

        //saveGroupname();

        startActivity(intent)
    }

    fun saveGroupname(groupName: String, groupId: Int) {

        val cv1 = ContentValues()
        cv1.put(GroupContract.GroupEntry.COLUMN_GROUP_NAME, groupName)

        mGroupDb!!.update(GroupContract.GroupEntry.TABLE_NAME,
                cv1,
                GroupContract.GroupEntry._ID + "=" + groupId + "", null)
    }

    override fun onClickEdit(tvGroupName: TextView, groupId: Int) {

        val groupNAD = Useful.NameAlertDialog("Group", "DONE", "CANCEL", tvGroupName.text.toString(), EditText(this@MainActivity), this@MainActivity)
        groupNAD.create()
        groupNAD.ad.setOnShowListener {
            val btnPos = groupNAD.ad.getButton(DialogInterface.BUTTON_POSITIVE)
            groupNAD.setBtnColor()

            btnPos.setOnClickListener {
                val groupName = groupNAD.et.text.toString()
                tvGroupName.text = groupName
                saveGroupname(groupName, groupId)

                groupNAD.ad.dismiss()
            }
        }

        groupNAD.ad.show()
    }


    fun removeGroup(): Boolean {

        memberDB!!.delete(MemberContract.MemberEntry.TABLE_NAME, MemberContract.MemberEntry.COLUMN_GROUP_ID + "=" + expandedGroupId, null)
        eventDB!!.delete(EventContract.EventEntry.TABLE_NAME, EventContract.EventEntry.COLUMN_GROUP_ID + "=" + expandedGroupId, null)
        return mGroupDb!!.delete(GroupContract.GroupEntry.TABLE_NAME, GroupContract.GroupEntry._ID + "=" + expandedGroupId, null) > 0
    }

    companion object {

        lateinit var famMain: FloatingActionMenu
        var closingCollateral = false
    }
}
