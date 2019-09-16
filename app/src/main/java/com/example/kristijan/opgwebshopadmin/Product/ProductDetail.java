package com.example.kristijan.opgwebshopadmin.Product;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.kristijan.opgwebshopadmin.Base;
import com.example.kristijan.opgwebshopadmin.CommentActivity;
import com.example.kristijan.opgwebshopadmin.Model.Product;
import com.example.kristijan.opgwebshopadmin.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

import info.hoang8f.widget.FButton;

public class ProductDetail extends Base {

    LinearLayout layoutDiscount;

    TextView ProductDiscountDescription,ProductPrice,ProductDiscount,ProductMesUnit,ProductDescription;
    ImageView ProductImage,DiscountImage;

    CollapsingToolbarLayout collapsingToolbarLayout;

    FirebaseDatabase database;
    DatabaseReference product;

    FButton btn_update,btn_comment;

    int discounted_price=0;

    String product_id="";
    String Category_id="";

    Product currentProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        getSupportActionBar().setTitle("");

        database=FirebaseDatabase.getInstance();
        product= database.getReference("product");


        ProductMesUnit=(TextView) findViewById(R.id.product_name_detail);
        ProductPrice=(TextView) findViewById(R.id.food_price_detail);
        ProductDiscount=(TextView) findViewById(R.id.food_discount_detail);
        ProductDiscountDescription=(TextView) findViewById(R.id.food_discount_description);
        ProductDescription=(TextView) findViewById(R.id.product_description_detail);

        ProductImage=(ImageView) findViewById(R.id.img_detail);
        DiscountImage=(ImageView) findViewById(R.id.DiscountImage);

        collapsingToolbarLayout=(CollapsingToolbarLayout)findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.ColapsedAppbar);

        btn_update=(FButton)findViewById(R.id.btn_update) ;
        btn_comment=(FButton)findViewById(R.id.btn_comment) ;

        layoutDiscount=(LinearLayout)findViewById(R.id.layoutDiscount);


        //dohvaćanje intenta iz ModelActivity
        if (getIntent() !=null)
        {
            product_id = getIntent().getStringExtra("productId");
            Category_id=getIntent().getStringExtra("CategoryId");
        }
        if(!product_id.isEmpty() && product_id != null)
        {
            load_product_details(product_id);

        }

        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent update = new Intent(ProductDetail.this, UpdateProduct.class);
                update.putExtra("productId",product_id);
                update.putExtra("CategoryId",Category_id);
                startActivity(update);

            }
        });

        btn_comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent comment = new Intent(ProductDetail.this,CommentActivity.class);
                comment.putExtra("productId",product_id);
                startActivity(comment);
            }
        });
    }

    private void load_product_details(String product_id) {
        product.child(product_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentProd=dataSnapshot.getValue(Product.class);
                String imgUrl = currentProd.getImage();
                Glide.with(getBaseContext()).load(imgUrl).thumbnail(0.5f).into(ProductImage);

                int discoount = Math.round((currentProd.getPrice() / 100.0f) * currentProd.getDiscount());
                discounted_price=currentProd.getPrice()-discoount;

                if (discoount > 0)
                {
                    ProductDiscount.setVisibility(View.VISIBLE);
                    ProductDiscountDescription.setVisibility(View.VISIBLE);
                    DiscountImage.setVisibility(View.VISIBLE);
                }
                else
                {
                    layoutDiscount.setVisibility(View.GONE);
                    ProductDiscount.setVisibility(View.GONE);
                    ProductDiscountDescription.setVisibility(View.GONE);
                    DiscountImage.setVisibility(View.GONE);
                }

                ProductDiscount.setText(" - "+(String.valueOf(currentProd.getDiscount())+"%"));
                collapsingToolbarLayout.setTitle(currentProd.getName());
                ProductMesUnit.setText(currentProd.getMesUnit());
                ProductDescription.setText(currentProd.getDescription());

                Locale locale = new Locale("hr","HR");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                ProductPrice.setText(fmt.format(discounted_price));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
