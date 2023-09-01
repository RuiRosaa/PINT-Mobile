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

class ClientesAdapter(private val clientesList: MutableList<Cliente>) :
    RecyclerView.Adapter<ClientesAdapter.ClienteViewHolder>() {

    inner class ClienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.cliente_description)
        val detailsButton: ImageView = itemView.findViewById(R.id.idea_image) //detalhes
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClienteViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_clientes_item, parent, false)
        return ClienteViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return clientesList.size
    }

    override fun onBindViewHolder(holder: ClienteViewHolder, position: Int) {
        val currentCliente = clientesList[position]
        holder.description.text = currentCliente.email
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, ClientesDetalhes::class.java)
            intent.putExtra("nome", currentCliente.nome)
            intent.putExtra("email", currentCliente.email)
            intent.putExtra("nif", currentCliente.nif)
            intent.putExtra("morada", currentCliente.morada)
            intent.putExtra("codpostal", currentCliente.codpostal)
            intent.putExtra("localidade", currentCliente.localidade)
            intent.putExtra("telefone", currentCliente.telefone)
            intent.putExtra("telemovel", currentCliente.telemovel)
            intent.putExtra("estado", currentCliente.estado)
            intent.putExtra("criadodata", currentCliente.criadodata)
            holder.itemView.context.startActivity(intent)
        }
    }

    fun clearClientes() {
        clientesList.clear()
        notifyDataSetChanged()
    }

    fun addClientes(newClientes: List<Cliente>) {
        clientesList.addAll(newClientes)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(beneficio: Beneficio) {
            itemView.findViewById<TextView>(R.id.idea_description).text = beneficio.descricao
        }
    }

    companion object
}

data class Cliente(
    val idcliente: Int,
    val iduser: Int,
    val nome: String,
    val nif: Int,
    val morada: String,
    val codpostal: String,
    val localidade: String,
    val email: String,
    val telefone: String,
    val telemovel: String,
    val estado: Int,
    val criadodata: String?
)
