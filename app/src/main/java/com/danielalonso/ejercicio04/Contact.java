package com.danielalonso.ejercicio04;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Contact extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private TextInputEditText nombre;
    private TextInputEditText correo;
    private TextInputEditText mensaje;
    private MaterialButton btnEnviar;
    private String sEmail;
    private String sPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        nombre = findViewById(R.id.nombre);
        correo = findViewById(R.id.correo);
        mensaje = findViewById(R.id.mensaje);
        btnEnviar = findViewById(R.id.btnEnviar);

        //Remplazar por correo y contraseña
        sEmail = "correo";
        sPassword = "contraseña";

        btnEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Inicializa las propiedades
                Properties properties = new Properties();
                properties.put(getString(R.string.smtp_host), getString(R.string.smtp_gmail));
                properties.put(getString(R.string.smtp_socketFactory_port), getString(R.string.port));
                properties.put(getString(R.string.smtp_socketFactory_class), getString(R.string.net_ssl));
                properties.put(getString(R.string.smtp_port), getString(R.string.port));
                properties.put(getString(R.string.smtp_auth), getString(R.string.auth_true));

                // Inicializa la sesión
                Session session = Session.getDefaultInstance(properties, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(sEmail, sPassword);
                    }
                });


                try {
                    // Inicializa el contenido del correo
                    Message message = new MimeMessage(session);
                    // Quien envia mensaje
                    message.setFrom(new InternetAddress(sEmail));

                    // Recipiente email
                    // Se puede remplazar sEmail para contestarle al usuario que nos pondremos en contacto proximamente
                    // En este caso se envía un correo al mismo correo con el que se inicia sesión
                    message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(sEmail));
                    // Email subject o Nombre del usuario
                    message.setSubject(nombre.getText().toString().trim());
                    // Cuerpo del correo
                    message.setText(getString(R.string.from) +
                            correo.getText().toString().trim() +
                            getString(R.string.body) +
                            mensaje.getText().toString().trim());

                    // Enviar email
                    new SendEmail().execute(message);

                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        });

        //Agregando toolbar a la pantalla principal
        toolbar = findViewById(R.id.toolbar_contact);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private class SendEmail extends AsyncTask<Message, String, String> {
        //Initialize progress dialog
        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(Contact.this,
                    getString(R.string.espere),
                    getString(R.string.enviando),
                    true,
                    false
            );
        }

        @Override
        protected String doInBackground(Message... messages) {
            try {
                Transport.send(messages[0]);
                return getString(R.string.envio_exitoso);
            } catch (MessagingException e) {
                e.printStackTrace();
                return getString(R.string.error);
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //Dismiss progress dialog
            progressDialog.dismiss();
            if (s.equals(getString(R.string.envio_exitoso))) {
                //When success
                //Initialize alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Contact.this);
                builder.setCancelable(false);
                builder.setTitle(Html.fromHtml(getString(R.string.envio_exitoso_html)));
                builder.setMessage(getString(R.string.envio_correcto));
                builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        correo.setText("");
                        nombre.setText("");
                        mensaje.setText("");
                    }
                });
                //Show alert dialog
                builder.show();
            } else {
                //When error
                Toast.makeText(getApplicationContext(), getString(R.string.intente_mas_tarde), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.aboutMe:
                // User chose the "Settings" item, show the app settings UI...
                Intent about = new Intent(this, About.class);
                startActivity(about);
                break;

        }
        return super.onOptionsItemSelected(item);
    }
}