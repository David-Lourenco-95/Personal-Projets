package pt.ubi.di.projetofirebase.projetofirebase;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// Classe que lista as preferências do utilizador, ou seja, os cursos e respectivas disciplinas
// em que está interessado
public class ListViewDisciplinas extends AppCompatActivity {

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da FirebaseAuth que permite verificar se um utilizador esta registado
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    String curso="",disciplina="";

    Button btnDescricao, btnDeleteDisciplina, btnCancelar, btnApagar;

    AlertDialog dialog, dialog2;

    private CollectionReference  collectionReference, collectionReference2;
    private DocumentReference documentReference, documentReference2;

    ArrayList<String> arrayList = new ArrayList<>();

    private ArrayAdapter adapter;

    ListView listView;

    String preferencia = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_disciplinas);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        listView = (ListView) findViewById(R.id.ListaDisciplinas);

        // Intento que recebe informação de outra atividade relativamente
        // ao curso e disciplina escolhido pelo utilizador

        // Declaração do caminho da base de dados
        if(auth.getCurrentUser() != null) {

            collectionReference = db.collection("RegistoAluno").document(auth.getUid()).collection("Preferencias");

            // Função que lista as preferências do utilizador
            collectionReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {

                        String curso  = documentSnapshot.getString("curso");

                      String disciplina = documentSnapshot.getString("disciplina");

                      String merge = curso + "\n" + disciplina;

                        arrayList.add(merge);

                    }
                    // Se o utilizador não tiver adicionado preferências uma mensagem é lhe mostrada
                    if(arrayList.isEmpty()) {
                        arrayList.add("Sem preferências adicionadas");
                        adapter = new ArrayAdapter(ListViewDisciplinas.this, R.layout.lista_preferencia_vazia, R.id.textovazio, arrayList);
                    }


                        // Adiciona a infomação ao arrayList que de seguida é adcionado ao adapter

                    adapter = new ArrayAdapter(ListViewDisciplinas.this, R.layout.adapter_testes ,R.id.nome_teste,arrayList);

                        // O adapter é adicionado á listView que mostra a informação
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();


                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                }
            });

            // Função que permite selecionar uma preferência e ou consultar a sua informação
            // ou eleminar a preferência da lista do utilizador
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(final AdapterView<?> parent, View view, final int position, long id) {

                    preferencia = (String) parent.getItemAtPosition(position);

                            // Se o utilizador selecionar um campo vazio nada acontece
                            if(preferencia.equals("Sem preferências adicionadas")){
                                return;
                            }

                     // Guarda os campos selecionados
                    curso = preferencia.substring(0, preferencia.indexOf("\n"));

                     disciplina = preferencia.substring(preferencia.indexOf("\n") + 1, preferencia.length());

                     // Caixa de dialgo que pergunta a operação que o utilizador deseja efetuar
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(ListViewDisciplinas.this);
                    View mView = getLayoutInflater().inflate(R.layout.dialog_remove_preferencia, null);

                    mBuilder.setView(mView);
                    dialog = mBuilder.create();
                    dialog.show();

                    btnDeleteDisciplina = (Button) mView.findViewById(R.id.btnDialogApagar);
                    btnDescricao = (Button) mView.findViewById(R.id.btnDialogDescricao);

                    // Botão que permite pesquisar pela sua preferência
                    btnDescricao.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            Intent intent = new Intent(ListViewDisciplinas.this,DescricaoDisciplina.class);

                            Bundle bundle = new Bundle();
                            bundle.putString("curso", curso);
                            bundle.putString("disciplina", disciplina);

                            intent.putExtras(bundle);
                            startActivity(intent);
                            dialog.dismiss();

                        }
                    });

                    // Botão que mostra outra caixa de dialgo relativamente à opção de eleminar a preferência
                    btnDeleteDisciplina.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            AlertDialog.Builder mBuilder = new AlertDialog.Builder(ListViewDisciplinas.this);
                            View mView = getLayoutInflater().inflate(R.layout.dialog_remove_preferencia_final, null);

                            mBuilder.setView(mView);
                            dialog2 = mBuilder.create();
                            dialog2.show();

                            btnCancelar = (Button) mView.findViewById(R.id.btnDialogCancelar);
                            btnApagar = (Button) mView.findViewById(R.id.btnDialogApagarFinal);

                            btnCancelar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog2.dismiss();
                                }
                            });

                            // Se o utilizador confirmar que pretende eleminar a preferência a mesma
                            // é eleminada da base de dados tal como o token do dispositivo
                            btnApagar.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    // caminho da base de dados da lista de preferências do utilizador
                                    collectionReference = db.collection("RegistoAluno").document(auth.getUid()).collection("Preferencias");

                                    documentReference = collectionReference.document(disciplina);

                                    // caminho da base de dados do curso e disciplina a que corresponde a preferência
                                    collectionReference2 = db.collection("Cursos").document(curso).collection("Disciplinas");
                                    documentReference2 = collectionReference2.document(disciplina);
                                    Map<String, Object> Token = new HashMap<>();

                                    // remoção do token da base de dados para não receber mais notificações
                                    Token.put("Token", FieldValue.arrayRemove(FirebaseInstanceId.getInstance().getToken()));

                                    documentReference2.update(Token).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("TAG", " Token removido com exito");
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("TAG", "Erro ao remover token");
                                        }
                                    });

                                    // remoção da preferência da base de dados
                                    documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {

                                                // atualização da list view no dispositivo do utilizador
                                                arrayList.remove(position);
                                                dialog.dismiss();
                                                adapter.notifyDataSetChanged();

                                                Toast.makeText(ListViewDisciplinas.this, "Preferência Removida com sucesso", Toast.LENGTH_LONG).show();
                                                dialog2.dismiss();
                                                dialog.dismiss();
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
                    });
                }
            });

            }
        }



    // Intento que permite adicionar uma nova preferência
    public void AddPreferencia(View view){

        if (auth.getCurrentUser() != null) {
            Intent intent = new Intent(ListViewDisciplinas.this, Escolha.class);
            startActivity(intent);
        }
    }

}
