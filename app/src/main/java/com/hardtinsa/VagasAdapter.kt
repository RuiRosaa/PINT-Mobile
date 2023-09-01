package com.hardtinsa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hardtinsa.R

class VagasAdapter(private val VagasList: MutableList<Vaga>) :
    RecyclerView.Adapter<VagasAdapter.VagaViewHolder>() {

    // ViewHolder class
    inner class VagaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.idea_image)
        val description: TextView = itemView.findViewById(R.id.idea_description)
        val tags : TextView = itemView.findViewById(R.id.vagas_Tags)
        val detailsButton: ImageView = itemView.findViewById(R.id.idea_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VagaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_vagas_item, parent, false)
        return VagaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: VagaViewHolder, position: Int) {
        holder.image.setImageResource(R.drawable.baseline_remove_red_eye_24)
        val currentVaga = VagasList[position]
        holder.description.text = currentVaga.descricao
        holder.tags.text = currentVaga.tags
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, VagasDetalhes::class.java)
            intent.putExtra("idoferta", currentVaga.idoferta)
            intent.putExtra("descricao", currentVaga.descricao)
            intent.putExtra("detalhe", currentVaga.detalhe)
            intent.putExtra("estado", currentVaga.estado)
            intent.putExtra("estadod", currentVaga.estadod)
            intent.putExtra("datainicio", currentVaga.datainicio)
            intent.putExtra("datafim", currentVaga.datafim)
            intent.putExtra("nome", currentVaga.nome)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return VagasList.size
    }

    fun clearVagas() {
        VagasList.clear()
        notifyDataSetChanged()
    }

    fun addVagas(newVaga: List<Vaga>) {
        VagasList.addAll(newVaga)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(vaga: Vaga) {
            itemView.findViewById<TextView>(R.id.idea_description).text = vaga.descricao
        }
    }
}

data class Vaga(
    val idoferta: Int,
    val descricao: String,
    val detalhe: String,
    val estado: String?,
    val estadod: String,
    val datainicio: String?,
    val datafim: String?,
    val nome: String,
    val morada: String?,
    val interna: Boolean,
    val tags: String
)
