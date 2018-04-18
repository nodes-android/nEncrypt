package dk.nodes.locksmith.core.fingerprint;

import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;

import dk.nodes.locksmith.core.Locksmith;
import dk.nodes.locksmith.core.exceptions.LocksmithCreationException;
import dk.nodes.locksmith.core.models.FingerprintDialogEvent;
import dk.nodes.locksmith.core.models.OnFingerprintDialogEventListener;

@RequiresApi(api = Build.VERSION_CODES.M)
public abstract class FingerprintAlertDialogBase extends AlertDialog {
    private static final String TAG = FingerprintAlertDialogBase.class.getSimpleName();
    // Listeners
    protected OnFingerprintDialogEventListener onFingerprintDialogEventListener;
    // Callbacks
    private FingerprintAuthenticationCallback fingerprintAuthenticationCallback = new FingerprintAuthenticationCallback();
    // Fingerprint Related Stuff
    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;
    private FingerprintCryptManager cryptManager;
    private CancellationSignal cancellationSignal;

    public FingerprintAlertDialogBase(@NonNull Context context) {
        super(context);

        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

        inflateView();
    }


    public FingerprintAlertDialogBase(@NonNull Context context, int themeResId) {
        super(context, themeResId);

        keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        fingerprintManager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);

        inflateView();
    }

    private void inflateView() {
        View view = LayoutInflater.from(getContext()).inflate(getDialogLayout(), null);
        setView(view);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        checkHardware();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private void checkHardware() {
        try {
            cryptManager = new FingerprintCryptManager();
        } catch (LocksmithCreationException e) {
            if (onFingerprintDialogEventListener != null) {
                onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.ERROR_CIPHER);
            }

            dismiss();
            return;
        }

        if (!keyguardManager.isDeviceSecure()) {
            if (onFingerprintDialogEventListener != null) {
                onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.ERROR_SECURE);
            }

            dismiss();
            return;
        }

        if (!fingerprintManager.isHardwareDetected()) {
            if (onFingerprintDialogEventListener != null) {
                onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.ERROR_HARDWARE);
            }

            dismiss();
            return;
        }

        if (!fingerprintManager.hasEnrolledFingerprints()) {
            if (onFingerprintDialogEventListener != null) {
                onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.ERROR_ENROLLMENT);
            }

            dismiss();
            return;
        }

        authenticate();
    }

    private void authenticate() {
        cancellationSignal = new CancellationSignal();
        FingerprintManager.CryptoObject cryptoObject = cryptManager.getCryptoObject();
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, fingerprintAuthenticationCallback, null);
    }

    @Override
    protected void onStop() {
        cancelSignal();
        super.onStop();
    }

    protected void cancelSignal() {
        if (cancellationSignal != null && !cancellationSignal.isCanceled()) {
            cancellationSignal.cancel();
        }
    }


    public class FingerprintAuthenticationCallback extends FingerprintManager.AuthenticationCallback {
        @Override
        public void onAuthenticationError(int errMsgId, CharSequence errString) {
            Log.d(TAG, "onAuthenticationError");
            onFingerprintHelp(errString.toString());
        }

        @Override
        public void onAuthenticationHelp(int helpMsgId, CharSequence helpString) {
            Log.d(TAG, "onAuthenticationHelp");
            onFingerprintHelp(helpString.toString());
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            Log.d(TAG, "onAuthenticationSucceeded");
            preDialogSuccessful();
            fingerprintAuthenticationCallback = null;
        }

        @Override
        public void onAuthenticationFailed() {
            Log.d(TAG, "onAuthenticationFailed");
            preDialogError();
            fingerprintAuthenticationCallback = null;
        }
    }

    private void preDialogSuccessful() {
        try {
            Locksmith.getInstance().init();
        } catch (LocksmithCreationException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "finishDialogSuccess");

        if (onFingerprintDialogEventListener != null) {
            onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.SUCCESS);
        }

        onFingerprintSuccess();
    }

    // Helpers

    private void preDialogError() {
        Log.d(TAG, "finishDialogError");

        if (onFingerprintDialogEventListener != null) {
            onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.ERROR);
        }

        onFingerprintError();
    }

    protected void onCancelClicked() {
        if (onFingerprintDialogEventListener != null) {
            onFingerprintDialogEventListener.onFingerprintEvent(FingerprintDialogEvent.CANCEL);
        }

        closeDialog();
    }

    protected void closeDialog() {
        Log.d(TAG, "closeDialog");
        cancelSignal();
        dismiss();
    }

    public abstract @LayoutRes
    int getDialogLayout();

    public abstract void onFingerprintSuccess();

    public abstract void onFingerprintHelp(String help);

    public abstract void onFingerprintError();
}