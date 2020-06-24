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

public class MemberQualificationAdapter extends RecyclerView.Adapter<MemberQualificationAdapter.MemberQualificationViewHolder>{

    private final String TAG = MemberQualificationAdapter.class.getSimpleName();

    private Context mContext;

    private final MemberQualificationAdapterOnClickHandler mClickHandler;

    List<String> mquals;

    public interface MemberQualificationAdapterOnClickHandler {

        void onClick(long qualification_id, boolean isDelete);
    }

    public MemberQualificationAdapter(Context context, List<String> inQuals, MemberQualificationAdapterOnClickHandler listener) {

        mContext = context;

        mquals = inQuals;

        mClickHandler = listener;
    }

    class MemberQualificationViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView mQualView;
        ImageButton deleteView;

        public MemberQualificationViewHolder(View itemView) {

            super(itemView);

            mQualView = (TextView) itemView.findViewById(R.id.tv_item_mqual);

            deleteView = (ImageButton) itemView.findViewById(R.id.ib_item_mqual);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {

            long id = Long.parseLong(String.valueOf(view.getTag()));

            mClickHandler.onClick(id, false);
        }
    }

    @Override
    public MemberQualificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        boolean shouldAttachImmediatelyToParent = false;

        View view = inflater.inflate(R.layout.mqual_list_item, parent, shouldAttachImmediatelyToParent);
        MemberQualificationViewHolder viewHolder = new MemberQualificationViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MemberQualificationViewHolder holder, final int position) {

        final String MemberQualification = mquals.get(position);

        holder.mQualView.setText(MemberQualification);

        holder.deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Toast.makeText(mContext, "Removed " + mquals.get((int) (position)), Toast.LENGTH_SHORT).show();

                mquals.remove(position);

                notifyDataSetChanged();

                notifyItemRangeChanged(position, mquals.size());

                mClickHandler.onClick(position, true);
            }
        });

        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {

        return mquals.size();
    }
}

