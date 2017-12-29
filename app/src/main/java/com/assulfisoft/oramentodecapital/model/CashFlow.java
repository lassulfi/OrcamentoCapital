package com.assulfisoft.oramentodecapital.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Classe para armazenar os valores do fluxo de caixa, que são armazenados na listView
 *
 * Created by LuisDaniel on 12/12/2017.
 */

public class CashFlow implements Parcelable {

    //Atributos
    private int period;
    private double cash;

    //Métodos especiais

    //Construtor

    /**
     * Construtor da classe CashFlow (vazio)
     */
    public CashFlow() {
    }

    /**
     * Construtor da classe CashFlow (com parâmetros iniciais
     * @param p período (int)
     * @param c cashFlow (double)
     */
    public CashFlow(int p, double c){
        this.period = p;
        this.cash = c;
    }

    //Getters & Setters
    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(period);
        parcel.writeDouble(cash);
    }

    protected CashFlow(Parcel in) {
        period = in.readInt();
        cash = in.readDouble();
    }

    public static final Creator<CashFlow> CREATOR = new Creator<CashFlow>() {
        @Override
        public CashFlow createFromParcel(Parcel in) {
            return new CashFlow(in);
        }

        @Override
        public CashFlow[] newArray(int size) {
            return new CashFlow[size];
        }
    };

}
