package com.example.foc_test_app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class ProfileFragment : Fragment() {

    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var iv3: ImageView
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private lateinit var tv3: TextView

    // Which avatar index (1..3) is being updated
    private var targetIndex = 0

    // Modern Photo Picker (no runtime storage permission required)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null && targetIndex in 1..3) {
            // Copy into app-private storage and get a stable file path
            val localPath = copyImageToAppStorage(uri, targetIndex)
            if (localPath != null) {
                when (targetIndex) {
                    1 -> iv1.setImageURI(Uri.fromFile(File(localPath)))
                    2 -> iv2.setImageURI(Uri.fromFile(File(localPath)))
                    3 -> iv3.setImageURI(Uri.fromFile(File(localPath)))
                }
                prefsPut("avatar_path_$targetIndex", localPath)
            }
        }
        targetIndex = 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_profile, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        iv1 = view.findViewById(R.id.ivAvatar1)
        iv2 = view.findViewById(R.id.ivAvatar2)
        iv3 = view.findViewById(R.id.ivAvatar3)
        tv1 = view.findViewById(R.id.tvUsername1)
        tv2 = view.findViewById(R.id.tvUsername2)
        tv3 = view.findViewById(R.id.tvUsername3)

        // Restore usernames
        tv1.text = prefsGet("username_1") ?: tv1.text
        tv2.text = prefsGet("username_2") ?: tv2.text
        tv3.text = prefsGet("username_3") ?: tv3.text

        // Restore avatars from stable file paths
        loadAvatar(iv1, prefsGet("avatar_path_1"))
        loadAvatar(iv2, prefsGet("avatar_path_2"))
        loadAvatar(iv3, prefsGet("avatar_path_3"))

        // Edit username actions
        view.findViewById<View>(R.id.btnEdit1).setOnClickListener { editUsername(1, tv1) }
        view.findViewById<View>(R.id.btnEdit2).setOnClickListener { editUsername(2, tv2) }
        view.findViewById<View>(R.id.btnEdit3).setOnClickListener { editUsername(3, tv3) }

        // Tap avatar -> launch Photo Picker
        iv1.setOnClickListener { choosePhoto(1) }
        iv2.setOnClickListener { choosePhoto(2) }
        iv3.setOnClickListener { choosePhoto(3) }
    }

    private fun choosePhoto(index: Int) {
        targetIndex = index
        runCatching {
            pickImage.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }.onFailure {
            // Extremely rare on broken OEMs; no-op rather than crashing
            targetIndex = 0
        }
    }

    private fun editUsername(index: Int, target: TextView) {
        val input = TextInputEditText(requireContext()).apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS
            setText(target.text)
            setSelection(text?.length ?: 0)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit username")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val txt = input.text?.toString()?.trim().orEmpty()
                if (txt.isNotEmpty()) {
                    target.text = txt
                    prefsPut("username_$index", txt)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Copy the picked image into app-private storage and return the absolute file path.
     * This avoids content-URI permission issues on subsequent loads.
     */
    private fun copyImageToAppStorage(src: Uri, index: Int): String? {
        val dir = File(requireContext().filesDir, "avatars").apply { mkdirs() }
        // Preserve extension if possible; default to .jpg
        val ext = guessExtension(src) ?: "jpg"
        val dst = File(dir, "avatar_$index.$ext")
        return try {
            requireContext().contentResolver.openInputStream(src).use { input ->
                FileOutputStream(dst).use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            dst.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    private fun guessExtension(uri: Uri): String? {
        val type = requireContext().contentResolver.getType(uri) ?: return null
        // e.g., "image/png" -> "png"
        return type.substringAfter('/', missingDelimiterValue = "").ifEmpty { null }
    }

    private fun loadAvatar(target: ImageView, path: String?) {
        if (path.isNullOrEmpty()) return
        val f = File(path)
        if (!f.exists()) {
            // Clean dead reference
            when (target.id) {
                R.id.ivAvatar1 -> prefsPut("avatar_path_1", "")
                R.id.ivAvatar2 -> prefsPut("avatar_path_2", "")
                R.id.ivAvatar3 -> prefsPut("avatar_path_3", "")
            }
            return
        }
        runCatching { target.setImageURI(Uri.fromFile(f)) }
        // If the device decides to re-query size later, this stays valid (file in app storage).
    }

    // --- Simple SharedPreferences helpers ---
    private fun prefs() = requireContext()
        .getSharedPreferences("profiles", Context.MODE_PRIVATE)

    private fun prefsPut(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
    }

    private fun prefsGet(key: String): String? = prefs().getString(key, null)
}
