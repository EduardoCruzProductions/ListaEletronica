package eduardocruz.listaeletronica2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import eduardocruz.listaeletronica2.adapters.ItensProdutoAdapterListView;
import eduardocruz.listaeletronica2.adapters.ProdutoAdapterListView;
import eduardocruz.listaeletronica2.database.ItensListaDao;
import eduardocruz.listaeletronica2.database.ListasDao;
import eduardocruz.listaeletronica2.database.ProdutoDao;
import eduardocruz.listaeletronica2.entidades.ItensLista;
import eduardocruz.listaeletronica2.entidades.Listas;
import eduardocruz.listaeletronica2.entidades.Produto;

public class Lista extends AppCompatActivity {


    static Listas l;
    Double listTotal = 0.d;
    TextView valorTotal;
    ArrayList<ItensLista> list = new ArrayList();
    EditText campoPesquisa;

    ViewFlipper flip;
    ListView pesqProduto;
    ListView itensProduto;
    ProdutoAdapterListView adapterFullList;
    ItensProdutoAdapterListView adapterItensList;
    ArrayList<Produto> fullList= new ArrayList();
    ArrayList<ItensLista> itensList= new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista);
        flip = (ViewFlipper) findViewById(R.id.viewFlipper);

        TextView nameList = (TextView) findViewById(R.id.txtName_list);
        valorTotal = (TextView) findViewById(R.id.txtTotal_list);
        upDateListTotal(0.d);
        Intent i = getIntent();
        String listName = i.getStringExtra("nome");
        nameList.setText(listName);
        createLista(listName);
        campoPesquisa = (EditText) findViewById(R.id.lista_editTextPesqProduto);

        campoPesquisa.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {

                upDateFullListByName(campoPesquisa.getText().toString());

                return false;
            }
        });

        //----------inicio configuracao de listview -------------

        pesqProduto = (ListView) findViewById(R.id.lista_listViewPesqProduto);
        itensProduto = (ListView) findViewById(R.id.lista_listViewItens);

        try {

            upDateFullList();
            upDateItensList();

            pesqProduto.setClickable(true);
            pesqProduto.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int item, final long l) {

                    final Produto p = adapterFullList.getItem(item);


                    LayoutInflater layoutInflater = LayoutInflater.from(Lista.this);
                    View promptView = layoutInflater.inflate(R.layout.activity_lista_fulllist_add, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Lista.this);
                    alertDialogBuilder.setView(promptView);

                    final TextView nome = (TextView) promptView.findViewById(R.id.txt_InfoLista_nome);
                    final TextView preco = (TextView) promptView.findViewById(R.id.txt_preco);
                    final TextView descricao = (TextView) promptView.findViewById(R.id.txt_descricao);
                    final EditText quantidade = (EditText) promptView.findViewById(R.id.edittext_quantidade);
                    quantidade.setText("1");
                    nome.setText(p.getNome());
                    preco.setText("Preço: "+p.getPreco()+" R$");
                    descricao.setText("Descrição: "+p.getDescricao());

                    AlertDialog.Builder fechar = alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("Adicionar à lista", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    //caso o botao Ok seja selecionado
                                    try{

                                        Double qt = Double.parseDouble(quantidade.getText().toString());

                                        if(qt != null){

                                            addItemLista(p,qt);

                                        }

                                    }catch(Exception e){

                                        Toast.makeText(getApplicationContext(), "Erro ao adicionar o item.", Toast.LENGTH_LONG).show();

                                    }

                                }
                            })
                            .setNegativeButton("Fechar",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            //caso o botao cancelar seja selecionado

                                            dialog.cancel();//cancelar operacao

                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();
                }

            });

            itensProduto.setClickable(true);
            itensProduto.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

                    final int item = i;

                    LayoutInflater layoutInflater = LayoutInflater.from(Lista.this);
                    View promptView = layoutInflater.inflate(R.layout.activity_lista_itens_remove, null);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Lista.this);
                    alertDialogBuilder.setView(promptView);

                    AlertDialog.Builder fechar = alertDialogBuilder.setCancelable(false)
                            .setPositiveButton("não", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                    dialog.cancel();

                                }
                            })
                            .setNegativeButton("sim",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {

                                            ItensLista il = itensList.get(item);
                                            Produto p = searchProdutoById(il.getId_produto());
                                            listTotal-= p.getPreco()*il.getQuantidade();
                                            upDateListTotal(0.d);
                                            itensList.remove(item);
                                            upDateItensList();

                                        }
                                    });

                    AlertDialog alert = alertDialogBuilder.create();
                    alert.show();

                    return true;
                }

            });


        }catch(Exception e){

        }


        //----------fim configuracao de listview -------------
    }

    //----------inicio metodos manipuladores de tela -------------

    public void nextView(View v){

        flip.showNext();

    }

    public void previousView(View v){

        flip.showPrevious();

    }

    private void upDateFullList(){
        try{

            ProdutoDao pd = new ProdutoDao(getApplicationContext());
            this.fullList = new ArrayList();
            this.fullList = pd.listar();
            this.adapterFullList = new ProdutoAdapterListView(getApplicationContext(),fullList);
            pesqProduto.setAdapter(adapterFullList);

        }catch(Exception e){

        }
    }

    private void upDateFullListByName(String nome){

        try{

            ProdutoDao pd = new ProdutoDao(getApplicationContext());
            this.fullList = new ArrayList();
            this.fullList = pd.pesquisar(nome);
            this.adapterFullList = new ProdutoAdapterListView(getApplicationContext(),fullList);
            pesqProduto.setAdapter(adapterFullList);

        }catch(Exception e){

        }

    }

    private void upDateItensList(){
        try{

            this.adapterItensList = new ItensProdutoAdapterListView(getApplicationContext(),fullList,itensList);
            itensProduto.setAdapter(adapterItensList);

        }catch(Exception e){

        }
    }

    //----------fim metodos manipuladores de tela -------------

    private void createLista(String nome){

        try{

            ListasDao ld = new ListasDao(this.getApplicationContext());

            l = new Listas();

            l.setNome(nome);
            l.setTotal(listTotal);

            l = ld.salvar(l);

        }catch(Exception e){
            System.err.println(e.getMessage());
        }

    }

    private void addItemLista(Produto p, Double qt){

        ItensLista il = new ItensLista();
        il.setId_lista(l.getId());
        il.setId_produto(p.getId());
        il.setQuantidade(qt);

        itensList.add(il);

        upDateListTotal(il.getQuantidade()*p.getPreco());
        upDateItensList();

    }

    private void upDateListTotal(Double qt){

        this.listTotal+=qt;
        valorTotal.setText("R$: "+listTotal);

    }

    private Produto searchProdutoById(int id){

        Produto produto = null;

        for(Produto p : fullList){

            if(p.getId()==id){
                produto = p;
            }

        }

        return produto;
    }

    public void salvar(View v){

        try{
            ItensListaDao ild = new ItensListaDao(this.getApplicationContext());
            ild.salvar(itensList);

            this.l.setTotal(listTotal);

            ListasDao ld = new ListasDao(this.getApplicationContext());
            ld.editar(this.l);

            Toast.makeText(getApplicationContext(), "Registros salvos com sucesso!", Toast.LENGTH_LONG).show();

            Intent i = new Intent(Lista.this, Home.class);
            startActivity(i);

        }catch(Exception e){
            Toast.makeText(getApplicationContext(), "Ocorreu um erro ao salvar os registros!", Toast.LENGTH_LONG).show();
            System.err.println(e.getMessage());
        }

    }

}
