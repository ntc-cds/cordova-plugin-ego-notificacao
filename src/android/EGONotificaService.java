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


public class EGONotificaService extends Service {

    static final long TIMER = 5000;

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    static String url = null;
    static MyLoopService mls = null;
    static boolean run = false;
    static int sessao = -1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mls == null) {
            run = true;
            mls = new MyLoopService();
            mls.start();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    static public void setURL(String url) {
        EGONotificaService.url = url;
    }

    static public void ativar(int sessao) {
        EGONotificaService.sessao = sessao;
        EGONotificaService.run = true;
    }

    static public void desativar() {
        EGONotificaService.run = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    class MyLoopService extends Thread {
        protected Notifica getData() throws IOException {
            if (EGONotificaService.sessao < 0) {
                return null;
            }
            try {
                Log.i("EGOSvc2", "Try load url "+EGONotificaService.url + "/cet/notificastatus/" + EGONotificaService.sessao);
                URL url = new URL(EGONotificaService.url + "/cet/notificastatus/" + EGONotificaService.sessao);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Content-Type", "applicaion/json");
                connection.setUseCaches(false);
                connection.connect();
                int status = connection.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: {
                        InputStream response = connection.getInputStream();
                        BufferedReader br = new BufferedReader(new InputStreamReader(response));
                        JsonReader jr = new JsonReader(br);
                        return readNotifica(jr);
                    }
                    default: {
                        return null;
                    }
                }
            } catch (Throwable e) {
                return null;
            }
        }

        protected Notifica readNotifica(JsonReader jr) throws IOException {
            boolean retorno = false;
            jr.beginObject();
            while (jr.hasNext()) {
                String nextName = jr.nextName();
                Log.i("EGOSvc2", nextName);
                if (nextName.equalsIgnoreCase("retorno")) {
                    retorno = jr.nextBoolean();
                }
            }
            return new Notifica(retorno);
        }

        @Override
        public void run() {
            super.run();
            android.os.Looper.prepare();
            while (true) {
                try {
                    if (run) {
                        Notifica notifica = getData();
                        if ((notifica != null) && notifica.retorno) {
                            heads_up_notify();
                        }
                    }
                } catch (Throwable e) {
                }
                try {
                    Thread.sleep(TIMER);
                } catch (Throwable e) {
                }
            }
        }
    }

    void heads_up_notify() {
        try {
            android.widget.Toast.makeText(getApplicationContext(), "RUN", Toast.LENGTH_SHORT).show();
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    class Notifica {
        private boolean retorno;

        public Notifica(boolean retorno) {
            this.retorno = retorno;
        }

        public boolean isRetorno() {
            return retorno;
        }
    }
}