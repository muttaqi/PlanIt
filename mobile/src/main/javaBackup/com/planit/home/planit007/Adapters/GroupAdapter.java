package com.planit.home.planit007.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.planit.home.planit003.R;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.data.Contracts.MemberContract;
import com.planit.mobile.data.DbHelpers.MemberDbHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 2017-08-01.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    //Used for logs
    private static final String TAG = GroupAdapter.class.getSimpleName();

    private SQLiteDatabase memberDB;

    //Holds number of items
    private Cursor mCursor;
    private Cursor MemberCursor;

    private Context mContext;

    private final GroupAdapterOnClickHandler mClickHandler;

    public interface GroupAdapterOnClickHandler {

        void onClick(int group_id);
    }

    //Constructor
    public GroupAdapter(Context context, Cursor cursor, GroupAdapterOnClickHandler listener) {

        MemberDbHelper memberDbHelper = new MemberDbHelper(context);
        memberDB = memberDbHelper.getReadableDatabase();

        this.mContext = context;
        this.mCursor = cursor;

        mClickHandler = listener;
    }

    //HANDLES ON CLICK

    //WHERE TO SHOW DATA

    class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listGroupNameView;
        TextView listGroupMembersView;

        public GroupViewHolder(View itemView) {

            super(itemView);

            listGroupNameView = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.gl_tv_group_name);

            listGroupMembersView = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.gl_tv_group_members);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int id = (int) (view.getTag());

            mClickHandler.onClick(id);
        }
    }

    //Inflates text view
    public GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(planit.planit.home.planit003.R.layout.group_list_item, viewGroup, shouldAttachToParentImmediately);
        GroupViewHolder viewHolder = new GroupViewHolder(view);

        return viewHolder;
    }

    //WHAT DATA TO SHOW

    @Override
    public void onBindViewHolder(GroupViewHolder holder, int position) {

        if(!mCursor.moveToPosition(position)) {

            return;
        }

        String name = mCursor.getString(mCursor.getColumnIndex(GroupContract.GroupEntry.COLUMN_GROUP_NAME));
        int id = mCursor.getInt(mCursor.getColumnIndex(GroupContract.GroupEntry._ID));

        holder.listGroupNameView.setText(name);

        MemberCursor = getMembers(id);

        List<String> names = new ArrayList<String>();

        if(MemberCursor.equals(null)) {

            Log.d(TAG, "DEBUG GA 122 MEMBER CURSOR IS BROKEN");
        };

        if(MemberCursor != null && MemberCursor.moveToFirst()) {

            do {

                names.add(MemberCursor.getString(MemberCursor.getColumnIndex(MemberContract.MemberEntry.COLUMN_NAME)));
            }

            while (MemberCursor.moveToNext());
        }

        String members = "";

        for (int i = 0; i < names.size(); i ++) {

            Log.d(TAG, "DEBUG GA 150 " + names.get(i));
            members += names.get(i) + ", ";
        }

        Log.d(TAG, "DEBUG 80 " + String.valueOf(members));

        if (members.length() > 2) {

            holder.listGroupMembersView.setText(members.substring(0, members.length() - 2));
        }

        else {

            holder.listGroupMembersView.setTypeface(holder.listGroupMembersView.getTypeface(), Typeface.ITALIC);
            holder.listGroupMembersView.setText("no members found");
        }

        holder.itemView.setTag(mCursor.getInt(mCursor.getColumnIndex(GroupContract.GroupEntry._ID)));
        Log.d(TAG, "DEBUG 133 " + mCursor.getInt(mCursor.getColumnIndex(GroupContract.GroupEntry._ID)));
    }

    //returns number of items
    public int getItemCount() {return mCursor.getCount();}

    public Cursor getMembers(int groupID) {

        String[] selection = {MemberContract.MemberEntry.COLUMN_NAME};

        return memberDB.query(
                MemberContract.MemberEntry.TABLE_NAME,
                selection,
                MemberContract.MemberEntry.COLUMN_GROUP_ID + "='" + groupID + "'",
                null,
                null,
                null,
                null
        );
    }
}
