package com.planit.mobile.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.planit.mobile.MainActivity;
import com.planit.mobile.data.Contracts.GroupContract;
import com.planit.mobile.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Home on 2017-08-01.
 */

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder>{

    //Used for logs
    private static final String TAG = GroupAdapter.class.getSimpleName();

    //Holds number of items
    private Cursor mCursor;

    private Context mContext;

    private List<View> groups = new ArrayList<View>();

    private final GroupAdapterOnClickHandler mClickHandler;

    public interface GroupAdapterOnClickHandler {

        void onClick(int group_id, RecyclerView memberList, RecyclerView eventList, TextView memberNull,
                     TextView eventNull, LinearLayout llContent, TextView listGroupNameView, ImageView arrow);

        void onClickEdit(final TextView tvGroupName, final int groupId);
    }

    //Constructor
    public GroupAdapter(Context context, Cursor cursor, GroupAdapterOnClickHandler listener) {

        this.mContext = context;
        this.mCursor = cursor;

        mClickHandler = listener;
    }

    //HANDLES ON CLICK

    //WHERE TO SHOW DATA

    class GroupViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView listGroupNameView;

        RecyclerView listMemberList;
        RecyclerView listEventList;

        TextView listMemberNull;
        TextView listEventNull;

        LinearLayout llContent;

        ImageView arrow;
        ImageView editName;

        public GroupViewHolder(final View itemView) {

            super(itemView);

            groups.add(itemView);

            listGroupNameView = (TextView) itemView.findViewById(R.id.gl_tv_group_name);
            listGroupNameView.bringToFront();

            listMemberList = (RecyclerView) itemView.findViewById(R.id.gli_rv_members);
            listEventList = (RecyclerView) itemView.findViewById(R.id.gli_rv_events);

            listMemberNull = (TextView) itemView.findViewById(R.id.gli_tv_members_null);
            listEventNull = (TextView) itemView.findViewById(R.id.gli_tv_events_null);

            llContent = (LinearLayout) itemView.findViewById(R.id.gli_ll_content);

            arrow = (ImageView) itemView.findViewById(R.id.gl_iv_arrow);
            editName = (ImageView) itemView.findViewById(R.id.gl_iv_edit);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            int id = (int) (view.getTag());

            mClickHandler.onClick(id, listMemberList, listEventList, listMemberNull, listEventNull, llContent, listGroupNameView, arrow);
        }
    }

    //Inflates text view
    public GroupViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(R.layout.group_list_item, viewGroup, shouldAttachToParentImmediately);
        GroupViewHolder viewHolder = new GroupViewHolder(view);

        return viewHolder;
    }

    //WHAT DATA TO SHOW

    @Override
    public void onBindViewHolder(final GroupViewHolder holder, int position) {

        if(!mCursor.moveToPosition(position)) {

            return;
        }

        String name = mCursor.getString(mCursor.getColumnIndex(GroupContract.GroupEntry.Companion.getCOLUMN_GROUP_NAME()));
        final int id = mCursor.getInt(mCursor.getColumnIndex(GroupContract.GroupEntry._ID));

        holder.listGroupNameView.setText(name);

        holder.itemView.setTag(id);

        holder.editName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mClickHandler.onClickEdit(holder.listGroupNameView, id);
            }
        });

        holder.llContent.setVisibility(View.GONE);
    }

    public void hideExpandedGroup(int groupId) {

        for (View v : groups) {

            if (Integer.valueOf(v.getTag().toString()) == groupId) {

                v.callOnClick();
            }
        }
    }

    //returns number of items
    public int getItemCount() {return mCursor.getCount();}

    public void refreshCursor(Cursor cursor) {

        mCursor = cursor;
    }
}
