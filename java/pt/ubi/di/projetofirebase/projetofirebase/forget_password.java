package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

// Função que permite atribuir uma nova password caso o utilizador se tenha esquecido
public class forget_password extends AppCompatActivity {

    EditText email;
    Button btnNewPass;
    FirebaseAuth firebaseAuth;

    String email_password="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        email = (EditText) findViewById(R.id.uyeEmail);
        btnNewPass = (Button) findViewById(R.id.yeniParolaGonder);


        firebaseAuth = FirebaseAuth.getInstance();
        btnNewPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_password = email.getText().toString();

                // Se o campo de texto estiver vazio, pede ao utilizador para inserir um email
                if(TextUtils.isEmpty(email_password)){
                    Toast.makeText(getApplicationContext(),"Insira um email!",Toast.LENGTH_SHORT).show();
                    return;
                }

                // Assim que o utilizador insere um email, basta consultar o seu correio eletronico
                // e definir a nova palavra passe para esse email
                firebaseAuth.sendPasswordResetEmail(email_password)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    Toast.makeText(getApplicationContext(),"Um link para adicionar uma nova password foi enviado para o seu email",Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(getApplicationContext(),"Email inválido!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(forget_password.this, LoginActivity.class);

        startActivity(iActivity5);

    }
}
