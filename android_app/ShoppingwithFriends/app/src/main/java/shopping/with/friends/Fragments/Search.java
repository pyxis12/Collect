package shopping.with.friends.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.gson.JsonObject;
import com.squareup.okhttp.Response;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import shopping.with.friends.Adapters.UserListviewAdapter;
import shopping.with.friends.Api.ApiInterface;
import shopping.with.friends.Objects.Profile;
import shopping.with.friends.R;

/**
 * Created by Ryan Brooks on 2/19/15.
 */
public class Search extends Fragment {

    private Button searchButton;
    private ListView userListView;
    private ArrayList<Profile> userList;


    public Search() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        userList = new ArrayList<>();

        userListView = (ListView) view.findViewById(R.id.sf_search_listview);
        searchButton = (Button) view.findViewById(R.id.sf_all_users_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RestAdapter restAdapter = new RestAdapter.Builder()
                        .setEndpoint("http://" + getString(R.string.server_address))
                        .build();

                ApiInterface apiInterface = restAdapter.create(ApiInterface.class);
                apiInterface.getAllUsers(new Callback<JsonObject>() {
                    @Override
                    public void success(JsonObject jsonObject, retrofit.client.Response response) {
                        Log.d("Json Object", jsonObject.toString());
                        try {
                            JSONObject mainObject = new JSONObject(jsonObject.toString());
                            JSONArray userArray = mainObject.getJSONArray("users");
                            for (int i = 0; i < userArray.length(); i++) {
                                JSONObject user = userArray.getJSONObject(i);

                                Profile profile = new Profile();
                                profile.setId(user.getString("_id"));
                                profile.setEmail(user.getString("email"));
                                profile.setPassword(user.getString("password"));
                                profile.setUsername(user.getString("username"));
                                profile.setName(user.getString("name"));

                                userList.add(profile);
                            }
                            UserListviewAdapter ulvw = new UserListviewAdapter(getActivity().getApplicationContext(), userList);
                            userListView.setAdapter(ulvw);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("Failure", error.getMessage());
                    }
                });
            }
        });
        return view;
    }
}