package com.clozerr.app.Activities.ExtraElements;

import android.app.Dialog;
import android.content.Context;

import com.clozerr.app.R;

/**
 * Created by maurice on 20/08/15.
 */
public class FeedbackForm {
    public static void showFeedbackForm(Context context){
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.feedback_dialog);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button
//        TextView text = (TextView) dialog.findViewById(R.id.text);
//        text.setText("Android custom dialog example!");
//        ImageView image = (ImageView) dialog.findViewById(R.id.image);
//        image.setImageResource(R.drawable.ic_launcher);
//
//        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButtonOK);
//        // if button is clicked, close the custom dialog
//        dialogButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                dialog.dismiss();
//            }
//        });

        dialog.show();
    }
}
