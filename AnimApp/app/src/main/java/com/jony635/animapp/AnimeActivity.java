package com.jony635.animapp;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class AnimeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private Anime anime;

    private RecyclerView episodesRV;
    private EpisodesAdapter episodesAdapter;
    private RecyclerView.LayoutManager episodesLayoutManager;

    private List<Episode> episodeList = new ArrayList<>();

    private SharedPreferences sharedPreferences;

    public class EpisodesAdapter extends RecyclerView.Adapter<EpisodesAdapter.EpisodesVH>
    {
        @NonNull
        @Override
        public EpisodesVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
        {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.anime_layout, parent, false);

            return new EpisodesVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull EpisodesVH holder, int position)
        {
            float value = episodeList.get(position).id;

            if(value - (int)value == 0)
                holder.episodeTV.setText(String.format("%.0f", value));
            else
                holder.episodeTV.setText(String.format("%.1f", value));


            boolean seen = sharedPreferences.getBoolean(anime.name + "_" + value, false);
            holder.seenCheckBox.setChecked(seen);
        }

        @Override
        public int getItemCount()
        {
            return episodeList.size();
        }

        class EpisodesVH extends RecyclerView.ViewHolder
        {
            TextView episodeTV;
            CheckBox seenCheckBox;
            Button downloadButton;

            public EpisodesVH(@NonNull final View itemView)
            {
                super(itemView);

                episodeTV = itemView.findViewById(R.id.animeTV);

                episodeTV.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        //Toast.makeText(AnimeActivity.this, "Link is " + episodeList.get(getLayoutPosition()).id, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(episodeList.get(getLayoutPosition()).link));
                        intent.setDataAndType(Uri.parse(episodeList.get(getLayoutPosition()).link), "video/mp4");
                        startActivity(intent);

                        sharedPreferences.edit().putBoolean(anime.name + "_" + episodeList.get(getLayoutPosition()).id, true).apply();
                        seenCheckBox.setChecked(true);
                    }
                });

                seenCheckBox = itemView.findViewById(R.id.seenCheckBox);
                seenCheckBox.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        sharedPreferences.edit().putBoolean(anime.name + "_" + episodeList.get(getLayoutPosition()).id, seenCheckBox.isChecked()).apply();
                    }
                });

                downloadButton = itemView.findViewById(R.id.downloadButton);

                downloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view)
                    {
                        Toast.makeText(AnimeActivity.this, "Downloading episode...", Toast.LENGTH_SHORT).show();
                        Download(anime, episodeList.get(getLayoutPosition()));
                    }
                });
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anime);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        if(intent != null)
        {
            db.collection("animes").
                    document(intent.getStringExtra(MainActivity.ANIME_NAME))
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>()
            {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot)
                {
                    anime = documentSnapshot.toObject(Anime.class);

                    db.collection("animes").document(anime.name)
                            .collection("episodes").orderBy("id")
                            .addSnapshotListener(new EventListener<QuerySnapshot>()
                            {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots,
                                                    @Nullable FirebaseFirestoreException e)
                                {
                                    if(e != null)
                                    {
                                        //Error
                                        return;
                                    }

                                    episodeList.clear();
                                    for(DocumentSnapshot doc : queryDocumentSnapshots)
                                    {
                                        episodeList.add(doc.toObject(Episode.class));
                                    }
                                    episodesAdapter.notifyDataSetChanged();
                                }
                            });
                }
            });
        }

        episodesRV = findViewById(R.id.episodesRV);
        episodesAdapter = new EpisodesAdapter();
        episodesRV.setAdapter(episodesAdapter);

        episodesLayoutManager = new LinearLayoutManager(this);
        episodesRV.setLayoutManager(episodesLayoutManager);

        sharedPreferences = getSharedPreferences("ANIME", MODE_PRIVATE);
    }

    private void Download(Anime anime, Episode episode)
    {
        String fileName = anime.name + "_" + (episode.id - (int) episode.id == 0 ?
                String.format("%.0f", episode.id) : String.format("%.1f", episode.id)) + ".mp4";

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(episode.link))
                .setTitle("Downloading " + fileName)// Title of the Download Notification
                //.setDescription("Downloading")// Description of the Download Notification
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)// Visibility of the download Notification
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)// Set if download is allowed on Mobile network
                .setAllowedOverRoaming(true)// Set if download is allowed on roaming network
                .setMimeType("video/mp4");

        DownloadManager downloadManager= (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                Toast.makeText(AnimeActivity.this, "dude", Toast.LENGTH_SHORT).show();
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
}
