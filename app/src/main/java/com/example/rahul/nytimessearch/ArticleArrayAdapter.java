package com.example.rahul.nytimessearch;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rahul.nytimessearch.com.example.rahul.nytimessearch.models.Article;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Adapter for article
 */
public class ArticleArrayAdapter extends ArrayAdapter<Article> {

    private final LayoutInflater layoutInflater;

    public ArticleArrayAdapter(Context context, List<Article> articles) {
        super(context, android.R.layout.simple_list_item_1, articles);
        layoutInflater = LayoutInflater.from(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Article article = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_article_result, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvTitle.setText(article.getHeadline());
        holder.ivImage.setImageResource(0);
        String thumbnailUrl = article.getThumbnail();
        if (!TextUtils.isEmpty(thumbnailUrl)) {
            Picasso.with(getContext())
                    .load(article.getThumbnail())
                    .into(holder.ivImage);
        }
        return convertView;
    }

    static class ViewHolder {

        @Bind(R.id.ivImage) ImageView ivImage;
        @Bind(R.id.tvTitle) TextView tvTitle;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

    }
}
