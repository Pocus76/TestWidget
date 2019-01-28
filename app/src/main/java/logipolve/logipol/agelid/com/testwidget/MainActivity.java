package logipolve.logipol.agelid.com.testwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import logipolve.logipol.agelid.com.testwidget.comUtil.AsyncResponse;
import logipolve.logipol.agelid.com.testwidget.comUtil.CommunicationApiTraduction;
import logipolve.logipol.agelid.com.testwidget.comUtil.CommunicationApiWeather;
import yuku.ambilwarna.AmbilWarnaDialog;

/**
 * Created by Agelid on 15/11/2018.
 */

public class MainActivity extends Activity implements AsyncResponse
{
    private static final String TAG = "MAIN";

    private TextView txtToday;
    private TextView txtNextDate;
    private TextView txtNbDays;
    private TextView txtDestination;
    private View viewColor;
    private Button btnChangeDate;
    private Button btnGetWeather;
    private LinearLayout layoutMeteo;
    private TextView txtVille;
    private TextView txtMeteo;
    private ImageView imgWeather;
    private TextView txtDescriptif;

    Calendar myCalendar = Calendar.getInstance();
    private String sharedPreferencesFile = "myPreferences";
    CommunicationApiWeather communique = new CommunicationApiWeather();
    CommunicationApiTraduction communiqueTrad = new CommunicationApiTraduction();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        txtToday = (TextView) findViewById(R.id.txt_today);
        txtNextDate = (TextView) findViewById(R.id.txt_next_date);
        txtNbDays = (TextView) findViewById(R.id.txt_nb_days);
        viewColor = findViewById(R.id.view_color);
        btnChangeDate = (Button) findViewById(R.id.btn_change_date);
        btnGetWeather = (Button) findViewById(R.id.btn_get_weather);
        layoutMeteo = (LinearLayout) findViewById(R.id.layout_meteo);
        txtVille = (TextView) findViewById(R.id.txt_ville);
        txtMeteo = (TextView) findViewById(R.id.txt_meteo);
        imgWeather = (ImageView) findViewById(R.id.img_weather);
        txtDescriptif = (TextView) findViewById(R.id.txt_descriptif);
        txtDestination = (TextView) findViewById(R.id.txt_destination);

        SharedPreferences settings = getSharedPreferences(sharedPreferencesFile, 0);
        if (settings.contains("nextDate"))
        {
            try
            {
                Constants.date = Constants.sdf.parse(settings.getString("nextDate", null));
                Constants.textColor = settings.getInt("txtColor", txtNbDays.getCurrentTextColor());
                if (settings.contains("ville")) Constants.ville = settings.getString("ville", null);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }

        txtToday.setText("Date du jour : "+Constants.sdf.format(new Date()));
        if (Constants.date!=null)
        {
            txtNextDate.setText("Date du voyage : "+Constants.sdf.format(Constants.date));
            txtNbDays.setText("Nombre de jours restants : "+String.valueOf(daysBetween(new Date(), Constants.date)));
        }

        if (Constants.ville!=null)
        {
            txtDestination.setText("Destination : "+Constants.ville);
            communique = new CommunicationApiWeather();
            communique.delegate = MainActivity.this;
            if (isConnected()) communique.execute(Constants.ville);
            else layoutMeteo.setVisibility(View.GONE);
        }

        Constants.context = getApplicationContext();
        if (Constants.textColor!=-1)
        {
            viewColor.setBackgroundColor(Constants.textColor);
        }


        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth)
            {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                Constants.date = myCalendar.getTime();
                updateLabel();
            }
        };

        viewColor.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int initialColor = txtNbDays.getCurrentTextColor();
                if (Constants.textColor!=-1)
                {
                     initialColor = Constants.textColor;
                }
                AmbilWarnaDialog dialog = new AmbilWarnaDialog(MainActivity.this, initialColor, new AmbilWarnaDialog.OnAmbilWarnaListener()
                {
                    @Override
                    public void onOk(AmbilWarnaDialog dialog, int color)
                    {
                        Constants.textColor = color;
                        viewColor.setBackgroundColor(color);
                        if (Constants.date!=null)
                        {
                            updateLabel();
                        }
                    }

                    @Override
                    public void onCancel(AmbilWarnaDialog dialog)
                    {
                        // cancel was selected by the user
                    }
                });
                dialog.show();
            }
        });


        btnChangeDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                new DatePickerDialog(MainActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                                     myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        btnGetWeather.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Renseignez une ville !");

                final EditText input = new EditText(MainActivity.this);
                if (Constants.ville!=null) input.setText(Constants.ville);
                input.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
                input.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                // --- On initialise les autocomplete text views
                builder.setView(input);
                builder.setCancelable(false);

                builder.setPositiveButton("Valider", new DialogInterface.OnClickListener()
                {
                    //--------------------------------------------------
                    //--------------------------------------------------
                    @Override
                    public void onClick(DialogInterface dialog, int which) {}
                });
                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        dialog.dismiss();
                    }
                });
                final AlertDialog dialog2 = builder.create();
                dialog2.show();
                input.requestFocus();
                dialog2.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                {
                    //--------------------------------------------------
                    //--------------------------------------------------
                    @Override
                    public void onClick(View v)
                    {
                        Boolean wantToCloseDialog;
                        if (input.getText().toString().trim().equals(""))
                        {
                            input.setError("Merci de renseigner une ville");
                            wantToCloseDialog = false;
                        }
                        else
                        {
                            communique = new CommunicationApiWeather();
                            communique.delegate = MainActivity.this;
                            if (isConnected()) communique.execute(input.getText().toString());
                            else
                            {
                                layoutMeteo.setVisibility(View.GONE);
                                Toast.makeText(MainActivity.this, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show();
                            }
                            wantToCloseDialog = true;
                        }
                        if (wantToCloseDialog) dialog2.dismiss();
                    }
                });
            }
        });
    }

    private void updateLabel()
    {
        txtNextDate.setText("Next date : "+Constants.sdf.format(myCalendar.getTime()));
        txtNbDays.setText("Number of days : "+String.valueOf(daysBetween(new Date(), myCalendar.getTime())));
        SharedPreferences settings = getSharedPreferences(sharedPreferencesFile, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("nextDate", Constants.sdf.format(Constants.date));
        editor.putInt("txtColor", Constants.textColor);
        editor.apply();
        if (Constants.textColor!=-1)
        {
            viewColor.setBackgroundColor(Constants.textColor);
        }
        Intent intent = new Intent(this, MyWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
        // since it seems the onUpdate() is only fired on that:
        int[] ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(new ComponentName(getApplication(), MyWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);
    }

    public int daysBetween(Date d1, Date d2)
    {
        if (Constants.sdf.format(d1).equals(Constants.sdf.format(d2))) return 0;
        else return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24))+1;
    }

    private boolean isConnected()
    {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    @Override
    public void processFinish(String output)
    {
        communique = new CommunicationApiWeather();
        if (output==null)
        {
            Toast.makeText(MainActivity.this, "Cette ville n'existe pas", Toast.LENGTH_SHORT).show();
            return;
        }
        try
        {
            JSONObject object = new JSONObject(output);
            //JSONObject list = object.getJSONObject("list");
            JSONArray weather = object.getJSONArray("weather");
            JSONObject objWeather = weather.getJSONObject(0);
            communiqueTrad = new CommunicationApiTraduction();
            communiqueTrad.delegate = MainActivity.this;
            if (isConnected()) communiqueTrad.execute(objWeather.getString("main"));
            else txtMeteo.setText(objWeather.getString("main"));
            JSONObject jMain = object.getJSONObject("main");
            txtDescriptif.setText(objWeather.getString("description")+" "+jMain.getInt("temp")+"°C");
            //txtDescriptif.setText(output);
            if (objWeather.getString("main").toLowerCase().equals("snow"))
            {
                imgWeather.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.snowflake));
            }
            else if (objWeather.getString("main").toLowerCase().equals("clouds"))
            {
                imgWeather.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.cloud));
            }
            else if (objWeather.getString("main").toLowerCase().equals("rain"))
            {
                imgWeather.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.drop));
            }
            else if (objWeather.getString("main").toLowerCase().equals("clear"))
            {
                imgWeather.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.sun));
            }
            else imgWeather.setImageDrawable(ContextCompat.getDrawable(MainActivity.this, R.drawable.all));
            txtVille.setText(object.optString("name"));
            SharedPreferences settings = getSharedPreferences(sharedPreferencesFile, 0);
            SharedPreferences.Editor editor = settings.edit();
            Constants.ville = object.optString("name");
            txtDestination.setText("Destination : "+Constants.ville);
            editor.putString("ville", Constants.ville);
            editor.apply();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void processTranslate(String output)
    {
        communiqueTrad = new CommunicationApiTraduction();
        if (output!=null)
        {
            String txtTraduit = "";
            try
            {
                JSONObject jOutput = new JSONObject(output);
                JSONArray jArrayTrad = jOutput.getJSONArray("text");
                txtTraduit = jArrayTrad.getString(0);
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
            txtMeteo.setText(txtTraduit);
        }
    }
}
