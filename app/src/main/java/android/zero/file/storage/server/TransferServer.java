package android.zero.file.storage.server;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import android.zero.R;
import android.zero.file.storage.transfer.model.Device;
import android.zero.file.storage.transfer.TransferHelper;
import android.zero.file.storage.transfer.model.Transfer;
import android.zero.file.storage.transfer.NotificationHelper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
/**
 * Listen for new connections and create Transfers for them
 */
public class TransferServer implements Runnable {

    private static final String TAG = "TransferServer";

    public interface Listener {
        void onNewTransfer(Transfer transfer);
    }
   // private final Selector mSelector;
  //  private final NsdManager.RegistrationListener mRegistrationListener;
    
    private Thread mThread = new Thread(this);
    private boolean mStop;

    private Context mContext;
    private Listener mListener;
    private NotificationHelper mNotificationHelper;
    private Selector mSelector = Selector.open();

    private NsdManager.RegistrationListener mRegistrationListener =
            new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, "service registered");
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.i(TAG, "service unregistered");
        }
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, String.format("registration failed: %d", errorCode));
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.e(TAG, String.format("unregistration failed: %d", errorCode));
        }
    };

    /**
     * Create a new transfer server
     * @param context context for retrieving string resources
     * @param notificationHelper notification manager
     * @param listener callback for new transfers
     */
    public TransferServer(Context context, NotificationHelper notificationHelper, Listener listener,Selector selector,NsdManager.RegistrationListener registrationListener) throws IOException {
        mContext = context;
        mNotificationHelper = notificationHelper;
        mListener = listener;
        
        mSelector = selector;
        mRegistrationListener = registrationListener;
        
        
        mStop = false;
    }

    /**
     * Start the server if it is not already running
     */
    public void start() {
        if (!mThread.isAlive()) {
            mStop = false;
            mThread.start();
        }
    }

    /**
     * Stop the transfer server if it is running and wait for it to finish
     */
    public void stop() {
        if (mThread.isAlive()) {
            mStop = true;
            mSelector.wakeup();
            try {
                mThread.join();
            } catch (InterruptedException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }

       @Override
    public void run() {
        Log.i(TAG, "Starting server...");
        mNotificationHelper.startListening();
        NsdManager nsdManager = null;
        ServerSocketChannel serverSocketChannel = null;

        try (ServerSocketChannel channel = ServerSocketChannel.open()) {
            serverSocketChannel = channel;
            serverSocketChannel.socket().bind(new InetSocketAddress(40818));
            serverSocketChannel.configureBlocking(false);
            Log.i(TAG, String.format("Server bound to port %d", serverSocketChannel.socket().getLocalPort()));

            nsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
            NsdServiceInfo serviceInfo = createNsdServiceInfo();
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
            Log.i(TAG, "Service registered.");

            SelectionKey selectionKey = serverSocketChannel.register(mSelector, SelectionKey.OP_ACCEPT);

            while (!Thread.currentThread().isInterrupted()) {
                mSelector.select();

                if (mStop) {
                    break;
                }

                if (selectionKey.isAcceptable()) {
                    acceptConnection(serverSocketChannel);
                }
            }
        } catch (IOException | NullPointerException e) {
            Log.e(TAG, "Error occurred: " + e.getMessage(), e);
        } finally {
            unregisterService(nsdManager);
            mNotificationHelper.stopListening();
            Log.i(TAG, "Server stopped.");
        }
    }

    private NsdServiceInfo createNsdServiceInfo() {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName(TransferHelper.deviceName());
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(40818);
        return serviceInfo;
    }

    private void acceptConnection(ServerSocketChannel serverSocketChannel) throws IOException {
        Log.i(TAG, "Accepting incoming connection");
        SocketChannel socketChannel = serverSocketChannel.accept();
        String unknownDeviceName = mContext.getString(R.string.service_transfer_unknown_device);
        mListener.onNewTransfer(new Transfer(socketChannel, TransferHelper.transferDirectory(), TransferHelper.overwriteFiles(), unknownDeviceName));
    }

    private void unregisterService(NsdManager nsdManager) {
        if (nsdManager != null) {
            nsdManager.unregisterService(mRegistrationListener);
        }
    }

    public void stopServer() {
        mStop = true;
    }

    public interface OnNewTransferListener {
        void onNewTransfer(Transfer transfer);
    }
    
    
    // TODO: this method could use some refactoring
/*
    @Override
    public void run() {
        Log.i(TAG, "starting server...");

        // Inform the notification manager that the server has started
        mNotificationHelper.startListening();

        NsdManager nsdManager = null;

        try {
            // Create a server and attempt to bind to a port
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(40818));
            serverSocketChannel.configureBlocking(false);

            Log.i(TAG, String.format("server bound to port %d",
                    serverSocketChannel.socket().getLocalPort()));

            // Register the service
            nsdManager = (NsdManager) mContext.getSystemService(Context.NSD_SERVICE);
            nsdManager.registerService(
                    new Device(
                            TransferHelper.deviceName(),
                            TransferHelper.deviceUUID(),
                            null,
                            40818
                    ).toServiceInfo(),
                    NsdManager.PROTOCOL_DNS_SD,
                    mRegistrationListener
            );

            // Register the server with the selector
            SelectionKey selectionKey = serverSocketChannel.register(mSelector,
                    SelectionKey.OP_ACCEPT);

            // Create Transfers as new connections come in
            while (true) {
                mSelector.select();
                if (mStop) {
                    break;
                }
                if (selectionKey.isAcceptable()) {
                    Log.i(TAG, "accepting incoming connection");
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    String unknownDeviceName = mContext.getString(
                            R.string.service_transfer_unknown_device);
                    mListener.onNewTransfer(
                            new Transfer(
                                    socketChannel,
                                    TransferHelper.transferDirectory(),
                                    TransferHelper.overwriteFiles(),
                                    unknownDeviceName
                            )
                    );
                }
            }

            // Close the server socket
            serverSocketChannel.close();

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        }

        // Unregister the service
        if (nsdManager != null) {
            nsdManager.unregisterService(mRegistrationListener);
        }

        // Inform the notification manager that the server has stopped
        mNotificationHelper.stopListening();

        Log.i(TAG, "server stopped");
    }
    */
    
}
