package com.group11.wikia;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity
{
    private TextView txtWikiData;
    private ProgressBar progressBar;
    private EditText etxSearch;

    // Http Request Methods
    // http://www.restapitutorial.com/lessons/httpmethods.html

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressbar);
        etxSearch = findViewById(R.id.etxSearch);
        txtWikiData = findViewById(R.id.txtWikiData);

        Button btnFetchData = findViewById(R.id.btnFetchData);
        btnFetchData.setOnClickListener(v -> {
            String keyword = etxSearch.getText().toString();

            String WIKIPEDIA_URL = "https://en.wikipedia.org/w/api.php?action=query&titles=" +
        keyword +
                    "&prop=revisions&rvprop=content&format=json&prop=extracts";

            FetchWikiDataAsync fetchWikiDataASync = new FetchWikiDataAsync();
            fetchWikiDataASync.execute(WIKIPEDIA_URL);
        });
    }
    @SuppressLint("StaticFieldLeak")
    private class FetchWikiDataAsync extends AsyncTask<String, Void, String>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(MainActivity.this, "Fetching Data. Please wait.", Toast.LENGTH_SHORT).show();
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected String doInBackground(String[] params)
        {
            try
            {
                String sURL = params[0];

                URL url = new URL(sURL);        // Convert String URL to java.net.URL
                // Connection: to Wikipedia API
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder stringBuilder = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(line);
                }

                String wikiData = stringBuilder.toString();

                // Parse JSON Data

                return parseJSONData(wikiData);

            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String formattedData)
        {
            super.onPostExecute(formattedData);
            progressBar.setVisibility(View.GONE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            {
                // HTML Data
                txtWikiData.setText(Html.fromHtml
                        (formattedData,Html.FROM_HTML_MODE_LEGACY));
            }
            else
            {
                // HTML Data
                txtWikiData.setText(Html.fromHtml(formattedData));
            }
        }
    }

    private String parseJSONData(String wikiData)
    {
        try
        {
            // Convert String JSON (wikiData) to JSON Object
            JSONObject rootJSON = new JSONObject(wikiData);
            JSONObject query = rootJSON.getJSONObject("query");
            JSONObject pages = query.getJSONObject("pages");
            JSONObject number = pages.getJSONObject(pages.keys().next());

            return number.getString("extract");
        }
        catch (JSONException json)
        {
            json.printStackTrace();
        }

        return null;
    }

}

