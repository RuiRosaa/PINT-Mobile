package app.navdrawer.basededados

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.util.ArrayList



private val DATABASE_NAME = "pessoas.db"
private val DATABASE_VERSION = 1

class BaseDeDados(context:Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


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
        inserePessoa(bd, "Paulo", "ptome@estv.ipv.pt")
        inserePessoa(bd, "Antonio Joaquim", "aj@estv.ipv.pt")
    }

    fun inserePessoa(bd:SQLiteDatabase, nome:String, email:String) {
        val tabsc = "INSERT INTO pessoas(nome, email) VALUES('$nome', '$email')"
        bd.execSQL(tabsc)
    }



    fun atualizaPessoa(bd:SQLiteDatabase, id:Int, nome:String, email:String) {
        val tabsc = "UPDATE pessoas SET nome='$nome', email='$email' WHERE id=$id"
        bd.execSQL(tabsc)
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
