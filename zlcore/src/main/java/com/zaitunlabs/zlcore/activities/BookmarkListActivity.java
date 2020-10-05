package com.zaitunlabs.zlcore.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ahsailabs.alcore.core.BaseActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.zaitunlabs.zlcore.R;
import com.zaitunlabs.zlcore.fragments.BookmarkListActivityFragment;

import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;


public class BookmarkListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        enableUpNavigation();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        showFragment(R.id.fragment, BookmarkListActivityFragment.class, new PostFragmentInstantiation<BookmarkListActivityFragment>() {
            @Override
            public void postInstantiation(BookmarkListActivityFragment fragment) {

            }
        }, savedInstanceState, "bookmarkList");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public static void start(Context context){
        Intent intent = new Intent(context,BookmarkListActivity.class);
        context.startActivity(intent);
    }

}
