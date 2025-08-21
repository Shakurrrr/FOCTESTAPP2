package com.example.foc_test_app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import java.io.File
import java.io.FileOutputStream

class ProductFragment : Fragment() {

    // UI
    private lateinit var ivProduct: ImageView
    private lateinit var etTitle: TextInputEditText
    private lateinit var etDescription: TextInputEditText
    private lateinit var btnChangeImage: MaterialButton
    private lateinit var btnBuyNow: MaterialButton

    // Photo Picker (modern; no storage permission)
    private val pickImage = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri ?: return@registerForActivityResult
        val path = copyImageToAppStorage(uri)
        if (path != null) {
            prefsPut(KEY_IMAGE, path)
            ivProduct.setImageURI(Uri.fromFile(File(path)))
        } else {
            snack("Couldn't load image")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_product, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Bind views
        ivProduct = view.findViewById(R.id.ivProduct)
        etTitle = view.findViewById(R.id.etTitle)
        etDescription = view.findViewById(R.id.etDescription)
        btnChangeImage = view.findViewById(R.id.btnChangeImage)
        btnBuyNow = view.findViewById(R.id.btnBuyNow)

        // Restore saved state
        prefsGet(KEY_TITLE)?.let { etTitle.setText(it) }
        prefsGet(KEY_DESC)?.let { etDescription.setText(it) }
        prefsGet(KEY_IMAGE)?.let { loadImageFromPath(it) }

        // Handlers
        btnChangeImage.setOnClickListener {
            runCatching {
                pickImage.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            }.onFailure { snack("This device cannot open the photo picker") }
        }

        btnBuyNow.setOnClickListener {
            val title = etTitle.text?.toString()?.trim().orEmpty()
            val desc = etDescription.text?.toString()?.trim().orEmpty()

            if (title.isEmpty()) {
                etTitle.error = "Title is required"
                return@setOnClickListener
            }

            // Persist current inputs (so returning to this screen shows latest values)
            prefsPut(KEY_TITLE, title)
            prefsPut(KEY_DESC, desc)

            // Hook for future checkout flow
            snack("Proceeding to buy \"$title\"")
        }
    }

    // ---- Helpers -------------------------------------------------------------

    /** Copy picked image to app-private storage and return absolute file path. */
    private fun copyImageToAppStorage(src: Uri): String? {
        val dir = File(requireContext().filesDir, "product").apply { mkdirs() }
        val ext = guessExt(src) ?: "jpg"
        val dst = File(dir, "main.$ext")
        return try {
            requireContext().contentResolver.openInputStream(src).use { input ->
                FileOutputStream(dst).use { output ->
                    if (input != null) input.copyTo(output)
                }
            }
            dst.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    private fun guessExt(uri: Uri): String? {
        val type = requireContext().contentResolver.getType(uri) ?: return null
        return type.substringAfter('/', "").ifEmpty { null }
    }

    private fun loadImageFromPath(path: String) {
        val f = File(path)
        if (f.exists()) {
            ivProduct.setImageURI(Uri.fromFile(f))
        } else {
            // Stale path; clear it so we don't keep trying to load a missing file
            prefsPut(KEY_IMAGE, "")
        }
    }

    private fun prefs(): Context =
        requireContext().applicationContext

    private fun sp() =
        prefs().getSharedPreferences("product", Context.MODE_PRIVATE)

    private fun prefsPut(key: String, value: String) {
        sp().edit().putString(key, value).apply()
    }

    private fun prefsGet(key: String): String? = sp().getString(key, null)

    private fun snack(message: String) {
        view?.let { Snackbar.make(it, message, Snackbar.LENGTH_SHORT).show() }
    }

    companion object {
        private const val KEY_IMAGE = "image_path"
        private const val KEY_TITLE = "title"
        private const val KEY_DESC = "desc"
    }
}
