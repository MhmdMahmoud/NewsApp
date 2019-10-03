package com.example.newz;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    Context context = this;

    private SwipeRefreshLayout refreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private List<Article> articles = new ArrayList<>();
    private NewsAdapter newsAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refreshLayout = findViewById(R.id.swipe_refreash_main);
        refreshLayout.setOnRefreshListener(this);
        refreshLayout.setColorSchemeResources(R.color.colorAccent);

        recyclerView = findViewById(R.id.main_recycler);
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        if (Utils.isNetworkConnected(context)){
            onLoading("");
        }
        else {
            Toast.makeText(context, "No Internet...", Toast.LENGTH_LONG).show();
        }
    }

    public void loadNews(String keyword){

        refreshLayout.setRefreshing(true);

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

                    initListener();

                    refreshLayout.setRefreshing(false);

                }else {

                    refreshLayout.setRefreshing(false);
                    Toast.makeText(MainActivity.this,"no news", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<News> call, Throwable t) {
                refreshLayout.setRefreshing(false);
            }
        });
    }

    private void initListener(){
        newsAdapter.setOnItemClickListener(new NewsAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int Position) {
                Intent intent = new Intent(MainActivity.this, NewsDetailsActivity.class);

                Article article = articles.get(Position);
                intent.putExtra("url", article.getUrl());
                intent.putExtra("title", article.getTitle());
                intent.putExtra("img", article.getUrlToImage());
                intent.putExtra("date", article.getPublishedAt());
                intent.putExtra("source", article.getSource().getName());
                intent.putExtra("author", article.getAuthor());

                startActivity(intent);
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
                onLoading(query);
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

    @Override
    public void onRefresh() {
        if (Utils.isNetworkConnected(context)){
            onLoading("");
        }
        else {
            Toast.makeText(context, "No Internet...", Toast.LENGTH_LONG).show();
            refreshLayout.setRefreshing(false);
        }
    }

    public void onLoading(final String ketword){
        refreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadNews(ketword);
            }
        });
    }
}
