package com.example.rahul.nytimessearch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.rahul.nytimessearch.com.example.rahul.nytimessearch.models.Article;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ArticleActivity extends AppCompatActivity {


    @Bind(R.id.wvArticle) WebView webView;
    private MenuItem menuItem;
    private Intent shareIntent;
    private ShareActionProvider shareAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Article article = (Article) getIntent().getSerializableExtra("article");

        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, article.getHeadline());
        shareIntent.putExtra(Intent.EXTRA_TEXT, article.getWebUrl());
        shareIntent.setType("text/plain");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        webView.loadUrl(article.getWebUrl());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_article_detail, menu);
        menuItem = menu.findItem(R.id.menu_item_share);
        shareAction = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        shareAction.setShareIntent(shareIntent);
        menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                System.out.println("clicked on share");
                return false;
            }
        });
        return true;
    }
}
