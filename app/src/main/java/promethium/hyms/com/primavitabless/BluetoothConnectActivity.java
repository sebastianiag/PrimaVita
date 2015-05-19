package promethium.hyms.com.primavitabless;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class BluetoothConnectActivity extends ActionBarActivity {

    //--------------------ACTIVITY CONSTANTS----------------------------
    public static final String EXTRA_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRA_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "bluetoothConnect";
    final int RECEIVE_MESSAGE = 1;
    private int length;
    //------------------------------------------------------------------


    //--------------------BLUETOOTH OBJECTS-----------------------------
    private BluetoothAdapter btAdapter = null;
    //------------------------------------------------------------------


    //--------------------DATA HANDLING OBJECTS-------------------------
    private StringBuilder sb = new StringBuilder();
    TextView txtMSP;
    EditText inputText;
    Button sendButton;
    Handler h;
    private ConnectedThread mConnectedThread;
    private ConnectThread mConnectThread;
    //------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_connect);

        txtMSP = (TextView) findViewById(R.id.txtMSP);      // for display the received data from the MSP430
        inputText = (EditText) findViewById(R.id.writeField);
        sendButton = (Button) findViewById(R.id.sendButton);

        sendButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                String message = inputText.getText().toString();
                if (message.length() > 0) {
                    mConnectedThread.write(message);
                }
            }

        });


        //Handle incoming data from the MSP430
        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                switch (msg.what) {
                    case RECEIVE_MESSAGE:                                                   // if receive massage
                        Log.e(TAG, "am I getting here?");
                        byte[] readBuf = (byte[]) msg.obj;
                        String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
                        Log.e(TAG, strIncom);
                        sb.append(strIncom);                                                // append string
                        int endOfLineIndex = sb.indexOf("\n");                            // determine the end-of-line
                        Log.e("INDEX", Integer.toString(endOfLineIndex));
                        if (endOfLineIndex > 0) {                                            // if end-of-line,
                            String temperature = sb.substring(0, endOfLineIndex);               // extract string
                            Log.e("SO I GOT HERE!", temperature);
                            sb.delete(0, sb.length());                                      // and clear
                            Sample sample = new Sample(Float.parseFloat(temperature.split(":")[1]));
                            sample.save();
                            txtMSP.setText("Data from MSP: " + temperature);            // update TextView
                        }
                        //Log.d(TAG, "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
                        break;
                }
            };
        };

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d(TAG, "...onResume - try connect...");

        Intent intent = getIntent();
        String address = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
        BluetoothDevice device = btAdapter.getRemoteDevice(address);



        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth_connect, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        // Emulator doesn't support Bluetooth and will return null
        if(btAdapter==null) {
            errorExit("Fatal Error", "Bluetooth not support");
        } else {
            if (btAdapter.isEnabled()) {
                Log.d(TAG, "...Bluetooth ON...");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    private void errorExit(String title, String message){
        Toast.makeText(getBaseContext(), title + " - " + message, Toast.LENGTH_LONG).show();
        finish();
    }

    private class ConnectThread extends Thread {
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        public ConnectThread(BluetoothDevice  mDevice){
            BluetoothSocket tmp = null;
            this.mDevice = mDevice;
            try{
                tmp = this.mDevice.createRfcommSocketToServiceRecord(MY_UUID);
            }catch(IOException e){}
            mSocket = tmp;
        }

        public void run(){
            try{
                mSocket.connect();
            }catch (IOException connectException){
                try{
                    mSocket.close();
                }catch (IOException closeException){
                    return;
                }
            }

            mConnectedThread = new ConnectedThread(mSocket);
            mConnectedThread.start();
        }

        public void cancel(){
            try{
                mSocket.close();
            }catch (IOException e){}
        }

    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int begin =0;
            int bytes =0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    Thread.sleep(1);
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"


                    String decoded = new String(buffer);
                    //decoded = decoded.substring(0,1);
                    Log.e(TAG, decoded);
                    h.obtainMessage(RECEIVE_MESSAGE, bytes, length, buffer).sendToTarget(); // Send to message queue Handler

                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String message) {
            Log.d(TAG, "...Data to send: " + message + "...");
            byte[] msgBuffer = message.getBytes();
            try {
                length = message.length();
                Log.e("MESSAGE", new String(msgBuffer));

                mmOutStream.write(msgBuffer);
            } catch (IOException e) {
                Log.d(TAG, "...Error data send: " + e.getMessage() + "...");
            }
        }

        public void cancel() throws IOException {
            mSocket.close();
        }

    }

    /**
    private class DrawThread extends Thread{
        public DrawThread (){
        }

        public void run(){
            List<Sample> samples = Sample.listAll(Sample.class);
            if(samples.isEmpty()){
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            else{

            }
        }
    }
     **/
}
