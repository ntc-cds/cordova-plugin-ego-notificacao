package cordova.plugins;

import android.util.Log;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class EgoNotificacao extends CordovaPlugin {

    static CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        EgoNotificacao.callbackContext = callbackContext;

        Log.i("TESTEEEE", "SERVICE TESTE" + args);

        if (action.equals("initService")) {
            String message = args.getString(0);
            this.initService(message, callbackContext);
            return true;
        } else if (action.equals("ativar")) {
            String message = args.getString(0);
            this.ativar(message, callbackContext);
            return true;
        } else if (action.equals("desativar")) {
            this.desativar(callbackContext);
            return true;
        }else if(action.equals("sincronizar")){
            String url = args.getString(0);
            String imei = args.getString(1);
            boolean cached = args.getBoolean(2);

            this.sincronizar(url, imei, cached);
            return true;                
        }

        return false;
    }

    static void sendOkResult(String message){
        if(EgoNotificacao.callbackContext != null){
            EgoNotificacao.callbackContext.success(message);
        }
    }

    static void sendErrorResult(String message){
        if(EgoNotificacao.callbackContext != null){
            EgoNotificacao.callbackContext.error(message);
        }
    }

    private void sincronizar(String url, String imei, boolean cached){
        SincronizacaoService.SERVICE_URL = url;
        SincronizacaoService.IMEI = imei;
        SincronizacaoService.IS_CACHED = cached;

        android.content.Context context = this.cordova.getActivity().getApplicationContext();
        context.startService(new android.content.Intent(context, SincronizacaoService.class));
    }

    private void initService(String message, CallbackContext callbackContext) {
        android.content.Context context = this.cordova.getActivity().getApplicationContext();
        context.startService(new android.content.Intent(context, EGONotificaService.class));

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