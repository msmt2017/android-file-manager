package android.zero.file.storage.server;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Point;
import java.io.ByteArrayInputStream;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;



import java.io.IOException;

import android.content.Context;
import android.net.Uri;
//import android.provider.DocumentsContract;
import android.zero.file.storage.DocumentsApplication;
import android.zero.file.storage.misc.ConnectionUtils;
import android.zero.file.storage.model.DocumentsContract;
import android.zero.file.storage.model.RootInfo;

import static android.zero.file.storage.cast.CastUtils.MEDIA_THUMBNAILS;
import static android.zero.file.storage.model.DocumentsContract.THUMBNAIL_BUFFER_SIZE;


import fi.iki.elonen.NanoHTTPD.Response.IStatus;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import fi.iki.elonen.NanoHTTPD;

public class WebServer extends SimpleWebServer {

    public static final int DEFAULT_PORT = 1212;

    private static WebServer server = null;
    private boolean isStarted;

    public static WebServer getServer() {
        if(server == null){
            RootInfo rootInfo = DocumentsApplication.getRootsCache().getPrimaryRoot();
            File root = null != rootInfo ? new File(rootInfo.path) : Environment.getExternalStorageDirectory();
            server = new WebServer(root);
        }
        return server;
    }

    public boolean startServer(Context context) {
        if (!isStarted) {
            try {
                if(ConnectionUtils.isConnectedToWifi(context)) {
                    server.start();
                    isStarted = true;
                }
                return isStarted;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean stopServer() {
        if (isStarted && server != null) {
            server.stop();
            isStarted = false;
            return true;
        }
        return false;
    }

    public WebServer(File root) {
        super(null, DEFAULT_PORT,
                Collections.singletonList(root),
                true, null);
    }

  
@Override
public NanoHTTPD.Response serve(IHTTPSession session) {
    Map<String, String> parms = session.getParms();
    String uri = session.getUri();

    if (uri.contains(MEDIA_THUMBNAILS)) {
        String docid = parms.get("docid");
        String authority = parms.get("authority");

        if (docid != null && authority != null) {
            final Uri mediaUri = DocumentsContract.buildDocumentUri(authority, docid);

            if (mediaUri != null) {
                String mimeType = "image/jpeg";
                InputStream inputStream = null;
                try {
                    Context context = DocumentsApplication.getInstance().getApplicationContext();
                    Bitmap thumbnail = DocumentsContract.getDocumentThumbnail(
                        context.getContentResolver(), // ContentResolver 对象
                        mediaUri,                    // 媒体文件的 Uri
                        new Point(400, 400),         // 请求的缩略图大小
                        null                         // 可选的取消信号
                    );

                    if (thumbnail != null) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] imageBytes = baos.toByteArray();
                        inputStream = new ByteArrayInputStream(imageBytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", "Internal Server Error");
                }

                if (inputStream != null) {
                    return newChunkedResponse(Status.OK, mimeType, inputStream);
                } else {
                    return newFixedLengthResponse(Status.NOT_FOUND, "text/plain", "Thumbnail not found");
                }
            }
        }
    }

    return super.serve(session);
}
}
