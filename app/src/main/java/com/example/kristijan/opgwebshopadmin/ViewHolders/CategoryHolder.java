package com.example.kristijan.opgwebshopadmin.ViewHolders;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.R;

public class CategoryHolder extends RecyclerView.ViewHolder implements
        View.OnClickListener {

    public TextView cat_text;
    public ImageView cat_img;
    private ItemClickListener itemClickListener;
    public ImageView dots;

    public CategoryHolder(@NonNull View itemView) {
        super(itemView);
        cat_text=(TextView) itemView.findViewById(R.id.txt_category_name);
        cat_img=(ImageView) itemView.findViewById(R.id.img_name);
        dots=(ImageView) itemView.findViewById(R.id.pop_up_menu);
        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(),false);

    }


}
