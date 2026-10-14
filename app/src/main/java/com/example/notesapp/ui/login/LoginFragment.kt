package com.example.notesapp.ui.login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.notesapp.databinding.FragmentLoginBinding
import com.example.notesapp.core.AuthService
import com.google.android.material.snackbar.Snackbar
import kotlin.getValue

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(AuthService.isUserLoggedIn()) {
            val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
            findNavController().navigate(action)
        }

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString()
            val pass = binding.loginPassword.text.toString()
            AuthService.login(email, pass) { success, err ->
                if(success) {
                    val action = LoginFragmentDirections.actionLoginFragmentToHomeFragment()
                    findNavController().navigate(action)
                } else {
                    // show a snackbar
                    val snackbar = Snackbar.make(
                        view,
                        err.toString(),
                        Snackbar.LENGTH_LONG
                    )

                    snackbar.show()
                }
            }
        }

        binding.goToRegister.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            findNavController().navigate(action)
        }
    }
}