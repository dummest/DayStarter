package com.example.daystarter.ui.news;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.daystarter.R;
import com.example.daystarter.ui.weather.ProgressDialog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFragment extends Fragment {
    @BindView(R.id.new_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.swiper_layout) SwipeRefreshLayout swipeRefreshLayout;
    private NewAdapter newAdapter;
    ArrayList<NewData> items = new ArrayList<>();
    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, v);

        newAdapter = new NewAdapter(items,getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(newAdapter);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        progressDialog.show();
        readRss();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                items.clear();
                newAdapter.notifyDataSetChanged();
                readRss();
            }
        });
        return v;
    }

    //rss xml?????? ???????????? ??????
    void readRss() {
        Log.d("Rss", "startRSS");
        try {
            //url ??????, http??? ????????? ??????x

            //URL url = new URL("https://rss.joins.com/sonagi/joins_sonagi_total_list.xml");
            URL url = new URL("https://rss.donga.com/total.xml");

            //???????????? ????????? Thread ?????? ???????????? ??????
            RssFeedTask task = new RssFeedTask();
            task.execute(url); //doInBackground()???????????? ??????[thread??? start()??? ?????? ??????]
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    //Thread class
    class RssFeedTask extends AsyncTask<URL, Void, String> {

        @Override
        protected String doInBackground(URL... urls) {
            Log.d("Thread", "thread");
            URL url = urls[0];

            //?????????(URL)?????? ???????????????(Stream) ?????????..
            try {
                InputStream is = url.openStream();

                //??????????????? ?????? ??????
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                //????????? ??????(??????)
                xpp.setInput(is, "utf-8");
                int eventType = xpp.getEventType();

                NewData item = null;
                String tagName = null;

                while (eventType != XmlPullParser.END_DOCUMENT) {
                    switch (eventType) {
                        case XmlPullParser.START_DOCUMENT:
                            break;
                        case XmlPullParser.START_TAG:
                            tagName = xpp.getName();
                            Log.d("TAG", tagName);

                            if (tagName.equals("item")) {
                                item = new NewData();
                            } else if (tagName.equals("title")) {
                                xpp.next();
                                if (item != null) item.setTitle(xpp.getText());
                            } else if (tagName.equals("link")) {
                                xpp.next();
                                if (item != null) item.setLink(xpp.getText());
                            } else if (tagName.equals("description")) {
                                xpp.next();
                                Log.d("description", "doInBackground: "+xpp.getLineNumber());
                                Log.d("descripition", "description: ");
                                if (item != null) {
                                    String a=xpp.getText();
                                    String[]splitText=a.split(">");
                                    for(int i=0; i<splitText.length;i++){
                                        Log.d("split", "split: ");
                                        item.setDesc(splitText[i]);
                                    }
                                    //item.setDesc(xpp.getText());
                                }
                            } else if (tagName.equals("media:content")) {
                                Log.d("0", "attributeCount: " + xpp.getAttributeCount());
                                xpp.getAttributeValue(null,"url");
                                xpp.next();
                                if (item != null) item.setImgUrl(xpp.getAttributeValue(null,"url"));
                                Log.d("image", "image: " + xpp.getText());
                            } else if (tagName.equals("pubDate")) {
                                xpp.next();
                                if (item != null) item.setDate(xpp.getText());
                            }
                            break;
                        case XmlPullParser.TEXT:
                            break;
                        case XmlPullParser.END_TAG:
                            tagName = xpp.getName();
                            if (tagName.equals("item")) {
                                Log.d("RSS", item.getTitle());
                                //????????? ?????? ????????? ????????? ???????????? ??????
                                items.add(item);
                                item = null;

                                //ui????????????
                                publishProgress();

                            }
                            break;
                    }
                    eventType=xpp.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            //return ?????? onPostExecute??? ????????????
            progressDialog.dismiss();
            return "????????????";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //ui ????????? ????????? ??????

            newAdapter.notifyItemInserted(items.size());//?????? ????????? ?????? ???????????? ???????????? ??????
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(getContext(), s+"."+items.size(), Toast.LENGTH_SHORT).show();

        }
    }
}