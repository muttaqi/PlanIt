package com.planit.mobile.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.mobile.R;

import java.util.List;

/**
 * Created by Home on 2017-08-23.
 */

public class MemberPreferenceAdapter extends RecyclerView.Adapter<MemberPreferenceAdapter.MemberPreferenceViewHolder>{

    private final String TAG = MemberPreferenceAdapter.class.getSimpleName();

    private Context mContext;

    private final MemberPreferenceAdapterOnClickHandler mClickHandler;

    private List<String> mprefs;

    public interface MemberPreferenceAdapterOnClickHandler {

        void onClick(long preference_id, String isDelete);
    }

    public MemberPreferenceAdapter(Context context, List<String> inprefs, MemberPreferenceAdapterOnClickHandler listener) {

        mContext = context;

        mprefs = inprefs;

        mClickHandler = listener;
    }

    class MemberPreferenceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mprefView;
        ImageButton deleteView;

        public MemberPreferenceViewHolder(View itemView) {

            super(itemView);

            mprefView = (TextView) itemView.findViewById(R.id.tv_item_mpref);

            deleteView = (ImageButton) itemView.findViewById(R.id.ib_item_mpref);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            long id = Long.parseLong(String.valueOf(view.getTag()));

            mClickHandler.onClick(id, "false");
        }
    }

    @Override
    public MemberPreferenceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        boolean shouldAttachImmediatelyToParent = false;

        View view = inflater.inflate(R.layout.mpref_list_item, parent, shouldAttachImmediatelyToParent);
        MemberPreferenceViewHolder viewHolder = new MemberPreferenceViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MemberPreferenceViewHolder holder, final int position) {

        String MemberPreference = mprefs.get(position);

        holder.mprefView.setText(MemberPreference);

        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "Removed " + mprefs.get((int) (position)), Toast.LENGTH_SHORT).show();

                mprefs.remove(position);

                notifyDataSetChanged();

                notifyItemRangeChanged(position, mprefs.size());

                mClickHandler.onClick(position, "true");
            }}
        );
    }

    @Override
    public int getItemCount() {
        return mprefs.size();
    }
}
