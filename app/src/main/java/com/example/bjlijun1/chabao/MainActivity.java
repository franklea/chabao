package com.example.bjlijun1.chabao;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {

    //Constants
    public static final String SERVER_ADDR = "http://10.234.128.144:8000/data/";
    public static final String LOGTAG = "=== chabao === ";
    private static final ArrayList<String> serverList = new ArrayList<>();
    private Button clickme;
    private ProgressBar bar;
    private TextView tv;
    private static final OkHttpClient client = new OkHttpClient();
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private String IP;
    private String location;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //serverlist
        serverList.add("https://www.hao123.com");
        serverList.add("http://www.163.com");
        serverList.add("http://www.sina.com");

        clickme = (Button) findViewById(R.id.button);
        bar = (ProgressBar) findViewById(R.id.progressBar);
        tv = (TextView) findViewById(R.id.result);


        clickme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyAsyncTask myAsyncTask =new MyAsyncTask(tv,bar);
                myAsyncTask.execute(serverList);
                clickme.setEnabled(false);

            }
        });
    }

    @SuppressWarnings("unchecked")
    private class MyAsyncTask extends AsyncTask<ArrayList<String>,Integer,String> {

        private ProgressBar bar;
        private TextView tv;

        public MyAsyncTask(TextView tv, ProgressBar bar){
            this.bar = bar;
            this.tv = tv;
        }

        /*
         * 当异步开始的时候触发
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
            Log.i(MainActivity.LOGTAG,"onPreExecute called");
            tv.setText("开始下载");
            super.onPreExecute();
        }

        /*
        * 当异步结束时触发此方法，其参数类型与第三个参数类型一致
        * (non-Javadoc)
        * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
        */
        @Override
        protected void onPostExecute(String result) {
            Log.i(MainActivity.LOGTAG,"onPostExecute called!");
            //tv.setText("数据采集完成");
            tv.setText(finalHint);
            clickme.setEnabled(true);
            super.onPostExecute(result);
        }

        /*
         * 正在处理的时候触发，与主UI线程交互，其参数与第二个参数一致
         * (non-Javadoc)
         * @see android.os.AsyncTask#onProgressUpdate(Progress[])
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            //第二个可变参数，由上面的publishProgress方法的参数决定
            Log.i(MainActivity.LOGTAG,"onProgressUpdate called!");
            bar.setProgress(values[0]);
            tv.setText(values[0]+"%");//可变参数就是这么用的，values[1]表示publishProgress的第二个参数
            super.onProgressUpdate(values);
        }


        String finalHint = "";
        @Override
        protected String doInBackground(ArrayList<String>... serverList) {
            Log.i(MainActivity.LOGTAG,"doInbackground called!");

            try{
                for (int i = 0 ; i < serverList[0].size(); i ++) {
                    Log.i(LOGTAG,serverList[0].get(i));
                    finalHint = getAndPost(serverList[0].get(i));
                }
            } catch (Exception e){
                e.printStackTrace();
            }

            return null;
        }


        private String getAndPost(String url) throws IOException {
            boolean flag = true;
            String hint = null;
            try {
                Connection conn = Jsoup.connect(url);
                Document doc = conn.get();

                // post to server
                RequestBody body = new FormBody.Builder().add("url",url).add("html",doc.toString()).build();
                Request request = new Request.Builder().url(SERVER_ADDR).post(body).build();
                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    StringBuilder entityStringBuilder = new StringBuilder();
                    InputStream inputStream = response.body().byteStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"),8*1024);
                    String line = null;
                    while((line = bufferedReader.readLine()) != null){
                        entityStringBuilder.append(line+"\n");
                    }

                    JSONObject result = new JSONObject(entityStringBuilder.toString());
                    String reString = result.getString("response");
                    Log.i(LOGTAG,reString);
                    hint = reString;
                }
                else {
                    flag = false;
                }

                //TODO: Add more infomation
                //.add(new BasicNameValuePair("ip",ip));
                //.add(new BasicNameValuePair("Android Version", version));


            } catch (Exception e) {
                e.printStackTrace();
            }
            if (flag == true) {
                return hint;
            } else {
                return "Something went wrong !";
            }
        }
    }
}
