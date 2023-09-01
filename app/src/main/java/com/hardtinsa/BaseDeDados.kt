package com.hardtinsa

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


private val DATABASE_NAME = "db"
private val DATABASE_VERSION = 112

class BaseDeDados(context: Context) : SQLiteOpenHelper(context,
    DATABASE_NAME, null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        apagaTabelas(db)
        criaTabelas(db)
        insereDadosIniciais(db)
        val currentVersion = DATABASE_VERSION
        if (currentVersion < DATABASE_VERSION) {
            onUpgrade(db, currentVersion, DATABASE_VERSION)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        apagaTabelas(db)
        onCreate(db)
    }


    private fun criaTabelas(bd: SQLiteDatabase) {

        //-----------------------Ideia-------------------------------------------
        val tabsc = ("CREATE TABLE Ideia (" +
                "idideia INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idtipo INTEGER," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "detalhe TEXT," +
                "estado INTEGER," +
                "estadoreg INTEGER DEFAULT 0," +   //novo reg adicionado com valor default 0
                "criadodata DATE," +
                "dataalt DATETIME)")

        try {
            bd.execSQL(tabsc)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-----------------------Beneficio-------------------------------------------
        val tabsb = ("CREATE TABLE Beneficio (" +
                "idbeneficio INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "detalhe TEXT," +
                "descricao TEXT," +
                "estado INTEGER," +
                "estado_reg INTERGER DEFAULT 0," +
                "criadodata DATETIME)")

        val dropTableQueryBeneficio = "DROP TABLE IF EXISTS Beneficio"
        try {
            bd.execSQL(dropTableQueryBeneficio)
            bd.execSQL(tabsb)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }
        //-----------------------------Vaga-------------------------------------
        val tabsv = ("CREATE TABLE Vaga (" +
                "idoferta INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idcentro INTEGER," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "detalhe TEXT," +
                "estado INTEGER," +
                "estadod TEXT," +
                "criadodata DATETIME," +
                "publicado BOOLEAN," +
                "dataalt INTEGER," +  //novo reg
                "datainicio DATETIME," +
                "datafim DATETIME," +
                "nome TEXT," +
                "morada TEXT," +
                "interna INTEGER," +
                "tags TEXT)")

        val dropTableQueryVaaga = "DROP TABLE IF EXISTS Vaga"
        try {
            bd.execSQL(dropTableQueryVaaga)
            bd.execSQL(tabsv)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------Oportunidade--------------------------------------
        val tabso = ("CREATE TABLE Oportunidade (" +
                "idoportunidade INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idtipo INTEGER," +
                "idcliente INTEGER," +
                "iduser INTEGER," +
                "idtpprojeto INTEGER," +
                "descricao TEXT," +
                "detalhe TEXT," +
                "valorprev INTEGER," +
                "estado INTEGER," +
                "estadoreg INTEGER DEFAULT 0,"+
                "criadodata DATETIME," +
                "publicado INTEGER," +
                "nomecliente TEXT," +
                "nomeuser TEXT)")

        val dropTableQueryOP = "DROP TABLE IF EXISTS Oportunidade"
        try {
            //bd.execSQL(dropTableQueryOP)
            bd.execSQL(tabso)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-----------------------------Oportunidade_Contato-------------------------------------
        val tabsoc = ("CREATE TABLE Oportunidade_Contato (" +
                "idoportunidade INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idcontacto INTEGER)")

        try {
            bd.execSQL(tabsoc)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }


        //-----------------------------Cliente-------------------------------------
        val tabscli = ("CREATE TABLE Cliente (" +
                "idcliente INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "nome TEXT," +
                "nif INTEGER," +
                "morada TEXT," +
                "codpostal TEXT," +
                "localidade TEXT," +
                "email TEXT," +
                "telefone TEXT," +
                "telemovel TEXT," +
                "estado INTEGER," +
                "estadoreg INTEGER DEFAULT 0," +
                "criadodata DATETIME)")

        val dropTableQueryCliente = "DROP TABLE IF EXISTS Cliente"
        try {
            bd.execSQL(dropTableQueryCliente)
            bd.execSQL(tabscli)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------Notificações--------------------------------------
        val tabsavis = ("CREATE TABLE Notificacoes (" +
                "idaviso INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "estado INTEGER," +
                "criadodata DATETIME," +
                "publicado Boolean," +
                "data_inicio DATE," +
                "data_fim DATE," +
                "generico Boolean)")

        try {
            bd.execSQL(tabsavis)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Aviso_User----------------------------------------
        val tabsavu = ("CREATE TABLE Aviso_User (" +
                "iduser INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idaviso INTEGER)")

        try {
            bd.execSQL(tabsavu)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-----------------------------Api_Token-------------------------------------
        val tabstok = ("CREATE TABLE Api_Token (" +
                "idtoken INTEGER PRIMARY KEY AUTOINCREMENT," +
                "token TEXT," +
                "dadosextra TEXT," +
                "validodata DATETIME," +
                "criadodata DATETIME," +
                "iduser INTEGER," +
                "estado INTEGER)")

        try {
            bd.execSQL(tabstok)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-------------------------------Centro-----------------------------------
        val tabscent = ("CREATE TABLE Centro (" +
                "idcentro INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "nome TEXT," +
                "morada TEXT," +
                "codpostal INTEGER," +
                "localidade TEXT," +
                "email TEXT, " +
                "telefone INTEGER," +
                "telemovel INTEGER," +
                "responsavel TEXT," +
                "estado INTEGER," +
                "criadodata DATETIME)")

        try {
            bd.execSQL(tabscent)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-------------------------------Contato-----------------------------------
        val tabscont = ("CREATE TABLE Contato (" +
                "idcontato INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idcliente INTEGER," +
                "iduser INTEGER," +
                "nome TEXT," +
                "funcao TEXT," +
                "nif INTEGER," +
                "morada TEXT, " +
                "codpostal TEXT," +
                "localidade TEXT," +
                "email TEXT," +
                "telefone TEXT," +
                "telemovel TEXT," +
                "estado INTEGER," +
                "estadoreg INTEGER DEFAULT 0," +
                "criadodata DATETIME)")

        val dropTableQueryContato = "DROP TABLE IF EXISTS Contato"
        try {
            bd.execSQL(dropTableQueryContato)
            bd.execSQL(tabscont)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------Entrevista--------------------------------------
        val tabsent = ("CREATE TABLE Entrevista (" +
                "identrevista INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idcandidatura INTEGER," +
                "iduser INTEGER," +
                "nomecandidato TEXT," +
                "nomeexterno TEXT," +
                "data_hora_prev DATETIME," +
                "data_hora DATETIME," +
                "data_hora_fim DATETIME," +
                "comentarios TEXT, " +
                "classificacao INTEGER," +
                "identrevistador INTEGER," +
                "estado INTEGER," +
                "estadoreg INTEGER DEFAULT 0," +
                "popVisto INTEGER DEFAULT 0," +
                "entrevistatecnica INTEGER," +
                "criadodata DATETIME)")

        val dropTableQueryEntrevista = "DROP TABLE IF EXISTS Entrevista"
        try {
            bd.execSQL(dropTableQueryEntrevista)
            bd.execSQL(tabsent)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

            //---------------------------Estado---------------------------------------
        val tabsesta = ("CREATE TABLE Estado (" +
                    "idestado INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "descricao TEXT," +
                    "nordem INTEGER," +
                    "inativo INTEGER," +
                    "usogeral INTEGER," +
                    "usooferta INTEGER," +
                    "usooportunidade INTEGER, " +
                    "usoideia INTEGER," +
                    "usobeneficio INTEGER," +
                    "usocandidatura INTEGER)")
        try {
            bd.execSQL(tabsesta)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

    //-----------------------------Funcionalidade-------------------------------------
        val tabsfunda = ("CREATE TABLE Funcionalidade (" +
                "idfuncionalidade INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "nomeunico TEXT," +
                "criadodata DATETIME," +
                "estado INTEGER)")
        try {
            bd.execSQL(tabsfunda)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

    //-------------------------------Idioma-----------------------------------
        val tabsido = ("CREATE TABLE Idioma (" +
                "ididioma INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "criadodata DATETIME," +
        "estado INTEGER)")
        try {
            bd.execSQL(tabsido)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }


    //------------------------------Log------------------------------------
        val tabslog = ("CREATE TABLE Log (" +
                "idlog INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "modulo TEXT," +
                "acao TEXT," +
                "valor INTEGER," +
                "criadodata DATETIME," +
                "estado INTEGER," +
                "tabela TEXT," +
                "tabelaid INTEGER)")
        try {
            bd.execSQL(tabslog)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }


    //------------------------------Anexo------------------------------------
        val tabsanx = ("CREATE TABLE Anexo (" +
                "idanexo INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "idcandidatura INTEGER," +
                "descricao TEXT," +
                "tipofile TEXT," +
                "ficheiro TEXT," +
                "estado INTEGER," +
                "criadodata DATETIME)")
        try {
            bd.execSQL(tabsanx)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------Perfil--------------------------------------
        val tabspef = ("CREATE TABLE Perfil (" +
                "idperfil INTEGER PRIMARY KEY AUTOINCREMENT," +
                "descricao TEXT," +
                "criadodata DATETIME," +
                "nivel INTEGER," +
                "estado INTEGER)")
        try {
            bd.execSQL(tabspef)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Perfil_Funcionalidade----------------------------------------
        val tabspff = ("CREATE TABLE Perfil_Func (" +
                "idperfil INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idfuncionalidade INTEGER)")
        try {
            bd.execSQL(tabspff)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------Tipo_Negocio--------------------------------------
        val tabstpn = ("CREATE TABLE TipoNegocio (" +
                "idtipo INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "estado INTEGER," +
                "criadodata DATETIME)")
        try {
            bd.execSQL(tabstpn)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //----------------------------TipoProjeto--------------------------------------
        val tabstpp = ("CREATE TABLE TipoProjeto (" +
                "idtpprojeto INTEGER PRIMARY KEY AUTOINCREMENT," +
                "iduser INTEGER," +
                "descricao TEXT," +
                "estado INTEGER," +
                "criadodata DATETIME)")
        try {
            bd.execSQL(tabstpp)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Tradução----------------------------------------
        val tabstd = ("CREATE TABLE Traducao (" +
                "idtraducao INTEGER PRIMARY KEY AUTOINCREMENT," +
                "ididioma INTEGER," +
                "iduser INTEGER," +
                "modulo TEXT," +
                "chave TEXT," +
                "texto TEXT," +
                "criadodata DATETIME," +
                "estado INTEGER)")
        try {
            bd.execSQL(tabstd)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //-------------------------Utilizador-----------------------------------------
        val tabsutl = ("CREATE TABLE Utilizador (" +
                "iduser INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idperfil INTEGER," +
                "nome TEXT," +
                "email TEXT," +
                "passwd TEXT," +
                "dtnascimento DATETIME," +
                "linkfoto TEXT," +
                "colaborador INTEGER," +
                "criadodata DATETIME," +
                "criadouser INTEGER," +
                "estado INTEGER," +
                "alterapassprox INTEGER," +
                "userguid TEXT," +
                "dtultlogin DATETIME)")
        try {
            bd.execSQL(tabsutl)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Candidatura----------------------------------------
        val tabscdt = ("CREATE TABLE Candidatura (" +
                "idcandidatura INTEGER PRIMARY KEY AUTOINCREMENT," +
                "idoferta INTEGER," +
                "iduser INTEGER," +
                "nome TEXT," +
                "criadodata DATETIME," +
                "externo_email TEXT," +
                "externo_nome TEXT,"+
                "anexo TEXT,"+
                "estado INTEGER," +
                "estadod TEXT)")
        val dropTableQueryCandidatura = "DROP TABLE IF EXISTS Candidatura"
        try {
            bd.execSQL(dropTableQueryCandidatura)
            bd.execSQL(tabscdt)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Atividade----------------------------------------
        val tabsati = ("CREATE TABLE Atividade (" +
                "idatividade INTEGER PRIMARY KEY AUTOINCREMENT UNIQUE," +
                "iduser INTEGER," +
                "idoportunidade INTEGER," +
                "tipoativ TEXT," +
                "criadodata DATETIME," +
                "data_hora DATETIME," +
                "data_hora_fim DATETIME," +
                "estado INTEGER," +
                "assunto TEXT," +
                "estadoreg INTEGER DEFAULT 0," +
                "popVisto INTEGER DEFAULT 0," +
                "detalhe TEXT)")

       val dropTableQueryAtividade = "DROP TABLE IF EXISTS Atividade"
        try {
            bd.execSQL(dropTableQueryAtividade)
            bd.execSQL(tabsati)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Atividade_User----------------------------------------
        val tabsatvu = ("CREATE TABLE Atividade_User (" +
                "idatividadeuser INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "iduser INTEGER, " +
                "idatividade INTEGER)")
        val dropTableQueryAU = "DROP TABLE IF EXISTS Atividade_User"
        try {
            //bd.execSQL(dropTableQueryAU)
            bd.execSQL(tabsatvu)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Tipo_Atividade---------------------------------------
        val tabstipoativ = ("CREATE TABLE tipoativ (" +
                "idtipoativ INTEGER PRIMARY KEY AUTOINCREMENT," +
                "nomeativ TEXT)")
        val dropTableQueryTA = "DROP TABLE IF EXISTS tipoativ"
        try {
            bd.execSQL(dropTableQueryTA)
            bd.execSQL(tabstipoativ)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------Estado_Desc---------------------------------------
        val tabestadodesc = ("CREATE TABLE EstadoDesc (" +
                "idestado INTEGER PRIMARY KEY AUTOINCREMENT," +
                "descricao TEXT)")
        try {
            bd.execSQL(tabestadodesc)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //--------------------------UserEmail---------------------------------------
        val tabUserEmail = ("CREATE TABLE UserEmail (" +
                "iduser INTEGER PRIMARY KEY," +
                "nome TEXT," +
                "email TEXT," +
                "colaborador TEXT)")
        try {
            bd.execSQL(tabUserEmail)
            Log.i("Tabelas", "As tabelas foram criadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas já existem")
        }

        //------------------------------------------------------------




        //-------------------------------------------------------------

    }




    private fun apagaTabelas(bd: SQLiteDatabase) {
        val tabsc = "DROP TABLE IF EXISTS Ideia"
        try {
            bd.execSQL(tabsc)
            Log.i("Tabelas", "As tabelas foram apagadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas não foram apagadas")
        }

        val tabsb = "DROP TABLE IF EXISTS Beneficio"
        try {
            bd.execSQL(tabsb)
            Log.i("Tabelas", "As tabelas foram apagadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas não foram apagadas")
        }

        val tabsv = "DROP TABLE IF EXISTS Vaga"
        try {
            bd.execSQL(tabsv)
            Log.i("Tabelas", "As tabelas foram apagadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas não foram apagadas")
        }

        val tabso = "DROP TABLE IF EXISTS Oportunidade"
        try {
            bd.execSQL(tabso)
            Log.i("Tabelas", "As tabelas foram apagadas")
        } catch (e: Exception) {
            Log.i("Tabelas", "As tabelas não foram apagadas")
        }
    }

    private fun apagaDadosIniciais(bd: SQLiteDatabase) {
        // Define the IDs of the initial data records you want to delete
        val idsToDelete = arrayOf(1, 2)

        // Loop through the array of IDs and delete the corresponding records
        for (id in idsToDelete) {
            val tabsc = "DELETE FROM Ideia WHERE idideia=$id"
            bd.execSQL(tabsc)
            val tabsb = "DELETE FROM Benefico WHERE idbeneficio=$id"
            bd.execSQL(tabsb)
            val tabsv = "DELETE FROM Vaga WHERE idvaga=$id"
            bd.execSQL(tabsv)
        }
    }
    private fun insereDadosIniciais(bd: SQLiteDatabase) {

        val currentDate = Date()
        val criadodata = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse("2023-05-14")
        val datainicio = Date()
        val datafimString = "2023-05-14"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val datafim = dateFormat.parse(datafimString)
        val data_hora_String = "2023-05-31" // Data desejada no formato "yyyy-MM-dd"
        val data_hora = dateFormat.parse(data_hora_String)
        val data_hora_fim_String = "2023-05-32" // Data desejada no formato "yyyy-MM-dd"
        val data_hora_fim = dateFormat.parse(data_hora_fim_String)
/*
        insereUtilizador(
            bd,
            10,
            "Joao",
            "joao.antonio@gmail.com",
            "123",
            data_hora,
            "link/foto",
            1,
            data_hora_fim,
            1,
            1,
            0,
            "guid-do-usuario",
            data_hora_fim
        )
        insereUtilizador(
            bd,
            11,
            "Antonio",
            "antonio.jose@gmail.com",
            "123",
            data_hora,
            "link/foto",
            1,
            data_hora_fim,
            1,
            1,
            0,
            "guid-do-usuario",
            data_hora_fim
        )

        insereTipoAtiv(bd, "REUNIAO")
        insereTipoAtiv(bd, "CHAMADA")
        insereOportunidade(bd, 1, 2, 1, 1, 1, "desc", "est", criadodata, 1)
        insereOportunidade(bd, 2, 2, 1, 1, 1, "desc2", "est", criadodata, 1)*/
    }

    fun insereUtilizador(
        bd: SQLiteDatabase,
        idperfil: Int,
        nome: String,
        email: String,
        passwd: String,
        dtnascimento: Date,
        linkfoto: String,
        colaborador: Int,
        criadodata: Date,
        criadouser: Int,
        estado: Int,
        alterapassprox: Int,
        userguid: String,
        dtultlogin: Date
    ) {
        val dtnascimentoFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dtnascimento)
        val criadodataFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(criadodata)
        val dtultloginFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dtultlogin)

        val tabsuti =
            "INSERT INTO Utilizador(idperfil, nome, email, passwd, dtnascimento, linkfoto, colaborador, criadodata, criadouser, estado, alterapassprox, userguid, dtultlogin) " +
                    "VALUES($idperfil, '$nome', '$email', '$passwd', '$dtnascimentoFormatted', '$linkfoto', $colaborador, '$criadodataFormatted', $criadouser, $estado, $alterapassprox, '$userguid', '$dtultloginFormatted')"
        bd.execSQL(tabsuti)
    }
    
    fun insereTipoAtiv(
        bd: SQLiteDatabase,
        nomeativ: String
    ) {
        val tabstipoativ =
            "INSERT INTO tipoativ(nomeativ) " +
                    "VALUES('$nomeativ')"
        bd.execSQL(tabstipoativ)
    }

    fun insereEstadoDesc(
        bd: SQLiteDatabase,
        descricao: String
    ) {
        val tabsed =
            "INSERT INTO EstadoDesc(descricao) " +
                    "VALUES('$descricao')"
        bd.execSQL(tabsed)
    }

    fun insereIdeia(
        bd: SQLiteDatabase,
        idtipo: Int,
        iduser: Int,
        descricao: String,
        detalhe: String,
        estado: String,
        estadoreg: Int,
        criadodata: Date,
        dataalt: Date

    ) {
        val criadodataFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
        }.format(criadodata)
        val dataaltFormatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(dataalt)

        val tabsc =
            "INSERT INTO Ideia(idtipo, iduser, descricao, detalhe, estado, estadoreg, dataalt, criadodata) " +
                    "VALUES($idtipo, $iduser, '$descricao', '$detalhe', '$estado', $estadoreg, '$criadodataFormatted', '$dataalt')"
        bd.execSQL(tabsc)
    }

    fun insereBeneficio(
        bd: SQLiteDatabase,
        idbeneficio: Int,
        iduser: Int,
        detalhe: String,
        descricao: String,
        estado: String,
        criadodata: Date
    ) {
        val tabsb =
            "INSERT INTO Beneficio(idbeneficio, iduser, detalhe, descricao, estado, criadodata) " +
                    "VALUES($idbeneficio, $iduser, '$detalhe', '$descricao', '$estado', '$criadodata')"
        bd.execSQL(tabsb)
    }

    fun insereVaga(
        bd: SQLiteDatabase,
        idoferta: Int,
        descricao: String,
        estado: String,
        datainicio: Date?,
        datafim: Date?,
        nome: String,
        morada: String,
    ) {
        val tabsv =
            "INSERT INTO Vaga(idoferta, descricao, estado, datainicio, datafim, nome, morada) " +
                    "VALUES($idoferta, '$descricao', '$estado', '$datainicio', '$datafim', '$nome', '$morada')"
        bd.execSQL(tabsv)
    }

    fun insereAtividade(
        bd: SQLiteDatabase,
        iduser: Int,
        idoportunidade: Int,
        tipoativ: String,
        criadodata: Date?,
        data_hora: Date?,
        data_hora_fim: Date?,
        estado: String,
        assunto: String,
        detalhe: String
    ) {
        val ultimoId = obterUltimoIdAtividade(bd)
        val novoId = ultimoId + 1

        val tabsa =
            "INSERT INTO Atividade(idatividade, iduser, idoportunidade, tipoativ, criadodata, data_hora, data_hora_fim, estado, assunto, detalhe) " +
                    "VALUES($novoId, $iduser, $idoportunidade, '$tipoativ', '$criadodata', '$data_hora', '$data_hora_fim', '$estado', '$assunto', '$detalhe')"
        bd.execSQL(tabsa)
    }

    fun obterUltimoIdAtividade(bd: SQLiteDatabase): Int {
        val query = "SELECT MAX(idatividade) FROM Atividade"
        val cursor = bd.rawQuery(query, null)
        var ultimoId = 0

        if (cursor.moveToFirst()) {
            ultimoId = cursor.getInt(0)
        }

        cursor.close()
        return ultimoId
    }

    fun insereOportunidade(
        bd: SQLiteDatabase,
        idoportunidade: Int,
        idtipo: Int,
        idcliente: Int,
        iduser: Int,
        idtpprojeto: Int,
        descricao: String,
        estado: String,
        criadodata: Date,
        publicado: Int,
    ) {
        val tabsv =
            "INSERT INTO Oportunidade(idoportunidade, idtipo, idcliente, iduser, idtpprojeto, descricao, estado, criadodata, publicado) " +
                    "VALUES($idoportunidade, $idtipo, $idcliente, $iduser, $idtpprojeto, '$descricao', '$estado', '$criadodata', $publicado)"
        bd.execSQL(tabsv)
    }

    fun atualizaVagas(
        bd: SQLiteDatabase,
        idoferta: Int,
        idcentro: Int,
        iduser: Int,
        descricao: String,
        estado: String,
        criadodata: Date,
        datainicio: Date,
        datafim: Date
    ) {
        val tabsv = "UPDATE Vaga SET " +
                "idoferta=$idoferta, idcentro=$idcentro, iduser=$iduser, descricao='$descricao', estado='$estado', criadodata='$criadodata', datainicio='$datainicio', datafim='$datafim' " +
                "WHERE idoferta=$idoferta"
        bd.execSQL(tabsv)
    }


    fun todasIdeias(bd: SQLiteDatabase): List<Idea> {
        val ideaList = mutableListOf<Idea>()
        val db = this.readableDatabase
        val idUser = Globals.userID
        val idtipo = Globals.idtipo
        val res = bd.rawQuery("SELECT idideia, iduser, idtipo, descricao, detalhe, estado, estadoreg, criadodata FROM Ideia", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idideia = res.getInt(0)
            val iduser = res.getInt(1)
            val idtipo = res.getInt(2)
            val descricao = res.getString(3)
            val detalhe = res.getString(4)
            val estado = res.getString(5)
            val estadoreg = res.getInt(6)
            val criadodata = res.getString(7)


            val idea = Idea(idideia, iduser, idtipo, descricao, detalhe, estado, estadoreg, criadodata)
            ideaList.add(idea)
            res.moveToNext()
        }

        res.close()
        return ideaList
    }
/*
    fun todasAtividades(bd: SQLiteDatabase): List<Atividade> {
        val atividadeList = mutableListOf<Atividade>()
        val db = this.readableDatabase
        val idUser = Globals.userID
        val idtipo = Globals.idtipo
        val res = bd.rawQuery("SELECT idatividade, iduser, idoportunidade, tipoativ, criadodata, data_hora, data_hora_fim, estado, assunto, estadoreg, detalhe FROM Atividade", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idatividade = res.getInt(0)
            val iduser = res.getInt(1)
            val idoportunidade = res.getInt(2)
            val tipoativ = res.getString(3)
            val criadodata = res.getString(4)
            val data_hora = res.getString(5)
            val data_hora_fim = res.getString(6)
            val estado = res.getString(7)
            val assunto = res.getString(8)
            val estadoreg = res.getInt(9)
            val detalhe = res.getString(10)

            val atividade = Atividade(idatividade, iduser, idoportunidade, tipoativ, criadodata, data_hora, data_hora_fim, estado, assunto, estadoreg, detalhe)
            atividadeList.add(atividade)
            res.moveToNext()
        }
        res.close()
        return atividadeList
    }
*/


    fun todosBeneficios(bd: SQLiteDatabase): List<Beneficio> {
        val beneficiosList = mutableListOf<Beneficio>()
        var cursor: Cursor? = null

        try {
            cursor = bd.rawQuery(
                "SELECT idbeneficio, iduser, detalhe, descricao, estado, criadodata FROM Beneficio",
                null
            )
            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getInt(0)
                    val iduser = cursor.getInt(1)
                    val detalhe = cursor.getString(2)
                    val descricao = cursor.getString(3)
                    val estado = cursor.getString(4)
                    val criadodata = cursor.getString(5)


                    val beneficio = Beneficio(id, iduser, detalhe, descricao, estado, criadodata)
                    beneficiosList.add(beneficio)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            // Handle any exceptions that occur during database operations
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return beneficiosList
    }

    fun todasVagas(bd: SQLiteDatabase): List<Vaga> {
        val VagasList = mutableListOf<Vaga>()
        val res = bd.rawQuery("SELECT idoferta, descricao, detalhe, estado, estadod, datainicio, datafim, nome, morada, interna, tags FROM Vaga", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idoferta = res.getInt(0)
            val descricao = res.getString(1)
            val detalhe = res.getString(2)
            val estado = res.getString(3)
            val estadod = res.getString(4)
            val datainicio = res.getString(5)
            val datafim = res.getString(6)
            val nome = res.getString(7)
            val morada = res.getString(8)
            val internaInt = res.getInt(9)
            val interna = internaInt != 0
            val tags = res.getString(10)

            val vaga = Vaga( idoferta, descricao, detalhe, estado, estadod, datainicio, datafim, nome, morada, interna, tags)
            VagasList.add(vaga)
            res.moveToNext()
        }

        res.close()
        return VagasList
    }

    fun todasNotificacoes(bd: SQLiteDatabase): List<Notificacoes> {
        val notificacoesList = mutableListOf<Notificacoes>()
        val res = bd.rawQuery("SELECT idaviso, iduser, descricao, estado, criadodata, publicado, data_inicio, data_fim, generico FROM Notificacoes", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idaviso = res.getInt(0)
            val iduser = res.getInt(1)
            val descricao = res.getString(2)
            val estado = res.getString(3)
            val criadodata = res.getString(4)
            val publicadoInt = res.getInt(5)
            val publicado = publicadoInt != 0       // Convert the integer to boolean
            val data_inicio = res.getString(6)
            val data_fim = res.getString(7)
            val genericoInt = res.getInt(8)
            val generico = genericoInt != 0 // Convert the integer to boolean



            val notificacoes = Notificacoes(idaviso, iduser, descricao, estado, criadodata, publicado, data_inicio, data_fim, generico)
            notificacoesList.add(notificacoes)
            res.moveToNext()
        }

        res.close()
        return notificacoesList
    }

    fun todosClientes(bd: SQLiteDatabase): List<Cliente> {
        val clienteList = mutableListOf<Cliente>()
        val res = bd.rawQuery("SELECT idcliente, iduser, nome, nif, morada, codpostal, localidade, email, telefone, telemovel, estado, criadodata FROM Cliente", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idcliente = res.getInt(0)
            val iduser = res.getInt(1)
            val nome = res.getString(2)
            val nif = res.getInt(3)
            val morada = res.getString(4)
            val codpostal = res.getString(5)
            val localidade = res.getString(6)
            val email = res.getString(7)
            val telefone = res.getString(8)
            val telemovel = res.getString(9)
            val estado = res.getInt(10)
            val criadodata = res.getString(11)



            val cliente = Cliente(idcliente, iduser, nome, nif, morada, codpostal, localidade, email, telefone, telemovel, estado, criadodata)
            clienteList.add(cliente)
            res.moveToNext()
        }

        res.close()
        return clienteList
    }

    fun todasOportunidades(bd: SQLiteDatabase): List<Oportunidade> {
        val oportunidadeList = mutableListOf<Oportunidade>()
        val res = bd.rawQuery("SELECT idoportunidade, idtipo, idcliente, iduser, idtpprojeto, descricao, detalhe, valorprev, estado, estadoreg, criadodata, publicado FROM Oportunidade", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idoportunidade = res.getInt(0)
            val idtipo = res.getInt(1)
            val idcliente = res.getInt(2)
            val iduser = res.getInt(3)
            val idtpprojeto = res.getInt(4)
            val descricao = res.getString(5)
            val detalhe = res.getString(6)
            val valorprev = res.getInt(7)
            val estado = res.getInt(8)
            val estadoreg = res.getInt(9)
            val criadodata = res.getString(10)
            val publicadoInt = res.getInt(11)
            val publicado = publicadoInt != 0 //é recebido como valor inteiro e dps é convertido para Boolean, verificando se não é igual a 0


            val oportunidade = Oportunidade(idoportunidade, idtipo, idcliente, iduser, idtpprojeto, descricao, detalhe, valorprev, estado, estadoreg, criadodata, publicado)
            oportunidadeList.add(oportunidade)
            res.moveToNext()
        }

        res.close()
        return oportunidadeList
    }
    fun todosContactos(bd: SQLiteDatabase): List<Contato> {
        val contatoList = mutableListOf<Contato>()
        val res = bd.rawQuery("SELECT idcontato, idcliente, iduser, nome, funcao, nif, morada, codpostal, localidade, email, telefone, telemovel, estado, estadoreg, criadodata FROM Contato", null)
        res.moveToFirst()

        while (!res.isAfterLast) {
            val idcontato = res.getInt(0)
            val idcliente = res.getInt(1)
            val iduser = res.getInt(2)
            val nome = res.getString(3)
            val funcao = res.getString(4)
            val nif = res.getInt(5)
            val morada = res.getString(6)
            val codpostal = res.getString(7)
            val localidade = res.getString(8)
            val email = res.getString(9)
            val telefone = res.getString(10)
            val telemovel = res.getString(11)
            val estado = res.getInt(12)
            val estadoreg = res.getInt(13)
            val criadodata = res.getString(14)

            val contato = Contato(idcontato, idcliente, iduser, nome, funcao, nif, morada, codpostal, localidade, email, telefone, telemovel, estado, estadoreg, criadodata)
            contatoList.add(contato)
            res.moveToNext()
        }

        res.close()
        return contatoList
    }




    fun rawQuery(query: String, selectionArgs: Array<String>?): Cursor? {
        val db = readableDatabase
        return db.rawQuery(query, selectionArgs)
    }



}