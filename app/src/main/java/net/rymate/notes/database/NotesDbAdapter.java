package net.rymate.notes.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple gen database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all gen as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class NotesDbAdapter {

    public static final String KEY_TITLE = "title";
    public static final String KEY_BODY = "body";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_CATID = "cat_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table gen (_id integer primary key autoincrement, "
        + "title text not null, body text not null, cat_id integer);";

    private static final String DATABASE_CATEGORIES_CREATE =
            "create table categories (_id integer primary key autoincrement, "
                    + "title text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "gen";
    private static final int DATABASE_VERSION = 4;

    private final Context mCtx;

    public static String getSample() {
        String sample = sample(KEY_BODY);
        return sample;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CATEGORIES_CREATE);
            addCategory("All Notes", db);
            addCategory("Uncategorised", db);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            //db.execSQL("DROP TABLE IF EXISTS gen");
            //onCreate(db);
            db.execSQL(DATABASE_CATEGORIES_CREATE);
            addCategory("All Notes", db);
            addCategory("Uncategorised", db);
        }

        public long addCategory(String title, SQLiteDatabase db) {
            ContentValues initialValues = new ContentValues();
            initialValues.put(KEY_TITLE, title);

            return db.insert("categories", null, initialValues);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the gen database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_CATID, 0);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    public long createNote(String title, String body, int catId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);
        initialValues.put(KEY_BODY, body);
        initialValues.put(KEY_CATID, catId);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Create a new category using the title provided.
     *
     * @param title the category name
     * @return rowId or -1 if failed
     */
    public long addCategory(String title) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TITLE, title);

        return mDb.insert("categories", null, initialValues);
    }


    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     * */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, null, null, null, null, null);
    }

    /**
     * Return a Cursor over the list of all notes in a category
     *
     * @param catId id of the category
     * @return Cursor over all notes in that category
     * */
    public Cursor fetchNotes(int catId) {
        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_TITLE,
                KEY_BODY}, KEY_CATID + " = " + catId, null, null, null, null);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_TITLE, KEY_BODY, KEY_CATID}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Return a Cursor over the list of all categories in the database
     *
     * @return Cursor over all notes
     * */
    public Cursor fetchCategories() {

        return mDb.query("categories", new String[] {KEY_ROWID, KEY_TITLE}
                , null, null, null, null, null);

    }

    /**
     * Return a Cursor over the list of nearly all categories in the database
     *
     * @return Cursor over all notes
     * */
    public Cursor fetchNearlyAllCategories() {

        return mDb.query("categories", new String[] {KEY_ROWID, KEY_TITLE}
                , KEY_ROWID + " NOT LIKE '%" + 1 + "%'", null, null, null, null);

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @param category value of the new category
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, int category) {
        ContentValues args = new ContentValues();
        args.put(KEY_TITLE, title);
        args.put(KEY_BODY, body);
        args.put(KEY_CATID, category);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }

    private static String sample(String s) {
        Pattern pattern = Pattern.compile("([\\S]+\\s*){1,8}");
        Matcher matcher = pattern.matcher(s);
        matcher.find();
        return matcher.group();
    }

}