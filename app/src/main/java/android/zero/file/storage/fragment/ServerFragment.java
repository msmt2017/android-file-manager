package android.zero.file.storage.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;

import android.zero.BuildConfig;
import android.zero.R;
import android.zero.file.storage.ShareDeviceActivity;
import android.zero.file.storage.common.BaseFragment;
import android.zero.file.storage.misc.ConnectionUtils;
import android.zero.file.storage.misc.IconUtils;
import android.zero.file.storage.misc.RootsCache;
import android.zero.file.storage.model.RootInfo;
import android.zero.file.storage.network.NetworkConnection;
import android.zero.file.storage.provider.CloudStorageProvider;
import android.zero.file.storage.provider.NetworkStorageProvider;
import android.zero.file.storage.setting.SettingsActivity;

import static android.zero.file.storage.misc.ConnectionUtils.ACTION_FTPSERVER_FAILEDTOSTART;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_FTPSERVER_STARTED;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_FTPSERVER_STOPPED;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_START_FTPSERVER;
import static android.zero.file.storage.misc.ConnectionUtils.ACTION_STOP_FTPSERVER;
import static android.zero.file.storage.misc.ConnectionUtils.editConnection;
import static android.zero.file.storage.misc.Utils.EXTRA_ROOT;

public class ServerFragment extends BaseFragment implements View.OnClickListener {

    public static final String TAG = "ServerFragment";

    private TextView status;
    private TextView username;
    private TextView password;
    private TextView path;
    private TextView address;
    private Button action;
    private TextView warning;
    private RootInfo root;
    private int connection_id;

    public static void show(FragmentManager fm, RootInfo root) {
        final ServerFragment fragment = new ServerFragment();
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_ROOT, root);
        fragment.setArguments(args);
        final FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container_directory, fragment, TAG);
        ft.commitAllowingStateLoss();
    }

    public static ServerFragment get(FragmentManager fm) {
        return (ServerFragment) fm.findFragmentByTag(TAG);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.fragment_server,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        status =(TextView) view.findViewById(R.id.status);
        username =(TextView) view.findViewById(R.id.username);
        password =(TextView) view.findViewById(R.id.password);
        path = (TextView) view.findViewById(R.id.path);
        address = (TextView) view.findViewById(R.id.address);
        address.setTextColor(SettingsActivity.getAccentColor());
        address.setHighlightColor(SettingsActivity.getPrimaryColor());
        warning = (TextView) view.findViewById(R.id.warning);
        action = (Button) view.findViewById(R.id.action);
        action.setOnClickListener(this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setRetainInstance(true);
        root = getArguments().getParcelable(EXTRA_ROOT);
        NetworkConnection connection = NetworkConnection.fromRootInfo(getActivity(), root);
        connection_id = connection.id;
        showData(connection);
    }

    private void showData(NetworkConnection connection) {
        if (null == connection){
            return;
        }
        path.setText(connection.getPath());
        username.setText(connection.getUserName());
        password.setText(connection.getPassword());
    }

    @Override
    public void onResume(){
        super.onResume();
        updateStatus();
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(mWifiReceiver, wifiFilter);

        IntentFilter ftpFilter = new IntentFilter();
        ftpFilter.addAction(ACTION_FTPSERVER_STARTED);
        ftpFilter.addAction(ACTION_FTPSERVER_STOPPED);
        ftpFilter.addAction(ACTION_FTPSERVER_FAILEDTOSTART);
        getActivity().registerReceiver(mFtpReceiver, ftpFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        getActivity().unregisterReceiver(mWifiReceiver);
        getActivity().unregisterReceiver(mFtpReceiver);
    }

    @Override
    public  void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_server, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      
          int  getitem =item.getItemId();
          
                if(getitem == R.id.action_edit_server){
                editConnection(getAppCompatActivity(), connection_id);
                
                } else if (getitem == R.id.action_transfer_help){
                      showHelp();
                
                }

        return super.onOptionsItemSelected(item);
    }

    public void reload(){
        NetworkConnection networkConnection =
                NetworkConnection.fromConnectionId(getActivity(), connection_id);
        RootsCache.updateRoots(getActivity(), NetworkStorageProvider.AUTHORITY);
        RootsCache.updateRoots(getActivity(), CloudStorageProvider.AUTHORITY);
        showData(networkConnection);
    }

    private void startServer() {
        Intent intent = new Intent(ACTION_START_FTPSERVER);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        intent.putExtras(getArguments());
        getActivity().sendBroadcast(intent);
    }

    private void stopServer() {
        Intent intent = new Intent(ACTION_STOP_FTPSERVER);
        intent.setPackage(BuildConfig.APPLICATION_ID);
        intent.putExtras(getArguments());
        getActivity().sendBroadcast(intent);
    }

    private void updateStatus(){
        setStatus(ConnectionUtils.isServerRunning(getActivity()));
    }

    private void setStatus(boolean running){
        if(running){
            setText(address, ConnectionUtils.getFTPAddress(getActivity()));
            status.setText(getString(R.string.ftp_status_running));
            action.setText(R.string.stop_ftp);
        } else {
            setText(address, "");
            setText(warning, "");
            status.setText(getString(R.string.ftp_status_not_running));
            action.setText(R.string.start_ftp);
        }
    }
    @Override
    public void onClick(View view) {

int viewId = view.getId();

if (viewId == R.id.action) {
    if (!ConnectionUtils.isServerRunning(getActivity())) {
        if (ConnectionUtils.isConnectedToLocalNetwork(getActivity())) {
            startServer();
        } else {
            setText(warning, getString(R.string.local_no_connection));
        }
    } else {
        stopServer();
    }
}

    }

    private BroadcastReceiver mWifiReceiver = new  BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectionUtils.isConnectedToLocalNetwork(context)){
                setText(warning, "");
            }
            else{
                stopServer();
                setStatus(false);
                setText(address, "");
                setText(warning, getString(R.string.local_no_connection));
            }
        }
    };

    private BroadcastReceiver mFtpReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action == ACTION_FTPSERVER_STARTED) {
            setStatus(true);
        }
        else if(action == ACTION_FTPSERVER_FAILEDTOSTART){
            setStatus(false);
            setText(warning, "Oops! Something went wrong");
        }
        else if(action == ACTION_FTPSERVER_STOPPED){
            setStatus(false);
        }
        }
    };

    private void setTintedImage(ImageView imageview, int resourceId){
        imageview.setImageDrawable(IconUtils.applyTintAttr(getActivity(), resourceId,
                android.R.attr.textColorPrimary));
    }

    private void setText(TextView textView, String text){
        textView.setText(text);
        textView.setVisibility(TextUtils.isEmpty(text) ? View.GONE : View.VISIBLE);
    }

    public void showHelp(){
        new AlertDialog.Builder(getActivity(),
                R.style.AlertDialogStyle)
                .setTitle("How to use Transfer to PC")
                .setMessage(R.string.ftp_server_help_description)
                .setPositiveButton("Got it!", null)
                .show();
    }
}