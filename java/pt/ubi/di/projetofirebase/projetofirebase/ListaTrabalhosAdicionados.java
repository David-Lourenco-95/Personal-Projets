package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class ListaTrabalhosAdicionados extends AppCompatActivity {

    // Declaração da FirebaseAuth que permite verificar se um utilizador esta registado
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da Storage no Cloud Firestore que permite adicionar images e ficheiros

    private FirebaseStorage storage;

    private StorageReference teste;

    private String  Curso="",Disciplina="", aluno="",TrabalhoRemover="", val="",linkPDF="", formato="";

    private Button btnRemover;
    private Button btnCancelar;

    private AlertDialog dialog;

    private CollectionReference collectionReference;
    private DocumentReference documentReference2;

    private DocumentReference documentReference ;

    private ArrayList<String> arrayList = new ArrayList<>();
    private  ArrayList<String> Link = new ArrayList<>();

    private ArrayAdapter adapter;

    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_trabalhos_adicionados);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        //nome da aplicação
        actionBar.setTitle("DocShare");

        listView = (ListView) findViewById(R.id.ListaDisciplinas);

        storage = FirebaseStorage.getInstance();

        // Vereficar qual é o utilizador no momento
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        aluno = currentFirebaseUser.getUid();

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
        collectionReference = db.collection("Cursos").document(Curso)
                .collection("Disciplinas").document(Disciplina).collection("Trabalhos");

        // Query que permite listar os trabalhos adicionados por um determiando utilizaodor
        collectionReference.whereEqualTo("Utilizador",aluno).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                for(QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){

                    String curso =  documentSnapshot.getString("Curso");
                    String disciplina = documentSnapshot.getString("Disciplina");
                    String nome = documentSnapshot.getString("Trabalho");
                    String merge = curso+"\n"+ disciplina+" \n"+ nome;

                    arrayList.add(merge);

                }
                // Adapter que adiciona a arraylist com a informação à listView
                adapter = new ArrayAdapter(ListaTrabalhosAdicionados.this, R.layout.adapter_testes ,R.id.nome_teste,arrayList);


                listView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


        // Função que permite remover um trabalho que foi adicionado por um determinado utilizador
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if (auth.getCurrentUser() != null) {

                    // Guarda a posição do elemento na listView
                    val = (String) parent.getItemAtPosition(position);

                    TrabalhoRemover = val.substring(val.indexOf(" \n") + 2, val.length());

                    // Caixa de dialogo que pergunta ao utilizador se pretende eleminar o trabalho selecionado
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(ListaTrabalhosAdicionados.this);
                    View mView = getLayoutInflater().inflate(R.layout.dialog_remove_trabalho_adicionado, null);

                    mBuilder.setView(mView);
                    dialog = mBuilder.create();
                    dialog.show();

                    btnRemover = (Button) mView.findViewById(R.id.btnDialogApagar);
                    btnCancelar = (Button) mView.findViewById(R.id.btnDialogCancelar);

                    btnCancelar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            dialog.dismiss();
                        }
                    });

                    btnRemover.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                    // Função que remove o trabalho selecionado
                            documentReference = collectionReference.document(TrabalhoRemover);
                            documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {

                                    // Verifica se o documento a remover é uma imagem
                                    formato = documentSnapshot.getString("Formato");

                                    if (formato.equals(".img")) {

                                        Link = (ArrayList<String>) documentSnapshot.get("urlImagem");
                                        for (String s : Link) {
                                            Log.d("Array", s);

                                            teste = storage.getReferenceFromUrl(s);
                                            Log.d("STORAGE", String.valueOf(teste));


                                            // Apaga o imagem da Storage
                                            teste.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // File deleted successfully
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception) {
                                                    // Uh-oh, an error occurred!
                                                }
                                            });
                                        }

                                        // Remove o documento da base de dados
                                        documentReference2 = collectionReference.document(TrabalhoRemover);
                                        documentReference2.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    arrayList.remove(position);
                                                    dialog.dismiss();
                                                    adapter.notifyDataSetChanged();
                                                    Toast.makeText(ListaTrabalhosAdicionados.this, "Trabalho removido com sucesso", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                                        // Verifica se o documento a remover é um PDF
                                    } else if (formato.equals(".pdf")){

                                        linkPDF = documentSnapshot.getString("urlPDF");

                                        teste = storage.getReferenceFromUrl(linkPDF);
                                        Log.d("STORAGE", String.valueOf(teste));


                                        // Apaga o PDF da Storage
                                        teste.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception exception) {

                                            }
                                        });

                                        // Remove o documento da base de dados
                                        documentReference2 = collectionReference.document(TrabalhoRemover);
                                        documentReference2.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()) {

                                                    arrayList.remove(position);
                                                    dialog.dismiss();
                                                    adapter.notifyDataSetChanged();
                                                    Toast.makeText(ListaTrabalhosAdicionados.this, "Trabalho removido com sucesso", Toast.LENGTH_LONG).show();

                                                }

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                            }
                                        });

                                    }
                                }




                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });

                            dialog.dismiss();
                        }
                    });
                }
            }
        });

    }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(ListaTrabalhosAdicionados.this, DescricaoDisciplina.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }
}
