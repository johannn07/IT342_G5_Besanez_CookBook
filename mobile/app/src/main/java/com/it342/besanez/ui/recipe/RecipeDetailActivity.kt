package com.it342.besanez.ui.recipe

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.it342.besanez.R

class RecipeDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RECIPE_ID = "recipe_id"
        const val RESULT_DELETED = 100
    }

    private lateinit var vm: RecipeDetailViewModel
    private var recipeId = 0L
    private var recipeName = ""

    private lateinit var ivImage: ImageView
    private lateinit var tvName: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvPrepTime: TextView
    private lateinit var tvCookTime: TextView
    private lateinit var tvTotalTime: TextView
    private lateinit var tvVisibility: TextView
    private lateinit var llIngredients: LinearLayout
    private lateinit var llInstructions: LinearLayout
    private lateinit var tvNotes: TextView
    private lateinit var sectionNotes: View
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recipe_detail)

        recipeId = intent.getLongExtra(EXTRA_RECIPE_ID, 0L)
        vm = ViewModelProvider(this)[RecipeDetailViewModel::class.java]

        ivImage        = findViewById(R.id.ivImage)
        tvName         = findViewById(R.id.tvName)
        tvDesc         = findViewById(R.id.tvDesc)
        tvPrepTime     = findViewById(R.id.tvPrepTime)
        tvCookTime     = findViewById(R.id.tvCookTime)
        tvTotalTime    = findViewById(R.id.tvTotalTime)
        tvVisibility   = findViewById(R.id.tvVisibility)
        llIngredients  = findViewById(R.id.llIngredients)
        llInstructions = findViewById(R.id.llInstructions)
        tvNotes        = findViewById(R.id.tvNotes)
        sectionNotes   = findViewById(R.id.sectionNotes)
        progressBar    = findViewById(R.id.progressBar)
        tvError        = findViewById(R.id.tvError)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Recipe"

        observe()
        vm.load(recipeId)
    }

    // ── Options menu ─────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_recipe_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_to_collection -> {
                AddToCollectionDialog(recipeId, recipeName)
                    .show(supportFragmentManager, "add_to_collection")
                true
            }
            R.id.action_edit -> {
                val intent = Intent(this, CreateRecipeActivity::class.java)
                intent.putExtra(CreateRecipeActivity.EXTRA_RECIPE_ID, recipeId)
                startActivityForResult(intent, 200)
                true
            }
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun observe() {
        vm.loading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) {
            if (!it.isNullOrBlank()) {
                tvError.text = it
                tvError.visibility = View.VISIBLE
            }
        }

        vm.recipe.observe(this) { recipe ->
            recipe ?: return@observe
            recipeName = recipe.name
            supportActionBar?.title = recipe.name
            tvName.text = recipe.name
            tvDesc.text = recipe.description ?: ""
            tvDesc.visibility = if (recipe.description.isNullOrBlank()) View.GONE else View.VISIBLE

            tvPrepTime.text  = formatTime(recipe.prepTimeMinutes, "Prep")
            tvCookTime.text  = formatTime(recipe.cookTimeMinutes, "Cook")
            tvTotalTime.text = formatTime(recipe.totalTimeMinutes, "Total")

            listOf(tvPrepTime, tvCookTime, tvTotalTime).forEach { tv ->
                tv.visibility = if (tv.text.isNullOrBlank()) View.GONE else View.VISIBLE
            }

            tvVisibility.text = if (recipe.isPublic) "🌐 Public" else "🔒 Private"

            tvNotes.text = recipe.notes ?: ""
            sectionNotes.visibility = if (recipe.notes.isNullOrBlank()) View.GONE else View.VISIBLE

            if (!recipe.imageUrl.isNullOrBlank()) {
                ivImage.visibility = View.VISIBLE
                Glide.with(this).load(recipe.imageUrl).centerCrop().into(ivImage)
            } else {
                ivImage.visibility = View.GONE
            }
        }

        vm.ingredients.observe(this) { list ->
            llIngredients.removeAllViews()
            list.forEach { ing ->
                val qty  = if (ing.quantity > 0) "${ing.quantity} " else ""
                val unit = if (!ing.unit.isNullOrBlank()) "${ing.unit.lowercase()} " else ""
                val tv = TextView(this).apply {
                    text = "• $qty$unit${ing.name}" +
                            if (!ing.notes.isNullOrBlank()) "  (${ing.notes})" else ""
                    textSize = 14f
                    setTextColor(getColor(R.color.text_mid))
                    setPadding(0, 8, 0, 8)
                }
                llIngredients.addView(tv)
            }
        }

        vm.instructions.observe(this) { list ->
            llInstructions.removeAllViews()
            list.sortedBy { it.stepNumber }.forEachIndexed { idx, step ->
                val tv = TextView(this).apply {
                    text = "${idx + 1}. ${step.description}"
                    textSize = 14f
                    setTextColor(getColor(R.color.text_mid))
                    setPadding(0, 10, 0, 10)
                }
                llInstructions.addView(tv)
            }
        }

        vm.deleted.observe(this) { deleted ->
            if (deleted) {
                setResult(RESULT_DELETED)
                finish()
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Recipe")
            .setMessage("This cannot be undone.")
            .setPositiveButton("Delete") { _, _ -> vm.delete(recipeId) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatTime(minutes: Int?, label: String): String {
        if (minutes == null || minutes <= 0) return ""
        val h = minutes / 60; val m = minutes % 60
        val t = if (h > 0 && m > 0) "${h}h ${m}m" else if (h > 0) "${h}h" else "${m}m"
        return "$label: $t"
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == RESULT_OK) vm.load(recipeId)
    }
}