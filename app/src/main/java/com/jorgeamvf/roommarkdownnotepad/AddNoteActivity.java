package com.jorgeamvf.roommarkdownnotepad;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.jorgeamvf.roommarkdownnotepad.notedb.NoteDatabase;
import com.jorgeamvf.roommarkdownnotepad.notedb.model.Note;

import java.lang.ref.WeakReference;

public class AddNoteActivity extends AppCompatActivity {

    private TextInputEditText editText_title, editText_content;
    private NoteDatabase noteDatabase;
    private Note note;
    private boolean update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);
        editText_title = findViewById(R.id.editText_title);
        editText_content = findViewById(R.id.editText_content);
        noteDatabase = NoteDatabase.getInstance(AddNoteActivity.this);
        Button button = findViewById(R.id.button_save);
        if ((note = (Note) getIntent().getSerializableExtra("note")) != null) {
            update = true;
            button.setText(R.string.button_save);
            editText_title.setText(note.getTitle());
            editText_content.setText(note.getContent());
        }
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (update) {
                    note.setContent(editText_content.getText().toString());
                    note.setTitle(editText_title.getText().toString());
                    noteDatabase.getNoteDao().updateNote(note);
                    setResult(note, 2);
                } else {
                    note = new Note(editText_content.getText().toString(), editText_title.getText().toString());
                    new InsertTask(AddNoteActivity.this, note).execute();
                }
            }
        });
    }

    private void setResult(Note note, int flag) {
        setResult(flag, new Intent().putExtra("note", note));
        finish();
    }

    private static class InsertTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<AddNoteActivity> activityReference;
        private Note note;

        InsertTask(AddNoteActivity context, Note note) {
            activityReference = new WeakReference<>(context);
            this.note = note;
        }

        @Override
        protected Boolean doInBackground(Void... objs) {
            long j = activityReference.get().noteDatabase.getNoteDao().insertNote(note);
            note.setNote_id(j);
            Log.e("ID ", "doInBackground: " + j);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            if (bool) {
                activityReference.get().setResult(note, 1);
                activityReference.get().finish();
            }
        }
    }

}
