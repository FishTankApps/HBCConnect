package com.fishtankapps.hbcconnect.dataStorage;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.Html;
import android.widget.TextView;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.activity.HBCConnectActivity;
import com.fishtankapps.hbcconnect.ui.virtualCard.VirtualCardFragment;

import java.io.Serializable;

public class SubmittedPrayerRequestCard implements Serializable {

    private static final long serialVersionUID = -1930328475766475470L;
    private final String name, phone, prayerRequest;

    public SubmittedPrayerRequestCard(String name, String phone, String prayerRequest) {
        this.name = name;
        this.phone = phone;
        this.prayerRequest = prayerRequest;
    }

    public boolean isInputValid() {
        return !name.isEmpty() && !phone.isEmpty() && !prayerRequest.isEmpty();
    }

    public String getHTMLEmail(){
        StringBuilder message = new StringBuilder("<div dir=3D\"ltr\"><font size=3D\"4\"><b>New Virtual Prayer Request Card:</b></fon" +
                "t><div><font size=3D\"4\"><b><br></b></font><div><b>Name of Activity: </b>");

        message.append("</div><div><b>Name:</b> ");
        message.append(name);

        message.append("</div><div><b>Phone:</b> ");
        message.append(phone);

        message.append("</div><div><br></div><div><b>Prayer Request:</b></div><div>");

        String[] split = prayerRequest.split("\n");

        for(String line : split)
            message.append(line).append("</div><div>");

        return message.toString();
    }

    public String getTextViewText(){
        StringBuilder message = new StringBuilder();

        message.append("</div><div><b>Name:</b> ");
        message.append(name);

        message.append("</div><div><br></div><div><b>Prayer Request:</b></div><div>");

        String[] split = prayerRequest.split("\n");

        for(String line : split)
            message.append(line).append("</div><div>");

        return message.toString();
    }

    public TextView getTextViewDisplay(Context context, VirtualCardFragment virtualCardFragment){
        TextView textView = new TextView(context);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(getTextViewText(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(getTextViewText()));
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            textView.setTextColor(context.getResources().getColor(R.color.black, null));
        }

        textView.setLongClickable(true);
        textView.setOnLongClickListener(l->removeCardFromHistory(context, virtualCardFragment));

        return textView;
    }

    private boolean removeCardFromHistory(Context context, VirtualCardFragment virtualCardFragment){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to remove this submission from your history?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            HBCConnectActivity.dataFile.getSubmittedPrayerRequestCards().remove(this);
            virtualCardFragment.refreshPreviousSubmissions();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        builder.show();
        return true;
    }
}


