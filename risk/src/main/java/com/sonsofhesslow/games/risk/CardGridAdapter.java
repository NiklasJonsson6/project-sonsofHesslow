package com.sonsofhesslow.games.risk;

/**
 * Created by fredr on 2016-05-24.
 */

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CardGridAdapter extends BaseAdapter{

    ArrayList<String> result;
    Context context;
    ArrayList<Integer> imageId;
    private static LayoutInflater inflater=null;
    ArrayList<Boolean> isClicked;
    ArrayList<View> rowView;

    public CardGridAdapter(MainActivity mainActivity, ArrayList<String> prgmNameList, ArrayList<Integer> prgmImages) {
        // TODO Auto-generated constructor stub
        result=prgmNameList;
        context=mainActivity;
        imageId=prgmImages;
        rowView = new ArrayList<View>();
        isClicked = new ArrayList<Boolean>();
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView tv;
        ImageView img;
        RelativeLayout rv;
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View temp = inflater.inflate(R.layout.activity_card, null);
        rowView.add(temp);
        isClicked.add(false);
        holder.tv=(TextView) rowView.get(position).findViewById(R.id.cardText);
        holder.img=(ImageView) rowView.get(position).findViewById(R.id.cardImage);
        System.out.println("Card in pos added: " + position);
        holder.tv.setText(result.get(position));
        holder.img.setImageResource(imageId.get(position));

        rowView.get(position).findViewById(R.id.cardMain).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Holder holder=new Holder();
                if(isClicked.get(position)){
                    holder.rv = (RelativeLayout) rowView.get(position).findViewById(R.id.frameColour);
                    holder.rv.setBackgroundColor(Color.parseColor("#7b7b7b"));
                    isClicked.set(position,false);
                } else {
                    holder.rv = (RelativeLayout) rowView.get(position).findViewById(R.id.frameColour);
                    holder.rv.setBackgroundColor(Color.parseColor("#a58218"));
                    isClicked.set(position,true);
                }
            }

        });

        return rowView.get(position);
    }

}
