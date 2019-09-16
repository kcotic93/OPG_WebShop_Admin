package com.example.kristijan.opgwebshopadmin.Orders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ViewSwitcher;

import com.example.kristijan.opgwebshopadmin.Base;
import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.Model.OrderRequest;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.OrderDetail;
import com.example.kristijan.opgwebshopadmin.R;
import com.example.kristijan.opgwebshopadmin.ViewHolders.OrderHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class ShippedOrders extends Base {

    FirebaseDatabase database;
    DatabaseReference order_request;

    RecyclerView order_recycler;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<OrderRequest, OrderHolder> adapter;

    ProgressDialog mdialog;

    ViewSwitcher viewSwitcher;
    View myFirstView,mySecondView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shipped_orders);
        getSupportActionBar().setTitle(R.string.shipped_ordersActivity);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherProduct);
        myFirstView = findViewById(R.id.switch_product_one);
        mySecondView = findViewById(R.id.switch_product_two);

        database = FirebaseDatabase.getInstance();
        order_request = database.getReference("orderRequest");

        order_recycler = (RecyclerView)findViewById(R.id.orders_recycler) ;
        order_recycler.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        order_recycler.setLayoutManager(layoutManager);

        mdialog = new ProgressDialog(ShippedOrders.this);
        mdialog.setMessage(getResources().getString(R.string.wait));

        final Query queries = order_request.orderByChild("status").equalTo("2");
        queries.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    viewSwitcher.showPrevious();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        loadOrders();
    }


    private void loadOrders() {

        mdialog.show();
        if( new CheckConnectivity(this).isNetworkConnectionAvailable())
        {
            mdialog.show();
            new CheckConnectivity.TestInternet(this).execute();
        }
        else
        {
            mdialog.dismiss();
        }
        // recycler adapter Library za rad sa firebase
        adapter = new FirebaseRecyclerAdapter<OrderRequest, OrderHolder>(OrderRequest.class, R.layout.order_item, OrderHolder.class, order_request.orderByChild("status").equalTo("2")) {

            @Override
            protected void populateViewHolder(final OrderHolder viewHolder, OrderRequest ord, final int position) {
                //stavljanje podataka iz baze u polja za prikaz na ekranu
                viewHolder.order_detail_btn.setVisibility(View.GONE);
                viewHolder.order_date.setText(ord.getDate());
                viewHolder.order_number.setText(adapter.getRef(position).getKey());
                viewHolder.order_status.setText(ord.getStatus());
                viewHolder.order_total_items.setText(String.valueOf(ord.getProducts().size()));
                viewHolder.order_total_price.setText(ord.getTotal()+" HRK");
                viewHolder.order_status.setText(R.string.order_shipped);
                viewHolder.order_status.setTextColor(Color.parseColor("#63ff00"));

                //klikanje na pojedinu kategoriju i pokretanje nove aktivnosti
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent order_detail = new Intent(ShippedOrders.this,OrderDetail.class);
                        order_detail.putExtra("OrderId",adapter.getRef(position).getKey());
                        order_detail.putExtra("status","2");
                        startActivity(order_detail);
                    }
                });
            }
            @Override
            protected void onDataChanged() {
                if (mdialog != null && mdialog.isShowing()) {
                    mdialog.dismiss();
                }
            }
        };

        adapter.notifyDataSetChanged();

        order_recycler.setAdapter(adapter);
    }
}
