package com.clozerr.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MyStampsFragment extends Fragment {

    Context c;
    FrameLayout layout;

    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState) {
        layout = (FrameLayout) inflater.inflate(R.layout.activity_mystamps_fragment, container, false);
        /*final Resources reso = getResources();
        final TextView num1 = (TextView)layout.findViewById(R.id.num1);
        final TextView num2 = (TextView)layout.findViewById(R.id.num2);
        final TextView num3 = (TextView)layout.findViewById(R.id.num3);
        final TextView num4 = (TextView)layout.findViewById(R.id.num4);
        final TextView num5 = (TextView)layout.findViewById(R.id.num5);
        final TextView num6 = (TextView)layout.findViewById(R.id.num6);
        final TextView num7 = (TextView)layout.findViewById(R.id.num7);
        final TextView num8 = (TextView)layout.findViewById(R.id.num8);
        final TextView num9 = (TextView)layout.findViewById(R.id.num9);
        final TextView num10 = (TextView)layout.findViewById(R.id.num10);
        num1.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num2.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num3.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num4.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num5.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num6.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num7.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num8.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num9.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });
        num10.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1)
            {
                switch(arg1.getActionMasked())
                {
                    case MotionEvent.ACTION_DOWN:
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable) reso.getDrawable(R.drawable.cirkbackhover));
                        return true;

                    }
                    case MotionEvent.ACTION_UP:
                        // TODO Auto-generated method stub
                    {
                        TextView tv = (TextView)layout.findViewById(arg0.getId());
                        tv.setBackground((GradientDrawable)reso.getDrawable(R.drawable.circularback));
                        return true;
                    }
                }
                return false;
            }
        });*/
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        c=activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initViews();
    }

    private void initViews() {
        layout.getForeground().mutate().setAlpha(0);
    }
}
