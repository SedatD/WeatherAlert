package smarteq.weatheralert.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * Created by SD on 25.02.2017
 * :)
 */

public class JsonParse extends AsyncTask<Void ,Void, Void> {

    String sehir ="";
    String result = "";
    Weather weather = new Weather();

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            URL weather_url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+sehir+"&appid=5519df78a91952f50079565124888a76");
//HttpURLConnection weather_url_con = (HttpURLConnection) weather_url.openConnection();
//InputStreamReader inputStreamReader = new InputStreamReader(weather_url_con.getInputStream());
            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new InputStreamReader(weather_url.openStream()));
            String line = null;
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }
            bufferedReader.close();

            JSONObject jsonObject = new JSONObject(result);
            JSONArray jsonArray = jsonObject.getJSONArray("weather");
            JSONObject jsonObject_weather = jsonArray.getJSONObject(0);
            String result_main = jsonObject_weather.getString("main");
            String result_description = jsonObject_weather.getString("description");
            String result_icon = jsonObject_weather.getString("icon");

            JSONObject jsonObject_main = jsonObject.getJSONObject("main");
            Double temp = jsonObject_main.getDouble("temp");

            String result_city = jsonObject.getString("city");

            int result_temp = (int) (temp-273);

            URL icon_url = new URL("http://openweathermap.org/img/w/"+result_icon+".png");
            Bitmap bitImage = BitmapFactory.decodeStream(icon_url.openConnection().getInputStream());

            weather.setCity(result_city);
            weather.setAciklama(result_description);
            weather.setImage(bitImage);
            weather.setTemperature(result_temp);
            weather.setWeather(result_main);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }
}
