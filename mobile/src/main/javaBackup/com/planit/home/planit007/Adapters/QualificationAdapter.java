package planit007.planit.home.planit007.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.planit.home.planit003.R;

import java.util.List;

/**
 * Created by Home on 2017-08-23.
 */

/**
     * Created by Home on 2017-08-23.
     */

    public class QualificationAdapter extends RecyclerView.Adapter<QualificationAdapter.QualificationViewHolder>{

        private final String TAG = QualificationAdapter.class.getSimpleName();

        private Context mContext;

        List<String> quals;

        TextView qualNullView;

        public QualificationAdapter(Context context, List<String> inQuals, TextView nullView) {

            mContext = context;

            qualNullView = nullView;

            for (String q : inQuals) {

                if (q.equals("")) {

                    inQuals.remove(q);
                }
            }

            quals = inQuals;
        }

        class QualificationViewHolder extends RecyclerView.ViewHolder {

            TextView qualView;
            ImageButton deleteView;

            public QualificationViewHolder(View itemView) {

                super(itemView);

                qualView = (TextView) itemView.findViewById(planit.planit.home.planit003.R.id.tv_item_qual);

                deleteView = (ImageButton) itemView.findViewById(planit.planit.home.planit003.R.id.ib_item_qual);
            }
        }

        @Override
        public QualificationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());

            boolean shouldAttachImmediatelyToParent = false;

            View view = inflater.inflate(planit.planit.home.planit003.R.layout.qual_list_item, parent, shouldAttachImmediatelyToParent);
            QualificationViewHolder viewHolder = new QualificationViewHolder(view);

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(QualificationViewHolder holder, final int position) {

            String qualification = quals.get(position);

            if (!qualification.equals("")) {

                holder.qualView.setText(qualification);

                holder.deleteView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {

                        Log.d(TAG, "DEBUG ONCLICK PAD " + position);

                        Toast.makeText(mContext, "Removed " + quals.get((int) (position)), Toast.LENGTH_SHORT).show();

                        quals.remove(position);

                        notifyDataSetChanged();

                        notifyItemRangeChanged(position, quals.size());

                        updateNullView(qualNullView);
                    }
                });
            }
        }

    public List<String> getQuals() {

        return quals;
    }

    @Override
        public int getItemCount() {

            return quals.size();
        }

    public void updateNullView(TextView textView) {
        if (quals.size() == 0) {

            textView.setVisibility(View.VISIBLE);
        }

        else {

            textView.setVisibility(View.GONE);
        }
    }
}

