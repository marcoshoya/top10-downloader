package com.marcoshoya.top10downloader;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private ListView listApps;
    private static final String TAG = "MainActivity";
    private String cachedUrl = "INVALIDATED";
    public static final String STATE_URL = "feedUrl";
    public static final String STATE_LIMIT = "feedLimit";
    private String feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml";
    private int limit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.listApps = (ListView) findViewById(R.id.xmlListView);

        if (savedInstanceState != null) {
            this.feedUrl = savedInstanceState.getString(STATE_URL);
            this.limit = savedInstanceState.getInt(STATE_LIMIT);
        }

        downloadData(String.format(this.feedUrl, this.limit));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.feeds_menu, menu);
        if (this.limit == 10) {
            menu.findItem(R.id.mnu10);
        } else {
            menu.findItem(R.id.mnu25).setChecked(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.mnuFree:
                this.feedUrl = this.feedUrl;
                break;
            case R.id.mnuPaid:
                this.feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml";
                break;
            case R.id.mnuSongs:
                this.feedUrl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml";
                break;
            case R.id.mnu10:
            case R.id.mnu25:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    this.limit = 35 - this.limit;
                }
                break;
            case R.id.mnuRefresh:
                cachedUrl = "INVALIDATED";
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        downloadData(String.format(this.feedUrl, this.limit));

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(STATE_URL, feedUrl);
        outState.putInt(STATE_LIMIT, limit);
        super.onSaveInstanceState(outState);
    }

    private void downloadData(String feedUrl) {
        if (!feedUrl.equalsIgnoreCase(cachedUrl)) {
            Log.d(TAG, "downloadData: url: " + feedUrl);
            DownloadData data = new DownloadData();
            data.execute(feedUrl);
            cachedUrl = feedUrl;
        }
    }

    /**
     * Download data class
     */
    private class DownloadData extends AsyncTask<String, Void, String> {

        private static final String TAG = "DownloadData";

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Log.d(TAG, "onPostExecute: content " + s);

            ParseApplications parseApplications = new ParseApplications();
            parseApplications.parse(s);

            //ArrayAdapter<FeedEntry> arrayAdapter = new ArrayAdapter<FeedEntry>(MainActivity.this, R.layout.list_item, parseApplications.getApplications());
            //listApps.setAdapter(arrayAdapter);

            FeedAdapter feedAdapter = new FeedAdapter(MainActivity.this, R.layout.list_record, parseApplications.getApplications());
            listApps.setAdapter(feedAdapter);
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

                //Log.d(TAG, "downloadXml: response code " + response);

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
