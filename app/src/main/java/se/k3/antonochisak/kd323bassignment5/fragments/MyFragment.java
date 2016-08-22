package se.k3.antonochisak.kd323bassignment5.fragments;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.DocumentsContract;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.MutableData;
import com.firebase.client.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import se.k3.antonochisak.kd323bassignment5.R;
import se.k3.antonochisak.kd323bassignment5.adapters.MyAdapter;
import se.k3.antonochisak.kd323bassignment5.adapters.PopularMoviesAdapter;
import se.k3.antonochisak.kd323bassignment5.api.RestClient;
import se.k3.antonochisak.kd323bassignment5.api.model.ApiResponse;
import se.k3.antonochisak.kd323bassignment5.api.model.RootApiResponse;
import se.k3.antonochisak.kd323bassignment5.models.movie.Movie;

import static se.k3.antonochisak.kd323bassignment5.helpers.StaticHelpers.FIREBASE_CHILD;
import static se.k3.antonochisak.kd323bassignment5.helpers.StaticHelpers.FIREBASE_URL;

/**
 * Created by Manne on 2015-04-27.
 */

public class MyFragment extends MoviesFragment
        implements Callback <List<RootApiResponse>>, ListView.OnItemClickListener {

    ArrayList<Movie> mMovies;

    HashMap<String, Object> mMovieMap;

    CountDownTimer mVoteTimer;
    boolean mIsVoteTimerRunning = false;

    RestClient mRestClient;
    //hej

    Firebase mFireBase;
    Firebase mRef;
    String tes;

    String mCurrentClickedMovie = "";

    @InjectView(R.id.listView)
    ListView mMoviesGrid;

    @InjectView(R.id.progress_bar)
    ProgressBar mProgressBar;


    MyAdapter mAdapter;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMovies = new ArrayList<>();
        mMovieMap = new HashMap<>();

        mRestClient = new RestClient();
        mFireBase = new Firebase(FIREBASE_URL);
        mRef = mFireBase.child(FIREBASE_CHILD);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my, container, false);

        ButterKnife.inject(this, view);

        // Create adapter
        mAdapter = new MyAdapter(mMovies, getActivity().getLayoutInflater());
        mMoviesGrid.setAdapter(mAdapter);

        // listener= GridView.OnItemClickListener
        mMoviesGrid.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // listener = Callback<List<ApiResponse>>
        // go to http://docs.trakt.apiary.io/#introduction/extended-info, what should you include?
        mRestClient.getApiService().getTrending("images", this);
        mProgressBar.setVisibility(View.VISIBLE);
        initVoteTimer();
    }

    @Override
    void initVoteTimer() {
        mVoteTimer = new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                mIsVoteTimerRunning = false;
            }
        };
    }

    @Override
    void voteOnMovie(final int i) {

        Movie movie = mMovies.get(i);

        // Very important
        mCurrentClickedMovie = movie.getSlugline();

        mMovieMap.put("title", movie.getTitle());
        mMovieMap.put("year", movie.getYear());
        mMovieMap.put("slugline", movie.getSlugline());
        mMovieMap.put("poster", movie.getPoster());
        mMovieMap.put("fanart", movie.getFanArt());

        mRef.child(mCurrentClickedMovie).updateChildren(mMovieMap, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                Toast.makeText(getActivity(), "Gillade " + mMovies.get(i).getTitle(), Toast.LENGTH_SHORT).show();
                updateVotes();
            }
        });
    }

    @Override
    void updateVotes() {
        mRef.child(mCurrentClickedMovie + "/votes").runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if (mutableData.getValue() == null) {
                    mutableData.setValue(1);
                } else {
                    mutableData.setValue((Long) mutableData.getValue() + 1);
                }
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(FirebaseError firebaseError, boolean b, DataSnapshot dataSnapshot) {

            }
        });
    }

    @Override
    public void success(List<RootApiResponse> rootApiResponses, Response response) {

        mProgressBar.setVisibility(View.GONE);
        for (RootApiResponse r : rootApiResponses) {

            // Build a new movie-object for every response and add to list
            Movie movie = new Movie.Builder()
                    .title(r.apiResponse.title)
                    .slugLine(r.apiResponse.ids.getSlug())
                    .poster(r.apiResponse.image.getPoster().getThumbPoster())
                    .fanArt(r.apiResponse.image.getFanArt().getFullFanArt())
                    .year(r.apiResponse.year)
                    .build();

            mMovies.add(movie);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void failure(RetrofitError error) {
        if(error.getKind() == RetrofitError.Kind.NETWORK) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.retrofit_network_error),
                    Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!mIsVoteTimerRunning) {
            voteOnMovie(position);
            mVoteTimer.start();
            mIsVoteTimerRunning = true;
        }
    }
}
