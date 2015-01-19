package com.alex.moblieDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.*;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.*;
import utility.Counter;
import utility.CounterTask;
import utility.Timer;
import utility.TimerTask;

import java.util.*;

/**
 * Created by Alex on 14-8-27.
 */
public class MainActivity extends Activity {
    private boolean isBond = false;
    private boolean isConnected = false;
    private boolean hasDataToSend = false;
    private boolean hasDataToReceive = false;
    private boolean firstScanToSend = true;//the first time scan finding have data to send
    private boolean firstScanToReceive = true;//the first time scan finding have data to send
    private SimpleAdapter simpleAdapter;
    private ListView listView;
    private Vibrator vibrator;
    private Handler handler;
    private Runnable runnable;
    private List<Map<String, Object>> list;
    private List<Integer> notFindDeviceIndex;//during one search that devices don't find compared to last search
    private List<String> receivedData;
    private AlertDialog dialog = null;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothGattCallback gattCallback;
    private BluetoothAdapter mAdapter;
    private BluetoothGatt bluetoothGatt = null;
    private Map<BluetoothDevice, Queue<String>> bondDevices;
    private Map<BluetoothDevice, Queue<String>> sleepDevices;
    private Counter connectCounter;
    private Counter dataTransferCounter;
    private utility.Timer dataTransferTimer;
    private final String SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    private final String CHARACTERISTIC_WRITE_UUID = "0000fff6-0000-1000-8000-00805f9b34fb";
    private final String CHARACTERISTIC_READ_UUID = "0000fff7-0000-1000-8000-00805f9b34fb";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        bondDevices = new HashMap<>();
        sleepDevices = new HashMap<>();
        handler = new Handler();
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        listView = (ListView) findViewById(R.id.device_list);
        listView.setOnItemClickListener(new DeviceClickListener());
        Button applyBondBTN = (Button) findViewById(R.id.apply_bond);
        list = new ArrayList<Map<String, Object>>();
        notFindDeviceIndex = new ArrayList<>();
        receivedData = new ArrayList<>();
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mAdapter = bluetoothManager.getAdapter();
        //find a device
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                    @Override
                    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (!isBond) {
                                    String hexRecord = bytesToHexString(scanRecord);
                                    int idx = indexInList(device);
                                    if (idx == -1) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("image", R.drawable.ic_launcher);
                                        map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + hexRecord);
                                        map.put("device", device);
                                        list.add(map);
                                    } else {
                                        Map<String, Object> map = list.get(idx);
                                        map.put("image", R.drawable.ic_launcher);
                                        map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + hexRecord);
                                        map.put("device", device);
                                        notFindDeviceIndex.remove(Integer.valueOf(idx));
                                    }
                                } else {
                                    if (bondDevices.containsKey(device)) {
                                        String hexRecord = bytesToHexString(scanRecord);
                                        //check record that whether have data to receive
                                        if (hexRecord.substring(14, 16).equals("01")) { //no data to receive
                                            if (bondDevices.get(device).size() == 0) {//no data to send
                                                int idx = indexInList(device);
                                                if (idx == -1) {
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("image", R.drawable.ic_launcher);
                                                    map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + hexRecord);
                                                    map.put("device", device);
                                                    list.add(map);
                                                } else {
                                                    Map<String, Object> map = list.get(idx);
                                                    map.put("image", R.drawable.ic_launcher);
                                                    map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + hexRecord);
                                                    map.put("device", device);
                                                    notFindDeviceIndex.remove(Integer.valueOf(idx));
                                                }
                                            } else {//have data to send
                                                if (firstScanToSend) {
                                                    firstScanToSend = false;
                                                    Log.d("service", "dataList size: " + bondDevices.get(device).size() + " data: " + bondDevices.get(device).peek());
                                                    list.clear();
                                                    Map<String, Object> map = new HashMap<>();
                                                    map.put("image", R.drawable.ic_launcher);
                                                    map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + "正在发送数据");
                                                    map.put("device", device);
                                                    list.add(map);
                                                    simpleAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.item, new String[]{"image", "info"}, new int[]{R.id.device_image, R.id.device_info});
                                                    listView.setAdapter(simpleAdapter);
                                                    connect(device);
                                                }
                                            }
                                        } else if (hexRecord.substring(14, 16).equals("02")) { //have data to receive
                                            if (firstScanToReceive) {
                                                firstScanToReceive = false;
                                                list.clear();
                                                Map<String, Object> map = new HashMap<>();
                                                map.put("image", R.drawable.ic_launcher);
                                                map.put("info", device.getName() + "\n" + device.getAddress() + "\n" + rssi + "\n" + "正在接收数据");
                                                map.put("device", device);
                                                list.add(map);
                                                simpleAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.item, new String[]{"image", "info"}, new int[]{R.id.device_image, R.id.device_info});
                                                listView.setAdapter(simpleAdapter);
                                                connect(device);
                                            }
                                        } else if (hexRecord.substring(14, 16).equals("03")) { //anchor point switch to sleep state
                                            Queue<String> strings = bondDevices.get(device);
                                            sleepDevices.put(device, strings);
                                            bondDevices.remove(device);
                                        }
                                    } else if (sleepDevices.containsKey(device)) {
                                        String hexRecord = bytesToHexString(scanRecord);
                                        String state = hexRecord.substring(14, 16);
                                        if (state.equals("01") || state.equals("02")) {
                                            Queue<String> strings = sleepDevices.get(device);
                                            bondDevices.put(device, strings);
                                            sleepDevices.remove(device);
                                        }
                                    }
                                }
                            }
                        });
                    }
                };
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                String data = characteristic.getStringValue(0);
                Log.d("service", "readStatus: " + status);
                Log.d("service", "data: " + data);
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_WRITE_UUID));
                if (characteristic.equals(writeCharacteristic)) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (data.equals("00000000000000000000")) {
                            dataTransferCounter.reset();
                            Queue<String> dataQueue = bondDevices.get(gatt.getDevice());
                            dataQueue.poll();
                            sendNextPacket();
                        } else {
                            dataTransferCounter.addCount();
                            gatt.readCharacteristic(characteristic);
                        }
                    } else {
                        //TODO here need to process death loop situation
                        gatt.readCharacteristic(characteristic);
                    }
                } else {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        Log.d("service", "test");
                        if (data.equals("00000000000000000000")) {
                            Log.d("service", "test1");
                            hasDataToReceive = false;
                            if (!hasDataToSend) {
                                Log.d("service", "test2");
                                for (String d : receivedData)
                                    Log.d("service", "final data1: " + d);
                                disconnect();
                            }
                        } else {//TODO here need to check whether 0*18 is a possible data and process received data
                            hasDataToReceive = true;
                            String validData = data.substring(0, 18);
                            String hasNext = data.substring(18);
                            //process data
                            Log.d("service", "valid: " + validData);
                            receivedData.add(validData);
                            if (hasNext.equals("01")) {//has next packet to receive
                                characteristic.setValue("00000000000000000000");
                                gatt.writeCharacteristic(characteristic);
                            } else if (hasNext.equals("00")) {//last packet
                                hasDataToReceive = false;
                                if (!hasDataToSend) {
                                    for (String d : receivedData)
                                        Log.d("service", "final data: " + d);
                                    disconnect();
                                } else {
                                    characteristic.setValue("00000000000000000000");
                                    gatt.writeCharacteristic(characteristic);
                                }
                            }
                        }
                    } else {
                        //TODO here need to process death loop situation
                        gatt.readCharacteristic(characteristic);
                    }
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.d("service", "writeStatus: " + status);
                Log.d("service", "data: " + characteristic.getStringValue(0));
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_WRITE_UUID));
                if (characteristic.equals(writeCharacteristic)) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.readCharacteristic(characteristic);
                    } else {
                        dataTransferCounter.addCount();
                        sendNextPacket();
                    }
                } else {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        gatt.readCharacteristic(characteristic);
                    } else {
                        //TODO may be here to process write too much time in death loop
                        characteristic.setValue("00000000000000000000");
                        gatt.writeCharacteristic(characteristic);
                    }
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d("service", "serviceDiscoverStatus: " + status);
                BluetoothGattService service = gatt.getService(UUID.fromString(SERVICE_UUID));
                BluetoothGattCharacteristic readCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_READ_UUID));
                BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_WRITE_UUID));
                gatt.readCharacteristic(readCharacteristic);
                Queue<String> dataQueue = bondDevices.get(gatt.getDevice());
                if (dataQueue.size() != 0) {
                    String data = dataQueue.peek();
                    writeCharacteristic.setValue(data);
//                    Log.d("service", "begin: " + gatt.beginReliableWrite());
                    dataTransferCounter = new Counter(5, new CounterTask() {
                        @Override
                        public void run() {
                            Log.e("service", "data transfer Counter over count\n" + "data: " + bondDevices.get(bluetoothGatt.getDevice()).poll());
                            sendNextPacket();
                        }
                    });
                    dataTransferCounter.start();
                    gatt.writeCharacteristic(writeCharacteristic);
                    dataTransferTimer = new Timer(5000, new TimerTask() {
                        @Override
                        public void run() {
                            Log.d("service", "over time");
                            dataTransferCounter.addCount();
                            sendNextPacket();
                        }
                    });
                    dataTransferTimer.start();
//                    Log.d("service", "execute: " + gatt.executeReliableWrite());
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d("service", "old: " + status);
                Log.d("service", "new: " + newState);
                if (newState == BluetoothGatt.STATE_DISCONNECTED && status == BluetoothGatt.STATE_DISCONNECTED) {
                    Log.d("service", "connect failed");
                    connectCounter.addCount();
                    bluetoothGatt.getDevice().connectGatt(MainActivity.this, true, gattCallback);
                }
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    isConnected = true;
                    connectCounter.reset();
                    Log.d("service", "" + gatt.discoverServices());
                }
            }

            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
                Log.d("service", "reliableWriteStatus: " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {
//                    bondDevices.get(gatt.getDevice()).poll();
                }
            }
        };
        applyBondBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBond) {
                    //release bond
                    applyBondBTN.setText("开启绑定");
                    isBond = false;
                    list.clear();
                    notFindDeviceIndex.clear();
                    vibrator.cancel();
                } else {
                    //apply bond
                    applyBondBTN.setText("解除绑定");
                    isBond = true;
                    list.clear();
                    notFindDeviceIndex.clear();
                }
            }
        });
        runnable = new Runnable() {
            @Override
            public void run() {
                if (notFindDeviceIndex.size() > 0) {
                    for (int i = notFindDeviceIndex.size() - 1; i >= 0; --i) {
                        list.remove(notFindDeviceIndex.get(i).intValue());
                    }
                }
                notFindDeviceIndex = new ArrayList<>();
                for (int i = 0; i < list.size(); ++i)
                    notFindDeviceIndex.add(i);
                simpleAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.item, new String[]{"image", "info"}, new int[]{R.id.device_image, R.id.device_info});
                listView.setAdapter(simpleAdapter);
                if (dialog != null) {
                    dialog.dismiss();
                    vibrator.cancel();
                    dialog = null;
                }
                if (isBond && list.size() != bondDevices.size()) {
                    long[] pattern = {1000, 1000};
                    vibrator.vibrate(pattern, 0);
                    dialog = new AlertDialog.Builder(MainActivity.this).setTitle("警告！！！").setMessage("设备丢失！！！").show();
                }
                mAdapter.stopLeScan(mLeScanCallback);
                search();
                handler.postDelayed(this, 1000);
            }
        };
        search();
        handler.postDelayed(runnable, 1000);
        tabHost.setup();
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("个人信息").setContent(R.id.tab1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("设备列表").setContent(R.id.tab2));
        tabHost.getTabWidget().getChildTabViewAt(0).setPadding(0, 0, 0, 30);
        tabHost.getTabWidget().getChildTabViewAt(1).setPadding(0, 0, 0, 30);
    }

    private void search() {
        if (!mAdapter.isEnabled())
            mAdapter.enable();
        mAdapter.startLeScan(mLeScanCallback);
    }

    private void connect(BluetoothDevice device) {
        handler.removeCallbacks(runnable);
        mAdapter.stopLeScan(mLeScanCallback);
        connectCounter = new Counter(5, new CounterTask() {
            @Override
            public void run() {
                Log.e("service", "connect Counter over count");
                search();
                handler.postDelayed(runnable, 1000);
            }
        });
        connectCounter.start();
        connectCounter.addCount();
        connectCounter.addCount();
        connectCounter.addCount();
        connectCounter.addCount();
        connectCounter.addCount();
        connectCounter.addCount();
        bluetoothGatt = device.connectGatt(MainActivity.this, true, gattCallback);
    }

    private void disconnect() {
        if (bluetoothGatt == null)
            return;
//        bluetoothGatt.disconnect();
        bluetoothGatt.close();
        bluetoothGatt = null;
        isConnected = false;
        firstScanToSend = true;
        firstScanToReceive = true;
        search();
        handler.postDelayed(runnable, 1000);
    }

    private class DeviceClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (isBond) {
                if (!isConnected) {
                    new AlertDialog.Builder(MainActivity.this).setTitle(((BluetoothDevice) list.get(position).get("device")).getName()).setMessage("是否连接？").setPositiveButton("连接", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Map<String, Object> map = new HashMap<>();
                            map.put("image", R.drawable.ic_launcher);
                            map.put("info", ((BluetoothDevice) list.get(position).get("device")).getName() + "\n" + ((BluetoothDevice) list.get(position).get("device")) + "\n" + "连接中");
                            map.put("device", ((BluetoothDevice) list.get(position).get("device")));
                            list.clear();
                            list.add(map);
                            simpleAdapter = new SimpleAdapter(MainActivity.this, list, R.layout.item, new String[]{"image", "info"}, new int[]{R.id.device_image, R.id.device_info});
                            listView.setAdapter(simpleAdapter);
                            connect((BluetoothDevice) list.get(position).get("device"));
                        }
                    }).setNegativeButton("发送数据", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText editText = new EditText(MainActivity.this);
                            new AlertDialog.Builder(MainActivity.this).setTitle("请输入数据").setIcon(android.R.drawable.ic_dialog_info).setView(editText).setPositiveButton("发送", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Queue<String> queue = bondDevices.get(list.get(position).get("device"));
                                    String s = editText.getText().toString();
                                    if (s.length() == 0)
                                        Toast.makeText(MainActivity.this, "输入不能为空！", Toast.LENGTH_LONG).show();
                                    else if (s.length() <= 20) {
                                        while (s.length() != 20)
                                            s += "0";
                                        queue.add(s);
                                        hasDataToSend = true;
                                        bondDevices.put((BluetoothDevice) list.get(position).get("device"), queue);
                                    } else {
                                        Toast.makeText(MainActivity.this, "输入长度需小于等于20！", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    }).show();
                } else {
                    new AlertDialog.Builder(MainActivity.this).setTitle(((BluetoothDevice) list.get(position).get("device")).getName()).setMessage("是否连接？").setPositiveButton("断开", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            disconnect();
                        }
                    }).setNegativeButton("发送数据", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EditText editText = new EditText(MainActivity.this);
                            new AlertDialog.Builder(MainActivity.this).setTitle("请输入数据").setIcon(android.R.drawable.ic_dialog_info).setView(editText).setPositiveButton("发送", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Queue<String> queue = bondDevices.get(list.get(position).get("device"));
                                    String s = editText.getText().toString();
                                    if (s.length() == 0)
                                        Toast.makeText(MainActivity.this, "输入不能为空！", Toast.LENGTH_LONG).show();
                                    else if (s.length() <= 20) {
                                        while (s.length() != 20)
                                            s += "0";
                                        queue.add(s);
                                        hasDataToSend = true;
                                        bondDevices.put((BluetoothDevice) list.get(position).get("device"), queue);
                                    } else {
                                        Toast.makeText(MainActivity.this, "输入长度需小于等于20！", Toast.LENGTH_LONG).show();
                                    }
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            }).show();
                        }
                    }).show();
                }
            }
            else
                new AlertDialog.Builder(MainActivity.this).setTitle(((BluetoothDevice) list.get(position).get("device")).getName()).setMessage("是否绑定？").setPositiveButton("绑定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (bondDevices.containsKey((BluetoothDevice) list.get(position).get("device")))
                            Toast.makeText(MainActivity.this, "该设备已经被绑定！", Toast.LENGTH_SHORT).show();
                        else {
                            bondDevices.put((BluetoothDevice) list.get(position).get("device"), new ArrayDeque<>());
                            Toast.makeText(MainActivity.this, "绑定成功！", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton("解绑", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (bondDevices.containsKey((BluetoothDevice) list.get(position).get("device"))) {
                            bondDevices.remove((BluetoothDevice) list.get(position).get("device"));
                            Toast.makeText(MainActivity.this, "解绑定成功！", Toast.LENGTH_SHORT).show();
                        } else
                            Toast.makeText(MainActivity.this, "未绑定该设备！", Toast.LENGTH_SHORT).show();
                    }
                }).show();
        }
    }

    private String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    private byte[] hexStringToBytes(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] chars = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(chars[pos]) << 4 | toByte(chars[pos + 1]));
        }
        return result;
    }

    private byte toByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    private void sendNextPacket() {
        Queue<String> dataQueue = bondDevices.get(bluetoothGatt.getDevice());
        if (dataQueue.size() != 0) {
            BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
            BluetoothGattCharacteristic writeCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_WRITE_UUID));
            String data = dataQueue.peek();
            writeCharacteristic.setValue(data);
            bluetoothGatt.writeCharacteristic(writeCharacteristic);
            dataTransferTimer.reset();
        } else {
            hasDataToSend = false;
            dataTransferTimer.close();
            if (!hasDataToReceive) {
                BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
                BluetoothGattCharacteristic readCharacteristic = service.getCharacteristic(UUID.fromString(CHARACTERISTIC_READ_UUID));
                bluetoothGatt.readCharacteristic(readCharacteristic);
            }
        }
    }

    private int indexInList(BluetoothDevice device) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i).containsValue(device))
                return i;
        }
        return -1;
    }
}
