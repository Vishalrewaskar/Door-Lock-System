package com.project.server.room;

import android.Manifest;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import localDatabase.EventLockDisplayData;
import localDatabase.Locks;
import logicBox.SharedSpace;

/**
 * LockOperate — simplified Bluetooth lock control.
 *
 * Fixes applied vs. the broken replacement file:
 *  1. View IDs corrected to match activity_lock_operate.xml:
 *       lockpic (ImageView), unlock (Button), tv_lockAddress (TextView).
 *       There is no lockBtn / txtStatus in the layout — removed.
 *  2. Menu IDs corrected to match bottom_navigation.xml:
 *       action_lock, action_logs, action_Users.
 *  3. setOnNavigationItemSelectedListener() — correct API for BottomNavigationView.
 *  4. EventLockDisplayData interface requires eventDisplayData() + eventGetAllLockId()
 *       — both implemented; the non-existent onDataReceived() removed.
 *  5. pushLogToFire() and updateLockStateInFirebase() are private instance methods
 *       (SharedSpace has no static helpers — it is an instance-based prefs wrapper).
 *  6. Single toggle-button (operateMode 0 = unlock, 1 = lock) preserved from original.
 */
public class LockOperate extends AppCompatActivity implements EventLockDisplayData {

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String TAG = "bluetooth2";

    private ProgressDialog progressDialog;
    private BluetoothAdapter btAdapter;
    private BluetoothSocket btSocket = null;
    private OutputStream outputStream = null;
    private final AtomicBoolean isConnecting = new AtomicBoolean(false);
    private boolean shouldCloseSocketOnDestroy = false;

    // FIX 1: Correct view IDs from activity_lock_operate.xml
    private TextView cLockAddress;   // R.id.tv_lockAddress
    private ImageView lockView;      // R.id.lockpic
    private Button unlock;           // R.id.unlock  (single toggle button — no separate lockBtn)

    private String address, lockNumber, lockName, lockAddress;
    private SharedSpace sharedSpace;
    private Locks locksLocalDb;
    private int operateMode = 0;     // 0 = ready to unlock, 1 = ready to lock

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_operate);

        // FIX 2 & 3: Correct method name + correct menu IDs handled in listener below
        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        progressDialog = new ProgressDialog(this);
        locksLocalDb = new Locks(LockOperate.this, this);
        sharedSpace = new SharedSpace(LockOperate.this);

        // FIX 1: Bind only IDs that actually exist in the layout
        lockView = findViewById(R.id.lockpic);
        unlock = findViewById(R.id.unlock);
        cLockAddress = findViewById(R.id.tv_lockAddress);

        lockNumber = getIntent().getExtras().getString("lockNumber");
        lockName = getIntent().getExtras().getString("lockName");
        lockAddress = getIntent().getStringExtra("lockAddress");
        cLockAddress.setText(lockAddress);

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Read MAC from Firebase, then connect
        DatabaseReference lockRef = FirebaseDatabase.getInstance()
                .getReference("lockentry")
                .child(lockNumber);

        lockRef.child("mac").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Toast.makeText(LockOperate.this,
                            "MAC address not found in Firebase", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                address = snapshot.getValue(String.class);
                if (address == null || address.trim().isEmpty()) {
                    Toast.makeText(LockOperate.this,
                            "Invalid MAC address in Firebase", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                address = address.trim().toUpperCase();
                Log.d(TAG, "MAC from Firebase: " + address);

                progressDialog.setMessage("Connecting...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                startBluetoothConnection();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                dismissDialogSafely();
                Toast.makeText(LockOperate.this,
                        "Firebase error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        // Single toggle button: first press = Unlock, second press = Lock
        unlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket == null || !btSocket.isConnected()) {
                    Toast.makeText(getApplication(), "Not connected yet", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (operateMode == 0) {
                    // --- UNLOCK ---
                    try {
                        outputStream.write("U".getBytes());
                        Log.d(TAG, "Sent: U (unlock)");
                    } catch (IOException e) {
                        Log.e(TAG, "Error sending unlock command", e);
                        Toast.makeText(getApplication(), "Error sending unlock", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressDialog.setMessage("Unlocking...");
                    progressDialog.show();
                    new Handler().postDelayed(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        lockView.setImageResource(R.drawable.unlock);
                        dismissDialogSafely();
                        // FIX 5: call private instance methods, not static SharedSpace calls
                        pushLogToFire(lockNumber);
                        updateLockStateInFirebase(lockNumber, false);
                        Toast.makeText(getApplication(), "Unlock Successfully", Toast.LENGTH_SHORT).show();
                    }, 3000);
                    operateMode = 1;

                } else {
                    // --- LOCK ---
                    try {
                        outputStream.write("L".getBytes());
                        Log.d(TAG, "Sent: L (lock)");
                    } catch (IOException e) {
                        Log.e(TAG, "Error sending lock command", e);
                        Toast.makeText(getApplication(), "Error sending lock", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    progressDialog.setMessage("Locking...");
                    progressDialog.show();
                    new Handler().postDelayed(() -> {
                        if (isFinishing() || isDestroyed()) return;
                        lockView.setImageResource(R.drawable.lock);
                        dismissDialogSafely();
                        // FIX 5: call private instance methods, not static SharedSpace calls
                        updateLockStateInFirebase(lockNumber, true);
                        Toast.makeText(getApplication(), "Lock Successfully", Toast.LENGTH_SHORT).show();
                        unlock.setEnabled(false);
                        shouldCloseSocketOnDestroy = true;
                        closeSocketSafely();
                    }, 3000);
                    operateMode = 0;
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Bluetooth connection logic
    // -------------------------------------------------------------------------

    private void startBluetoothConnection() {
        if (btSocket != null && btSocket.isConnected()) {
            Log.d(TAG, "Already connected.");
            dismissDialogSafely();
            return;
        }
        if (isConnecting.getAndSet(true)) {
            Log.d(TAG, "Connection already in progress.");
            return;
        }

        new Thread(() -> {
            try {
                // Android 12+: check BLUETOOTH_CONNECT at runtime
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                        ActivityCompat.checkSelfPermission(LockOperate.this,
                                Manifest.permission.BLUETOOTH_CONNECT)
                                != PackageManager.PERMISSION_GRANTED) {
                    runOnUiThread(() -> {
                        dismissDialogSafely();
                        Toast.makeText(LockOperate.this,
                                "BLUETOOTH_CONNECT permission required", Toast.LENGTH_LONG).show();
                    });
                    return;
                }

                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                btAdapter.cancelDiscovery();   // speeds up and stabilises RFCOMM connect

                BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID);
                Log.d(TAG, "Connecting to " + address + "...");
                socket.connect();              // blocking — runs off UI thread
                Log.d(TAG, "Connected to " + address);

                outputStream = socket.getOutputStream();
                btSocket = socket;

                runOnUiThread(() -> {
                    dismissDialogSafely();
                    Toast.makeText(LockOperate.this, "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                Log.e(TAG, "Connect failed: " + e.getMessage(), e);
                runOnUiThread(() -> {
                    dismissDialogSafely();
                    Toast.makeText(LockOperate.this,
                            "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                closeSocketSafely();
            } finally {
                isConnecting.set(false);
            }
        }).start();
    }

    private void dismissDialogSafely() {
        try {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        } catch (Exception ignored) {}
    }

    private void closeSocketSafely() {
        try {
            if (btSocket != null) {
                btSocket.close();
                Log.d(TAG, "Bluetooth socket closed");
            }
        } catch (IOException e) {
            Log.e(TAG, "Could not close socket", e);
        } finally {
            btSocket = null;
            outputStream = null;
        }
    }

    // -------------------------------------------------------------------------
    // Bottom navigation
    // FIX 2 & 3: correct method name and correct menu item IDs
    // -------------------------------------------------------------------------

    private final BottomNavigationView.OnNavigationItemSelectedListener
            mOnNavigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    shouldCloseSocketOnDestroy = true;
                    int id = item.getItemId();
                    // FIX 2: R.id.action_lock (Home/Locks), R.id.action_Users (capital U), R.id.action_logs
                    if (id == R.id.action_lock) {
                        closeSocketSafely();
                        startActivity(new Intent(getApplication(), Home.class));
                        finish();
                        return true;
                    } else if (id == R.id.action_Users) {
                        closeSocketSafely();
                        intentCall(getApplication(), LockUsers.class);
                        return true;
                    } else if (id == R.id.action_logs) {
                        closeSocketSafely();
                        intentCall(getApplication(), LockLog.class);
                        return true;
                    }
                    return false;
                }
            };

    private void intentCall(Context mContext, Class<?> aClass) {
        Intent intent = new Intent(mContext, aClass);
        intent.putExtra("lockNumber", lockNumber);
        intent.putExtra("lockName", lockName);
        intent.putExtra("lockAddress", lockAddress);
        startActivity(intent);
        finish();
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onDestroy() {
        if (shouldCloseSocketOnDestroy) {
            closeSocketSafely();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        closeSocketSafely();
        super.onBackPressed();
    }

    // -------------------------------------------------------------------------
    // FIX 4: EventLockDisplayData interface — correct methods, no onDataReceived()
    // -------------------------------------------------------------------------

    @Override
    public void eventDisplayData(String id, String name, String location) {
        // Not used in this activity
    }

    @Override
    public void eventGetAllLockId(String lockId) {
        // Not used in this activity
    }

    // -------------------------------------------------------------------------
    // FIX 5: Firebase helpers as private instance methods
    // (SharedSpace has no static methods — it is an instance-based prefs wrapper)
    // -------------------------------------------------------------------------

    private void pushLogToFire(String lockNumber) {
        Map<String, Object> logData = new HashMap<>();
        @SuppressWarnings("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("dd MMM yy HH:mm").format(new Date());
        logData.put("name", sharedSpace.getString("name"));
        logData.put("timestamp", timeStamp);
        FirebaseDatabase.getInstance()
                .getReference("logs")
                .child(lockNumber)
                .push()
                .setValue(logData);
        Log.d(TAG, "Log pushed to Firebase for lock: " + lockNumber);
    }

    private void updateLockStateInFirebase(String lockNumber, boolean isLocked) {
        Map<String, Object> stateData = new HashMap<>();
        stateData.put("isLocked", isLocked);
        stateData.put("status", isLocked ? "locked" : "unlocked");
        stateData.put("lastUpdatedBy", sharedSpace.getString("name"));
        stateData.put("lastUpdatedTimestamp", System.currentTimeMillis());
        FirebaseDatabase.getInstance()
                .getReference("lockStates")
                .child(lockNumber)
                .setValue(stateData);
        Log.d(TAG, "Firebase lock state → " + (isLocked ? "locked" : "unlocked"));
    }
}