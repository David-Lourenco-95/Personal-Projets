package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import static android.app.Service.START_NOT_STICKY;

public class LoginActivity extends Activity {

    private EditText inputEmail, inputPassword;
    private FirebaseAuth auth;
    private Button btnRegistar, btnLogin;
    private ProgressDialog PD;
    private String email;
    private String password;

    // Conexão à Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Barra de progressão que é mostrada quando o utilizador espera que que o sistema confirme a autenticação
        PD = new ProgressDialog(this);
        PD.setMessage("Loading...");
        PD.setCancelable(true);
        PD.setCanceledOnTouchOutside(false);

        auth = FirebaseAuth.getInstance();

        // Verfica se o utilizador já efetuou o login para assim náo voltar ao pedir
        // quando aceder novamanete à aplicação
         if(PreferenceUtils.getEmail2(this) != null){

                    Intent intent = new Intent(LoginActivity.this, Escolha.class);
                    startActivity(intent);
                }


        // Declaração de variáveis que que recebem dados que entrada
        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnRegistar = (Button) findViewById(R.id.sign_up_button);
        btnLogin = (Button) findViewById(R.id.sign_in_button);

        // Função que ao efetuar o login, verifica se os dados do utilizador são válidos
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               email = inputEmail.getText().toString();
                boolean emailFormat = android.util.Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches();
               password = inputPassword.getText().toString();


                try {
                        // Se os dados forem válidos o utilizador têm acesso á aplicação
                    if (password.length() > 0 && email.length() > 0) {

                        if(emailFormat != true){

                            Toast.makeText(LoginActivity.this, "Email mal formatado!", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        PD.show();
                        auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {

                                                // Guada-se o ID do utilizador
                                                FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                                                Map<String, Object> Utilizador = new HashMap<>();

                                                Utilizador.put("Email", currentFirebaseUser.getEmail());

                                                // Guarda o email na share preferances
                                                PreferenceUtils.saveEmail(email,LoginActivity.this);


                                                // Assim que o utlizador efetuada o login com sucesso, é criado um documento na database com o seu ID único
                                                db.collection("RegistoAluno").document(currentFirebaseUser.getUid()).set(Utilizador).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                      startActivity(new Intent(LoginActivity.this, Escolha.class));
                                                       finish();

                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("ERRO", "Error writing document", e);
                                                            }
                                                        });


                                        }
                                        PD.dismiss();
                                    }

                                }).addOnFailureListener(new OnFailureListener() {
                            // Caso contrario o utilizador é notifcado sobre a causa da falha do login
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                // Condições de vários casos quando o login falha
                           if (e instanceof FirebaseAuthInvalidUserException) {

                               String errorCode =
                                       ((FirebaseAuthInvalidUserException) e).getErrorCode();

                               if (errorCode.equals("ERROR_USER_NOT_FOUND")) {

                                   Toast.makeText(LoginActivity.this, "Email Inválido", Toast.LENGTH_LONG).show();
                               }
                           }

                                    else if (e instanceof FirebaseAuthInvalidCredentialsException){
                                    Toast.makeText(LoginActivity.this,"Password Incorreta", Toast.LENGTH_LONG).show();
                            }

                            }
                        });
                    }
                    // Verefica se os campos de entrada estão vazios
                    else {
                        Toast.makeText(LoginActivity.this, "Preencha os campos para efetuar o login", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        // Função que permite aceder à aplicação sem registo
        btnRegistar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, EscolhaSemRegisto.class);
                startActivity(intent);
            }
        });

    }
    // Função que permite aceder à atividade de criar um registo
    public void Registar(View view){
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    // Função que permite aceder à atividade de gerar uma nova password
    public void ForgertPassword(View view){
        Intent intent = new Intent(LoginActivity.this, forget_password.class);
        startActivity(intent);
    }
    // Função que permite aceder à atividade de contactar o administrador
    public void Suporte(View view){
        Intent intent = new Intent(LoginActivity.this, SendEmail.class);
        startActivity(intent);
    }


   /* public class ServiceAlarms extends Service {
        @Override
        public int onStartCommand
                (Intent intent, int flags, int startId) {
            Toast.makeText(this, "Service Started ! ", Toast.LENGTH_SHORT).show();
            return START_NOT_STICKY;
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

    }*/


}