package pt.ubi.di.projetofirebase.projetofirebase;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;

// Classe que faz é invocada para adicionar os cursos e disciplinas escolhidas pelo utilizador
// a serem depois guardadas na base de dados na sua lista de preferências
public class note {

    private String Disciplina;
    private  String Curso;

    public note (){

    }

    public note (String Curso,String Disciplina){

        this.Curso=Curso;
        this.Disciplina= Disciplina;

    }

    public String getCurso() {
        return Curso;
    }

    public void setCurso(String curso) {
        Curso = curso;
    }

    public String getDisciplina() {
        return Disciplina;
    }

    public void setDisciplina(String disciplina) {
        Disciplina = disciplina;
    }
}
