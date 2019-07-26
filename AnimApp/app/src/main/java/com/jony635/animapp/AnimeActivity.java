package com.jony635.animapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Document;

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
            holder.episodeTV.setText(String.valueOf(episodeList.get(position).id));
        }

        @Override
        public int getItemCount()
        {
            return episodeList.size();
        }

        class EpisodesVH extends RecyclerView.ViewHolder
        {
            TextView episodeTV;

            public EpisodesVH(@NonNull View itemView)
            {
                super(itemView);

                episodeTV = itemView.findViewById(R.id.animeTV);

                episodeTV.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Toast.makeText(AnimeActivity.this, "Link is " + episodeList.get(getLayoutPosition()).id, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(episodeList.get(getLayoutPosition()).link));
                        intent.setDataAndType(Uri.parse(episodeList.get(getLayoutPosition()).link), "video/mp4");
                        startActivity(intent);
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
    }
}
