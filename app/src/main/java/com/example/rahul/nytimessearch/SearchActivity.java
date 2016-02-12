package com.example.rahul.nytimessearch;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rahul.nytimessearch.DatePickerFragment.DateListener;
import com.example.rahul.nytimessearch.FilterDialog.SaveListener;
import com.example.rahul.nytimessearch.com.example.rahul.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SaveListener, DateListener {

    public static final String NYTIMES_API_KEY = "7fe0055631c2758530900598ef528030:6:62230673";
    public static final String BASE_URL
            = "http://api.nytimes.com/svc/search/v2/articlesearch.json?";
    public static final String FILTER_PARAM = "fq";
    public static final String API_KEY_PARAM = "api-key";
    private static final String FILTER_FRAGMENT_ID = "filter_fragment";
    private static final int PAGE_SIZE = 10;

    private ArrayList<Article> articles;

    @Bind(R.id.rvResults) RecyclerView rvResults;
    private ArticleAdapter articleRecyclerAdapter;
    private String searchQuery;
    private Calendar beginDate;
    private String sort;
    private ArrayList<String> newsDesks;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        searchQuery = "";
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Sorry, looks like internet is not available. Internet is "
                            + "required for this app to function properly.",
                    Toast.LENGTH_SHORT).show();
        }

        articles = new ArrayList<>();
        articleRecyclerAdapter = new ArticleAdapter(articles);
        rvResults.setAdapter(articleRecyclerAdapter);
        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        rvResults.addOnScrollListener(new EndlessRecyclerViewScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                int curSize = articles.size();
//                Toast.makeText(getApplicationContext(), "Searching " + searchQuery, Toast.LENGTH_SHORT)
//                        .show();
                makeRequest(true);
                articleRecyclerAdapter.notifyItemRangeInserted(curSize, articles.size() - 1);
            }
        });
        rvResults.setLayoutManager(gridLayoutManager);
    }

    public class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

        public class ViewHolder extends RecyclerView.ViewHolder {

            public ImageView ivImage;
            public TextView tvTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                ivImage = (ImageView) itemView.findViewById(R.id.ivImage);
                tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            }
        }

        private List<Article> articles;

        @Override
        public ArticleAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            View articleView = inflater.inflate(R.layout.item_article_result, parent, false);
            return new ViewHolder(articleView);
        }

        @Override
        public void onBindViewHolder(ArticleAdapter.ViewHolder holder, int position) {
            final Article article = articles.get(position);
            TextView tvTitle = holder.tvTitle;
            tvTitle.setText(article.getHeadline());
            String thumbnail = article.getThumbnail();
            if (!TextUtils.isEmpty(thumbnail)) {
                Picasso.with(getApplicationContext())
                        .load(thumbnail)
                        .placeholder(R.mipmap.ic_launcher)
                        .into(holder.ivImage);
            } else {
                holder.ivImage.setImageResource(R.mipmap.ic_launcher);
            }
            OnClickListener urlOpen = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                    intent.putExtra("article", article);
                    startActivity(intent);
                }
            };
            holder.ivImage.setOnClickListener(urlOpen);
            holder.tvTitle.setOnClickListener(urlOpen);
        }

        @Override
        public int getItemCount() {
            return articles.size();
        }

        public ArticleAdapter(List<Article> articles) {
            this.articles = articles;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                articles.clear();
                articleRecyclerAdapter.notifyDataSetChanged();
                searchQuery = query;
                performSearch(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    private void performSearch(boolean addToResults) {
        makeRequest(addToResults);
    }

    private RequestParams constructRequestParams(boolean addToResults) {
        if (TextUtils.isEmpty(searchQuery)) {
            return null;
        }
        RequestParams requestParams = new RequestParams();
        requestParams.put(API_KEY_PARAM, NYTIMES_API_KEY);
        if (addToResults) {
            requestParams.put("page", articles.size() / PAGE_SIZE);
        } else {
            requestParams.put("page", 0);
        }

        requestParams.put("q", searchQuery);
        if (beginDate != null) {
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String calendarParam = format.format(beginDate.getTime());
            if (!TextUtils.isEmpty(calendarParam)) {
                requestParams.put("begin_date", calendarParam);
            }
        }
        if (!TextUtils.isEmpty(sort)) {
            requestParams.put("sort", sort);
        }
        if (newsDesks != null) {
            String newsDesksToAdd = "";
            for (String desk : newsDesks) {
                newsDesksToAdd += desk + " ";
            }
            newsDesksToAdd = newsDesksToAdd.trim();
            String newsDeskParam = null;
            if (!TextUtils.isEmpty(newsDesksToAdd)) {
                // should look like: 'news_desk:("Sports" "Foreign")'
                newsDeskParam = String.format("news_desk:(%s)", newsDesksToAdd);
                requestParams.put("fq", newsDeskParam);
            }
        }
        return requestParams;
    }

    private void makeRequest(final boolean addToResults) {
        RequestParams requestParams = constructRequestParams(addToResults);
        if (requestParams == null) {
            return;
        }
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(BASE_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                try {
                    if (response.isNull("response") || response.getJSONObject("response").isNull(
                            "docs")) {
                        return;
                    }
                    JSONArray docsJson = response.getJSONObject("response").getJSONArray("docs");
                    System.out.println("made it! found # docs: " + docsJson.length());
                    ArrayList<Article> requestArticles = Article.fromJsonArray(docsJson);
                    if (addToResults) {
                        int curSize = articles.size();
                        articles.addAll(requestArticles);
                        articleRecyclerAdapter.notifyItemRangeInserted(curSize,
                                articles.size() - 1);
                    } else {
                        articles.clear();
                        articles.addAll(requestArticles);
                        articleRecyclerAdapter.notifyDataSetChanged();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("something has gone very wrong " + e.toString());
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                    JSONObject errorResponse) {
                System.out.println(errorResponse);
                Toast.makeText(getApplicationContext(), "SorryRequest to NYTimes API failed.",
                        Toast.LENGTH_SHORT).show();
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            showFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showFilterDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FilterDialog filterDialog = FilterDialog.newInstance(beginDate, sort, newsDesks);
        filterDialog.show(fragmentManager, FILTER_FRAGMENT_ID);
    }

    @Override
    public void setFilters(Calendar beginDate, String order, ArrayList<String> newsDesks) {
        this.beginDate = beginDate;
        this.sort = order;
        this.newsDesks = newsDesks;
        MenuItem filterMenuItem = menu.findItem(R.id.action_settings);
        Drawable icon = filterMenuItem.getIcon();
        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(Color.argb(255, 255,
                0, 0), Mode.SRC_IN);
        icon.setColorFilter(porterDuffColorFilter);
        performSearch(false);
    }

    @Override
    public void setDateString(Calendar calendarSet) {
        // Update date in filter dialog edit text
        FilterDialog filterDialog = (FilterDialog) getSupportFragmentManager().findFragmentByTag(
                FILTER_FRAGMENT_ID);
        filterDialog.updateDate(calendarSet);
    }
}
