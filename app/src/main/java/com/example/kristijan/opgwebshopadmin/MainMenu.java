package com.example.kristijan.opgwebshopadmin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kristijan.opgwebshopadmin.Categorys.AddCategory;
import com.example.kristijan.opgwebshopadmin.Categorys.UpdateCategory;
import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.Model.AdminUser;
import com.example.kristijan.opgwebshopadmin.Model.Category;
import com.example.kristijan.opgwebshopadmin.Model.Token;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.Orders.CancelOrders;
import com.example.kristijan.opgwebshopadmin.Orders.NewOrders;
import com.example.kristijan.opgwebshopadmin.Orders.RecievedOrders;
import com.example.kristijan.opgwebshopadmin.Orders.ShippedOrders;
import com.example.kristijan.opgwebshopadmin.Product.Products;
import com.example.kristijan.opgwebshopadmin.UserAdministration.CreateAccount;
import com.example.kristijan.opgwebshopadmin.UserAdministration.LogIn;
import com.example.kristijan.opgwebshopadmin.UserAdministration.Profile;
import com.example.kristijan.opgwebshopadmin.ViewHolders.CategoryHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

public class MainMenu extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SwipeRefreshLayout.OnRefreshListener {
    TextView set_email;

    int admin;

    FirebaseDatabase database;
    DatabaseReference category;
    DatabaseReference adminUser;
    FirebaseRecyclerAdapter<Category,CategoryHolder> adapter;

    AdminUser user= new AdminUser();

    RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;

    SwipeRefreshLayout refresh_category;

    ProgressDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        refresh_category = findViewById(R.id.refresh_category);

        View headerView = navigationView.getHeaderView(0);

        set_email =  headerView.findViewById(R.id.set_user_email);
        set_email.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail());

        database=FirebaseDatabase.getInstance();

        category= database.getReference("category");
        adminUser = database.getReference("adminUser");

        is_admin();

        //Inicijalizacija recycler view-a
        recycler_menu= findViewById(R.id.category_recycler);
        recycler_menu.setHasFixedSize(true);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,2));

        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( MainMenu.this,  new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String mToken = instanceIdResult.getToken();
                Log.e("Token",mToken);
                updateToken(mToken);
            }
        });


        mdialog = new ProgressDialog(MainMenu.this);
        mdialog.setMessage(getResources().getString(R.string.wait));

        loadCategory();

        refresh_category.setOnRefreshListener(this);
    }

    private void updateToken(String mToken) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens=db.getReference("Tokens");
        Token data=new Token(mToken,true);
        tokens.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(data);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {

            if(admin==0)
            {
                Toast.makeText(this, R.string.rights, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Intent create_account = new Intent(MainMenu.this, CreateAccount.class);
                startActivity(create_account);
            }
            return true;
        }
        else if(id == R.id.add)
        {
            Intent add_category = new Intent(MainMenu.this, AddCategory.class);
            startActivity(add_category);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent Profie = new Intent(MainMenu.this, Profile.class);
            startActivity(Profie);

        } else if (id == R.id.nav_orders_shipped) {
            Intent shipped = new Intent(MainMenu.this, ShippedOrders.class);
            startActivity(shipped);

        } else if (id == R.id.nav_log_out) {
            SignOut();

        }else if (id == R.id.nav_orders_recieved) {
            Intent received = new Intent(MainMenu.this, RecievedOrders.class);
            startActivity(received);
        }
        else if (id == R.id.nav_orders_cancel) {
            Intent cancel = new Intent(MainMenu.this, CancelOrders.class);
            startActivity(cancel);
        }

        else if (id == R.id.nav_orders) {
            Intent newOrders = new Intent(MainMenu.this, NewOrders.class);
            startActivity(newOrders);
        }
        else if (id == R.id.nav_about) {
            Intent about = new Intent(MainMenu.this, UpdateAbout.class);
            startActivity(about);
        }
        else if (id == R.id.nav_news) {
            Intent news = new Intent(MainMenu.this, News.class);
            startActivity(news);
        }

        DrawerLayout drawer =  findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRefresh() {
        loadCategory();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh_category.setRefreshing(false);
            }
        }, 2000);
    }



    private void SignOut() {
        Toast.makeText(this, "Singout", Toast.LENGTH_SHORT).show();
        FirebaseAuth.getInstance().signOut();
        Intent login = new Intent(MainMenu.this, LogIn.class);
        startActivity(login);
        finish();

    }

    private void loadCategory() {

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
        adapter = new FirebaseRecyclerAdapter<Category, CategoryHolder>(Category.class,R.layout.category_item,CategoryHolder.class,category) {
            @Override
            protected void populateViewHolder(final CategoryHolder viewHolder, Category model, final int position) {

                //stavljanje podataka iz baze u polja za prikaz na ekranu
                viewHolder.cat_text.setText(model.getName());
                String imgUrl = model.getImage();
                Glide.with(getBaseContext()).load(imgUrl).thumbnail(0.5f).into(viewHolder.cat_img);


                //klikanje na pojedinu kategoriju i pokretanje nove aktivnosti
                viewHolder.setItemClickListener(new ItemClickListener() {

                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        Intent product_intent = new Intent(MainMenu.this,Products.class);
                        product_intent.putExtra("categoryId",adapter.getRef(position).getKey());
                        startActivity(product_intent);
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
                if (mdialog != null && mdialog.isShowing()) {
                    mdialog.dismiss();
                }
            }

        };

        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
    }

    public void showMenu(View view, final String pos) {
        PopupMenu popup = new PopupMenu(this,view);
        popup.inflate(R.menu.menu_click);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_update:
                        Intent product_Update = new Intent(MainMenu.this,UpdateCategory.class);
                        product_Update.putExtra("categoryIdUpdate",pos);
                        startActivity(product_Update);
                        return true;
                    case R.id.action_delete:
                        deleteCategory(pos);
                        return true;
                    default:

                }
                return false;
            }
        });
        //displaying the popup
        popup.show();

    }

    private void deleteCategory(String key) {
        category.child(key).removeValue();
    }

    private void is_admin()
    {
        adminUser.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //FirebaseUser Auth_user = FirebaseAuth.getInstance().getCurrentUser();
                //Provjera dali korisnik postoji u bazi
                if (dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists()) {
                    user = dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(AdminUser.class);
                    admin=user.getIsAdmin();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
