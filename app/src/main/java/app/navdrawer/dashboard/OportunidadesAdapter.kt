package app.navdrawer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import app.navdrawer.R

class OportunidadesAdapter(private val oportunidadesList: List<String>) : RecyclerView.Adapter<OportunidadesAdapter.OportunidadeViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OportunidadeViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.items_oportunidade, parent, false)
        return OportunidadeViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: OportunidadeViewHolder, position: Int) {
        val oportunidade = oportunidadesList[position]
        holder.bind(oportunidade)
    }

    override fun getItemCount(): Int {
        return oportunidadesList.size
    }

    inner class OportunidadeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textOportunidade: TextView = itemView.findViewById(R.id.textOportunidade)

        fun bind(oportunidade: String) {
            textOportunidade.text = oportunidade
        }
    }
}
