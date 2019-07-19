package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class EscolhaSemRegisto extends AppCompatActivity {


    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference collectionReference;

    private CollectionReference collectionReference2;

    // Declaração da listas que vão popular os spinners
    final List<String> areas = new ArrayList<String>();
    final List<String> areas2 = new ArrayList<String>();

    private Spinner areaSpinnerCurso, areaSpinnerDisciplina;

    TextView Texto;

    private String Curso2 = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sem_registo_escolha);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        areaSpinnerCurso = (Spinner) findViewById(R.id.spinnerCurso);

        areaSpinnerDisciplina = (Spinner) findViewById(R.id.spinnerCadeira);

        Texto = (TextView) findViewById(R.id.texto);


        // Declaração do caminho da base de dados
        collectionReference = db.collection("Cursos");

        areas.add(0, "Escolha");

        // Função que adiciona os cursos ao spinner
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    // ID dos documentos que representam os cursos
                    areas.add(documentSnapshot.getId());

                }

                // ArrayList que contem os cursos que são adicionados ao Adapter no spinner
                ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(EscolhaSemRegisto.this, android.R.layout.simple_spinner_item, areas);
                areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                areaSpinnerCurso.setAdapter(areasAdapter);

            }
        });

        // Função que preenche o segundo spinner com as disciplinas do curso selecionado
        // no primeiro spinner
        areaSpinnerCurso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                areas2.clear();
                areas2.add(0, "Escolha");

                Curso2 = areaSpinnerCurso.getSelectedItem().toString();

                collectionReference2 = collectionReference.document(Curso2).collection("Disciplinas");

                // Função que adiciona as disciplinas ao spinner
                collectionReference2.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // ID dos documentos que representam as disciplinas
                            areas2.add(documentSnapshot.getId());

                        }

                        // ArrayList que contém as disciplinas que são adicionadas ao Adapter no spinner
                        ArrayAdapter<String> areasAdapter = new ArrayAdapter<String>(EscolhaSemRegisto.this, android.R.layout.simple_spinner_item, areas2);
                        areasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        areaSpinnerDisciplina.setAdapter(areasAdapter);
                    }
                });

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {

            }

        });
    }

    public void Teste(View view) {

       String Curso = areaSpinnerCurso.getSelectedItem().toString();
       String  Disciplina = areaSpinnerDisciplina.getSelectedItem().toString();

        // Condição que impossibilita a escolher a opção "Escolha" como curso ou disciplina
        if (Curso != ("Escolha") && Disciplina.equals("Escolha") || Curso.equals("Escolha") && Disciplina != ("Escolha") ||
                Curso.equals("Escolha") && Disciplina.equals("Escolha")) {

            Toast.makeText(this, "Escolha é inválido!!", Toast.LENGTH_SHORT).show();
            return;
        }
        // Intento que pesquisa pela curso e disciplina selecionado
                    Intent iActivity = new Intent(EscolhaSemRegisto.this, SemRegistoDescricaoDisciplina.class);

                    Bundle bundle = new Bundle();

                    bundle.putString("curso", Curso);
                    bundle.putString("disciplina", Disciplina);

                    iActivity.putExtras(bundle);

                    startActivity(iActivity);
        }

        // Função que ao clicar na seta back vai para a atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(EscolhaSemRegisto.this, LoginActivity.class);

        startActivity(iActivity5);

    }

    }

