package pt.ubi.di.projetofirebase.projetofirebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

// Função que permite criar um serviço que recebe as notificações do Firebase
public class MeuFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    Intent pendingIntent;
    String titulo="";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Map que guarda a informação recebida pela notificação
        Map<String, String> payload = null;

        try{
            if(remoteMessage.getData().size()>0) {
                payload = remoteMessage.getData();
            }

        }catch (NullPointerException e){
            Log.e(TAG, "onMessageReceived: NullPointerException: " + e.getMessage() );
        }

            // Guarda-se as várias informações que a notificação enviada pelo Firebase contém
            String title = payload.get("title");
            String message = payload.get("text");
            String curso = payload.get("curso");
            String disciplina = payload.get("disciplina");
            String documento = payload.get("documento");
            String formato = payload.get("formato");
            String identificador = payload.get("identificador");

            // Adiciona-se essa informação à função que inicia o serviço que de criar uma notificação
            // e mostrar para o utilizador
            sendMessageNotification(title, message, curso, disciplina, documento, formato, identificador);


    }

    private void sendMessageNotification(String title, String message, String curso, String disciplina, String documento, String formato, String identificador){

        // Criação da notificação
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Criação de um canal de notificações que desde o android Oreo, é necessário
        // para dar a possiblidade ao utilizador desativar as notifacações se quiser
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("CHANNEL_ID",
                    "NOTIFICATIONS",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(" FIREBASE NOTIFICATION");
            mNotificationManager.createNotificationChannel(channel);
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"CHANNEL_ID");


        // Criação do intento que vai inciado quando o utilizador clicar na notificação
        // Verifica-se se é um teste
        if(identificador.equals("Teste")){

            // Verifica se é uma imagem e mostra o documento ao utilizador
            if(formato.equals(".img")){

                pendingIntent = new Intent(this,LoadImagesFirebase.class);

                Bundle bundle = new Bundle();

                bundle.putString("curso", curso);
                bundle.putString("disciplina", disciplina);
                bundle.putString("teste",documento);

                pendingIntent.putExtras(bundle);
            }
            // Verifica se é uma pdf e mostra a informação ao utilizador
            else if (formato.equals(".pdf")){

                pendingIntent = new Intent(this,LoadPDF.class);

                Bundle bundle = new Bundle();

                bundle.putString("curso", curso);
                bundle.putString("disciplina", disciplina);
                bundle.putString("teste",documento);

                pendingIntent.putExtras(bundle);
            }
        }
        // Verefica se é um trabalho
        else  if(identificador.equals("Trabalho")){

            // Verifica se é uma imagem e mostra o documento ao utilizador
            if(formato.equals(".img")){

                pendingIntent = new Intent(this,LoadImagesTrabalho.class);

                Bundle bundle = new Bundle();

                bundle.putString("curso", curso);
                bundle.putString("disciplina", disciplina);
                bundle.putString("trabalho",documento);

                pendingIntent.putExtras(bundle);
            }
            // Verifica se é uma pdf e mostra a informação ao utilizador
            else if (formato.equals(".pdf")){

                pendingIntent = new Intent(this,LoadPDFTrabalho.class);

                Bundle bundle = new Bundle();

                bundle.putString("curso", curso);
                bundle.putString("disciplina", disciplina);
                bundle.putString("trabalho",documento);

                pendingIntent.putExtras(bundle);
            }
        }


        // Permite  que a notificação inicie uma nova atividade
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Criaçáo a notificaçáo do tipo pending, ou seja, que só é removida
        // se o utilizador interagir com a mesma
        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        //adição de propriedades à notificação
        builder.setSmallIcon(R.mipmap.ic_launcher) // icon da notificação
                .setLights(Color.BLUE, 2000, 2000) // cor do LED da notificação
                // vibraçáo, som e LED da notificação
                .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Som da notificação
                .setContentTitle(title) // Titulo da notificação
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // Texto da notificação
                .setOnlyAlertOnce(true);

        // Construção da notificaçáo
       builder.setContentIntent(notifyPendingIntent);

        mNotificationManager.notify(0,builder.build());

    }
}