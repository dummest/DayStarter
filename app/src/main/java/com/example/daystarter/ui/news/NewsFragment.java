package com.example.daystarter.ui.news;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Context;
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

import com.example.daystarter.R;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsFragment extends Fragment {
    @BindView(R.id.new_recyclerview)
    RecyclerView recyclerView;
    private NewAdapter newAdapter;
    ArrayList<NewData> items = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v;
        v = inflater.inflate(R.layout.fragment_news, container, false);
        ButterKnife.bind(this, v);

        newAdapter = new NewAdapter(items,getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(newAdapter);

        readRss();

        return v;
    }

    //rss xml문서 가져와서 파싱
    void readRss() {
        Log.d("Rss", "startRSS");
        try {
            //url 주소, http은 보안성 사용x

            //URL url = new URL("https://rss.joins.com/sonagi/joins_sonagi_total_list.xml");
            URL url = new URL("https://rss.donga.com/total.xml");

            //네트워크 작업을 Thread 객체 생성해서 해결
            RssFeedTask task = new RssFeedTask();
            task.execute(url); //doInBackground()메소드가 발동[thread의 start()와 같은 역할]
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

            //해임달(URL)에게 무지개로드(Stream) 열도록..
            try {
                InputStream is = url.openStream();

                //파싱해주는 객체 생성
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser xpp = factory.newPullParser();

                //인코딩 방식(한글)
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
                                Log.d("description", "description "+xpp.getAttributeCount());
                                xpp.next();
                                Log.d("link", "link: " + xpp.getText());
                                if (item != null) item.setDesc(xpp.getText());
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
                                //읽어온 기사 한개를 대량의 데이터에 추가
                                items.add(item);
                                item = null;

                                //ui변경사항
                                publishProgress();

                            }
                            break;
                    }
                    eventType = xpp.next();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }
            //return 값은 onPostExecute에 들어간다
            return "파싱종료";
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            //ui 변경시 여기서 작업

            newAdapter.notifyItemInserted(items.size());//새로 추가한 것은 마지막에 추가하는 내용
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Toast.makeText(getContext(), s+"."+items.size(), Toast.LENGTH_SHORT).show();

        }
    }
}