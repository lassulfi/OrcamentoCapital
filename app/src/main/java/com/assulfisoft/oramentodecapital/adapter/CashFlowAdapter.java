package com.assulfisoft.oramentodecapital.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.assulfisoft.oramentodecapital.R;
import com.assulfisoft.oramentodecapital.model.CashFlow;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter para popular a listView com as informações do fluxo de caixa
 *
 * Created by LuisDaniel on 12/12/2017.
 */

public class CashFlowAdapter extends ArrayAdapter<CashFlow> {

    //Atributos
    private ArrayList<CashFlow> cashFlowArrayList;

    private String currency;

    //Métodos especiais

    /**
     * Construtor da classe CashFlowAdapter
     *
     * @param c the Context of the Adapter
     * @param objects
     */
    public CashFlowAdapter(Activity c, ArrayList<CashFlow> objects) {
        super(c, 0, objects);
        this.cashFlowArrayList = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //Criando a view
        View listItemView = convertView;

        //Verifica se o @link cashFlowArrayList não está vazia
        if (!cashFlowArrayList.isEmpty()){
            //Inicializa o objeto para a montagem da view

            if (listItemView == null) {
                //Montagem da view a partir do arquivo .xml
                listItemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.list_item,parent,false);
            }

            //Recupera os elementos para exibição
            TextView periodTextView = (TextView) listItemView.findViewById(R.id.tv_periodo);
            TextView cashFlowTextView = (TextView) listItemView.findViewById(R.id.tv_fluxo_caixa);

            //Passa os dados da ArrayList para a classe CashFlow
            CashFlow cashFlow = cashFlowArrayList.get(position);

            //Recupera a unidade de moeda da sharedPreferences
            setupSharedPreferences();

            //Exibe os valores no elemento do layout
            periodTextView.setText(Integer.toString(cashFlow.getPeriod()));
            String formattedCashFlow = String.format("%.2f",cashFlow.getCash());
            cashFlowTextView.setText(currency + " " + formattedCashFlow);
        }

        return listItemView;
    }

    /**
     * Método que recupera as informações da sharedPreferences
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        currency = sharedPreferences.getString(getContext().getString(R.string.pref_currency_key)
                ,getContext().getString(R.string.pref_currency_default_value));
    }

}
