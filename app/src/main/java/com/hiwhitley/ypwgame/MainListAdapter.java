package com.hiwhitley.ypwgame;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

/**
 * Created by hiwhitley on 2016/10/17.
 */

public class MainListAdapter extends BaseMultiItemQuickAdapter<RecyclerItem> {
    public MainListAdapter(List<RecyclerItem> data) {
        super(data);
        addItemType(RecyclerItem.ITEM_HEADER, R.layout.item_header);
        addItemType(RecyclerItem.ITEM_POSTER, R.layout.item_poster);
        addItemType(RecyclerItem.ITEM_DESC, R.layout.item_desc);
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, RecyclerItem recyclerItem) {

    }
}
