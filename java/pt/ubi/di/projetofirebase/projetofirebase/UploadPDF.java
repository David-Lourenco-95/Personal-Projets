package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Classe que permite efetuar o upload dum ficheiro PDF, no dispositivo móvel do utilizador,
// para a base de dados na Cloud Firestore
public class UploadPDF extends AppCompatActivity {

    // Declaração da FirebaseAuth que permite verificar se um utilizador esta registado
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final int RESULT_PDF = 4;

    private Button BtnLoad;
    private ImageButton BtnPDF;
    private EditText Nome;

    private RecyclerView mUploadList;

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private int STORAGE_PERMISSION_CODE = 200;

    private UploadListAdapter uploadListAdapter;

    // Declaração da base de dados Cloud Firestore
  private FirebaseFirestore mDatabaseRef = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference notebookRef;

   private String fileName = "", aluno="", Curso="",Disciplina="",NomeTeste="";

   // Declaração de uma Task para efetuar operações na Storage
   StorageTask mUlploadTask;

   // Declaração à Storage para guardar imagens e ficheiros
    private FirebaseStorage storage;
    private StorageReference storageReference;

    int finalI;
    Uri uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_pdf);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

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

        // Vereficar qual é o utilizador atual
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        aluno = currentFirebaseUser.getUid();

        // Declaração do caminho da base de dados
        notebookRef = mDatabaseRef.collection("Cursos").document(Curso).collection("Disciplinas").document(Disciplina).collection("Testes");

        BtnLoad = (Button) findViewById(R.id.btnLoad);

        BtnPDF = (ImageButton) findViewById(R.id.btnPDF);

        Nome = (EditText)  findViewById(R.id.Edit_Teste);

        mUploadList = (RecyclerView) findViewById(R.id.upload_list);

        // arrays que vão guardar as imagens a ser adicionadas à base de dados
        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();

        uploadListAdapter = new UploadListAdapter(fileNameList, fileDoneList);

        mUploadList.setLayoutManager(new LinearLayoutManager(this));

        mUploadList.setHasFixedSize(true);
        mUploadList.setAdapter(uploadListAdapter);


        // Intento para selecionar um PDF do dispositivo móvel do utilizador
        BtnPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(UploadPDF.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select PDF file"), RESULT_PDF);
                }
                else {
                    requestStoragePermission();
                }
            }
        });

    }

    // Função que recebe um pdf e faz o upload do mesmo para a base de dados na Cloud Firestore
    @Override
    protected void onActivityResult( final int requestCode,  final int resultCode,  final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // limpa a RecycleView
        mUploadList.setAdapter(null);

        // Se o utilizador tentar fazer upload selecionar um ficheiro é retornado uma mensagem
        if(data == null){
            Toast.makeText(UploadPDF.this, "Escolha um ficheiro!", Toast.LENGTH_SHORT).show();
            return;
        }

        BtnLoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Limpa as listas antes de receber novos ficheiros
                fileNameList.clear();
                fileDoneList.clear();

                if (requestCode == RESULT_PDF && resultCode == RESULT_OK) {

                    if (auth.getCurrentUser() != null) {

                        if (data.getData() != null) {

                            // Guarda o nome dado ao ficheiro pelo utilizador
                            NomeTeste = Nome.getText().toString();


                            uri = data.getData();

                            if (uri != null) {

                                finalI = 0;

                                // Obtêm o uri do ficheiro
                                fileName = getFileName(uri);

                                // adiciona o nome do ficheiro á lista que depois é mostrada na recycleView no
                                // momento do upload
                                fileNameList.add(fileName);
                                fileDoneList.add("uploading");

                                mUploadList.setAdapter(uploadListAdapter);

                                uploadListAdapter.notifyDataSetChanged();

                                // Referência à Storage para guardar o ficheiro
                                StorageReference Diretoria = storageReference.child("PDFs");
                                StorageReference fileToUpload = Diretoria.child( NomeTeste).child(fileName);

                                mUlploadTask = fileToUpload.putFile(uri).
                                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                fileDoneList.remove(finalI);
                                                fileDoneList.add(finalI, "done");

                                                uploadListAdapter.notifyDataSetChanged();

                                                // Depois do ficheiro estar armazenado na storage é obtido o seu url de download
                                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();
                                                Map<String, Object> UrlPDF = new HashMap<>();

                                                // O url é guardado na base de dados com o resto da informação
                                                // relativa a esse mesmo ficheiro
                                                while (!urlTask.isSuccessful()) ;
                                                Uri downloadUrl = urlTask.getResult();
                                                String url = downloadUrl.toString();

                                                UrlPDF.put("Curso", Curso);
                                                UrlPDF.put("Disciplina", Disciplina);
                                                UrlPDF.put("Teste", NomeTeste);
                                                UrlPDF.put("urlPDF", url);
                                                UrlPDF.put("Formato", ".pdf");
                                                UrlPDF.put("Utilizador", aluno);
                                                UrlPDF.put("Identificador" , "Teste");
                                                UrlPDF.put("Token" , FirebaseInstanceId.getInstance().getToken());

                                                notebookRef.document(NomeTeste).set(UrlPDF).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Nome.getText().clear();

                                                        data.setData(null);

                                                        Log.d("hellooooo", "DocumentSnapshot successfully written!");
                                                    }
                                                })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {
                                                                Log.w("EROOOOO", "Error writing document", e);
                                                            }
                                                        });

                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(UploadPDF.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }

            }
            });
    }


    public void VazioPDF (View v) {
        if(TextUtils.isEmpty(NomeTeste)) {
            Toast.makeText(UploadPDF.this, "Escolha um Ficheiro!", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Função que ao fazer upload de um ficheiro PDF permite obter o seu nome
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    // Função que explica a razão de o utilozador ter de aceitar a permissão
    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("É necessária a sua permissão para aceder ao seu aplicativo")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(UploadPDF.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    // Permissão para aceder à galeria ou camarâ do dispositivo do utilizador
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

            if(requestCode == STORAGE_PERMISSION_CODE) {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    Intent intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select PDF file"), RESULT_PDF);
                }
                else{
                    Toast.makeText(this, "Permission GALERY denied!", Toast.LENGTH_SHORT).show();
                }
            }
        }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(UploadPDF.this, DescricaoDisciplina.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }
    }
