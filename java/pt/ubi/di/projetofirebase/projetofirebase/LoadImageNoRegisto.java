package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class LoadImageNoRegisto extends AppCompatActivity {

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference collectionReference ;

    // Declaração da Storage que vai armazenar imagens e pdfs na Cloud Firestore
    private StorageReference teste;
    private FirebaseStorage storage;

    // Declaração da referência aos documentos das coleções da base de dados
    private DocumentReference documentReference;

    private ArrayList<String> Link;

    private int WRITE_PERMISSION_CODE = 300;

    String Curso="",Disciplina="",Teste = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_images_firebase);

        storage = FirebaseStorage.getInstance();

        // Declaração da viewPager que permite mostrar as imangens na forma de um livro

        final ViewPager viewPager = (ViewPager) findViewById(R.id.view_page);

        // Intento que recebe informação de outra atividade relativamente
        // ao curso e disciplina escolhido pelo utilizador
        Intent intent = getIntent();

        if (intent != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                Curso = bundle.getString("curso");
                Disciplina = bundle.getString("disciplina");
                Teste = bundle.getString("teste");

            }
        }

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome do teste
        actionBar.setTitle(Teste);

        // Declaração do caminho da base de dados
        collectionReference = db.collection("Cursos").document(Curso)
                .collection("Disciplinas").document(Disciplina).collection("Testes");

        // Função que vai pesquisar um teste na base de dados e retornar a infomação ao utilizador
        collectionReference.document(Teste).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // ArrayList que contém uma ou mais imagens
                        ArrayList<String> arrayList = (ArrayList<String>) document.get("urlImagem");

                        // Ciclo que vai popular a ViewPager com as imagens do teste
                        for (String s : arrayList) {
                            ViewPagerAdapter adapter  = new ViewPagerAdapter(LoadImageNoRegisto.this, arrayList);
                            viewPager.setAdapter(adapter);

                        }
                    }
                }
            }
        });

    }

    // Função que permite adicionar um icon de download ao top da barra da aplicação
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu,menu);
        return true;
    }

    // Função que ao clicar nesse icon faz o download da imagem ou de um conjunto de imagens
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            // antes de iniciar o download pede permissão ao utilizador
            case R.id.Download:
                if(ContextCompat.checkSelfPermission(LoadImageNoRegisto.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    startDownload();
                }
                else {
                    requestWritePermission();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    // Função que faz o download
    private void startDownload(){

        documentReference = collectionReference.document(Teste);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                // ArrayList que vai conter as imagens a ser descarregadas da base de dados
                Link = (ArrayList<String>) documentSnapshot.get("urlImagem");

                for (String s : Link) {
                    Log.d("Array", s);

                    // Referência à Storage que procura a imagem que corresponde ao URL na base de dados
                    teste = storage.getReferenceFromUrl(s);
                    Log.d("STORAGE", String.valueOf(teste));

                    // A imagem ou conjunto de imagens são descarregadas para o dispostivo móvel
                    teste.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String url = uri.toString();
                            DonwloadFiles(LoadImageNoRegisto.this,Teste,".jpeg", DIRECTORY_DOWNLOADS,url);
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
    }

    // Função que ativa o serviço de download e que constroi a notificação que é mostrada ao utilizador
    public void DonwloadFiles(Context context, String fileName, String fileExtension , String destinationDiretory, String Link){

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri uri = Uri.parse(Link);

        // Obtem as imagens para download
        DownloadManager.Request request = new DownloadManager.Request(uri);
        // verifica se existe uma conexão à internet para iniciar o download
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        request.allowScanningByMediaScanner();
        //Assim que o download termina, mostra para o utilizado um icona na gaveta de notificações
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //Guarda o documento na diretoria especificada e com o formato especificado
        request.setDestinationInExternalFilesDir(context, destinationDiretory,fileName + fileExtension);

        downloadManager.enqueue(request);

    }

    // Informa ao utilizador a razão pela qual é preciso aceitar a permisão
    private void requestWritePermission(){

        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permissão Necessária")
                    .setMessage("É necessário a sua permissão para efetuar o download")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            ActivityCompat.requestPermissions(LoadImageNoRegisto.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        }
        else{

            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
        }
    }

    @Override
    // Pede permissão ao utilizador para fazer download do documento, caso seja a primeira vez
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == WRITE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startDownload();
            }
            else{
                Toast.makeText(this, "Permission WRITE denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(LoadImageNoRegisto.this, ListViewTestesNoRegisto.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }
}
