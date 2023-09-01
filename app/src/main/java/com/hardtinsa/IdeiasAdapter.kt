package com.hardtinsa

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.hardtinsa.R


class IdeasAdapter(private val ideasList: MutableList<Idea>) :
    RecyclerView.Adapter<IdeasAdapter.IdeaViewHolder>() {

    private var isFetchButtonClicked: Boolean = false

    // ViewHolder class
    inner class IdeaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.findViewById(R.id.idea_image)
        val description: TextView = itemView.findViewById(R.id.idea_description)
        val editButton: ImageView = itemView.findViewById(R.id.idea_edit)
        val detailsButton: ImageView = itemView.findViewById(R.id.idea_image)
        val deleteButton: Button = itemView.findViewById(R.id.idea_delete)

    }
    fun setFetchButtonClicked(clicked: Boolean) {
        isFetchButtonClicked = clicked
    }
    private fun isFetchButtonClicked(): Boolean {
        return isFetchButtonClicked
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IdeaViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_ideia_item, parent, false)
        return IdeaViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: IdeaViewHolder, position: Int) {
        val currentIdea = ideasList[position]
        holder.description.text = currentIdea.descricao

        holder.detailsButton.setOnClickListener {
            val intent = Intent(holder.itemView.context, Ideias_detalhe::class.java).apply {
                putExtra("descricao", currentIdea.descricao)
                putExtra("detalhe", currentIdea.detalhe)
                putExtra("estado", currentIdea.estado)
                putExtra("criadodata", currentIdea.criadodata)
                putExtra("idtipo", currentIdea.idtipo)
            }
            holder.itemView.context.startActivity(intent)
        }
        holder.editButton.setOnClickListener {
            val currentIdea = ideasList[position]
            val adjustedIdtipo = currentIdea.idtipo?.minus(1)
            val intent = Intent(holder.itemView.context, EditarIdeiasActivity::class.java).apply {
                putExtra("idea_id", currentIdea.idideia)
                putExtra("descricao", currentIdea.descricao)
                putExtra("detalhe", currentIdea.detalhe)
                putExtra("idtipo", adjustedIdtipo)
            }
            if (currentIdea.estadoreg == 1 || currentIdea.estadoreg == 2) {
                holder.itemView.context.startActivity(intent)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Não é possível editar esta Ideia",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        holder.deleteButton.setOnClickListener {
            val currentIdea = ideasList[position]
            if (shouldDeleteIdea(currentIdea)) {      //se o estado reg = 1 consigo dar delete
                ideasList.removeAt(position)
                notifyDataSetChanged()
                deleteIdeaFromDatabase(holder.itemView.context, currentIdea.idideia)
            } else {
                Toast.makeText(
                    holder.itemView.context,
                    "Não é possível excluir esta Ideia",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun deleteIdeaFromDatabase(context: Context, ideaId: Int) {
        // Perform the deletion operation in the database
        val dbHelper = DatabaseHelper(context)
        dbHelper.apagaIdeia(ideaId)
    }
    fun clearIdeas() {
        ideasList.clear()
        notifyDataSetChanged()
    }
    private fun shouldDeleteIdea(idea: Idea): Boolean {
        return idea.estadoreg != 0
    }

    private fun shouldEditIdea(idea: Idea): Boolean {
        return idea.estadoreg != 0
    }

    fun updateData(newIdeas: List<Idea>) {
        ideasList.addAll(newIdeas)
    }

    override fun getItemCount(): Int {
        return ideasList.size
    }

    fun addIdeas(newIdeas: List<Idea>) {
        ideasList.addAll(newIdeas)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(idea: Idea) {
            itemView.findViewById<TextView>(R.id.idea_description).text = idea.descricao
        }
    }

}

data class Idea(
    val idideia: Int,
    val iduser: Int,
    val idtipo: Int?,
    val descricao: String,
    val detalhe: String,
    var estado: String?,
    val estadoreg: Int?,
    val criadodata: String?
)
