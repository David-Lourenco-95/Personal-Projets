package pt.ubi.di.projetofirebase.projetofirebase;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

import static com.crashlytics.android.Crashlytics.TAG;

public class MeuFirebaseInstanceIDService extends FirebaseInstanceIdService {

    // ATENÇÃO este classe não foi implementada

    // Função que permite criar que serviço, que detete quando o token do
    // dispositivo mudou para assim adicionar o novo token à base de dados
    private FirebaseFirestore mDatabaseRef = FirebaseFirestore.getInstance();

    private FirebaseAuth auth;

    // Declaração da referência as coleções da base de dados
  CollectionReference collectionReference;

  DocumentReference documentReference, documentReference2;


    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

      /*  auth = FirebaseAuth.getInstance();

        if ( auth.getCurrentUser() != null) {

            // Obter o novo InstanceID
            String refreshedToken = FirebaseInstanceId.getInstance().getToken();
            // Declaração da base de dados Cloud Firestore

            Log.d("REFRESFED TOKEENNNNN: ", refreshedToken);

            collectionReference = mDatabaseRef.collection("Cursos").document("Bioquímica").collection("Disciplinas");

            documentReference2 = collectionReference.document("Cadeira1");
            Map<String, Object> Token = new HashMap<>();

            Token.put("Token", FieldValue.arrayUnion(refreshedToken));

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

        }*/
    }

    }



