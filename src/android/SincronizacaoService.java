package cordova.plugins;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;




public class SincronizacaoService extends IntentService{

    static String SERVICE_URL =  null;
    static String IMEI = null;
    static Boolean IS_CACHED = false;

    final String SHARED_FILE = "ego_sincronizacao";
    final String CACHED = "ego_get_cached";
    final String PRIMEIRA_EXECUCAO = "ego_primeira_execucao";
    final String SINCRONIZANDO = "ego_sincronizando";
    final String SINC_COMPLETA = "/sincronizar/dados/completa";
    final String SINC_PARCIAL = "/sincronizar/dados/parcial";
    final String SINC_CONFIRMAR = "/sincronizar/dados/confirmar";
    final String LOG_TAG = getClass().getName();
    final int TIMEOUT = 360000;

    public SincronizacaoService() {
        super("SincronizacaoService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(sincronizando()){
            //do nothing
            return;
        }

        String imei = getImei();
        String url = getSincronizacaoUrl(imei);
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
                salvaCache(res);
                confirmarRecebimento(imei);
                salvaPrimeiraExecucao();
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

    private String getSincronizacaoUrl(String imei){
        String url = SINC_PARCIAL;
        return SERVICE_URL + url + "?imei=" + imei;
    }

    private String getConfirmarRecebimentoUrl(){
        return SERVICE_URL + SINC_CONFIRMAR;
    }

    private void confirmarRecebimento(String imei) throws IOException {
        String url = getConfirmarRecebimentoUrl();
        HttpURLConnection urlConnection = (HttpURLConnection) new URL(url).openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

        OutputStream data = urlConnection.getOutputStream();
        data.write(("{\"imei\": \""+imei+ "\"}").getBytes());
        data.close();

        InputStream in = urlConnection.getInputStream();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte[512];
        int length;

        while((length = in.read(buffer)) != -1) {
            os.write(buffer, 0, length);
        }

        in.close();
        os.flush();
        os.close();

        String result = os.toString();
        Log.i(LOG_TAG, url + " -- " + result);

        if(!result.contains("\"status\":\"OK\"")){
            throw new IOException("REQUEST GET URL "+ url + " FAILED ");
        }
    }

    private String getImei(){
        return IMEI;
    }

    private boolean primeiraExecucao(){
        SharedPreferences sp = sharedPreferences();
        return sp.getBoolean(PRIMEIRA_EXECUCAO, true);
    }

    private boolean sincronizando(){
        SharedPreferences sp = sharedPreferences();
        return sp.getBoolean(SINCRONIZANDO, false);
    }

    private void salvaPrimeiraExecucao(){
        SharedPreferences sp = sharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(PRIMEIRA_EXECUCAO, false);
        editor.apply();
    }

    private void salvaSincronizando(boolean sincronizando){
        SharedPreferences sp = sharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean(SINCRONIZANDO, sincronizando);
        editor.apply();
    }

    private void salvaCache(String data){
        SharedPreferences sp = sharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(CACHED, data);
        editor.apply();
    }

    private void removeCache(){
        SharedPreferences sp = sharedPreferences();
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(CACHED);
        editor.apply();
    }

    private String getDataCached(){
        SharedPreferences sp = sharedPreferences();
        return sp.getString(CACHED, null);
    }

    private SharedPreferences sharedPreferences(){
        return this.getSharedPreferences(SHARED_FILE, MODE_PRIVATE);
    }
}

