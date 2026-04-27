package com.example.iskolarphh.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iskolarphh.R;
import com.example.iskolarphh.database.entity.Scholarship;
import com.example.iskolarphh.ui.ScholarshipDetailActivity;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RecyclerView adapter for displaying scholarship cards with view recycling
 * Optimized for performance with ViewHolder pattern and DiffUtil
 */
public class ScholarshipAdapter extends ListAdapter<Scholarship, ScholarshipAdapter.ScholarshipViewHolder> {

    private OnScholarshipClickListener clickListener;

    public interface OnScholarshipClickListener {
        void onScholarshipClick(Scholarship scholarship);
    }

    private static final DiffUtil.ItemCallback<Scholarship> DIFF_CALLBACK = new DiffUtil.ItemCallback<Scholarship>() {
        @Override
        public boolean areItemsTheSame(@NonNull Scholarship oldItem, @NonNull Scholarship newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Scholarship oldItem, @NonNull Scholarship newItem) {
            return Objects.equals(oldItem.getScholarshipName(), newItem.getScholarshipName()) &&
                    Objects.equals(oldItem.getDescription(), newItem.getDescription()) &&
                    oldItem.getAwardAmount() == newItem.getAwardAmount() &&
                    Objects.equals(oldItem.getProviderOrganization(), newItem.getProviderOrganization()) &&
                    Objects.equals(oldItem.getApplicationDeadline(), newItem.getApplicationDeadline()) &&
                    oldItem.isSaved() == newItem.isSaved();
        }
    };

    public ScholarshipAdapter() {
        super(DIFF_CALLBACK);
    }

    public ScholarshipAdapter(OnScholarshipClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ScholarshipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_scholarship, parent, false);
        return new ScholarshipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScholarshipViewHolder holder, int position) {
        Scholarship scholarship = getItem(position);
        holder.bind(scholarship);
    }

    @Override
    public int getItemCount() {
        return getCurrentList().size();
    }

    /**
     * ViewHolder pattern for efficient view recycling
     */
    public static class ScholarshipViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView providerView;
        private final TextView deadlineView;
        private final TextView amountView;
        private final TextView descriptionView;
        private final TextView locationTagView;
        private final View cardView;

        public ScholarshipViewHolder(@NonNull View itemView) {
            super(itemView);
            // Cache view references for performance
            cardView = itemView.findViewById(R.id.card_scholarship);
            titleView = itemView.findViewById(R.id.tv_scholarship_name);
            providerView = itemView.findViewById(R.id.tv_provider);
            deadlineView = itemView.findViewById(R.id.tv_deadline);
            amountView = itemView.findViewById(R.id.tv_amount);
            descriptionView = itemView.findViewById(R.id.tv_short_description);
            locationTagView = itemView.findViewById(R.id.tv_location_tag);
        }

        /**
         * Binds scholarship data to the ViewHolder views
         * Optimized to minimize view operations
         */
        public void bind(Scholarship scholarship) {
            // Set scholarship data
            titleView.setText(scholarship.getScholarshipName());
            deadlineView.setText("Deadline: " + scholarship.getApplicationDeadline());
            
            // Set optional fields with null checks
            providerView.setText(scholarship.getProviderOrganization() != null ? scholarship.getProviderOrganization() : "Provider Information");
            amountView.setText("Award Amount: ₱" + String.format("%.2f", scholarship.getAwardAmount()));
            descriptionView.setText(scholarship.getDescription() != null ? scholarship.getDescription() : "Scholarship description not available");
            locationTagView.setText(scholarship.getLocation() != null ? scholarship.getLocation() : "GENERAL");
            
            // Set click listener for navigation
            cardView.setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), ScholarshipDetailActivity.class);
                intent.putExtra(ScholarshipDetailActivity.EXTRA_SCHOLARSHIP_ID, scholarship.getId());
                v.getContext().startActivity(intent);
            });
        }
    }
}
