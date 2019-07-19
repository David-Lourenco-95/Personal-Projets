package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

// Classe que permite consultar os testes de uma dada disciplina de um determinado curso
public class ListViewTestes extends AppCompatActivity {

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference collectionReference;

    // Declaração da referência aos documentos das coleções da base de dados
    private DocumentReference documentReference;

    ArrayList<String> arrayList = new ArrayList<>();

    ListView listView;

    String Curso;
    String Disciplina;
    String Teste = "";
    String formato="";

    private ArrayAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_testes);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        listView = (ListView) findViewById(R.id.ListaTestes);

        // Intento que recebe informação de outra atividade relativamente
        // ao curso e disciplina escolhido pelo utilizador
        Intent intent = getIntent();

        if (intent != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                Curso = bundle.getString("curso");
                Disciplina = bundle.getString("disciplina");
            }
        }

        // Declaração do caminho da base de dados
        collectionReference = db.collection("Cursos").document(Curso).collection("Disciplinas").document(Disciplina).collection("Testes");

        //Função que lista os testes de uma determinada disciplina
        collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                    // Os testes são adicionados a um arrayList
                    arrayList.add(documentSnapshot.getId());

                }
                // O arrayList é adicionado a um adapter

                adapter = new ArrayAdapter(ListViewTestes.this, R.layout.adapter_testes ,R.id.nome_teste,arrayList);

                // O adapter é adicionado á listView que mostra a informação ao utilizador
                listView.setAdapter(adapter);


                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "Erro" + e.toString());
            }
        });

        // Função que ao selecionar um teste, o mesmo é mostrado ao utilizador
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                // posição do teste na list view
                Teste = (String) parent.getItemAtPosition(position);

                // pesquisa na base de dados pelo teste
                documentReference = collectionReference.document(Teste);

                documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        // Obtem o campo formato
                        if(documentSnapshot.exists()) {

                            formato = documentSnapshot.getString("Formato");

                        }

                        // Se o teste for uma imagem ou um conjunto de imagens, então vai para a respetiva atividade
                        if(formato.equals(".img")) {

                            Intent iActivity = new Intent(ListViewTestes.this, LoadImagesFirebase.class);

                            Bundle bundle = new Bundle();

                            bundle.putString("curso", Curso);
                            bundle.putString("disciplina", Disciplina);
                            bundle.putString("teste", Teste);

                            iActivity.putExtras(bundle);

                           startActivity(iActivity);
                        }

                        // Se o teste for um PDF , então vai para a respetiva atividade
                        else if(formato.equals(".pdf")){

                            Intent iActivity = new Intent(ListViewTestes.this, LoadPDF.class);

                            Bundle bundle = new Bundle();

                            bundle.putString("curso", Curso);
                            bundle.putString("disciplina", Disciplina);
                            bundle.putString("teste", Teste);

                            iActivity.putExtras(bundle);

                            startActivity(iActivity);

                        }


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

            }
        });
    }

// Função que permite voltar à atividade anterior
   @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(ListViewTestes.this, DescricaoDisciplina.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }


}