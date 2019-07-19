package pt.ubi.di.projetofirebase.projetofirebase;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UploadImageTrabalho extends AppCompatActivity {

    // Declaração da FirebaseAuth que permite verificar se um utilizador esta registado
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    // Declaração de variáveis que indetificam os respectivos intentos
    private static final int RESULT_LOAD_IMAGE = 1;
    private static final int RESULT_CAMARA = 3;
    private int STORAGE_PERMISSION_CODE = 200;
    private static final int REQUEST_CODE = 123;

    String[]  permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private RecyclerView mUploadList;
    private ImageButton btnMult, btncamara;
    private EditText img_texto;

    private  String NomeTeste="",fileName = "",Curso="", Disciplina="", aluno="";

    private Button btn;

    private ArrayList<String> imagem = new ArrayList<String>();

    private List<String> fileNameList;
    private List<String> fileDoneList;

    private UploadListAdapter uploadListAdapter;

    // Declaração da base de dados Cloud Firestore
    private FirebaseFirestore mDatabaseRef = FirebaseFirestore.getInstance();

    // Declaração da referência as coleções da base de dados
    private CollectionReference notebookRef;

    // Declaração de uma Task para efetuar operações na Storage
    StorageTask mUlploadTask;

    // Declaração à Storage para guardar imagens e ficheiros
    private FirebaseStorage storage;
    private StorageReference storageReference;

    File file=null;
    // Declaração do bitmap que vai receber as imagens
    Bitmap bn;
    Uri imageUri;

    int finalI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_image_trabalho);

        // Referência à barra de no topo da aplicação
        final ActionBar actionBar = getSupportActionBar();

        // nome da aplicação
        actionBar.setTitle("DocShare");

        // Obtem o ID do utilizador atual
        FirebaseUser currentFirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        aluno = currentFirebaseUser.getUid();

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

        // Declaração do caminho da base de dados
        notebookRef = mDatabaseRef.collection("Cursos").document(Curso).collection("Disciplinas").document(Disciplina).collection("Trabalhos");

        btnMult = (ImageButton) findViewById(R.id.btnMult);

        mUploadList = (RecyclerView) findViewById(R.id.upload_list);

        btncamara = (ImageButton) findViewById(R.id.btnCamara);

        img_texto = (EditText) findViewById(R.id.Edit_Teste);

        btn = (Button) findViewById(R.id.btnLoad);;

        // arrays que vão guardar as imagens a ser adicionadas à base de dados
        fileNameList = new ArrayList<>();
        fileDoneList = new ArrayList<>();

        // adiciona-se o adapter que contém as imagens para upload à RecyclerView
        uploadListAdapter = new UploadListAdapter(fileNameList, fileDoneList);

        mUploadList.setLayoutManager(new LinearLayoutManager(this));
        mUploadList.setHasFixedSize(true);
        mUploadList.setAdapter(uploadListAdapter);

        // Intento que direciona para a aplicação da camara fotografica no dispositivo do utilizador
        btncamara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ContextCompat.checkSelfPermission(UploadImageTrabalho.this,
                        permissions[0]) == PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(UploadImageTrabalho.this,
                        permissions[1]) == PackageManager.PERMISSION_GRANTED) {

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());

                    imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                    startActivityForResult(intent, RESULT_CAMARA);

                }
                else {
                    ActivityCompat.requestPermissions(UploadImageTrabalho.this,permissions,REQUEST_CODE);
                }

            }
        });

        // Intento para a aplicação da galeria no dispositivo do utilizador
        btnMult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(ContextCompat.checkSelfPermission(UploadImageTrabalho.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
                }

                else {
                    requestStoragePermission();
                }
            }
        });

    }

    // Função que recebe a informação dos vários intentos
    @Override
    protected void onActivityResult( final int requestCode,  final int resultCode,  final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // limpa a RecycleView
        mUploadList.setAdapter(null);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fileNameList.clear();
                fileDoneList.clear();
                imagem.clear();

                // Função que recebe a foto diretamente da camara fotográfica e faz o upload para a storage e para a base de dados
                if (requestCode == RESULT_CAMARA && resultCode == RESULT_OK) {

                    if (auth.getCurrentUser() != null) {
                        // Se tiver sido tirado uma foto é retornado uma mensagem
                        if(imageUri == null){
                            Toast.makeText(UploadImageTrabalho.this, "Imagem não selecionada!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        NomeTeste = img_texto.getText().toString();
                        // Se tiver sido dado um nome ao ficheiro é retornado uma mensagem
                        if((TextUtils.isEmpty(NomeTeste))){
                            Toast.makeText(UploadImageTrabalho.this, "Insira um nome para o ficheiro!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        // Verfica se o nome contém o caracter "/"
                        if (NomeTeste.contains("/")) {
                            Toast.makeText(UploadImageTrabalho.this, " / é um caracter inválido!", Toast.LENGTH_SHORT).show();
                        }

                        // Recebe o uri da foto e guarda na forma de bitmap
                        try {
                            bn = MediaStore.Images.Media.getBitmap(UploadImageTrabalho.this.getContentResolver(), imageUri);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        // De seguida converte a imagem em bitmap para um array de bytes dimnuido o seu tamanho
                        byte[] bytes = getBytesFromBitmap(bn, 50);


                        if (imageUri != null) {
                            finalI = 0;
                            // Guardar o nome do ficheiro
                            fileName = getFileName(imageUri);
                            // Adiciona à RecycleView
                            fileNameList.add(fileName);
                            fileDoneList.add("uploading");

                            mUploadList.setAdapter(uploadListAdapter);

                            uploadListAdapter.notifyDataSetChanged();

                            // Faz uma referência a storage para guardar a imagem
                            StorageReference Diretoria = storageReference.child("Imagens");
                            StorageReference fileToUpload = Diretoria.child(NomeTeste).child(fileName);

                            mUlploadTask = fileToUpload.putBytes(bytes).
                                    addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                            fileDoneList.remove(finalI);
                                            fileDoneList.add(finalI, "done");

                                            uploadListAdapter.notifyDataSetChanged();

                                            // Depois do ficheiro estar armazenado na storage é obtido o seu url de download
                                            Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                                            Map<String, Object> UrlImagem = new HashMap<>();

                                            // O url é guardado na base de dados com o resto da informação
                                            // relativa a esse mesmo ficheiro
                                            while (!urlTask.isSuccessful()) ;
                                            Uri downloadUrl = urlTask.getResult();
                                            String url = downloadUrl.toString();

                                            imagem.add(url);

                                            UrlImagem.put("Curso", Curso);
                                            UrlImagem.put("Disciplina", Disciplina);
                                            UrlImagem.put("Trabalho", NomeTeste);
                                            UrlImagem.put("urlImagem", imagem);
                                            UrlImagem.put("Formato", ".img");
                                            UrlImagem.put("Utilizador", aluno);
                                            UrlImagem.put("Identificador" , "Trabalho");
                                            UrlImagem.put("Token" ,  FirebaseInstanceId.getInstance().getToken());


                                            notebookRef.document(NomeTeste).set(UrlImagem).addOnSuccessListener(new OnSuccessListener<Void>() {

                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                    img_texto.getText().clear();

                                                    imageUri = null;

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

                                            Toast.makeText(UploadImageTrabalho.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                        }
                    }
                }

                // Recebe a imagem ou imagens escolhida pelo utilizador através da galeria do dispositivo móvel
                else if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK) {

                    if (auth.getCurrentUser() != null) {

                        fileNameList.clear();
                        fileDoneList.clear();
                        imagem.clear();

                        if (data.getClipData() != null) {

                            // Guarda o nome escolhido pelo utilizador
                            NomeTeste = img_texto.getText().toString();

                            // Se o ficheiro não têm um nome é retornado uma mensagem
                            if(TextUtils.isEmpty(NomeTeste)) {
                                Toast.makeText(UploadImageTrabalho.this, "Insira um nome para o ficheiro!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Se o nome contém o caracter "/" é retornado uma mensagem
                            if (NomeTeste.contains("/")) {
                                Toast.makeText(UploadImageTrabalho.this, " / é um caracter inválido!", Toast.LENGTH_SHORT).show();
                            }

                            // Guarda quantas imagens foram selecionadas para upload
                            int totalItemsSelected = data.getClipData().getItemCount();


                            for (int i = 0; i < totalItemsSelected; i++) {

                                final int final2 = i;

                                // Obtêm o uri de cada imagem a ser armazenada na base de dados
                                imageUri = data.getClipData().getItemAt(i).getUri();

                                fileName = getFileName(imageUri);

                                // Converte a imagem para o formato bitmap
                                try {
                                    bn = MediaStore.Images.Media.getBitmap(UploadImageTrabalho.this.getContentResolver(), imageUri);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Converte a imagem para um array de bytes e comprime a imagem
                                byte[] bytes = getBytesFromBitmap(bn, 50);

                                fileNameList.add(fileName);
                                fileDoneList.add("uploading");

                                mUploadList.setAdapter(uploadListAdapter);

                                uploadListAdapter.notifyDataSetChanged();

                                // Faz uma referência a storage para guardar a imagem
                                StorageReference Diretoria = storageReference.child("Imagens");
                                StorageReference fileToUpload = Diretoria.child(NomeTeste).child(fileName);


                                mUlploadTask = fileToUpload.putBytes(bytes).
                                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                fileDoneList.remove(final2);
                                                fileDoneList.add(final2, "done");

                                                uploadListAdapter.notifyDataSetChanged();

                                                // Depois do ficheiro estar armazenado na storage é obtido o seu url de download
                                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();


                                                Map<String, Object> UrlImagem = new HashMap<>();

                                                // O url é guardado na base de dados com o resto da informação
                                                // relativa a esse mesmo ficheiro
                                                while (!urlTask.isSuccessful()) ;
                                                Uri downloadUrl = urlTask.getResult();
                                                String url = downloadUrl.toString();

                                                imagem.add(url);

                                                UrlImagem.put("Curso", Curso);
                                                UrlImagem.put("Disciplina", Disciplina);
                                                UrlImagem.put("Trabalho", NomeTeste);
                                                UrlImagem.put("urlImagem", imagem);
                                                UrlImagem.put("Formato", ".img");
                                                UrlImagem.put("Utilizador", aluno);
                                                UrlImagem.put("Identificador" , "Trabalho");
                                                UrlImagem.put("Token" ,  FirebaseInstanceId.getInstance().getToken());

                                                // referência ao caminho onde o url de download da imagem ou conjunto de imagens é guardado

                                                notebookRef.document(NomeTeste).set(UrlImagem).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        img_texto.getText().clear();
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

                                                Toast.makeText(UploadImageTrabalho.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        // Caso só tenha sido selecionada uma imagem para upload
                        else if (data.getData() != null){

                            fileNameList.clear();
                            fileDoneList.clear();
                            imagem.clear();

                            // Guarda-se o nome inserido pelo utilizador
                            NomeTeste = img_texto.getText().toString();

                            // Se não tiver nome é retornado uma mensagem
                            if(TextUtils.isEmpty(NomeTeste)) {
                                Toast.makeText(UploadImageTrabalho.this, "Insira um nome para o ficheiro!", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            // Se o nome contém o caracter "/" é retornado uma mensagem
                            if (NomeTeste.contains("/")) {
                                Toast.makeText(UploadImageTrabalho.this, " / é um caracter inválido!", Toast.LENGTH_SHORT).show();
                            }

                            // Guarda a imagem
                            imageUri = data.getData();

                            Bitmap bn = null;

                            if (imageUri != null) {

                                finalI = 0;

                                fileName = getFileName(imageUri);

                                // Converte o uri da imagem para bitmap
                                try {
                                    bn = MediaStore.Images.Media.getBitmap(UploadImageTrabalho.this.getContentResolver(), imageUri);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // Adiciona o bitmap a um array de bytes de forma a comprimir a imagem
                                byte[] bytes = getBytesFromBitmap(bn, 50);

                                fileNameList.add(fileName);
                                fileDoneList.add("uploading");

                                mUploadList.setAdapter(uploadListAdapter);

                                uploadListAdapter.notifyDataSetChanged();

                                // Faz uma referência a storage para guardar a imagem
                                StorageReference Diretoria = storageReference.child("Imagens");
                                StorageReference fileToUpload = Diretoria.child(NomeTeste).child(fileName);

                                mUlploadTask = fileToUpload.putBytes(bytes).
                                        addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                            @Override
                                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                                fileDoneList.remove(finalI);
                                                fileDoneList.add(finalI, "done");

                                                uploadListAdapter.notifyDataSetChanged();

                                        // Depois do ficheiro estar armazenado na storage é obtido o seu url de download
                                                Task<Uri> urlTask = taskSnapshot.getStorage().getDownloadUrl();

                                                Map<String, Object> UrlImagem = new HashMap<>();

                                                // O url é guardado na base de dados com o resto da informação
                                                // relativa a esse mesmo ficheiro
                                                while (!urlTask.isSuccessful()) ;
                                                Uri downloadUrl = urlTask.getResult();
                                                String url = downloadUrl.toString();

                                                imagem.add(url);

                                                UrlImagem.put("Curso", Curso);
                                                UrlImagem.put("Disciplina", Disciplina);
                                                UrlImagem.put("Trabalho", NomeTeste);
                                                UrlImagem.put("urlImagem", imagem);
                                                UrlImagem.put("Formato", ".img");
                                                UrlImagem.put("Utilizador", aluno);
                                                UrlImagem.put("Identificador" , "Trabalho");
                                                UrlImagem.put("Token" ,  FirebaseInstanceId.getInstance().getToken());
                                            // referência ao caminho onde o url de download da imagem ou conjunto de imagens é guardado
                                                notebookRef.document(NomeTeste).set(UrlImagem).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        img_texto.getText().clear();

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

                                                Toast.makeText(UploadImageTrabalho.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    }
                }

            }
        });
    }

    // caso o utilizador tente fazer upload sem selecionar um ficheiro é retornado uma mensagem
    public void VazioTrabalho (View v) {
        if(TextUtils.isEmpty(NomeTeste)) {
            Toast.makeText(UploadImageTrabalho.this, "Escolha um Ficheiro", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    // Função que ao fazer upload de um ficheiro PDF permite obter o seu nome de indetificação no dispositivo
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


    // Função que converte um bitmap para um array de bytes de forma a compressar
    // a imagem e diminuir o seu tempo de upload
    public static byte[] getBytesFromBitmap(Bitmap bn, int quality){

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bn.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
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
                            ActivityCompat.requestPermissions(UploadImageTrabalho.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
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

    // Permissão para aceder à galeria ou camarâ do dispositivo do utilizado
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == STORAGE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), RESULT_LOAD_IMAGE);
            }
            else{
                Toast.makeText(this, "Permission GALERY denied!", Toast.LENGTH_SHORT).show();
            }
        }

        if(requestCode == REQUEST_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.TITLE, "MyPicture");
                values.put(MediaStore.Images.Media.DESCRIPTION, "Photo taken on " + System.currentTimeMillis());

                imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                startActivityForResult(intent, RESULT_CAMARA);
            }
            else{
                Toast.makeText(this, "Permission CAMARA denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Função que permite voltar à atividade anterior
    @Override
    public void onBackPressed() {

        Intent iActivity5 = new Intent(UploadImageTrabalho.this, DescricaoDisciplina.class);

        Bundle bundle = new Bundle();

        bundle.putString("curso", Curso);
        bundle.putString("disciplina", Disciplina);

        iActivity5.putExtras(bundle);

        startActivity(iActivity5);

    }
}



