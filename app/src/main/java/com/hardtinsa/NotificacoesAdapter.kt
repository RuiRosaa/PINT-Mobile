package com.hardtinsa

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instamobile.kotlinlogin.R

class NotificacoesAdapter(private val notificacoesList: MutableList<Notificacoes>) :
    RecyclerView.Adapter<NotificacoesAdapter.NotificacoesViewHolder>() {

    // ViewHolder class
    inner class NotificacoesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val description: TextView = itemView.findViewById(R.id.noti_description1)
        val detailsButton: ImageView = itemView.findViewById(R.id.noti_image1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificacoesViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_noti, parent, false)
        return NotificacoesViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NotificacoesViewHolder, position: Int) {
        val currentNotificacoes = notificacoesList[position]
        holder.description.text = currentNotificacoes.descricao
        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, VerNotificacoes::class.java)
            intent.putExtra("descricao", currentNotificacoes.descricao)
            intent.putExtra("estado", currentNotificacoes.estado)
            intent.putExtra("data_inicio", currentNotificacoes.data_inicio)
            intent.putExtra("data_fim", currentNotificacoes.data_fim)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return notificacoesList.size
    }

    fun clearNotificacoes() {
        notificacoesList.clear()
        notifyDataSetChanged()
    }

    fun addNotificacoes(newNotificacoes: List<Notificacoes>) {
        notificacoesList.addAll(newNotificacoes)
        notifyDataSetChanged()
    }
}
data class Notificacoes(
    val idaviso: Int,
    val iduser: Int,
    val descricao: String,
    val estado: String?,
    val criadodata: String,
    val publicado: Boolean,
    val data_inicio: String,
    val data_fim: String,
    val generico: Boolean
)




