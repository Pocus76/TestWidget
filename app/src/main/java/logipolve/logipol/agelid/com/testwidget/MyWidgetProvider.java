package logipolve.logipol.agelid.com.testwidget;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyWidgetProvider extends AppWidgetProvider
{
    private String sharedPreferencesFile = "myPreferences";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        for (int i = 0 ; i < appWidgetIds.length ; i++)
        {

            SharedPreferences settings = context.getSharedPreferences(sharedPreferencesFile, 0);
            if (settings.contains("nextDate"))
            {
                try
                {
                    Constants.date = Constants.sdf.parse(settings.getString("nextDate", null));
                    Constants.textColor = settings.getInt("txtColor", -1);
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
            // On crée la hiérarchie sous la forme d'un RemotViews
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            // On récupère l'identifiant du widget actuel
            int id = appWidgetIds[i];
            // On met à jour toutes les vues du widget
            Date date = null;
            if (Constants.date!=null) date = Constants.date;
            else
            {
                try
                {
                    date = Constants.sdf.parse("13/02/2019");
                    Constants.date = date;
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
            views.setTextViewText(R.id.txt_nb_jours, String.valueOf(daysBetween(new Date(), date)));
            if (Constants.textColor != -1)
            {
                views.setTextColor(R.id.txt_nb_jours, Constants.textColor);
                views.setTextColor(R.id.txt_jours, Constants.textColor);
            }
            // Enregistrer un onClickListener
            Intent intent = new Intent(context, MyWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                                                                     0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            views.setOnClickPendingIntent(R.id.txt_nb_jours, pendingIntent);
            appWidgetManager.updateAppWidget(id, views);
        }
        int daysBetween = daysBetween(new Date(), Constants.date);
        if (daysBetween == 100) createNotification(daysBetween);
        else if (daysBetween == 75) createNotification(daysBetween);
        else if (daysBetween == 50) createNotification(daysBetween);
        else if (daysBetween == 25) createNotification(daysBetween);
        else if (daysBetween == 15) createNotification(daysBetween);
        else if (daysBetween == 10) createNotification(daysBetween);
        else if (daysBetween == 5) createNotification(daysBetween);
        else if (daysBetween == 3) createNotification(daysBetween);
        else if (daysBetween == 2) createNotification(daysBetween);
        else if (daysBetween == 1) createNotification(daysBetween);
        else if (daysBetween == 0) createNotification(daysBetween);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        super.onReceive(context, intent);
    }

    public int daysBetween(Date d1, Date d2)
    {
        if (Constants.sdf.format(d1).equals(Constants.sdf.format(d2))) return 0;
        else return (int)( (d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24))+1;
    }

    public void createNotification(int nbDays)
    {
        String title = "It's coming !";
        String content = "Only "+nbDays+" days left !";
        if (nbDays==1) content = "Gosh, it's tomorrow !";
        if (nbDays==0)
        {
            title = "Here it is !";
            content = "TODAAAAY !";
        }
        NotificationCompat.Builder mBuilder =   new NotificationCompat.Builder(Constants.context)
                .setSmallIcon(R.drawable.plane_icon) // notification icon
                .setContentTitle(title) // title for notification
                .setContentText(content) // message for notification
                .setAutoCancel(true); // clear notification after click
        Intent intent = new Intent(Constants.context, MyWidgetProvider.class);
        PendingIntent pi = PendingIntent.getActivity(Constants.context,0,intent,0);
        mBuilder.setContentIntent(pi);
        NotificationManager mNotificationManager =
                (NotificationManager) Constants.context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
