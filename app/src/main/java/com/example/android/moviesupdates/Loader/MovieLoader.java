package com.example.android.moviesupdates.Loader;

import android.content.Context;

import com.example.android.moviesupdates.Model.Movies;
import com.example.android.moviesupdates.Network.MovieQueryUtils;

import java.util.List;

/**
 * Created by sunilkumar on 12/12/17.
 */

public class MovieLoader extends android.content.AsyncTaskLoader<List<Movies>> {

    private String mUrl;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Movies> loadInBackground() {
        if (mUrl == null) {
            return null;
        }
        List<Movies> moviesList = MovieQueryUtils.fetchMovieData(mUrl);
        return moviesList;
    }
}
