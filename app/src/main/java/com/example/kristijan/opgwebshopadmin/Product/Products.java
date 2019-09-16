package com.example.kristijan.opgwebshopadmin.Product;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ViewSwitcher;

import com.bumptech.glide.Glide;
import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.Model.Product;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.R;
import com.example.kristijan.opgwebshopadmin.ViewHolders.ProductHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.Locale;

public class Products extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    FirebaseDatabase database;
    DatabaseReference product;

    RecyclerView recycler_product;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<Product,ProductHolder> adapter;

    String categoryId="";
    AlertDialog adialog;

    SwipeRefreshLayout refresh_product;

    ViewSwitcher viewSwitcher;
    View myFirstView,mySecondView;

    Locale locale = new Locale("hr","HR");
    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(R.string.productActivity);

        adialog = new ProgressDialog(Products.this);
        adialog.setMessage(getResources().getString(R.string.wait));

        viewSwitcher = (ViewSwitcher)findViewById(R.id.viewSwitcherProduct);
        myFirstView= findViewById(R.id.switch_product_one);
        mySecondView = findViewById(R.id.switch_product_two);

        refresh_product = (SwipeRefreshLayout) findViewById(R.id.refresh_product);

        database=FirebaseDatabase.getInstance();
        product= database.getReference("product");

        recycler_product=(RecyclerView) findViewById(R.id.product_recycler);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(this, 2);
        recycler_product.setLayoutManager(mLayoutManager);
        recycler_product.setItemAnimator(new DefaultItemAnimator());

        if (getIntent() !=null)
        {
            categoryId = getIntent().getStringExtra("categoryId");
        }

        final Query queries=product.orderByChild("categoryId").equalTo(categoryId);
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists())
                {
                    viewSwitcher.showPrevious();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        if(!categoryId.isEmpty() && categoryId != null)
        {
            loadProduct(categoryId);
        }

        refresh_product.setOnRefreshListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_category, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.add)
        {
            Intent Product_add = new Intent(Products.this, AddProduct.class);
            Product_add.putExtra("CategoryId",categoryId);
            startActivity(Product_add);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadProduct(final String categoryId) {
        adialog.show();
        if( new CheckConnectivity(this).isNetworkConnectionAvailable())
        {
            adialog.show();
            new CheckConnectivity.TestInternet(this).execute();
        }
        else
        {
            adialog.dismiss();
        }

        // recycler adapter Library za rad sa firebase
        adapter = new FirebaseRecyclerAdapter<Product, ProductHolder>(Product.class,R.layout.product_item,ProductHolder.class,product.orderByChild("categoryId").equalTo(categoryId)) {

            @Override
            protected void populateViewHolder(final ProductHolder viewHolder, Product prod, final int position) {
                //stavljanje podataka iz baze u polja za prikaz na ekranu

                int discoount = Math.round((prod.getPrice() / 100.0f) * prod.getDiscount());
                if (discoount > 0)
                {
                    viewHolder.product_discount.setVisibility(View.VISIBLE);
                }
                else
                {
                    viewHolder.product_discount.setVisibility(View.INVISIBLE);
                }

                int discounted_price=prod.getPrice()-discoount;
                viewHolder.product_name.setText(prod.getName());
                viewHolder.product_discount.setText(" - "+(String.valueOf(prod.getDiscount())+"%"));
                String imgUrl = prod.getImage();
                Glide.with(getBaseContext()).load(imgUrl).thumbnail(0.5f).into(viewHolder.product_image);


                viewHolder.product_price.setText(fmt.format(discounted_price));

                //klikanje na pojedinu kategoriju i pokretanje nove aktivnosti
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent Product_detail = new Intent(Products.this,ProductDetail.class);
                        Product_detail.putExtra("CategoryId",categoryId);
                        Product_detail.putExtra("productId",adapter.getRef(position).getKey());
                        startActivity(Product_detail);
                    }
                });
                viewHolder.dots.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showMenu(viewHolder.dots,adapter.getRef(position).getKey());
                    }
                });
            }
            @Override
            protected void onDataChanged() {
                if (adialog != null && adialog.isShowing()) {
                    adialog.dismiss();
                }
            }
        };

        adapter.notifyDataSetChanged();
        recycler_product.setAdapter(adapter);

    }

    @Override
    public void onRefresh() {
        loadProduct(categoryId);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_product.setRefreshing(false);
            }
        }, 2000);
    }

    public void showMenu(View view, final String pos) {
        PopupMenu popup = new PopupMenu(this,view);
        popup.inflate(R.menu.menu_click);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_update:

                        Intent Product_Update = new Intent(Products.this, UpdateProduct.class);
                        Product_Update.putExtra("CategoryId",categoryId);
                        Product_Update.putExtra("productId",pos);
                        startActivity(Product_Update);
                        return true;

                    case R.id.action_delete:
                        //Toast.makeText(mContext, "Play next", Toast.LENGTH_SHORT).show();
                        deleteProduct(pos);

                        return true;
                    default:
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();

    }

    private void deleteProduct(String key) {
        product.child(key).removeValue();

        if (adapter.getItemCount()==1)
        {
            viewSwitcher.showPrevious();

        }
    }

}
