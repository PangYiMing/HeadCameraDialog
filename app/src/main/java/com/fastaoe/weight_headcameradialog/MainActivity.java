package com.fastaoe.weight_headcameradialog;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.Window;

public class MainActivity extends AppCompatActivity {

    private UpdateUserHeadDialog mUserHeadDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircleImageView iv_head = (CircleImageView) findViewById(R.id.iv_head);
        mUserHeadDialog = new UpdateUserHeadDialog(this, iv_head);

        if (iv_head != null) {
            iv_head.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Window window = mUserHeadDialog.getWindow();
                    if (window != null) {
                        window.setGravity(Gravity.BOTTOM);
                    }

                    mUserHeadDialog.show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mUserHeadDialog.activityResult(resultCode, requestCode, data);
        mUserHeadDialog.dismiss();
    }
}
