package com.shaverz.cream;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.shaverz.cream.views.OverviewCustomizationViewAdapter;

public class OverviewCustomizationActivity extends AppCompatActivity {

    ListView listView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview_customization);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_home_black_24dp));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list_view);

        OverviewCustomizationViewAdapter adapter = new OverviewCustomizationViewAdapter(this);

        // Assign adapter to ListView
        listView.setAdapter(adapter);
    }

}
