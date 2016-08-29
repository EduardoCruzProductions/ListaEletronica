package eduardocruz.listaeletronica2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import eduardocruz.listaeletronica2.adapters.ProdutoAdapterListView;
import eduardocruz.listaeletronica2.database.ProdutoDao;
import eduardocruz.listaeletronica2.entidades.Produto;

public class ListaProduto extends AppCompatActivity {

    ArrayList<Produto> list = new ArrayList();
    ListView produtoListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_produto);
        produtoListView = (ListView) findViewById(R.id.listView_lista_produto);

        ProdutoDao pd = new ProdutoDao(getApplicationContext());


        try {

            list = pd.listar();
            ProdutoAdapterListView adapter = new ProdutoAdapterListView(getApplicationContext(),list);
            produtoListView.setAdapter(adapter);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void cadastrarProduto(View v){

        Intent i = new Intent(ListaProduto.this, CadastroProduto.class);
        startActivity(i);

    }

}