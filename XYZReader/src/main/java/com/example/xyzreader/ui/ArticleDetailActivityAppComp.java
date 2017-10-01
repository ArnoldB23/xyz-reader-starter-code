package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * Created by Arnold on 4/28/2017.
 */

public class ArticleDetailActivityAppComp extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private ViewPager mViewPager;
    private Toolbar mAppbar;
    private ActionBar mActionBar;
    private MyPagerAdapter mPagerAdapter;
    private long mStartId;
    private int lastPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_detail_appcompat);

        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mAppbar = (Toolbar) findViewById(R.id.detail_appbar);

        setSupportActionBar(mAppbar);
        mActionBar = getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);

        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);


        getLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mPagerAdapter.swapCursor(data);

        if (mStartId > 0)
        {
            data.moveToFirst();
            // TODO: optimize
            while (!data.isAfterLast()) {
                if (data.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = data.getPosition();
                    mViewPager.setCurrentItem(position, false);
                    break;
                }
                data.moveToNext();
            }
            mStartId = 0;

        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mPagerAdapter.swapCursor(null);
    }

    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        private Cursor mCursor;

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
            ArticleDetailFragmentAppCompat fragment = (ArticleDetailFragmentAppCompat) object;
            if (fragment != null && lastPosition == -1) {

                lastPosition = position;

            }
        }

        @Override
        public Fragment getItem(int position) {
            mCursor.moveToPosition(position);
            return ArticleDetailFragmentAppCompat.newInstance(mCursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }

        public Cursor swapCursor(Cursor newCursor)
        {
            if (mCursor == newCursor)
            {
                return null;
            }

            Cursor oldCursor = mCursor;

            this.mCursor = newCursor;
            notifyDataSetChanged();

            return oldCursor;
        }
    }
}
