package com.hardtinsa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.hardtinsa.R
/*import com.instamobile.kotlinlogin.R*/

class BeneficiosAdapter(private val beneficiosList: MutableList<Beneficio>) :
    RecyclerView.Adapter<BeneficiosAdapter.BeneficioViewHolder>() {

    // ViewHolder class
    inner class BeneficioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.bene_description)
        val detailsButton: ImageView = itemView.findViewById(R.id.idea_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BeneficioViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_beneficios_item, parent, false)
        return BeneficioViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: BeneficioViewHolder, position: Int) {
        val currentBeneficio = beneficiosList[position]
        holder.description.text = currentBeneficio.descricao
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, BeneficiosDetalhes::class.java)
            intent.putExtra("detalhe", currentBeneficio.detalhe)
            intent.putExtra("descricao", currentBeneficio.descricao)
            intent.putExtra("estado", currentBeneficio.estado)
            intent.putExtra("criadodata", currentBeneficio.criadodata)
            holder.itemView.context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return beneficiosList.size
    }

    fun clearBeneficios() {
        beneficiosList.clear()
        notifyDataSetChanged()
    }

    fun addBeneficios(newBeneficios: List<Beneficio>) {
        beneficiosList.addAll(newBeneficios)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(beneficio: Beneficio) {
            itemView.findViewById<TextView>(R.id.idea_description).text = beneficio.descricao
        }
    }

    companion object
}

data class Beneficio(
    val idbeneficio: Int,
    val iduser: Int,
    val detalhe: String?,
    val descricao: String,
    var estado: String?,
    val criadodata: String?
)