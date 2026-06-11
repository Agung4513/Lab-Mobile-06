package com.example.habittracker;

import com.squareup.moshi.Json;

public class QuoteResponse {
    @Json(name = "id")
    private int id;

    @Json(name = "quote")
    private String quote;

    @Json(name = "author")
    private String author;

    public QuoteResponse() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
