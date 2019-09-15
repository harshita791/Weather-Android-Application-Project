package com.example.myweather;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import Util.Utils;
import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderIterator;
import cz.msebera.android.httpclient.ProtocolVersion;
import cz.msebera.android.httpclient.RequestLine;
import cz.msebera.android.httpclient.client.methods.HttpUriRequest;
import cz.msebera.android.httpclient.params.HttpParams;
import data.CityPreference;
import data.JSONWeatherParser;
import data.WeatherHttpClient;
import model.Weather;

public class MainActivity extends AppCompatActivity {

    private TextView cityName, temp, description, humidity, pressure, wind, sunrise, sunset, updated;
    private ImageView iconView;
    Weather weather = new Weather();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cityName = findViewById(R.id.city);
        iconView = findViewById(R.id.thumbnailIcon);
        temp = findViewById(R.id.tempText);
        description = findViewById(R.id.cloudText);
        humidity = findViewById(R.id.humidText);
        pressure = findViewById(R.id.pressureText);
        wind = findViewById(R.id.windText);
        sunrise = findViewById(R.id.riseText);
        sunset = findViewById(R.id.setText);
        updated = findViewById(R.id.update);

        getSupportActionBar().setTitle("");
        CityPreference cityPreference = new CityPreference(MainActivity.this);

        renderWeatherData(cityPreference.getCity());
      /*  if(weather.currentCondition.getDescription().equals("clear sky"))
            im.setVisibility(View.VISIBLE);
        else if(weather.currentCondition.getDescription().equals("rain"))
            im1.setVisibility(View.VISIBLE);*/
    }

    public void renderWeatherData(String city) {

        WeatherTask weatherTask = new WeatherTask();

            weatherTask.execute(new String[]{city + "&units=metric&APPID=a7d8e8573366d08e4de21cdb5304a1c4"});

    }


    private class WeatherTask extends AsyncTask<String, Void, Weather> {

        @Override
        protected Weather doInBackground(String... strings) {
            String data = ((new WeatherHttpClient()).getWeatherData(strings[0]));
                weather.iconData = weather.currentCondition.getIcon();

                weather = JSONWeatherParser.getWeather(data);

           Log.v("Data: ", weather.currentCondition.getDescription());

            new DownloadImageAsyncTask().execute(weather.currentCondition.getIcon());
            return weather;
        }

        @Override
        protected void onPostExecute(Weather weather) {
            super.onPostExecute(weather);

            DateFormat df = DateFormat.getTimeInstance();

            long dvsunrise = Long.valueOf(weather.place.getSunrise()) * 1000;
            Date dfsunrise = new java.util.Date(dvsunrise);
            String sunriseDate = new SimpleDateFormat("hh:mm a").format(dfsunrise);

            long dvsunset = Long.valueOf(weather.place.getSunset()) * 1000;
            Date dfsunset = new java.util.Date(dvsunset);
            String sunsetDate = new SimpleDateFormat("hh:mm a").format(dfsunset);
            long dvupdate = Long.valueOf(weather.place.getLastupdate()) * 1000;
            Date dfupdate = new java.util.Date(dvupdate);
            String updateDate = new SimpleDateFormat("hh:mm a").format(dfupdate);


            //  String sunriseDate=df.format(new Date(weather.place.getSunrise()));
            //  String sunsetDate=df.format(new Date(weather.place.getSunset()));
            //String updateDate=df.format(new Date(weather.place.getLastupdate()));

            DecimalFormat decimalFormat = new DecimalFormat("#.#");

            String tempFormat = decimalFormat.format(weather.currentCondition.getTemperature());

            cityName.setText(weather.place.getCity() + "," + weather.place.getCountry());
            temp.setText("" + tempFormat + " °C");
            humidity.setText("Humidity: " + weather.currentCondition.getHumidity() + "%");
            pressure.setText("Pressure: " + weather.currentCondition.getPressure() + " hPa");
            wind.setText("Wind: " + weather.wind.getSpeed() + " mps");
            sunrise.setText("Sunrise: " + sunriseDate);
            sunset.setText("Sunset: " + sunsetDate);
            updated.setText("Last Updated: " + updateDate);
            description.setText("Codition: " + /*weather.currentCondition.getCondition()*/  weather.currentCondition.getDescription());
        }
    }

    private class DownloadImageAsyncTask extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... strings) {
            return downloadImage(strings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            iconView.setImageBitmap(bitmap);

        }

        private Bitmap downloadImage(String code) {
            final DefaultHttpClient client = new DefaultHttpClient();

          //  Toast.makeText(MainActivity.this, code, Toast.LENGTH_SHORT).show();
             final HttpGet getRequest = new HttpGet(Utils.ICON_URL + code + ".png");
         //   final org.apache.http.client.methods.HttpGet getRequest = new HttpGet("http://www.9ori.com/store/media/images/8ab579a656.jpg");

            try {
                HttpResponse response = client.execute(getRequest);
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != HttpStatus.SC_OK) {
                    Log.e("DownloadImage", "Error:" + statusCode);
                    return null;
                }
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    InputStream inputStream = null;
                    inputStream = entity.getContent();

                    //decode contents from stream
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }
    }
   /*  private Bitmap downloadImage(String code)
     {
         try {
             URL url = new URL(Utils.ICON_URL + code + ".png");
            // URL url=new URL("http://www.9ori.com/store/media/images/8ab579a656.jpg");
             Log.d("Data : ", url.toString());
             HttpURLConnection connection = (HttpURLConnection) url
                     .openConnection();
             connection.setDoInput(true);
             connection.connect();
             InputStream input = connection.getInputStream();
             Bitmap currentBitmap = BitmapFactory.decodeStream(input);
             return currentBitmap;
         }
         catch (IOException e)
         {
             e.printStackTrace();
             return null;
         }
     }
    }*/

        private void showInputDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Change City");

            final EditText cityInput = new EditText(MainActivity.this);
            cityInput.setInputType(InputType.TYPE_CLASS_TEXT);
            cityInput.setHint("Portland,US");
            builder.setView(cityInput);
            builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CityPreference cityPreference = new CityPreference(MainActivity.this);
                    cityPreference.setCity(cityInput.getText().toString());

                    String newCity = cityPreference.getCity();
                    renderWeatherData(newCity);
                }
            });
            builder.show();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            int id = item.getItemId();

            if (id == R.id.change_cityId) {
                showInputDialog();
            }
            return super.onOptionsItemSelected(item);
        }

}
