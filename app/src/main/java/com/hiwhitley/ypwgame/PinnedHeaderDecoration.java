package com.hiwhitley.ypwgame;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PinnedHeaderDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "PinnedHeaderDecoration";

    private int mHeaderPosition;
    private int mPinnedHeaderTop;

    private boolean mIsAdapterDataChanged;

    private Rect mClipBounds;
    private View mPinnedHeaderView;
    private RecyclerView.Adapter mAdapter;
    private int viewType;
    private List<Integer> pinnedTypeHeader = new ArrayList<>();

    private final SparseArray<PinnedHeaderCreator> mTypePinnedHeaderFactories = new SparseArray<>();
    private final RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            mIsAdapterDataChanged = true;
        }
    };

    public PinnedHeaderDecoration() {
        this.mHeaderPosition = -1;
    }

    // 当我们调用mRecyclerView.addItemDecoration()方法添加decoration的时候，RecyclerView在绘制的时候，去会绘制decorator，即调用该类的onDraw和onDrawOver方法，
    // 1.onDraw方法先于drawChildren
    // 2.onDrawOver在drawChildren之后，一般我们选择复写其中一个即可。
    // 3.getItemOffsets 可以通过outRect.set()为每个Item设置一定的偏移量，主要用于绘制Decorator。

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {

        //检测标签，并将标签强制固定在顶部
        createPinnedHeader(parent);

        if (mPinnedHeaderView != null && pinnedTypeHeader.contains(viewType)) {
            int headerEndAt = mPinnedHeaderView.getTop() + mPinnedHeaderView.getHeight();

            // 根据坐标查找view，headEnd + 1找到的就是mPinnedHeaderView底部下面的view
            View v = parent.findChildViewUnder(c.getWidth() / 2, headerEndAt + 1);

            if (isHeaderView(parent, v)) {
                // 如果是标签的话，缓存的标签就要同步跟此标签移动
                mPinnedHeaderTop = v.getTop() - mPinnedHeaderView.getHeight();
            } else {
                mPinnedHeaderTop = 0;
            }

            Log.d(TAG, "onDraw" + v.getTop());
            Log.d(TAG, "mPinnedHeaderView.getHeight()" + mPinnedHeaderView.getHeight());
            mClipBounds = c.getClipBounds();
            mClipBounds.top = mPinnedHeaderTop + mPinnedHeaderView.getHeight();
            c.clipRect(mClipBounds);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mPinnedHeaderView != null && pinnedTypeHeader.contains(viewType)) {
            c.save();

            mClipBounds.top = 0;
            c.clipRect(mClipBounds, Region.Op.UNION);
            c.translate(0, mPinnedHeaderTop);
            mPinnedHeaderView.draw(c);

            c.restore();
        }
    }

    private void createPinnedHeader(RecyclerView parent) {
        updatePinnedHeader(parent);

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager == null || layoutManager.getChildCount() <= 0) {
            return;
        }
        //获取第一个可见item的position
        int firstVisiblePosition = ((RecyclerView.LayoutParams) layoutManager.getChildAt(0).getLayoutParams()).getViewAdapterPosition();
        int headerPosition = findPinnedHeaderPosition(parent, firstVisiblePosition);

        if (headerPosition >= 0 && mHeaderPosition != headerPosition) {

            mHeaderPosition = headerPosition;
            //获取标签类型
            viewType = mAdapter.getItemViewType(headerPosition);
            //手动调用创建标签
            RecyclerView.ViewHolder pinnedViewHolder = mAdapter.createViewHolder(parent, viewType);
            mAdapter.bindViewHolder(pinnedViewHolder, headerPosition);
            mPinnedHeaderView = pinnedViewHolder.itemView;

            ViewGroup.LayoutParams layoutParams = mPinnedHeaderView.getLayoutParams();
            if (layoutParams == null) {
                // 标签默认宽度占满parent
                layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mPinnedHeaderView.setLayoutParams(layoutParams);
            }

            // 测量高度
            int heightMode = View.MeasureSpec.getMode(layoutParams.height);
            int heightSize = View.MeasureSpec.getSize(layoutParams.height);

            if (heightMode == View.MeasureSpec.UNSPECIFIED) {
                heightMode = View.MeasureSpec.EXACTLY;
            }

            int maxHeight = parent.getHeight() - parent.getPaddingTop() - parent.getPaddingBottom();
            if (heightSize > maxHeight) {
                heightSize = maxHeight;
            }

            int ws = View.MeasureSpec.makeMeasureSpec(parent.getWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.EXACTLY);
            int hs = View.MeasureSpec.makeMeasureSpec(heightSize, heightMode);
            mPinnedHeaderView.measure(ws, hs);

            // 位置强制布局在顶部
            mPinnedHeaderView.layout(0, 0, mPinnedHeaderView.getMeasuredWidth(), mPinnedHeaderView.getMeasuredHeight());
        }
    }

    /**
     * 从传入位置递减找出标签的位置
     * @param parent
     * @param fromPosition
     * @return
     */
    private int findPinnedHeaderPosition(RecyclerView parent, int fromPosition) {
        if (fromPosition > mAdapter.getItemCount() || fromPosition < 0) {
            return -1;
        }

        for (int position = fromPosition; position >= 0; position--) {
            final int viewType = mAdapter.getItemViewType(position);
            if (isPinnedViewType(parent, position, viewType)) {
                return position;
            }
        }

        return -1;
    }

    public PinnedHeaderDecoration setPinnedTypeHeader(Integer... pinnedTypeHeader) {
        this.pinnedTypeHeader.addAll(Arrays.asList(pinnedTypeHeader));
        return this;
    }

    private boolean isPinnedViewType(RecyclerView parent, int adapterPosition, int viewType) {
        PinnedHeaderCreator pinnedHeaderCreator = mTypePinnedHeaderFactories.get(viewType);

        return pinnedHeaderCreator != null && pinnedHeaderCreator.create(parent, adapterPosition);
    }

    private boolean isHeaderView(RecyclerView parent, View v) {
        int position = parent.getChildAdapterPosition(v);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }

        return isPinnedViewType(parent, position, mAdapter.getItemViewType(position));
    }

    private void updatePinnedHeader(RecyclerView parent) {
        RecyclerView.Adapter adapter = parent.getAdapter();
        if (mAdapter != adapter || mIsAdapterDataChanged) {
            resetPinnedHeader();
            if (mAdapter != null) {
                mAdapter.unregisterAdapterDataObserver(mAdapterDataObserver);
            }

            mAdapter = adapter;
            if (mAdapter != null) {
                mAdapter.registerAdapterDataObserver(mAdapterDataObserver);
            }
        }
    }

    private void resetPinnedHeader() {
        mHeaderPosition = -1;
        mPinnedHeaderView = null;
    }

    public void registerTypePinnedHeader(int itemType, PinnedHeaderCreator pinnedHeaderCreator) {
        mTypePinnedHeaderFactories.put(itemType, pinnedHeaderCreator);
    }

    public interface PinnedHeaderCreator {
        boolean create(RecyclerView parent, int adapterPosition);
    }
}
