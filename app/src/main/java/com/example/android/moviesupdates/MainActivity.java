package com.example.android.moviesupdates;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.android.moviesupdates.Adapter.GridAdapter;
import com.example.android.moviesupdates.Loader.MovieLoader;
import com.example.android.moviesupdates.Model.Movies;
import com.example.android.moviesupdates.data.MovieContract;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<List<Movies>>, SharedPreferences.OnSharedPreferenceChangeListener, GridAdapter.Callbacks{

    private static final int MOVIE_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID = 4;
    private boolean mTwoPane;
    public static final String SORT_ORDER = "sort_order";
    public static final String TOP_RATED = "top_rated";
    public static final String POPULAR = "popular";
    private static final String MOVIE_BASE_URL = "https://api.themoviedb.org/3/movie/";
    private static final String API_KEY_STRING = "api_key";
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.progress_bar)
    ProgressBar mProgressBar;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.moviesGrid)
    GridView mGridView;
    @Bind(R.id.iv_movie_film)
    ImageView mEmptyFavMovieView;

    ConstraintLayout mErrorLayout, mEmptyFavoriteLayout;
    GridAdapter mAdapter;
    ConnectivityManager connManager;
    NetworkInfo networkInfo;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(LOG_TAG, "OnCreate called");
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mErrorLayout = (ConstraintLayout) findViewById(R.id.error_layout);
        mErrorLayout.setVisibility(View.GONE);
        mEmptyFavoriteLayout = (ConstraintLayout) findViewById(R.id.empty_fav_container);
        mEmptyFavoriteLayout.setVisibility(View.GONE);
        mToolbar.setTitle(R.string.app_name);
        setSupportActionBar(mToolbar);

        mAdapter = new GridAdapter(MainActivity.this, new ArrayList<Movies>(), this);
        mGridView.setAdapter(mAdapter);
        mTwoPane = findViewById(R.id.movie_detail_container) != null;
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movies selectedMovie = (Movies) parent.getItemAtPosition(position);
                Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
                intent.putExtra(DetailActivity.EXTRA_MOVIE, selectedMovie);
                startActivity(intent);

            }
        });
        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            LoaderManager loaderManager = getLoaderManager();

            Bundle b = new Bundle();
            b.putString(SORT_ORDER, POPULAR);

            loaderManager.initLoader(MOVIE_LOADER_ID, b, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            mErrorLayout.setVisibility(View.VISIBLE);
            Snackbar snackbar = Snackbar.make(mGridView, getString(R.string.check_connection), Snackbar.LENGTH_LONG);
            View snackBarView = snackbar.getView();
            snackBarView.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
            snackbar.show();
        }
    }

    @Override
    public void openForTab(Movies movie, int position) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.EXTRA_MOVIE, movie);
            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction().replace(R.id.movie_detail_container, fragment).commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class);
            intent.putExtra(DetailFragment.EXTRA_MOVIE, movie);
            startActivity(intent);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter.isEmpty()) {
            mAdapter.getMovies();
        }
        Log.v(LOG_TAG, "OnStart called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Bundle b = new Bundle();
        switch (item.getItemId()) {
            case R.id.most_pop:
                b.putString(SORT_ORDER, POPULAR);
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, b, this);

                return true;

            case R.id.high_rated:
                b.putString(SORT_ORDER, TOP_RATED);
                getLoaderManager().restartLoader(MOVIE_LOADER_ID, b, this);

                return true;

            case R.id.favorite:

                getLoaderManager().initLoader(CURSOR_LOADER_ID, null, favoriteLoaderManager);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }


    }

    @Override
    public Loader<List<Movies>> onCreateLoader(int id, Bundle args) {

        String apiParam = null;
        if ((args != null) && (args.getString(SORT_ORDER) != null)) {
            apiParam = args.getString(SORT_ORDER);
        }

        Uri baseUri = Uri.parse(MOVIE_BASE_URL + apiParam);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendQueryParameter(API_KEY_STRING, BuildConfig.API_KEY);
        uriBuilder.appendQueryParameter("language", "en-US");
        uriBuilder.appendQueryParameter("page", "1");

        return new MovieLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movies>> loader, List<Movies> movies) {
        mProgressBar.setVisibility(View.GONE);
        mAdapter.clear();
        if (movies != null && !movies.isEmpty()) {
            mAdapter.addAll(movies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movies>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mAdapter.clear();
        mProgressBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(MOVIE_LOADER_ID, null, this);
    }

    private LoaderManager.LoaderCallbacks<Cursor> favoriteLoaderManager = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            return new CursorLoader(getApplicationContext(), MovieContract.MovieEntry.CONTENT_URI, MovieContract.MovieEntry.MOVIE_COLUMNS, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (data.getCount() > 0) {
                mToolbar.setTitle(R.string.favorites);
                mErrorLayout.setVisibility(View.GONE);
                findViewById(R.id.movie_list_container).setVisibility(View.VISIBLE);
                mEmptyFavoriteLayout.setVisibility(View.GONE);
            } else {
                mToolbar.setTitle(R.string.favorites);
                mErrorLayout.setVisibility(View.GONE);
                findViewById(R.id.movie_list_container).setVisibility(View.GONE);
                mEmptyFavoriteLayout.setVisibility(View.VISIBLE);
                mEmptyFavMovieView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = getIntent();
                        finish();
                        startActivity(intent);
                    }
                });
            }
            mAdapter.add(data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.clear();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(LOG_TAG, "OnResume called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(LOG_TAG, "OnPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(LOG_TAG, "OnStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(LOG_TAG, "OnDestroy called");
    }
}
