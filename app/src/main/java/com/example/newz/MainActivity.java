package com.example.newz;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.example.newz.API.ApiClient;
import com.example.newz.API.INewsApi;
import com.example.newz.Model.Article;
import com.example.newz.Model.News;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private NewsAdapter newsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.main_recycler);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        loadNews();
    }

    public void loadNews(){

        INewsApi newsApi = ApiClient.getApiClient().create(INewsApi.class);

        String country = Utils.getCountry();

        Call<News> call;
        call = newsApi.getNews(country, Const.API_KEY);
        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(Call<News> call, Response<News> response) {
                if(response.isSuccessful() && response.body().getArticles() != null){
                    if (!articles.isEmpty()){
                        articles.clear();
                    }
                    articles = response.body().getArticles();
                    newsAdapter = new NewsAdapter(articles, MainActivity.this);
                    recyclerView.setAdapter(newsAdapter);
                    newsAdapter.notifyDataSetChanged();

                }else {
                    Toast.makeText(MainActivity.this,"no news", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {

            }
        });
    }
}
