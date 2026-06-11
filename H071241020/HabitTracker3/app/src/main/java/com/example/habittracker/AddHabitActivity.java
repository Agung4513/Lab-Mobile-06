package com.example.habittracker;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.DatabaseHelper;
import com.example.habittracker.Habit;
import com.example.habittracker.databinding.ActivityAddHabitBinding;
import com.google.android.material.chip.Chip;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddHabitActivity extends AppCompatActivity {

    private ActivityAddHabitBinding binding;
    private DatabaseHelper dbHelper;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    private boolean isAdvancedExpanded = true;
    private boolean isEditMode = false;
    private int habitIdToEdit = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddHabitBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dbHelper = new DatabaseHelper(this);

        setUpFrequencyCalculator();
        setUpAccordionToggle();
        setUpTimeSpinners();

        // Save & Cancel actions
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveHabitForm());

        // Check for edit recycling mode
        checkIntentForEditMode();
    }

    private void setUpFrequencyCalculator() {
        // Dynamic handler when chips are clicked to satisfy "Auto-Calculate Frequency"
        CompoundButton.OnCheckedChangeListener frequencyListener = (buttonView, isChecked) -> {
            updateAutomaticFrequency();
        };

        binding.formChipSenin.setOnCheckedChangeListener(frequencyListener);
        binding.formChipSelasa.setOnCheckedChangeListener(frequencyListener);
        binding.formChipRabu.setOnCheckedChangeListener(frequencyListener);
        binding.formChipKamis.setOnCheckedChangeListener(frequencyListener);
        binding.formChipJumat.setOnCheckedChangeListener(frequencyListener);
        binding.formChipSabtu.setOnCheckedChangeListener(frequencyListener);
        binding.formChipMinggu.setOnCheckedChangeListener(frequencyListener);
    }

    private void updateAutomaticFrequency() {
        int count = 0;
        if (binding.formChipSenin.isChecked()) count++;
        if (binding.formChipSelasa.isChecked()) count++;
        if (binding.formChipRabu.isChecked()) count++;
        if (binding.formChipKamis.isChecked()) count++;
        if (binding.formChipJumat.isChecked()) count++;
        if (binding.formChipSabtu.isChecked()) count++;
        if (binding.formChipMinggu.isChecked()) count++;

        String frequencyLabel = getString(R.string.freq_prefix) + count + " hari/minggu";
        binding.textFrequencyIndicator.setText(frequencyLabel);
    }

    private void setUpAccordionToggle() {
        // Hide by default setup or state management
        binding.btnAdvancedAccordionHeader.setOnClickListener(v -> {
            isAdvancedExpanded = !isAdvancedExpanded;
            if (isAdvancedExpanded) {
                binding.layoutAdvancedRoot.setVisibility(View.VISIBLE);
                binding.btnAdvancedAccordionHeader.setText("Pengaturan Lanjutan (Sembunyikan) ▾");
                
                // Realize soft smoothScrollTo when advanced section opens up
                binding.scrollViewForm.post(() -> {
                    binding.scrollViewForm.smoothScrollTo(0, binding.btnAdvancedAccordionHeader.getTop());
                });
            } else {
                binding.layoutAdvancedRoot.setVisibility(View.GONE);
                binding.btnAdvancedAccordionHeader.setText("Pengaturan Lanjutan (Tampilkan) ▸");
            }
        });
    }

    private void setUpTimeSpinners() {
        // Populate Hour Spinner (00 - 23)
        List<String> hoursList = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            hoursList.add(String.format(java.util.Locale.US, "%02d", i));
        }
        android.widget.ArrayAdapter<String> hourAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, hoursList
        );
        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerHour.setAdapter(hourAdapter);

        // Populate Minute Spinner (00 - 59)
        List<String> minutesList = new ArrayList<>();
        for (int i = 0; i < 60; i++) {
            minutesList.add(String.format(java.util.Locale.US, "%02d", i));
        }
        android.widget.ArrayAdapter<String> minuteAdapter = new android.widget.ArrayAdapter<>(
            this, android.R.layout.simple_spinner_item, minutesList
        );
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerMinute.setAdapter(minuteAdapter);

        // Radio Group change listener to show/hide specific pickers
        binding.radioGroupTimeType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_time_specific) {
                binding.layoutSpecificTimePickers.setVisibility(View.VISIBLE);
            } else {
                binding.layoutSpecificTimePickers.setVisibility(View.GONE);
            }
        });
    }

    private void checkIntentForEditMode() {
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("habit_id")) {
            isEditMode = true;
            habitIdToEdit = intent.getIntExtra("habit_id", -1);
            binding.textFormHeading.setText(getString(R.string.edit_habit_title));

            binding.editTextHabitTitle.setText(intent.getStringExtra("title"));
            String time = intent.getStringExtra("time");
            if (time == null || time.equalsIgnoreCase("Bebas") || !time.contains(":")) {
                binding.radioTimeFree.setChecked(true);
                binding.layoutSpecificTimePickers.setVisibility(View.GONE);
            } else {
                binding.radioTimeSpecific.setChecked(true);
                binding.layoutSpecificTimePickers.setVisibility(View.VISIBLE);
                String[] parts = time.split(":");
                if (parts.length == 2) {
                    try {
                        int h = Integer.parseInt(parts[0].trim());
                        int m = Integer.parseInt(parts[1].trim());
                        if (h >= 0 && h < 24) {
                            binding.spinnerHour.setSelection(h);
                        }
                        if (m >= 0 && m < 60) {
                            binding.spinnerMinute.setSelection(m);
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Pre-tick days
            String days = intent.getStringExtra("days");
            if (days != null) {
                if (days.contains("Senin")) binding.formChipSenin.setChecked(true);
                if (days.contains("Selasa")) binding.formChipSelasa.setChecked(true);
                if (days.contains("Rabu")) binding.formChipRabu.setChecked(true);
                if (days.contains("Kamis")) binding.formChipKamis.setChecked(true);
                if (days.contains("Jumat")) binding.formChipJumat.setChecked(true);
                if (days.contains("Sabtu")) binding.formChipSabtu.setChecked(true);
                if (days.contains("Minggu")) binding.formChipMinggu.setChecked(true);
            }
            updateAutomaticFrequency();

            // Pre-tick priority
            String priority = intent.getStringExtra("priority");
            if (priority != null) {
                if (priority.equals(getString(R.string.priority_urgent))) binding.chipPrioUrgent.setChecked(true);
                else if (priority.equals(getString(R.string.priority_high))) binding.chipPrioHigh.setChecked(true);
                else if (priority.equals(getString(R.string.priority_medium))) binding.chipPrioMedium.setChecked(true);
                else if (priority.equals(getString(R.string.priority_low))) binding.chipPrioLow.setChecked(true);
            }

            // Pre-tick difficulty
            String difficulty = intent.getStringExtra("difficulty");
            if (difficulty != null) {
                if (difficulty.equals(getString(R.string.difficulty_hard))) binding.chipDiffHard.setChecked(true);
                else if (difficulty.equals(getString(R.string.difficulty_medium))) binding.chipDiffMedium.setChecked(true);
                else if (difficulty.equals(getString(R.string.difficulty_easy))) binding.chipDiffEasy.setChecked(true);
            }
        } else if (intent != null && (intent.hasExtra("prefill_title") || intent.hasExtra("EXTRA_SUGGESTED_TITLE"))) {
            String titleVal = intent.hasExtra("prefill_title") ? 
                    intent.getStringExtra("prefill_title") : 
                    intent.getStringExtra("EXTRA_SUGGESTED_TITLE");
            binding.editTextHabitTitle.setText(titleVal);
            // Pre-tick Monday to Friday to make direct saving frictionless for recommendations
            binding.formChipSenin.setChecked(true);
            binding.formChipSelasa.setChecked(true);
            binding.formChipRabu.setChecked(true);
            binding.formChipKamis.setChecked(true);
            binding.formChipJumat.setChecked(true);
            updateAutomaticFrequency();
        }
    }

    private void saveHabitForm() {
        String title = binding.editTextHabitTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Nama habit tidak boleh kosong", Toast.LENGTH_SHORT).show();
            return;
        }

        // Days calculation
        List<String> selectedDays = new ArrayList<>();
        if (binding.formChipSenin.isChecked()) selectedDays.add("Senin");
        if (binding.formChipSelasa.isChecked()) selectedDays.add("Selasa");
        if (binding.formChipRabu.isChecked()) selectedDays.add("Rabu");
        if (binding.formChipKamis.isChecked()) selectedDays.add("Kamis");
        if (binding.formChipJumat.isChecked()) selectedDays.add("Jumat");
        if (binding.formChipSabtu.isChecked()) selectedDays.add("Sabtu");
        if (binding.formChipMinggu.isChecked()) selectedDays.add("Minggu");

        if (selectedDays.isEmpty()) {
            Toast.makeText(this, "Pilih minimal satu hari pelaksanaan", Toast.LENGTH_SHORT).show();
            return;
        }

        String daysOfWeekString = TextUtils.join(", ", selectedDays);

        // Fetch remaining options based on dropdown design
        String time;
        if (binding.radioTimeFree.isChecked()) {
            time = "Bebas";
        } else {
            String hour = binding.spinnerHour.getSelectedItem().toString();
            String minute = binding.spinnerMinute.getSelectedItem().toString();
            time = hour + ":" + minute;
        }

        // Priority calculation (Low / Paling Rendah is the new default to satisfy user intent)
        String priority = getString(R.string.priority_low); // Default
        if (binding.chipPrioUrgent.isChecked()) priority = getString(R.string.priority_urgent);
        else if (binding.chipPrioHigh.isChecked()) priority = getString(R.string.priority_high);
        else if (binding.chipPrioMedium.isChecked()) priority = getString(R.string.priority_medium);

        // Difficulty calculation (Easy / Paling Rendah is the new default to satisfy user intent)
        String difficulty = getString(R.string.difficulty_easy); // Default
        if (binding.chipDiffHard.isChecked()) difficulty = getString(R.string.difficulty_hard);
        else if (binding.chipDiffMedium.isChecked()) difficulty = getString(R.string.difficulty_medium);

        final Habit habit = new Habit();
        habit.setTitle(title);
        habit.setDaysOfWeek(daysOfWeekString);
        habit.setTime(time);
        habit.setPriority(priority);
        habit.setDifficulty(difficulty);

        if (isEditMode) {
            habit.setId(habitIdToEdit);
        }

        // ALL DATABASE CRUD OPERATIONS MUST BE WRAPPED IN BACKGROUND THREADS
        diskExecutor.execute(() -> {
            // DUPLICATE CHECK
            if (dbHelper.isHabitTitleExists(habit.getTitle(), isEditMode ? habit.getId() : -1)) {
                runOnUiThread(() -> {
                    Toast.makeText(AddHabitActivity.this, "Habit dengan nama tersebut sudah ada!", Toast.LENGTH_LONG).show();
                });
                return;
            }

            if (isEditMode) {
                dbHelper.updateHabit(habit);
            } else {
                dbHelper.insertHabit(habit);
            }

            // Return success callback block to main UI thread
            runOnUiThread(() -> {
                Toast.makeText(AddHabitActivity.this, getString(R.string.habit_saved), Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            });
        });
    }

    // Helper getter to keep access clean
    private android.widget.EditText editTextTitleHintFix() {
        return binding.editTextHabitTitle;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        diskExecutor.shutdown();
    }
}
