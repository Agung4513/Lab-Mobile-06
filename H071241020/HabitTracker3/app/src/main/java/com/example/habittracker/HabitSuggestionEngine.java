package com.example.habittracker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

public class HabitSuggestionEngine {

    public static class Suggestion {
        private final String title;
        private final String category;
        private final String description;

        public Suggestion(String title, String category, String description) {
            this.title = title;
            this.category = category;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public String getCategory() {
            return category;
        }

        public String getDescription() {
            return description;
        }
    }

    private static final String RAW_JSON = "[" +
        "{\"title\": \"Minum Air Putih 2L Sehari\", \"category\": \"Kesehatan\", \"description\": \"Menghidrasi tubuh, memelihara fokus, dan menyegarkan organ dalam harian.\"}," +
        "{\"title\": \"Peregangan Tubuh 10 Menit\", \"category\": \"Kesehatan\", \"description\": \"Peregangan sendi-sendi kaku setelah duduk lama demi postur ideal.\"}," +
        "{\"title\": \"Berjalan Kaki 10.000 Langkah\", \"category\": \"Kesehatan\", \"description\": \"Membakar energi berlebih, merawat fungsi jantung, dan melatih kaki.\"}," +
        "{\"title\": \"Tidur Sebelum Jam 22:00\", \"category\": \"Kesehatan\", \"description\": \"Mengoptimalkan sekresi hormon pertumbuhan dan detoksifikasi alami tubuh.\"}," +
        "{\"title\": \"Olahraga Calisthenics Ringan\", \"category\": \"Kesehatan\", \"description\": \"Push-up, squat, dan plank selama 15 menit tanpa alat untuk kekuatan otot.\"}," +
        "{\"title\": \"Kurangi Kafein Setelah Siang\", \"category\": \"Kesehatan\", \"description\": \"Menyetop konsumsi kopi setelah jam 14.00 demi kualitas tidur nyenyak.\"}," +
        "{\"title\": \"Makan Sayur Tiap Makan Siang\", \"category\": \"Kesehatan\", \"description\": \"Memasukkan porsi serat hijau segar dalam menu wajib makan siangmu.\"}," +
        "{\"title\": \"Puasa Layar Gadget Sebelum Tidur\", \"category\": \"Kesehatan\", \"description\": \"Mematikan HP atau tablet 30 menit sebelum tidur untuk mengurangi radiasi blue light.\"}," +
        "{\"title\": \"Lari Pagi 20 Menit\", \"category\": \"Kesehatan\", \"description\": \"Menghirup udara pagi segar dan memicu endorfin pembakar semangat harian.\"}," +
        "{\"title\": \"Membersihkan Ranjang Pagi Hari\", \"category\": \"Kesehatan\", \"description\": \"Meraih kemenangan kecil pertama di pagi hari dengan merapikan bantal guling.\"}," +
        "{\"title\": \"Merawat Tanaman Hias\", \"category\": \"Kesehatan\", \"description\": \"Menyiram tanaman kesayangan di pagi hari untuk terapi ketenangan alami.\"}," +
        "{\"title\": \"Membaca Jurnal / Artikel Karir\", \"category\": \"Karir\", \"description\": \"Menyerap 1 artikel industri atau tulisan terkait profesi untuk wawasan global.\"}," +
        "{\"title\": \"Merapikan Ruang Kerja Harian\", \"category\": \"Karir\", \"description\": \"Menyeka meja, menata dokumen selama 5 menit guna mengondisikan konsentrasi.\"}," +
        "{\"title\": \"Sesi Inbox Zero di Sore Hari\", \"category\": \"Karir\", \"description\": \"Memproses, membalas, dan mengarsipkan semua email kerjaan agar tidak menumpuk berdampak.\"}," +
        "{\"title\": \"Belajar Skills Baru 20 Menit\", \"category\": \"Karir\", \"description\": \"Mempelajari coding, desain, atau bahasa asing secara bertahap setiap hari.\"}," +
        "{\"title\": \"Networking Santai di LinkedIn\", \"category\": \"Karir\", \"description\": \"Menyapa kolega profesional baru atau meninggalkan komentar cerdas di feed berkualitas.\"}," +
        "{\"title\": \"Review Progress Mingguan\", \"category\": \"Karir\", \"description\": \"Menilai pencapaian minggu ini dan merancang to-do-list penting esok hari.\"}," +
        "{\"title\": \"Latihan Deep Work 45 Menit\", \"category\": \"Karir\", \"description\": \"Bekerja tanpa distraksi ponsel, chat, ataupun tab browser yang tidak relevan.\"}," +
        "{\"title\": \"Mencatat Pengeluaran Harian\", \"category\": \"Karir\", \"description\": \"Merekam setiap rupiah yang keluar demi perencanaan finansial masa depan cerah.\"}," +
        "{\"title\": \"Latihan Public Speaking\", \"category\": \"Karir\", \"description\": \"Berlatih teknik presentasi atau intonasi bicara singkat di depan cermin 5 menit.\"}," +
        "{\"title\": \"Membuat Rencana Keuangan Mikro\", \"category\": \"Karir\", \"description\": \"Mengevaluasi anggaran bulanan secara berkala agar tidak overspending.\"}," +
        "{\"title\": \"Membaca 1 Bab Buku Bisnis\", \"category\": \"Karir\", \"description\": \"Menambah referensi bisnis praktis untuk merangsang ide wirausaha baru.\"}," +
        "{\"title\": \"Meditasi Tenang 5 Menit\", \"category\": \"Mental\", \"description\": \"Melatih pernapasan penuh, menurunkan kecemasan, dan menenangkan saraf lelah.\"}," +
        "{\"title\": \"Menulis Jurnal Rasa Syukur\", \"category\": \"Mental\", \"description\": \"Mencatat 3 hal bermakna yang disyukuri hari ini agar pikiran selalu positif.\"}," +
        "{\"title\": \"Membaca Buku Non-fiksi 10 Halaman\", \"category\": \"Mental\", \"description\": \"Menenangkan pikiran sekaligus memperkaya paradigma berpikir kritis.\"}," +
        "{\"title\": \"Satu Kebaikan Tanpa Pamrih\", \"category\": \"Mental\", \"description\": \"Melakukan aksi menolong orang lain secara rahasia atau memberi pujian jujur.\"}," +
        "{\"title\": \"Detoks Media Sosial Harian\", \"category\": \"Mental\", \"description\": \"Membatasi akses media sosial hanya 1 jam sehari untuk kesejahteraan jiwa.\"}," +
        "{\"title\": \"Latihan Napas Kotak (Box Breathing)\", \"category\": \"Mental\", \"description\": \"Menarik napas 4 detik, tahan 4 detik, embuskan 4 detik untuk meredakan ketegangan.\"}," +
        "{\"title\": \"Mendengarkan Musik Klasik / Ambient\", \"category\": \"Mental\", \"description\": \"Merelaksasi gelombang otak saat beristirahat atau belajar di sore hari.\"}," +
        "{\"title\": \"Waktu Bebas Gadget 15 Menit\", \"category\": \"Mental\", \"description\": \"Menikmati alam terbuka, tanaman, atau bercengkrama langsung tanpa asisten digital.\"}," +
        "{\"title\": \"Sesi Refleksi Diri Malam Hari\", \"category\": \"Mental\", \"description\": \"Mengevaluasi emosi yang dialami hari ini dan melepas sisa kemarahan terpendam.\"}," +
        "{\"title\": \"Minum Teh Chamomile Hangat\", \"category\": \"Mental\", \"description\": \"Meminum teh herbal bebas kafein sebelum tidur untuk merilekskan pikiran gelisah.\"}," +
        "{\"title\": \"Afirmasi Positif Pagi Hari\", \"category\": \"Mental\", \"description\": \"Mengucapkan kalimat penguat diri di pagi hari demi meningkatkan rasa percaya diri.\"}" +
    "]";

    public static List<Suggestion> getRandomSuggestions(int count) {
        List<Suggestion> allList = new ArrayList<>();
        try {
            JSONArray arr = new JSONArray(RAW_JSON);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                String title = obj.getString("title");
                String category = obj.getString("category");
                String description = obj.getString("description");
                allList.add(new Suggestion(title, category, description));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Shuffle the complete list to randomize
        Collections.shuffle(allList);

        // Sublist up to count
        if (allList.size() <= count) {
            return allList;
        } else {
            return allList.subList(0, count);
        }
    }
}
