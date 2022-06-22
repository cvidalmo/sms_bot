package com.lacerbo.sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.os.AsyncTask;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/***********************************************************************************
 * EMPRESA..: La Cerbo - Tecnologia do futuro usada no presente.
 * DESCRIÇÃO: Serviço que acessa o WebService e pegar as mensagens a serem enviadas.
 *            É responsável também pelo envio das mensagens por SMS.
 * AUTOR....: Carlos Vidal.
 * DATA.....: 28 de agosto de 2015.
 ***********************************************************************************/
public class PegaMensagens extends Service {

    private String jsonResult;
    private String urlWebService;
    private SharedPreferences preferences;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        preferences = getSharedPreferences("com.lacerbo.sms", MODE_PRIVATE);
        boolean booSMSConfig = preferences.getBoolean("sms_config", true);
        boolean booSMSReiniciar = preferences.getBoolean("sms_reiniciar", true);
        boolean booSMSUsuarios = preferences.getBoolean("sms_usuarios", true);
        boolean booSMSAdmin = preferences.getBoolean("sms_admin", true);
        String strTempoAcesso = preferences.getString("tempo_acesso", "00:01:00");
        urlWebService = preferences.getString("url_webservice", "https://rastrear.lacerbo.com/tracker/WebServiceMsg.php?filtro=");
        String strValoresArgumento = preferences.getString("valores_argumento", "C,R,U,A");
        String[] strValArq = strValoresArgumento.split(",");

        if (strValArq[0] == null) strValArq[0] = "";
        if (strValArq[1] == null) strValArq[1] = "";
        if (strValArq[2] == null) strValArq[2] = "";
        if (strValArq[3] == null) strValArq[3] = "";

        if (booSMSConfig) urlWebService += strValArq[0];
        if (booSMSReiniciar) urlWebService += strValArq[1];
        if (booSMSUsuarios) urlWebService += strValArq[2];
        if (booSMSAdmin) urlWebService += strValArq[3];

        String[] strTemExec = strTempoAcesso.split(":");
        int intTempoAcesso = 0;

        for (int i = strTemExec.length-1; i > 0; i--) {
            intTempoAcesso += (Integer.parseInt((strTemExec[i])) * Math.pow(60, (strTemExec.length-1)-i));
        }

        Toast.makeText(getApplicationContext(), "Ciclo de acesso ao servidor:\n" + intTempoAcesso + " segundos.", Toast.LENGTH_LONG).show();

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                accessWebService(urlWebService);
            }
        }, 30000, (intTempoAcesso * 1000)); // 30000 milisegundos = 30 segundos.

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // O service será invocado apenas atraves de startService().
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void accessWebService(String url) {
        JsonReadTask task = new JsonReadTask();
        task.execute(new String[]{url});
    }

    // Async Task to access the web
    private class JsonReadTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            try {
                URL urlCliente = new URL(urlWebService);
                HttpURLConnection httpClliente = (HttpURLConnection) urlCliente.openConnection();
                httpClliente.setRequestMethod("POST");
                httpClliente.setRequestProperty("Accept", "application/json");
                httpClliente.setConnectTimeout(3000);
                httpClliente.connect();
                jsonResult = inputStreamToString(httpClliente.getInputStream()).toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private StringBuilder inputStreamToString(InputStream is) {
            String rLine = "";
            StringBuilder answer = new StringBuilder();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));

            try {
                while ((rLine = rd.readLine()) != null) {
                    answer.append(rLine);
                }
            } catch (IOException e) {
                // e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Erro..." + e.toString(), Toast.LENGTH_LONG).show();
            }
            return answer;
        }

        @Override
        protected void onPostExecute(String result) {
            ListDrwaer();
        }
    }// end async task

    // build hash set for list view
    public void ListDrwaer() {
//        String imei;
//        String id_message;
        String message;
        String telefones = "";
        String strNumeroSMS = "";
        boolean booZeroNumSMS = preferences.getBoolean("zero_no_numero_sms", true);
        String strNumSIM1 = preferences.getString("numero_sim_1", "");
        String strNumSIM2 = preferences.getString("numero_sim_2", "");

        strNumSIM1 = strNumSIM1.replaceAll("[+ .]+", "");
        strNumSIM1 = strNumSIM1.replaceAll("-", "");
        strNumSIM1 = strNumSIM1.replaceAll(",55", ",");
        strNumSIM1 = strNumSIM1.replaceAll("\\(", "");
        strNumSIM1 = strNumSIM1.replaceAll("\\)", "");
        strNumSIM1 = strNumSIM1.replaceAll(",,,", "");
        strNumSIM1 = strNumSIM1.replaceAll(",,", "");
        strNumSIM1 = strNumSIM1.replaceAll(",", ";");

        strNumSIM2 = strNumSIM2.replaceAll("[+ .]+", "");
        strNumSIM2 = strNumSIM2.replaceAll("-", "");
        strNumSIM2 = strNumSIM2.replaceAll(",55", ",");
        strNumSIM2 = strNumSIM2.replaceAll("\\(", "");
        strNumSIM2 = strNumSIM2.replaceAll("\\)", "");
        strNumSIM2 = strNumSIM2.replaceAll(",,,", "");
        strNumSIM2 = strNumSIM2.replaceAll(",,", "");
        strNumSIM2 = strNumSIM2.replaceAll(",", ";");

//        String imeisConfirmacao = "";
//        String idsConfirmacao = "";
        try {
            JSONObject jsonResponse = new JSONObject(jsonResult);
            JSONArray jsonMainNode = jsonResponse.optJSONArray("dados");
            for (int i = 0; i < jsonMainNode.length(); i++) {
                JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);
//                imei = jsonChildNode.optString("imei");
//                id_message = jsonChildNode.optString("id");
                message = jsonChildNode.optString("message");
                telefones = ",," + jsonChildNode.optString("telefones") + ",,";

//                imeisConfirmacao += imei + ",";
//                idsConfirmacao += id_message + ",";

                telefones = telefones.replaceAll("[+ .]+", "");
                telefones = telefones.replaceAll("-", "");
                telefones = telefones.replaceAll(",55", ",");
                telefones = telefones.replaceAll("\\(", "");
                telefones = telefones.replaceAll("\\)", "");
                telefones = telefones.replaceAll(",,,", "");
                telefones = telefones.replaceAll(",,", "");
                telefones = telefones.replaceAll(",", ";");

                if (telefones.length() > 8) {
                    List<String> telLista = new ArrayList<String>(Arrays.asList(telefones.split(";")));
                    for (int item = 0; item < telLista.size(); item++) {
                        strNumeroSMS = telLista.get(item).trim();
                        if (strNumeroSMS.length() > 10) {
                            strNumeroSMS = "0"+strNumeroSMS.substring(strNumeroSMS.length()-11);
                            if (strNumeroSMS.length() > 12) {
                                strNumeroSMS = strNumeroSMS.substring(strNumeroSMS.length()-12);
                            }
                            if (!booZeroNumSMS) {
                                strNumeroSMS = strNumeroSMS.substring(1);
                            }
                        }

                        if (( strNumSIM1.length() > 0 && (strNumSIM1.contains(strNumeroSMS) || strNumeroSMS.contains(strNumSIM1)) ) ||
                            ( strNumSIM2.length() > 0 && (strNumSIM2.contains(strNumeroSMS) || strNumeroSMS.contains(strNumSIM2)) ) ) {
                            strNumeroSMS = "";
                        }

                        if (strNumeroSMS.length() > 8) {
                            Toast.makeText(getApplicationContext(), "Enviando: " + strNumeroSMS, Toast.LENGTH_LONG).show();
                            sendSMS(strNumeroSMS, message);
                            //Thread.sleep(3000);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Toast.makeText(getApplicationContext(), "Erro >>> " + e.toString(), Toast.LENGTH_LONG).show();
        } /*catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        /*if (imeisConfirmacao.length() > 10) {
            String url2 = urlWebService + "?imeis=" + imeisConfirmacao + "&ids=" + idsConfirmacao;
            accessWebService(url2);
        }*/
    }

    /*********************************************************
     * BroadcastReceiver mBrSend; BroadcastReceiver mBrReceive;
     **********************************************************/
    private void sendSMS(String phoneNumber, String message) {
        ArrayList<PendingIntent> sentPendingIntents = new ArrayList<PendingIntent>();
        ArrayList<PendingIntent> deliveredPendingIntents = new ArrayList<PendingIntent>();
        PendingIntent sentPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent(getApplicationContext(), SmsSentReceiver.class), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(getApplicationContext(), 0,
                new Intent(getApplicationContext(), SmsDeliveredReceiver.class), 0);

        SubscriptionManager localSubscriptionManager = SubscriptionManager.from(this);
        // TODO: Consider calling
        //    Activity#requestPermissions
        // here to request the missing permissions, and then overriding
        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
        //                                          int[] grantResults)
        // to handle the case where the user grants the permission. See the documentation
        // for Activity#requestPermissions for more details.
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
            return;
        List localList = localSubscriptionManager.getActiveSubscriptionInfoList();
        int intChipAtivo =  preferences.getInt("chip_ativo", 1);
        int intChipEnviar = intChipAtivo -1;

        //Se for AMBOS, ficará alternando.
        if (intChipAtivo == 0) {
            intChipEnviar = preferences.getInt("chip_enviar", 1);

            if (intChipEnviar == 0) {
                intChipEnviar = 1;
            } else {
                intChipEnviar = 0;
            }
            preferences.edit().putInt("chip_enviar", intChipEnviar).apply();
        }

        //SubscriptionInfo simInfo1 = (SubscriptionInfo) localList.get(0);
        SubscriptionInfo simInfo = (SubscriptionInfo) localList.get(intChipEnviar);  //0-SIM 1 e 1=SIM 2.
        final int subId = simInfo.getSubscriptionId();
        try {
            SmsManager sms;  // = SmsManager.getDefault();
            sms = SmsManager.getSmsManagerForSubscriptionId(subId);
            ArrayList<String> mSMSMessage = sms.divideMessage(message);
            for (int i = 0; i < mSMSMessage.size(); i++) {
                sentPendingIntents.add(i, sentPI);
                deliveredPendingIntents.add(i, deliveredPI);
            }
            sms.sendMultipartTextMessage(phoneNumber, null, mSMSMessage,
                    sentPendingIntents, deliveredPendingIntents);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getBaseContext(), "SMS falha no envio...",Toast.LENGTH_SHORT).show();
        }
    }

    public class SmsDeliveredReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS entregue", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS não entregue", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public class SmsSentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent arg1) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context, "SMS Enviado", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS falha genérica", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS sem serviço", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS PDU nulo", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS rádio desligado", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    /* ****************************************************************************
                                           F  I  M
     **************************************************************************** */
}

