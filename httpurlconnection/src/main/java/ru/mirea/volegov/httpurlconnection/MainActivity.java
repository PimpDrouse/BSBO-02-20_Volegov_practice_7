package ru.mirea.volegov.httpurlconnection;

import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import ru.mirea.volegov.httpurlconnection.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        binding.connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkinfo = null;
                if (connectivityManager != null)
                {
                    networkinfo = connectivityManager.getActiveNetworkInfo();
                }
                if (networkinfo != null && networkinfo.isConnected())
                {
                    new DownloadPageTask().execute("https://ipinfo.io/json");
                } else
                {
                    Snackbar.make(MainActivity.this.getCurrentFocus(), "огтсутствует интернет", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String getDataFromURL(String address) throws IOException {
        InputStream inputStream = null;
        String data = "";
        try {
            URL url = new URL(address);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(100000);
            connection.setConnectTimeout(100000);
            connection.setRequestMethod("GET");
            connection.setInstanceFollowRedirects(true);
            connection.setUseCaches(false);
            connection.setDoInput(true);
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) { // 200 OK
                inputStream = connection.getInputStream();
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                int read = 0;
                while ((read = inputStream.read()) != -1) {
                    bos.write(read); }
                bos.close();
                data = bos.toString();
            } else {
                data = connection.getResponseMessage()+". Error Code: " + responseCode;
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return data;
    }
    private class DownloadPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            binding.connect.setText("Загружаем...");
        }
        @Override
        protected String doInBackground(String... urls) {
            try
            {
                JSONObject jSONObject = new JSONObject(getDataFromURL(urls[0]));
                String[] local = jSONObject.getString("loc").split(",");
                return getDataFromURL(String.format("https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current_weather=true", local[0], local[1]));

            }

            catch (IOException e)
            {
                e.printStackTrace();
                return "error";
            } catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
        }
        @Override
        protected void onPostExecute(String result) {
            JSONObject jsonObject = null;
            String temp;
            String wind;
            try
            {
                jsonObject = new JSONObject(result);
                JSONObject weather = new JSONObject(String.valueOf(jsonObject.getJSONObject("current_weather")));
                temp = weather.getString("temperature");
                wind = weather.getString("windspeed");
            }

            catch (JSONException e)
            {
                throw new RuntimeException(e);
            }
            binding.wind.setText(String.format("ветер -  %s", wind));
            binding.temp.setText(String.format("температура -  %s", temp));
            binding.connect.setText("получить прогноз");
            super.onPostExecute(result);
        }
    }

}