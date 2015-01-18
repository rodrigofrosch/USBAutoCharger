package devsnewapps.usbautocharger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by rfrosch on 10/01/2015.
 */
public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";
    private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";
    private ServerSocket serverSocket;
    Handler updateConversationHandler;
    Thread serverThread = null;
    private static final int SERVERPORT = 8888;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(ACTION_BOOT)) {

            this.serverThread = new Thread(new ServerThread());
            this.serverThread.start();

            Intent monitorIntent = new Intent(context, BatteryService.class);
            context.startService(monitorIntent);

        }
    }

    private class ServerThread implements Runnable {



        @Override
        public void run() {
            Socket socket = null;
            try {
                serverSocket = new ServerSocket(SERVERPORT);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    socket = serverSocket.accept();
                    CommunicationThread communicationThread = new CommunicationThread(socket);
                    new Thread(communicationThread).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private BufferedReader input;
        public CommunicationThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String read = input.readLine();
                    String header = "";
                    String args = "";
                    updateConversationHandler.post(new UpdateUIThread(header, args));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    private class UpdateUIThread implements Runnable {
        private String header;
        private String args;
        public UpdateUIThread(String header, String args) {
            this.header = header;
            this.args = args;
        }
        @Override
        public void run() {
            switch (header) {
                case "SMS":
                    //Sending SMS
                    switch (args) {
                        case "1":
                            //case code 1 sending conclusion for processing
                            break;
                    }
                    break;
            }
        }
    }
}