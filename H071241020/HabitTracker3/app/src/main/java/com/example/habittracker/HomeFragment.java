package com.example.habittracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.habittracker.databinding.FragmentHomeBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeFragment extends Fragment implements HabitAdapter.HabitInteractionListener {

    private FragmentHomeBinding binding;
    private DatabaseHelper dbHelper;
    private HabitAdapter adapter;
    private final List<Habit> habitList = new ArrayList<>();
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    private static final int REQUEST_CODE_ADD = 1001;
    private static final int REQUEST_CODE_EDIT = 1002;
    private String selectedDayName = "Senin";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());
        setUpRecyclerView();
        setUpDayFilterChips();
        setUpFabAction();
        detectAndSelectTodayChip();

        binding.accentCircle.setOnClickListener(v -> {
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView navView = 
                        getActivity().findViewById(R.id.nav_view);
                if (navView != null) navView.setSelectedItemId(R.id.navigation_profile);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHomeProfileHeader();
    }

    private void loadHomeProfileHeader() {
        if (getContext() == null || binding == null) return;
        SharedPreferences prefs = getContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
        String emoji = prefs.getString("pref_user_avatar_emoji", "👤");
        String path = prefs.getString("pref_user_avatar_path", "");

        if (!path.isEmpty()) {
            java.io.File file = new java.io.File(path);
            if (file.exists()) {
                binding.cardHomeAvatarPhoto.setVisibility(View.VISIBLE);
                binding.imageHomeAvatar.setImageURI(android.net.Uri.fromFile(file));
                binding.textHomeAvatarEmoji.setVisibility(View.GONE);
                return;
            }
        }
        binding.cardHomeAvatarPhoto.setVisibility(View.GONE);
        binding.textHomeAvatarEmoji.setVisibility(View.VISIBLE);
        binding.textHomeAvatarEmoji.setText(emoji);
    }

    private void setUpRecyclerView() {
        binding.recyclerViewHabits.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new HabitAdapter(habitList, this);
        binding.recyclerViewHabits.setAdapter(adapter);
    }

    private void setUpDayFilterChips() {
        binding.chipGroupDaysFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_senin) selectedDayName = "Senin";
            else if (checkedId == R.id.chip_selasa) selectedDayName = "Selasa";
            else if (checkedId == R.id.chip_rabu) selectedDayName = "Rabu";
            else if (checkedId == R.id.chip_kamis) selectedDayName = "Kamis";
            else if (checkedId == R.id.chip_jumat) selectedDayName = "Jumat";
            else if (checkedId == R.id.chip_sabtu) selectedDayName = "Sabtu";
            else if (checkedId == R.id.chip_minggu) selectedDayName = "Minggu";
            loadHabitDataBySelectedDay();
        });
    }

    private void detectAndSelectTodayChip() {
        selectedDayName = DateUtils.getIndonesianDayName();
        switch (selectedDayName) {
            case "Senin": binding.chipSenin.setChecked(true); break;
            case "Selasa": binding.chipSelasa.setChecked(true); break;
            case "Rabu": binding.chipRabu.setChecked(true); break;
            case "Kamis": binding.chipKamis.setChecked(true); break;
            case "Jumat": binding.chipJumat.setChecked(true); break;
            case "Sabtu": binding.chipSabtu.setChecked(true); break;
            case "Minggu": binding.chipMinggu.setChecked(true); break;
        }
        binding.textTodayDate.setText("Hari ini · " + DateUtils.getFormattedFullDate());
        loadHabitDataBySelectedDay();
    }

    private void loadHabitDataBySelectedDay() {
        diskExecutor.execute(() -> {
            final List<Habit> updatedList = dbHelper.getHabitsForDay(selectedDayName, DateUtils.getTodayDateString());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    habitList.clear();
                    habitList.addAll(updatedList);
                    adapter.notifyDataSetChanged();
                    binding.layoutEmptyState.emptyStateRoot.setVisibility(updatedList.isEmpty() ? View.VISIBLE : View.GONE);
                    binding.recyclerViewHabits.setVisibility(updatedList.isEmpty() ? View.GONE : View.VISIBLE);
                });
            }
        });
    }

    private void setUpFabAction() {
        binding.fabAddHabit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddHabitActivity.class);
            startActivityForResult(intent, REQUEST_CODE_ADD);
        });
    }

    @Override
    public void onCheckIn(Habit habit, int position) {
        diskExecutor.execute(() -> {
            if (dbHelper.checkInHabit(habit.getId(), DateUtils.getTodayDateString())) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Berhasil check-in: " + habit.getTitle(), Toast.LENGTH_SHORT).show();
                        checkNewBadges();
                        loadHabitDataBySelectedDay();
                    });
                }
            }
        });
    }

    @Override
    public void onLongPress(Habit habit, int position) {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(habit.getTitle())
                .setItems(new CharSequence[]{"Edit Habit", "Hapus Habit"}, (dialog, which) -> {
                    if (which == 0) {
                        Intent intent = new Intent(getActivity(), AddHabitActivity.class);
                        intent.putExtra("habit_id", habit.getId());
                        intent.putExtra("title", habit.getTitle());
                        intent.putExtra("days", habit.getDaysOfWeek());
                        intent.putExtra("time", habit.getTime());
                        intent.putExtra("priority", habit.getPriority());
                        intent.putExtra("difficulty", habit.getDifficulty());
                        startActivityForResult(intent, REQUEST_CODE_EDIT);
                    } else {
                        showDeletionConfirmDialog(habit);
                    }
                }).show();
    }

    private void showDeletionConfirmDialog(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Habit")
                .setMessage("Apakah Anda yakin ingin menghapus habit '" + habit.getTitle() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> diskExecutor.execute(() -> {
                    dbHelper.softDeleteHabit(habit.getId());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), getString(R.string.habit_deleted), Toast.LENGTH_SHORT).show();
                            loadHabitDataBySelectedDay();
                        });
                    }
                }))
                .setNegativeButton("Batal", null).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            loadHabitDataBySelectedDay();
            checkNewBadges();
        }
    }

    private void checkNewBadges() {
        diskExecutor.execute(() -> {
            if (getContext() == null || dbHelper == null) return;
            final int totalCreated = dbHelper.getTotalHabitsEverCreated();
            final int totalCheckins = dbHelper.getTotalSessions();
            final int maxStreak = dbHelper.getMaxStreak();

            final List<BadgeData> newUnlocks = new ArrayList<>();
            SharedPreferences prefs = requireContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);

            checkAndAddBadge(newUnlocks, prefs, totalCreated >= 1, "step", "First Step", "Bikin 1 task", R.drawable.ic_badge_step);
            checkAndAddBadge(newUnlocks, prefs, totalCreated >= 10, "planner", "Planner", "Bikin 10 task", R.drawable.ic_badge_planner);
            checkAndAddBadge(newUnlocks, prefs, totalCheckins >= 1, "starter", "Starter", "Check-in 1 kali", R.drawable.ic_badge_starter);
            checkAndAddBadge(newUnlocks, prefs, totalCheckins >= 50, "consistent", "Consistent", "Check-in 50 kali", R.drawable.ic_badge_consistent);
            checkAndAddBadge(newUnlocks, prefs, totalCheckins >= 100, "master", "Master", "Check-in 100 kali", R.drawable.ic_badge_master);
            checkAndAddBadge(newUnlocks, prefs, maxStreak >= 3, "resilient", "Resilient", "Streak 3 hari", R.drawable.ic_badge_resilient);

            if (!newUnlocks.isEmpty() && getActivity() != null) {
                getActivity().runOnUiThread(() -> showCelebrateDialogChain(newUnlocks, 0, prefs));
            }
        });
    }

    private void checkAndAddBadge(List<BadgeData> list, SharedPreferences prefs, boolean eligible, String id, String title, String desc, int icon) {
        if (eligible && !prefs.getBoolean("pref_badge_unlocked_" + id, false)) {
            list.add(new BadgeData(id, title, desc, icon));
        }
    }

    private void showCelebrateDialogChain(List<BadgeData> list, int index, SharedPreferences prefs) {
        if (getContext() == null || index >= list.size()) return;
        BadgeData badge = list.get(index);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_badge_unlocked, null);
        ((ImageView) dialogView.findViewById(R.id.dialog_badge_icon)).setImageResource(badge.iconResId);
        ((TextView) dialogView.findViewById(R.id.dialog_badge_title)).setText(badge.title);
        ((TextView) dialogView.findViewById(R.id.dialog_badge_description)).setText("Misi Tercapai: " + badge.description);

        AlertDialog dialog = new AlertDialog.Builder(requireContext()).setView(dialogView).setCancelable(false).create();
        if (dialog.getWindow() != null) dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));

        dialogView.findViewById(R.id.dialog_btn_claim).setOnClickListener(v -> {
            prefs.edit().putBoolean("pref_badge_unlocked_" + badge.id, true).apply();
            dialog.dismiss();
            showCelebrateDialogChain(list, index + 1, prefs);
        });
        dialog.show();
    }

    private static class BadgeData {
        String id, title, description; int iconResId;
        BadgeData(String id, String title, String description, int iconResId) {
            this.id = id; this.title = title; this.description = description; this.iconResId = iconResId;
        }
    }

    @Override
    public void onDestroyView() { super.onDestroyView(); binding = null; }

    @Override
    public void onDestroy() { super.onDestroy(); diskExecutor.shutdown(); }
}
