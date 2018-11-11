package com.assulfisoft.oramentodecapital.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Classe que armazena as informações de um determinado projeto
 * <p>
 * Created by LuisDaniel on 03/11/2018.
 */

public class Projeto implements Parcelable {

    public static final Creator<Projeto> CREATOR = new Creator<Projeto>() {
        @Override
        public Projeto createFromParcel(Parcel in) {
            return new Projeto(in);
        }

        @Override
        public Projeto[] newArray(int size) {
            return new Projeto[size];
        }
    };
    //Atributos
    private String mNome; //nome do projeto
    private double mTaxaDeReinvestimento; //taxa de reinvestimento
    private double mTaxaDeFinanciamento; //taxa de financiamento
    private double mValorPresenteLiquido; //valor presente líquido
    private double mTaxaInternaDeRetorno; //Valor da taxa interna de retorno
    private double mPayback; //tempo de retorno
    private ArrayList<CashFlow> mFluxoDeCaixa;

    //Construtor
    public Projeto(String nome, double taxaDeReinvestimento, double taxaDeFinanciamento) {
        this.mNome = nome;
        this.mTaxaDeReinvestimento = taxaDeReinvestimento;
        this.mTaxaDeFinanciamento = taxaDeFinanciamento;
        this.mValorPresenteLiquido = 0.0;
        this.mTaxaInternaDeRetorno = 0.0;
        this.mPayback = 0.0;
        this.mFluxoDeCaixa = new ArrayList<>();
    }

    private Projeto(Parcel in) {
        mNome = in.readString();
        mTaxaDeReinvestimento = in.readDouble();
        mTaxaDeFinanciamento = in.readDouble();
        mValorPresenteLiquido = in.readDouble();
        mTaxaInternaDeRetorno = in.readDouble();
        mPayback = in.readDouble();
        mFluxoDeCaixa = in.createTypedArrayList(CashFlow.CREATOR);
    }

    public String getNome() {
        return mNome;
    }

    //Geters & Setters
    public void setNome(String nome) {
        this.mNome = nome;
    }

    public double getTaxaDeReinvestimento() {
        return mTaxaDeReinvestimento;
    }

    public void setTaxaDeReinvestimento(double taxa) {
        this.mTaxaDeReinvestimento = taxa;
    }

    public double getTaxaDeFinanciamento() {
        return mTaxaDeFinanciamento;
    }

    public void setTaxaDeFinanciamento(double taxa) {
        this.mTaxaDeFinanciamento = taxa;
    }

    /**
     * Recupera o valor calculado do valor presente liquido
     *
     * @return
     */
    public double getVPL() {
        return mValorPresenteLiquido;
    }

    /**
     * Define o atributo do valor presente líquido (VPL)
     *
     * @param vpl double com o valor calculado do valor presente líquido
     */
    public void setVPL(double vpl) {
        this.mValorPresenteLiquido = vpl;
    }

    /**
     * Recupera o valor da taxa interna de retorno
     *
     * @return double contendo o valor da taxa interna de retorno
     */
    public double getTIR() {
        return mTaxaInternaDeRetorno;
    }

    /**
     * Define o valor da taxa interna de retorno
     *
     * @param tir double com o valor calculado da tir
     */
    public void setTIR(double tir) {
        this.mTaxaInternaDeRetorno = tir;
    }

    public double getPayback() {
        return mPayback;
    }

    public void setPayback(double payback) {
        this.mPayback = payback;
    }

    /**
     * Adiciona um fluxo de caixa a lista
     *
     * @param cashFlow
     */
    public void adicionarFluxoDeCaixa(CashFlow cashFlow) {
        mFluxoDeCaixa.add(cashFlow);
    }

    /**
     * Atualiza o fluxo de caixa em uma dada posicão
     *
     * @param position [int] posicao a ser atualizada
     * @param cashFlow fluxo de caixa
     */
    public void atualizarFluxoDeCaixa(int position, CashFlow cashFlow) {
        mFluxoDeCaixa.set(position, cashFlow);
    }

    /**
     * Apaga todos os itens do fluxo de caixa
     */
    public void apagarFluxoDeCaixa() {
        mFluxoDeCaixa.clear();
    }

    /**
     * Recupera o fluxo de caixa
     *
     * @return
     */
    public ArrayList<CashFlow> getFluxoDeCaixa() {
        return mFluxoDeCaixa;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mNome);
        out.writeDouble(mTaxaDeFinanciamento);
        out.writeDouble(mTaxaDeReinvestimento);
        out.writeDouble(mTaxaInternaDeRetorno);
        out.writeDouble(mValorPresenteLiquido);
        out.writeDouble(mPayback);
        out.writeTypedList(mFluxoDeCaixa);
    }
}
