package android.zero.file.storage.adapter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import android.zero.R;
import android.zero.file.storage.common.CursorRecyclerViewAdapter;
import android.zero.file.storage.misc.IconHelper;
import android.zero.file.storage.model.DocumentInfo;
import android.zero.file.storage.model.DocumentsContract;
import android.zero.file.storage.setting.SettingsActivity;

public class RecentsAdapter extends CursorRecyclerViewAdapter<RecentsAdapter.ViewHolder> {

    private final IconHelper mIconHelper;
    private final int mDefaultColor;
    private Context mContext;
    private OnItemClickListener onItemClickListener;

    public RecentsAdapter(Context context, Cursor cursor, IconHelper iconHelper){
        super(context, cursor);
        mContext = context;
        mDefaultColor = SettingsActivity.getPrimaryColor();
        mIconHelper = iconHelper;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Cursor cursor) {
        viewHolder.setData(cursor);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recent, parent, false);
        return new ViewHolder(itemView);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener(){
        return onItemClickListener;
    }

    public interface OnItemClickListener{
        void onItemClick(ViewHolder item, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconMime;
        private final ImageView iconThumb;
        private final View iconMimeBackground;
        private Cursor mCursor;
        public DocumentInfo mDocumentInfo;

        public ViewHolder(View v) {
            super(v);
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(ViewHolder.this, getLayoutPosition());
                }
            });
            iconMime = (ImageView) v.findViewById(R.id.icon_mime);
            iconThumb = (ImageView) v.findViewById(R.id.icon_thumb);
            iconMimeBackground = v.findViewById(R.id.icon_mime_background);
        }

        public void setData(Cursor cursor){
            mDocumentInfo = DocumentInfo.fromDirectoryCursor(cursor);

            mIconHelper.stopLoading(iconThumb);

            iconMime.animate().cancel();
            iconMime.setAlpha(1f);
            iconThumb.animate().cancel();
            iconThumb.setAlpha(0f);

            final Uri uri = DocumentsContract.buildDocumentUri(mDocumentInfo.authority, mDocumentInfo.documentId);
            mIconHelper.load(uri, mDocumentInfo.path, mDocumentInfo.mimeType,
                    mDocumentInfo.flags, mDocumentInfo.icon, mDocumentInfo.lastModified,
                    iconThumb, iconMime, iconMimeBackground);
        }
    }
}