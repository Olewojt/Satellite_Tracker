
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.satellitetracker.R
import com.example.satellitetracker.models.ListItem

class ListItemAdapter(private val dataList: MutableList<ListItem>) :
    RecyclerView.Adapter<ListItemAdapter.ViewHolder>(), Filterable {

    private var filteredDataList: MutableList<ListItem> = dataList.toMutableList()
    private lateinit var mListener : onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: ListItem)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    filterResults.values = dataList.toMutableList()
                } else {
                    val filteredList = dataList.filter {
                        it.name.contains(constraint.toString(), true) ||
                            it.operator.contains(constraint.toString(), true) ||
                                it.user.contains(constraint.toString(), true)
                    }.toMutableList()
                    filterResults.values = filteredList
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredDataList.clear()
                filteredDataList.addAll(results?.values as? MutableList<ListItem> ?: mutableListOf())
                notifyDataSetChanged()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item, parent, false)
        return ViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = filteredDataList[position]
        holder.textViewNorad.text = item.norad.toString()
        holder.textViewName.text = item.name
        holder.textViewOperator.text = item.operator
        holder.textViewUser.text = item.user
    }

    override fun getItemCount(): Int {
        return filteredDataList.size
    }

    inner class ViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val textViewNorad: TextView = itemView.findViewById(R.id.text_sat_norad)
        val textViewName: TextView = itemView.findViewById(R.id.text_sat_name)
        val textViewOperator: TextView = itemView.findViewById(R.id.text_sat_operator)
        val textViewUser: TextView = itemView.findViewById(R.id.text_sat_user)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(filteredDataList[adapterPosition])
            }
        }
    }
}
