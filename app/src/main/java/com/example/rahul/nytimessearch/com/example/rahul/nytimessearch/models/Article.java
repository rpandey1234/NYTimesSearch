package com.example.rahul.nytimessearch.com.example.rahul.nytimessearch.models;

import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * A nytimes document model
 */
public class Article implements Serializable {

    private static final String NYTIMES_BASE = "http://www.nytimes.com/";
    private String webUrl;
    private String headline;
    private String thumbnail;

    public String getWebUrl() {
        return webUrl;
    }

    public String getHeadline() {
        return headline;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public Article(String webUrl, String headline, String thumbnail) {
        this.webUrl = webUrl;
        this.headline = headline;
        this.thumbnail = thumbnail;
    }

    public static Article makeArticle(JSONObject jsonObject) {
        try {
            String webUrl = jsonObject.getString("web_url");
            String headline = jsonObject.getJSONObject("headline").getString("main");
            JSONArray multimedia = jsonObject.getJSONArray("multimedia");
            String thumbnail = "";
            if (multimedia.length() > 0) {
                JSONObject multimediaJson = multimedia.getJSONObject(0);
                thumbnail = NYTIMES_BASE + multimediaJson.getString("url");
            }
            return new Article(webUrl, headline, thumbnail);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ArrayList<Article> fromJsonArray(JSONArray array) {
        ArrayList<Article> articles = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                articles.add(makeArticle(jsonObject));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return articles;
    }
}
