package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Arnold on 4/30/2017.
 */

public class ArticleDetailFragmentAppCompat extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArtcleDetlFrgmAppCompat";
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy");
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2,1,1);
    private int mMutedColor = 0xFF333333;


    public long mItemId;
    private boolean mIsCard = false;
    private CoordinatorLayout mCoordinatorLayout;
    private ImageView mImageView;
    public Toolbar mToolbar;
    private TextView mBodyTextView;
    private TextView mSubtitleView;
    private TextView mTitleView;

    private FloatingActionButton mFab;

    public static ArticleDetailFragmentAppCompat newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragmentAppCompat fragment = new ArticleDetailFragmentAppCompat();
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause id: " + mItemId);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);


        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_article_detail_appcompat, container, false);

        mImageView = (ImageView) rootView.findViewById(R.id.detail_imageview);
        mBodyTextView = (TextView) rootView.findViewById(R.id.article_body);
        mCoordinatorLayout = (CoordinatorLayout) rootView.findViewById(R.id.coordinator_rootlayout);
        mToolbar = (Toolbar) rootView.findViewById(R.id.detail_toolbar);
        mTitleView = (TextView) rootView.findViewById(R.id.article_title);
        mSubtitleView = (TextView) rootView.findViewById(R.id.article_subtitle);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                Log.d(TAG, "NavigationOnClickListener...");
                getActivity().onBackPressed();
            }
        });


        /*
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        mActionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setDisplayShowHomeEnabled(true);
        mActionBar.setDisplayShowTitleEnabled(false);
        */



        mFab = (FloatingActionButton)rootView.findViewById(R.id.share_fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(mTitleView.getText())
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        mBodyTextView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));



        return rootView;
    }



    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected...");

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                Log.d(TAG, "onOptionsItemSelected, home!");
                NavUtils.navigateUpFromSameTask(getActivity());


                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private Date parsePublishedDate(Cursor c) {
        try {
            String date = c.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (!isAdded()) {
            if (data != null) {
                data.close();
            }
            return;
        }

        if (data == null || !data.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            if (data != null)
            {
                data.close();
            }

            data = null;

            return;
        }
        mTitleView.setText(data.getString(ArticleLoader.Query.TITLE));
        Date publishedDate = parsePublishedDate(data);


        mSubtitleView.setText(Html.fromHtml("<font color='#ffffff'>"
                        + data.getString(ArticleLoader.Query.AUTHOR)
                        + "</font>  (" +
                outputFormat.format(publishedDate) + ")"));
        /*
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            mSubtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + data.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

        } else {
            // If date is before 1902, just show the string
            mSubtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate) + " by <font color='#ffffff'>"
                            + data.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));

        }
        */

        mBodyTextView.setText(Html.fromHtml(data.getString(ArticleLoader.Query.BODY).replaceAll("(\\r\\n\\r\\n)","<br /><br />").replaceAll("(\r\n|\n)", " ")));
        ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
                .get(data.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                        Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            Palette p = Palette.generate(bitmap, 12);
                            mMutedColor = p.getDarkMutedColor(0xFF333333);
                            mImageView.setImageBitmap(imageContainer.getBitmap());
                            mToolbar.setBackgroundColor(mMutedColor);
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
