package com.example.kristijan.opgwebshopadmin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kristijan.opgwebshopadmin.Common.ItemClickListener;
import com.example.kristijan.opgwebshopadmin.Model.NewsModel;
import com.example.kristijan.opgwebshopadmin.Network.CheckConnectivity;
import com.example.kristijan.opgwebshopadmin.ViewHolders.NewsHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class News extends Base {

    FirebaseDatabase database;
    DatabaseReference news;

    NewsModel NewNews;
    String date;

    RecyclerView recycler_news;
    RecyclerView.LayoutManager layoutManager;
    FirebaseRecyclerAdapter<NewsModel, NewsHolder> adapter;

    ProgressDialog mdialog;

    TextView news_heading, news_content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        getSupportActionBar().setTitle(R.string.news);

        database = FirebaseDatabase.getInstance();
        news = database.getReference("news");

        recycler_news = (RecyclerView) findViewById(R.id.recycler_news);
        recycler_news.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler_news.setLayoutManager(layoutManager);

        final Locale locale = new Locale("hr", "HR");

        Calendar calendar = Calendar.getInstance(locale);
        SimpleDateFormat mdformat = new SimpleDateFormat("dd-MM-yyyy");

        date = mdformat.format(calendar.getTime());

        mdialog = new ProgressDialog(News.this);
        mdialog.setMessage(getResources().getString(R.string.wait));

        loadNews();
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

        if (id == R.id.add) {
            showAddDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadNews() {

        mdialog.show();
        if (new CheckConnectivity(this).isNetworkConnectionAvailable()) {
            mdialog.show();
            new CheckConnectivity.TestInternet(this).execute();
        } else {
            mdialog.dismiss();
        }

        adapter = new FirebaseRecyclerAdapter<NewsModel, NewsHolder>(NewsModel.class, R.layout.news_item, NewsHolder.class, news.orderByChild("date")) {
            @Override
            protected void populateViewHolder(final NewsHolder viewHolder, NewsModel model, int position) {

                //stavljanje podataka iz baze u polja za prikaz na ekranu
                viewHolder.news_heading.setText(model.getHeading());
                viewHolder.news_content.setText(model.getContent());
                viewHolder.news_date.setText(model.getDate());

                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {

                        showMenu(viewHolder.dots, adapter.getRef(position).getKey(), adapter.getItem(position));
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
        recycler_news.setAdapter(adapter);
    }

    public void showMenu(View view, final String pos, final NewsModel current) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.menu_click);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_update:
                        showUpdateDialog(pos, current);
                        return true;
                    case R.id.action_delete:
                        deleteNews(pos);
                        return true;
                    default:
                }
                return false;
            }
        });
        //displaying the popup
        popup.show();
    }

    private void showAddDialog() {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(News.this);
        alertDialog.setTitle(R.string.adding_news);
        alertDialog.setMessage(R.string.fill_informations);
        LayoutInflater inflater = this.getLayoutInflater();
        final View add_category_item = inflater.inflate(R.layout.add_news, null);
        news_heading = add_category_item.findViewById(R.id.txt_news_heading);
        news_content = add_category_item.findViewById(R.id.txt_news_content);

        alertDialog.setView(add_category_item);
        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                NewNews = new NewsModel(news_heading.getText().toString(), news_content.getText().toString(), date);
                dialog.dismiss();

                if (NewNews != null) {
                    //Dodavanje nove kategorije u bazu
                    news.push().setValue(NewNews);
                    Toast.makeText(News.this, R.string.news_added, Toast.LENGTH_SHORT).show();
                }
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showUpdateDialog(final String key, final NewsModel item) {

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(News.this);
        alertDialog.setTitle(R.string.update_news);
        alertDialog.setMessage(R.string.fill_informations);
        LayoutInflater inflater = this.getLayoutInflater();
        final View add_category_item = inflater.inflate(R.layout.add_news, null);
        news_heading = add_category_item.findViewById(R.id.txt_news_heading);
        news_content = add_category_item.findViewById(R.id.txt_news_content);
        news_heading.setText(item.getHeading());
        news_content.setText(item.getContent());

        alertDialog.setView(add_category_item);

        alertDialog.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                item.setHeading(news_heading.getText().toString());
                item.setContent(news_content.getText().toString());
                item.setDate(date);
                news.child(key).setValue(item);
                Toast.makeText(News.this, R.string.news_updated, Toast.LENGTH_SHORT).show();
            }
        });
        alertDialog.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDialog.show();
    }

    private void deleteNews(String key) {
        news.child(key).removeValue();
    }
}