package com.example.android.moviesupdates.Loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.android.moviesupdates.Model.Trailers;
import com.example.android.moviesupdates.Network.TrailerQueryUtils;

import java.util.List;

/**
 * Created by sunilkumar on 12/12/17.
 */

public class TrailerLoader extends AsyncTaskLoader<List<Trailers>> {

    private String mUrl;

    public TrailerLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Trailers> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        List<Trailers> trailersList = TrailerQueryUtils.fetchTrailerData(mUrl);
        return trailersList;
    }

}
