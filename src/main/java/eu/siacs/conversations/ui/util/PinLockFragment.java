package eu.siacs.conversations.ui.util;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alimuzaffar.lib.pin.PinEntryEditText;

import eu.siacs.conversations.R;
import eu.siacs.conversations.databinding.FragmentPinCodeBinding;

public class PinLockFragment {

    private FragmentPinCodeBinding binding;
    private PinStatus pinStatus;

    public enum PinStatus {
        MODE_CREATE, MODE_RETYPE, MODE_AUTH
    }

    public static PinLockFragment newInstance(PinStatus status) {
        PinLockFragment pinFragment = new PinLockFragment();

        Bundle args = new Bundle();
        args.putSerializable("pin_status", status);
        pinFragment.setArguments(args);

        return pinFragment;
    }

    private void readBundle(Bundle bundle) {
        if (bundle != null) {
            pinStatus = (PinStatus) bundle.get("pin_status");
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_pin_code, container, false);
        readBundle(getArguments());

        String toolbarTitle, pinTitle;
        if (pinStatus == PinStatus.MODE_RETYPE) {
            toolbarTitle = getResources().getString(R.string.toolbar_pinlock_retype);
            pinTitle = getResources().getString(R.string.pincode_title_retype);
            setToolbarIcon(BaseFragment.BACK_ICON);
        } else if (pinStatus == PinStatus.MODE_CREATE) {
            toolbarTitle = getResources().getString(R.string.toolbar_pinlock_create);
            pinTitle = getResources().getString(R.string.pincode_title_create);
            setToolbarIcon(BaseFragment.NONE);
        } else {
            toolbarTitle = getResources().getString(R.string.toolbar_pinlock_auth);
            pinTitle = getResources().getString(R.string.pincode_title);
            setToolbarIcon(BaseFragment.NONE);
        }

        setToolbarTitle(toolbarTitle);
        binding.pinTitle.setText(pinTitle);

        final PinEntryEditText pinEntry = binding.txtPinEntry;
        if (pinEntry != null) {

            pinEntry.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.toString().length() == 1) {
                        if (binding.txtPinMatchError.getVisibility() == View.VISIBLE)
                            binding.txtPinMatchError.setVisibility(View.GONE);
                    }
                }
            });

            pinEntry.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {

                    if (pinStatus == PinStatus.MODE_CREATE) {
                        PrefHelper.setRetypePinCode(getContext(), str.toString());
                        binding.txtPinEntry.setText("");
                        changeFragment(PinLockFragment.newInstance(PinStatus.MODE_RETYPE), AuthenticationActivity.PINLOCKTAG_RETYPE);
                    } else if (pinStatus == PinStatus.MODE_RETYPE) {

                        if (PrefHelper.getRetypePinCode(getContext()).equals(str.toString())) {
                            PrefHelper.setPinAuth(getContext(), true);
                            PrefHelper.setPinCode(getContext(), str.toString());
                            startHomeActivity();
                        } else {
                            binding.txtPinEntry.setText("");
                            binding.txtPinMatchError.setText(getResources().getText(R.string.pin_match_error));
                            binding.txtPinMatchError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (str.toString().equals(PrefHelper.getPinCode(getContext()))) {
                            hideKeyboard();
                            startHomeActivity();
                        } else {
                            binding.txtPinEntry.setText("");
                            binding.txtPinMatchError.setText(getResources().getText(R.string.pin_incorrect_error));
                            binding.txtPinMatchError.setVisibility(View.VISIBLE);

                            if (binding.txtPinForgot.getVisibility() != View.VISIBLE)
                                binding.txtPinForgot.setVisibility(View.VISIBLE);
                        }
                    }

                }
            });

            pinEntry.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {

                        binding.txtPinEntry.clearFocus();
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(binding.txtPinEntry.getWindowToken(), 0);

                    }
                    return false;
                }
            });

            binding.txtPinEntry.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showKeyboard(binding.txtPinEntry);

                }
            },200);
        }

        return binding.getRoot();
    }


    private void startHomeActivity() {
        getActivity().finish();
        startActivity(new Intent(getActivity(), MainActivity.class));
    }

    @Override
    public void onNavigationIconClick() {
        super.onNavigationIconClick();
        getActivity().onBackPressed();
    }
}
