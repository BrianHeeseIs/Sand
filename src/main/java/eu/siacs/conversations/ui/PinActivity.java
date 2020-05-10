package eu.siacs.conversations.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.securepreferences.SecurePreferences;

import eu.siacs.conversations.R;

public class PinActivity extends AppCompatActivity {
    private static final String TAG = "PinActivity";
    public static final String PASSWORD = "default_password";
    public static final String GREEN = "#007c52";

    private TextView tv_pinInfo, tv_pin_explain;
    private PinLockView pinLockView;
    private PinLockListener pinLockListener;
    private IndicatorDots indicatorDots;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String firstPin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin);

        tv_pinInfo = findViewById(R.id.tv_pin_header);
        tv_pin_explain = findViewById(R.id.tv_pin_explain);
        tv_pin_explain.setText("This PIN is to identify that you are the current user of the device. The PIN will only be stored in your current device.");

        sharedPreferences = new SecurePreferences(this, PASSWORD, "loginInfo.xml");
        editor = sharedPreferences.edit();
        final String storedPinCode = sharedPreferences.getString("pinCode", null);

        pinLockView = findViewById(R.id.pinlock_view);
        indicatorDots = findViewById(R.id.indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);

        if (storedPinCode != null) {
            tv_pin_explain.setVisibility(View.INVISIBLE);
            tv_pinInfo.setText("Enter PIN");
        } else {
            tv_pinInfo.setText("Create PIN");
            tv_pin_explain.setVisibility(View.VISIBLE);
        }


        pinLockListener = new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                if (storedPinCode != null) {
                    if (pin.equals(storedPinCode)) {
                        startActivity(new Intent(PinActivity.this, ConversationActivity.class));
                        finish();
                    } else {
                        Log.e(TAG, "Wrong pinCode");
                        Toast toast = Toast.makeText(PinActivity.this, "Wrong Pin", Toast.LENGTH_SHORT);
                        toast.show();
                        pinLockView.resetPinLockView();
                    }
                } else {
                    if (firstPin == null) {
                        firstPin = pin;
                        pinLockView.resetPinLockView();
                        tv_pinInfo.setText("Confirm PIN");
                    } else if (firstPin.equals(pin)) {
                        tv_pinInfo.setText("PIN Match");
                        tv_pinInfo.setTextColor(Color.parseColor(GREEN));
                        editor.putBoolean("pinExist", true);
                        editor.putString("pinCode", pin);
                        if (editor.commit())
                            startActivity(new Intent(PinActivity.this, ConversationActivity.class));

                    } else {
                        Vibrator vibrate = (Vibrator) PinActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                        vibrate.vibrate(500);
                        tv_pinInfo.setText("Create PIN again");
                        pinLockView.resetPinLockView();
                        firstPin = null;
                    }
                }
            }

            @Override
            public void onEmpty() {
                Log.i(TAG, "EMPTY");
            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {
            }
        };
        pinLockView.setPinLockListener(pinLockListener);
    }
}
