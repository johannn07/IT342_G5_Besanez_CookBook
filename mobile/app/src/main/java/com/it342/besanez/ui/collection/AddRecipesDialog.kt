package com.it342.besanez.ui.collection

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.it342.besanez.R
import com.it342.besanez.model.RecipeResponse
import com.it342.besanez.network.ApiClient
import kotlinx.coroutines.launch

/**
 * Dialog listing all user recipes with checkboxes.
 * Adds selected recipes to the given collectionId on confirm.
 * Excludes recipes already in the collection via [existingIds].
 */
class AddRecipesDialog(
    private val collectionId: Long,
    private val existingIds: Set<Long> = emptySet(),
    private val onAdded: () -> Unit = {}
) : DialogFragment() {

    private lateinit var adapter: RecipeCheckAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvSelected: TextView
    private lateinit var rv: RecyclerView
    private lateinit var etSearch: TextInputEditText

    private var allRecipes: List<RecipeResponse> = emptyList()

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        MaterialAlertDialogBuilder(requireContext(), R.style.CookBook_Dialog)
            .setTitle("Add Recipes")
            .setView(buildView())
            .create()

    private fun buildView(): View {
        val view = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_recipes, null)

        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)
        tvSelected = view.findViewById(R.id.tvSelected)
        rv = view.findViewById(R.id.rvRecipes)
        etSearch = view.findViewById(R.id.etSearch)

        adapter = RecipeCheckAdapter { count ->
            if (count > 0) {
                tvSelected.text = "$count recipe${if (count > 1) "s" else ""} selected"
                tvSelected.visibility = View.VISIBLE
            } else {
                tvSelected.visibility = View.GONE
            }
        }
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val q = s?.toString()?.trim() ?: ""
                adapter.submitList(allRecipes.filter {
                    q.isEmpty() || it.name.contains(q, ignoreCase = true)
                })
                tvEmpty.visibility = if (adapter.itemCount == 0) View.VISIBLE else View.GONE
                rv.visibility = if (adapter.itemCount > 0) View.VISIBLE else View.GONE
            }
        })

        loadRecipes()
        return view
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as? androidx.appcompat.app.AlertDialog ?: return
        d.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE, "Add"
        ) { _, _ -> /* handled below */ }
        d.setButton(
            androidx.appcompat.app.AlertDialog.BUTTON_NEGATIVE, "Cancel"
        ) { _, _ -> dismiss() }
        d.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            ?.setOnClickListener { confirmAdd() }
    }

    private fun loadRecipes() {
        progressBar.visibility = View.VISIBLE
        rv.visibility = View.GONE
        tvEmpty.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.getRecipes(size = 200, sort = "createdAt,asc")
                val list = (if (res.isSuccessful) res.body()?.content ?: emptyList() else emptyList())
                    .filter { it.id !in existingIds }
                allRecipes = list
                progressBar.visibility = View.GONE
                if (list.isEmpty()) {
                    tvEmpty.text = "All your recipes are already in this collection."
                    tvEmpty.visibility = View.VISIBLE
                } else {
                    rv.visibility = View.VISIBLE
                    adapter.submitList(list)
                }
            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                tvEmpty.text = "Failed to load recipes."
                tvEmpty.visibility = View.VISIBLE
            }
        }
    }

    private fun confirmAdd() {
        val selected = adapter.getSelected()
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one recipe", Toast.LENGTH_SHORT).show()
            return
        }
        lifecycleScope.launch {
            var successCount = 0
            selected.forEach { recipe ->
                try {
                    val res = ApiClient.apiService.addRecipeToCollection(collectionId, recipe.id)
                    if (res.isSuccessful) successCount++
                } catch (_: Exception) {}
            }
            Toast.makeText(
                requireContext(),
                "Added $successCount recipe${if (successCount != 1) "s" else ""} to collection!",
                Toast.LENGTH_SHORT
            ).show()
            dismiss()
            onAdded()
        }
    }
}

// ── Adapter ──────────────────────────────────────────────────────────────────

class RecipeCheckAdapter(
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<RecipeCheckAdapter.VH>() {

    private val items = mutableListOf<RecipeResponse>()
    private val selected = mutableSetOf<Long>()

    fun submitList(list: List<RecipeResponse>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun getSelected(): List<RecipeResponse> = items.filter { it.id in selected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recipe_check, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cb: CheckBox = itemView.findViewById(R.id.checkbox)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvMeta: TextView = itemView.findViewById(R.id.tvMeta)
        private val ivThumb: ImageView = itemView.findViewById(R.id.ivThumb)

        fun bind(recipe: RecipeResponse) {
            tvName.text = recipe.name
            tvMeta.text = recipe.totalTimeMinutes?.let { min ->
                if (min < 60) "${min}m" else "${min / 60}h${if (min % 60 > 0) " ${min % 60}m" else ""}"
            } ?: if (recipe.isPublic) "Public" else "Private"

            if (!recipe.imageUrl.isNullOrBlank()) {
                Glide.with(itemView).load(recipe.imageUrl).centerCrop().into(ivThumb)
            } else {
                ivThumb.setImageResource(android.R.drawable.ic_menu_gallery)
            }

            cb.isChecked = recipe.id in selected
            val toggle = {
                if (recipe.id in selected) selected.remove(recipe.id) else selected.add(recipe.id)
                cb.isChecked = recipe.id in selected
                onSelectionChanged(selected.size)
            }
            cb.setOnClickListener { toggle() }
            itemView.setOnClickListener { toggle() }
        }
    }
}