package app.drool.respite.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import app.drool.respite.R;
import app.drool.respite.impl.SubmissionParcelable;

/**
 * Created by drool on 6/18/16.
 */

public class CommentsActivity extends AppCompatActivity {

    SubmissionParcelable submissionParcelable = null;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView(R.layout.activity_comments);
        submissionParcelable = getIntent().getParcelableExtra("top");

        ListView mList = (ListView) findViewById(R.id.comments_list);
        ViewGroup header = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_comments_header, mList, false);

        ((TextView) findViewById(R.id.comments_header_description)).setText(submissionParcelable.getDescription());
        ((TextView) findViewById(R.id.comments_header_title)).setText(submissionParcelable.getTitle());
        ((TextView) findViewById(R.id.comments_header_comments)).setText(submissionParcelable.getComments());
        ((TextView) findViewById(R.id.comments_header_score)).setText(submissionParcelable.getScore());
        ((TextView) findViewById(R.id.comments_header_selftext)).setText(submissionParcelable.getSelfText());


    }

    @Override
    protected void onResume() {
        super.onResume ();
    }

}
