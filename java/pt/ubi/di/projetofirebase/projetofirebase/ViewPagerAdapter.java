package pt.ubi.di.projetofirebase.projetofirebase;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.github.chrisbanes.photoview.PhotoView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
// Classe que serve de adapter e permite carregar as imagens a ser mostradas ao utilizador
public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private ArrayList<String> imageUrls;
    private LayoutInflater layoutInflater;
    View view;

    ViewPagerAdapter(Context context,ArrayList<String> imageUrls){

        this.context= context;
        this.imageUrls = imageUrls;

    }

    // Conta o numero de imagens
    @Override
    public int getCount() {
        return imageUrls.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view == object;
    }

    // Função que usa o Picasso para conter as imagens que depois são carregadas numa PhotoView
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {


        int orientation = context.getResources().getConfiguration().orientation;

        // Verefica se a orientação está na vertical
                if(orientation == Configuration.ORIENTATION_LANDSCAPE){

                    layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                     view = layoutInflater.inflate(R.layout.row,container,false);

                    // Declaração da photoView que vai conter as imagens
           PhotoView  photoView = (PhotoView) view.findViewById(R.id.photo_view);

           // Declaração do picasso que vai permitir carregar as imagens para o display do utilizador
                   Picasso.get()
                      .load(imageUrls.get(position))
                       .fit()
                       .centerInside()
                       .into(photoView);

                    container.addView(view);

                }

    // Verefica se a orientação está na horizontal
                if(orientation == Configuration.ORIENTATION_PORTRAIT){

                   layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                   view = layoutInflater.inflate(R.layout.row,container,false);

                    // Declaração da photoView que vai conter as imagens
                PhotoView photoView = (PhotoView) view.findViewById(R.id.photo_view);

                    // Declaração do picasso que vai permitir carregar as imagens para o display do utilizador
               Picasso.get()
                    .load(imageUrls.get(position))
                     .fit()
                     .centerInside()
                     .into(photoView);

                   container.addView(view);
               }

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((LinearLayout) object);
    }

}
