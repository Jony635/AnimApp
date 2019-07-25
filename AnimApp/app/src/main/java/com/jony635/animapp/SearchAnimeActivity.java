package com.jony635.animapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

public class SearchAnimeActivity extends AppCompatActivity {

    Toolbar searchToolbar;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_anime);

        searchToolbar = findViewById(R.id.searchToolbar);
        setSupportActionBar(searchToolbar);

        actionBar = getSupportActionBar();
        actionBar.setTitle("");
    }


}
