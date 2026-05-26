package com.it342.besanez.ui.collection

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.it342.besanez.R
import com.it342.besanez.model.CollectionResponse
import com.it342.besanez.model.RecipeResponse
import com.it342.besanez.ui.recipe.RecipeDetailActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class CollectionDetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_ID   = "collection_id"
        const val EXTRA_NAME = "collection_name"
        private const val SLIDE_INTERVAL_MS = 3_000L
    }

    private lateinit var vm: CollectionViewModel
    private lateinit var recipeAdapter: CollectionRecipeAdapter

    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvCount: TextView
    private lateinit var rvRecipes: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvEmpty: TextView
    private lateinit var tvError: TextView

    private lateinit var heroFrame: FrameLayout
    private lateinit var ivHero: ImageView
    private val slideshowHandler = Handler(Looper.getMainLooper())
    private var slideImages: List<String> = emptyList()
    private var slideIndex = 0

    private val advanceSlide = object : Runnable {
        override fun run() {
            if (slideImages.size > 1) {
                slideIndex = (slideIndex + 1) % slideImages.size
                loadHeroImage(slideImages[slideIndex])
                slideshowHandler.postDelayed(this, SLIDE_INTERVAL_MS)
            }
        }
    }

    private var collectionId = 0L

    // ── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection_detail)

        collectionId = intent.getLongExtra(EXTRA_ID, 0L)
        val collectionName = intent.getStringExtra(EXTRA_NAME) ?: "Collection"

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = collectionName

        vm = ViewModelProvider(this)[CollectionViewModel::class.java]

        tvTitle     = findViewById(R.id.tvTitle)
        tvDesc      = findViewById(R.id.tvDesc)
        tvCount     = findViewById(R.id.tvCount)
        rvRecipes   = findViewById(R.id.rvRecipes)
        progressBar = findViewById(R.id.progressBar)
        tvEmpty     = findViewById(R.id.tvEmpty)
        tvError     = findViewById(R.id.tvError)
        heroFrame   = findViewById(R.id.heroFrame)
        ivHero      = findViewById(R.id.ivHero)

        setupList()
        setupObservers()

        vm.loadById(collectionId)
    }

    override fun onResume() {
        super.onResume()
        startSlideshow()
    }

    override fun onPause() {
        super.onPause()
        stopSlideshow()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSlideshow()
    }

    // ── Slideshow ────────────────────────────────────────────────────────────

    private fun initSlideshow(images: List<String>) {
        slideImages = images.filter { it.isNotBlank() }
        slideIndex = 0
        if (slideImages.isEmpty()) {
            heroFrame.visibility = View.GONE
            return
        }
        heroFrame.visibility = View.VISIBLE
        loadHeroImage(slideImages[0])
        startSlideshow()
    }

    private fun startSlideshow() {
        if (slideImages.size > 1) {
            slideshowHandler.removeCallbacks(advanceSlide)
            slideshowHandler.postDelayed(advanceSlide, SLIDE_INTERVAL_MS)
        }
    }

    private fun stopSlideshow() {
        slideshowHandler.removeCallbacks(advanceSlide)
    }

    private fun loadHeroImage(url: String) {
        Glide.with(this)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade(400))
            .centerCrop()
            .into(ivHero)
    }

    // ── Options menu ─────────────────────────────────────────────────────────

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_collection_detail, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add_recipes -> {
                showAddRecipesDialog()
                true
            }
            R.id.action_edit -> {
                val col = vm.selected.value ?: return true
                showEditDialog(col)
                true
            }
            R.id.action_delete -> {
                MaterialAlertDialogBuilder(this, R.style.CookBook_Dialog)
                    .setTitle("Delete Collection")
                    .setMessage("Delete this collection? Recipes inside are not deleted.")
                    .setPositiveButton("Delete") { _, _ -> vm.delete(collectionId) }
                    .setNegativeButton("Cancel", null)
                    .show()
                true
            }
            android.R.id.home -> { finish(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // ── Recipes list ─────────────────────────────────────────────────────────

    private fun setupList() {
        recipeAdapter = CollectionRecipeAdapter(
            onView = { recipe ->
                startActivity(
                    Intent(this, RecipeDetailActivity::class.java)
                        .putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id)
                )
            },
            onRemove = { recipe -> confirmRemove(recipe) }
        )
        rvRecipes.layoutManager = LinearLayoutManager(this)
        rvRecipes.adapter = recipeAdapter
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        vm.selected.observe(this) { col ->
            col ?: return@observe
            tvTitle.text = col.name
            tvDesc.text  = col.description ?: ""
            tvDesc.visibility = if (col.description.isNullOrBlank()) View.GONE else View.VISIBLE
            tvCount.text = "${col.recipeCount} recipe${if (col.recipeCount != 1) "s" else ""}"
            supportActionBar?.title = col.name
            initSlideshow(col.recipeImages ?: emptyList())
        }

        vm.recipes.observe(this) { list ->
            recipeAdapter.submitList(list)
            tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        vm.loading.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }

        vm.error.observe(this) { msg ->
            if (!msg.isNullOrBlank()) {
                tvError.text = msg
                tvError.visibility = View.VISIBLE
                vm.clearError()
            } else {
                tvError.visibility = View.GONE
            }
        }

        vm.event.observe(this) { event ->
            when (event) {
                is CollectionViewModel.Event.Updated -> {
                    Toast.makeText(this, "Collection updated", Toast.LENGTH_SHORT).show()
                    vm.clearEvent()
                }
                is CollectionViewModel.Event.Deleted -> {
                    Toast.makeText(this, "Collection deleted", Toast.LENGTH_SHORT).show()
                    vm.clearEvent()
                    finish()
                }
                is CollectionViewModel.Event.RecipeRemoved -> {
                    Toast.makeText(this, "Recipe removed", Toast.LENGTH_SHORT).show()
                    vm.clearEvent()
                    vm.loadById(collectionId)
                }
                else -> {}
            }
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun showAddRecipesDialog() {
        val existingIds = recipeAdapter.currentList.map { it.id }.toSet()
        AddRecipesDialog(
            collectionId = collectionId,
            existingIds = existingIds,
            onAdded = { vm.loadById(collectionId) }
        ).show(supportFragmentManager, "add_recipes")
    }

    private fun showEditDialog(col: CollectionResponse) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_collection_form, null)
        val etName = dialogView.findViewById<android.widget.EditText>(R.id.etName)
        val etDesc = dialogView.findViewById<android.widget.EditText>(R.id.etDesc)
        etName.setText(col.name)
        etDesc.setText(col.description ?: "")

        MaterialAlertDialogBuilder(this, R.style.CookBook_Dialog)
            .setTitle("Edit Collection")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                vm.update(col.id, etName.text.toString(), etDesc.text.toString())
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmRemove(recipe: RecipeResponse) {
        MaterialAlertDialogBuilder(this, R.style.CookBook_Dialog)
            .setTitle("Remove Recipe")
            .setMessage("Remove \"${recipe.name}\" from this collection?")
            .setPositiveButton("Remove") { _, _ -> vm.removeRecipe(collectionId, recipe.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}