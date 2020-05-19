package co.za.roomexample;

import android.content.Context;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = Note.class, version = 1)
public abstract class NoteDatabase extends RoomDatabase {

    private static NoteDatabase instance;

    public abstract NoteDao noteDao();

    //Returns the instance of the DB
    public static synchronized NoteDatabase getInstance(Context context){
        if(instance == null){
            instance = Room.databaseBuilder(context.getApplicationContext(), NoteDatabase.class,
                    "note+database")
                    .fallbackToDestructiveMigration()
                    .addCallback(roomDatabaseCallBack)
                    .build();
        }
        return instance;
    }

    //Callback method that is called just after the DB is newly created
    private static RoomDatabase.Callback roomDatabaseCallBack = new RoomDatabase.Callback(){
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            new PopulateDbAsyncTask(instance).execute();
        }


    };

    //Async Task class that populates db with dummy data when it is first created.
    private static class PopulateDbAsyncTask extends AsyncTask<Void, Void, Void>{

        private NoteDao noteDao;

        private PopulateDbAsyncTask(NoteDatabase db){
            noteDao = db.noteDao();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            noteDao.insert(new Note("Title 1", "Description 1", 1));
            noteDao.insert(new Note("Title 2", "Description 2", 2));
            noteDao.insert(new Note("Title 3", "Description 3", 3));
            return null;
        }
    }
}
