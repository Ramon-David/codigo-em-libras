package com.example.codigo_em_libras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ContaFragment extends Fragment {

    private ImageView imgPerfil, logoutButton;
    private TextView txtNomeUsuario, txtEmailUsuario;
    private Switch switchTema;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
    private SharedPreferences prefs;

    private ActivityResultLauncher<Intent> abrirGaleria;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);

        prefs = requireActivity().getSharedPreferences("app-config", requireActivity().MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conta, container, false);

        imgPerfil = view.findViewById(R.id.imgPerfil);
        txtNomeUsuario = view.findViewById(R.id.txtNomeUsuario);
        txtEmailUsuario = view.findViewById(R.id.txtEmailUsuario);
        switchTema = view.findViewById(R.id.switchTema);
        logoutButton = view.findViewById(R.id.logoutButton);

        // Recupera dados salvos
        String nome = prefs.getString("nome", "Usuário");
        String email = prefs.getString("email", "usuario@gmail.com");
        String imagemBase64 = prefs.getString("fotoPerfil", null);
        boolean modoEscuro = prefs.getBoolean("modoEscuro", false);

        txtNomeUsuario.setText(nome);
        txtEmailUsuario.setText(email);
        switchTema.setChecked(modoEscuro);

        if (imagemBase64 != null) {
            byte[] bytes = Base64.decode(imagemBase64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imgPerfil.setImageBitmap(bitmap);
        }

        // Define o modo salvo
        AppCompatDelegate.setDefaultNightMode(
                modoEscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        // Alternar tema
        switchTema.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            prefs.edit().putBoolean("modoEscuro", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // Configura a galeria
        abrirGaleria = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imagemSelecionada = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(), imagemSelecionada);
                            imgPerfil.setImageBitmap(bitmap);

                            // Salva em SharedPreferences
                            prefs.edit().putString("fotoPerfil", converterBitmapParaBase64(bitmap)).apply();
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        imgPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            abrirGaleria.launch(intent);
        });

        logoutButton.setOnClickListener(v -> {

            // Limpa SharedPreferences locais
            SharedPreferences prefs = requireActivity().getSharedPreferences("app-config", getActivity().MODE_PRIVATE);
            prefs.edit().clear().apply();

            // Firebase Sign-out
            mAuth.signOut();

            // Google Sign-out
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                // Vai para a tela de login
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
                Toast.makeText(getContext(), "Logout bem sucedido!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            });
        });

        return view;
    }

    private String converterBitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}