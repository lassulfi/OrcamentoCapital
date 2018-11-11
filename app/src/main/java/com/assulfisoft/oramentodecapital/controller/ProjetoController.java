package com.assulfisoft.oramentodecapital.controller;

import android.os.Parcel;
import android.os.Parcelable;

import com.assulfisoft.oramentodecapital.model.CashFlow;
import com.assulfisoft.oramentodecapital.model.Projeto;

import java.util.ArrayList;

/**
 * Classe controller que controla o ciclo de vida de um projeto
 * Created by LuisDaniel on 03/11/2018.
 */

public class ProjetoController implements Parcelable {

    public static final Creator<ProjetoController> CREATOR = new Creator<ProjetoController>() {
        @Override
        public ProjetoController createFromParcel(Parcel in) {
            return new ProjetoController(in);
        }

        @Override
        public ProjetoController[] newArray(int size) {
            return new ProjetoController[size];
        }
    };
    //Atributos
    private Projeto projeto;
    //Taxa de retorno inicial para cálculo das iterações
    private double LOW_RATE = 0.01;
    //Taxa de retorno máxima adotada para o cálculo das iterações
    private double HIGH_RATE = 0.5;
    //Numero máximo de iterações
    private int MAX_ITERATION = 1000;
    //Precisão máxima para o cálculo da TIR
    private double PRECISION_REQ = 1E-10;

    //Construtor
    public ProjetoController() {
    }

    private ProjetoController(Parcel in) {
        projeto = in.readParcelable(Projeto.class.getClassLoader());
        LOW_RATE = in.readDouble();
        HIGH_RATE = in.readDouble();
        MAX_ITERATION = in.readInt();
        PRECISION_REQ = in.readDouble();
    }

    /**
     * Cria um novo projeto
     *
     * @param nome                 nome do projeto [String]
     * @param taxaDeReinvestimento valor da taxa de reinvestimento [double]
     * @param taxaDeFinanciamento  valor da taxa de financiamento [double]
     */
    public void criarProjeto(String nome, double taxaDeReinvestimento, double taxaDeFinanciamento) {
        projeto = new Projeto(nome, taxaDeReinvestimento, taxaDeFinanciamento);
    }

    public void setLowRate(double lowRate) {
        LOW_RATE = lowRate;
    }

    public void setHighRate(double highRate) {
        HIGH_RATE = highRate;
    }

    public void setMaxIterations(int maxIterations) {
        MAX_ITERATION = maxIterations;
    }

    public void setPrecisionRequirements(double prec) {
        PRECISION_REQ = prec;
    }

    /**
     * Atualiza o nome do projeto
     *
     * @param nome
     */
    public void atualizarNomeDoProjeto(String nome) {
        projeto.setNome(nome);
    }

    /**
     * Retorna o nome do projeto
     *
     * @return String contendo o nome do projeto
     */
    public String obterNomeDoProjeto() {
        return projeto.getNome();
    }

    public void definirTaxaDeReinvestimento(double taxa) {
        projeto.setTaxaDeReinvestimento(taxa);
    }

    /**
     * Retorna a taxa de reinvestimento do projeto
     *
     * @return taxa de reinvestimento
     */
    public double obterTaxaDeReinvestimento() {
        return projeto.getTaxaDeReinvestimento();
    }

    /**
     * Define a taxa de financiamento
     *
     * @param taxa
     */
    public void definirTaxaDeFinanciamento(double taxa) {
        projeto.setTaxaDeFinanciamento(taxa);
    }

    /**
     * Obtém a taxa de financiamento
     *
     * @return
     */
    public double obterTaxaDeFinancimento() {
        return projeto.getTaxaDeFinanciamento();
    }

    /**
     * Adiciona um valor de fluxo de caixa no fim da lista
     *
     * @param valor [double] valor do fluxo de caixa
     */
    public void adicionarFluxoDeCaixa(double valor) {
        int periodo = projeto.getFluxoDeCaixa().size();
        CashFlow c = new CashFlow(periodo, valor);
        projeto.adicionarFluxoDeCaixa(c);
    }

    /**
     * Adiciona um fluxo de caixa ao projeto
     *
     * @param valor
     */
    public void adicionarFluxoDeCaixa(int periodo, double valor) {

        CashFlow c = new CashFlow(periodo, valor);
        projeto.adicionarFluxoDeCaixa(c);
    }

    /**
     * Verifica se o projeto tem um fluxo de caixa
     *
     * @return true se possui e false se não possui
     */
    public boolean temFluxoDeCaixa() {
        return projeto.getFluxoDeCaixa().isEmpty() ? false : true;
    }

    /**
     * Método para validação do fluxo de caixa. As operações só podem ser executadas se existir
     * ao menos um valor positivo e um valor negativo
     *
     * @return true se existe ao menos um valor positivo e negativo e false se não.
     */
    public boolean fluxoDeCaixaValido() {

        boolean hasPositive = false;
        boolean hasNegative = false;

        for (CashFlow cashFlow : projeto.getFluxoDeCaixa()) {
            if (cashFlow.getCash() > 0) {
                hasPositive = true;
            }
            if (cashFlow.getCash() < 0) {
                hasNegative = true;
            }
        }

        if (hasPositive && hasNegative) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Recupera a lista de fluxo de faixa
     *
     * @return lista de fluxos de caixa
     */
    public ArrayList<CashFlow> recuperarListaDeFluxoDeCaixa() {
        return projeto.getFluxoDeCaixa();
    }

    /**
     * Atualiza o valor de um fluxo de caixa e retorna lista
     *
     * @param position [int] posicao (periodo de tempo) a ser atualizado
     * @param valor    [double] valor da posicao a ser alterado
     */
    public ArrayList<CashFlow> atualizarValor(int position, double valor) {

        CashFlow c = new CashFlow(position, valor);
        projeto.atualizarFluxoDeCaixa(position, c);

        return projeto.getFluxoDeCaixa();
    }

    /**
     * Esse
     *
     * @return
     */
    public double calcularVPL(double taxa) {

        //Valor que armazena a somatória dos fluxos de caixa no periodo zero
        double sum = 0;

        //Valor do investimento inicial
        double investiment = 0;

        //Loop para cálculo da somatória
        for (CashFlow flow : projeto.getFluxoDeCaixa()) {
            int period = flow.getPeriod();
            //Se o periodo for nulo, considera o primeiro fluxo de caixa como investimento
            if (period == 0) {
                investiment = flow.getCash();
            } else {
                double clashflow = flow.getCash();
                sum += clashflow / Math.pow((1 + taxa), period);
            }
        }

        sum += investiment;

        projeto.setVPL(sum);

        return projeto.getVPL();
    }

    /**
     * Método para cálculo do payback para um dado fluxo de caixa
     * *
     *
     * @return valor do fluxo de caixa [double]
     */
    public double calcularPayback() {

        double payback = 0.0;

        ArrayList<CashFlow> list = projeto.getFluxoDeCaixa();

        if (list.size() > 2) {
            //Indice da ArrayList
            int index = 0;
            //Periodo inicial e final onde o valor acumulado do fluxo de caixa se torna positivo
            int startPeriod = list.get(index).getPeriod();
            int midPeriod = 0;
            //Investimento inicial
            double investiment = Math.abs(list.get(startPeriod).getCash());
            //Valor que acumula a somatoria
            double sum = 0;
            double aux = 0;
            //realiza o loop até que a soma dos fluxos de caixa seja positiva
            while (sum < investiment) {
                index += 1;
                if (index > list.size()) return 0;
                aux += list.get(index).getCash();
                sum += aux;
                midPeriod = list.get(index).getPeriod();
            }
            //Convertendo valores inteiros para double
            payback = (investiment - aux) / (sum - aux) + (double) midPeriod;

            projeto.setPayback(payback);

            return projeto.getPayback();
        } else {
            return 0.0;
        }
    }

    /**
     * Método para calculo da taxa intena de retorno (TIR ou IRR)
     * <p>
     * Baseado no algoritmo em https://www.codeproject.com/Tips/461049/Internal-Rate-of-Return-IRR-Calculation
     *
     * @return valor da taxa interna de retorno [double]
     */
    public double calcularTIR() {

        //Taxa de retorno calculada
        double guessRate = LOW_RATE;
        //Taxas utilizadas para o cálculo das iterações
        double oldGuessRate = LOW_RATE;
        double newGuessRate = LOW_RATE;
        double lowGuessRate = LOW_RATE;
        double highGuessRate = HIGH_RATE;

        ArrayList<CashFlow> list = projeto.getFluxoDeCaixa();

        double m = 0.0;
        double oldValue = 0.0;
        double newValue = 0.0;

        double npv = 0;
        double denom = 0;

        int numOfFlows = list.size();

        for (int i = 0; i < MAX_ITERATION; i++) {
            npv = 0.0;
            for (int j = 0; j < numOfFlows; j++) {
                denom = Math.pow((1 + guessRate), j);
                CashFlow cashFlow = list.get(j);
                npv += cashFlow.getCash() / denom;
            }
            //Para o loop assim que a precisão é atingida
            if ((npv > 0) && (npv < PRECISION_REQ)) {
                break;
            }
            if (oldValue == 0.0) {
                oldValue = npv;
            } else {
                oldValue = newValue;
            }
            newValue = npv;
            if (i > 0) {
                if (oldValue < newValue) {
                    if ((oldValue < 0.0) && (newValue < 0.0)) {
                        highGuessRate = newGuessRate;
                    } else {
                        lowGuessRate = newGuessRate;
                    }
                } else {
                    if ((oldValue > 0.0) && (newValue < 0.0)) {
                        highGuessRate = newGuessRate;
                    } else {
                        lowGuessRate = newGuessRate;
                    }
                }
            } else {
                if ((oldValue > 0.0) && (newValue > 0.0)) {
                    lowGuessRate = newGuessRate;
                } else {
                    highGuessRate = newGuessRate;
                }
            }

            oldGuessRate = guessRate;
            guessRate = (lowGuessRate + highGuessRate) / 2;
            newGuessRate = guessRate;
        }

        projeto.setTIR(guessRate);

        return projeto.getTIR();
    }

    /**
     * Método para cálculo da tir modificada.
     * Ref.:https://www.sumproduct.com/thought/modified-internal-rate-of-return-revisited
     *
     * @param rrate valor da taxa de reinvestimento (double)
     * @param frate valor da taxa de financimento (double)
     * @return taxa interna de retorno modificada double
     */
    public double calcularTIRModificada(double rrate, double frate) throws NumberFormatException {

        //Cria duas arraylists, uma contendo apenas valores positivos e outra contendo apenas
        //valores negativos
        ArrayList<CashFlow> positiveCashFlowList = new ArrayList<>();
        ArrayList<CashFlow> negativeCashFlowList = new ArrayList<>();

        //Faz um loop no ArrayList<CashFlow> do projeto e separa para cada ArrayList valores
        //positivos ou negativos, respectivamente
        for (CashFlow cashFlow : projeto.getFluxoDeCaixa()) {
            //Instancia dois objetos: positiveCashFlow e negativeCashFlow, que armazenam os valores
            //para o fluxo de caixa positivo e negativo, respectivamente
            CashFlow positiveCashFlow = new CashFlow(projeto);
            CashFlow negativeCashFlow = new CashFlow(projeto);
            if (cashFlow.getCash() > 0) {
                positiveCashFlow = cashFlow;
                negativeCashFlow.setPeriod(cashFlow.getPeriod());
                negativeCashFlow.setCash(0.0);

            } else {
                negativeCashFlow = cashFlow;
                positiveCashFlow.setPeriod(cashFlow.getPeriod());
                positiveCashFlow.setCash(0.0);
            }
            positiveCashFlowList.add(positiveCashFlow);
            negativeCashFlowList.add(negativeCashFlow);
        }
        //Calculo da VPL para os fluxos de caixa positivo e negativo, respectivamente
        double positiveVPL = calcularVPL(rrate);
        double negativeVPL = calcularVPL(frate);

        int numberOfPeriods = projeto.getFluxoDeCaixa().size();
        double futureRate = Math.pow((1 + rrate), numberOfPeriods);
        double dividendo = -1 * positiveVPL * futureRate;
        double divisor = negativeVPL * (1 + frate);
        if (divisor != 0) {
            double factor = 1.0 / ((double) numberOfPeriods - 1.0);

            double tirm = Math.pow(dividendo / divisor, factor);

            projeto.setTIR(tirm);

            return projeto.getTIR();
        } else {
            throw new NumberFormatException();
        }

    }

    public double obterVPL() {
        return projeto.getTIR();
    }

    public double obterTIR() {
        return projeto.getTIR();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        projeto.writeToParcel(out, flags);
        out.writeDouble(LOW_RATE);
        out.writeDouble(HIGH_RATE);
        out.writeDouble(PRECISION_REQ);
        out.writeInt(MAX_ITERATION);
    }

    /**
     * Apaga todos os itens do fluxo de caixa
     */
    public void excluirFluxoDeCaixa() {
        projeto.apagarFluxoDeCaixa();
    }
}
