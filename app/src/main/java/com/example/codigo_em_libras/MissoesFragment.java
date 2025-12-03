package com.example.codigo_em_libras;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MissoesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MissoesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private ViewPager2 viewPager2;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    TextView quantidadeEstrelas;


    public MissoesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MissoesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MissoesFragment newInstance(String param1, String param2) {
        MissoesFragment fragment = new MissoesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_missoes, container, false);
        FrameLayout rootLayout = view.findViewById(R.id.fragmentMissoes);

        View layoutFilho = inflater.inflate(R.layout.mundo_tipo, rootLayout, false);
        rootLayout.addView(layoutFilho);

        ImageView imageMesa = layoutFilho.findViewById(R.id.mesaMundoImageView);
        TextView tituloMundo = layoutFilho.findViewById(R.id.tituloMundoTextView);
        View linhaDivisora = layoutFilho.findViewById(R.id.linhaDivisor);

        /*ImageButton buttonMundo1 = layoutFilho.findViewById(R.id.iniciarMundo1ImageButton);
        buttonMundo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), Mundo1Activity.class);
                startActivity(intent);
            }
        });*/

        ViewPager2 viewPager2 = layoutFilho.findViewById(R.id.viewPagerImageSlider);

        List<SliderItem> sliderItems = new ArrayList<>();
        sliderItems.add(new SliderItem(R.drawable.mundo1_layout));
        sliderItems.add(new SliderItem(R.drawable.mundo2_layout));
        sliderItems.add(new SliderItem(R.drawable.mundo3_layout));

        SliderAdapter adapter = new SliderAdapter(sliderItems, viewPager2);
        viewPager2.setAdapter(adapter);

        quantidadeEstrelas = layoutFilho.findViewById(R.id.quantidadeEstrelas);
        PrefsHelper prefs = new PrefsHelper(requireContext());

        // Listener para atualizar as estrelas quando mudar
        listener = (sharedPrefs, key) -> {
            if ("estrelas".equals(key)) {
                int novasEstrelas = new SalvamentoDados().contarEstrelas(requireContext());
                quantidadeEstrelas.setText(novasEstrelas + "/63");
            }
        };

        int novasEstrelas = new SalvamentoDados().contarEstrelas(requireContext());

        quantidadeEstrelas.setText(novasEstrelas + "/63");

        // Registrar o listener
        PrefsHelper prefsHelper = new PrefsHelper(requireContext());

        prefsHelper.getPrefs().registerOnSharedPreferenceChangeListener(listener);
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            String imagemAntiga = "";
            String imagemAtual = "";
            int mesaDrawable;
            String mundoTitulo = "";

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                imagemAntiga = imagemAtual;


                // Exemplo de ação ao rolar
                if (position == 0) {
                    mesaDrawable = R.drawable.mesa_mundo1;
                    imagemAtual = "mundo1";
                    mundoTitulo = "Mundo Silencioso";
                } else if (position == 1) {
                    mesaDrawable = R.drawable.mesa_mundo1;
                    imagemAtual = "mundo1";
                    mundoTitulo = "Treinamento";
                } else{
                    mesaDrawable = R.drawable.mesa_mundo3;
                    imagemAtual = "mundo3";
                    mundoTitulo = "Campo de Batalha";
                }

                tituloMundo.setText(mundoTitulo);

                if (!Objects.equals(imagemAntiga, imagemAtual)) {
                    imageMesa.animate()
                            .translationY(500f)
                            .setDuration(300)
                            .setInterpolator(new AccelerateDecelerateInterpolator()) // deixa suave
                            .withEndAction(() -> {
                                imageMesa.setImageResource(mesaDrawable);
                                imageMesa.animate()
                                        .translationY(0f)
                                        .setDuration(500)
                                        .setInterpolator(new AccelerateDecelerateInterpolator()) // deixa suave
                                        .start();
                            })
                            .start();

                }else {
                    imageMesa.setImageResource(mesaDrawable);

                };

                linhaDivisora.setScaleX(0f);

                linhaDivisora.animate()
                        .scaleX(1f)
                        .setDuration(500)
                        .start();

            }
        });

        adapter.setOnItemClickListener(position -> {
            switch (position) {
                case 0:
                    // Mundo 1
                    startActivity(new Intent(getContext(), Mundo1Activity.class));
                    break;
                case 1:
                    // Mundo 2
                    startActivity(new Intent(getContext(), Mundo2Activity.class));
                    break;
                case 2:
                    // Mundo 3
                    startActivity(new Intent(getContext(), Mundo3Activity.class));
                    break;
            }
        });

        // Remove o efeito de overscroll (aquele brilho feio nas bordas quando tentamos ir além da primeira ou última imagens)
        RecyclerView recyclerView = (RecyclerView) viewPager2.getChildAt(0);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        ImageButton previousButton = view.findViewById(R.id.previousButton);
        ImageButton nextButton = view.findViewById(R.id.nextButton);

        previousButton.setOnClickListener(v -> {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem > 0) {
                viewPager2.setCurrentItem(currentItem - 1, true);
            }
        });

        nextButton.setOnClickListener(v -> {
            int currentItem = viewPager2.getCurrentItem();
            if (currentItem < sliderItems.size() - 1) {
                viewPager2.setCurrentItem(currentItem + 1, true);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}