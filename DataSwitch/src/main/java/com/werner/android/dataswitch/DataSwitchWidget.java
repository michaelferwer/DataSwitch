package com.werner.android.dataswitch;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


/**
 * Implementation of App Widget functionality.
 */
public class DataSwitchWidget extends AppWidgetProvider {

  public static final String MY_EXTRAT_KEY = "SwitchButton";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i=0; i<N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
            int appWidgetId) {

      // Construct the RemoteViews object
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.data_switch_widget);

      //views.setTextViewText(R.id.appwidget_text, widgetText);
      Intent clickIntent = new Intent(context, DataSwitchWidget.class);
      clickIntent.putExtra(DataSwitchWidget.MY_EXTRAT_KEY, "Data");
      clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
      // Add random UUID in Data to have not recycled Intent
      clickIntent.setData(Uri.withAppendedPath(Uri.parse("myapp://widget/id/#togetituniqie" + appWidgetId), String.valueOf(UUID.randomUUID())));
      clickIntent.setAction("SYNC_CLICKED");

      PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent, 0);
      views.setOnClickPendingIntent(R.id.dataButton, pendingIntent);

      // Instruct the widget manager to update the widget
      appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
      super.onReceive(context, intent);

      if(intent.getExtras() != null) {
        for (String key : intent.getExtras().keySet()) {
          Log.d(String.valueOf(Log.DEBUG), "Extrat Key : " + key);
        }
      }
      if(intent.getExtras() != null && intent.getStringExtra(MY_EXTRAT_KEY) != null) {
        if(intent.getExtras().getString(MY_EXTRAT_KEY).equals("Data")){
          this.setMobileDataEnabled(context);
        }
      }
    }

  private void setMobileDataEnabled(Context context) {
    ConnectivityManager connectivityManager = (ConnectivityManager)  context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo info = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    Log.d(String.valueOf(Log.DEBUG),info.toString());

    if(info.isAvailable()) {
      try {
        final Class connectivityManagerClass = Class.forName(connectivityManager.getClass().getName());
        final Method setMobileDataEnabledMethod = connectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
        setMobileDataEnabledMethod.setAccessible(true);
        setMobileDataEnabledMethod.invoke(connectivityManager, !info.isConnected());
      } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
        Log.e(String.valueOf(Log.ERROR),e.getMessage());
      }
    }
  }
}


