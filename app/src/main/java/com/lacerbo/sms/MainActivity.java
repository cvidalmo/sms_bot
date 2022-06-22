package com.lacerbo.sms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences preferences;
    private Menu mnMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConstraintLayout clConfigBasico = findViewById(R.id.clConfigBasico);
        ConstraintLayout clConfigAvancado = findViewById(R.id.clConfigAvancado);
        ConstraintLayout clPrincipal = findViewById(R.id.clPrincipal);
        clConfigBasico.setVisibility(INVISIBLE);
        clConfigAvancado.setVisibility(INVISIBLE);
        clPrincipal.setVisibility(VISIBLE);

        preferences = getSharedPreferences("com.lacerbo.sms", MODE_PRIVATE);
        int intChipAtivo = preferences.getInt("chip_ativo", 1);
        boolean booSMSConfig = preferences.getBoolean("sms_config", true);
        boolean booSMSReiniciar = preferences.getBoolean("sms_reiniciar", true);
        boolean booSMSUsuarios = preferences.getBoolean("sms_usuarios", true);
        boolean booSMSAdmin = preferences.getBoolean("sms_admin", true);
        String strNumSIM1 = preferences.getString("numero_sim_1", "");
        String strNumSIM2 = preferences.getString("numero_sim_2", "");
        boolean booZeroNumSMS = preferences.getBoolean("zero_no_numero_sms", true);
        String strTempoAcesso = preferences.getString("tempo_acesso", "00:01:00");
        String strURLWebService = preferences.getString("url_webservice", "https://rastrear.lacerbo.com/tracker/WebServiceMsg.php?filtro=");
        String strValoresArgumento = preferences.getString("valores_argumento", "C,R,U,A");

        Button btAtivarDesativar = findViewById(R.id.btAtivarDesativar);
        RadioButton rbCHIP1 = findViewById(R.id.rbCHIP1);
        RadioButton rbCHIP2 = findViewById(R.id.rbCHIP2);
        RadioButton rbCHIP0 = findViewById(R.id.rbCHIP0);
        RadioGroup rgCHIPs = findViewById(R.id.rgCHIPs);
        CheckBox cbConfig = findViewById(R.id.cbConfig);
        CheckBox cbReiniciar = findViewById(R.id.cbReiniciar);
        CheckBox cbUsuarios = findViewById(R.id.cbUsuarios);
        CheckBox cbAdmin = findViewById(R.id.cbAdmin);
        EditText etNumSMS1 = findViewById(R.id.etSIM1);
        EditText etNumSMS2 = findViewById(R.id.etSIM2);
        RadioButton rbComZero = findViewById(R.id.rbComZero);
        RadioButton rbSemZero = findViewById(R.id.rbSemZero);
        EditText etTempoAcesso = findViewById(R.id.etTempoAcesso);
        EditText etUrlWebService = findViewById(R.id.etURLWebService);
        EditText etValoresArgumento = findViewById(R.id.etValoresArgumento);

        if (intChipAtivo == 1) {
            rgCHIPs.check(rbCHIP1.getId());
        } else if (intChipAtivo == 2) {
            rgCHIPs.check(rbCHIP2.getId());
        } else {
            rgCHIPs.check(rbCHIP0.getId());
        }
        cbConfig.setChecked(booSMSConfig);
        cbReiniciar.setChecked(booSMSReiniciar);
        cbUsuarios.setChecked(booSMSUsuarios);
        cbAdmin.setChecked(booSMSAdmin);
        etNumSMS1.setText(strNumSIM1);
        etNumSMS2.setText(strNumSIM2);
        rbComZero.setChecked(booZeroNumSMS);
        rbSemZero.setChecked(!booZeroNumSMS);
        etTempoAcesso.setText(strTempoAcesso);
        etUrlWebService.setText(strURLWebService);
        etValoresArgumento.setText(strValoresArgumento);

        preferences.edit().putInt("chip_ativo", intChipAtivo).apply();
        preferences.edit().putBoolean("sms_config", booSMSConfig).apply();
        preferences.edit().putBoolean("sms_reiniciar", booSMSReiniciar).apply();
        preferences.edit().putBoolean("sms_usuarios", booSMSUsuarios).apply();
        preferences.edit().putBoolean("sms_admin", booSMSAdmin).apply();
        preferences.edit().putString("numero_sim_1", strNumSIM1).apply();
        preferences.edit().putString("numero_sim_2", strNumSIM2).apply();
        preferences.edit().putBoolean("zero_no_numero_sms", booZeroNumSMS).apply();
        preferences.edit().putString("tempo_acesso", strTempoAcesso).apply();
        preferences.edit().putString("url_webservice", strURLWebService).apply();
        preferences.edit().putString("valores_argumento", strValoresArgumento).apply();

        if (isServicoRodando()) {
            rbCHIP1.setEnabled(false);
            rbCHIP2.setEnabled(false);
            rbCHIP0.setEnabled(false);
            rgCHIPs.setEnabled(false);
            btAtivarDesativar.setTag(0);
            btAtivarDesativar.setText(R.string.desativar);
            cbConfig.setEnabled(false);
            cbReiniciar.setEnabled(false);
            cbUsuarios.setEnabled(false);
            cbAdmin.setEnabled(false);
        } else {
            rbCHIP1.setEnabled(true);
            rbCHIP2.setEnabled(true);
            rbCHIP0.setEnabled(true);
            rgCHIPs.setEnabled(true);
            btAtivarDesativar.setTag(1);
            btAtivarDesativar.setText(R.string.ativar);
            cbConfig.setEnabled(true);
            cbReiniciar.setEnabled(true);
            cbUsuarios.setEnabled(true);
            cbAdmin.setEnabled(true);
        }

        List<String> lisStrPermissao = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            lisStrPermissao.add(Manifest.permission.SEND_SMS);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            lisStrPermissao.add(Manifest.permission.RECEIVE_SMS);
        }
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            lisStrPermissao.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!lisStrPermissao.isEmpty()) {
            String[] strPermissao = new String[lisStrPermissao.size()];
            strPermissao = lisStrPermissao.toArray(strPermissao);
            ActivityCompat.requestPermissions(this, strPermissao, 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        mnMenu = menu;
        inflater.inflate(R.menu.menu, menu);
        if (isServicoRodando()) {
            menu.findItem(R.id.mnItem).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        ConstraintLayout clConfigBasico = findViewById(R.id.clConfigBasico);
        ConstraintLayout clConfigAvancado = findViewById(R.id.clConfigAvancado);
        ConstraintLayout clPrincipal = findViewById(R.id.clPrincipal);
        mnMenu.findItem(R.id.miBasico).setEnabled(true);
        mnMenu.findItem(R.id.miAvancado).setEnabled(true);
        switch (item.getItemId()) {
            case R.id.miBasico:
                clPrincipal.setVisibility(INVISIBLE);
                clConfigAvancado.setVisibility(INVISIBLE);
                clConfigBasico.setVisibility(VISIBLE);
                item.setEnabled(false);
                break;
            case R.id.miAvancado:
                clPrincipal.setVisibility(INVISIBLE);
                clConfigBasico.setVisibility(INVISIBLE);
                clConfigAvancado.setVisibility(VISIBLE);
                item.setEnabled(false);
        }

        return super.onOptionsItemSelected(item);
    }

    public void onClickVoltar(View view) {
        ConstraintLayout clConfigBasico = findViewById(R.id.clConfigBasico);
        ConstraintLayout clConfigAvancado = findViewById(R.id.clConfigAvancado);
        ConstraintLayout clPrincipal = findViewById(R.id.clPrincipal);
        mnMenu.findItem(R.id.miBasico).setEnabled(true);
        mnMenu.findItem(R.id.miAvancado).setEnabled(true);
        clConfigBasico.setVisibility(INVISIBLE);
        clConfigAvancado.setVisibility(INVISIBLE);
        clPrincipal.setVisibility(VISIBLE);
    }

    public void onClickSalvar(View view) {
        EditText edNumSIM1 = findViewById(R.id.etSIM1);
        EditText edNumSIM2 = findViewById(R.id.etSIM2);
        RadioButton rbCOMZero = findViewById(R.id.rbComZero);
        EditText etTempoAcesso = findViewById(R.id.etTempoAcesso);
        EditText etUrlWebService = findViewById(R.id.etURLWebService);
        EditText etValoresArgumento = findViewById(R.id.etValoresArgumento);

        preferences.edit().putString("numero_sim_1", edNumSIM1.getText().toString()).apply();
        preferences.edit().putString("numero_sim_2", edNumSIM2.getText().toString()).apply();
        preferences.edit().putBoolean("zero_no_numero_sms", rbCOMZero.isChecked()).apply();
        preferences.edit().putString("tempo_acesso", etTempoAcesso.getText().toString()).apply();
        preferences.edit().putString("url_webservice", etUrlWebService.getText().toString()).apply();
        preferences.edit().putString("valores_argumento", etValoresArgumento.getText().toString()).apply();
    }

    public void onClickAtivarDesativar(View view) {
        Button btAtivarDesativar = (Button) view;
        Button btSair = findViewById(R.id.btSair);
        RadioButton rbCHIP1 = findViewById(R.id.rbCHIP1);
        RadioButton rbCHIP2 = findViewById(R.id.rbCHIP2);
        RadioButton rbCHIP0 = findViewById(R.id.rbCHIP0);
        RadioGroup rgCHIPs = findViewById(R.id.rgCHIPs);
        CheckBox cbConfig = findViewById(R.id.cbConfig);
        CheckBox cbReiniciar = findViewById(R.id.cbReiniciar);
        CheckBox cbUsuarios = findViewById(R.id.cbUsuarios);
        CheckBox cbAdmin = findViewById(R.id.cbAdmin);

        if (!cbConfig.isChecked() && !cbReiniciar.isChecked() && !cbUsuarios.isChecked() && !cbAdmin.isChecked()) {
            Toast.makeText(getApplicationContext(), "Deve-se marcar pelo menos 1 tipo.", Toast.LENGTH_LONG).show();
        } else {

            if (rgCHIPs.getCheckedRadioButtonId() == rbCHIP1.getId()) {
                preferences.edit().putInt("chip_ativo", 1).apply();
            } else if (rgCHIPs.getCheckedRadioButtonId() == rbCHIP2.getId()) {
                preferences.edit().putInt("chip_ativo", 2).apply();
            } else {
                preferences.edit().putInt("chip_ativo", 0).apply();
            }
            preferences.edit().putBoolean("sms_config", cbConfig.isChecked()).apply();
            preferences.edit().putBoolean("sms_reiniciar", cbReiniciar.isChecked()).apply();
            preferences.edit().putBoolean("sms_usuarios", cbUsuarios.isChecked()).apply();
            preferences.edit().putBoolean("sms_admin", cbAdmin.isChecked()).apply();

            btAtivarDesativar.setEnabled(false);
            btSair.setEnabled(false);
            rbCHIP1.setEnabled(false);
            rbCHIP2.setEnabled(false);
            rbCHIP0.setEnabled(false);
            rgCHIPs.setEnabled(false);
            cbConfig.setEnabled(false);
            cbReiniciar.setEnabled(false);
            cbUsuarios.setEnabled(false);
            cbAdmin.setEnabled(false);

            if (btAtivarDesativar.getTag().equals(1)) {
                if (isServicoRodando()) {
                    Toast.makeText(getApplicationContext(), "Serviço já estava ativado", Toast.LENGTH_LONG).show();
                } else {
                    startService(new Intent(getApplicationContext(), com.lacerbo.sms.PegaMensagens.class));
                    Toast.makeText(getApplicationContext(), "Ativado", Toast.LENGTH_LONG).show();
                }
                mnMenu.findItem(R.id.mnItem).setVisible(false);
                btAtivarDesativar.setTag(0);
                btAtivarDesativar.setText(R.string.desativar);
            } else {

                Toast.makeText(getApplicationContext(), "Desativando... e saindo.", Toast.LENGTH_LONG).show();

                stopService(new Intent(getApplicationContext(), PegaMensagens.class));

                if (isServicoRodando()) {
                    Toast.makeText(getApplicationContext(), "Não foi possível desativar o serviço", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Desativado", Toast.LENGTH_LONG).show();
                    btAtivarDesativar.setTag(1);
                    btAtivarDesativar.setText(R.string.ativar);
                    System.exit(0);  // Esse comando para tanto a activity como o serviço, mesmo que ele esteja em execução
                }
            }

            btAtivarDesativar.setEnabled(true);
            btSair.setEnabled(true);
        }

    }

    private boolean isServicoRodando(){
        boolean toReturnServicoIsRodando = false;
        try{
            ActivityManager activityManager = (ActivityManager)getSystemService(ACTIVITY_SERVICE);
            assert activityManager != null;
            for (ActivityManager.RunningServiceInfo servicosRodando : activityManager.getRunningServices(Integer.MAX_VALUE)) {
                if ("com.lacerbo.sms.PegaMensagens".equals(servicosRodando.service.getClassName())) {
                    toReturnServicoIsRodando = true;
                    break;
                }
            }
        }catch (Exception ignored) {
        }
        return toReturnServicoIsRodando;
    }

    public void onClickSair(View view) {
        if (isServicoRodando()) {
            // Paro o serviço, mas ele vai reiniciar sozinho e só vai aparecer na lista de serviço
            // do Android quando fizer acesso ao webservice para pegar as mensagens.
            // startService(new Intent(getApplicationContext(), PegaMensagens.class));
            finish();  // Esse comando sai do aplicativo mas deixa o serviço reiniciar, mesmo tendo sido parado.
        } else {
            System.exit(0);  // Esse comando para tanto a activity como o serviço, mesmo que ele esteja em execução.
        }
    }
}
