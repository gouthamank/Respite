package app.drool.respite.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import app.drool.respite.R;

/**
 * Created by drool on 7/12/16.
 */

public class ReplyActivity extends AppCompatActivity {
    static final int REPLY_REQUEST = 100;
    static final int REPLY_POST = 101;
    static final int REPLY_COMMENT = 102;

    private EditText input = null;
    private Button submitBtn, discardBtn;
    private boolean isForPost = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reply);
        getSupportActionBar().hide();

        input = (EditText) findViewById(R.id.activity_reply_edittext);
        isForPost = getIntent().getBooleanExtra("postRequest", false);
        submitBtn = (Button) findViewById(R.id.activity_reply_submit);
        submitBtn.setEnabled(false);
        discardBtn = (Button) findViewById(R.id.activity_reply_discard);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReplyActivity.this);
                builder.setTitle(getString(R.string.replyactivity_confirm));
                builder.setPositiveButton(getString(R.string.replyactivity_submit), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("reply", input.getText().toString());
                        if (isForPost)
                            setResult(REPLY_POST, returnIntent);
                        else
                            setResult(REPLY_COMMENT, returnIntent);
                        finish();
                    }
                });
                builder.create().show();
            }
        });

        discardBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ReplyActivity.this);
                builder.setTitle(getString(R.string.replyactivity_confirm));
                builder.setPositiveButton(getString(R.string.replyactivity_discard), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
            }
        });

        input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                submitBtn.setEnabled(s.length() > 0);
            }
        });
    }

    @Override
    public void onBackPressed() {
        discardBtn.callOnClick();
    }
}
