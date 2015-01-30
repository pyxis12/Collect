package shopping.with.friends.Login;


/**
 * Imports of all widgets and Activities, etc.
 * All library imports and other activities will be here
 */
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import shopping.with.friends.MainActivity;
import shopping.with.friends.R; // <-- This file is sooooo important. I will be explaining more in the future

/**
 * Created by Ryan Brooks on 1/24/15.
 *
 * Make sure all activities extend Activity. I'll teach y'all
 * about other activities and Fragments later when they come.
 */
public class LoginActivity extends Activity {

    /**
     * Declarations
     */
    private EditText usernameET, passwordET;
    private Button loginButton, registerButton;
    private HttpClient client;
    private HttpPost post;
    private JSONObject loginObj;
    private boolean loginSuccessful;

    /**
     * onCreate is basically the main method for the application
     * It always needs to be included. It is the second method
     * called after onStart (which we probably won't worry about)
     * and is before onResume (which is called after sending an
     * activity to the background).
     *
     * setContentView ties the layout you choose to the activity.
     *
     * find all views in here using findViewById(<YOUR ID HERE>)!
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // <-- Very important line. You must include this. Basically it sets the view to any layout you specify
        getActionBar().hide(); // Hiding actionbar

        usernameET = (EditText) findViewById(R.id.al_username_et); // Finding username "form" aka EditText
        passwordET = (EditText) findViewById(R.id.al_password_et); // Finding password EditText

        loginButton = (Button) findViewById(R.id.al_login_button); // Finding login button
        registerButton = (Button) findViewById(R.id.al_register_button); // Finding register button


        /**
         * This is a click listener. Whenever a button is clickable,
         * this method needs to be implemented. Anything inside onClick
         * will be run after a click. In this case, it checks to make
         * sure all forms are filled so no null or empty data is sent
         * to the api. If all is good, it sends an Http Post located below.
         */
        loginButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // If username or password fields don't have text in them, let the user know :
                if (usernameET.getText().toString().trim().equals("") || usernameET.getText().toString().trim().equals(null)) {
                    Toast.makeText(LoginActivity.this, "You didn't enter a username!", Toast.LENGTH_SHORT).show();
                } else if (passwordET.getText().toString().trim().equals("") || passwordET.getText().toString().trim().equals(null)) {
                    Toast.makeText(LoginActivity.this, "You didn't enter a password!", Toast.LENGTH_SHORT).show();
                } else {
                    //Set all buttons disabled to prevent multiple posts from firing at the same time. This can easily crash an app.
                    loginButton.setEnabled(false);
                    usernameET.setEnabled(false);
                    passwordET.setEnabled(false);
                    // Create the ASyncTask that will run at the same time as the activity but in the background (see below)
                    new HttpAsyncTask().execute("http://128.61.72.151:5000/api/checkUser"); // TODO: Change to server URL
                }
            }
        });

        /**
         * Another onClickListener, but for the register button. Inside this
         * is an intent. An intent basically allows users to switch between
         * activities. Data can also be passed between activities, but this won't
         * come until later.
         */
        registerButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent registerActivity = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(registerActivity); // Starts the RegisterActivity, but this activity is paused in the background
            }
        });
    }

    /**
     * Post Method that creates HTTP POST and sends it to the
     * specified URL. This is called in the doInBackground method
     * of the ASyncTask just in case it takes a little while to connect
     * and receive a response.
     */
    public static String POST(String url, String username, String password){

        // Declarations
        InputStream inputStream;
        String result = "";

        try {
            // Create HttpClient to send Http requests
            HttpClient httpclient = new DefaultHttpClient();

            // Make POST request to the passed URL
            HttpPost httpPost = new HttpPost(url);

            // Attach items as POST variables
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("username", username));
            nameValuePairs.add(new BasicNameValuePair("password", password));

            // Make sure httpPost has the POST variables attached
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            // Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            // Receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // Convert inputStream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!"; // Error
            }
        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage()); // Error
        }

        return result;
    }

    /**
     * This is an ASyncTask. Basically, it is a Thread that is run at the
     * same time as the activity without hurting the UI. In this case, we
     * are running it to send an HTTP POST. We do this because the delay
     * of obtaining a connection to the api and activity can sometimes
     * take a long time and will crash the app if not handled correctly.
     *
     * Proper implementation here would be to set some sort of timeout
     * after about 10 seconds or not receiving a response, but for our
     * demo purposes, it should be fine.
     *
     * TODO: Add timeout
     */
    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        /**
         * Does any prep before doInBackground is called. In this case,
         * in the future it will activate a progress indicator so the
         * user is not left hanging about their login status.
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Takes in params from the creation of the ASyncTask. This is
         * the most important part of the ASyncTask and the background
         * thread will run until all inside functions, methods, etc. are
         * fully executed. After this will be onPostExecute
         */
        @Override
        protected String doInBackground(String... urls) {

            String userNameToPost = usernameET.getText().toString().trim();

            //TODO: Encrypt Password
            String passwordToPost = passwordET.getText().toString().trim();

            Log.d("username", userNameToPost);
            Log.d("password", passwordToPost);


            return POST(urls[0], userNameToPost, passwordToPost);
        }

        /**
         * onPostExecute displays the results of the AsyncTask. In this case,
         * we will take care of the results of the HTTP POST
         */
        @Override
        protected void onPostExecute(String result) {
            try {
                readJSONResponse(result);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This is a copied method, I didn't write it. It just converts the input stream in
     * the POST method to a readable string, in JSON format.
     */
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;
    }

    /**
     * This method is pretty simple and Android has a lot of built in functions to help
     * with this. It stores the result as a JSON object where you can easily parse it
     * into usable data.
     */
    private void readJSONResponse(String result) throws JSONException {
        Log.d("JSON", result);
        JSONObject mainObject = new JSONObject(result);
        // JSON response should look like: { correct: true/false }
        loginSuccessful = mainObject.getBoolean("correct"); // Cast correct variable as boolean

        if(loginSuccessful)
        {
            Intent mainActivity = new Intent(LoginActivity.this, MainActivity.class); // Successful login, starts main
            startActivity(mainActivity);
            Toast.makeText(getBaseContext(), "Login Successful!", Toast.LENGTH_SHORT).show(); // Shows a quick message to the user saying it worked.
            finish(); // !! VERY IMPORTANT !! only call this if you're for sure done with an activity for the rest of the app. Any new instance will be brand new
        }
        else {
            // Incorrect login, tell user, re-enable all buttons
            Toast.makeText(getBaseContext(), "Username or Password incorrect! Please try again.", Toast.LENGTH_SHORT).show();
            loginButton.setEnabled(true);
            usernameET.setEnabled(true);
            passwordET.setEnabled(true);
        }
    }
}