package xyz.devdiscovery.examplexml;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> mTitles = new ArrayList<>();
    private String bestUrl = "https://www.andr-discovery.xyz/category.xml";
    private ListView mListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);
    }

    private class DownloadPageTask extends
            AsyncTask<String, Void, List<String>> {

        @Override
        protected ArrayList<String> doInBackground(String... urls) {
            try {
                return downloadOneUrl(urls[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mTitles;
        }

        @Override
        protected void onPostExecute(List<String> result) {
            mListView.setAdapter(new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_list_item_1, result));
            super.onPostExecute(result);
        }
    }

    private ArrayList<String> downloadOneUrl(String myurl) throws
            Exception {
        InputStream inputStream = null;

        try {
            URL url = new URL(myurl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputStream = connection.getInputStream();

                InputSource inputSource = new InputSource(inputStream);
                // Создаем экземпляр XPath
                XPath xpath = XPathFactory.newInstance().newXPath();
                // задаем выражение для разбора
                String expression = "//title";
                // список полученных узлов
                NodeList nodes = (NodeList) xpath.evaluate(expression,
                        inputSource, XPathConstants.NODESET);


                // если узел найден
                if (nodes != null && nodes.getLength() > 0) {
                    mTitles.clear();
                    int nodesLength = nodes.getLength();
                    for (int i = 0; i < nodesLength; ++i) {
                        // формируем списочный массив
                        Node node = nodes.item(i);
                        mTitles.add(node.getTextContent());
                    }
                }
            } else {
                String data = connection.getResponseMessage()
                        + " . Error Code : " + responseCode;
            }
            connection.disconnect();
            // return data;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return mTitles;
    }

    public void onClick(View view) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadPageTask().execute(bestUrl);
        } else {
            Toast.makeText(this, "Нет интернета", Toast.LENGTH_SHORT).show();
        }
    }
}
