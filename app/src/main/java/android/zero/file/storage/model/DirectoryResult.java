package android.zero.file.storage.model;

import android.content.ContentProviderClient;
import android.database.Cursor;

import java.io.Closeable;

import android.zero.file.storage.libcore.io.IoUtils;
import android.zero.file.storage.misc.ContentProviderClientCompat;

import static android.zero.file.storage.BaseActivity.State.MODE_UNKNOWN;
import static android.zero.file.storage.BaseActivity.State.SORT_ORDER_UNKNOWN;

public class DirectoryResult implements Closeable {
	public ContentProviderClient client;
    public Cursor cursor;
    public Exception exception;

    public int mode = MODE_UNKNOWN;
    public int sortOrder = SORT_ORDER_UNKNOWN;

    @Override
    public void close() {
        IoUtils.closeQuietly(cursor);
        ContentProviderClientCompat.releaseQuietly(client);
        cursor = null;
        client = null;
    }
}