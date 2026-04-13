package com.example.iskolarphh.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;

import java.util.Locale;

public class ScholarshipAdapter extends ListAdapter<Scholarship, ScholarshipAdapter.ScholarshipViewHolder> {

    private final OnScholarshipClickListener listener;

    public interface OnScholarshipClickListener {
        void onScholarshipClick(Scholarship scholarship);
    }

    public ScholarshipAdapter(OnScholarshipClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Scholarship> DIFF_CALLBACK = new DiffUtil.ItemCallback<Scholarship>() {
        @Override
        public boolean areItemsTheSame(@NonNull Scholarship oldItem, @NonNull Scholarship newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Scholarship oldItem, @NonNull Scholarship newItem) {
            return oldItem.getScholarshipName().equals(newItem.getScholarshipName()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getLocation().equals(newItem.getLocation());
        }
    };

    @NonNull
    @Override
    public ScholarshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_scholarship, parent, false);
        return new ScholarshipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScholarshipViewHolder holder, int position) {
        Scholarship scholarship = getItem(position);
        holder.bind(scholarship, listener);
    }

    static class ScholarshipViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvLocationTag, tvScholarshipName, tvProvider, tvDeadline, tvAmount, tvShortDescription;
        private final Button btnSeeMore;

        public ScholarshipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLocationTag = itemView.findViewById(R.id.tv_location_tag);
            tvScholarshipName = itemView.findViewById(R.id.tv_scholarship_name);
            tvProvider = itemView.findViewById(R.id.tv_provider);
            tvDeadline = itemView.findViewById(R.id.tv_deadline);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvShortDescription = itemView.findViewById(R.id.tv_short_description);
            btnSeeMore = itemView.findViewById(R.id.btn_see_more);
        }

        public void bind(Scholarship scholarship, OnScholarshipClickListener listener) {
            tvLocationTag.setText(scholarship.getLocation() != null ? scholarship.getLocation().toUpperCase() : "N/A");
            tvScholarshipName.setText(scholarship.getScholarshipName());
            tvProvider.setText(scholarship.getProviderOrganization());
            tvDeadline.setText("Deadline: " + scholarship.getApplicationDeadline());
            tvAmount.setText(String.format(Locale.getDefault(), "Award: ₱%.2f", scholarship.getAwardAmount()));
            tvShortDescription.setText(scholarship.getDescription());

            View.OnClickListener clickListener = v -> {
                if (listener != null) {
                    listener.onScholarshipClick(scholarship);
                }
            };

            itemView.setOnClickListener(clickListener);
            btnSeeMore.setOnClickListener(clickListener);
        }
    }
}
