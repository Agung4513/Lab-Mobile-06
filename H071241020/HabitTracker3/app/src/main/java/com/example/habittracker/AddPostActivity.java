package com.example.habittracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.habittracker.DatabaseHelper;
import com.example.habittracker.ExploreFragment;
import com.example.habittracker.MainActivity;
import com.example.habittracker.SocialPost;
import com.example.habittracker.databinding.ActivityAddPostBinding;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddPostActivity extends AppCompatActivity {

    private ActivityAddPostBinding binding;
    private DatabaseHelper dbHelper;
    private String selectedPhotoPath = "";
    private String selectedTag = "PRODUKTIVITAS";
    private int editingPostId = -1;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            copyUriToInternalStorageAsync(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        dbHelper = new DatabaseHelper(this);

        // Bind Category Chip selection changes
        setupCategoryChips();

        // Bind back close button
        binding.btnClosePostForm.setOnClickListener(v -> finish());

        // Bind photo container picker click
        binding.cardSelectPostPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // Setup real-time character constraint counters
        setupCharacterCounters();

        // Check if we are in Edit mode
        editingPostId = getIntent().getIntExtra("post_id", -1);
        if (editingPostId != -1) {
            setupEditMode();
        }

        // Submit Button click
        binding.btnSubmitPost.setOnClickListener(v -> submitPost());
    }

    private void setupEditMode() {
        SocialPost post = dbHelper.getUserPostById(editingPostId);
        if (post != null) {
            binding.textPostFormHeading.setText("Edit Aktivitas");
            binding.btnSubmitPost.setText("Simpan Perubahan");
            binding.editTextPostTitle.setText(post.getTitle());
            binding.editTextPostBody.setText(post.getBody());
            
            String tag = post.getTag();
            if (tag != null) {
                selectedTag = tag;
                int chipId = R.id.chip_pro;
                if (tag.equals("KESEHATAN")) chipId = R.id.chip_health;
                else if (tag.equals("BELAJAR")) chipId = R.id.chip_learning;
                else if (tag.equals("MENTAL")) chipId = R.id.chip_mental;
                else if (tag.equals("KEUANGAN")) chipId = R.id.chip_finance;
                else if (tag.equals("FOKUS")) chipId = R.id.chip_focus;
                binding.chipGroupTagSelect.check(chipId);
            }
            
            String photoPath = post.getPhotoPath();
            if (photoPath != null && !photoPath.isEmpty()) {
                File file = new File(photoPath);
                if (file.exists()) {
                    selectedPhotoPath = photoPath;
                    binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
                    binding.layoutPhotoPreview.setVisibility(View.VISIBLE);
                    binding.imagePostPreview.setImageURI(Uri.fromFile(file));
                }
            }
        }
    }

    private void setupCategoryChips() {
        binding.chipGroupTagSelect.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_pro) {
                selectedTag = "PRODUKTIVITAS";
            } else if (checkedId == R.id.chip_health) {
                selectedTag = "KESEHATAN";
            } else if (checkedId == R.id.chip_learning) {
                selectedTag = "BELAJAR";
            } else if (checkedId == R.id.chip_mental) {
                selectedTag = "MENTAL";
            } else if (checkedId == R.id.chip_finance) {
                selectedTag = "KEUANGAN";
            } else if (checkedId == R.id.chip_focus) {
                selectedTag = "FOKUS";
            }
        });
    }

    private void setupCharacterCounters() {
        // Title Length
        binding.editTextPostTitle.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textTitleCounter.setText(s.length() + "/30");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Body Length
        binding.editTextPostBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.textBodyCounter.setText(s.length() + "/120");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void copyUriToInternalStorageAsync(Uri uri) {
        new Thread(() -> {
            try {
                InputStream inputStream = getContentResolver().openInputStream(uri);
                if (inputStream == null) return;

                File outputDir = getFilesDir();
                String fileName = "post_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(outputDir, fileName);

                OutputStream outputStream = new FileOutputStream(destFile);
                byte[] buf = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bytesRead);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                selectedPhotoPath = destFile.getAbsolutePath();

                runOnUiThread(() -> {
                    binding.layoutPhotoPlaceholder.setVisibility(View.GONE);
                    binding.layoutPhotoPreview.setVisibility(View.VISIBLE);
                    binding.imagePostPreview.setImageURI(Uri.fromFile(destFile));
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(AddPostActivity.this, "Gagal mengunggah foto", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void submitPost() {
        String titleStr = binding.editTextPostTitle.getText().toString().trim();
        String descriptionStr = binding.editTextPostBody.getText().toString().trim();

        if (titleStr.isEmpty()) {
            Toast.makeText(this, "Silakan isi judul aktivitas Anda!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (descriptionStr.isEmpty()) {
            Toast.makeText(this, "Silakan tulis caption / deskripsi aktivitas Anda!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editingPostId != -1) {
            SocialPost post = dbHelper.getUserPostById(editingPostId);
            if (post != null) {
                post.setTitle(titleStr);
                post.setBody(descriptionStr);
                post.setTag(selectedTag);
                post.setPhotoPath(selectedPhotoPath);
                
                boolean updated = dbHelper.updateUserPost(post);
                if (updated) {
                    Toast.makeText(this, "Aktivitas berhasil diperbarui!", Toast.LENGTH_LONG).show();
                    ExploreFragment.newlyCreatedPostId = editingPostId;
                    
                    // Redirect to MainActivity on Explore tab
                    android.content.Intent mainIntent = new android.content.Intent(this, MainActivity.class);
                    mainIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    mainIntent.putExtra("navigate_to_explore", true);
                    startActivity(mainIntent);
                    
                    finish();
                } else {
                    Toast.makeText(this, "Gagal memperbarui aktivitas", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            // Get saved username from SharedPreferences, matching the profile's configuration
            SharedPreferences prefs = getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
            String savedName = prefs.getString("pref_user_name", "Pejuang Disiplin");
            
            // Clean format, e.g., @pejuang_disiplin
            String username = "@" + savedName.toLowerCase().replace(" ", "_");

            // Format current time beautifully
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String curTime = "Hari ini, " + sdf.format(new Date());

            SocialPost newPost = new SocialPost();
            newPost.setUsername(username);
            newPost.setTime(curTime);
            newPost.setTitle(titleStr);
            newPost.setBody(descriptionStr);
            newPost.setTag(selectedTag);
            newPost.setPhotoPath(selectedPhotoPath); // empty if they chose to use random

            // Insert dynamically into our SQLite user_posts Database table
            long insertedId = dbHelper.insertUserPost(newPost);

            if (insertedId != -1) {
                Toast.makeText(this, "Aktivitas berhasil dibagikan di Komunitas!", Toast.LENGTH_LONG).show();
                ExploreFragment.newlyCreatedPostId = (int) insertedId;
                
                // Redirect to MainActivity on Explore tab
                android.content.Intent mainIntent = new android.content.Intent(this, MainActivity.class);
                mainIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mainIntent.putExtra("navigate_to_explore", true);
                startActivity(mainIntent);
                
                finish();
            } else {
                Toast.makeText(this, "Gagal menyimpan aktivitas", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
