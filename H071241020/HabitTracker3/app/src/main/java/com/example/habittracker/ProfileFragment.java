package com.example.habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.habittracker.SocialAdapter;
import com.example.habittracker.SocialPost;
import com.example.habittracker.databinding.FragmentProfileBinding;
import com.google.android.material.button.MaterialButton;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private DatabaseHelper dbHelper;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    private int mTotalCreated = 0;
    private int mTotalCheckins = 0;
    private int mMaxStreak = 0;
    private List<Habit> mTopHabits = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        // Setup badges recycler grid view
        binding.recyclerBadges.setLayoutManager(new GridLayoutManager(requireContext(), 3));

        loadProfileAndAchievements();
        setupDarkModeSwitch();

        // Click listeners
        binding.btnShareAchievements.setOnClickListener(v -> shareAchievements());
        binding.btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });
        binding.avatarContainer.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), EditProfileActivity.class);
            startActivity(intent);
        });
    }

    private void setupDarkModeSwitch() {
        if (binding == null || binding.switchDarkMode == null) return;

        // Retrieve current active override or fallback to system configuration
        int nightMode = androidx.appcompat.app.AppCompatDelegate.getDefaultNightMode();
        boolean isDarkByDefault;
        
        if (nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES) {
            isDarkByDefault = true;
        } else if (nightMode == androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO) {
            isDarkByDefault = false;
        } else {
            // Check current configuration UI Mode
            int currentNightMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            isDarkByDefault = (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        }

        // Apply checked state without triggering a listener
        binding.switchDarkMode.setChecked(isDarkByDefault);

        binding.switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("pref_dark_mode", isChecked).apply();

            int targetMode = isChecked ? androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES : androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
            androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(targetMode);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProfileAndAchievements();
    }

    private void loadProfileAndAchievements() {
        if (getActivity() == null || binding == null) return;

        // Load name & bio & custom photo from preferences
        SharedPreferences prefs = requireContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
        String savedName = prefs.getString("pref_user_name", "Pejuang Disiplin");
        String savedBio = prefs.getString("pref_user_bio", "\"Konsistensi mengalahkan bakat!\"");
        String savedEmoji = prefs.getString("pref_user_avatar_emoji", "👤");
        String savedPhotoPath = prefs.getString("pref_user_avatar_path", "");

        // Apply immediately to views
        binding.textUserName.setText(savedName);
        binding.textUserBio.setText(savedBio);

        if (!savedPhotoPath.isEmpty()) {
            File photoFile = new File(savedPhotoPath);
            if (photoFile.exists()) {
                binding.cardAvatarPhoto.setVisibility(View.VISIBLE);
                binding.imageAvatar.setImageURI(Uri.fromFile(photoFile));
                binding.textAvatarEmoji.setVisibility(View.GONE);
            } else {
                binding.cardAvatarPhoto.setVisibility(View.GONE);
                binding.textAvatarEmoji.setVisibility(View.VISIBLE);
                binding.textAvatarEmoji.setText(savedEmoji);
            }
        } else {
            binding.cardAvatarPhoto.setVisibility(View.GONE);
            binding.textAvatarEmoji.setVisibility(View.VISIBLE);
            binding.textAvatarEmoji.setText(savedEmoji);
        }

        diskExecutor.execute(() -> {
            // Fetch stats and top habits asynchronously
            final int totalCreated = dbHelper.getTotalHabitsEverCreated();
            final int totalCheckins = dbHelper.getTotalSessions();
            final int maxStreak = dbHelper.getMaxStreak();
            final List<Habit> topHabits = dbHelper.getTopHabits(3);
            final List<SocialPost> userPosts = dbHelper.getUserPosts();

            mTotalCreated = totalCreated;
            mTotalCheckins = totalCheckins;
            mMaxStreak = maxStreak;
            mTopHabits = topHabits;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (binding == null) return;

                    // 1. Update text counters
                    binding.textTotalCreatedCount.setText(String.valueOf(totalCreated));
                    binding.textTotalCheckinsCount.setText(String.valueOf(totalCheckins));

                    // 2. Evaluate Badge List and Bind Adapter
                    List<BadgeItem> badgeList = evaluateBadges(totalCreated, totalCheckins, maxStreak);
                    BadgeAdapter adapter = new BadgeAdapter(badgeList);
                    binding.recyclerBadges.setAdapter(adapter);

                    // 3. Render Top Habits UI
                    if (topHabits == null || topHabits.isEmpty()) {
                        binding.textTopHabitsEmpty.setVisibility(View.VISIBLE);
                        binding.cardTop1.setVisibility(View.GONE);
                        binding.cardTop2.setVisibility(View.GONE);
                        binding.cardTop3.setVisibility(View.GONE);
                    } else {
                        binding.textTopHabitsEmpty.setVisibility(View.GONE);

                        // Position 1 top habit
                        if (topHabits.size() >= 1) {
                            binding.cardTop1.setVisibility(View.VISIBLE);
                            binding.textTop1Title.setText(topHabits.get(0).getTitle());
                            binding.textTop1Streak.setText(topHabits.get(0).getStreakCount() + " Sesi");
                        } else {
                            binding.cardTop1.setVisibility(View.GONE);
                        }

                        // Position 2 top habit
                        if (topHabits.size() >= 2) {
                            binding.cardTop2.setVisibility(View.VISIBLE);
                            binding.textTop2Title.setText(topHabits.get(1).getTitle());
                            binding.textTop2Streak.setText(topHabits.get(1).getStreakCount() + " Sesi");
                        } else {
                            binding.cardTop2.setVisibility(View.GONE);
                        }

                        // Position 3 top habit
                        if (topHabits.size() >= 3) {
                            binding.cardTop3.setVisibility(View.VISIBLE);
                            binding.textTop3Title.setText(topHabits.get(2).getTitle());
                            binding.textTop3Streak.setText(topHabits.get(2).getStreakCount() + " Sesi");
                        } else {
                            binding.cardTop3.setVisibility(View.GONE);
                        }
                    }

                    // 4. Render User Community Activities UI
                    if (userPosts == null || userPosts.isEmpty()) {
                        binding.textUserPostsEmpty.setVisibility(View.VISIBLE);
                        binding.recyclerUserActivities.setVisibility(View.GONE);
                    } else {
                        binding.textUserPostsEmpty.setVisibility(View.GONE);
                        binding.recyclerUserActivities.setVisibility(View.VISIBLE);
                        binding.recyclerUserActivities.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
                        SocialAdapter userActivitiesAdapter = new SocialAdapter(getContext(), userPosts);
                        binding.recyclerUserActivities.setAdapter(userActivitiesAdapter);
                    }
                });
            }
        });
    }

    private List<BadgeItem> evaluateBadges(int totalCreated, int totalCheckins, int maxStreak) {
        List<BadgeItem> list = new ArrayList<>();
        list.add(new BadgeItem("step", "First Step", "Bikin 1 task", totalCreated >= 1, R.drawable.ic_badge_step));
        list.add(new BadgeItem("planner", "Planner", "Bikin 10 task", totalCreated >= 10, R.drawable.ic_badge_planner));
        list.add(new BadgeItem("starter", "Starter", "Check-in 1 kali", totalCheckins >= 1, R.drawable.ic_badge_starter));
        list.add(new BadgeItem("consistent", "Consistent", "Check-in 50 kali", totalCheckins >= 50, R.drawable.ic_badge_consistent));
        list.add(new BadgeItem("master", "Master", "Check-in 100 kali", totalCheckins >= 100, R.drawable.ic_badge_master));
        list.add(new BadgeItem("resilient", "Resilient", "Streak 3 hari", maxStreak >= 3, R.drawable.ic_badge_resilient));
        return list;
    }

    private void shareAchievements() {
        if (getContext() == null) return;

        // Fetch name for inclusion in the share text to make it extremely personalised!
        SharedPreferences prefs = requireContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
        String savedName = prefs.getString("pref_user_name", "Pejuang Disiplin");

        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("🏆 *PENCAPAIAN DISIPLIN SAYA* 🏆\n\n");
        textBuilder.append("Hai! Saya *").append(savedName).append("* sedang mendisiplinkan diri membangun kebiasaan positif menggunakan *Habit Tracker Pro*!\n");
        textBuilder.append("Berikut adalah rangkuman perjalanan olahraga & pengembangan diri saya:\n\n");

        textBuilder.append("📈 *Statistik Aktivitas:* \n");
        textBuilder.append("• Total Target Kebiasaan Dibuat: ").append(mTotalCreated).append("\n");
        textBuilder.append("• Total Check-in Sesi Diselesaikan: ").append(mTotalCheckins).append(" kali\n\n");
        textBuilder.append("🔥 *Streak Beruntun Tertinggi:* ").append(mMaxStreak).append(" hari\n\n");

        textBuilder.append("🏅 *Lencana Keberhasilan:* \n");
        textBuilder.append(mTotalCreated >= 1 ? "✅ [First Step] - Membuat 1 Target\n" : "🔒 [First Step] - Belum Terbuka\n");
        textBuilder.append(mTotalCreated >= 10 ? "✅ [Planner] - Membuat 10 Target\n" : "🔒 [Planner] - Belum Terbuka\n");
        textBuilder.append(mTotalCheckins >= 1 ? "✅ [Starter] - Check-in 1 kali\n" : "🔒 [Starter] - Belum Terbuka\n");
        textBuilder.append(mTotalCheckins >= 50 ? "✅ [Consistent] - Minimal 50 Kali Check-in\n" : "🔒 [Consistent] - Belum Terbuka\n");
        textBuilder.append(mTotalCheckins >= 100 ? "✅ [Master] - Minimal 100 Kali Check-in\n" : "🔒 [Master] - Belum Terbuka\n");
        textBuilder.append(mMaxStreak >= 3 ? "✅ [Resilient] - Minimal Streak 3 Hari\n" : "🔒 [Resilient] - Belum Terbuka\n\n");

        if (mTopHabits != null && !mTopHabits.isEmpty()) {
            textBuilder.append("🔥 *Habit Unggulan Teratas:* \n");
            for (int i = 0; i < mTopHabits.size(); i++) {
                Habit h = mTopHabits.get(i);
                String medal = (i == 0) ? "🥇 " : (i == 1) ? "🥈 " : "🥉 ";
                textBuilder.append(medal).append(h.getTitle())
                           .append(" (").append(h.getStreakCount()).append(" Sesi Check-in)\n");
            }
            textBuilder.append("\n");
        }

        textBuilder.append("Yuk, mari mulai merancang masa depan dan melatih konsistensimu hari demi hari! 💪✨");

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Pencapaian Disiplin Habit Tracker");
        shareIntent.putExtra(Intent.EXTRA_TEXT, textBuilder.toString());
        startActivity(Intent.createChooser(shareIntent, "Bagikan Pencapaian Melalui"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
