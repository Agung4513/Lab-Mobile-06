package com.example.habittracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import com.example.habittracker.databinding.ActivityEditProfileBinding;
import com.bumptech.glide.Glide;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private SharedPreferences prefs;
    private String selectedEmoji = "👤";
    private String customPhotoPath = "";
    private final List<TextView> emojiTextViews = new ArrayList<>();

    // Launcher for photo picking from Gallery
    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    String localPath = copyUriToInternalStorage(uri);
                    if (localPath != null) {
                        customPhotoPath = localPath;
                        updateAvatarViews();
                    } else {
                        Toast.makeText(this, "Gagal memproses gambar.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setTheme(R.style.Theme_HabitTracker);
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);

        // Load existing settings
        String currentName = prefs.getString("pref_user_name", "Pejuang Disiplin");
        String currentBio = prefs.getString("pref_user_bio", "\"Konsistensi mengalahkan bakat!\"");
        selectedEmoji = prefs.getString("pref_user_avatar_emoji", "👤");
        customPhotoPath = prefs.getString("pref_user_avatar_path", "");

        // Set name and bio values inside modern edit texts
        binding.editProfileName.setText(currentName);
        binding.editProfileBio.setText(currentBio);

        // Core visual update
        updateAvatarViews();
        buildPresetEmojisList();

        // Register action listeners
        binding.btnBack.setOnClickListener(v -> finish());
        
        binding.btnPickerGallery.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        binding.btnRemovePicture.setOnClickListener(v -> {
            customPhotoPath = "";
            updateAvatarViews();
            Toast.makeText(this, "Menggunakan ikon preset emoji saat ini", Toast.LENGTH_SHORT).show();
        });

        binding.btnSaveProfile.setOnClickListener(v -> saveProfileChanges());
    }

    private void updateAvatarViews() {
        if (customPhotoPath != null && !customPhotoPath.isEmpty()) {
            File file = new File(customPhotoPath);
            if (file.exists()) {
                binding.cardEditAvatarPhoto.setVisibility(View.VISIBLE);
                binding.textEditAvatarEmoji.setVisibility(View.GONE);
                Glide.with(this)
                        .load(file)
                        .circleCrop()
                        .into(binding.imgEditAvatar);
                return;
            }
        }
        // Fallback to emoji
        binding.cardEditAvatarPhoto.setVisibility(View.GONE);
        binding.textEditAvatarEmoji.setVisibility(View.VISIBLE);
        binding.textEditAvatarEmoji.setText(selectedEmoji);
    }

    private void buildPresetEmojisList() {
        binding.layoutEmojisHost.removeAllViews();
        emojiTextViews.clear();
        
        String[] preSetEmojis = {"👤", "🦁", "🐼", "🦊", "🚀", "🧠", "⚡", "🌟", "🏆", "🧘", "👟", "🏋️", "🎯", "🥑", "🏕️", "🔥"};
        
        int itemPaddingStartEnd = 16;
        int itemPaddingTopBottom = 8;
        float density = getResources().getDisplayMetrics().density;
        
        for (String emojiChar : preSetEmojis) {
            TextView emView = new TextView(this);
            emView.setText(emojiChar);
            emView.setTextSize(26);
            emView.setPadding(
                    (int) (itemPaddingStartEnd * density), 
                    (int) (itemPaddingTopBottom * density), 
                    (int) (itemPaddingStartEnd * density), 
                    (int) (itemPaddingTopBottom * density)
            );
            emView.setGravity(Gravity.CENTER);
            emView.setClickable(true);
            emView.setFocusable(true);
            emView.setBackgroundResource(R.drawable.bg_badge_rounded);

            // Highlight selected item background
            if (emojiChar.equals(selectedEmoji)) {
                emView.setBackgroundColor(getResources().getColor(R.color.color_container));
            } else {
                emView.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            }

            emView.setOnClickListener(v -> {
                selectedEmoji = emojiChar;
                // Clear photo path if they choose to use an emoji
                customPhotoPath = ""; 
                updateAvatarViews();
                
                // Refresh list highlight
                for (TextView otherItem : emojiTextViews) {
                    if (otherItem.getText().toString().equals(emojiChar)) {
                        otherItem.setBackgroundColor(getResources().getColor(R.color.color_container));
                    } else {
                        otherItem.setBackgroundColor(android.graphics.Color.TRANSPARENT);
                    }
                }
            });

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins((int) (6 * density), 0, (int) (6 * density), 0);
            emView.setLayoutParams(lp);

            binding.layoutEmojisHost.addView(emView);
            emojiTextViews.add(emView);
        }
    }

    private String copyUriToInternalStorage(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;
            
            File outputDir = getFilesDir();
            File destFile = new File(outputDir, "profile_avatar.jpg");
            
            OutputStream outputStream = new FileOutputStream(destFile);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
            
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void saveProfileChanges() {
        String finalName = binding.editProfileName.getText().toString().trim();
        String finalBio = binding.editProfileBio.getText().toString().trim();

        if (finalName.isEmpty()) {
            Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show();
            return;
        }

        prefs.edit()
                .putString("pref_user_name", finalName)
                .putString("pref_user_bio", finalBio)
                .putString("pref_user_avatar_emoji", selectedEmoji)
                .putString("pref_user_avatar_path", customPhotoPath)
                .apply();

        Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show();
        finish();
    }
}
