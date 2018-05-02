package com.assulfisoft.oramentodecapital;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.assulfisoft.oramentodecapital.adapter.CashFlowAdapter;
import com.assulfisoft.oramentodecapital.model.CashFlow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import static android.graphics.pdf.PdfDocument.Page;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String LOG_TAG = "preferences";
    //String para armazenar os dados que são exibidos
    private static final String PROJECT_NAME = "project name";
    private static final String VPL_RESULT = "vpl result";
    private static final String STATE_LIST = "State Adapter Data";
    private static final String TIR_RESULT = "tir result";
    private static final String PAYBACK_RESULT = "payback result";
    private static final String REINVESTIMENT_RATE_STRING = "rrate";
    private static final String FINANCE_RATE_STRING = "frate";
    //Taxa de retorno inicial para cálculo das iterações
    private static double LOW_RATE = 0.01;
    //Taxa de retorno máxima adotada para o cálculo das iterações
    private static double HIGH_RATE = 0.5;
    //Numero máximo de iterações
    private static int MAX_ITERATION = 1000;
    //Precisão máxima para o cálculo da TIR
    private static double PRECISION_REQ = 1E-10;
    //Taxa de reinvestimento
    private static double REINVESTIMENT_RATE = 0.08;
    //Taxa de financiamento
    private static double FINANCE_RATE = 0.12;
    //Método de cálculo da TIR. Se true calcula a TIRM caso contrário calcula a TIR
    private static boolean IRR_METHOD = false;
    //String que representa a moeda adotada
    private static String CURRENCY;
    //String que representa o periodo de tempo selecionado
    private static String PERIOD;
    //Atributos
    private TextView projectNameTextView;
    private TextView vplTextView;
    private TextView tirTextView;
    private TextView paybackTextView;
    private TextView rrateTextView;
    private TextView frateTextView;
    private ListView cashFlowListView;
    private FloatingActionButton FAB;
    private Button calculateButton;
    private Toast mToast;
    private ArrayList<CashFlow> cashFlowArrayList;
    private CashFlowAdapter cashFlowAdapter;
    //Variáveis para armazenar os resultados do cálculo de VPL e payback para que seja possível
    //atualizar as textviews com o cálculo
    private String mVPLResult;
    private String mPaybackResult;

    //Variáveis para a configuração da gaveta de navegacao
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private String[] mNavOptions;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Configuração da gaveta de navegacao
        setupNavigationDrawer(savedInstanceState);

        //Chamada do método para recuperar os valores da SharedPreferences
        setupSharedPreferences();

        //Recupera os elementos da tela
        projectNameTextView = (TextView) findViewById(R.id.tv_nome_projeto);
        vplTextView = (TextView) findViewById(R.id.tv_vpl);
        tirTextView = (TextView) findViewById(R.id.tv_tir);
        paybackTextView = (TextView) findViewById(R.id.tv_payback);
        rrateTextView = (TextView) findViewById(R.id.tv_rrate);
        frateTextView = (TextView) findViewById(R.id.tv_frate);
        FAB = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        calculateButton = (Button) findViewById(R.id.btn_calcular);

        //Se o Bundle savedInstanceState for null, recupera os resultados pré-calculados das textView e do Adapter
        if (savedInstanceState != null){
            String projectName = savedInstanceState.getString(PROJECT_NAME);
            String vplResult = savedInstanceState.getString(VPL_RESULT);
            String tirResult = savedInstanceState.getString(TIR_RESULT);

            projectNameTextView.setText(projectName);
            vplTextView.setText(vplResult);
            tirTextView.setText(tirResult);

            cashFlowArrayList = savedInstanceState.getParcelableArrayList(STATE_LIST);

            REINVESTIMENT_RATE = savedInstanceState.getDouble(REINVESTIMENT_RATE_STRING);
            setRRateTVString(REINVESTIMENT_RATE);

            FINANCE_RATE = savedInstanceState.getDouble(FINANCE_RATE_STRING);
            setFRateTVString(FINANCE_RATE);
            //cashFlowAdapter.notifyDataSetChanged();
        } else {
            //Cria o ArrayList
            //Cria um ArrayList inicial com valores default
            cashFlowArrayList = new ArrayList<CashFlow>();

            //Adicionando alguns valores adicionais
            cashFlowArrayList.add(new CashFlow(0,-42000));
            cashFlowArrayList.add(new CashFlow(1,16000));
            cashFlowArrayList.add(new CashFlow(2,16000));
            cashFlowArrayList.add(new CashFlow(3,16000));
            cashFlowArrayList.add(new CashFlow(4,16000));
            cashFlowArrayList.add(new CashFlow(5,16000));

            setRRateTVString(REINVESTIMENT_RATE);

            setFRateTVString(FINANCE_RATE);
        }

        /*
            Cria um evento de onclick no @link projectNameTextView que exibe um alertdialog para o
            usuario informar um nome para o projeto
         */
        projectNameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.project_dialog_title);
                builder.setMessage(R.string.project_dialog_message);

                final EditText input = new EditText(MainActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);

                builder.setView(input);

                builder.setPositiveButton(R.string.project_dialog_positive_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        String mProjectName = input.getText().toString();
                        if (!mProjectName.isEmpty()){
                            projectNameTextView.setText(mProjectName);
                        } else {
                            String toastMessage = getResources()
                                    .getString(R.string.invalid_project_name_toast);
                            showToast(toastMessage);
                        }
                    }
                });

                builder.setNegativeButton(R.string.project_dialog_negative_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if (dialogInterface != null){
                            dialogInterface.dismiss();
                        }
                    }
                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });

        //Cria um evento de clique no @rrateTextView para que o usuário possa informar um valor para
        //a taxa de reinvestimento
        rrateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showRRateAlertDialog();
            }
        });

        //Cria um evento de clique no @frateTextView para que o usuário possa informar um valor para
        //a taxa de financiamento
        frateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showFRateAlertDialog();
            }
        });

        /*Adiciona um evento de onclick ao FAB para exibir o AlertDialog para adicionar o fluxo
         * o fluxo de caixa
         */
        FAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddCashFlowAlertDialog();
            }
        });

        //Cria o adapter
        cashFlowAdapter = new CashFlowAdapter(MainActivity.this,cashFlowArrayList);

        //Recupera a listview
        cashFlowListView = (ListView) findViewById(R.id.lv_cashflow);

        //Carrega a Empty ListView
        View emptyView = findViewById(R.id.ll_empty_view);
        cashFlowListView.setEmptyView(emptyView);

        //Insere o adapter na listView
        cashFlowListView.setAdapter(cashFlowAdapter);

        //Adiciona um evento de onClick em um item da listView para editar um valor de fluxo de caixa
        cashFlowListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Chamada do método para exibir um AlertDialog para editar um valor do fluxo de caixa
                showEditCashFlowAlertDialog(position);
            }
        });

        //Adicona um evento de onClick ao botão calcular
        calculateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //complete: implementar o método de cálculo da TIRM
                boolean validCashFlow = validateCashFlow(cashFlowArrayList);

                //valida se a listView não está vazia
                if (!cashFlowArrayList.isEmpty() && validCashFlow){
                    //Chamada do método para o cálculo do VPL
                    //Recupera a taxa de rendimento padrão
                    double rate = getRates(rrateTextView.getText().toString());
                    //Converte o valor para String e formata com 2 casas decimais
                    mVPLResult = String.format("%.2f",calculateVLP(cashFlowArrayList,rate));
                    //Retorna o valor para a vplTextView
                    vplTextView.setText(getResources().getString(R.string.tv_vpl_result) + " " + CURRENCY +
                            " " + mVPLResult);
                    //Chamada do método para cálculo do Payback
                    mPaybackResult = String.format("%.2f",calculatePayback(cashFlowArrayList));
                    paybackTextView.setText(getResources().getString(R.string.tv_payback_result)
                            + " " + mPaybackResult + " " + PERIOD);
                    //Chamada do método para cálculo da TIR ou TIRM
                    //Recupera o valor da preferencia
                    if (IRR_METHOD == false) {
                        //Cálculo da TIR
                        String tirString = String.format("%.2f", calculateIRR(cashFlowArrayList) * 100);
                        tirTextView.setText(getString(R.string.tv_tir_result_start) + " "
                                + tirString + " " + getString(R.string.tv_tir_result_end));
                    } else {
                        //Cálculo da TIRM
                        double rrate = getRates(rrateTextView.getText().toString());
                        double frate = getRates(frateTextView.getText().toString());
                        String tirmString = String.format("%.2f", calculateModifiedTIR(cashFlowArrayList, rrate, frate) * 100);
                        tirTextView.setText(getString(R.string.tv_tirm_result_start) + " " +
                                tirmString + " " + getString(R.string.tv_tirm_result_end));
                    }
                } else {
                    String toastMessage = getResources().getString(R.string.empty_arraylist_toast);
                        showToast(toastMessage);
                }
            }
        });
    }

    /**
     * Método para configurar a gaveta de navegação
     */
    private void setupNavigationDrawer(Bundle savedInstanceState) {

        //Recupera o StringArray com as opções
        mNavOptions = getResources().getStringArray(R.array.navigation_drawer_options);

        mTitle = mDrawerTitle = getTitle();

        //Recupera os componentes da tela
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        //Define o adapter para a ListView
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item,
                mNavOptions) {


            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView tv = (TextView) view;

                if (position == mNavOptions.length - 1) {
                    tv.setTextColor(Color.GRAY);
                }

                return view;
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @Override
            public boolean isEnabled(int position) {
                //Desabilita o ultimo item - Sobre
                if (position == mNavOptions.length - 1) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout,
                R.drawable.ic_drawer_white,
                R.string.drawer_open,
                R.string.drawer_close){
            @Override
            public void onDrawerClosed(View view){
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer_toogle_white);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Exibe um alertDialog para que o usuario insira um valor para a taxa de financiamento
     */
    private void showFRateAlertDialog() {

        //Cria um objeto AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        //Configura o builder
        builder.setTitle(R.string.show_add_fr_alert_dialog_title);
        builder.setMessage(R.string.show_add_fr_alert_dialog_message);
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER| InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton(R.string.project_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Converte o valor do input para Double
                String inputValue = input.getText().toString();
                if (!inputValue.isEmpty()){
                    try {
                        double frate = Double.parseDouble(inputValue);
                        FINANCE_RATE = frate;
                        setFRateTVString(FINANCE_RATE);
                    } catch (NumberFormatException nfe){
                        showToast(getString(R.string.invalid_type));
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if (dialog != null){
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Exibe um alertDialog para que o usuario insira um valor para a taxa de reinvestimento
     */
    private void showRRateAlertDialog() {

        //Cria um objeto AlertDialog.Builder
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        //Configura o builder
        builder.setTitle(R.string.show_add_rr_alert_dialog_title);
        builder.setMessage(R.string.show_add_rr_alert_dialog_message);
        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton(R.string.project_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //Converte o valor da EditText input para Double
                String inputValue = input.getText().toString();
                if(!inputValue.isEmpty()){
                    try {
                        double rrate = Double.parseDouble(inputValue);
                        REINVESTIMENT_RATE = rrate;
                        setRRateTVString(REINVESTIMENT_RATE);
                    } catch (NumberFormatException nfe){
                        showToast(getString(R.string.invalid_type));
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Método que recupera as informações da sharedPreferences
     */
    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        //Taxa de retorno inicial para cálculo das iterações
        LOW_RATE = Double.parseDouble(sharedPreferences.getString(getString(R.string.low_rate_key)
                ,getString(R.string.low_rate_default_value)));
        //Taxa de retorno máxima adotada para o cálculo das iterações
        HIGH_RATE = Double.parseDouble(sharedPreferences.getString(getString(R.string.high_rate_key)
                ,getString(R.string.high_rate_default_value)));
        //Numero máximo de iterações
        MAX_ITERATION = Integer.parseInt(sharedPreferences.getString(getString(R.string.max_iterations_key)
                ,getString(R.string.max_iterations_default_value)));
        //Precisão máxima para o cálculo da TIR
        PRECISION_REQ = Double.parseDouble(sharedPreferences.getString(getString(R.string.req_precision_key)
                ,getString(R.string.reg_precision_default_value)));
        //complete: recuperar o valor booleano para opção de cálculo da TIR modificada
        IRR_METHOD = sharedPreferences.getBoolean(getString(R.string.calculate_tirm_key),
                getResources().getBoolean(R.bool.calculate_tirm));
        //Unidade correspondente a moeda
        CURRENCY = sharedPreferences.getString(getString(R.string.pref_currency_key)
                ,getString(R.string.pref_currency_default_value));
        //Unidade de tempo
        PERIOD = sharedPreferences.getString(getString(R.string.pref_period_key),
            getString(R.string.periodo_value));

        //Registra o listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Método para validação do fluxo de caixa. As operações só podem ser executadas se existir
     * ao menos um valor positivo e um valor negativo
     *
     * @param list ArrayList<CashFlow> contendo os dados do fluxo de caixa
     * @return boolean
     */
    private boolean validateCashFlow(ArrayList<CashFlow> list) {

        boolean hasPositive = false;
        boolean hasNegative = false;

        for(CashFlow cashFlow : list){
            if (cashFlow.getCash() > 0){
                hasPositive = true;
            }
            if (cashFlow.getCash() < 0){
                hasNegative = true;
            }
        }

        if (hasPositive && hasNegative){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Método para cálculo do payback para um dado fluxo de caixa
     * @param list ArrayList<CashFlow> contendo o fluxo de caixa
     * @return payback double
     */
    private double calculatePayback(ArrayList<CashFlow> list) {

        //Variável que armazena o payback
        double payback;

        if (list.size() > 2){
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
            while (sum < investiment){
                index += 1;
                aux += list.get(index).getCash();
                sum += aux;
                midPeriod = list.get(index).getPeriod();
            }
            //Convertendo valores inteiros para double
            payback = (investiment - aux) / (sum - aux) + (double)midPeriod;
            return payback;
        } else {
            showToast(getString(R.string.invalid_cash_flow_arraylist_size));
            return 0.0;
        }

    }

    /**
     * Método para cálculo do VPL
     * @param list ArrayList<CashFlow> ArrayList contendo os dados do fluxo de caixa
     */
    private double calculateVLP(ArrayList<CashFlow> list, double rate) {

        //Valor que armazena a somatória dos fluxos de caixa no periodo zero
        double sum = 0;

        //Valor do investimento inicial
        double investiment = 0;

        //Loop para cálculo da somatória
        for (CashFlow flow : list){
            int period = flow.getPeriod();
            //Se o periodo for nulo, considera o primeiro fluxo de caixa como investimento
            if (period == 0){
                investiment = flow.getCash();
            } else {
                double clashflow = flow.getCash();
                sum += clashflow / Math.pow((1 + rate), period);
            }
        }

        sum += investiment;

        return sum;
    }

    /**
     * Método para calculo da taxa intena de retorno (TIR ou IRR)
     *
     * Baseado no algoritmo em https://www.codeproject.com/Tips/461049/Internal-Rate-of-Return-IRR-Calculation
     *
     * @param list ArrayList<CashFlow>
     * @return IRR double
     */
    private double calculateIRR(ArrayList<CashFlow> list){

       //Taxa de retorno calculada
        double guessRate = LOW_RATE;
        //Taxas utilizadas para o cálculo das iterações
        double oldGuessRate = LOW_RATE;
        double newGuessRate = LOW_RATE;
        double lowGuessRate = LOW_RATE;
        double highGuessRate = HIGH_RATE;

        double m = 0.0;
        double oldValue = 0.0;
        double newValue = 0.0;

        double npv = 0;
        double denom = 0;

        int numOfFlows = list.size();

        for(int i = 0; i < MAX_ITERATION; i++){
            npv = 0.0;
            for (int j = 0;j < numOfFlows; j++){
                denom = Math.pow((1 + guessRate), j);
                CashFlow cashFlow = list.get(j);
                npv += cashFlow.getCash() / denom;
            }
            //Para o loop assim que a precisão é atingida
            if ((npv > 0) && (npv < PRECISION_REQ)){
                break;
            }
            if (oldValue == 0.0){
                oldValue = npv;
            } else {
                oldValue = newValue;
            }
            newValue = npv;
            if (i > 0){
                if (oldValue < newValue){
                    if ((oldValue < 0.0) && (newValue < 0.0)){
                       highGuessRate = newGuessRate;
                    } else {
                        lowGuessRate = newGuessRate;
                    }
                } else {
                    if ((oldValue > 0.0) && (newValue < 0.0)){
                        highGuessRate = newGuessRate;
                    } else {
                        lowGuessRate = newGuessRate;
                    }
                }
                } else {
                    if ((oldValue > 0.0) && (newValue > 0.0)){
                        lowGuessRate = newGuessRate;
                    } else {
                        highGuessRate = newGuessRate;
                }
            }

            oldGuessRate = guessRate;
            guessRate = (lowGuessRate + highGuessRate) / 2;
            newGuessRate = guessRate;
        }

        return guessRate;
    }

    /**
     * Método para cálculo da tir modificada.
     * Ref.:https://www.sumproduct.com/thought/modified-internal-rate-of-return-revisited
     *
     * @param list Arraylist de CashFlow contendo o fluxo de caixa real do projeto
     * @param rrate valor da taxa de reinvestimento (double)
     * @param frate valor da taxa de financimento (double)
     *
     * @return taxa interna de retorno modificada double
     */
    private double calculateModifiedTIR(ArrayList<CashFlow> list, double rrate, double frate) {

        //Cria duas arraylists, uma contendo apenas valores positivos e outra contendo apenas
        //valores negativos
        ArrayList<CashFlow> positiveCashFlowList = new ArrayList<>();
        ArrayList<CashFlow> negativeCashFlowList = new ArrayList<>();

        //Faz um loop no ArrayList<CashFlow> do projeto e separa para cada ArrayList valores
        //positivos ou negativos, respectivamente
        for(CashFlow cashFlow : list){
            //Instancia dois objetos: positiveCashFlow e negativeCashFlow, que armazenam os valores
            //para o fluxo de caixa positivo e negativo, respectivamente
            CashFlow positiveCashFlow = new CashFlow();
            CashFlow negativeCashFlow = new CashFlow();
            if (cashFlow.getCash() > 0){
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
         try {
             //Calculo da VPL para os fluxos de caixa positivo e negativo, respectivamente
             double positiveVPL = calculateVLP(positiveCashFlowList, rrate);
             double negativeVPL = calculateVLP(negativeCashFlowList, frate);

             int numberOfPeriods = list.size();
             double futureRate = Math.pow((1 + rrate), numberOfPeriods);
             double dividendo = -1 * positiveVPL * futureRate;
             double divisor = negativeVPL * (1 + frate);
             double factor = 1.0 / ((double) numberOfPeriods - 1.0);

             double tirm = Math.pow(dividendo / divisor, factor);
             return tirm;
         } catch (NumberFormatException nfe){
            showToast(getString(R.string.error_calculate_tirm));
            return  0.0;
         }

    }

    /**
     * Método para inflar o menu
     * @param menu Menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //Se a gaveta de navegação está aberta esconde os itens relacionados com o conteúdo da view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        menu.findItem(R.id.action_delete_cash_flow).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //A ação home/up da action bar deve abrir ou fechar a gaveta de nagegação
        //ActionBarDrawerToggle é responável por essa ação
        if (mDrawerToggle.onOptionsItemSelected(item)){
            return true;
        }
        //Ações para as demais opções
        switch (item.getItemId()){
            case R.id.action_delete_cash_flow:
                showDeleteItemFromListViewAlert();
                return true;
            case R.id.action_generate_pdf_report:
                showToast(getString(R.string.generate_report_option));
                //createPdfReport();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Cria um relatório PDF do projeto atual
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void createPdfReport() {

        try {
            //Cria uma isntancia do objeto PdfDocument
            PdfDocument document = new PdfDocument();

            //Cria um página
            PageInfo pageInfo = new PageInfo.Builder(210, 291, 1).create();

            //Inicia a página
            Page page = document.startPage(pageInfo);

            Canvas canvas = page.getCanvas();
            Paint projectTitle = new Paint();
            projectTitle.setColor(getResources().getColor(R.color.textColorBlue));
            canvas.drawText(projectNameTextView.getText().toString(), 50.0f, 50.0f, projectTitle);

            Paint subTitle = new Paint();
            subTitle.setColor(getResources().getColor(R.color.colorBlack));
            String cashFlowSubtitle = getString(R.string.cash_flow_subtitle_report);
            canvas.drawText(cashFlowSubtitle, 50.0f, 75.0f, subTitle);

            Paint text = new Paint();
            text.setColor(getResources().getColor(R.color.colorBlack));

            int listIndex = 0;
            String periodColumn = getString(R.string.cash_flow_period_column);
            canvas.drawText(periodColumn, 50.0f, 100.0f, text);
            String valueColumn = getString(R.string.cash_flow_value_column);
            canvas.drawText(valueColumn, 125.0f, 100.0f, text);
            for (CashFlow cashFlow : cashFlowArrayList) {
                listIndex++;
                String periodValue = String.valueOf(cashFlow.getPeriod());
                canvas.drawText(periodValue, 50.0f, 105.0f * ((float) listIndex), text);
                String cashFlowValue = String.format("%.2f", cashFlow.getCash());
                cashFlowValue = CURRENCY + " " + cashFlowValue;
                canvas.drawText(cashFlowValue, 125.0f, 105.0f * ((float) listIndex), text);
            }

            String indicatorsTitle = getString(R.string.title_indicators);
            float indicatorsPosition = 105.0f * ((float) listIndex) + 20.0f;
            canvas.drawText(indicatorsTitle, 50.0f, indicatorsPosition, subTitle);

            String vplText = getString(R.string.vpl_text);
            canvas.drawText(vplText, 50.0f, indicatorsPosition + 20.0f, text);
            canvas.drawText(vplTextView.getText().toString(), 150.0f, indicatorsPosition + 20.0f,
                    text);

            String tirText = getString(R.string.tir_text);
            canvas.drawText(tirText, 50.0f, indicatorsPosition + 40.0f, text);
            canvas.drawText(tirTextView.getText().toString(), 150.0f, indicatorsPosition + 40.0f,
                    text);

            String paybackText = getString(R.string.payback_text);
            canvas.drawText(paybackText, 50.0f, indicatorsPosition + 60.0f, text);
            canvas.drawText(paybackTextView.getText().toString(), 150.0f, indicatorsPosition + 60.0f,
                    text);

            document.finishPage(page);

            //Define o nome do arquivo
            //O nome será composto pelo nome do projeto e uma data e hora em que o arquivo foi gerado
            //exemplo: PROJETO A-20180218_153945

            //Recupera a data atual
            String currentTime = new SimpleDateFormat("yyyyMMdd_hhmmss")
                    .format(Calendar.getInstance().getTime());
            String targetPdf = projectNameTextView.getText().toString() + "-" + currentTime;

            String filename = "/" + targetPdf + ".pdf";

            File file = new File(getApplicationContext().getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            showToast(getString(R.string.generate_report_success));
            //Abre o arquivo pdf
            openPdfFile(file);
        } catch (IOException ex) {
            ex.printStackTrace();
            showToast(getString(R.string.generate_report_fail));
        }
    }

    /**
     * Esse método abre o arquivo pdf
     *
     * @param file arquivo pdf
     */
    private void openPdfFile(File file) {
        Intent readerIntent = new Intent(Intent.ACTION_VIEW);
        readerIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
        readerIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        Intent chooserIntent = Intent.createChooser(readerIntent,
                getString(R.string.open_report_title));

        try {
            startActivity(chooserIntent);
        } catch (ActivityNotFoundException ex) {
            ex.printStackTrace();
            showToast(getString(R.string.pdf_reader_invalid));
        }
    }

    /**
     * Método que exibe um alertDialog para que o usuário confirme se a lista será deletada ou não
     */
    private void showDeleteItemFromListViewAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.delete_cash_flow_title_alert);
        builder.setMessage(R.string.delete_cash_flow_message_alert);

        builder.setPositiveButton(R.string.delete_cash_flow_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                clearCashFlowListView();
                dialogInterface.dismiss();
            }
        });

        builder.setNegativeButton(R.string.delete_cash_flow_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Método para limpar a listView
     */
    private void clearCashFlowListView(){

            cashFlowArrayList.clear();
            cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo para exibir um alertDialog para inserir um novo fluxo de caixa
     */
    private void showAddCashFlowAlertDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.project_dialog_title);
        builder.setMessage(R.string.add_cash_flow_message_alert);

        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
        InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);

        builder.setPositiveButton(R.string.project_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Converte o valor da EditText input para Double
                String inputValue = input.getText().toString();
                if (!inputValue.isEmpty()) {
                    try {
                        double cash = Double.parseDouble(inputValue);
                        addChashFlow(cash);
                    } catch (NumberFormatException nfe){
                        showToast(getString(R.string.invalid_type));
                    }
                }
            }
        });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     *  Método para adicionar um fluxo de caixa a ArrayList e atualizar o adapter
      * @param cash Double fluxo de caixa no período considerado
     */
    private void addChashFlow(Double cash){

        //Calcula o ultimo periodo adicionado
        int period = cashFlowArrayList.size();

        //Adiciona o valor a ArrayList
        cashFlowArrayList.add(new CashFlow(period,cash));

        //Atualiza o Adapter
        cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo para exibir um alertDialog para editar um novo fluxo de caixa
     * @param index int indice da lista
     */
    private void showEditCashFlowAlertDialog(final int index){

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle(R.string.project_dialog_title);
        builder.setMessage(R.string.add_cash_flow_message_alert);

        final EditText input = new EditText(MainActivity.this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL |
                InputType.TYPE_NUMBER_FLAG_SIGNED);
        builder.setView(input);

        builder.setPositiveButton(R.string.project_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Converte o valor da EditText input para Double
                String inputValue = input.getText().toString();
                if (!inputValue.isEmpty()) {
                    double cash = Double.parseDouble(inputValue);
                    editCashFlow(index,cash);
                }
            }
        });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null){
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Edita o valor do fluxo de caixa ao clicar em um indice da lista
     * @param index int indice clicado da lista
     * @param cash double valor do fluxo de caixa
     */
    private void editCashFlow(int index, double cash) {

        //Recupera o objeto a ser editado
        CashFlow cashFlow = cashFlowArrayList.get(index);
        //Atualiza o valor do fluxo de caixa
        cashFlow.setCash(cash);
        //retorna o valor ao arrayList
        cashFlowArrayList.set(index,cashFlow);
        //Atualiza a visualizacao da listView
        cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Método para exibir um toast personalizado
     * @param message
     */
    private void showToast(String message){
        if (mToast != null){
            mToast.cancel();
        }
        mToast = Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT);
        mToast.show();
    }

    /**
     * Método que recupera o valor das caixas de texto com informações sobre a taxa de retorno
     * e transforma em um número que possibilita o cálculo
     *
     * @param rateString String contendo a taxa de retorno
     * @return taxa de retorno para cálculo
     */
    private double getRates(String rateString) {
        int stringRateLength = rateString.length();
        rateString = rateString.substring(5, stringRateLength - 1);
        double rate = Double.parseDouble(rateString);
        return rate / 100;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        String projectName = projectNameTextView.getText().toString();
        outState.putString(PROJECT_NAME,projectName);

        String vplResult = vplTextView.getText().toString();
        outState.putString(VPL_RESULT,vplResult);

        String tirResult = tirTextView.getText().toString();
        outState.putString(TIR_RESULT,tirResult);

        String paybackResult = paybackTextView.getText().toString();
        outState.putString(PAYBACK_RESULT,paybackResult);

        ArrayList<CashFlow> list = cashFlowArrayList;
        outState.putParcelableArrayList(STATE_LIST, list);
        super.onSaveInstanceState(outState);

        outState.putDouble(REINVESTIMENT_RATE_STRING,REINVESTIMENT_RATE);

        outState.putDouble(FINANCE_RATE_STRING, FINANCE_RATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.low_rate_key))){
            //Taxa de retorno inicial para cálculo das iterações
            LOW_RATE = Double.parseDouble(sharedPreferences.getString(getString(R.string.low_rate_key)
                    ,getString(R.string.low_rate_default_value)));
            Log.i(LOG_TAG,key + LOW_RATE);
        } else if(key.equals(getString(R.string.high_rate_key))){
            HIGH_RATE = Double.parseDouble(sharedPreferences.getString(getString(R.string.high_rate_key)
                    ,getString(R.string.high_rate_default_value)));
            Log.i(LOG_TAG,key + HIGH_RATE);
        } else if(key.equals(getString(R.string.max_iterations_key))){
            MAX_ITERATION = Integer.parseInt(sharedPreferences.getString(getString(R.string.max_iterations_key)
                    ,getString(R.string.max_iterations_default_value)));
            Log.i(LOG_TAG,key + MAX_ITERATION);
        } else if(key.equals(getString(R.string.req_precision_key))){
            //Precisão máxima para o cálculo da TIR
            PRECISION_REQ = Double.parseDouble(sharedPreferences.getString(getString(R.string.req_precision_key)
                    ,getString(R.string.reg_precision_default_value)));
            //Log.i(LOG_TAG, key + PRECISION_REQ);
        } else if (key.equals(getString(R.string.calculate_tirm_key))){
            IRR_METHOD = sharedPreferences.getBoolean(getString(R.string.calculate_tirm_key),
                    getResources().getBoolean(R.bool.calculate_tirm));
            Log.i(LOG_TAG, key + IRR_METHOD);
        } else if(key.equals(getString(R.string.pref_currency_key))){
            CURRENCY = sharedPreferences.getString(getString(R.string.pref_currency_key),
                    getString(R.string.pref_currency_default_value));
            //Atualiza a TextView com o resultado do cálculo do VPL
            vplTextView.setText(getResources().getString(R.string.tv_vpl_result) + " " + CURRENCY +
                    " " + mVPLResult);
            //Informa o adapter da alteração
            cashFlowAdapter.notifyDataSetChanged();
        } else if(key.equals(getString(R.string.pref_period_key))){
            PERIOD = sharedPreferences.getString(getString(R.string.pref_period_key),
                    getString(R.string.pref_period_month_value));
            //Atualiza a TextView com o resultado do cálculo do Payback
            paybackTextView.setText(getResources().getString(R.string.tv_payback_result)
                    + " " + mPaybackResult + " " + PERIOD);
        }
    }

    /**
     * Esse método exibe o valor da taxa de reinvestimento na rrateTextView
     * @param rrate taxa de reinvestimento Double
     */
    private void setRRateTVString(double rrate){
        //Cria a string rrateString com o valor definido nos atributos
        String rrateString = getString(R.string.tv_rrate_start) + " " + rrate * 100 +
                " " + getString(R.string.tv_rrate_end);
        rrateTextView.setText(rrateString);
    }

    /**
     * Esse método exibe o valor da taxa de financiamento na frateTextView
     * @param frate taxa de financiamento Double
     */
    private void setFRateTVString(double frate){
        //Cria a string frateString com o valor definido nos atributos
        String frateString = getString(R.string.tv_frate_start) + " " + frate * 100 +
                " " + getString(R.string.tv_frate_end);
        frateTextView.setText(frateString);
    }

    /**
     * Define a ação para cada item, dependendo a posicao
     * @param postiton posição da ListView clicada
     */
    private void selectItem(int postiton) {
        switch (postiton){
            case 0:
                Intent intent = new Intent(this, GlossaryActivity.class);
                startActivity(intent);
                break;
            case 1:
                Intent preferencesItent = new Intent(this, SettingsActivity.class);
                startActivity(preferencesItent);
                break;
            default:
                return;
        }

        //Atualiza o item selecionado e fecha a gaveta de navegação
        mDrawerList.setItemChecked(postiton, true);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    /**
     * Classe que define a ação de clique em um item do menu de navegação
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int postiton, long id) {
            selectItem(postiton);
        }
    }
}
