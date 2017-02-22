package com.marcoshoya.top10downloader;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.StringReader;
import java.util.ArrayList;

/**
 * Created by Marcos on 13/02/2017.
 */
public class ParseApplications {
    private static final String TAG = "ParseApplications";
    private ArrayList<FeedEntry> applications;

    public ParseApplications() {
        this.applications = new ArrayList<>();
    }

    public ArrayList<FeedEntry> getApplications() {
        return applications;
    }

    public boolean parse(String data) {
        boolean status = true;
        String value = "";
        FeedEntry record = null;
        boolean entry = false;

        try {

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);

            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(data));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                String tagName = xpp.getName();
                switch (eventType) {
                    case XmlPullParser.START_TAG:
                        //Log.d(TAG, "parse: Start tag " + tagName);
                        if ("entry".equalsIgnoreCase(tagName)) {
                            entry = true;
                            record = new FeedEntry();
                        }
                        break;
                    case XmlPullParser.TEXT:
                        value = xpp.getText();
                        break;
                    case XmlPullParser.END_TAG:
                        //Log.d(TAG, "parse: Ending tag: " + tagName);
                        if (entry) {
                            if ("entry".equalsIgnoreCase(tagName)) {
                                applications.add(record);
                                entry = false;
                            } else if ("name".equalsIgnoreCase(tagName)) {
                                record.setName(value);
                            } else if ("artist".equalsIgnoreCase(tagName)) {
                                record.setArtist(value);
                            } else if ("releaseDate".equalsIgnoreCase(tagName)) {
                                record.setReleaseDate(value);
                            } else if ("summary".equalsIgnoreCase(tagName)) {
                                record.setSummary(value);
                            } else if ("image".equalsIgnoreCase(tagName)) {
                                record.setImageURL(value);
                            }
                        }
                        break;

                    default:
                        // nothing to do
                }

                eventType = xpp.next();
            }

        } catch (Exception e) {
            status = false;
            Log.e(TAG, "parse: Error: " + e.getMessage());
        }

        return status;
    }
}
