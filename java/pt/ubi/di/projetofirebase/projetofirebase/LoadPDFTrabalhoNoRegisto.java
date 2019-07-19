package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class LoadPDFTrabalhoNoRegisto extends AppCompatActivity {

    private PDFView pdfView;

    // Declaração da Storage onde estão PDFs
    private StorageReference teste;
    private FirebaseStorage storage;

    private int WRITE_PERMISSION_CODE = 300;

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore mDatabaseRef = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference collectionReference;
    private DocumentReference documentReference ;

    private String Curso="", Disciplina="", Trabalho="",Link="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_pdftrabalho);

        storage = FirebaseStorage.getInstance();

        pdfView = (PDFView) findViewById(R.id.pdfview);

        // Intento que recebe informação de outra atividade relativamente
        // ao curso e disciplina escolhido pelo utilizador
        Intent intent = getIntent();

        if (intent != null) {

            Bundle bundle = intent.getExtras();

            if (bundle != null) {

                Curso = bundle.getString("curso");
                Disciplina = bundle.getString("disciplina");
                Trabalho = bundle.getString("trabalho");

            }
        }

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome do trabalho
        actionBar.setTitle(Trabalho);

        // Declaração do caminho da base de dados
        collectionReference = mDatabaseRef.collection("Cursos").document(Curso).collection("Disciplinas").document(Disciplina).collection("Trabalhos");

        documentReference = collectionReference.document(Trabalho);

        // Função que pesquisa por um determinado trabalho no formato PDF e o retorna para o utilizador
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                String url = documentSnapshot.getString("urlPDF");

                new LoadPDFTrabalhoNoRegisto.RetrivePdfStream().execute(url);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    // Função que executa em segundo plano ao ser passado um pdf, e o retorna num PDFView para uma melhor
    // experiência com a informação para o utilizador
    class RetrivePdfStream extends AsyncTask<String,Void,InputStream> {

        @Override
        protected InputStream doInBackground(String... strings) {

            InputStream inputStream=null;
            try {
                URL url = new URL (strings[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                if(urlConnection.getResponseCode()==200)
                {

                    inputStream = new BufferedInputStream(urlConnection.getInputStream());
                }
            }
            catch (IOException e){
                return  null;
            }

            return  inputStream;
        }

        @Override
        protected void onPostExecute(InputStream inputStream){

            pdfView.fromStream(inputStream).load();
        }
    }

    // Função que permite adicionar um icon de download ao top da barra da aplicação
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu,menu);
        return true;
    }

    // Função que ao clicar nesse icon faz o download do PDF
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.Download:
                // antes de iniciar o download pede permissão ao utilizador
                if(ContextCompat.checkSelfPermission(LoadPDFTrabalhoNoRegisto.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){

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

        documentReference = collectionReference.document(Trabalho);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                // O url do PDF é guardado numa string
                Link = documentSnapshot.getString("urlPDF");

                //A referência do url é procurada na Storage da Cloud Firestore
                teste = storage.getReferenceFromUrl(Link);

                // É efetuado o download do PDF para o dispositivo móvel do utilizador
                teste.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        String url = uri.toString();
                        DonwloadFiles(LoadPDFTrabalhoNoRegisto.this,Trabalho,".pdf",DIRECTORY_DOWNLOADS,url);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


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
        request.setDestinationInExternalFilesDir(context,destinationDiretory,fileName + fileExtension);

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

                            ActivityCompat.requestPermissions(LoadPDFTrabalhoNoRegisto.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},WRITE_PERMISSION_CODE);
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
    // Pede permissão ao utilizador para fazer download do documento, caso seja a primeira vez
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //  verifyPermissions();
        if(requestCode == WRITE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                startDownload();
            }
            else{
                Toast.makeText(this, "Permission WRITE denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    // Função que permite voltar à atividade anterior
    public void onBackPressed() {

        Intent iActivity5 = new Intent(LoadPDFTrabalhoNoRegisto.this, ListViewTrabalhosNoRegisto.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }
}
