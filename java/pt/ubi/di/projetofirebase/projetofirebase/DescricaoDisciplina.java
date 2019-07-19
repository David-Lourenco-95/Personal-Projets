package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;
import java.util.Map;

// Classe que permite ao utilizador consultar a informação de uma determinada disciplina
// de um determinado curso, e consequentemente adicionar novos testes ou trabalhos

public class DescricaoDisciplina extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    // Declaração de variáveis

    private DrawerLayout drawer;

    private TextView textCurso;
    private TextView textAno;
    private TextView textSemestre;
    private TextView textDocente;
    TextView Email;

    private  String disciplina = "";
    private String curso = "";

    private Button btnPDF;
    private Button btnImage;
    private Button Cancelar;
    private Button LogOut;

    private AlertDialog dialog;

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    CollectionReference collectionReference,  collectionReference2, collectionReference3;

    // Declaração da referência aos documentos das coleções da base de dados
    private DocumentReference documentReference, documentReference2;

    // Declaração do Firebase Authentication para obter o verficar se o utilizador esta online
    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descricao_disciplina);

        //Criação do menu lateral que vai permitir o acesso a várias funcionalidades
        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer,toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View hView =  navigationView.getHeaderView(0);

        TextView nav_user = (TextView)hView.findViewById(R.id.Email);

        // Aceder ao email do utilizador guardado na share preferences para o colocar no menu lateral
        String email = PreferenceUtils.getEmail2(this);
        nav_user.setText(email);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // Adicionar o nome da aplicação à atividade
        actionBar.setTitle("DocShare");


        // Intento que recebe o curso e disciplina que utilizador escolheu para consultar
     Intent intent = getIntent();

        if (intent != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                curso = bundle.getString("curso");
                disciplina = bundle.getString("disciplina");

            }
        }

        // Declaração de variáveis
       textCurso = (TextView) findViewById(R.id.CampoCurso);
        textAno = (TextView) findViewById(R.id.CampoAno);
        textSemestre = (TextView) findViewById(R.id.CampoSemestre);
        textDocente = (TextView) findViewById(R.id.CampoDocente);

        auth = FirebaseAuth.getInstance();

            // Declaração do caminho da base de dados
            collectionReference = db.collection("Cursos").document(curso).collection("Disciplinas");

            documentReference = collectionReference.document(disciplina);

            // Função que permite consultar a informação de um determinado documento
            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {

                    String curso = documentSnapshot.getString("Nome");
                    String ano = documentSnapshot.getString("Ano de Licenciatura");
                    String semestre = documentSnapshot.getString("Semestre");
                    String docente = documentSnapshot.getString("Docente");

                    textCurso.setText(curso);
                    textAno.setText(ano);
                    textSemestre.setText(semestre);
                    textDocente.setText(docente);

                }

            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("ERORRRRRRRRR","WHYYYYYYYYYYY");
                }
            });
    }


    // Função que permite adicionar um icon de download ao top da barra da aplicação
   @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.preferencia, menu);
        return true;
    }

    // Função que ao clicar nesse icon adiciona o curso e respetiva disciplina como uma preferência
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.Preferencia:

                // adiciona a preferência à lista de preferências do utilizador
                collectionReference2 = db.collection("RegistoAluno").document(auth.getUid()).collection("Preferencias");

                        note n = new note(curso, disciplina);

                        documentReference = collectionReference2.document(disciplina);
                        documentReference.set(n).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", "Criação de documento com Exito");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Criação de documento com Erro");
                            }
                        });

                        // Adiciona o token do dispositivo à base de dados para poder receber notficações
                        collectionReference3 = db.collection("Cursos").document(curso).collection("Disciplinas");
                        documentReference2 = collectionReference3.document(disciplina);
                        Map<String, Object> Token = new HashMap<>();

                        Token.put("Token", FieldValue.arrayUnion(FirebaseInstanceId.getInstance().getToken()));

                        documentReference2.update(Token).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d("TAG", " Token adicionado com exito");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("TAG", "Erro ao adicionar token");
                            }
                        });

                        // Informa o utilizador que a preferência foi criado com sucesso
                Toast.makeText(this, "Disciplina adicionada como favorita!", Toast.LENGTH_SHORT).show();

        }
        return super.onOptionsItemSelected(item);
    }


    // Função que permite consultar os testes de uma determinada disciplina
    public void ConsultarTestes(View view){

        if(auth.getCurrentUser()!= null) {

            Intent iActivity = new Intent(getApplicationContext(), ListViewTestes.class);

            Bundle bundle = new Bundle();

            bundle.putString("curso", curso);
            bundle.putString("disciplina", disciplina);

            iActivity.putExtras(bundle);

            startActivity(iActivity);
        }
    }

    // Função que permite consultar os trabalhos de uma determinada disciplina
    public void ConsultarTrabalhos(View view){
        if(auth.getCurrentUser()!= null) {

            Intent iActivity = new Intent(getApplicationContext(), ListViewTrabalhos.class);

            Bundle bundle = new Bundle();

            bundle.putString("curso", curso);
            bundle.putString("disciplina", disciplina);

            iActivity.putExtras(bundle);

            startActivity(iActivity);

        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        if(auth.getCurrentUser()!= null) {
        switch (item.getItemId()) {
            //Opção que permite adicionar um teste
            case R.id.nav_photo:

                // Janela de dialgo que permite ao utilizador escolher uma opção
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(DescricaoDisciplina.this);
                View mView = getLayoutInflater().inflate(R.layout.layout_dialog, null);
                btnPDF = (Button) mView.findViewById(R.id.btnDialogPDF);
                btnImage = (Button) mView.findViewById(R.id.btnDialogImage);

                // Mostrar a janela de dialgo
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();

                // Opção que permite adicionar uma novo teste no formato PDF
                btnPDF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iActivity = new Intent(getApplicationContext(), UploadPDF.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("curso", curso);
                        bundle.putString("disciplina", disciplina);

                        iActivity.putExtras(bundle);

                        startActivity(iActivity);
                        dialog.dismiss();
                    }
                });

                // Opção que permite adicionar uma novo teste no formato de imagem
                btnImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iActivity = new Intent(getApplicationContext(), UploadImage.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("curso", curso);
                        bundle.putString("disciplina", disciplina);

                        iActivity.putExtras(bundle);

                        startActivity(iActivity);
                        dialog.dismiss();
                    }
                });

                break;

            //Opção que permite aceder a lista de testes adicionados pelo utilizador
            case R.id.nav_Remover:

                Intent iActivity3 = new Intent(getApplicationContext(), ListaTestesAdicionados.class);
                Bundle bundle3 = new Bundle();

                bundle3.putString("curso", curso);
                bundle3.putString("disciplina", disciplina);

                iActivity3.putExtras(bundle3);

                startActivity(iActivity3);

                break;

           // Opção que permite consultar a lista de preferências do utilizador
            case R.id.nav_Preferencia:

                Intent iActivity = new Intent(getApplicationContext(), ListViewDisciplinas.class);

                startActivity(iActivity);

                break;

            //Opção que permite adicionar um trabalho
            case R.id.nav_foto_trabalho:

                // Janela de dialgo que permite ao utilizador escolher uma opção
                AlertDialog.Builder mBuilder2 = new AlertDialog.Builder(DescricaoDisciplina.this);
                View mView2 = getLayoutInflater().inflate(R.layout.layout_dialog, null);
                btnPDF = (Button) mView2.findViewById(R.id.btnDialogPDF);
                btnImage = (Button) mView2.findViewById(R.id.btnDialogImage);

                // Mostrar a janela de dialgo
                mBuilder2.setView(mView2);
                dialog = mBuilder2.create();
                dialog.show();

                // Opção que permite adicionar uma novo trabalho no formato PDF
                btnPDF.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iActivity = new Intent(getApplicationContext(), UploadPdfTrabalho.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("curso", curso);
                        bundle.putString("disciplina", disciplina);

                        iActivity.putExtras(bundle);

                        startActivity(iActivity);
                        dialog.dismiss();
                    }
                });

                // Opção que permite adicionar uma novo trabalho no formato de imagem
                btnImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent iActivity = new Intent(getApplicationContext(), UploadImageTrabalho.class);
                        Bundle bundle = new Bundle();

                        bundle.putString("curso", curso);
                        bundle.putString("disciplina", disciplina);

                        iActivity.putExtras(bundle);

                        startActivity(iActivity);
                        dialog.dismiss();
                    }
                });

                break;


            //Opção que permite aceder a lista de trabalhos adicionados pelo utilizador
            case R.id.nav_remove_trabalho:

                Intent iActivity5 = new Intent(getApplicationContext(), ListaTrabalhosAdicionados.class);

                Bundle bundle = new Bundle();

                bundle.putString("curso", curso);
                bundle.putString("disciplina", disciplina);

                iActivity5.putExtras(bundle);

                startActivity(iActivity5);

                break;
            //Opção que permite aceder à atividade para contactar o adminstrador
            case R.id.nav_Suporte:

             Intent intent = new Intent(DescricaoDisciplina.this, SendEmail.class);
             startActivity(intent);

            break;
            // Opção que permite fazer logout da aplicação
            case R.id.nav_Logout:

                if (auth.getCurrentUser() != null) {
                    AlertDialog.Builder mBuilder3 = new AlertDialog.Builder(DescricaoDisciplina.this);
                    View mView3 = getLayoutInflater().inflate(R.layout.dialog_logout, null);


                    mBuilder3.setView(mView3);
                    dialog = mBuilder3.create();
                    dialog.show();

                    Cancelar = (Button) mView3.findViewById(R.id.btnDialogCancelar);
                    LogOut = (Button) mView3.findViewById(R.id.btnDialogLogout);

                    // Limpa as share preferences para que quando voltar ao usar a aplicação seja
                    // mostrado o menu de login
                    LogOut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            PreferenceUtils.saveEmail(null,DescricaoDisciplina.this);

                            FirebaseAuth.getInstance().signOut();


                            Intent iActivity4 = new Intent(DescricaoDisciplina.this, LoginActivity.class);
                            startActivity(iActivity4);
                            dialog.dismiss();

                        }
                    });

                    Cancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                }
                break;


        }

        drawer.closeDrawer(GravityCompat.START);

    }
        return true;
    }

    // Função que ao clicar na seta back do dispositivo volta para a atividade anterior
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START);
        }else {
            super.onBackPressed();
            Intent iActivity4 = new Intent(DescricaoDisciplina.this, Escolha.class);
            startActivity(iActivity4);
        }
    }


}
