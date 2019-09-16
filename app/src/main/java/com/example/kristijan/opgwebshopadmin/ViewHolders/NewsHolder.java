package com.example.kristijan.opgwebshopadmin.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.R;

public class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView news_heading;
    public TextView news_content;
    public TextView news_date;
    public ImageView dots;
    private ItemClickListener itemClickListener;

    public NewsHolder(@NonNull View itemView) {
        super(itemView);
        news_heading=(TextView) itemView.findViewById(R.id.news_heading);
        news_content=(TextView) itemView.findViewById(R.id.news_content);
        news_date=(TextView) itemView.findViewById(R.id.news_date);
        dots=(ImageView)itemView.findViewById(R.id.popup_news);
        dots.setOnClickListener(this);

    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(),false);

    }

}
