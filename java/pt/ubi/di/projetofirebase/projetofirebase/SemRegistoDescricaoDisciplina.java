package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SemRegistoDescricaoDisciplina extends AppCompatActivity {

    // Declaração de variáveis

    private TextView textCurso;
    private TextView textAno;
    private TextView textSemestre;
    private TextView textDocente;

    private String disciplina="";
    private String curso="";

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference collectionReference;

    // Declaração da referência aos documentos das coleções da base de dados
    private DocumentReference documentReference ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sem_registo_descricao_disciplina);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");


        // Declaração de variáveis
        textCurso = (TextView) findViewById(R.id.CampoCurso);
        textAno = (TextView) findViewById(R.id.CampoAno);
        textSemestre = (TextView) findViewById(R.id.CampoSemestre);
        textDocente = (TextView)  findViewById(R.id.CampoDocente);

        // Intento que recebe informação de outra atividade relativamente
        // ao curso e disciplina escolhido pelo utilizador
        Intent intent = getIntent();

        if (intent != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                disciplina = bundle.getString("disciplina");
                curso = bundle.getString("curso");

            }
        }


        // Declaração do caminho da base de dados
        collectionReference= db.collection("Cursos").document(curso).collection("Disciplinas");

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

            }
        });

    }

    // Função que permite consultar os testes de uma determinada disciplina
    public void ConsultarTestes(View view){

        Intent iActivity = new Intent(SemRegistoDescricaoDisciplina.this,ListViewTestesNoRegisto.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso",curso);
        bundle.putString("disciplina",disciplina);

        iActivity.putExtras(bundle);

        startActivity(iActivity);

    }

    // Função que permite consultar os trabalhos de uma determinada disciplina
    public void ConsultarTrabalhos(View view){

        Intent iActivity = new Intent(SemRegistoDescricaoDisciplina.this,ListViewTrabalhosNoRegisto.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso",curso);
        bundle.putString("disciplina",disciplina);

        iActivity.putExtras(bundle);

        startActivity(iActivity);

    }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(SemRegistoDescricaoDisciplina.this, EscolhaSemRegisto.class);

        startActivity(iActivity5);

    }


}
