package com.assulfisoft.oramentodecapital;

import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.assulfisoft.oramentodecapital.adapter.GlossaryAdapter;

public class GlossaryActivity extends AppCompatActivity {

    //Atributos

    //Intancia do objeto ViewPager
    private ViewPager mPager;

    //Instacia do objeto PageAdapter
    private PagerAdapter mPageAdapter;

    //Instancia do objeto TabLayout
    private TabLayout mTabLayout;

    //Métodos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_glossary);

        //complete: Terminar a configuração da GlossaryActivity

        //Recupera o ViewPager da tela
        mPager = (ViewPager) findViewById(R.id.glossary_viewpager);

        //Instancia do adapter
        mPageAdapter = new GlossaryAdapter(this,getSupportFragmentManager());

        //Coloca o adapter no ViewPager
        mPager.setAdapter(mPageAdapter);

        //Recupera a TabLayout
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mTabLayout.setupWithViewPager(mPager, true);

    }
}
