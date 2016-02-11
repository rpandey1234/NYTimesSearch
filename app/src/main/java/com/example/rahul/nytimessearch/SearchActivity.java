package com.example.rahul.nytimessearch;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import com.example.rahul.nytimessearch.DatePickerFragment.DateListener;
import com.example.rahul.nytimessearch.FilterDialog.SaveListener;
import com.example.rahul.nytimessearch.com.example.rahul.nytimessearch.models.Article;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class SearchActivity extends AppCompatActivity implements SaveListener, DateListener {

    public static final String NYTIMES_API_KEY = "7fe0055631c2758530900598ef528030:6:62230673";
    public static final String BASE_URL
            = "http://api.nytimes.com/svc/search/v2/articlesearch.json?";
    public static final String FILTER_PARAM = "fq";
    public static final String API_KEY_PARAM = "api-key";
    private static final String FILTER_FRAGMENT_ID = "filter_fragment";

    private ArrayList<Article> articles;
    private ArrayAdapter<Article> articleAdapter;

    @Bind(R.id.btnSearch) Button btnSearch;
    @Bind(R.id.etQuery) EditText etQuery;
    @Bind(R.id.gvResults) GridView gvResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // TODO(rahul): test this
        if (!isNetworkAvailable()) {
            Toast.makeText(this, "Sorry, looks like internet is not available. Internet is "
                            + "required for this app to function properly.",
                    Toast.LENGTH_SHORT).show();
        }

        articles = new ArrayList<>();
        articleAdapter = new ArticleArrayAdapter(this, articles);
        gvResults.setAdapter(articleAdapter);
        gvResults.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ArticleActivity.class);
                Article article = articles.get(position);
                intent.putExtra("article", article);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    @OnClick(R.id.btnSearch)
    public void onSearchClicked(View view) {
        String query = etQuery.getText().toString();
        Toast.makeText(this, "Searching " + query, Toast.LENGTH_SHORT)
                .show();
        RequestParams requestParams = new RequestParams();
        requestParams.put(API_KEY_PARAM, NYTIMES_API_KEY);
        requestParams.put("page", 0);
        requestParams.put("q=", query);
        makeRequest(requestParams);
    }

    private void makeRequest(RequestParams requestParams) {
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(BASE_URL, requestParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                articleAdapter.clear();
                try {
                    JSONArray docsJson = response.getJSONObject("response").getJSONArray("docs");
                    System.out.println("made it! found # docs: " + docsJson.length());
                    articleAdapter.addAll(Article.fromJsonArray(docsJson));
                    System.out.println(articles);
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.println("something has gone very wrong " + e.toString());
                }
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
        FilterDialog filterDialog = FilterDialog.newInstance();
        filterDialog.show(fragmentManager, FILTER_FRAGMENT_ID);
    }

    @Override
    public void setOrder(String order) {
        RequestParams requestParams = new RequestParams();
        requestParams.put(API_KEY_PARAM, NYTIMES_API_KEY);
        requestParams.put("page", 0);
        requestParams.put("q=", etQuery.getText().toString());
        requestParams.put("sort", order);
        makeRequest(requestParams);
    }

    @Override
    public void setDateString(String date) {
        // Update date in filter dialog edit text
        FilterDialog filterDialog = (FilterDialog) getSupportFragmentManager().findFragmentByTag(
                FILTER_FRAGMENT_ID);
        filterDialog.updateDate(date);
    }
}
