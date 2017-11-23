package es.ellacer.masterlistas

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import es.ellacer.masterlistas.ListaAdapter.ListaViewHolder
import kotlinx.android.synthetic.main.elemento_lista.view.*


/**
 * Created by eloy on 29/10/17.
 */
class ListaAdapter(val items: List<Lista>, val ctx: Context) : RecyclerView.Adapter<ListaViewHolder>() { //, val listener: (Lista) -> Unit


    override fun onBindViewHolder(viewHolder: ListaViewHolder, position: Int) {
        val item = items.get(position)
        viewHolder.imagen.setImageResource(item.imagen)
        viewHolder.nombre.text = item.nombre
        viewHolder.elementos.text = ctx.getString(R.string.elementos) + item.elementos.toString()
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup?, viewType: Int): ListaViewHolder {
        val v = LayoutInflater.from(viewGroup?.getContext()).inflate(R.layout.elemento_lista, viewGroup, false)
        return ListaViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }


    class ListaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imagen = itemView.imagen
        val nombre = itemView.nombre
        val elementos = itemView.elementos
    }
}