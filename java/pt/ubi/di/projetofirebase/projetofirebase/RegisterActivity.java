package pt.ubi.di.projetofirebase.projetofirebase;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;


public class RegisterActivity extends Activity {

    // Declaração de variáveis
    private EditText inputEmail, inputPassword, repeatPassword;
    private FirebaseAuth auth;
    private Button btnRegistar;
    private ProgressDialog PD;

    @Override  protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Barra de progressão que é mostrada quando o utilizador espera que que o sistema efetue o registo
        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);


        // Conexão à Cloud Firestore
        auth = FirebaseAuth.getInstance();

       // Declaração de variáveis que que recebem dados que entrada
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        repeatPassword = (EditText) findViewById(R.id.Repeatpassword);
        btnRegistar = (Button) findViewById(R.id.btnregistar);

        // Função que ao efetuar o registo verifica se os dados do utilizador são válidos e valida o registo
        btnRegistar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = inputEmail.getText().toString();
                boolean emailFormat = android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches();
                final String password = inputPassword.getText().toString();
                final String repeatpassword = repeatPassword.getText().toString();

                try {
                    // Verifica se os campos de texto estão vazios
                    if (password.length() > 0 && repeatpassword.length() > 0 && email.length() > 0) {

                        if(emailFormat != true){

                            // Verifica se o email do utilizador têm o formato dum email
                            Toast.makeText(RegisterActivity.this, "Email mal formatado!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Verifica se a password têm no minimo 6 caracteres
                        if(password.length() < 6 || repeatpassword.length() < 6){
                            Toast.makeText(RegisterActivity.this, "Password têm de ter no mínimo 6 caracteres!", Toast.LENGTH_SHORT).show();
                            return;
                        }



                        if(password.equals(repeatpassword)){

                            PD.show();
                            // Função que cria o registo dum utilizador
                            auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            // Quando o registo é efetuado com sucesso o utilizador é notificado
                                            if (task.isSuccessful()) {

                                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                startActivity(intent);

                                                PD.dismiss();

                                            }

                                        else{
                                                PD.dismiss();
                                                Toast.makeText(RegisterActivity.this, "Failed to create user:"+task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        }
                                        // No caso de ocorer uma falha de registo o utilizador é notificado sobre a causa
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                    Toast.makeText(RegisterActivity.this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        else { // Verefica se a confirmação da password é valida
                            Toast.makeText(RegisterActivity.this, "Passwords não coincidem!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    else {
                        Toast.makeText(
                                RegisterActivity.this,
                                "Preencha os campos para efetuar o registo",
                                Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }
// Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(RegisterActivity.this, LoginActivity.class);

        startActivity(iActivity5);

    }

}
