package com.example.hipnosprueba.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.hipnosprueba.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegistrar.setOnClickListener {
            val user = binding.etUsuario.text.toString()
            val pass = binding.etContrasena.text.toString()
            if (user.isNotEmpty() && pass.isNotEmpty()) {
                Toast.makeText(this, "Registrado correctamente (simulado)", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }
}