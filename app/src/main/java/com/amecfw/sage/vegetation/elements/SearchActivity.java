package com.amecfw.sage.vegetation.elements;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.amecfw.sage.model.SageApplication;

/**
 */
public class SearchActivity extends ListActivity {

    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(SageApplication.getInstance().getThemeID());
        super.onCreate(savedInstanceState);
        setContentView(com.amecfw.sage.model.R.layout.simple_list_layout);
        list = (ListView) findViewById(android.R.id.list);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent){
        if(Intent.ACTION_SEARCH.equals(intent.getAction())){
            String query = intent.getStringExtra(SearchManager.QUERY);
        }
    }

    private void doSearch(String queryString){

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        //super.onListItemClick(l, v, position, id);
    }
}
