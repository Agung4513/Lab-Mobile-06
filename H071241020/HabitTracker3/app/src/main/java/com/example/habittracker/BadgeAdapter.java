package com.example.habittracker;

import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habittracker.BadgeItem;

import java.util.List;

public class BadgeAdapter extends RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder> {

    private final List<BadgeItem> badgeList;

    public BadgeAdapter(List<BadgeItem> badgeList) {
        this.badgeList = badgeList;
    }

    @NonNull
    @Override
    public BadgeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_badge, parent, false);
        return new BadgeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BadgeViewHolder holder, int position) {
        BadgeItem badge = badgeList.get(position);
        holder.bind(badge);
    }

    @Override
    public int getItemCount() {
        return badgeList.size();
    }

    public static class BadgeViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageIcon;
        private final TextView textName;
        private final TextView textCondition;

        public BadgeViewHolder(@NonNull View itemView) {
            super(itemView);
            imageIcon = itemView.findViewById(R.id.image_badge_icon);
            textName = itemView.findViewById(R.id.text_badge_name);
            textCondition = itemView.findViewById(R.id.text_badge_condition);
        }

        public void bind(final BadgeItem badge) {
            textName.setText(badge.getTitle());
            textCondition.setText(badge.getDescription());
            imageIcon.setImageResource(badge.getIconResId());

            if (badge.isUnlocked()) {
                // Remove grayscale filter & set normal alpha level
                imageIcon.setColorFilter(null);
                itemView.setAlpha(1.0f);
            } else {
                // Apply grayscale filter
                ColorMatrix matrix = new ColorMatrix();
                matrix.setSaturation(0); // 0 means completely grayscale
                ColorMatrixColorFilter filter = new ColorMatrixColorFilter(matrix);
                imageIcon.setColorFilter(filter);
                itemView.setAlpha(0.6f); // Lower card opacity to visualize the locked state clearly
            }

            // Click listener for showing details
            itemView.setOnClickListener(v -> {
                String statusStr = badge.isUnlocked() ? " [Terbuka! 🎉]" : " [Terkunci 🔒]";
                Toast.makeText(v.getContext(), 
                        badge.getTitle() + statusStr + "\n" + badge.getDescription(), 
                        Toast.LENGTH_SHORT).show();
            });
        }
    }
}
