package com.fasa.ziptechdevmovieapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.fasa.ziptechdevmovieapp.R
import com.fasa.ziptechdevmovieapp.database.MoviesDatabase
import com.fasa.ziptechdevmovieapp.databinding.ActivityMainBinding
import com.fasa.ziptechdevmovieapp.repository.MoviesRepository
import com.fasa.ziptechdevmovieapp.ui.viewmodelfactories.MainViewModelFactory
import com.fasa.ziptechdevmovieapp.ui.viewmodels.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding

    val viewModel : MainViewModel by lazy {
        val db = MoviesDatabase(this)
        val moviesRepository = MoviesRepository(db)
        val viewModelProviderFactory = MainViewModelFactory(moviesRepository)
        ViewModelProvider(this, viewModelProviderFactory)[MainViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)




        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

    }
}