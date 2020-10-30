package material.hunter.GPS;

import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Objects;

import material.hunter.utils.NhPaths;
import material.hunter.utils.ShellExecuter;

public class GpsdServer extends AsyncTask<Void, Void, Void> {
    private static final String SCRIPT_PATH = NhPaths.APP_SCRIPTS_PATH;

    private static final String TAG = "GpsdServer";
    /**
     * The TCP/IP port used for Socket communication.
     */
    private static final int PORT = 10110;
    private ConnectionListener listener;

    GpsdServer(ConnectionListener listener) {
        this.listener = listener;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            SocketAddress socketAddress = new InetSocketAddress("127.0.0.1", PORT);
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);

            new Thread(() -> {

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ShellExecuter exe = new ShellExecuter();
                String command = "su -c '" + SCRIPT_PATH + File.separator + "bootkali start_gpsd " + PORT + "'";
                //Log.d(TAG, command);
                String response = exe.RunAsRootOutput(command);
                //Log.d(TAG, "Response = " + response);
            }).start();


            Socket clientSocket = serverSocket.accept();
            listener.onSocketConnected(clientSocket);
            //Log.d(TAG, "Client bound");
        } catch (IOException e) {
            Log.d(TAG, "Unable to create ServerSocket for port: " + PORT);
            Log.d(TAG, Objects.requireNonNull(e.getMessage()));
            return null;
        }

        return null;
    }

    public interface ConnectionListener {
        void onSocketConnected(Socket clientSocket);
    }

}