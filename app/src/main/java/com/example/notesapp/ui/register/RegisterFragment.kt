package com.example.notesapp.ui.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.notesapp.databinding.FragmentRegisterBinding
import com.example.notesapp.core.AuthService
import com.google.android.material.snackbar.Snackbar
import kotlin.getValue

class RegisterFragment : Fragment() {
    private val viewModel: RegisterViewModel by viewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            val email = binding.registerEmail.text.toString()
            val pass = binding.registerPassword.text.toString()
            AuthService.register(email, pass) { success, err ->
                if(success) {
                    findNavController().popBackStack()
                } else {
                    // Log.e("error", err.toString())
                    val snackbar = Snackbar.make(
                        view,
                        err.toString(),
                        Snackbar.LENGTH_LONG
                    )

                    snackbar.show()
                }
            }
        }

        binding.goToLogin.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}