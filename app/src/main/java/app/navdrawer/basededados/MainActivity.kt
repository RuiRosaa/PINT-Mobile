package app.navdrawer.basededados

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import app.navdrawer.R

class MainActivity : AppCompatActivity() {
    private lateinit var listView: ListView
    private lateinit var db: BaseDeDados
    private lateinit var arrayAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        val myToolbar = findViewById<Toolbar>(R.id.my_toolbar)
        setSupportActionBar(myToolbar)

        db = BaseDeDados(this)
        listView = findViewById(R.id.listView)
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, db.todasPessoas(db.writableDatabase))
        listView.adapter = arrayAdapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val pessoaId = arrayAdapter.getItem(position)?.split(" - ")?.get(0)?.toIntOrNull()
            if (pessoaId != null) {
                db.apagaPessoa(db.writableDatabase, pessoaId)
                atualizaListaPessoas()
                Toast.makeText(this, "Pessoa removida com sucesso", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Erro ao remover pessoa", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun atualizaListaPessoas() {
        arrayAdapter.clear()
        arrayAdapter.addAll(db.todasPessoas(db.writableDatabase))
        arrayAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        db.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menuInflater.inflate(R.menu.main_menu, menu)
            return true
        }
        return false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create -> {
                val intent = Intent(this, CreateActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_update -> {
                val intent = Intent(this, UpdateActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.menu_delete -> {
                val intent = Intent(this, DeleteActivity::class.java)
                startActivity(intent)
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

}
