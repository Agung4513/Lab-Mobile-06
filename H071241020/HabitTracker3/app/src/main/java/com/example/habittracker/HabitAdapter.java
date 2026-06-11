package com.example.habittracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.databinding.ItemHabitBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {

    private final List<Habit> habitList;
    private final HabitInteractionListener listener;
    private final String todayDateString;

    public interface HabitInteractionListener {
        void onCheckIn(Habit habit, int position);
        void onLongPress(Habit habit, int position);
    }

    public HabitAdapter(List<Habit> habitList, HabitInteractionListener listener) {
        this.habitList = habitList;
        this.listener = listener;
        this.todayDateString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemHabitBinding binding = ItemHabitBinding.inflate(inflater, parent, false);
        return new HabitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        holder.bind(habitList.get(position), listener, todayDateString);
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public static class HabitViewHolder extends RecyclerView.ViewHolder {
        private final ItemHabitBinding binding;
        private final Context context;

        public HabitViewHolder(ItemHabitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = binding.getRoot().getContext();
        }

        public void bind(final Habit habit, final HabitInteractionListener listener, String todayDateString) {
            binding.textHabitTitle.setText(habit.getTitle());
            
            // Build schedule string
            String scheduleText = habit.getTime() + " · " + habit.getDaysOfWeek();
            binding.textHabitSchedule.setText(scheduleText);
            
            // Streak representation
            String freqText = "Streak: " + habit.getStreakCount() + " hari";
            binding.textFrequency.setText(freqText);

            // Populate Priority Badge
            binding.textPriorityBadge.setText(habit.getPriority());
            int pColor;
            switch (habit.getPriority()) {
                case "Urgent":
                    pColor = ContextCompat.getColor(context, R.color.priority_urgent);
                    break;
                case "Tinggi":
                    pColor = ContextCompat.getColor(context, R.color.priority_high);
                    break;
                case "Sedang":
                    pColor = ContextCompat.getColor(context, R.color.priority_medium);
                    break;
                default:
                    pColor = ContextCompat.getColor(context, R.color.priority_low);
                    break;
            }
            binding.textPriorityBadge.setBackgroundTintList(ColorStateList.valueOf(pColor));

            // Populate Difficulty Badge
            binding.textDifficultyBadge.setText(habit.getDifficulty());
            int dColor;
            switch (habit.getDifficulty()) {
                case "Sulit":
                    dColor = ContextCompat.getColor(context, R.color.difficulty_hard);
                    break;
                case "Sedang":
                    dColor = ContextCompat.getColor(context, R.color.difficulty_medium);
                    break;
                default:
                    dColor = ContextCompat.getColor(context, R.color.difficulty_easy);
                    break;
            }
            binding.textDifficultyBadge.setBackgroundTintList(ColorStateList.valueOf(dColor));

            // Check completion state to bind action buttons
            boolean isCompleted = todayDateString.equals(habit.getLastCheckInDate());
            if (isCompleted) {
                applyCompletedStyle();
            } else {
                applyPendingStyle(habit, listener, getBindingAdapterPosition());
            }

            // Long Press trigger for Edit/Delete dialog options
            binding.getRoot().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (listener != null) {
                        listener.onLongPress(habit, getBindingAdapterPosition());
                    }
                    return true;
                }
            });
        }

        private void applyCompletedStyle() {
            binding.btnCompleteAction.setText(context.getString(R.string.status_completed));
            binding.btnCompleteAction.setEnabled(false);
            binding.btnCompleteAction.setBackgroundColor(Color.LTGRAY);
            binding.btnCompleteAction.setTextColor(Color.DKGRAY);
        }

        private void applyPendingStyle(final Habit habit, final HabitInteractionListener listener, final int position) {
            binding.btnCompleteAction.setText(context.getString(R.string.status_pending));
            binding.btnCompleteAction.setEnabled(true);
            binding.btnCompleteAction.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
            binding.btnCompleteAction.setTextColor(Color.WHITE);

            binding.btnCompleteAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // OPTIMISTIC UI UPDATE
                    // Instantly set the state of current layout button to look disabled and checked on tap,
                    // without waiting for SQlite threads to roundtrip!
                    binding.btnCompleteAction.setText(context.getString(R.string.status_completed));
                    binding.btnCompleteAction.setEnabled(false);
                    binding.btnCompleteAction.setBackgroundColor(Color.LTGRAY);
                    binding.btnCompleteAction.setTextColor(Color.DKGRAY);

                    if (listener != null) {
                        listener.onCheckIn(habit, position);
                    }
                }
            });
        }
    }
}
