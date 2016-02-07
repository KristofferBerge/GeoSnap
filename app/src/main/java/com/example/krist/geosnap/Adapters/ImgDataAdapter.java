package com.example.krist.geosnap.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.krist.geosnap.Models.ImgData;
import com.example.krist.geosnap.R;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Created by krist on 07-Feb-16.
 */
public class ImgDataAdapter extends ArrayAdapter<ImgData> {

    ArrayList<ImgData> Resource;

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
        System.out.println(diff);
        String text = "";
        //TODO: Should probably be own function
        if(TimeUnit.MILLISECONDS.toMinutes(diff) < 59){
            text = TimeUnit.MILLISECONDS.toMinutes(diff)
                    + " " + super.getContext().getString(R.string.minutes)
                    + " " + super.getContext().getString(R.string.ago);
            System.out.println("IS MINUTES");
        }
        else if(TimeUnit.MILLISECONDS.toHours(diff) < 23){
            text = TimeUnit.MILLISECONDS.toHours(diff)
                            + " " + super.getContext().getString(R.string.hours)
                            + " " + super.getContext().getString(R.string.ago);
            System.out.println("IS HOURS");
        }
        else{
            text = TimeUnit.MILLISECONDS.toDays(diff)
                            + " " + super.getContext().getString(R.string.days)
                            + " " + super.getContext().getString(R.string.ago);
            System.out.println("IS" + TimeUnit.MILLISECONDS.toDays(diff) + "DAYS");

        }
        ageView.setText(text);
        return rowView;
    }
}
