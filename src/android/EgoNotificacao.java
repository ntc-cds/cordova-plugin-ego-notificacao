package cordova.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;
import android.widget.Toast;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;


public class EgoNotificacao extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("initService")) {
            String message = args.getString(0);
            this.initService(message, callbackContext);
            return true;
        } else if (action.equals("ativar")) {
            String message = args.getString(0);
            this.ativar(message, callbackContext);
        } else if (action.equals("desativar")) {
            this.desativar(callbackContext);
        }
        return false;
    }

    private void initService(String message, CallbackContext callbackContext) {
        android.content.Context context = this.cordova.getActivity().getApplicationContext();
        context.startService(new android.content.Intent(context, EGONotificaService.class));
        (new android.media.ToneGenerator(android.media.AudioManager.STREAM_NOTIFICATION, 100)).startTone(android.media.ToneGenerator.TONE_CDMA_PIP, 150);
        if (message != null && message.length() > 0) {
            EGONotificaService.setURL(message);
            callbackContext.success(message);
        } else {
            callbackContext.error("Parâmetro esperado.");
        }
    }

    private void ativar(String message, CallbackContext callbackContext) {
        int sessao;
        if (message != null && message.length() > 0) {
            try {
                sessao = Integer.parseInt(message);
                EGONotificaService.ativar(sessao);
                callbackContext.success(message);
            } catch (NumberFormatException e) {
                callbackContext.error("Parâmetro inválido.");
            }
        } else {
            callbackContext.error("Parâmetro esperado.");
        }
    }

    private void desativar(CallbackContext callbackContext) {
        try {
            EGONotificaService.desativar();
            callbackContext.success("ok");
        } catch (Throwable e) {
            callbackContext.error(e.getMessage());
        }
    }
}