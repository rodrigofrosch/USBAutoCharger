package devsnewapps.usbautocharger;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by rfrosch on 10/01/2015.
 */
public class BatteryService extends Service{

    private static final String IP = ""; //
    private static final int PORT = 8887;
    private static final String MSG_BATTERY_DANGER = "";
    private static final String MSG_BATTERY_OKAY = "";
    private static final int TYPE_DANGER = 0;
    private static final int TYPE_OKAY = 1;

    private float levelBattery;
    private int scale;
    private Intent batteryStatus;
    private IntentFilter ifilter;
    private static  String TAG = "BatteryService";

    private ServerSocket serverSocket;
    private Thread communicationThread = null;
    private static final int SERVERPORT = 0;


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        BatteryCheckAsync checkAsync;
        checkAsync = new BatteryCheckAsync();
        checkAsync.execute();

        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class BatteryCheckAsync extends AsyncTask<Void, Void, Float> {



        @Override
        protected Float doInBackground(Void... arg0) {
            //Battery State check - create log entries of current battery state
            ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            batteryStatus = BatteryService.this.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;
            Log.i("BatteryInfo", "Battery is charging: " + isCharging);

            levelBattery = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            Log.i("BatteryInfo", "Battery charge level: " + (levelBattery / scale));
            Float aux = (levelBattery / scale);

            if (aux <= 0.05){
                communicationThread = new Thread(new CommunicationThread(TYPE_OKAY));
                if (communicationThread.isInterrupted()){
                    communicationThread.start();
                } else {
                    communicationThread.interrupt();
                    communicationThread.start();
                }
            }

            if (aux <= 1){
                communicationThread = new Thread(new CommunicationThread(TYPE_OKAY));
                if (communicationThread.isInterrupted()){
                    communicationThread.start();
                } else {
                    communicationThread.interrupt();
                    communicationThread.start();
                }
            }
            return levelBattery;
        }

        protected void onPostExecute(){
            //BatteryService.this.stopSelf();
        }
    }

    private class CommunicationThread implements Runnable{
        private Socket clientSocket;
        private DataOutputStream dataOutputStream;
        private DataInputStream dataInputStream;
        private int type;
        public CommunicationThread(int type) {
            this.type = type;
        }
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    clientSocket = new Socket(IP, PORT);
                    switch (type){
                        case TYPE_DANGER:
                            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                            dataInputStream = new DataInputStream(clientSocket.getInputStream());
                            SystemClock.sleep(500);
                            dataOutputStream.writeUTF(MSG_BATTERY_DANGER);
                            SystemClock.sleep(500);
                            Log.d(TAG, String.valueOf(dataInputStream));
                            SystemClock.sleep(500);
                            dataInputStream = new DataInputStream(clientSocket.getInputStream());
                            Log.d(TAG, String.valueOf(dataInputStream));
                            SystemClock.sleep(500);
                            break;
                        case TYPE_OKAY:
                            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                            dataInputStream = new DataInputStream(clientSocket.getInputStream());
                            SystemClock.sleep(500);
                            dataOutputStream.writeUTF(MSG_BATTERY_OKAY);
                            SystemClock.sleep(500);
                            Log.d(TAG, String.valueOf(dataInputStream));
                            SystemClock.sleep(500);
                            dataInputStream = new DataInputStream(clientSocket.getInputStream());
                            Log.d(TAG, String.valueOf(dataInputStream));
                            SystemClock.sleep(500);
                            break;
                    }


                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {
                    if (clientSocket != null) {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (dataOutputStream != null) {
                        try {
                            dataOutputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    if (dataInputStream != null) {
                        try {
                            dataInputStream.close();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }
}
