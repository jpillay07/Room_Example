package co.za.roomexample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int ADD_NOTE_REQUEST = 1;
    static final int EDIT_NOTE_REQUEST = 2;
    NoteViewModel noteViewModel;
    RecyclerView noteRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton buttonAddNote = findViewById(R.id.button_add_note);

        buttonAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addNewNoteIntent = new Intent(MainActivity.this,
                        AddEditNoteActivity.class);

                startActivityForResult(addNewNoteIntent, ADD_NOTE_REQUEST);
            }
        });

        noteRecyclerView = findViewById(R.id.recycler_view);
        noteRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteRecyclerView.setHasFixedSize(true);

        final NoteAdapter noteAdapter = new NoteAdapter();

        noteRecyclerView.setAdapter(noteAdapter);

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        noteViewModel.getAllNotes().observe(this, new Observer<List<Note>>() {
            @Override
            public void onChanged(List<Note> notes) {
                noteAdapter.submitList(notes);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                noteViewModel.delete(noteAdapter.getNoteAt(viewHolder.getAdapterPosition()));
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        }).attachToRecyclerView(noteRecyclerView);

        noteAdapter.setOnItemClickListener(new NoteAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Note note) {
                Intent editNoteIntent = new Intent(MainActivity.this, AddEditNoteActivity.class);
                editNoteIntent.putExtra(AddEditNoteActivity.EXTRA_ID, note.getId());
                editNoteIntent.putExtra(AddEditNoteActivity.EXTRA_TITLE, note.getTitle());
                editNoteIntent.putExtra(AddEditNoteActivity.EXTRA_DESCRIPTION, note.getDescription());
                editNoteIntent.putExtra(AddEditNoteActivity.EXTRA_PRIORITY, note.getPriority());

                startActivityForResult(editNoteIntent, EDIT_NOTE_REQUEST);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.delete_all_menu_item:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void deleteAll() {
        noteViewModel.deleteAllNotes();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_NOTE_REQUEST && resultCode == RESULT_OK){

            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);

            Note newNote = new Note(title, description, priority);

            noteViewModel.insert(newNote);

            Toast.makeText(this, "Note added", Toast.LENGTH_LONG).show();
        }
        else if(requestCode == EDIT_NOTE_REQUEST && resultCode == RESULT_OK){
            String title = data.getStringExtra(AddEditNoteActivity.EXTRA_TITLE);
            String description = data.getStringExtra(AddEditNoteActivity.EXTRA_DESCRIPTION);
            int priority = data.getIntExtra(AddEditNoteActivity.EXTRA_PRIORITY, 1);
            int id = data.getIntExtra(AddEditNoteActivity.EXTRA_ID, -1);

            Note newNote = new Note(title, description, priority);

            if(id != -1){
                newNote.setId(id);
                noteViewModel.update(newNote);
                Toast.makeText(this, "Note added", Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(this, "Note NOT added", Toast.LENGTH_LONG).show();
                return;
            }
        }else{
            Toast.makeText(this, "Note not saved", Toast.LENGTH_LONG).show();
        }
    }
}
