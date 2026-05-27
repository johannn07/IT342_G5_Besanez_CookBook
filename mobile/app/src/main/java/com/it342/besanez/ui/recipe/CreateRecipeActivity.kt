package com.it342.besanez.ui.recipe

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.it342.besanez.R
import com.it342.besanez.network.ApiClient
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class CreateRecipeActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id" // null = create, non-zero = edit
    }

    private lateinit var vm: CreateRecipeViewModel
    private var editId = 0L

    // Form fields
    private lateinit var etName: EditText
    private lateinit var etDesc: EditText
    private lateinit var etPrepTime: EditText
    private lateinit var etCookTime: EditText
    private lateinit var etTotalTime: EditText
    private lateinit var etNotes: EditText
    private lateinit var etImageUrl: EditText
    private lateinit var cbPublic: CheckBox
    private lateinit var llIngredients: LinearLayout
    private lateinit var llInstructions: LinearLayout
    private lateinit var btnAddIngredient: Button
    private lateinit var btnAddStep: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPickImage: Button
    private var uploadedImageUrl: String? = null

    private val ingredientRows = mutableListOf<IngredientRow>()
    private val instructionRows = mutableListOf<InstructionRow>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_recipe)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        editId = intent.getLongExtra(EXTRA_RECIPE_ID, 0L)
        vm = ViewModelProvider(this)[CreateRecipeViewModel::class.java]

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (editId > 0) "Edit Recipe" else "New Recipe"

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = if (editId > 0) "Edit Recipe" else "New Recipe"

        bindViews()

        etTotalTime.apply {
            isEnabled = false
            isFocusable = false
            hint = "Auto‑calculated"
        }
        fun updateTotal() {
            val prep = etPrepTime.text.toString().toIntOrNull() ?: 0
            val cook = etCookTime.text.toString().toIntOrNull() ?: 0
            val total = prep + cook
            etTotalTime.setText(if (total > 0) total.toString() else "")
        }
        val timeWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) { updateTotal() }
        }
        etPrepTime.addTextChangedListener(timeWatcher)
        etCookTime.addTextChangedListener(timeWatcher)

        setupListeners()
        observe()

        if (editId > 0) loadForEdit()
        else {
            addIngredientRow()   // Start with one empty row
            addInstructionRow()
        }
    }

    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri ?: return@registerForActivityResult
        ivImagePreview.setImageURI(uri)
        ivImagePreview.visibility = View.VISIBLE
        uploadImage(uri)
    }

    private fun bindViews() {
        etName = findViewById(R.id.etName)
        etDesc = findViewById(R.id.etDesc)
        etPrepTime = findViewById(R.id.etPrepTime)
        etCookTime = findViewById(R.id.etCookTime)
        etTotalTime = findViewById(R.id.etTotalTime)
        etNotes = findViewById(R.id.etNotes)
        cbPublic = findViewById(R.id.cbPublic)
        llIngredients = findViewById(R.id.llIngredients)
        llInstructions = findViewById(R.id.llInstructions)
        btnAddIngredient = findViewById(R.id.btnAddIngredient)
        btnAddStep = findViewById(R.id.btnAddStep)
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)

        ivImagePreview = findViewById(R.id.ivImagePreview)
        btnPickImage = findViewById(R.id.btnPickImage)
    }

    private fun setupListeners() {
        btnAddIngredient.setOnClickListener { addIngredientRow() }
        btnAddStep.setOnClickListener { addInstructionRow() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { save() }

        btnPickImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
    }

    private fun uploadImage(uri: Uri) {
        val stream = contentResolver.openInputStream(uri) ?: return
        val bytes = stream.readBytes()
        val mime = contentResolver.getType(uri) ?: "image/jpeg"
        val ext = when (mime) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val part = MultipartBody.Part.createFormData(
            "file", "recipe.$ext",
            bytes.toRequestBody(mime.toMediaType())
        )
        lifecycleScope.launch {
            try {
                val res = ApiClient.apiService.uploadImage(part, "recipes")
                if (res.isSuccessful) {
                    uploadedImageUrl = res.body()?.url
                    Toast.makeText(this@CreateRecipeActivity, "Photo uploaded", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@CreateRecipeActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@CreateRecipeActivity, "Network error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observe() {
        vm.loading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
            findViewById<Button>(R.id.btnSave).isEnabled = !it
        }
        vm.error.observe(this) {
            tvError.text = it ?: ""
            tvError.visibility = if (it.isNullOrBlank()) View.GONE else View.VISIBLE
        }
        vm.saved.observe(this) { recipe ->
            recipe ?: return@observe
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun addIngredientRow(prefill: IngredientRow? = null) {
        val row = prefill ?: IngredientRow()
        ingredientRows.add(row)

        val rowView = layoutInflater.inflate(R.layout.item_ingredient_input, llIngredients, false)

        val etQty = rowView.findViewById<EditText>(R.id.etQty)
        val spinnerUnit = rowView.findViewById<Spinner>(R.id.spinnerUnit)
        val etIngName = rowView.findViewById<EditText>(R.id.etIngName)
        val etIngNotes = rowView.findViewById<EditText>(R.id.etIngNotes)
        val btnRemove = rowView.findViewById<ImageButton>(R.id.btnRemoveIng)

        // IngredientUnit enum values (exactly as in backend)
        val unitOptions = listOf("") + listOf(
            "G", "KG", "OZ", "LB",                       // Weight
            "ML", "L", "TSP", "TBSP", "CUP", "FL_OZ",    // Volume
            "PIECE", "PINCH", "CLOVE", "SLICE", "OTHER"   // Count / other
        )

        // Use custom HintAdapter to show "Unit" as grey hint
        val adapter = HintAdapter(this, unitOptions, "Unit")
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = adapter

        // Pre-select if editing, else keep hint (position 0)
        val selectedIndex = if (row.unit.isNotEmpty()) unitOptions.indexOf(row.unit).coerceAtLeast(0) else 0
        spinnerUnit.setSelection(selectedIndex)

        spinnerUnit.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Position 0 is the hint, keep unit empty
                row.unit = if (position == 0) "" else unitOptions[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // Populate other fields
        etQty.setText(if (row.quantity > 0) row.quantity.toString() else "")
        etIngName.setText(row.name)
        etIngNotes.setText(row.notes)

        etQty.onFocusChange { row.quantity = it.trim().toIntOrNull() ?: 0 }
        etIngName.onFocusChange { row.name = it.trim() }
        etIngNotes.onFocusChange { row.notes = it.trim() }

        btnRemove.setOnClickListener {
            ingredientRows.remove(row)
            llIngredients.removeView(rowView)
        }

        llIngredients.addView(rowView)
    }

    private fun addInstructionRow(prefill: InstructionRow? = null) {
        val row = prefill ?: InstructionRow(stepNumber = instructionRows.size + 1)
        instructionRows.add(row)

        val rowView = layoutInflater.inflate(R.layout.item_instruction_input, llInstructions, false)
        val tvStep = rowView.findViewById<TextView>(R.id.tvStepNum)
        val etStep = rowView.findViewById<EditText>(R.id.etStepDesc)
        val btnRemove = rowView.findViewById<ImageButton>(R.id.btnRemoveStep)

        tvStep.text = "${instructionRows.size}"
        etStep.setText(row.description)
        etStep.onFocusChange { row.description = it.trim() }

        btnRemove.setOnClickListener {
            instructionRows.remove(row)
            llInstructions.removeView(rowView)
            renumberSteps()
        }

        llInstructions.addView(rowView)
    }

    private fun renumberSteps() {
        instructionRows.forEachIndexed { idx, row -> row.stepNumber = idx + 1 }
        for (i in 0 until llInstructions.childCount) {
            llInstructions.getChildAt(i)?.findViewById<TextView>(R.id.tvStepNum)?.text = "${i + 1}"
        }
    }

    private fun save() {
        // Flush focus to capture last typed value
        currentFocus?.clearFocus()

        val name = etName.text.toString().trim()
        if (name.isBlank()) { tvError.text = "Name required"; tvError.visibility = View.VISIBLE; return }

        val prep = etPrepTime.text.toString().toIntOrNull()
        val cook = etCookTime.text.toString().toIntOrNull()
        val total = etTotalTime.text.toString().toIntOrNull()

        if (editId > 0) {
            vm.update(editId, name, etDesc.text(), prep, cook, total,
                etNotes.text(), uploadedImageUrl, cbPublic.isChecked,
                ingredientRows, instructionRows)
        } else {
            vm.create(name, etDesc.text(), prep, cook, total,
                etNotes.text(), uploadedImageUrl, cbPublic.isChecked,
                ingredientRows, instructionRows)
        }
    }

    private fun loadForEdit() {
        lifecycleScope.launch {
            try {
                val api = ApiClient.apiService
                val r = api.getRecipeById(editId)
                val ing = api.getIngredients(editId)
                val inst = api.getInstructions(editId)

                if (r.isSuccessful) r.body()?.let { recipe ->
                    etName.setText(recipe.name)
                    etDesc.setText(recipe.description ?: "")
                    etPrepTime.setText(recipe.prepTimeMinutes?.toString() ?: "")
                    etCookTime.setText(recipe.cookTimeMinutes?.toString() ?: "")
                    etTotalTime.setText(recipe.totalTimeMinutes?.toString() ?: "")
                    etNotes.setText(recipe.notes ?: "")
                    etImageUrl.setText(recipe.imageUrl ?: "")
                    cbPublic.isChecked = recipe.isPublic

                    if (!recipe.imageUrl.isNullOrBlank()) {
                        uploadedImageUrl = recipe.imageUrl
                        ivImagePreview.visibility = View.VISIBLE
                        Glide.with(this@CreateRecipeActivity).load(recipe.imageUrl).centerCrop().into(ivImagePreview)
                    }
                }
                if (ing.isSuccessful) ing.body()?.forEach { i ->
                    addIngredientRow(IngredientRow(i.id, i.name, i.quantity, i.unit ?: "", i.notes ?: ""))
                }
                if (inst.isSuccessful) inst.body()?.sortedBy { it.stepNumber }?.forEach { s ->
                    addInstructionRow(InstructionRow(s.id, s.stepNumber, s.description))
                }
            } catch (_: Exception) {}
        }
    }

    // Extension helpers
    private fun EditText.text() = text.toString().trim().ifBlank { null }
    private fun EditText.onFocusChange(block: (String) -> Unit) {
        setOnFocusChangeListener { _, hasFocus -> if (!hasFocus) block(text.toString()) }
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }
}

class HintAdapter(
    context: Context,
    private val items: List<String>,
    private val hintText: String = "Unit"
) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    override fun getCount(): Int = items.size

    override fun getItem(position: Int): String = items[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent) as TextView
        if (position == 0) {
            view.text = hintText
            view.setTextColor(Color.GRAY)
        } else {
            view.setTextColor(Color.BLACK)  // or use your app's default text color
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent) as TextView
        if (position == 0) {
            view.text = hintText
            view.setTextColor(Color.GRAY)
        } else {
            view.setTextColor(Color.BLACK)
        }
        return view
    }

    override fun isEnabled(position: Int): Boolean = position != 0
}