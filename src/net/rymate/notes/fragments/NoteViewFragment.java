package net.rymate.notes.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.rymate.notes.R;
import net.rymate.notes.activities.NotesListActivity;
import net.rymate.notes.database.NotesDbAdapter;

/**
 * Created by Ryan on 05/07/13.
 */
public class NoteViewFragment extends Fragment {

    private TextView mBodyText;
    public static Long mRowId;
    private NotesDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            mRowId = (savedInstanceState == null) ? null :
                    (Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
            if (mRowId == null) {
                Bundle extras = getActivity().getIntent().getExtras();
                mRowId = extras != null ? extras.getLong(NotesDbAdapter.KEY_ROWID)
                        : null;
            }

            if (mRowId == null) {
                NotesListActivity list = (NotesListActivity) getActivity();
                mRowId = list.getID();
            }
        }

        mDbHelper = new NotesDbAdapter(this.getActivity());
        mDbHelper.open();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_note_view, container, false);

        mBodyText = (TextView) rootView.findViewById(R.id.noteView);

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            getActivity().startManagingCursor(note);
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
        }

        return rootView;
    }

}
