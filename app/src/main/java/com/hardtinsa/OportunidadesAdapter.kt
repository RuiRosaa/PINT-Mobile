package com.hardtinsa

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hardtinsa.R


class OportunidadesAdapter(private val oportunidadesList: MutableList<Oportunidade>) :
    RecyclerView.Adapter<OportunidadesAdapter.OportunidadeViewHolder>() {

    // ViewHolder class
    inner class OportunidadeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.opo_description)
        val editButton: ImageView = itemView.findViewById(R.id.opo_image)
        val detailsButton: ImageView = itemView.findViewById(R.id.opo_view)
        val deleteButton: Button = itemView.findViewById(R.id.opo_delete)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OportunidadeViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_oportunidades_item, parent, false)
        return OportunidadeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OportunidadeViewHolder, position: Int) {
        val currentOportunidade = oportunidadesList[position]
        holder.description.text = currentOportunidade.descricao

        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, Oportunidades_detalhe::class.java).apply {
                putExtra("oportunidade_id", currentOportunidade.idoportunidade)
                putExtra("descricao", currentOportunidade.descricao)
                putExtra("detalhe", currentOportunidade.detalhe)
                putExtra("estado", currentOportunidade.estado)
                putExtra("idtipo", currentOportunidade.idtipo)
                putExtra("idcliente", currentOportunidade.idcliente)
                putExtra("idtpprojeto", currentOportunidade.idtpprojeto)
                putExtra("valorprev", currentOportunidade.valorprev)

                Log.d("IntentExtraDetalhe", "oportunidade_id: ${currentOportunidade.idoportunidade}")

        }
            holder.itemView.context.startActivity(intent)
        }

        holder.editButton.setOnClickListener {
            val currentOportunidade = oportunidadesList[position]
            val adjustedIdtipo = currentOportunidade.idtipo?.minus(1)
            val adjustedIdtp = currentOportunidade.idtpprojeto?.minus(1)
            val adjustedCliente = currentOportunidade.idcliente?.minus(1)
            val intent = Intent(holder.itemView.context, EditarOportunidadesActivity::class.java)
            intent.putExtra("oportunidade_id", currentOportunidade.idoportunidade)
            intent.putExtra("descricao", currentOportunidade.descricao)
            intent.putExtra("idcliente", adjustedCliente)
            intent.putExtra("detalhe", currentOportunidade.detalhe)
            intent.putExtra("estado", currentOportunidade.estado)
            intent.putExtra("idtipo", adjustedIdtipo)
            intent.putExtra("idtpprojeto", adjustedIdtp)
            intent.putExtra("valorprev", currentOportunidade.valorprev)
            holder.itemView.context.startActivity(intent) }



        holder.deleteButton.setOnClickListener {
            val currentOport = oportunidadesList[position]
            if (shouldDeleteOport(currentOport)) {      //se o estado reg = 1 consigo dar delete
                deleteOportFromDatabase(holder.itemView.context, currentOport.idoportunidade)
                oportunidadesList.removeAt(position)
                currentOport.estado = 99
                notifyDataSetChanged()

            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Não é possível excluir esta oportunidade",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun getItemCount(): Int {
        return oportunidadesList.size
    }

    fun clearOportunidades() {
        oportunidadesList.clear()
        notifyDataSetChanged()
    }
    private fun deleteOportFromDatabase(context: Context, OportunidadeId: Int) {
        // Perform the deletion operation in the database
        val dbHelper = DatabaseHelper(context)
        dbHelper.apagaOportunidade(OportunidadeId)
    }
    private fun shouldDeleteOport(Oportunidade: Oportunidade): Boolean {
        return Oportunidade.estadoreg != 0
    }

    fun addOportunidades(newOportunidades: List<Oportunidade>) {
        oportunidadesList.addAll(newOportunidades)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(oportunidade: Oportunidade) {
            itemView.findViewById<TextView>(R.id.idea_description).text = oportunidade.descricao
        }
    }
}

data class Oportunidade(
    val idoportunidade: Int,
    val idtipo: Int,
    val idcliente: Int,
    val iduser: Int,
    val idtpprojeto: Int,
    val descricao: String,
    val detalhe: String?,
    val valorprev: Int,
    var estado: Int,
    val estadoreg: Int?,
    val criadodata: String?,
    val publicado: Boolean
)