package com.hiwhitley.ypwgame;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<RecyclerItem> mRecyclerItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_main);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerItems = new ArrayList<>();
        for (int i = 0; i < 15; i++) {
            mRecyclerItems.add(new RecyclerItem(RecyclerItem.ITEM_HEADER));
            mRecyclerItems.add(new RecyclerItem(RecyclerItem.ITEM_POSTER));
            mRecyclerItems.add(new RecyclerItem(RecyclerItem.ITEM_DESC));
        }
        mRecyclerView.setAdapter(new MainListAdapter(mRecyclerItems));
        PinnedHeaderDecoration pinnedHeaderDecoration = new PinnedHeaderDecoration();
        pinnedHeaderDecoration.registerTypePinnedHeader(RecyclerItem.ITEM_HEADER, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        pinnedHeaderDecoration.registerTypePinnedHeader(RecyclerItem.ITEM_DESC, new PinnedHeaderDecoration.PinnedHeaderCreator() {
            @Override
            public boolean create(RecyclerView parent, int adapterPosition) {
                return true;
            }
        });
        mRecyclerView.addItemDecoration(pinnedHeaderDecoration);
    }
}
