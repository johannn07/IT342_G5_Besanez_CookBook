package com.it342.besanez.ui.recipe

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.it342.besanez.R
import com.it342.besanez.model.CollectionResponse
import com.it342.besanez.network.ApiClient
import kotlinx.coroutines.launch

class AddToCollectionDialog(
    private val recipeId: Long,
    private val recipeName: String
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext(), R.style.CookBook_Dialog)
            .setTitle("Add to Collection")
            .setView(buildView())
            .create()

    private lateinit var adapter: CollectionCheckAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var rv: RecyclerView

    private fun buildView(): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_to_collection, null)

        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        rv = view.findViewById(R.id.rvCollections)

        adapter = CollectionCheckAdapter()
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        loadCollections()

        // Positive button wired after show — set here via neutral workaround
        view.tag = "ready"
        return view
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as? androidx.appcompat.app.AlertDialog ?: return
        // Override buttons after dialog shown so we control dismiss
        d.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE,
            "Add"
        ) { _, _ -> /* handled below */ }
        d.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE,
            "Cancel"
        ) { _, _ -> dismiss() }

        d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener { confirmAdd() }
    }

    private fun loadCollections() {
        progressBar.visibility = View.VISIBLE
        rv.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.getCollections(size = 100, sort = "createdAt,asc")
                val list = if (res.isSuccessful) res.body()?.content ?: emptyList() else emptyList()
                progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    rv.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.text = "Failed to load collections."
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun confirmAdd() {
        val selected = adapter.getSelected()
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one collection", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            var successCount = 0
            selected.forEach { col ->
                try {
                    val res = ApiClient.apiService.addRecipeToCollection(col.id, recipeId)
                    if (res.isSuccessful) successCount++
                } catch (_: Exception) {}
            }
            val msg = if (successCount == selected.size)
                "Added \"$recipeName\" to ${successCount} collection${if (successCount > 1) "s" else ""}!"
            else
                "Added to $successCount of ${selected.size} collections."
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            dismiss()
        }
    }
}

// ── Adapter ──────────────────────────────────────────────────────────────────

class CollectionCheckAdapter : RecyclerView.Adapter<CollectionCheckAdapter.VH>() {

    private val items = mutableListOf<CollectionResponse>()
    private val selected = mutableSetOf<Long>()

    fun submitList(list: List<CollectionResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelected(): List<CollectionResponse> = items.filter { it.id in selected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection_check, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cb: CheckBox = itemView.findViewById(R.id.checkbox)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvCount: TextView = itemView.findViewById(R.id.tvCount)

        fun bind(col: CollectionResponse) {
            tvName.text = col.name
            tvCount.text = "${col.recipeCount} recipe${if (col.recipeCount != 1) "s" else ""}"
            cb.isChecked = col.id in selected
            val toggle = {
                if (col.id in selected) selected.remove(col.id) else selected.add(col.id)
                cb.isChecked = col.id in selected
            }
            cb.setOnClickListener { toggle() }
            itemView.setOnClickListener { toggle() }
        }
    }
}