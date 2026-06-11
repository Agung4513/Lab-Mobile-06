package com.example.habittracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.habittracker.databinding.FragmentStatsBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsFragment extends Fragment {

    private FragmentStatsBinding binding;
    private DatabaseHelper dbHelper;
    private final ExecutorService diskExecutor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dbHelper = new DatabaseHelper(requireContext());

        loadStatisticsDashboard();
    }

    private void loadStatisticsDashboard() {
        if (getActivity() == null) return;

        // Fetch Indonesian Today Day Name
        String englishDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(new Date());
        String indonesianDay = getIndonesianDay(englishDay);
        String todayString = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // Perform statistical queries on background executor thread
        diskExecutor.execute(() -> {
            final int activeCount = dbHelper.getActiveHabitsCount();
            final int maxStreak = dbHelper.getMaxStreak();
            final int totalSessions = dbHelper.getTotalSessions();
            final double rawCompletionRate = dbHelper.getCompletionRate(indonesianDay, todayString);
            final int completionRate = (int) Math.round(rawCompletionRate);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Update UI views directly with results
                    binding.textStreakValue.setText(maxStreak + " Hari");
                    binding.textCompletionRate.setText(completionRate + "%");
                    binding.textTotalHabitsCount.setText(String.valueOf(activeCount));
                    binding.textTotalHabitsDesc.setText("Kamu mengaktifkan " + activeCount + " target disiplin");
                    binding.textTotalSessionsCount.setText(String.valueOf(totalSessions));

                    // Dynamic tips customized directly based on user's completion rate
                    String feedbackText;
                    if (activeCount == 0) {
                        feedbackText = "Belum ada target disiplin yang aktif! Tambahkan habit di menu utama untuk mulai mengukur pencatatan dan perkembangan dirimu.";
                    } else if (completionRate == 100) {
                        feedbackText = "Luar biasa! Semua target habit-mu hari ini tuntas 100%. Kamu menunjukkan disiplin tingkat tinggi. Pertahankan konsistensi emas ini!";
                    } else if (completionRate >= 75) {
                        feedbackText = "Sangat bagus! Hari ini tingkat penyelesaian habit-mu mencapai " + completionRate + "%. Sedikit lagi menuju kesempurnaan. Pertahankan winstreak aktif agar kebiasaan kecil menetap di kehidupanmu.";
                    } else if (completionRate >= 40) {
                        feedbackText = "Progres yang lumayan! Kamu berhasil menyelesaikan sebagian target (" + completionRate + "%). Luangkan sedikit waktu lagi malam ini untuk menuntaskan sisanya agar streak harian tetap berjalan.";
                    } else if (completionRate > 0) {
                        feedbackText = "Kamu baru memulai (" + completionRate + "%). Jangan menyerah! Mengaktifkan kembali habit-mu hari ini akan mencegah streak terputus. Satu langkah kecil lebih baik daripada tidak sama sekali.";
                    } else {
                        feedbackText = "Kamu belum menyelesaikan satu pun target habit-mu hari ini (0%). Ingat tujuan awal dirimu membuat habit ini. Yuk, centang dan tuntas satu habit sekarang juga!";
                    }
                    binding.textTipsBody.setText(feedbackText);
                });
            }
        });
    }

    private String getIndonesianDay(String englishDay) {
        switch (englishDay) {
            case "Monday": return "Senin";
            case "Tuesday": return "Selasa";
            case "Wednesday": return "Rabu";
            case "Thursday": return "Kamis";
            case "Friday": return "Jumat";
            case "Saturday": return "Sabtu";
            case "Sunday": return "Minggu";
            default: return "Senin";
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
