package com.example.habittracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.habittracker.HabitSuggestionEngine;
import com.example.habittracker.QuoteResponse;
import com.example.habittracker.QuoteService;
import com.example.habittracker.SocialAdapter;
import com.example.habittracker.SocialDataEngine;
import com.example.habittracker.SocialPost;
import com.example.habittracker.SuggestionAdapter;
import com.example.habittracker.databinding.FragmentExploreBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

public class ExploreFragment extends Fragment {

    private FragmentExploreBinding binding;
    private QuoteService quoteService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentExploreBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setUpRetrofitClient();
        fetchDailyQuote();

        // Setup dynamic smart suggestions RecyclerView
        setUpSmartSuggestions();

        // Setup social feed
        loadSocialFeed();

        // Bind FAB to open AddPostActivity
        binding.fabAddActivity.setOnClickListener(v -> {
            if (getContext() != null) {
                android.content.Intent intent = new android.content.Intent(getContext(), AddPostActivity.class);
                startActivity(intent);
            }
        });

        // Bind Retry ("Coba Lagi") button action in offline warn UI
        binding.btnRetryConnection.setOnClickListener(v -> fetchDailyQuote());
    }

    public static int newlyCreatedPostId = -1;

    @Override
    public void onResume() {
        super.onResume();
        loadSocialFeed();
    }

    private void loadSocialFeed() {
        if (getContext() == null || binding == null) return;
        
        DatabaseHelper db = new DatabaseHelper(getContext());
        java.util.List<SocialPost> userPosts = db.getUserPosts();
        java.util.List<SocialPost> randomPosts = SocialDataEngine.getRandomPosts(10);
        
        java.util.List<SocialPost> allPosts = new java.util.ArrayList<>();
        
        // Pin newly created or edited post to the very top on first load
        if (newlyCreatedPostId != -1) {
            SocialPost pinnedPost = null;
            for (SocialPost post : userPosts) {
                if (post.getId() == newlyCreatedPostId) {
                    pinnedPost = post;
                    break;
                }
            }
            if (pinnedPost != null) {
                allPosts.add(pinnedPost);
                userPosts.remove(pinnedPost);
            }
            newlyCreatedPostId = -1; // Reset pinning after first load
        }
        
        allPosts.addAll(userPosts);
        allPosts.addAll(randomPosts);
        
        SocialAdapter adapter = new SocialAdapter(getContext(), allPosts);
        binding.recyclerSocial.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.recyclerSocial.setAdapter(adapter);
    }

    private void setUpSmartSuggestions() {
        if (getContext() == null) return;

        // Fetch 10 random suggestions from HabitSuggestionEngine
        java.util.List<HabitSuggestionEngine.Suggestion> list = HabitSuggestionEngine.getRandomSuggestions(10);

        SuggestionAdapter adapter = new SuggestionAdapter(list, suggestion -> {
            openAddHabitWithPrefill(suggestion.getTitle());
        });

        binding.recyclerSuggestions.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(getContext()));
        binding.recyclerSuggestions.setAdapter(adapter);
    }

    private void openAddHabitWithPrefill(String title) {
        if (getContext() == null) return;
        android.content.Intent intent = new android.content.Intent(getContext(), AddHabitActivity.class);
        intent.putExtra("prefill_title", title);
        intent.putExtra("EXTRA_SUGGESTED_TITLE", title);
        startActivity(intent);
    }

    private void setUpRetrofitClient() {
        Retrofit quoteRetrofit = new Retrofit.Builder()
                .baseUrl("https://dummyjson.com/")
                .addConverterFactory(MoshiConverterFactory.create())
                .build();

        quoteService = quoteRetrofit.create(QuoteService.class);
    }

    private void fetchDailyQuote() {
        // Guard checking device physical connectivity first to provide immediate response
        if (!isDeviceOnline()) {
            displayOfflineState();
            return;
        }

        // Apply loading states
        binding.progressLoader.setVisibility(View.VISIBLE);
        binding.scrollExploreContent.setVisibility(View.VISIBLE);
        binding.layoutOfflineWarning.setVisibility(View.GONE);

        // Execute asynchronous Retrofit networking API call for Daily Quote
        Call<QuoteResponse> call = quoteService.getRandomQuote();
        call.enqueue(new Callback<QuoteResponse>() {
            @Override
            public void onResponse(@NonNull Call<QuoteResponse> call, @NonNull Response<QuoteResponse> response) {
                if (getActivity() == null || binding == null) return;

                binding.progressLoader.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    QuoteResponse qr = response.body();
                    
                    // Render fetched motivational text
                    binding.textQuote.setText("\"" + qr.getQuote() + "\"");
                    binding.textQuoteAuthor.setText("— " + qr.getAuthor());
                } else {
                    displayOfflineState();
                }
            }

            @Override
            public void onFailure(@NonNull Call<QuoteResponse> call, @NonNull Throwable t) {
                if (getActivity() == null || binding == null) return;
                displayOfflineState();
            }
        });
    }

    private void displayOfflineState() {
        if (binding == null) return;
        binding.progressLoader.setVisibility(View.GONE);
        binding.scrollExploreContent.setVisibility(View.VISIBLE);
        binding.layoutOfflineWarning.setVisibility(View.GONE);
        
        // Populating offline local fallback quote
        binding.textQuote.setText("\"Konsistensi adalah jembatan antara impian besar dan pencapaian nyata harian Anda.\"");
        binding.textQuoteAuthor.setText("— Standby Motivator");
    }

    private boolean isDeviceOnline() {
        if (getContext() == null) return false;
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo info = cm.getActiveNetworkInfo();
        return info != null && info.isConnectedOrConnecting();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
