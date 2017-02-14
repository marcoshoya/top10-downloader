package com.marcoshoya.top10downloader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ListView listApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listApps = (ListView) findViewById(R.id.xmlListView);

        DownloadData data = new DownloadData();
        data.execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
    }

    /**
     * Download data class
     */
    private class DownloadData extends AsyncTask<String, Void, String> {

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d(TAG, "onPostExecute: content " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
            listApps.setAdapter(arrayAdapter);
        }

        @Override
        protected String doInBackground(String... params) {
            String feed = downloadXml(params[0]);
            if (feed == null) {
                Log.e(TAG, "doInBackground: Error downloading");
            }
            return feed;
        }

        private String downloadXml(String path) {
            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(path);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                int response = conn.getResponseCode();

                Log.d(TAG, "downloadXml: response code " + response);

                //InputStream inputStream = conn.getInputStream();
                //InputStreamReader streamReader = new InputStreamReader(inputStream);
                //BufferedReader reader = new BufferedReader(streamReader);

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                int charsRead;
                char[] inputBuffer = new char[500];
                while (true) {
                    charsRead = reader.read(inputBuffer);
                    if (charsRead < 0) {
                        break;
                    }
                    if (charsRead > 0) {
                        result.append(String.copyValueOf(inputBuffer, 0, charsRead));
                    }
                }

                reader.close();

                return result.toString();

            } catch (MalformedURLException e) {
                Log.e(TAG, "downloadXml: invalid url " + e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, "downloadXml: IO Exception " + e.getMessage());
            } catch (SecurityException e) {
                Log.e(TAG, "downloadXml: Security Exception " + e.getMessage());
            }

            return null;
        }
    }
}
