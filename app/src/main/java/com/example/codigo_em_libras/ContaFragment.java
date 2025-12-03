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

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ContaFragment extends Fragment {

    private ImageView imgPerfil, logoutButton;
    private TextView txtNomeUsuario, txtEmailUsuario;
    private Switch switchTema;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private ActivityResultLauncher<Intent> abrirGaleria;

    // Variáveis carregadas do SharedPreferences
    String email;
    String nome;
    boolean modoEscuro;
    String imagemBase64;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso);
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

        // -------------------------------
        //   Carrega SharedPreferences
        // -------------------------------
        PrefsHelper prefs = new PrefsHelper(requireContext());
        String dadosConta = prefs.getString("prefs_conta");

        if (dadosConta != null && dadosConta.contains(";")) {
            String[] partes = dadosConta.split(";");

            email = partes[0];
            nome = partes[1];
            imagemBase64 = partes[2];
            modoEscuro = Boolean.parseBoolean(partes[3]);
        }

        txtNomeUsuario.setText(nome);
        txtEmailUsuario.setText(email);
        switchTema.setChecked(modoEscuro);

        // -------------------------------
        //   Carrega Foto de Perfil
        // -------------------------------
        if (imagemBase64 != null && !imagemBase64.isEmpty()) {
            imgPerfil.setImageBitmap(converterBase64ParaBitmap(imagemBase64));
        }

        // -------------------------------
        //   Aplica Tema
        // -------------------------------
        AppCompatDelegate.setDefaultNightMode(
                modoEscuro ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        switchTema.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.putBoolean("modoEscuro", isChecked);

            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        // -------------------------------
        //   Ação para selecionar imagem
        // -------------------------------
        abrirGaleria = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri imagemSelecionada = result.getData().getData();
                        try {
                            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                    requireActivity().getContentResolver(), imagemSelecionada);

                            imgPerfil.setImageBitmap(bitmap);

                            // Salvar em SharedPreferences
                            if (dadosConta != null && dadosConta.contains(";")) {
                                String[] partes = dadosConta.split(";");

                                String dadosAtualizados = partes[0]+";"+partes[1]+";"+converterBitmapParaBase64(bitmap)+";"+partes[3];
                                prefs.putString("prefs_conta", dadosAtualizados);

                                new SalvamentoDados().salvarDadosConta(requireContext());
                            }


                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Erro ao carregar imagem", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        imgPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            abrirGaleria.launch(intent);
        });

        // -------------------------------
        //   Logout
        // -------------------------------
        logoutButton.setOnClickListener(v -> {
            // Firebase logout
            mAuth.signOut();

            // Google logout
            mGoogleSignInClient.signOut().addOnCompleteListener(requireActivity(), task -> {
                startActivity(new Intent(getActivity(), LoginActivity.class));
                Toast.makeText(getContext(), "Logout bem sucedido!", Toast.LENGTH_SHORT).show();
                requireActivity().finish();
            });
        });

        return view;
    }

    // -------------------------------
    //   Conversores Base64
    // -------------------------------
    private String converterBitmapParaBase64(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT);
    }

    private Bitmap converterBase64ParaBitmap(String imagemBase64) {
        byte[] bytes = Base64.decode(imagemBase64, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}
