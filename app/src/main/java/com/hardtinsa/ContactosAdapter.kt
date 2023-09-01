package com.hardtinsa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hardtinsa.R

//import com.instamobile.kotlinlogin.R

class ContactosAdapter( private val contactosList: MutableList<Contato>) :
    RecyclerView.Adapter<ContactosAdapter.ContactoViewHolder>() {

    inner class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.opo_description)
        val detailsButton: ImageView = itemView.findViewById(R.id.opo_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_oportcontacto_item, parent, false)
        return ContactoViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contato = contactosList[position]
        holder.description.text = contato.email

        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ContactosDetalhe::class.java).apply {
                putExtra("nome", contato.nome)
                putExtra("morada", contato.morada)
                putExtra("funcao", contato.funcao)
                putExtra("telefone", contato.telefone)
                putExtra("idcliente", contato.idcliente)
                putExtra("email", contato.email)
                putExtra("telemovel", contato.telemovel)
            }
            holder.itemView.context.startActivity(intent)
        }


    }

    override fun getItemCount(): Int {
        return contactosList.size
    }

    fun clearContato() {
        contactosList.clear()
        notifyDataSetChanged()
    }

    fun addContato(newContatos: List<Contato>) {
        contactosList.addAll(newContatos)
        notifyDataSetChanged()
    }

    companion object
}
data class Contato(
    val idcontato: Int,
    val idcliente: Int,
    val iduser: Int,
    val nome: String,
    val funcao: String,
    val nif: Int,
    val morada: String,
    val codpostal: String,
    val localidade: String,
    val email: String,
    val telefone: String,
    val telemovel: String,
    val estado: Int,
    val estadoreg: Int,
    val criadodata: String?
)

