package com.gathin.tvhome;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;

/**
 * Direct AIDL bridge to WhaleService (com.droidlogic / com.zeasn.whaleservice.WhaleService).
 * This service runs under system UID 1000 and has direct access to the Amlogic hardware
 * to execute real lamp shutdown and hardware power state transitions.
 */
public final class WhaleHardwareHelper {
    private static final String TAG = "WhaleHardware";
    private static final String SERVICE_ACTION = "com.zeasn.TV_SERVICE";
    private static final String SERVICE_PACKAGE = "com.droidlogic";
    private static final String DESCRIPTOR = "com.zeasn.zeasnlib.IWhaleServiceManager";

    // AIDL Transaction Codes in IWhaleServiceManager$Stub
    private static final int TRANSACTION_SleepTimeSet = 2;
    private static final int TRANSACTION_eventSystemSleepTimeSetSleepTimeOff = 10;

    /**
     * Set the native hardware sleep timer in minutes (e.g. 5, 10, 15, 30, 45, 60, 120, 180).
     * WhaleService will handle the hardware shutdown when the timer expires.
     */
    public static void setHardwareSleepTimer(Context context, int minutes) {
        if (context == null) return;
        try {
            Intent intent = new Intent(SERVICE_ACTION);
            intent.setPackage(SERVICE_PACKAGE);
            context.getApplicationContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        try {
                            data.writeInterfaceToken(DESCRIPTOR);
                            data.writeInt(minutes);
                            service.transact(TRANSACTION_SleepTimeSet, data, reply, 0);
                            reply.readException();
                            Log.d(TAG, "Hardware SleepTimeSet called successfully with " + minutes + " min");
                        } finally {
                            data.recycle();
                            reply.recycle();
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Failed calling SleepTimeSet on WhaleService", t);
                    } finally {
                        try {
                            context.getApplicationContext().unbindService(this);
                        } catch (Throwable ignored) {}
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {}
            }, Context.BIND_AUTO_CREATE);
        } catch (Throwable t) {
            Log.e(TAG, "Error binding to WhaleService", t);
        }
    }

    /**
     * Cancel the native hardware sleep timer.
     */
    public static void cancelHardwareSleepTimer(Context context) {
        if (context == null) return;
        try {
            Intent intent = new Intent(SERVICE_ACTION);
            intent.setPackage(SERVICE_PACKAGE);
            context.getApplicationContext().bindService(intent, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    try {
                        Parcel data = Parcel.obtain();
                        Parcel reply = Parcel.obtain();
                        try {
                            data.writeInterfaceToken(DESCRIPTOR);
                            data.writeInt(0);
                            service.transact(TRANSACTION_SleepTimeSet, data, reply, 0);
                            reply.readException();
                        } finally {
                            data.recycle();
                            reply.recycle();
                        }

                        // Also invoke eventSystemSleepTimeSetSleepTimeOff
                        Parcel dataOff = Parcel.obtain();
                        Parcel replyOff = Parcel.obtain();
                        try {
                            dataOff.writeInterfaceToken(DESCRIPTOR);
                            service.transact(TRANSACTION_eventSystemSleepTimeSetSleepTimeOff, dataOff, replyOff, 0);
                            replyOff.readException();
                            Log.d(TAG, "Hardware SleepTime set to OFF successfully");
                        } finally {
                            dataOff.recycle();
                            replyOff.recycle();
                        }
                    } catch (Throwable t) {
                        Log.e(TAG, "Failed cancelling SleepTimer on WhaleService", t);
                    } finally {
                        try {
                            context.getApplicationContext().unbindService(this);
                        } catch (Throwable ignored) {}
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {}
            }, Context.BIND_AUTO_CREATE);
        } catch (Throwable t) {
            Log.e(TAG, "Error binding to cancel WhaleService", t);
        }
    }
}
