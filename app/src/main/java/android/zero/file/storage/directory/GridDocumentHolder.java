package android.zero.file.storage.directory;

import android.content.Context;
import android.view.ViewGroup;

import android.zero.R;
import android.zero.file.storage.common.RecyclerFragment.RecyclerItemClickListener.OnItemClickListener;
import android.zero.file.storage.directory.DocumentsAdapter.Environment;

public class GridDocumentHolder extends ListDocumentHolder {

    public GridDocumentHolder(Context context, ViewGroup parent,
                              OnItemClickListener onItemClickListener, Environment environment) {
        super(context, parent, R.layout.item_doc_grid, onItemClickListener, environment);
    }

}
