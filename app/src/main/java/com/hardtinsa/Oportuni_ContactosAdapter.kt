package com.hardtinsa

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.instamobile.kotlinlogin.R

class Oportuni_ContactosAdapter(private val contactosList: MutableList<Contacto>) : RecyclerView.Adapter<Oportuni_ContactosAdapter.ContactoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_oportcontacto_item, parent, false)
        return ContactoViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactoViewHolder, position: Int) {
        val contacto = contactosList[position]

    }

    override fun getItemCount(): Int {
        return contactosList.size
    }

    inner class ContactoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {



    }
}

data class Contacto(
    val idoportunidade: Int,
    val idcontacto: Int
)


