package com.assulfisoft.oramentodecapital.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.assulfisoft.oramentodecapital.fragment.OutrosFragment;
import com.assulfisoft.oramentodecapital.fragment.PaybackFragment;
import com.assulfisoft.oramentodecapital.fragment.TIRFragment;
import com.assulfisoft.oramentodecapital.fragment.TIRMFragment;
import com.assulfisoft.oramentodecapital.fragment.VPLFragment;

/**
 * PageAdapter para exibir os fragments da apresentação do Glossário em sequencia
 *
 * Created by LuisDaniel on 27/12/2017.
 */

public class GlossaryAdapter extends FragmentStatePagerAdapter {

    private Context mContext;

    //Número de páginas (fragments) da apresentação
    private static final int NUM_PAGES = 5;

    public GlossaryAdapter(Context context,FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new VPLFragment();
            case 1:
                return new TIRFragment();
            case 2:
                return new TIRMFragment();
            case 3:
                return new PaybackFragment();
            case 4:
                return new OutrosFragment();
            default:
                return new VPLFragment();
        }
    }

    @Override
    public int getCount() {
        return NUM_PAGES;
    }
}
