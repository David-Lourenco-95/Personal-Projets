// Initialize Firebase Admin SDK
const functions = require('firebase-functions');

const admin = require('firebase-admin');
//admin.initializeApp(functions.config().firebase);
admin.initializeApp();

var db = admin.firestore();

exports.SendNotificationTeste = functions.firestore.document('Cursos/{cursoId}/Disciplinas/{disciplinasID}/Testes/{testeId}').onCreate((snap, context) => {
  const request = snap.data();
  let teste = "";
  let aluno = "";
  let token = [];
  let frequencia = request.Teste;
  let tokenUser = request.Token;
  let curso = request.Curso;
  let disciplina = request.Disciplina;
  let disciplinaNot = "";
  let formato = request.Formato;
  let identificador = request.Identificador;
  let novoArray = [];

  console.log("Curso = ", curso);
  console.log("Disciplina = ", disciplina);
  console.log("Frequencia = ", frequencia);
  console.log("Token  USER = ", tokenUser);
  console.log("Formato",formato);
  console.log("Identificador", identificador);

  var bdRef = db.collection('Cursos').doc(curso).collection('Disciplinas');
    bdRef.get()
      .then(snapshot => {
          snapshot.forEach(doc => {

              if(doc.id === disciplina){
                token = doc.data().Token;
                disciplinaNot = doc.data().Nome;

                console.log("Disciplina Notificao = ", disciplinaNot);

                console.log(" ARRAY ",token);
                 novoArray = token.filter(tokenID => tokenID !== tokenUser);
                console.log(" NOVO ARRAY ",novoArray);

                const payload = {
                          notification: {
                            title: "Firebase",
                            text: "Novo teste adicionado à disciplina" + " " + disciplinaNot,
                            curso: curso,
                            disciplina: disciplina,
                            documento: frequencia,
                            formato: formato

                          },
                          data: {
                            title: "Firebase",
                            text: "Novo teste adicionado à disciplina" + " " + disciplinaNot,
                            curso: curso,
                            disciplina: disciplina,
                            documento: frequencia,
                            formato: formato,
                            identificador: identificador
                          }
                      };

                teste = admin.messaging().sendToDevice(novoArray, payload);
              }

            });
            return null;
            })
          .catch(err => {
            console.log('Error getting documents', err);
      });

   console.log("fim da função")
  return teste;

});

exports.SendNotificationTrabalho = functions.firestore.document('Cursos/{cursoId}/Disciplinas/{disciplinasID}/Trabalhos/{trabalhoId}').onCreate((snap, context) => {
  const request = snap.data();
  let notificationtrabalho = "";
  let aluno = "";
  let token = [];
    let tokenUser = request.Token;
    let trabalho = request.Trabalho;
  let curso = request.Curso;
  let disciplina = request.Disciplina;
  let disciplinaNot = "";
  let formato = request.Formato;
  let identificador = request.Identificador;
  let novoArray = [];

  console.log("Curso = ", curso);
  console.log("Disciplina = ", disciplina);
  console.log("Trabalho = ", trabalho);
  console.log("Token  USER = ", tokenUser);
  console.log("Formato",formato);
  console.log("Identificador", identificador);

  var bdRef = db.collection('Cursos').doc(curso).collection('Disciplinas');
    bdRef.get()
      .then(snapshot => {
          snapshot.forEach(doc => {

              if(doc.id === disciplina){
                token = doc.data().Token;
                disciplinaNot = doc.data().Nome;

                console.log("Disciplina Notificao = ", disciplinaNot);

                console.log("ARRAY ",token);
                novoArray = token.filter(tokenID => tokenID !== tokenUser);
                console.log(" NOVO ARRAY ",novoArray);

                payload = {
                          notification: {
                            title: "Firebase",
                              text: "Novo trabalho adicionado à disciplina" + " " + disciplinaNot,
                              curso: curso,
                              disciplina: disciplina,
                              documento: trabalho,
                              formato: formato,
                              identificador: identificador
                            },
                          data: {
                            title: "Firebase",
                            text: "Novo trabalho adicionado à disciplina" + " " + disciplinaNot,
                            curso: curso,
                            disciplina: disciplina,
                            documento: trabalho,
                            formato: formato,
                            identificador: identificador
                          }
                      }

                notificationtrabalho = admin.messaging().sendToDevice(novoArray, payload);
              }

            });
            return null;
            })
          .catch(err => {
            console.log('Error getting documents', err);
      });

   console.log("fim da função")
  return notificationtrabalho;
});
