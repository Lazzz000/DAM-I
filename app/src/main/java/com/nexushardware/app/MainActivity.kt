package com.nexushardware.app

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import androidx.fragment.app.Fragment
import com.nexushardware.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Esto es para cargar Inicio por defecto
        replaceFragment(InicioFragment())

        //configurar clics
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> replaceFragment(InicioFragment())
                R.id.nav_productos -> replaceFragment(ProductosFragment())
                R.id.nav_mapa -> replaceFragment(MapaFragment())
                R.id.nav_contacto -> replaceFragment(ContactoFragment())
                R.id.nav_carrito -> replaceFragment(CarritoFragment())//agregado para probarlo
            }
            true
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}