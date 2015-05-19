package promethium.hyms.com.primavitabless;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;


public class BluetoothActivity extends ActionBarActivity {

    private Button bluetoothOn;
    private Button bluetoothOff;
    private Button bluetoothScan;
    private ArrayAdapter<String> btArrayAdapter;
    private ListView listDevicesFound;
    private Button bluetoothPaired;
    private Set<BluetoothDevice> pairedDevices;
    private BluetoothAdapter myBluetoothAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        listDevicesFound = (ListView)findViewById(R.id.devicesfound);
        btArrayAdapter = new ArrayAdapter<String>(BluetoothActivity.this, android.R.layout.simple_list_item_1);

        listDevicesFound.setAdapter(btArrayAdapter);

        registerReceiver(myBluetoothReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        bluetoothOn = (Button) findViewById(R.id.btnOn);
        bluetoothOff = (Button) findViewById(R.id.btnOff);
        bluetoothScan = (Button) findViewById(R.id.btnScan);

        bluetoothPaired = (Button) findViewById(R.id.btnPaired);

        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothScan.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                btArrayAdapter.clear();
                myBluetoothAdapter.startDiscovery();
            }
        });

        bluetoothOn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (!myBluetoothAdapter.isEnabled()) {
                    Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(turnOn, 0);

                    myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    Toast.makeText(getApplicationContext(), "Turned on"
                            , Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Already on",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        bluetoothOff.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                myBluetoothAdapter.disable();
                Toast.makeText(getApplicationContext(), "Turned off",
                        Toast.LENGTH_LONG).show();
            }
        });


        bluetoothPaired.setOnClickListener(new View.OnClickListener() {

            @SuppressWarnings("unchecked")
            @Override
            public void onClick(View v) {

                pairedDevices = myBluetoothAdapter.getBondedDevices();


                for(BluetoothDevice bt : pairedDevices)
                    btArrayAdapter.add(bt.getName() + "\n" + bt.getAddress());

                Toast.makeText(getApplicationContext(),"Showing Paired Devices",
                        Toast.LENGTH_SHORT).show();

                //final ArrayAdapter adapter = new ArrayAdapter(BluetoothActivity.this,android.R.layout.simple_list_item_1, list);
                //listDevicesFound.setAdapter(adapter);
            }
        });


        listDevicesFound.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3){
                String[] device_info = listDevicesFound.getItemAtPosition(arg2).toString().split("\n");
                Intent connectToDevice = new Intent(BluetoothActivity.this, BluetoothConnectActivity.class);
                connectToDevice.putExtra(BluetoothConnectActivity.EXTRA_DEVICE_NAME, device_info[0]);
                connectToDevice.putExtra(BluetoothConnectActivity.EXTRA_DEVICE_ADDRESS, device_info[1]);
                myBluetoothAdapter.cancelDiscovery();
                startActivity(connectToDevice);
            }

        });

    }




    private final BroadcastReceiver myBluetoothReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                btArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                btArrayAdapter.notifyDataSetChanged();
            }
        }};

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        unregisterReceiver(myBluetoothReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_bluetooth, menu);
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
}
