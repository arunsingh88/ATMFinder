package com.thinktanki.atmfinder.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.view.ViewGroup.LayoutParams;

import com.thinktanki.atmfinder.R;

/**
 * Created by aruns512 on 19/12/2016.
 */

public class AndroidUtil {
    private Context context;
    private PopupWindow popupWindow;
    private Intent shareApp;

    public AndroidUtil(Context context) {
        this.context = context;
    }
    public void rateApp() {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }

    public void shareApp() {
        String SHAREAPP ="https://play.google.com/store/apps/details?id=" + context.getPackageName();
        shareApp = new Intent();
        shareApp.setAction(Intent.ACTION_SEND);
        shareApp.putExtra(Intent.EXTRA_TEXT,SHAREAPP);
        shareApp.setType("text/plain");
        // Verify that the intent will resolve to an activity
        if (shareApp.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(shareApp);
        }
    }

    /*Description about app in popup window*/
    public void aboutApp(LinearLayout linearLayout)
    {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View customView = inflater.inflate(R.layout.about_app_dialog,null);
        // Initialize a new instance of popup window
        popupWindow = new PopupWindow(
                customView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        ImageButton closeButton = (ImageButton) customView.findViewById(R.id.ib_close);
        TextView textView=(TextView)customView.findViewById(R.id.tv) ;
        textView.setText(Html.fromHtml(context.getResources().getString(R.string.about_app_desc)));
        // Set a click listener for the popup window close button
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Dismiss the popup window
                popupWindow.dismiss();
            }
        });
        // Closes the popup window when touch outside.
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        // Removes default background.
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(linearLayout, Gravity.CENTER,0,0);
    }
}
