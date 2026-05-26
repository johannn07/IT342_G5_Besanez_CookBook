package com.it342.besanez.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.it342.besanez.R
import com.it342.besanez.ui.main.HomeActivity
import com.it342.besanez.ui.settings.SettingsActivity
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var viewModel: ProfileViewModel

    // Header
    private lateinit var ivAvatar: CircleImageView
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvCookingLevel: TextView

    // Section header button
    private lateinit var btnEditInfo: ImageButton

    // View-mode group + display fields
    private lateinit var groupViewMode: LinearLayout
    private lateinit var tvDisplayFirstName: TextView
    private lateinit var tvDisplayLastName: TextView
    private lateinit var tvDisplayEmail: TextView
    private lateinit var tvDisplayCookingLevel: TextView

    // Edit-mode group + input fields
    private lateinit var groupEditMode: LinearLayout
    private lateinit var etFirstName: com.google.android.material.textfield.TextInputEditText
    private lateinit var etLastName: com.google.android.material.textfield.TextInputEditText
    private lateinit var etEmailEdit: com.google.android.material.textfield.TextInputEditText
    private lateinit var spinnerCookingLevel: Spinner
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button

    // Nav
    private lateinit var btnSettings: Button
    private lateinit var btnLogout: Button

    private val cookingLevels = listOf("BEGINNER", "INTERMEDIATE", "ADVANCED")
    private var userId: Long = -1
    private var isEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "Profile"

        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        // Bind views
        ivAvatar               = view.findViewById(R.id.ivAvatar)
        tvName                 = view.findViewById(R.id.tvName)
        tvEmail                = view.findViewById(R.id.tvEmail)
        tvCookingLevel         = view.findViewById(R.id.tvCookingLevel)
        btnEditInfo            = view.findViewById(R.id.btnEditInfo)
        groupViewMode          = view.findViewById(R.id.groupViewMode)
        tvDisplayFirstName     = view.findViewById(R.id.tvDisplayFirstName)
        tvDisplayLastName      = view.findViewById(R.id.tvDisplayLastName)
        tvDisplayEmail         = view.findViewById(R.id.tvDisplayEmail)
        tvDisplayCookingLevel  = view.findViewById(R.id.tvDisplayCookingLevel)
        groupEditMode          = view.findViewById(R.id.groupEditMode)
        etFirstName            = view.findViewById(R.id.etFirstName)
        etLastName             = view.findViewById(R.id.etLastName)
        etEmailEdit            = view.findViewById(R.id.etEmailEdit)
        spinnerCookingLevel    = view.findViewById(R.id.spinnerCookingLevel)
        tvError                = view.findViewById(R.id.tvError)
        progressBar            = view.findViewById(R.id.progressBar)
        btnSave                = view.findViewById(R.id.btnSave)
        btnCancel              = view.findViewById(R.id.btnCancel)
        btnSettings            = view.findViewById(R.id.btnSettings)
        btnLogout              = view.findViewById(R.id.btnLogout)

        // Spinner adapter
        val spinnerAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            cookingLevels
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        spinnerCookingLevel.adapter = spinnerAdapter

        viewModel.loadProfile()
        setupObservers()
        setupListeners()
    }

    // ── Observers ────────────────────────────────────────────────────────────

    private fun setupObservers() {
        viewModel.user.observe(viewLifecycleOwner) { user ->
            userId = user.userId

            // Header
            tvName.text         = "${user.firstName} ${user.lastName}"
            tvEmail.text        = user.email
            tvCookingLevel.text = user.cookingLevel ?: "BEGINNER"

            if (!user.profileImage.isNullOrBlank()) {
                Glide.with(this).load(user.profileImage)
                    .placeholder(android.R.drawable.ic_menu_my_calendar)
                    .circleCrop().into(ivAvatar)
            }

            // Display fields
            tvDisplayFirstName.text    = user.firstName
            tvDisplayLastName.text     = user.lastName
            tvDisplayEmail.text        = user.email
            tvDisplayCookingLevel.text = user.cookingLevel ?: "BEGINNER"

            // Pre-fill edit fields (ready for when user taps Edit)
            etFirstName.setText(user.firstName)
            etLastName.setText(user.lastName)
            etEmailEdit.setText(user.email)
            val idx = cookingLevels.indexOf(user.cookingLevel ?: "BEGINNER")
            spinnerCookingLevel.setSelection(if (idx >= 0) idx else 0)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnSave.isEnabled = !isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            tvError.text = error ?: ""
            tvError.visibility = if (!error.isNullOrBlank()) View.VISIBLE else View.GONE
        }

        viewModel.updateResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { updated ->
                tvError.visibility = View.GONE
                // Update display fields immediately
                tvDisplayFirstName.text    = updated.firstName
                tvDisplayLastName.text     = updated.lastName
                tvDisplayCookingLevel.text = updated.cookingLevel ?: "BEGINNER"
                tvName.text                = "${updated.firstName} ${updated.lastName}"
                tvCookingLevel.text        = updated.cookingLevel ?: "BEGINNER"
                Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                setEditMode(false)
            }
            result.onFailure { e ->
                tvError.text = e.message
                tvError.visibility = View.VISIBLE
            }
        }
    }

    // ── Listeners ────────────────────────────────────────────────────────────

    private fun setupListeners() {
        btnEditInfo.setOnClickListener { setEditMode(true) }

        btnCancel.setOnClickListener {
            tvError.visibility = View.GONE
            // Restore edit fields from current display values
            etFirstName.setText(tvDisplayFirstName.text)
            etLastName.setText(tvDisplayLastName.text)
            val idx = cookingLevels.indexOf(tvDisplayCookingLevel.text.toString())
            spinnerCookingLevel.setSelection(if (idx >= 0) idx else 0)
            setEditMode(false)
        }

        btnSave.setOnClickListener {
            val firstName    = etFirstName.text.toString().trim()
            val lastName     = etLastName.text.toString().trim()
            val email        = etEmailEdit.text.toString().trim()
            val cookingLevel = cookingLevels[spinnerCookingLevel.selectedItemPosition]

            if (firstName.isEmpty() || lastName.isEmpty()) {
                tvError.text = "First and last name are required"
                tvError.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (userId == -1L) return@setOnClickListener

            viewModel.updateProfile(userId, firstName, lastName, email, cookingLevel)
        }

        btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        btnLogout.setOnClickListener {
            (activity as? HomeActivity)?.logout()
        }
    }

    // ── Mode toggle ──────────────────────────────────────────────────────────

    private fun setEditMode(editing: Boolean) {
        isEditing = editing
        groupViewMode.visibility = if (editing) View.GONE else View.VISIBLE
        groupEditMode.visibility = if (editing) View.VISIBLE else View.GONE
        btnEditInfo.visibility   = if (editing) View.GONE else View.VISIBLE
    }
}