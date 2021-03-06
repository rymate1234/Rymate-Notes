package net.rymate.notes.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.app.NavUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import net.rymate.notes.R;
import net.rymate.notes.data.NotesDbAdapter;
import net.rymate.notes.fragments.DeleteNoteDialogFragment;
import net.rymate.notes.fragments.NoteViewFragment;
import net.rymate.notes.ui.UIUtils;

/**
 * Created by Ryan on 05/07/13.
 */
public class NoteViewActivity extends AppCompatActivity
        implements DeleteNoteDialogFragment.DeleteNoteDialogListener {

    Long mRowId;
    private NoteViewFragment nvf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_view);

        Toolbar toolbar = (Toolbar) findViewById(R.id.action_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the note view fragment and add it to the activity
            // using a fragment transaction.
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }

            Bundle arguments = new Bundle();
            arguments.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
            nvf = new NoteViewFragment();
            nvf.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.note_container, nvf)
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.noteview_menu_phone, menu);
        if (!UIUtils.hasKitKat())
            menu.removeItem(R.id.print_note);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (nvf == null) {
            return true;
        }

        if (nvf.isEditing()) {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.edit_activity, menu);
        } else {
            menu.clear();
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.noteview_menu_phone, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.swap_in_bottom_back, R.anim.swap_out_bottom_back);
                return true;
            case R.id.edit_note:
                Intent i = new Intent(this, NoteEditActivity.class);
                i.putExtra(NotesDbAdapter.KEY_ROWID, mRowId);
                startActivityForResult(i, 2);
                overridePendingTransition(R.anim.swap_in_bottom, R.anim.swap_out_bottom);
                return true;
            case R.id.delete_note:
                showDeleteDialog(mRowId);
                return true;
            case R.id.save_note:
                nvf.saveNote();
                return true;
            case R.id.print_note:
                nvf.printNote();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.swap_in_bottom_back, R.anim.swap_out_bottom_back);
    }

    public void showDeleteDialog(long noteId) {
        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment dialog = new DeleteNoteDialogFragment();
        dialog.show(getSupportFragmentManager(), "dialog");
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the NoticeDialogFragment.NoticeDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        NotesDbAdapter mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mDbHelper.deleteNote(mRowId);
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, R.string.note_deleted, duration);
        toast.show();
        NavUtils.navigateUpTo(this, new Intent(this, NotesListActivity.class));
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button

    }
}