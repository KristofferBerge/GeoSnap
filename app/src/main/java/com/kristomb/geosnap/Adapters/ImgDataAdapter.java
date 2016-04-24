package com.kristomb.geosnap.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kristomb.geosnap.Models.ImgData;
import com.kristomb.geosnap.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by krist on 07-Feb-16.
 */
public class ImgDataAdapter extends ArrayAdapter<ImgData> {

    ArrayList<ImgData> Resource;
    Context C = super.getContext();
    Resources res = C.getResources();

    public ImgDataAdapter(Context context, ArrayList<ImgData> resource) {
        super(context, -1, resource);
        Resource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(super.getContext().LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_img_data, parent, false);
        TextView userNameView = (TextView) rowView.findViewById(R.id.Username);
        TextView ageView = (TextView) rowView.findViewById(R.id.Age);
        ImageView icon = (ImageView) rowView.findViewById(R.id.SeenIcon);
        userNameView.setText(Resource.get(position).getUser());
        Date date = new Date();
        Timestamp now = new Timestamp(date.getTime());
        long diff = now.getTime() - Resource.get(position).getmTimestamp().getTime();

        ageView.setText(getAgeInText(diff));

        if(Resource.get(position).getSeenStatus()){
            icon.setImageResource(R.color.black_overlay);
        }
        else if(!Resource.get(position).getLoadedStatus()){
            icon.setImageResource(R.color.colorPrimaryDark);
        }

        return rowView;
    }
    //Reason for "weird" order: A image will most likely be several hours old. Days will probably never be used due to implemented max-age
    private String getAgeInText(long diff){
        String timeText = "";
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        if(hours > 0 && hours < 24){
            timeText += hours + " ";
            timeText += res.getQuantityString(R.plurals.hours, (int) hours);
            timeText += " " + C.getString(R.string.ago);
            return timeText;
        }
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        if(minutes > 0 && minutes < 60){
            timeText += minutes + " ";
            timeText += res.getQuantityString(R.plurals.minutes, (int) minutes);
            timeText += " " + C.getString(R.string.ago);
            return timeText;
        }
        long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
        if(seconds > 0 && seconds < 60){
            timeText += seconds + " ";
            timeText += res.getQuantityString(R.plurals.seconds, (int) seconds);
            timeText += " " + C.getString(R.string.ago);
            return timeText;
        }
        else{
            return TimeUnit.MILLISECONDS.toDays(diff)
                    + " " + res.getQuantityString(R.plurals.days, 1)
                    + " " + C.getString(R.string.ago);
        }
    }
}
