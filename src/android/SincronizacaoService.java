package cordova.plugins;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SincronizacaoService extends IntentService{

    static String SERVICE_URL =  null;
    static Long LAST_UPDATE = 0l;

    final String SHARED_FILE = "ego_sincronizacao";
    final String SINCRONIZANDO = "ego_sincronizando";
    final String SINC_PARCIAL = "/sincronizar/dados/parcial";
    final String LOG_TAG = getClass().getName();
    final int TIMEOUT = 360000;

    public SincronizacaoService() {
        super("SincronizacaoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Long lastUpdate = getLastUpdate();
        String url = getSincronizacaoUrl(lastUpdate);
        long startTime = System.currentTimeMillis();

        try {
            salvaSincronizando(true);
            Log.i(LOG_TAG, "START REQUEST "+ url);
            HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
            urlConnection.setConnectTimeout(TIMEOUT);
            InputStream in = urlConnection.getInputStream();
            Log.i(LOG_TAG, "RESPONSE GOT IN " + ((System.currentTimeMillis() - startTime) / 1000 / 60) + " MINUTE");

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;

            while((length = in.read(buffer)) != -1){
                result.write(buffer, 0, length);
            }

            result.flush();
            result.close();

            String res = result.toString().trim();
            Log.i(LOG_TAG, "RESPONSE: " + urlConnection.getResponseCode());

            if(res.contains("\"status\":\"OK\"")){
                EgoNotificacao.sendOkResult(res);
            }else{
                EgoNotificacao.sendErrorResult(res);
            }

            in.close();
        }catch (IOException e){
            Log.e(LOG_TAG, e.getMessage());
            EgoNotificacao.sendErrorResult(e.getMessage());
        }finally {
            salvaSincronizando(false);
        }
    }

    private Long getLastUpdate(){
        return LAST_UPDATE;
    }

    private String getSincronizacaoUrl(Long lastUpdate){
        String url = SINC_PARCIAL;
        if(lastUpdate == 0 || lastUpdate == null){
            return SERVICE_URL + url + "?ultima_sincronizacao=null";
        }
        return SERVICE_URL + url + "?ultima_sincronizacao=" + lastUpdate;
    }

    private void salvaSincronizando(boolean sincronizando){
        SharedPreferences sp = sharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SINCRONIZANDO, sincronizando);
        editor.apply();
    }

    private SharedPreferences sharedPreferences(){
        return this.getSharedPreferences(SHARED_FILE, MODE_PRIVATE);
    }
}

