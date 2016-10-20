package mx.grupohi.acarreos;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.PACKAGE_USAGE_STATS;
import static android.Manifest.permission.READ_PHONE_STATE;

public class LoginActivity extends AppCompatActivity {

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView userText;
    private EditText passText;
    private TextInputLayout loginFormLayout;
    private ProgressDialog loginProgressDialog;
    private DBScaSqlite db_sca;
    Intent mainActivity;
    Usuario usuario;

    // GPSTracker class
    GPSTracker gps;
    double latitude;
    double longitude;
    public String IMEI;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainActivity = new Intent(this, MainActivity.class);
        usuario = new Usuario(this);
        if (usuario.isAuth()) {
            mainActivity = new Intent(this, MainActivity.class);
            startActivity(mainActivity);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(getString(R.string.title_activity_login));
        gps = new GPSTracker(LoginActivity.this);
        checkPermissions();
        // Set up the login form.
        userText = (AutoCompleteTextView) findViewById(R.id.userText);
        passText = (EditText) findViewById(R.id.passText);
        loginFormLayout = (TextInputLayout) findViewById(R.id.layout);

        db_sca = new DBScaSqlite(getApplicationContext(), "sca", null, 1);

        final Button loginButton = (Button) findViewById(R.id.loginButton);
        assert loginButton != null;
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkPermissions()) {
                    latitude = gps.getLatitude();
                    longitude = gps.getLongitude();
                    TelephonyManager phneMgr = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    IMEI = phneMgr.getDeviceId();
                    attemptLogin();
                    /* if(latitude + longitude == 0) {
                        Snackbar snackbar;
                        snackbar = Snackbar.make(view, "No se puede detectar la ubicación. ¡Espere un momento por favor e intentelo de nuevo!", Snackbar.LENGTH_SHORT);
                        View snackBarView = snackbar.getView();
                        snackBarView.setBackgroundColor(Color.RED);
                        snackbar.show();
                    } else {
                        attemptLogin();
                    }*/
                    attemptLogin();
                }
            }
        });
    }
    private Boolean checkPermissions() {
        Boolean permission_fine_location = true;
        Boolean permission_read_phone_state = true;
        Boolean _gps = true;
        Boolean internet = true;

        if(ContextCompat.checkSelfPermission(LoginActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            permission_fine_location = false;
        }

        if(ContextCompat.checkSelfPermission(LoginActivity.this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, 100);
            permission_read_phone_state =  false;
        }

        if(!gps.canGetLocation()) {
            gps.showSettingsAlert();
            _gps = false;
        }

        if(!Util.isNetworkStatusAvialable(getApplicationContext())) {
            Toast.makeText(LoginActivity.this, R.string.error_internet, Toast.LENGTH_LONG).show();
            internet = false;
        }
        return (permission_fine_location && permission_read_phone_state && _gps && internet);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(usuario.isAuth()) {
            startActivity(mainActivity);
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Resetear errores.
        userText.setError(null);
        passText.setError(null);

        // Store values at the time of the login attempt.
        String user = userText.getText().toString();
        String pass = passText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(user)) {
            userText.setError(getResources().getString(R.string.error_field_required));
            focusView = userText;
            cancel = true;
        } else if (TextUtils.isEmpty(pass)) {
            passText.setError(getResources().getString(R.string.error_field_required));
            focusView = passText;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            loginProgressDialog = ProgressDialog.show(LoginActivity.this, "Autenticando", "Por favor espere...", true);
            mAuthTask = new UserLoginTask(user, pass);
            mAuthTask.execute((Void) null);
        }
    }
    
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String user;
        private final String pass;

        UserLoginTask(String user, String pass) {
            this.user = user;
            this.pass = pass;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            ContentValues data = new ContentValues();
            data.put("usr", user);
            data.put("pass", pass);

            try {
                URL url = new URL("http://sca.grupohi.mx/android20160923.php");
                final JSONObject JSON = HttpConnection.POST(url, data);
                db_sca.deleteCatalogos();
                if (JSON.has("error")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginFormLayout.setErrorEnabled(true);
                            try {
                                loginFormLayout.setError((String) JSON.get("error"));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    return false;
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loginProgressDialog.setTitle("Actualizando");
                            loginProgressDialog.setMessage("Actualizando datos de usuario...");
                        }
                    });

                    Usuario usuario = new Usuario(getApplicationContext());

                    data.clear();
                    data.put("idusuario", JSON.getString("IdUsuario"));
                    data.put("idproyecto", JSON.getString("IdProyecto") );
                    data.put("nombre", JSON.getString("Nombre"));
                    data.put("user", user);
                    data.put("pass", pass);
                    data.put("base_datos", JSON.getString("base_datos"));
                    data.put("descripcion_database", JSON.getString("descripcion_database"));

                    if (!usuario.create(data)) {
                        return false;
                    }

                    //Camuiones
                    Camion camion = new Camion(getApplicationContext());
                    final JSONArray camiones = new JSONArray(JSON.getString("Camiones"));
                    for (int i = 0; i < camiones.length(); i++) {
                        final int finalI = i + 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setMessage("Actualizando catálogo de camiones... \n Camion " + finalI + " de " + camiones.length());
                            }
                        });
                        JSONObject info = camiones.getJSONObject(i);

                        data.clear();
                        data.put("idcamion", info.getString("idcamion"));
                        data.put("placas", info.getString("placas"));
                        data.put("marca", info.getString("marca") );
                        data.put("modelo",  info.getString("modelo"));
                        data.put("ancho",  info.getString("ancho"));
                        data.put("largo",  info.getString("largo"));
                        data.put("alto",  info.getString("alto"));
                        data.put("economico",  info.getString("economico"));
                        data.put("capacidad",  info.getString("capacidad"));

                        if (!camion.create(data)) {
                            return false;
                        }
                    }

                    //Tiros

                    Tiro tiro = new Tiro(getApplicationContext());
                    final JSONArray tiros = new JSONArray(JSON.getString("Tiros"));
                    for (int i = 0; i < tiros.length(); i++) {
                        final int finalI = i + 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setMessage("Actualizando catálogo de tiros... \n Tiro " + finalI + " de " + tiros.length());
                            }
                        });
                        JSONObject info = tiros.getJSONObject(i);

                        data.clear();
                        data.put("idtiro", info.getString("idtiro"));
                        data.put("descripcion", info.getString("descripcion"));

                        if (!tiro.create(data)) {
                            return false;
                        }
                    }

                    //Origenes

                    Origen origen = new Origen(getApplicationContext());
                    final JSONArray origenes = new JSONArray(JSON.getString("Origenes"));
                    for (int i = 0; i < origenes.length(); i++) {
                        final int finalI = i + 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setMessage("Actualizando catálogo de Origenes... \n Origen " + finalI + " de " + origenes.length());
                            }
                        });
                        JSONObject info = origenes.getJSONObject(i);

                        data.clear();
                        data.put("idorigen", info.getString("idorigen"));
                        data.put("descripcion", info.getString("descripcion"));
                        data.put("estado", info.getString("estado"));


                        if (!origen.create(data)) {
                            return false;
                        }
                    }

                    //Rutas

                    Ruta ruta = new Ruta(getApplicationContext());
                    final JSONArray rutas = new JSONArray(JSON.getString("Rutas"));
                    for (int i = 0; i < rutas.length(); i++) {
                        final int finalI = i + 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setMessage("Actualizando catálogo de Rutas... \n Ruta " + finalI + " de " + rutas.length());
                            }
                        });
                        JSONObject info = rutas.getJSONObject(i);

                        data.clear();
                        data.put("idruta", info.getString("idruta"));
                        data.put("clave", info.getString("clave"));
                        data.put("idorigen", info.getString("idorigen"));
                        data.put("idtiro", info.getString("idtiro"));
                        data.put("totalkm", info.getString("totalkm"));

                        if (!ruta.create(data)) {
                            return false;
                        }
                    }

                    //Materiales

                    Material material = new Material(getApplicationContext());
                    final JSONArray materiales = new JSONArray(JSON.getString("Materiales"));
                    for (int i = 0; i < materiales.length(); i++) {
                        final int finalI = i + 1;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                loginProgressDialog.setMessage("Actualizando catálogo de Materiales... \n Material " + finalI + " de " + materiales.length());
                            }
                        });
                        JSONObject info = materiales.getJSONObject(i);

                        data.clear();
                        data.put("idmaterial", info.getString("idmaterial"));
                        data.put("descripcion", info.getString("descripcion"));

                        if (!material.create(data)) {
                            return false;
                        }
                    }

                    //Tags

                    TagModel tag = new TagModel(getApplicationContext());
                    try {
                        final JSONArray tags = new JSONArray(JSON.getString("Tags"));
                        for (int i = 0; i < tags.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginProgressDialog.setMessage("Actualizando catálogo de Tags... \n Tag " + finalI + " de " + tags.length());
                                }
                            });
                            JSONObject info = tags.getJSONObject(i);

                            data.clear();
                            data.put("uid", info.getString("uid"));
                            data.put("idcamion", info.getString("idcamion"));
                            data.put("idproyecto", info.getString("idproyecto"));

                            if (!tag.create(data)) {
                                return false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Viajes

                    Viaje viaje = new Viaje(getApplicationContext());
                    try {
                        final JSONArray viajesNetos = new JSONArray(JSON.getString("ViajesNetos"));
                        for (int i = 0; i < viajesNetos.length(); i++) {
                            final int finalI = i + 1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    loginProgressDialog.setMessage("Actualizando catálogo de Viajes... \n Tag " + finalI + " de " + viajesNetos.length());
                                }
                            });
                            JSONObject info = viajesNetos.getJSONObject(i);

                            data.clear();
                            data.put("uid", info.getString("uid"));
                            data.put("idcamion", info.getString("idcamion"));
                            data.put("idproyecto", info.getString("idproyecto"));

                            if (!tag.create(data)) {
                                return false;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    data.clear();
                    data.put("IMEI", IMEI);
                    data.put("idevento", 1);
                    data.put("latitud", latitude);
                    data.put("longitud", longitude);
                    data.put("fecha_hora", Util.timeStamp());
                    data.put("code", "");

                    Coordenada coordenada = new Coordenada(getApplicationContext());
                    if(!coordenada.create(data)) {
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            super.onPostExecute(success);
            mAuthTask = null;
            loginProgressDialog.dismiss();
            if (success) {
                startActivity(mainActivity);
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            loginProgressDialog.dismiss();
        }
    }
}

