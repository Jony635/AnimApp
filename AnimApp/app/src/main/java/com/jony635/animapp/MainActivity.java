package com.jony635.animapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.SearchButton:
            {
                OnSearchButtonPressed();
                break;
            }

            default:
                return super.onOptionsItemSelected(item);
        }

        return false;
    }

    private void OnSearchButtonPressed()
    {
        //Open another activity, replace the action bar with a custom one, which:
            // - Has to have a "Back" button.
            // - Contains an "Input Text" bar.
            // - Shows below the options matched. //CHOOSE ONE: Button or auto search?

        Intent intent = new Intent(this, SearchAnimeActivity.class);
        startActivity(intent);
    }
}
