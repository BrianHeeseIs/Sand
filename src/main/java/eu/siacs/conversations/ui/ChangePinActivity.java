package eu.siacs.conversations.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;
import android.widget.Toast;

import com.andrognito.pinlockview.IndicatorDots;
import com.andrognito.pinlockview.PinLockListener;
import com.andrognito.pinlockview.PinLockView;
import com.securepreferences.SecurePreferences;

import eu.siacs.conversations.R;

public class ChangePinActivity extends AppCompatActivity {
    private static final String TAG = "ChangePinActivity";
    public static final String PASSWORD = "default_password";

    SharedPreferences sharedPreferences;
    TextView tvHeader, tvError;

    private PinLockView pinLockView;
    private PinLockListener pinLockListener;
    private IndicatorDots indicatorDots;

    private String storedPin, firstPin;
    int stage = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Change PIN code");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvHeader = (TextView) findViewById(R.id.tv_change_pin_header);
        tvError = (TextView) findViewById(R.id.tv_change_pin_error);
        pinLockView = (PinLockView) findViewById(R.id.change_pin_pinlock_view);
        indicatorDots = (IndicatorDots) findViewById(R.id.change_pin_indicator_dots);
        pinLockView.attachIndicatorDots(indicatorDots);

        sharedPreferences = new SecurePreferences(this, PASSWORD, "loginInfo.xml");
        storedPin = sharedPreferences.getString("pinCode", null);

        tvHeader.setText("Enter your current PIN");

        pinLockListener = new PinLockListener() {
            @Override
            public void onComplete(String pin) {
                switch (stage) {
                    case 0:
                        if (pin.equals(storedPin)) {
                            tvHeader.setText("Enter your new PIN");
                            pinLockView.resetPinLockView();
                            stage = 1;
                        } else {
                            Toast.makeText(getApplicationContext(), "Wrong PIN", Toast.LENGTH_SHORT).show();
                            pinLockView.resetPinLockView();
                        }
                        break;
                    case 1:
                        firstPin = pin;
                        tvHeader.setText("Confirm your new PIN");
                        pinLockView.resetPinLockView();
                        stage = 2;
                        break;
                    case 2:
                        if (pin.equals(firstPin)) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("pinCode", pin);
                            if (editor.commit()) {
                                Toast.makeText(getApplicationContext(), "PIN successfully changed", Toast.LENGTH_SHORT).show();
                                finish();
                                stage = 0;
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed to changed PIN", Toast.LENGTH_SHORT).show();
                                tvHeader.setText("Enter your current PIN");
                                firstPin = null;
                                stage = 0;
                            }
                        } else {
                            firstPin = null;
                            tvHeader.setText("Enter your current PIN");
                            pinLockView.resetPinLockView();
                            Toast.makeText(getApplicationContext(), "New PIN doesn't match", Toast.LENGTH_SHORT).show();
                            stage = 0;
                        }
                        break;
                }
            }

            @Override
            public void onEmpty() {

            }

            @Override
            public void onPinChange(int pinLength, String intermediatePin) {

            }
        };

        pinLockView.setPinLockListener(pinLockListener);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
