package com.example.android.moviesupdates.Loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.moviesupdates.Model.Reviews;
import com.example.android.moviesupdates.Network.ReviewQueryUtils;

import java.util.List;

/**
 * Created by sunilkumar on 12/12/17.
 */

public class ReviewLoader extends AsyncTaskLoader<List<Reviews>> {

    private String mUrl;

    public ReviewLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Reviews> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        List<Reviews> reviewsList = ReviewQueryUtils.fetchReviewData(mUrl);
        return reviewsList;
    }
}
