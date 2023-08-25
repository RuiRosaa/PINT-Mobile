package app.navdrawer.basededados


import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

private val DATABASE_VERSION = 1
private val DATABASE_NAME = "pessoas.db"


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val TABLE_NAME = "pessoas"
    private val COL_ID = "id"
    private val COL_NOME = "nome"
    private val COL_EMAIL = "email"

    override fun onCreate(db: SQLiteDatabase) {
        criaTabelas(db)
        insereDadosIniciais(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        apagaTabelas(db)
        onCreate(db)
    }

    private fun criaTabelas(bd:SQLiteDatabase) {
        val tabsc = ("CREATE TABLE pessoas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nome TEXT," +
                "email TEXT)")

        try {
            bd.execSQL(tabsc)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e:Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }
    }

    private fun apagaTabelas(bd:SQLiteDatabase) {
        val tabsc = "DROP TABLE IF EXISTS pessoas"

        try {
            bd.execSQL(tabsc)
            Log.i("Tabelas", "As tabelas foram apagadas")
        } catch (e:Exception) {
            Log.i("Tabelas", "As tabelas não foram apagadas")
        }
    }

    private fun insereDadosIniciais(bd:SQLiteDatabase) {
        inserePessoa(bd, "Paulo Tomé", "ptome@estv.ipv.pt")
        inserePessoa(bd, "Antonio Joaquim", "aj@estv.ipv.pt")
    }

    fun inserePessoa(bd:SQLiteDatabase, nome:String, email:String) {
        val tabsc = "INSERT INTO pessoas(nome, email) VALUES('$nome', '$email')"
        bd.execSQL(tabsc)
    }

    fun atualizaPessoa(id: String, nome: String, email: String): Int {
        val db = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(COL_NOME, nome)
            put(COL_EMAIL, email)
        }
        val result = db.update(TABLE_NAME, contentValues, "$COL_ID=?", arrayOf(id))
        db.close()
        return result
    }
    class getPessoa(val id: Int, val nome: String, val email: String) {
        // class methods and properties
    }


    fun apagaPessoa(bd:SQLiteDatabase, id:Int) {
        val tabsc = "DELETE FROM pessoas WHERE id=$id"
        bd.execSQL(tabsc)
    }

    fun todasPessoas(bd:SQLiteDatabase): ArrayList<String> {
        val array_list = ArrayList<String>()
        val res = bd.rawQuery("SELECT id, nome FROM pessoas", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            array_list.add(res.getInt(0).toString() + " - " + res.getString(1))
            res.moveToNext()
        }

        res.close()
        return array_list
    }
}
