package com.hiwhitley.ypwgame;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by hiwhitley on 2016/10/17.
 */

public class RecyclerItem implements MultiItemEntity {
    public static final int ITEM_HEADER = 1;
    public static final int ITEM_POSTER = 2;
    public static final int ITEM_DESC = 3;

    private int type;

    public RecyclerItem(int type) {
        this.type = type;
    }

    @Override
    public int getItemType() {
        return type;
    }
}
