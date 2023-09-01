package com.hardtinsa

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log



private val DATABASE_VERSION = 112
private val DATABASE_NAME = "db"

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    private val TABLE_NAME = "Ideia"
    private val TABLE_NAME1 = "Oportunidade"
    private val COL_IDIDEIA = "idideia"
    private val COL_IDOPORTUNIDADE = "idoportunidade"
    private val COL_IDTIPO = "idtipo"
    private val COL_IDUSER = "iduser"
    private val COL_DESCRICAO = "descricao"
    private val COL_DESCRICAO1 = "descricao"
    private val COL_ESTADOREG = "estadoreg"
    private val COL_ESTADOREG1 = "estadoreg"
    private val COL_IDTPPROJETO = "idtpprojeto"
    private val COL_VALORPREV = "valorprev"
    private val COL_DETALHE = "detalhe"
    private val COL_DETALHE1 = "detalhe"
    private val COL_ESTADO = "estado"
    private val COL_IDCLIENTE = "idcliente"
    private val COL_CRIADODATA = "criadodata"
    private val COL_TIPO_PROJETO = "tipoprojeto"
    private val COL_TIPO_NEGOCIO = "tiponegocio"
    override fun onCreate(db: SQLiteDatabase?) {
        TODO("n é preciso")
    }


    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

    }

    fun atualizaIdeia(idideia: Int, descricao: String, detalhe: String, estadoreg: Int, idtipo : Int): Int {
        Log.i("DatabaseHelper", "ideaId: $idideia, descricao: $descricao, detalhe: $detalhe, estadoreg: $estadoreg, idtipo : $idtipo")
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_DESCRICAO, descricao)
            put(COL_DETALHE, detalhe)
            put(COL_ESTADOREG, estadoreg)
            put(COL_IDTIPO, idtipo)
        }
        val selection = "$COL_IDIDEIA = ?"
        val selectionArgs = arrayOf(idideia.toString())
        val updatedRows = db.update(TABLE_NAME, values, selection, selectionArgs)
        db.close()
        Log.i("DatabaseHelper", "Rows updated: $updatedRows")
        return updatedRows
    }

    fun atualizaOportunidade(idOportunidade: Int, idcliente: Int?, descricao: String, detalhe: String, idtipo: Int, estadoreg: Int, estado: Int, valorprev: Int, idtpprojeto: Int): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COL_DESCRICAO1, descricao)
            put(COL_DETALHE1, detalhe)
            put(COL_IDTIPO, idtipo)
            put(COL_IDCLIENTE, idcliente)
            put(COL_ESTADOREG1, estadoreg)
            put(COL_ESTADO, estado)
            put(COL_VALORPREV, valorprev)
            put(COL_IDTPPROJETO,idtpprojeto)
        }
        val selection = "$COL_IDOPORTUNIDADE = ?"
        val selectionArgs = arrayOf(idOportunidade.toString())
        val updatedRows = db.update(TABLE_NAME1, values, selection, selectionArgs)
        db.close()
        return updatedRows
    }
    fun apagaIdeia(idideia: Int): Int {
        val db = writableDatabase
        val deletedRows = db.delete("Ideia", "idideia = ? AND estado = 0", arrayOf(idideia.toString()))
        if (deletedRows > 0) {
            Log.d("Idea deleted", "Deleted $deletedRows rows")
        } else {
            Log.d("Idea deletion failed", "Failed to delete the idea")
        }
        db.close()
        return deletedRows
    }

    fun apagaOportunidade(idOportunidade: Int): Int {
        val db = writableDatabase
        val deletedRows = db.delete("Oportunidade", "idoportunidade = ?  AND (estadoreg = 1 OR estadoreg = 2)", arrayOf(idOportunidade.toString()))
        if (deletedRows > 0) {
            Log.d("Oportunidade deleted", "Deleted $deletedRows rows")
        } else {
            Log.d("Oportu deletion failed", "Failed to delete the Oportu")
        }
        db.close()
        return deletedRows
    }



}
