package com.example.habittracker;

import com.example.habittracker.SocialPost;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SocialDataEngine {

    public static String getRawJson() {
        return "[]";
    }

    public static List<SocialPost> getRandomPosts(int count) {
        List<SocialPost> posts = new ArrayList<>();
        
        posts.add(new SocialPost(1, "@budi_produktif", "2 jam lalu", "Hari ke-10 Lari Pagi!", "Akhirnya tembus 5km tanpa henti pagi ini! Konsistensi benar-benar kuncinya. Selamat pagi pejuang subuh!"));
        posts.add(new SocialPost(2, "@dewi_mindful", "4 jam lalu", "Sesi Meditasi 15 Menit Efektif", "Meditasi sebelum mulai kerja membantu banget mengontrol kecemasan dan mengasah kejernihan berfikir hari ini."));
        posts.add(new SocialPost(3, "@rudi_focus", "5 jam lalu", "Kemenangan Inbox Zero!", "Merapikan inbox surel kantor di pagi hari emang bikin tenang. Bersiap melakukan deep-work sesi pertama!"));
        posts.add(new SocialPost(4, "@kaizen_expert", "1 hari lalu", "Prinsip 1% Setiap Hari", "Ingat rumus Kaizen: daripada langsung besar tapi gampang bosan, ubah kebiasaan kecil 1% setiap hari secara konsisten!"));
        posts.add(new SocialPost(5, "@lisa_habittracker", "3 jam lalu", "Hari ke-30 Membaca Buku", "Satu bab sebelum tidur rasanya jauh berbeda daripada scrolling medsos tak menentu. Otak lebih rileks dan siap istirahat."));
        posts.add(new SocialPost(6, "@fajar_deepwork", "30 menit lalu", "Teknik Pomodoro Sangat Ampuh", "Selesaikan target menulis 2000 kata hari ini dengan 4 blok Pomodoro tanpa interupsi gadget sama sekali."));
        posts.add(new SocialPost(7, "@clara_zen", "6 jam lalu", "Teh Chamomile Sebelum Tidur", "Rutinitas minum teh hangat herbal sebelum tidur membantu mengurangi insomnia dan membuat tidur jauh lebih nyenyak."));
        posts.add(new SocialPost(8, "@eko_fitness", "8 jam lalu", "Push-Up & Squat Pagi Hari", "Cukup 15 menit olahraga calisthenics ringan di kamar, keringat bercucuran rasa kantuk langsung hilang seketika."));
        posts.add(new SocialPost(9, "@habits_guru", "12 jam lalu", "Pentingnya Merapikan Ranjang", "Kemenangan pertama hari ini ada di bantal dan guling yang rapi. Rasakan aura produktivitas langsung berkobar!"));
        posts.add(new SocialPost(10, "@gita_financial", "2 hari lalu", "Catat Pengeluaran Walau Sedikit", "Habit menulis pengeluaran harian membantu mendeteksi bocor alus finasial. Dompet jadi aman pikiran pun tenang."));
        posts.add(new SocialPost(11, "@agus_pembelajar", "1 jam lalu", "Belajar Duolingo Non-stop!", "Hari ke-50 belajar Bahasa Spanyol di sela-sela waktu makan siang. Konsistensi kecil yang membawa hasil nyata."));
        posts.add(new SocialPost(12, "@sari_greenthumb", "7 jam lalu", "Merawat Tanaman di Pagi Hari", "Menyiram monstera dan aglaonema kesayangan memberi ketenangan tersendiri sebelum sibuk dengan tugas kantor."));
        posts.add(new SocialPost(13, "@dono_coding", "3 jam lalu", "Satu Jam Belajar Algoritma", "Lagi asik menguliti struktur data baru. Tidak perlu instan, yang penting konsisten menyisihkan waktu tiap sore."));
        posts.add(new SocialPost(14, "@ani_journaling", "9 jam lalu", "Mencatatkan Rasa Syukur Hari Ini", "Menulis 3 hal yang saya syukuri hari ini sebelum tidur membuat hati jauh lebih lapang dan bahagia. Yuk mulai bersyukur!"));
        posts.add(new SocialPost(15, "@lukas_speakup", "10 jam lalu", "Latihan Bicara Depan Cermin", "Berlatih teknik intonasi dan eye-contact selama 10 menit untuk persiapan presentasi pitching besok pagi."));
        posts.add(new SocialPost(16, "@mega_nutrition", "11 jam lalu", "Bawa Bekal Sehat Buatan Sendiri", "Habit baru membawa bekal nasi merah dan sayuran segar ke tempat kerja. Lebih hemat, bersih, bergizi, dan higienis!"));
        posts.add(new SocialPost(17, "@yoga_stoic", "15 jam lalu", "Refleksi Stoik Sore Hari", "Fokus pada apa yang bisa kita kontrol, lepaskan apa yang di luar kendali kita. Jiwa jadi lebih damai menghadapi deadline."));
        posts.add(new SocialPost(18, "@tina_creative", "1 hari lalu", "Sketsa Bebas 15 Menit", "Menggambar sketsa acak melatih kreativitas belahan otak kanan. Sangat seru dilakukan sembari rehat santai sore."));
        posts.add(new SocialPost(19, "@yudi_unplugged", "5 jam lalu", "Bebas Gadget Selama 2 Jam", "Mencoba detoks digital di sore hari. Pergi ke taman tanpa bawa HP, rasanya hidup jadi damai dan rileks kali ini."));
        posts.add(new SocialPost(20, "@santi_hydrate", "20 menit lalu", "Air Putih Pengganti Kopi", "Mengurangi asupan kafein berlebihan dan menggantinya dengan segelas air putih hangat setiap pagi. Berasa bugar!"));
        posts.add(new SocialPost(21, "@wawan_writer", "12 jam lalu", "Menulis Jurnal Harian", "Menumpahkan seluruh isi pikiran yang berisik ke dalam kertas. Terapi mental paling murah dan menenangkan."));
        posts.add(new SocialPost(22, "@putri_sehat", "3 jam lalu", "Menyiapkan Sarapan Bergizi", "Roti gandum dengan alpukat tumbuk dan telur rebus. Sarapan padat nutrisi penunjang fokus kerja seharian."));
        posts.add(new SocialPost(23, "@rinto_minimalis", "14 jam lalu", "Decluttering Meja Kerja!", "Membuang tumpukan kertas tak berguna di meja. Ruangan yang bersih sebanding lurus dengan ketenangan jasmani."));
        posts.add(new SocialPost(24, "@andi_no_games", "4 jam lalu", "Kurangi Screen-Time Game", "Beralih dari main game kompetitif ke membaca e-book di HP saat senggang. Produktivitas melonjak drastis!"));
        posts.add(new SocialPost(25, "@nita_breathe", "8 jam lalu", "Latihan Napas Box Breathing", "Saat panik melanda karena bug codingan, lakukan napas 4-4-4-4. Detak jantung kembali melambat, logika jalan lagi."));
        posts.add(new SocialPost(26, "@indra_invest", "2 hari lalu", "Membaca Berita Ekonomi Pagi", "Menempatkan habit membaca prospektus investasi 20 menit sebelum pasar saham buka demi masa depan finansial."));
        posts.add(new SocialPost(27, "@lulu_bilingual", "6 jam lalu", "Mendengarkan Podcast Bahasa Asing", "Mendengarkan materi percakapan bahasa Jepang di transportasi umum menuju tempat kerja. Pemanfaatan waktu yang cerdas!"));
        posts.add(new SocialPost(28, "@adi_morningrun", "45 menit lalu", "Lari Pagi dan Sinar Matahari", "Sinar matahari pagi di kulit merangsang vitamin D alami dan mengondisikan ritme sirkadian tidur makin teratur."));
        posts.add(new SocialPost(29, "@meta_meditation", "13 jam lalu", "Mengirimkan Doa Kebaikan", "Melafalkan metta meditation, mendoakan kebahagiaan untuk semua makhluk hidup. Menghapus kebencian di lubuk hati."));
        posts.add(new SocialPost(30, "@hendra_stoic", "18 jam lalu", "Jurnal Malam Hari Sebelum Tidur", "Menulis evaluasi harian: Apa yang sudah berjalan baik? Apa yang salah? Bagaimana memperbaikinya besok pagi?"));
        posts.add(new SocialPost(31, "@mira_planner", "10 jam lalu", "Merancang To-Do List Esok Hari", "Membuat daftar 3 pekerjaan terpenting untuk besok malam ini agar saat bangun pagi langsung gerak tanpa bingung."));

        // Shuffle the full list
        Collections.shuffle(posts);

        // Slice up to count
        if (posts.size() <= count) {
            return posts;
        } else {
            return new ArrayList<>(posts.subList(0, count));
        }
    }
}
