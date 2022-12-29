package com.spectator.detailedinfo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import com.spectator.BaseActivity;
import com.spectator.R;
import com.spectator.counter.EditTextDialog;
import com.spectator.data.Comment;
import com.spectator.utils.JsonIO;

import java.io.File;
import java.util.ArrayList;

public class ViewComments extends BaseActivity {

    private static final int EDIT_EXISTING_COMMENT_REQUEST = 13;
    private  static final int CREATE_COMMENT_REQUEST = 12;

    private LinearLayout commentList;
    private JsonIO commentsJsonIO;
    private String commentsPath;
    private ArrayList<Comment> comments;
    private LayoutInflater rowInflater;
    private int editingCommentIndex = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_comments);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Log.i("CommentsExtras", "null");
            commentsPath = Comment.COMMENTS_PATH;
        }
        else {
            Log.i("CommentsExtras", "not null");
            commentsPath = extras.getString("commentsPath");
        }

        commentList = (LinearLayout) findViewById(R.id.comment_list);
        commentsJsonIO = new JsonIO(this.getFilesDir(), commentsPath, Comment.ARRAY_KEY, true);

        comments = commentsJsonIO.parseJsonArray(false, new ArrayList<Comment>(),true, Comment.ARRAY_KEY, Comment.class, Comment.constructorArgs, Comment.jsonKeys, null);

        rowInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int j = 0; j < comments.size(); j++) {
            ConstraintLayout newRow = makeNewRow(comments.get(j));
            commentList.addView(newRow);
        }

        LinearLayout addComment = (LinearLayout) findViewById(R.id.leave_comment);
        addComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent editTextIntent = new Intent(getApplicationContext(), EditTextDialog.class);
                final Bundle bundle = new Bundle();
                bundle.putString(EditTextDialog.textHintExtras, getString(R.string.comment_hint));
                bundle.putInt(EditTextDialog.textInputTypeExtras, InputType.TYPE_CLASS_TEXT);
                bundle.putInt(EditTextDialog.textMaxLengthExtras, 500);
                editTextIntent.putExtras(bundle);
                startActivityForResult(editTextIntent, CREATE_COMMENT_REQUEST);
            }
        });

        TextView exportComments = (TextView) findViewById(R.id.export_comments);
        exportComments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportData();
            }
        });
    }

    private ConstraintLayout makeNewRow(final Comment newComment) {
        final ConstraintLayout layout = (ConstraintLayout) rowInflater.inflate(R.layout.comment, null);

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(layoutParams);

        TextView newCommentView = layout.findViewById(R.id.comment_text);
        newCommentView.setText(newComment.getCommentText());

        TextView newDateView = layout.findViewById(R.id.comment_date);
        newDateView.setText(newComment.getFormattedDate());

        TextView newTimeView = layout.findViewById(R.id.comment_time);
        newTimeView.setText(newComment.getFormattedTime());

        TextView deleteView = layout.findViewById(R.id.comment_delete);
        deleteView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    int index = commentsJsonIO.getIndexOfObject(Comment.commentKey, newComment.getCommentText(), Comment.ARRAY_KEY);
                    comments.remove(index);
                    commentList.removeViewAt(index);
                    commentsJsonIO.deleteAt(index, Comment.ARRAY_KEY);
                } catch (JsonIO.ObjectNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        TextView editView = layout.findViewById(R.id.comment_edit);
        editView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editingCommentIndex = commentList.indexOfChild(layout);
                Intent intentWithResult = new Intent(getApplicationContext(), EditTextDialog.class);
                final Bundle bundle = new Bundle();
                bundle.putString(EditTextDialog.textHintExtras, getString(R.string.comment_hint));
                bundle.putInt(EditTextDialog.textInputTypeExtras, InputType.TYPE_CLASS_TEXT);
                bundle.putInt(EditTextDialog.textMaxLengthExtras, 500);
                TextView text = layout.findViewById(R.id.comment_text);
                bundle.putString(EditTextDialog.textDefaultExtras, text.getText().toString());
                intentWithResult.putExtras(bundle);
                startActivityForResult(intentWithResult, EDIT_EXISTING_COMMENT_REQUEST);
            }
        });

        return layout;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case EDIT_EXISTING_COMMENT_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("textResult")) {
                        if (!data.getStringExtra("textResult").equals("")) {
                            if (editingCommentIndex >= 0 && comments.size() > editingCommentIndex && commentList.getChildCount() > editingCommentIndex) {
                                try {
                                    Comment oldComment = comments.get(editingCommentIndex);
                                    Comment newComment = oldComment.getCommentWithChanged(data.getStringExtra("textResult"));
                                    commentsJsonIO.replaceObject(newComment.toJSONObject(), Comment.commentKey, oldComment.getCommentText(), Comment.ARRAY_KEY);
                                    comments.set(editingCommentIndex, newComment);
                                    commentList.removeViewAt(editingCommentIndex);
                                    commentList.addView(makeNewRow(newComment), editingCommentIndex);
                                } catch (JsonIO.ObjectNotFoundException e) {
                                    e.printStackTrace();
                                } finally {
                                    editingCommentIndex = -1;
                                }
                            }
                        }
                    }
                }
                break;
            }
            case CREATE_COMMENT_REQUEST: {
                if (resultCode == RESULT_OK) {
                    if (data.hasExtra("textResult")) {
                        if (!data.getStringExtra("textResult").equals("")) {
                            Comment newComment = new Comment(System.currentTimeMillis(), data.getStringExtra("textResult"));
                            commentsJsonIO.writeToEndOfFile(newComment.toJSONObject());
                            comments.add(newComment);
                            commentList.addView(makeNewRow(newComment));
                        }
                    }
                }
            }
        }
    }

    private void exportData() {
        File file = new File(this.getFilesDir(), commentsPath);
        Uri uri = FileProvider.getUriForFile(this, "com.spectator.fileProvider", file);

        Intent exportingIntent = new Intent(android.content.Intent.ACTION_SEND);
        exportingIntent.setType("application/json");
        exportingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        exportingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getString(R.string.comments));
        exportingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(exportingIntent, getString(R.string.export_via)));
    }
}
