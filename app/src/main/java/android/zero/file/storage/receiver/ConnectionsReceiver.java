package android.zero.file.storage.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.zero.file.storage.misc.ConnectionUtils;
import android.zero.file.storage.misc.NotificationUtils;
import android.zero.file.storage.service.ConnectionsService;
import android.zero.file.storage.service.TransferService;
import android.zero.file.storage.transfer.TransferHelper;

import static android.zero.file.storage.misc.ConnectionUtils.ACTION_FTPSERVER_STARTED;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_FTPSERVER_STOPPED;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_START_FTPSERVER;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_STOP_FTPSERVER;
import static android.zero.file.storage.misc.NotificationUtils.FTP_NOTIFICATION_ID;
import static android.zero.file.storage.transfer.TransferHelper.ACTION_START_LISTENING;
import static android.zero.file.storage.transfer.TransferHelper.ACTION_STOP_LISTENING;

public class ConnectionsReceiver extends BroadcastReceiver {

    static final String TAG = ConnectionsReceiver.class.getSimpleName();

    public ConnectionsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (ACTION_START_FTPSERVER.equals(action)) {
            Intent serverService = new Intent(context, ConnectionsService.class);
            serverService.putExtras(intent.getExtras());
            if (!ConnectionUtils.isServerRunning(context)) {
                context.startService(serverService);
            }
        } else if (ACTION_STOP_FTPSERVER.equals(action)) {
            Intent serverService = new Intent(context, ConnectionsService.class);
            serverService.putExtras(intent.getExtras());
            context.stopService(serverService);
        } else if (ACTION_FTPSERVER_STARTED.equals(action)) {
            NotificationUtils.createFtpNotification(context, intent, FTP_NOTIFICATION_ID);
        } else if (ACTION_FTPSERVER_STOPPED.equals(action)) {
            NotificationUtils.removeNotification(context, FTP_NOTIFICATION_ID);
        } else if (ACTION_START_LISTENING.equals(action)) {
            Intent serverService = new Intent(context, TransferService.class);
            serverService.setAction(action);
            if (!TransferHelper.isServerRunning(context)) {
                context.startService(serverService);
            }
        } else if (ACTION_STOP_LISTENING.equals(action)) {
            Intent serverService = new Intent(context, TransferService.class);
            serverService.setAction(action);
            context.startService(serverService);
        }
    }
}
