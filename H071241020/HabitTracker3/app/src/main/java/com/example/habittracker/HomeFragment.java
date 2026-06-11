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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        // Autodetect calendar today to highlight matching day tab
        detectAndSelectTodayChip();

        // Navigate to Profile when clicking the header profile avatar icon
        binding.accentCircle.setOnClickListener(v -> {
            try {
                if (getActivity() != null) {
                    com.google.android.material.bottomnavigation.BottomNavigationView navView = 
                            getActivity().findViewById(R.id.nav_view);
                    if (navView != null) {
                        navView.setSelectedItemId(R.id.navigation_profile);
                    } else {
                        androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_profile);
                    }
                } else {
                    androidx.navigation.Navigation.findNavController(v).navigate(R.id.navigation_profile);
                }
            } catch (Exception e) {
                e.printStackTrace();
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
            } else {
                binding.cardHomeAvatarPhoto.setVisibility(View.GONE);
                binding.textHomeAvatarEmoji.setVisibility(View.VISIBLE);
                binding.textHomeAvatarEmoji.setText(emoji);
            }
        } else {
            binding.cardHomeAvatarPhoto.setVisibility(View.GONE);
            binding.textHomeAvatarEmoji.setVisibility(View.VISIBLE);
            binding.textHomeAvatarEmoji.setText(emoji);
        }
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
        String englishDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date());
        switch (englishDay) {
            case "Monday":
                binding.chipSenin.setChecked(true);
                selectedDayName = "Senin";
                break;
            case "Tuesday":
                binding.chipSelasa.setChecked(true);
                selectedDayName = "Selasa";
                break;
            case "Wednesday":
                binding.chipRabu.setChecked(true);
                selectedDayName = "Rabu";
                break;
            case "Thursday":
                binding.chipKamis.setChecked(true);
                selectedDayName = "Kamis";
                break;
            case "Friday":
                binding.chipJumat.setChecked(true);
                selectedDayName = "Jumat";
                break;
            case "Saturday":
                binding.chipSabtu.setChecked(true);
                selectedDayName = "Sabtu";
                break;
            case "Sunday":
                binding.chipMinggu.setChecked(true);
                selectedDayName = "Minggu";
                break;
            default:
                binding.chipSenin.setChecked(true);
                selectedDayName = "Senin";
                break;
        }

        // Setup formatted Indonesian Today Label inside Bento Banner Card
        String formattedDate = new SimpleDateFormat("EEEE, d MMMM yyyy", new Locale("id", "ID")).format(new Date());
        binding.textTodayDate.setText("Hari ini · " + formattedDate);

        loadHabitDataBySelectedDay();
    }

    private void loadHabitDataBySelectedDay() {
        if (getActivity() == null) return;
        
        String todayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        // SQLite query is evaluated in raw diskExecutor background loops
        diskExecutor.execute(() -> {
            final List<Habit> updatedList = dbHelper.getHabitsForDay(selectedDayName, todayString);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    habitList.clear();
                    habitList.addAll(updatedList);
                    adapter.notifyDataSetChanged();

                    // Empty layout checker
                    if (updatedList.isEmpty()) {
                        binding.layoutEmptyState.emptyStateRoot.setVisibility(View.VISIBLE);
                        binding.recyclerViewHabits.setVisibility(View.GONE);
                    } else {
                        binding.layoutEmptyState.emptyStateRoot.setVisibility(View.GONE);
                        binding.recyclerViewHabits.setVisibility(View.VISIBLE);
                    }
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

    // RecyclerView interaction: Check-in complete
    @Override
    public void onCheckIn(Habit habit, int position) {
        String todayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        
        diskExecutor.execute(() -> {
            boolean success = dbHelper.checkInHabit(habit.getId(), todayString);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        Toast.makeText(getContext(), "Berhasil check-in: " + habit.getTitle(), Toast.LENGTH_SHORT).show();
                        checkNewBadges(); // Trigger immediate evaluation of unlocked achievements
                    }
                    // Reload list on separate thread to realize Smart Sorting reorganizations
                    loadHabitDataBySelectedDay();
                });
            }
        });
    }

    // RecyclerView interaction: Long press to edit or delete
    @Override
    public void onLongPress(Habit habit, int position) {
        if (getContext() == null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(habit.getTitle())
                .setItems(new CharSequence[]{"Edit Habit", "Hapus Habit"}, (dialog, which) -> {
                    if (which == 0) {
                        // RECYCLE AddHabitActivity via Intent passing extras
                        Intent intent = new Intent(getActivity(), AddHabitActivity.class);
                        intent.putExtra("habit_id", habit.getId());
                        intent.putExtra("title", habit.getTitle());
                        intent.putExtra("days", habit.getDaysOfWeek());
                        intent.putExtra("time", habit.getTime());
                        intent.putExtra("priority", habit.getPriority());
                        intent.putExtra("difficulty", habit.getDifficulty());
                        startActivityForResult(intent, REQUEST_CODE_EDIT);
                    } else {
                        // Confirm deletion dialog
                        showDeletionConfirmDialog(habit);
                    }
                })
                .create()
                .show();
    }

    private void showDeletionConfirmDialog(Habit habit) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hapus Habit")
                .setMessage("Apakah Anda yakin ingin menghapus habit '" + habit.getTitle() + "'?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    diskExecutor.execute(() -> {
                        // Soft delete execution
                        dbHelper.softDeleteHabit(habit.getId());
                        
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), getString(R.string.habit_deleted), Toast.LENGTH_SHORT).show();
                                loadHabitDataBySelectedDay();
                            });
                        }
                    });
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            loadHabitDataBySelectedDay();
            checkNewBadges(); // Evaluate newly unlocked badges when habits are created/updated
        }
    }

    private void checkNewBadges() {
        diskExecutor.execute(() -> {
            if (getContext() == null || dbHelper == null) return;

            final int totalCreated = dbHelper.getTotalHabitsEverCreated();
            final int totalCheckins = dbHelper.getTotalSessions();
            final int maxStreak = dbHelper.getMaxStreak();

            // Evaluate eligibility for each defined badge
            boolean stepEligible = totalCreated >= 1;
            boolean plannerEligible = totalCreated >= 10;
            boolean starterEligible = totalCheckins >= 1;
            boolean consistentEligible = totalCheckins >= 50;
            boolean masterEligible = totalCheckins >= 100;
            boolean resilientEligible = maxStreak >= 3;

            final List<BadgeData> newUnlocks = new ArrayList<>();
            SharedPreferences prefs = requireContext().getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);

            if (stepEligible && !prefs.getBoolean("pref_badge_unlocked_step", false)) {
                newUnlocks.add(new BadgeData("step", "First Step", "Bikin 1 task", R.drawable.ic_badge_step));
            }
            if (plannerEligible && !prefs.getBoolean("pref_badge_unlocked_planner", false)) {
                newUnlocks.add(new BadgeData("planner", "Planner", "Bikin 10 task", R.drawable.ic_badge_planner));
            }
            if (starterEligible && !prefs.getBoolean("pref_badge_unlocked_starter", false)) {
                newUnlocks.add(new BadgeData("starter", "Starter", "Check-in 1 kali", R.drawable.ic_badge_starter));
            }
            if (consistentEligible && !prefs.getBoolean("pref_badge_unlocked_consistent", false)) {
                newUnlocks.add(new BadgeData("consistent", "Consistent", "Check-in 50 kali", R.drawable.ic_badge_consistent));
            }
            if (masterEligible && !prefs.getBoolean("pref_badge_unlocked_master", false)) {
                newUnlocks.add(new BadgeData("master", "Master", "Check-in 100 kali", R.drawable.ic_badge_master));
            }
            if (resilientEligible && !prefs.getBoolean("pref_badge_unlocked_resilient", false)) {
                newUnlocks.add(new BadgeData("resilient", "Resilient", "Streak 3 hari", R.drawable.ic_badge_resilient));
            }

            if (!newUnlocks.isEmpty()) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // Show modal dialog chain for newly unlocked badges sequentially
                        showCelebrateDialogChain(newUnlocks, 0, prefs);
                    });
                }
            }
        });
    }

    private void showCelebrateDialogChain(List<BadgeData> list, int index, SharedPreferences prefs) {
        if (getContext() == null || index >= list.size()) return;
        BadgeData badge = list.get(index);

        // Inflate custom congrats XML dialog layout
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_badge_unlocked, null);
        ImageView iconView = dialogView.findViewById(R.id.dialog_badge_icon);
        TextView titleView = dialogView.findViewById(R.id.dialog_badge_title);
        TextView descView = dialogView.findViewById(R.id.dialog_badge_description);
        View btnClaim = dialogView.findViewById(R.id.dialog_btn_claim);

        iconView.setImageResource(badge.iconResId);
        titleView.setText(badge.title);
        descView.setText("Misi Tercapai: " + badge.description);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(false)
                .create();

        // Elegant translucent dialog window background for custom corner radius card overlay
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnClaim.setOnClickListener(v -> {
            // Persist the notified/unlocked state so it never repeats the popup
            prefs.edit().putBoolean("pref_badge_unlocked_" + badge.id, true).apply();
            dialog.dismiss();
            
            // Recurse to show next congratulations award card sequentially
            showCelebrateDialogChain(list, index + 1, prefs);
        });

        // Trigger gorgeous popping action
        dialog.show();
    }

    private static class BadgeData {
        String id;
        String title;
        String description;
        int iconResId;

        BadgeData(String id, String title, String description, int iconResId) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.iconResId = iconResId;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        diskExecutor.shutdown();
    }
}
