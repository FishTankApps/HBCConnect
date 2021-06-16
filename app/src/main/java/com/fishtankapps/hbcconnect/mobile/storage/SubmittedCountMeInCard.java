package com.fishtankapps.hbcconnect.mobile.storage;

import android.content.Context;
import android.text.Html;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.fishtankapps.hbcconnect.R;
import com.fishtankapps.hbcconnect.mobile.pageui.virtualCard.VirtualCardFragment;

import java.io.Serializable;

public class SubmittedCountMeInCard implements Serializable {

    private static final long serialVersionUID = -1930328475766475470L;
    private final String activity, name, phone, adults, children, comments;

    public SubmittedCountMeInCard(String activity, String name, String phone, String adults, String children, String comments) {
        this.activity = activity;
        this.name = name;
        this.phone = phone;
        this.adults = adults;
        this.children = children;
        this.comments = (comments.isEmpty()) ? "(none)" : comments;
    }

    public boolean isInputValid() {
        return !activity.isEmpty() && !name.isEmpty() && !phone.isEmpty() && !adults.isEmpty() && !children.isEmpty();
    }

    public String getHTMLEmail(){
        StringBuilder message = new StringBuilder("<div dir=3D\"ltr\"><font size=3D\"4\"><b>New Virtual Count Me In Card:</b></fon" +
                "t><div><font size=3D\"4\"><b><br></b></font><div><b>Name of Activity: </b>");

        message.append(activity);

        message.append("</div><div><b>Name:</b> ");
        message.append(name);

        message.append("</div><div><b>Phone:</b> ");
        message.append(phone);

        message.append("</div><div><br></div><div><b>Number of Attending:</b><br><b>Adults: </b>");
        message.append(adults);

        message.append(" <b>Children: </b>");
        message.append(children);

        message.append("</div><div><br></div><div><b>Comments:</b></div><div>");

        String[] split = comments.split("\n");

        for(String line : split)
            message.append(line).append("</div><div>");

        return message.toString();
    }

    public String getTextViewText(){
        StringBuilder message = new StringBuilder("<font size=3D\"4\"><b><br></b></font><div><b>Name of Activity: </b>");

        message.append(activity);

        message.append("</div><div><b>Name:</b> ");
        message.append(name);

        message.append("</div><div><b>Phone:</b> ");
        message.append(phone);

        message.append("</div><div><br></div><div><b>Number of Attending:</b><br><b>Adults: </b>");
        message.append(adults);

        message.append(" <b>Children: </b>");
        message.append(children);

        message.append("</div><div><br></div><div><b>Comments:</b></div><div>");

        String[] split = comments.split("\n");

        for(String line : split)
            message.append(line).append("</div><div>");

        return message.toString();
    }

    public TextView getTextViewDisplay(Context context, VirtualCardFragment virtualCardFragment){
        TextView textView = new TextView(context);
        textView.setText(Html.fromHtml(getTextViewText(), Html.FROM_HTML_MODE_COMPACT));

        textView.setTextColor(context.getResources().getColor(R.color.black, null));

        textView.setLongClickable(true);
        textView.setOnLongClickListener(l->removeCardFromHistory(context, virtualCardFragment));

        return textView;
    }

    private boolean removeCardFromHistory(Context context, VirtualCardFragment virtualCardFragment){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Are you sure you want to remove this submission from your history?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            DataFile.getDataFile(context).getSubmittedCountMeInCards().remove(this);
            virtualCardFragment.refreshPreviousSubmissions();
        });
        builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        builder.show();
        return true;
    }
}


