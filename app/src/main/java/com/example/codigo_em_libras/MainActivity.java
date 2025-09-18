package com.example.codigo_em_libras;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.codigo_em_libras.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Firebase;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    String tituloAtual;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);

        tituloAtual = "Missões";
        replaceFragment(new MissoesFragment());
        View iconMissions = bottomNav.findViewById(R.id.missions);

        iconMissions.setBackground(getDrawable(R.drawable.bottom_nav_bar_customizada));
        iconMissions.animate().scaleX(1.25f).scaleY(1.25f).setDuration(150).start();

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            for (int i = 0; i < bottomNav.getMenu().size(); i++) {
                View iconView = bottomNav.findViewById(bottomNav.getMenu().getItem(i).getItemId());
                if (iconView != null) {
                    iconView.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
                    iconView.setBackground(null);
                }
            }

            if (id == R.id.missions) {
                // Ação para Missões
                tituloAtual = "Missões";
                replaceFragment(new MissoesFragment());
            } else if (id == R.id.materiais) {
                // Ação para Materiais
                tituloAtual = "Materiais";
                replaceFragment(new MateriaisFragment());
            } else if (id == R.id.conta) {
                tituloAtual = "Conta";
                // Ação para Conta
                replaceFragment(new ContaFragment());
            }

            // Aumenta o ícone do item selecionado
            View selectedIconView = bottomNav.findViewById(id);
            if (selectedIconView != null) {
                selectedIconView.setBackground(getDrawable(R.drawable.bottom_nav_bar_customizada));
                selectedIconView.animate().scaleX(1.25f).scaleY(1.25f).setDuration(150).start();
             //   selectedIconView.animate().y(-10f).setDuration(150).start();
            }

            return true;

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.frameLayout,fragment);
        fragmentTransaction.commit();
    }
}