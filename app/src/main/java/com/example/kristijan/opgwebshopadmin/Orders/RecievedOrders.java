package com.example.kristijan.opgwebshopadmin.Orders;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.kristijan.opgwebshopadmin.Base;
import com.example.kristijan.opgwebshopadmin.Common.Common;
import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.Model.Order;
import com.example.kristijan.opgwebshopadmin.Model.Product;
import com.example.kristijan.opgwebshopadmin.PDF.Pdf;
import com.example.kristijan.opgwebshopadmin.Model.DataMessage;
import com.example.kristijan.opgwebshopadmin.Model.MyResponse;
import com.example.kristijan.opgwebshopadmin.Model.OrderRequest;
import com.example.kristijan.opgwebshopadmin.Model.Token;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.OrderDetail;
import com.example.kristijan.opgwebshopadmin.R;
import com.example.kristijan.opgwebshopadmin.Remote.APIService;
import com.example.kristijan.opgwebshopadmin.ViewHolders.OrderHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecievedOrders extends Base {

    FirebaseDatabase database;
    DatabaseReference order_request;
    DatabaseReference product;

    RecyclerView order_recycler;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<OrderRequest, OrderHolder> adapter;

    Product prod;

    List<Order > products;
    int i;

    ProgressDialog mdialog;

    ViewSwitcher viewSwitcher;
    View myFirstView,mySecondView;

    APIService mservice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recieved_orders);
        getSupportActionBar().setTitle(R.string.recieved_ordersActivity);

        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcherProduct);
        myFirstView = findViewById(R.id.switch_product_one);
        mySecondView = findViewById(R.id.switch_product_two);

        database = FirebaseDatabase.getInstance();
        order_request = database.getReference("orderRequest");
        product=database.getReference("product");

        mservice= Common.getFCMClient();

        order_recycler = (RecyclerView)findViewById(R.id.orders_recycler) ;
        order_recycler.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        order_recycler.setLayoutManager(layoutManager);

        mdialog = new ProgressDialog(RecievedOrders.this);
        mdialog.setMessage(getResources().getString(R.string.wait));

        final Query queries = order_request.orderByChild("status").equalTo("1");
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
        adapter = new FirebaseRecyclerAdapter<OrderRequest, OrderHolder>(OrderRequest.class, R.layout.order_item, OrderHolder.class, order_request.orderByChild("status").equalTo("1")) {

            @Override
            protected void populateViewHolder(final OrderHolder viewHolder, OrderRequest ord, final int position) {
                //stavljanje podataka iz baze u polja za prikaz na ekranu
                viewHolder.order_date.setText(ord.getDate());
                viewHolder.order_number.setText(adapter.getRef(position).getKey());
                viewHolder.order_status.setText(ord.getStatus());
                viewHolder.order_total_items.setText(String.valueOf(ord.getProducts().size()));
                viewHolder.order_total_price.setText(ord.getTotal()+" HRK");
                viewHolder.order_status.setText(R.string.order_recieved_small);
                viewHolder.order_status.setTextColor(Color.parseColor("#ff9900"));

                //klikanje na pojedinu kategoriju i pokretanje nove aktivnosti
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent order_detail = new Intent(RecievedOrders.this,OrderDetail.class);
                        order_detail.putExtra("OrderId",adapter.getRef(position).getKey());
                        order_detail.putExtra("status","1");
                        startActivity(order_detail);
                    }
                });

                viewHolder.order_detail_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showMenu(viewHolder.order_detail_btn, adapter.getRef(position).getKey(), adapter.getItem(position));
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

    public void showMenu(View view, final String pos, final OrderRequest current) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.order_update_shipped);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_update_recieved:

                        current.setStatus("2");
                        order_request.child(pos).setValue(current);

                        sendOrderStatusToUser(pos,current);

                        Toast.makeText(RecievedOrders.this, R.string.status_updated, Toast.LENGTH_SHORT).show();

                        return true;

                    default:
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();
    }

    private void sendOrderStatusToUser(final String key, OrderRequest current) {

        DatabaseReference tokens= database.getReference("Tokens");
        tokens.orderByKey().equalTo(current.getUserUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {

                    Token token = postSnapShot.getValue(Token.class);

                    Map<String, String> datasend=new HashMap<>();

                    datasend.put("title","OPG WebShop");
                    datasend.put("message",getResources().getString(R.string.your_order)+ " " +key+ " " +getResources().getString(R.string.updated_status)+" "+getResources().getString(R.string.shipped_status));
                    DataMessage dataMessage=new DataMessage(token.getToken(),datasend);

                    mservice.sendNotification(dataMessage).enqueue(new Callback<MyResponse>() {
                        @Override
                        public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {

                            if(response.body().success==1)
                            {
                                Toast.makeText(RecievedOrders.this, R.string.order_updated, Toast.LENGTH_SHORT).show();
                            }
                            else
                            {
                                Toast.makeText(RecievedOrders.this, R.string.order_notification_failed, Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResponse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText(RecievedOrders.this, R.string.order_notification_failed, Toast.LENGTH_SHORT).show();


            }
        });
    }
}
