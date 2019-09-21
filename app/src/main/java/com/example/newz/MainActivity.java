package com.example.newz;

import android.app.SearchManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SearchView;
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

        loadNews("");
    }

    public void loadNews(String keyword){

        INewsApi newsApi = ApiClient.getApiClient().create(INewsApi.class);

        String country = Utils.getCountry();

        Call<News> call;

        if(keyword.length() > 0){
            call = newsApi.getNewsSearch(keyword, "en", "publishedAt", Const.API_KEY);
        }else {
            call = newsApi.getNews(country, Const.API_KEY);
        }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search_main).getActionView();
        MenuItem searchMenuItem = menu.findItem(R.id.app_bar_search_main);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint("Search Latest News");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadNews(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadNews(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

}
