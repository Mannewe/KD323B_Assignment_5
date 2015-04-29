package se.k3.antonochisak.kd323bassignment5.adapters;

import android.view.LayoutInflater;
import android.widget.BaseAdapter;

import java.util.ArrayList;

import se.k3.antonochisak.kd323bassignment5.models.movie.Movie;

/**
 * Created by Hans on 2015-04-29.
 */
public class TrendingMoviesAdapter extends BaseAdapter{

    ArrayList<Movie> mMovies;
    LayoutInflater mLayoutInflater;

    //kek


    public TrendingMoviesAdapter(ArrayList<Movie> mMovies) {
        this.mMovies = mMovies;
    }
}
