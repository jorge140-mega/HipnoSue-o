package com.example.hipnosprueba

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.hipnosprueba.databinding.ActivityMenuBinding
import com.example.hipnosprueba.ui.SettingsActivity
import com.example.hipnosprueba.ui.fragments.MonitorFragment
import com.example.hipnosprueba.ui.fragments.ReportFragment
import com.example.hipnosprueba.ui.fragments.SettingsFragment
import com.example.hipnosprueba.ui.fragments.StatsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Cargar por defecto el MonitorFragment
        loadFragment(MonitorFragment())

        // Botones superiores
        binding.btnMonitor.setOnClickListener {
            loadFragment(MonitorFragment())
        }

        binding.btnReport.setOnClickListener {
            loadFragment(ReportFragment())
        }

        binding.btnStats.setOnClickListener {
            loadFragment(StatsFragment())
        }

        // Barra de navegación inferior
        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_back -> {
                    onBackPressedDispatcher.onBackPressed()
                    true
                }
                R.id.menu_settings -> {
                    startActivity(Intent(this,SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }
}
