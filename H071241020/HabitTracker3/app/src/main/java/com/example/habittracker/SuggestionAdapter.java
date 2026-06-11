package com.example.habittracker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habittracker.databinding.ItemSuggestionBinding;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    private final List<HabitSuggestionEngine.Suggestion> suggestions;
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onAddClicked(HabitSuggestionEngine.Suggestion suggestion);
    }

    public SuggestionAdapter(List<HabitSuggestionEngine.Suggestion> suggestions, OnSuggestionClickListener listener) {
        this.suggestions = suggestions;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSuggestionBinding binding = ItemSuggestionBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HabitSuggestionEngine.Suggestion item = suggestions.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSuggestionBinding binding;

        public ViewHolder(ItemSuggestionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final HabitSuggestionEngine.Suggestion suggestion, final OnSuggestionClickListener listener) {
            binding.textSuggestionTitle.setText(suggestion.getTitle());
            binding.textSuggestionCategory.setText(suggestion.getCategory());
            binding.textSuggestionDescription.setText(suggestion.getDescription());

            // Get relevant emoji icon
            String emoji = getEmojiForSuggestion(suggestion.getTitle(), suggestion.getCategory());
            binding.textSuggestionEmoji.setText(emoji);

            Context context = itemView.getContext();

            // Category tag colors
            int textTint;
            int bgTint;
            if (suggestion.getCategory().equalsIgnoreCase("Kesehatan")) {
                textTint = context.getResources().getColor(R.color.difficulty_easy);
                bgTint = 0x1A006A6A; // 10% opacity
                binding.textSuggestionEmoji.setBackgroundTintList(ColorStateList.valueOf(0x10006A6A));
            } else if (suggestion.getCategory().equalsIgnoreCase("Karir")) {
                textTint = context.getResources().getColor(R.color.priority_high);
                bgTint = 0x1AE06000; // 10% opacity
                binding.textSuggestionEmoji.setBackgroundTintList(ColorStateList.valueOf(0x10E06000));
            } else { // Mental
                textTint = context.getResources().getColor(R.color.difficulty_hard);
                bgTint = 0x1A8F0099; // 10% opacity
                binding.textSuggestionEmoji.setBackgroundTintList(ColorStateList.valueOf(0x108F0099));
            }

            binding.textSuggestionCategory.setTextColor(textTint);
            binding.textSuggestionCategory.setBackgroundTintList(ColorStateList.valueOf(bgTint));

            // Set clicks on entire card or "Tambah" button
            binding.btnAddSuggestion.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClicked(suggestion);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAddClicked(suggestion);
                }
            });
        }

        private String getEmojiForSuggestion(String title, String category) {
            String lower = title.toLowerCase();
            if (lower.contains("air") || lower.contains("hidrasi") || lower.contains("minum")) return "💧";
            if (lower.contains("stretching") || lower.contains("peregangan") || lower.contains("yoga")) return "🤸";
            if (lower.contains("langkah") || lower.contains("jalan") || lower.contains("gerak")) return "👟";
            if (lower.contains("lari") || lower.contains("jogging")) return "🏃";
            if (lower.contains("calisthenics") || lower.contains("push-up") || lower.contains("plank") || lower.contains("squat")) return "💪";
            if (lower.contains("tidur") || lower.contains("ranjang") || lower.contains("bantal")) return "😴";
            if (lower.contains("buku") || lower.contains("baca") || lower.contains("membaca") || lower.contains("jurnal")) return "📚";
            if (lower.contains("meditasi") || lower.contains("napas") || lower.contains("tenang")) return "🧘";
            if (lower.contains("teh") || lower.contains("herbal")) return "☕";
            if (lower.contains("tanaman") || lower.contains("bunga") || lower.contains("siram")) return "🌱";
            if (lower.contains("uang") || lower.contains("anggaran") || lower.contains("belanja") || lower.contains("pengeluaran")) return "💵";
            if (lower.contains("kerja") || lower.contains("inbox") || lower.contains("email") || lower.contains("linkedin")) return "💼";
            if (lower.contains("speaking") || lower.contains("cermin") || lower.contains("presentasi")) return "🎤";
            if (lower.contains("afirmasi") || lower.contains("puji") || lower.contains("syukur")) return "✨";

            // Category defaults
            if (category.equalsIgnoreCase("Kesehatan")) return "🍎";
            if (category.equalsIgnoreCase("Karir")) return "📈";
            return "🧠"; // "Mental" default
        }
    }
}
