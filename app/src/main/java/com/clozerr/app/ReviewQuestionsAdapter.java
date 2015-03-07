package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.Session;

import java.util.List;


/**
 * Created by Aravind on 19/1/14.
 * Adapter used in Questions
 */
public class ReviewQuestionsAdapter extends RecyclerView.Adapter<ReviewQuestionsAdapter.ListItemViewHolder>{

    private List<String> items;
    static Context c;
    static List<Integer> stars;

    ReviewQuestionsAdapter(List<String> modelData, Context c) {
        if (modelData == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.items = modelData;
        this.c = c;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.dialog_reviews_layout,
                        viewGroup,
                        false);
        return new ListItemViewHolder(itemView);
    }



    @Override
    public void onBindViewHolder(final ListItemViewHolder viewHolder, int position) {
        viewHolder.currentItem = items.get(position);

        viewHolder.star1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.star1.setImageResource(R.drawable.star_glow);
                viewHolder.star2.setImageResource(R.drawable.star_outline);
                viewHolder.star3.setImageResource(R.drawable.star_outline);
                viewHolder.star4.setImageResource(R.drawable.star_outline);
                viewHolder.star5.setImageResource(R.drawable.star_outline);
                //stars.add(viewHolder.getPosition(),1);
            }
        });
        viewHolder.star2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.star1.setImageResource(R.drawable.star_glow);
                viewHolder.star2.setImageResource(R.drawable.star_glow);
                viewHolder.star3.setImageResource(R.drawable.star_outline);
                viewHolder.star4.setImageResource(R.drawable.star_outline);
                viewHolder.star5.setImageResource(R.drawable.star_outline);
                //stars.add(viewHolder.getPosition(),2);
            }
        });
        viewHolder.star3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.star1.setImageResource(R.drawable.star_glow);
                viewHolder.star2.setImageResource(R.drawable.star_glow);
                viewHolder.star3.setImageResource(R.drawable.star_glow);
                viewHolder.star4.setImageResource(R.drawable.star_outline);
                viewHolder.star5.setImageResource(R.drawable.star_outline);
                //stars.add(viewHolder.getPosition(),3);
            }
        });
        viewHolder.star4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.star1.setImageResource(R.drawable.star_glow);
                viewHolder.star2.setImageResource(R.drawable.star_glow);
                viewHolder.star3.setImageResource(R.drawable.star_glow);
                viewHolder.star4.setImageResource(R.drawable.star_glow);
                viewHolder.star5.setImageResource(R.drawable.star_outline);
                //stars.add(viewHolder.getPosition(),4);
            }
        });
        viewHolder.star5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewHolder.star1.setImageResource(R.drawable.star_glow);
                viewHolder.star2.setImageResource(R.drawable.star_glow);
                viewHolder.star3.setImageResource(R.drawable.star_glow);
                viewHolder.star4.setImageResource(R.drawable.star_glow);
                viewHolder.star5.setImageResource(R.drawable.star_glow);
                //stars.add(viewHolder.getPosition(),5);
            }
        });
            }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int getStarCount(int position) {
        return stars.get(position);
    }

    public final static class ListItemViewHolder
            extends RecyclerView.ViewHolder {


        //TextView txtrating;
        public String currentItem;
        TextView txtQuestion;
        ImageView star1,star2,star3,star4,star5;

        public ListItemViewHolder(final View itemView) {
            super(itemView);

            txtQuestion = (TextView) itemView.findViewById(R.id.textView_question);
            star1 = (ImageView) itemView.findViewById(R.id.star1);
            star2 = (ImageView) itemView.findViewById(R.id.star2);
            star3 = (ImageView) itemView.findViewById(R.id.star3);
            star4 = (ImageView) itemView.findViewById(R.id.star4);
            star5 = (ImageView) itemView.findViewById(R.id.star5);
        }
        //suggest rest
        //border -- lines
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        SharedPreferences example = c.getSharedPreferences("USER", 0);
                        SharedPreferences.Editor editor = example.edit();
                        editor.putString("notNow", "false");
                        editor.apply();
                        //Yes button clicked
                           /* Intent mStartActivity = new Intent(c,Login.class);
                            int mPendingIntentId = 123456;
                            PendingIntent mPendingIntent = PendingIntent.getActivity(c, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                            AlarmManager mgr = (AlarmManager)c.getSystemService(Context.ALARM_SERVICE);
                            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                            System.exit(0);*/
                        // Toast.makeText(c, "Logging out", Toast.LENGTH_SHORT).show();

                        Session session = Session.getActiveSession();
                        if (session != null) {
                            if (!session.isClosed()) {
                                session.closeAndClearTokenInformation();
                            }
                        } else {
                            session = new Session(c);
                            Session.setActiveSession(session);
                            session.closeAndClearTokenInformation();
                        }

                        c.startActivity(new Intent(c, Login.class));

                        if(c instanceof Activity)
                            ((Activity)c).finish();
                        else
                            Toast.makeText(c,"Error", Toast.LENGTH_SHORT);
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        //Toast.makeText(Home.this, "Thanks for staying. You are awesome.", Toast.LENGTH_SHORT).show();
                        //No button clicked
                        break;
                }
            }
        };
    }


}