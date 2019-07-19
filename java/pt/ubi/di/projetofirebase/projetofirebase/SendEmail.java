package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

// Função que permite mandar um email ao administrador
public class SendEmail extends AppCompatActivity {

    private TextView emailDestino;
    private EditText assunto, emailTexto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_email);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        emailDestino = (TextView) findViewById(R.id.EmailDestino);
        assunto = (EditText) findViewById(R.id.Assunto);
        emailTexto = (EditText) findViewById(R.id.TextoEmail);
    }

    // Função que guarda os valores inseridos nos campos de texto
    // pelo utilizador
    public void EnviarEmail(View view){

        String email = emailDestino.getText().toString().trim();
        String titulo =  assunto.getText().toString().trim();
        String texto = emailTexto.getText().toString().trim();

        Send(email, titulo, texto);

    }

    // Função que faz um intento e pergunta ao sistema operacional
    // quais as aplicações que permitem enviar emails
    private void Send(String email, String titulo, String texto){

        Intent intent = new Intent(Intent.ACTION_SEND);

        intent.setData(Uri.parse("mailto:"));
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_EMAIL, new String[] {email});
        intent.putExtra(Intent.EXTRA_SUBJECT,titulo);
        intent.putExtra(Intent.EXTRA_TEXT, texto);

        try {

            startActivity(Intent.createChooser(intent,"Escolha um email"));


        }
        catch (Exception e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

}
