package pt.ubi.di.projetofirebase.projetofirebase;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

// Classe que ao serem escolhidos testes ou trabalhos para upload são guadados numa arrayList que é mostrado
// numa recycleView e que durante o processo de upload, um icon é mostrado quando o upload foi efetuado com sucesso
public class UploadListAdapter extends RecyclerView.Adapter<UploadListAdapter.ViewHolder>{

    // Arraylist que vai conter os ficheiros
    public List<String> fileNameList;
    public List<String> fileDoneList;

    public UploadListAdapter(List<String> fileNameList, List<String>fileDoneList){

        this.fileDoneList = fileDoneList;
        this.fileNameList = fileNameList;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Invocação do layout ao ser efetuado o upload
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_single, parent, false);
        return new ViewHolder(v);

    }

    // Função que muda os ficheiros num estado de espera, para concluido, quando o upload termina
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String fileName = fileNameList.get(position);
        holder.fileNameView.setText(fileName);

        String fileDone = fileDoneList.get(position);

        if(fileDone.equals("uploading")){

            holder.fileDoneView.setImageResource(R.mipmap.progress);

        } else {

            holder.fileDoneView.setImageResource(R.mipmap.checked);

        }

    }

    // Função que conta o total de ficheiros
    @Override
    public int getItemCount() {
        return fileNameList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // Declaração de variavéis
        View mView;

        public TextView fileNameView;
        public ImageView fileDoneView;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;

            fileNameView = (TextView) mView.findViewById(R.id.upload_filename);
            fileDoneView = (ImageView) mView.findViewById(R.id.upload_loading);


        }

    }

}
