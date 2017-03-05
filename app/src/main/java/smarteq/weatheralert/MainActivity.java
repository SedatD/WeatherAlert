package smarteq.weatheralert;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Calendar;

import smarteq.weatheralert.controller.AlarmReceiver;
import smarteq.weatheralert.controller.DBHelper;
import smarteq.weatheralert.controller.MyConstants;
import smarteq.weatheralert.view.MyDialogFragment;

public class MainActivity extends FragmentActivity {
    private static int timeHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    private static int timeMinute = Calendar.getInstance().get(Calendar.MINUTE);
    private static TextView textView2;
    protected Button buton;
    TextView textView1;
    AlarmManager alarmManager;
    TextView txt_Sehir, txt_Sicaklik, txt_Weather, txt_Aciklama;
    private EditText editText, editText2, editText3;
    private ImageView image;
    private String sehir;
    private DBHelper myDb;
    private Bitmap bitImage;
    private String ilk = "false";
    private PendingIntent pendingIntent;

    public static TextView getTextView2() {
        return textView2;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_Sehir = (TextView) findViewById(R.id.txt_sehir);
        txt_Aciklama = (TextView) findViewById(R.id.txt_aciklama);
        txt_Sicaklik = (TextView) findViewById(R.id.txt_sicaklik);
        txt_Weather = (TextView) findViewById(R.id.txt_weather);
        buton = (Button) findViewById(R.id.button);
        editText = (EditText) findViewById(R.id.editText);
        editText2 = (EditText) findViewById(R.id.editText2);
        editText3 = (EditText) findViewById(R.id.editText3);
        image = (ImageView) findViewById(R.id.imageView);
        myDb = new DBHelper(this);

        Cursor res = myDb.getAllData();
        if (res.getCount() == 0) {
            Toast.makeText(getApplicationContext(), "" + "Ayarları Girin !", Toast.LENGTH_SHORT).show();
            ilk = "true";
        } else {
            while (res.moveToNext()) {
                editText.setText(res.getString(0));
                editText2.setText(res.getString(1));
                editText3.setText(res.getString(2));
            }
        }

        if (editText.getText() != null) {
            sehir = String.valueOf(editText.getText());
            new JsonParse().execute();
        }

        buton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ilk.equalsIgnoreCase("true")) {
                    boolean isInserted = myDb.insertData(editText.getText().toString(), editText2.getText().toString(), editText3.getText().toString(), ilk);
                    if (isInserted)
                        Toast.makeText(MainActivity.this, "Tercihiniz Kaydedildi", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "Kayit Yapilamadi !", Toast.LENGTH_SHORT).show();
                } else {
                    boolean isUpdated = myDb.updateData(editText.getText().toString(), editText2.getText().toString(), editText3.getText().toString(), ilk);
                    if (isUpdated)
                        Toast.makeText(MainActivity.this, "Tercihiniz Guncellendi", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(MainActivity.this, "Guncelleme Yapilamadi !", Toast.LENGTH_SHORT).show();
                }

                sehir = String.valueOf(editText.getText());
                new JsonParse().execute();




                //Notification

                //Drawable d = new BitmapDrawable(getResources(), bitImage);
                //d to icon//icon zaten d

                //Icon aa = createWithBitmap(bitImage);
                //icon alıyor gozukuyordu artık icon almıyor

                NotificationManager notif = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Notification notify = new NotificationCompat.Builder
                        (getApplicationContext())
                        //.setLargeIcon(bitImage)
                        .setSmallIcon(R.drawable.google_logo)
                        //.setSmallIcon(image.getId())//calısması lazım calısmıyor

                        //.setOngoing(true)//bu iptal edilememesini sağlar
                        //.setAutoCancel(true)//bunu bilmiyorum

                        //.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))//once def sesine bak
                        //.setWhen(System.currentTimeMillis())//bak

                        .setContentTitle(txt_Sehir.getText().toString().trim())
                        .setContentText(txt_Sicaklik.getText().toString().trim() + "° //".trim() + " " + txt_Weather.getText().toString().trim())
                        .build();
                notify.flags |= Notification.FLAG_AUTO_CANCEL;
                notif.notify(0, notify);
            }
        });

        textView1 = (TextView) findViewById(R.id.msg1);
        textView1.setText(timeHour + ":" + timeMinute);
        textView2 = (TextView) findViewById(R.id.msg2);

        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent myIntent = new Intent(MainActivity.this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, myIntent, 0);

        View.OnClickListener listener1 = new View.OnClickListener() {
            public void onClick(View view) {
                textView2.setText("");
                Bundle bundle = new Bundle();
                bundle.putInt(MyConstants.HOUR, timeHour);
                bundle.putInt(MyConstants.MINUTE, timeMinute);
                MyDialogFragment fragment = new MyDialogFragment(new MyHandler());
                fragment.setArguments(bundle);
                FragmentManager manager = getSupportFragmentManager();
                FragmentTransaction transaction = manager.beginTransaction();
                transaction.add(fragment, MyConstants.TIME_PICKER);
                transaction.commit();
            }
        };

        Button btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(listener1);
        View.OnClickListener listener2 = new View.OnClickListener() {
            public void onClick(View view) {
                textView2.setText("");
                cancelAlarm();
            }
        };
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(listener2);
    }

    private void setAlarm() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timeHour);
        calendar.set(Calendar.MINUTE, timeMinute);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private void cancelAlarm() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }


    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle = msg.getData();
            timeHour = bundle.getInt(MyConstants.HOUR);
            timeMinute = bundle.getInt(MyConstants.MINUTE);
            textView1.setText(timeHour + ":" + timeMinute);
            setAlarm();
        }
    }

    protected class JsonParse extends AsyncTask<Void, Void, Void> {
        String result_main = "";
        String result_description = "";
        String result_icon = "";
        int result_temp;
        String result_city;

        @Override
        protected Void doInBackground(Void... params) {
            String result = "";
            try {
                URL weather_url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + sehir + "&appid=5519df78a91952f50079565124888a76");
//HttpURLConnection weather_url_con = (HttpURLConnection) weather_url.openConnection();
//InputStreamReader inputStreamReader = new InputStreamReader(weather_url_con.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(weather_url.openStream()));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    result += line;
                }
                bufferedReader.close();

                JSONObject jsonObject = new JSONObject(result);
                JSONArray jsonArray = jsonObject.getJSONArray("weather");
                JSONObject jsonObject_weather = jsonArray.getJSONObject(0);

                result_main = jsonObject_weather.getString("main");
                result_description = jsonObject_weather.getString("description");
                result_icon = jsonObject_weather.getString("icon");

                JSONObject jsonObject_main = jsonObject.getJSONObject("main");
                Double temp = jsonObject_main.getDouble("temp");

                result_city = jsonObject.getString("name");
                result_temp = (int) (temp - 273);

                URL icon_url = new URL("http://openweathermap.org/img/w/" + result_icon + ".png");
                bitImage = BitmapFactory.decodeStream(icon_url.openConnection().getInputStream());

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            txt_Sicaklik.setText(String.valueOf(result_temp));
            txt_Weather.setText(result_main);
            txt_Sehir.setText(result_city);
            txt_Aciklama.setText(result_description);
            image.setImageBitmap(bitImage);
            super.onPostExecute(aVoid);
        }
    }
}
