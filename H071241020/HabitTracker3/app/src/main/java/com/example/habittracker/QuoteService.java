package com.example.habittracker;

import retrofit2.Call;
import retrofit2.http.GET;

public interface QuoteService {
    @GET("quotes/random")
    Call<QuoteResponse> getRandomQuote();
}
