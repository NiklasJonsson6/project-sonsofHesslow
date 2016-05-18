package com.sonsofhesslow.games.risk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sonsofhesslow.games.risk.MainActivity;
import com.sonsofhesslow.games.risk.R;

public class CustomAdapter extends BaseAdapter{
    String[] playerName;
    String[] armyCount;
    Context context;
    int [] imageId;
    private static LayoutInflater inflater=null;
    public CustomAdapter(MainActivity mainActivity, String[] playerName, int[] playerImage, String[] armyCount) {
        // TODO Auto-generated constructor stub
        this.playerName=playerName;
        this.armyCount=armyCount;
        context=mainActivity;
        imageId=playerImage;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return playerName.length;
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
    }
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        Holder holder=new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.activity_playerinfo, null);
        holder.tv=(TextView) rowView.findViewById(R.id.playerName);
        holder.img=(ImageView) rowView.findViewById(R.id.playerImage);
        holder.tv.setText(playerName[position]);
        holder.tv=(TextView) rowView.findViewById(R.id.armiesToPlace);
        holder.tv.setText(armyCount[position]);
        //holder.img.setImageResource(imageId[position]);
        rowView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(context, "You Clicked "+playerName[position], Toast.LENGTH_LONG).show();
            }
        });
        return rowView;
    }

} 