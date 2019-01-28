package logipolve.logipol.agelid.com.testwidget.comUtil;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Pocus on 23/01/2019.
 */

public class CommunicationApiTraduction extends AsyncTask<String, Void, String>
{
    private static final String TAG = "COM_API";
    private static final String API_URL = "https://translate.yandex.net/api/v1.5/tr.json/translate";
    private static final String API_ID = "trnsl.1.1.20190123T101503Z.32323acc4085c124.62ca6fe96ca575962491dcc4928abbc07a933c71";

    public AsyncResponse delegate = null;

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... strings)
    {
        InputStream inputStream = null;
        HttpURLConnection conn = null;

        try
        {
            URL url = new URL(API_URL+"?key="+API_ID+"&text="+strings[0]+"&lang=fr");
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int response = conn.getResponseCode();
            if (response != 200)
            {
                return null;
            }

            inputStream = conn.getInputStream();
            if (inputStream == null)
            {
                return null;
            }

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuffer buffer = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null)
            {
                buffer.append(line);
                buffer.append("\n");
            }

            return new String(buffer);
        }
        catch (IOException e)
        {
            return null;
        }
        finally
        {
            if (conn != null)
            {
                conn.disconnect();
            }
            if (inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException ignored) {}
            }
        }
    }

    @Override
    protected void onPostExecute(String s)
    {
        super.onPostExecute(s);
        if (s != null)
        {
            delegate.processTranslate(s);
        }
        else delegate.processTranslate(null);
    }
}
