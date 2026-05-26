package com.it342.besanez.ui.collection

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.it342.besanez.R
import com.it342.besanez.model.CollectionResponse

class CollectionFragment : Fragment() {

    private lateinit var vm: CollectionViewModel
    private lateinit var adapter: CollectionAdapter

    private lateinit var rvCollections: RecyclerView
    private lateinit var etSearch: TextInputEditText
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_collection, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Collections"

        vm = ViewModelProvider(this)[CollectionViewModel::class.java]

        rvCollections = view.findViewById(R.id.rvCollections)
        etSearch = view.findViewById(R.id.etSearch)
        fabAdd = view.findViewById(R.id.fabAddCollection)
        progressBar = view.findViewById(R.id.progressBar)
        tvEmpty = view.findViewById(R.id.tvEmpty)

        setupList()
        setupSearch()
        setupObservers()

        vm.load()

        fabAdd.setOnClickListener { showFormDialog(null) }
    }

    // Reload when returning from CollectionDetailActivity (e.g. after deletion)
    override fun onResume() {
        super.onResume()
        if (::vm.isInitialized) {
            vm.load()
        }
    }

    // ── RecyclerView ─────────────────────────────────────────────────────────

    private fun setupList() {
        adapter = CollectionAdapter(
            onClick = { col ->
                startActivity(
                    Intent(requireContext(), CollectionDetailActivity::class.java)
                        .putExtra(CollectionDetailActivity.EXTRA_ID, col.id)
                        .putExtra(CollectionDetailActivity.EXTRA_NAME, col.name)
                )
            },
            onEdit = { col -> showFormDialog(col) },
            onDelete = { col -> confirmDelete(col) }
        )
        rvCollections.layoutManager = LinearLayoutManager(requireContext())
        rvCollections.adapter = adapter
    }

    // ── Search ───────────────────────────────────────────────────────────────

    private fun setupSearch() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                vm.searchDebounced(s?.toString() ?: "")
            }
        })
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        vm.collections.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility =
                if (loading && adapter.itemCount == 0) View.VISIBLE else View.GONE
        }

        vm.error.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                vm.clearError()
            }
        }

        vm.event.observe(viewLifecycleOwner) { event ->
            event ?: return@observe
            val msg = when (event) {
                is CollectionViewModel.Event.Created -> "Collection created"
                is CollectionViewModel.Event.Updated -> "Collection updated"
                is CollectionViewModel.Event.Deleted -> "Collection deleted"
                else -> null
            }
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            vm.clearEvent()
        }
    }

    // ── Create / Edit dialog ─────────────────────────────────────────────────

    private fun showFormDialog(existing: CollectionResponse?) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_collection_form, null)

        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etDesc = dialogView.findViewById<EditText>(R.id.etDesc)

        existing?.let {
            etName.setText(it.name)
            etDesc.setText(it.description ?: "")
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.CookBook_Dialog)
            .setTitle(if (existing == null) "New Collection" else "Edit Collection")
            .setView(dialogView)
            .setPositiveButton(if (existing == null) "Create" else "Save") { _, _ ->
                val name = etName.text.toString()
                val desc = etDesc.text.toString()
                if (existing == null) vm.create(name, desc)
                else vm.update(existing.id, name, desc)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Delete confirm ───────────────────────────────────────────────────────

    private fun confirmDelete(col: CollectionResponse) {
        MaterialAlertDialogBuilder(requireContext(), R.style.CookBook_Dialog)
            .setTitle("Delete Collection")
            .setMessage("Delete \"${col.name}\"? Recipes inside are not deleted.")
            .setPositiveButton("Delete") { _, _ -> vm.delete(col.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}