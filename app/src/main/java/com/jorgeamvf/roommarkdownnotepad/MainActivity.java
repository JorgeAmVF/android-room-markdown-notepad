package com.jorgeamvf.roommarkdownnotepad;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.jorgeamvf.roommarkdownnotepad.adapter.NotesAdapter;
import com.jorgeamvf.roommarkdownnotepad.notedb.NoteDatabase;
import com.jorgeamvf.roommarkdownnotepad.notedb.model.Note;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesAdapter.OnNoteItemClick {

    RecyclerView recyclerView;
    private TextView textViewMsg;
    private NoteDatabase noteDatabase;
    private List<Note> notes;
    private NotesAdapter notesAdapter;
    private int pos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeViews();
        displayList();
    }

    private void displayList() {
        noteDatabase = NoteDatabase.getInstance(MainActivity.this);
        new RetrieveTask(this).execute();
    }

    private static class RetrieveTask extends AsyncTask<Void, Void, List<Note>> {

        private WeakReference<MainActivity> activityReference;

        RetrieveTask(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected List<Note> doInBackground(Void... voids) {
            if (activityReference.get() != null)
                return activityReference.get().noteDatabase.getNoteDao().getNotes();
            else
                return null;
        }

        @Override
        protected void onPostExecute(List<Note> notes) {
            if (notes != null && notes.size() > 0) {
                activityReference.get().notes.clear();
                activityReference.get().notes.addAll(notes);
                activityReference.get().textViewMsg.setVisibility(View.GONE);
                activityReference.get().notesAdapter.notifyDataSetChanged();
            }
        }
    }

    private void initializeViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewMsg = findViewById(R.id.textview_empty);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(listener);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        notes = new ArrayList<>();
        notesAdapter = new NotesAdapter(notes, MainActivity.this);
        recyclerView.setAdapter(notesAdapter);
    }

    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(MainActivity.this, AddNoteActivity.class), 100);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode > 0) {
            if (resultCode == 1) {
                notes.add((Note) data.getSerializableExtra("note"));
            } else if (resultCode == 2) {
                notes.set(pos, (Note) data.getSerializableExtra("note"));
            }
            listVisibility();
        }
    }

    @Override
    public void onNoteClick(final int pos) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Select:")
                .setItems(new String[]{"Read", "Update", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                MainActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(MainActivity.this,
                                                ViewNoteActivity.class).putExtra(
                                                "note", notes.get(pos)), 100);
                                break;
                            case 1:
                                MainActivity.this.pos = pos;
                                startActivityForResult(
                                        new Intent(MainActivity.this,
                                                AddNoteActivity.class).putExtra(
                                                "note", notes.get(pos)), 100);
                                break;
                            case 2:
                                noteDatabase.getNoteDao().deleteNote(notes.get(pos));
                                notes.remove(pos);
                                listVisibility();
                                break;
                        }
                    }
                }).show();
    }

    private void listVisibility() {
        int emptyMsgVisibility = View.GONE;
        if (notes.size() == 0) {
            if (textViewMsg.getVisibility() == View.GONE)
                emptyMsgVisibility = View.VISIBLE;
        }
        textViewMsg.setVisibility(emptyMsgVisibility);
        notesAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        noteDatabase.cleanUp();
        super.onDestroy();
    }

}
