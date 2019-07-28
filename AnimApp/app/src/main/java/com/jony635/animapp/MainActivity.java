package com.jony635.animapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.javiersantos.appupdater.AppUpdater;
import com.github.javiersantos.appupdater.enums.UpdateFrom;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class MainActivity extends AppCompatActivity {

    public static final String ANIME_NAME = "com.jony635.animapp_ANIME_NAME";

    public class AnimeAdapter extends RecyclerView.Adapter<AnimeAdapter.AnimeViewHolder>
    {
        @NonNull
        @Override
        public AnimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.anime_layout, parent, false);
            AnimeViewHolder vh = new AnimeViewHolder(view);

            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull AnimeViewHolder holder, int position)
        {
            holder.textView.setText(animeList.get(position).name);
        }

        @Override
        public int getItemCount()
        {
            return animeList.size();
        }

        public class AnimeViewHolder extends RecyclerView.ViewHolder
        {
            TextView textView;
            CheckBox checkBox;
            Button downloadButton;

            public AnimeViewHolder(@NonNull View itemView)
            {
                super(itemView);

                textView = itemView.findViewById(R.id.animeTV);

                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        int position = getLayoutPosition();
                        OnAnimeClicked(animeList.get(position));
                    }
                });

                checkBox = itemView.findViewById(R.id.seenCheckBox);
                checkBox.setVisibility(View.GONE);

                downloadButton = itemView.findViewById(R.id.downloadButton);
                downloadButton.setVisibility(View.GONE);
            }
        }
    }


    private RecyclerView animesRV;
    private AnimeAdapter animesAdapter;
    private RecyclerView.LayoutManager animesLayoutManager;

    private List<Anime> animeList;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppUpdater appUpdater = new AppUpdater(this).setUpdateFrom(UpdateFrom.GITHUB)
                .setGitHubUserAndRepo("Jony635", "AnimApp").setCancelable(false)
                .setButtonDismiss(null).setButtonDoNotShowAgain(null);
        appUpdater.start();


        animeList = new ArrayList<Anime>();

        animesRV = findViewById(R.id.animesRV);

        animesLayoutManager = new LinearLayoutManager(this);
        animesRV.setLayoutManager(animesLayoutManager);

        animesAdapter = new AnimeAdapter();
        animesRV.setAdapter(animesAdapter);

        db = FirebaseFirestore.getInstance();

//        Anime anime = new Anime();
//        anime.name = "Shigatsu wa Kimi no Uso";
//        anime.numEpisodes = 23;
//
//        DocumentReference animeDoc = db.collection("animes").document(anime.name);
//        animeDoc.set(anime);
//
//        for(int i = 1; i <= anime.numEpisodes; ++i)
//        {
//            Episode episode = new Episode();
//            episode.id = i;
//            episode.link = "undefined";
//
//            animeDoc.collection("episodes").document(String.valueOf(i)).set(episode);
//        }

        db.collection("animes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e)
            {
                if(e != null)
                {
                    //Error
                    return;
                }

                animeList.clear();
                for(DocumentSnapshot doc : queryDocumentSnapshots)
                {
                    animeList.add(doc.toObject(Anime.class));
                }

                animesAdapter.notifyDataSetChanged();
            }
        });
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

    private void OnAnimeClicked(Anime anime)
    {
        Intent intent = new Intent(this, AnimeActivity.class);
        intent.putExtra(ANIME_NAME, anime.name);

        startActivity(intent);
    }
}
