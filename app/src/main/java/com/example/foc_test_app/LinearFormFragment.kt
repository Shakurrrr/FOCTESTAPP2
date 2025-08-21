package com.example.foc_test_app


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment


class LinearFormFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_linear_form, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val name = view.findViewById<EditText>(R.id.etName)
        val email = view.findViewById<EditText>(R.id.etEmail)
        val phone = view.findViewById<EditText>(R.id.etPhone)
        view.findViewById<View>(R.id.btnSubmit).setOnClickListener {
            Toast.makeText(requireContext(), "Submitted: ${'$'}{name.text}", Toast.LENGTH_SHORT).show()
        }
    }
}