package com.hoanvo.vongtaythongminh;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hoanvo.vongtaythongminh.API.APIManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("Registered")
public class HandlerBT extends AppCompatActivity {
    public static String mydevice;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket msocket;
    private InputStream mmInStream;
    private OutputStream mmOutStream;
    private Handler handler;
    private TextView txtnhiptim, txtnietdo, txtcao, txtapsuat;
    private DrawerLayout drawerLayout;

    private ListView lvBT;
    private List<String> listBT;
    private BluetoothAdapter adapterBT;
    private ArrayAdapter<String> adapterLVBT;
    private TelephonyManager tManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_one);
        //check permission
        if (ContextCompat.checkSelfPermission(HandlerBT.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(HandlerBT.this, Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(HandlerBT.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            } else {
                ActivityCompat.requestPermissions(HandlerBT.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 1);
            }
        }
        tManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //start service
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Objects.requireNonNull(tManager).listen(new PhoneStateListener() {
                    @Override
                    public void onCallStateChanged(int state, String incomingNumber) {
                        super.onCallStateChanged(state, incomingNumber);
                        switch (state) {
                            case TelephonyManager.CALL_STATE_RINGING:
                                Log.d("callring", "onCallStateChanged:calll ");
                                if (msocket != null) {
                                    String str = "l";
                                    try {
                                        mmOutStream.write(str.getBytes());
                                        Log.d("call ring", "onCallStateChanged:calll " + str);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                }, PhoneStateListener.LISTEN_CALL_STATE);


                //
            }
        });

        addControl();
        addEvent();
        //code set background k quan trong
        if (getSupportActionBar() != null) {
            Drawable drawable = getResources().getDrawable(R.drawable.ic_menu_white_24dp);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            Drawable newdrawable = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(newdrawable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (msocket != null)
            disconnectDevice();// ngat ket noi bluetooth
    }

    private void addEvent() {
        //quet bluetooth da ket noi trong may
        findViewById(R.id.btnKetNoi).setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                if (listBT == null) {
                    listBT = new ArrayList<>();
                } else if (!adapterBT.isEnabled()) {
                    Intent enableBt = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBt, 22);
                } else {
                    listBT.clear();
                    Set<BluetoothDevice> connDevices = adapterBT.getBondedDevices();
                    for (BluetoothDevice device : connDevices) {
                        listBT.add(device.getAddress());
                    }
                    adapterLVBT.notifyDataSetChanged();
                }

            }
        });


        //nhan vao bluetooth de ket noi toi bluet can ket noi
        lvBT.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                drawerLayout.closeDrawer(GravityCompat.START);
                connectDevice(listBT.get(position));
                ((TextView)findViewById(R.id.macbt)).setText(listBT.get(position));
            }
        });
        handler = new Hauler(txtnhiptim, txtnietdo, txtcao, txtapsuat);

    }


    //code xu ly ben he thong k quan trong
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    //ham nay de lay cac bien ben giao dien
    private void addControl() {
        txtnhiptim = findViewById(R.id.nhiptim);
        txtnietdo = findViewById(R.id.nhietdo);
        txtcao = findViewById(R.id.docao);
        txtapsuat = findViewById(R.id.apxuat);
        drawerLayout = findViewById(R.id.drawerLayout);
        lvBT = findViewById(R.id.lvBT);
        listBT = new ArrayList<>();
        adapterLVBT = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listBT);
        lvBT.setAdapter(adapterLVBT);
        adapterBT = BluetoothAdapter.getDefaultAdapter();
    }

    //ham de ket noi bluetooth mo socket nhan du lieu
    private void connectDevice(String strDevice) {
        try {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            BluetoothDevice mDevice = bluetoothAdapter.getRemoteDevice(strDevice);
            msocket = mDevice.createInsecureRfcommSocketToServiceRecord(MY_UUID);
            BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
            msocket.connect();
            mmInStream = msocket.getInputStream();
            mmOutStream = msocket.getOutputStream();
            mydevice = strDevice;
            new HandlerThreadBT(mmInStream, handler).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
// ngat ket noi toi bluettoh
    private void disconnectDevice() {
        try {
            msocket.close();
            mmInStream.close();
            mmOutStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class HandlerThreadBT extends Thread {
        private InputStream istream;
        private Handler handler;

        HandlerThreadBT(InputStream istream, Handler handler) {
            this.istream = istream;
            this.handler = handler;
        }


        //ham nhan data tu bluetooth
        @Override
        public void run() {
            byte[] buffer = new byte[512];
            int bytes;
            while (true) {
                // Read from the InputStream
                try {
                    bytes = istream.read(buffer);
                    handler.obtainMessage(123, bytes, -1, buffer).sendToTarget();
                    sleep(1000);
                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }


            }
        }
    }

    class Hauler extends Handler {
        private static final String TAG = "text";
        private StringBuilder sb = new StringBuilder();
        private TextView txtnhiptim;
        private TextView txtnhiet;
        private TextView txtcao;
        private TextView txtap;

        Hauler(TextView txtnhiptim, TextView txtnhiet, TextView txtcao, TextView txtap) {
            this.txtnhiptim = txtnhiptim;
            this.txtnhiet = txtnhiet;
            this.txtcao = txtcao;
            this.txtap = txtap;

        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 123:
                    byte[] readBuf = (byte[]) msg.obj;
                    String strIncom = new String(readBuf, 0, msg.arg1);
                    sb.append(strIncom);
                    int endOfLineIndex = sb.indexOf("\r\n");                          // determine the end-of-line
                    if (endOfLineIndex > 0) {                                            // if end-of-line,
                        String sbprint = sb.substring(0, endOfLineIndex);               // extract string
                        sb.delete(0, sb.length());                                      // and clear
                        Log.d(TAG, "handleMessage: " + sbprint);
                        if (sbprint.substring(0, 1).equals("H")) {
                            txtnhiet.setText(sbprint.replace("H", ""));//set gia tri cho cac bien nhan dduoc
                        }
                        if (sbprint.substring(0, 1).equals("T"))
                            txtnhiptim.setText(sbprint.replace("T", ""));//tuong tu
                        if (sbprint.substring(0, 1).equals("A"))
                            txtcao.setText(sbprint.replace("A", ""));
                        if (sbprint.substring(0, 1).equals("P"))
                            txtap.setText(sbprint.replace("P", ""));
                    }

                    break;
            }
        }
    }
}
