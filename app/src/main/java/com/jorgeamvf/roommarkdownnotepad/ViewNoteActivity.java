package com.jorgeamvf.roommarkdownnotepad;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.jorgeamvf.roommarkdownnotepad.notedb.NoteDatabase;
import com.jorgeamvf.roommarkdownnotepad.notedb.model.Note;

import us.feras.mdv.MarkdownView;

public class ViewNoteActivity extends AppCompatActivity {

    TextView note_content;
    Note note;
    NoteDatabase noteDatabase;
    private MarkdownView markdownView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_note);
        note_content = findViewById(R.id.textview_content);
        markdownView = findViewById(R.id.markdownView);
        noteDatabase = NoteDatabase.getInstance(ViewNoteActivity.this);
        if ((note = (Note) getIntent().getSerializableExtra("note")) != null) {
            note_content.setText(note.getContent());
            updateMarkdownView();
        }
    }

    private void updateMarkdownView() {
        markdownView.loadMarkdown(note_content.getText().toString());
    }

}
