package android.zero.file.storage.misc;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.annotation.StringRes;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.zero.BuildConfig;
import android.zero.file.storage.DocumentsActivity;
import android.zero.file.storage.DocumentsApplication;
import android.zero.R;
import android.zero.file.storage.model.RootInfo;
import android.zero.file.storage.setting.SettingsActivity;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_STOP_FTPSERVER;
import static android.zero.file.storage.misc.Utils.isWatch;

/**
 * Created by HaKr on 05/09/16.
 */
public class NotificationUtils {
    public static final String SERVER_CHANNEL = "server_channel";
    public static final String TRANSFER_CHANNEL = "transfer_channel";
    public static final String RECEIVE_CHANNEL = "receive_channel";
    public static final int FTP_NOTIFICATION_ID = 916;

    public static void createFtpNotification(Context context, Intent intent, int notification_id) {
        RootsCache roots = DocumentsApplication.getRootsCache(context);
        RootInfo root = roots.getServerRoot();
        if (null == root) {
            return;
        }
        long when = System.currentTimeMillis();
        CharSequence contentTitle = getString(context, R.string.ftp_notif_title);
        CharSequence contentText = String.format(getString(context, R.string.ftp_notif_text), ConnectionUtils.getFTPAddress(context));
        CharSequence tickerText = getString(context, R.string.ftp_notif_starting);
        CharSequence stopText = getString(context, R.string.ftp_notif_stop_server);

        Intent notificationIntent = new Intent(context, DocumentsActivity.class);
        notificationIntent.setData(root.getUri());
        notificationIntent.putExtras(intent.getExtras());
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        Intent stopIntent = new Intent(ACTION_STOP_FTPSERVER);
        stopIntent.setPackage(BuildConfig.APPLICATION_ID);
        stopIntent.putExtras(intent.getExtras());
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, SERVER_CHANNEL)
                .setContentTitle(contentTitle)
                .setContentText(contentText)
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.ic_stat_server)
                .setTicker(tickerText)
                .setWhen(when)
                .setOngoing(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setColor(SettingsActivity.getPrimaryColor())
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(false);

        boolean isWatch = isWatch(context);
        NotificationCompat.Action.Builder actionBuilder = new NotificationCompat.Action.Builder(
                R.drawable.ic_action_stop, stopText, stopPendingIntent);
        if (isWatch) {
            final NotificationCompat.Action.WearableExtender inlineActionForWear = new NotificationCompat.Action.WearableExtender()
                    .setHintDisplayActionInline(true)
                    .setHintLaunchesActivity(false);
            actionBuilder.extend(inlineActionForWear);
            builder.extend(new NotificationCompat.WearableExtender()
                    .setHintContentIntentLaunchesActivity(true));
        }
        NotificationCompat.Action stopAction = actionBuilder.build();
        builder.addAction(stopAction);

        Notification notification = builder.build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notification_id, notification);
    }

    public static void removeNotification(Context context, int notification_id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(notification_id);
    }

    private static String getString(Context context, int id) {
        return context.getResources().getString(id);
    }

    public static void createNotificationChannels(Context context) {
        createServerNotificationChannels(context);
        createTransferChannels(context);
        createReceiveChannels(context);
    }

    public static void createServerNotificationChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager, SERVER_CHANNEL, "Info", "Server Notification", Color.BLUE);
    }

    public static void createTransferChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager, TRANSFER_CHANNEL, context.getString(R.string.channel_transfer_name), "Transfer Notifications", Color.GREEN);
    }

    public static void createReceiveChannels(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(manager, RECEIVE_CHANNEL, context.getString(R.string.channel_service_name), "Receive Notifications", Color.RED);
    }

    private static void createNotificationChannel(NotificationManager manager, String id, String name, String description, int color) {
        createNotificationChannel(manager, id, name, description, color, getImportance());
    }

    private static void createNotificationChannel(NotificationManager manager, String id, String name, String description, int color, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            channel.setDescription(description);
            channel.setLightColor(color);
            manager.createNotificationChannel(channel);
        }
    }

    private static int getImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return NotificationManager.IMPORTANCE_DEFAULT;
        } else {
            // 对于 API 级别低于 24 的设备，选择一个合适的默认值
            return NotificationManager.IMPORTANCE_HIGH;
        }
    }
}