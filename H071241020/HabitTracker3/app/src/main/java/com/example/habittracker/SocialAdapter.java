package com.example.habittracker;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.habittracker.SocialPost;
import com.example.habittracker.databinding.ItemSocialPostBinding;
import java.util.List;

public class SocialAdapter extends RecyclerView.Adapter<SocialAdapter.ViewHolder> {

    private final List<SocialPost> posts;
    private final Context context;

    public SocialAdapter(Context context, List<SocialPost> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSocialPostBinding binding = ItemSocialPostBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SocialPost post = posts.get(position);
        holder.bind(post, position);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ItemSocialPostBinding binding;
        private boolean isLiked = false;
        private int likesCount;

        public ViewHolder(ItemSocialPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final SocialPost post, int position) {
            binding.textSocialUsername.setText(post.getUsername());
            binding.textSocialTime.setText(post.getTime());
            
            // Assign custom tags based on title contents deterministically
            String tag = post.getTag();
            if (tag == null || tag.isEmpty()) {
                tag = "PRODUKTIVITAS";
                String lowerTitle = post.getTitle().toLowerCase();
                if (lowerTitle.contains("lari") || lowerTitle.contains("olahraga") || lowerTitle.contains("fit") || lowerTitle.contains("air")) {
                    tag = "KESEHATAN";
                } else if (lowerTitle.contains("buku") || lowerTitle.contains("baca") || lowerTitle.contains("belajar") || lowerTitle.contains("pembelajar")) {
                    tag = "BELAJAR";
                } else if (lowerTitle.contains("meditasi") || lowerTitle.contains("mental") || lowerTitle.contains("napas") || lowerTitle.contains("syukur") || lowerTitle.contains("stoik")) {
                    tag = "MENTAL";
                } else if (lowerTitle.contains("keuangan") || lowerTitle.contains("catat") || lowerTitle.contains("investasi")) {
                    tag = "KEUANGAN";
                } else if (lowerTitle.contains("kerja") || lowerTitle.contains("deep-work") || lowerTitle.contains("pomodoro") || lowerTitle.contains("inbox")) {
                    tag = "FOKUS";
                }
            }
            binding.textSocialTag.setText(tag);

            binding.textSocialTitle.setText(post.getTitle());
            binding.textSocialDescription.setText(post.getBody());

            // 5. Load attractive main post photo: check for custom local file path first, otherwise use Lorem Picsum via seed using Glide
            if (post.getPhotoPath() != null && !post.getPhotoPath().isEmpty()) {
                java.io.File file = new java.io.File(post.getPhotoPath());
                if (file.exists()) {
                    Glide.with(context)
                            .load(file)
                            .placeholder(android.R.drawable.progress_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .centerCrop()
                            .into(binding.imgSocialPost);
                } else {
                    // Fallback to picsum if file was declared but doesn't exist
                    String seedPost = "post_seed_" + post.getId() + "_" + position;
                    String imageUrl = "https://picsum.photos/seed/" + seedPost + "/400/200";
                    Glide.with(context)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.progress_horizontal)
                            .error(android.R.drawable.stat_notify_error)
                            .centerCrop()
                            .into(binding.imgSocialPost);
                }
            } else {
                String seedPost = "post_seed_" + post.getId() + "_" + position;
                String imageUrl = "https://picsum.photos/seed/" + seedPost + "/400/200";
                
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(android.R.drawable.progress_horizontal)
                        .error(android.R.drawable.stat_notify_error)
                        .centerCrop()
                        .into(binding.imgSocialPost);
            }

            // Load unique circular avatar using username as seed
            // Strip '@' to make a valid seed name
            String seedAvatar = post.getUsername().replace("@", "");
            String avatarUrl = "https://picsum.photos/seed/" + seedAvatar + "/100/100";
            
            // Determine if this is the logged-in user's own post
            android.content.SharedPreferences prefs = context.getSharedPreferences("app_profile_prefs", Context.MODE_PRIVATE);
            String savedName = prefs.getString("pref_user_name", "Pejuang Disiplin");
            String myUsername = "@" + savedName.toLowerCase().replace(" ", "_");
            
            boolean isCurrentUserPost = post.getUsername() != null && post.getUsername().equalsIgnoreCase(myUsername);

            if (isCurrentUserPost) {
                // Show Edit/Delete triggers
                binding.layoutSocialEdit.setVisibility(android.view.View.VISIBLE);
                binding.layoutSocialDelete.setVisibility(android.view.View.VISIBLE);

                // Edit Button click handler
                binding.layoutSocialEdit.setOnClickListener(v -> {
                    Intent intent = new Intent(context, AddPostActivity.class);
                    intent.putExtra("post_id", post.getId());
                    context.startActivity(intent);
                });

                // Delete Button click handler with secure confirmation dialog
                binding.layoutSocialDelete.setOnClickListener(v -> {
                    new androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Hapus Aktivitas")
                            .setMessage("Apakah Anda yakin ingin menghapus postingan ini dari Komunitas?")
                            .setPositiveButton("Hapus", (dialog, which) -> {
                                DatabaseHelper db = new DatabaseHelper(context);
                                boolean deleted = db.deleteUserPost(post.getId());
                                if (deleted) {
                                    Toast.makeText(context, "Postingan berhasil dihapus!", Toast.LENGTH_SHORT).show();
                                    try {
                                        posts.remove(post);
                                        notifyDataSetChanged();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(context, "Gagal menghapus postingan", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Batal", null)
                            .show();
                });

                // Profile Avatar: load actual customized setup
                String savedPhotoPath = prefs.getString("pref_user_avatar_path", "");

                boolean hasCustomPhoto = false;
                if (savedPhotoPath != null && !savedPhotoPath.isEmpty()) {
                    java.io.File file = new java.io.File(savedPhotoPath);
                    if (file.exists()) {
                        hasCustomPhoto = true;
                        binding.cardSocialAvatarPhoto.setVisibility(android.view.View.VISIBLE);
                        binding.textSocialAvatarEmoji.setVisibility(android.view.View.GONE);
                        Glide.with(context)
                                .load(file)
                                .placeholder(android.R.drawable.sym_def_app_icon)
                                .error(android.R.drawable.sym_def_app_icon)
                                .circleCrop()
                                .into(binding.imgSocialAvatar);
                    }
                }

                if (!hasCustomPhoto) {
                    // Fallback to the beautiful circular Picsum avatar (like dummy users)
                    binding.cardSocialAvatarPhoto.setVisibility(android.view.View.VISIBLE);
                    binding.textSocialAvatarEmoji.setVisibility(android.view.View.GONE);
                    Glide.with(context)
                            .load(avatarUrl)
                            .placeholder(android.R.drawable.sym_def_app_icon)
                            .error(android.R.drawable.sym_def_app_icon)
                            .circleCrop()
                            .into(binding.imgSocialAvatar);
                }
            } else {
                // Hide Edit/Delete triggers for other posts
                binding.layoutSocialEdit.setVisibility(android.view.View.GONE);
                binding.layoutSocialDelete.setVisibility(android.view.View.GONE);

                // Show default dummy user photo (seed picsum)
                binding.cardSocialAvatarPhoto.setVisibility(android.view.View.VISIBLE);
                binding.textSocialAvatarEmoji.setVisibility(android.view.View.GONE);

                Glide.with(context)
                        .load(avatarUrl)
                        .placeholder(android.R.drawable.sym_def_app_icon)
                        .error(android.R.drawable.sym_def_app_icon)
                        .circleCrop()
                        .into(binding.imgSocialAvatar);
            }

            // Interactive Liked state
            likesCount = (post.getId() * 3) % 45 + 15;
            binding.textSocialLikesCount.setText(String.valueOf(likesCount));
            
            isLiked = false;
            binding.imgSocialLikeIcon.setImageResource(android.R.drawable.btn_star_big_off);

            binding.layoutSocialLike.setOnClickListener(v -> {
                isLiked = !isLiked;
                if (isLiked) {
                    likesCount++;
                    binding.imgSocialLikeIcon.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(context, "Menyukai kiriman " + post.getUsername(), Toast.LENGTH_SHORT).show();
                } else {
                    likesCount--;
                    binding.imgSocialLikeIcon.setImageResource(android.R.drawable.btn_star_big_off);
                }
                binding.textSocialLikesCount.setText(String.valueOf(likesCount));
            });

            // Interactive Share Action (System Share Intent)
            binding.layoutSocialShare.setOnClickListener(v -> {
                try {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareText = post.getUsername() + " di Komunitas Tracker: \"" + post.getTitle() + "\"\n\n" + post.getBody() + "\n\nYuk jalani gaya hidup produktif bersama!";
                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, post.getTitle());
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                    
                    Intent chooser = Intent.createChooser(shareIntent, "Bagikan ke teman");
                    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(chooser);
                } catch (Exception e) {
                    Toast.makeText(context, "Gagal membagikan postingan", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
