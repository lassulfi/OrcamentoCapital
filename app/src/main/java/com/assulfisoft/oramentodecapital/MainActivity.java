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
import com.assulfisoft.oramentodecapital.controller.ProjetoController;
import com.assulfisoft.oramentodecapital.model.CashFlow;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.graphics.pdf.PdfDocument.Page;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private final static String LOG_TAG = "preferences";
    //String para armazenar os dados que são exibidos
    private static final String PROJECT_STATE = "Project State Data";

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
    private CashFlowAdapter cashFlowAdapter;
    //Variáveis para armazenar os resultados do cálculo de VPL e payback para que seja possível
    //atualizar as textviews com o cálculo
    private String mVPLResult;

    private ProjetoController controller;

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
        if (savedInstanceState != null) {
            controller = savedInstanceState.getParcelable(PROJECT_STATE);

        } else {
            //Cria um novo projeto
            controller = new ProjetoController();
            controller.criarProjeto("Projeto Incrivel", REINVESTIMENT_RATE, FINANCE_RATE);

            //Cria um ArrayList inicial com valores default
            controller.adicionarFluxoDeCaixa(0, -42000);
            controller.adicionarFluxoDeCaixa(1, 16000);
            controller.adicionarFluxoDeCaixa(2, 16000);
            controller.adicionarFluxoDeCaixa(3, 16000);
            controller.adicionarFluxoDeCaixa(4, 16000);
            controller.adicionarFluxoDeCaixa(5, 16000);
        }

        //Chamada do método para recuperar os valores da SharedPreferences
        setupSharedPreferences();

        //Preenche todas as TextView
        projectNameTextView.setText(controller.obterNomeDoProjeto());
        vplTextView.setText(getResources().getString(R.string.tv_vpl_result) + " " + CURRENCY +
                " " + String.valueOf(controller.obterVPL()));
        tirTextView.setText(getString(R.string.tv_tir_result_start) + " "
                + String.valueOf(controller.obterTIR()) + " "
                + getString(R.string.tv_tir_result_end));

        setRRateTVString(controller.obterTaxaDeReinvestimento());

        setFRateTVString(controller.obterTaxaDeFinancimento());

        setPaybackTextView();

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
                                if (!mProjectName.isEmpty()) {
                                    controller.atualizarNomeDoProjeto(mProjectName);
                                    projectNameTextView.setText(controller.obterNomeDoProjeto());
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

                                if (dialogInterface != null) {
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
        cashFlowAdapter = new CashFlowAdapter(MainActivity.this,
                controller.recuperarListaDeFluxoDeCaixa());

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

                //valida se a listView não está vazia
                if (controller.temFluxoDeCaixa() && controller.fluxoDeCaixaValido()) {
                    //Chamada do método para o cálculo do VPL
                    //Recupera a taxa de rendimento padrão
                    double rate = getRates(rrateTextView.getText().toString());
                    //Converte o valor para String e formata com 2 casas decimais
                    mVPLResult = String.format("%.2f", controller.calcularVPL(rate));
                    //Retorna o valor para a vplTextView
                    vplTextView.setText(getResources().getString(R.string.tv_vpl_result) + " " + CURRENCY +
                            " " + mVPLResult);
                    //Chamada do método para cálculo do Payback
                    if (controller.calcularPayback() == 0) {
                        showToast(getString(R.string.cashflow_invalid_payback));
                    }
                    setPaybackTextView();
                    //Chamada do método para cálculo da TIR ou TIRM
                    //Recupera o valor da preferencia
                    if (IRR_METHOD == false) {
                        //Cálculo da TIR
                        String tirString = String.format("%.2f", controller.calcularTIR() * 100);
                        tirTextView.setText(getString(R.string.tv_tir_result_start) + " "
                                + tirString + " " + getString(R.string.tv_tir_result_end));
                    } else {
                        //Cálculo da TIRM
                        double rrate = getRates(rrateTextView.getText().toString());
                        double frate = getRates(frateTextView.getText().toString());
                        try {
                            String tirmString = String.format("%.2f", controller.calcularTIRModificada(rrate, frate) * 100);
                            tirTextView.setText(getString(R.string.tv_tirm_result_start) + " " +
                                    tirmString + " " + getString(R.string.tv_tirm_result_end));
                        } catch (NumberFormatException nfe) {
                            showToast(getString(R.string.error_calculate_tirm));
                        }

                    }
                } else {
                    String toastMessage = getResources().getString(R.string.empty_arraylist_toast);
                    showToast(toastMessage);
                }
            }
        });
    }

    private void setPaybackTextView() {
        String result = String.format("%.2f", controller.calcularPayback());
        paybackTextView.setText(getResources().getString(R.string.tv_payback_result)
                + " " + result + " " + PERIOD);
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
                R.string.drawer_close) {
            @Override
            public void onDrawerClosed(View view) {
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
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        builder.setView(input);

        builder.setPositiveButton(R.string.project_dialog_positive_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //Converte o valor do input para Double
                        String inputValue = input.getText().toString();
                        if (!inputValue.isEmpty()) {
                            try {
                                double frate = Double.parseDouble(inputValue);
                                controller.definirTaxaDeFinanciamento(frate);
                                setFRateTVString(controller.obterTaxaDeFinancimento());
                            } catch (NumberFormatException nfe) {
                                showToast(getString(R.string.invalid_type));
                            }
                        }
                    }
                });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (dialog != null) {
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
                        if (!inputValue.isEmpty()) {
                            try {
                                double rrate = Double.parseDouble(inputValue);
                                controller.definirTaxaDeReinvestimento(rrate);
                                setRRateTVString(controller.obterTaxaDeReinvestimento());
                            } catch (NumberFormatException nfe) {
                                showToast(getString(R.string.invalid_type));
                            }
                        }
                    }
                });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
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
        controller.setLowRate(Double.parseDouble(sharedPreferences
                .getString(getString(R.string.low_rate_key)
                        , getString(R.string.low_rate_default_value))));
        //Taxa de retorno máxima adotada para o cálculo das iterações
        controller.setHighRate(Double.parseDouble(sharedPreferences
                .getString(getString(R.string.high_rate_key)
                        , getString(R.string.high_rate_default_value))));
        //Numero máximo de iterações
        controller.setMaxIterations(Integer.parseInt(sharedPreferences
                .getString(getString(R.string.max_iterations_key)
                        , getString(R.string.max_iterations_default_value))));
        //Precisão máxima para o cálculo da TIR
        controller.setPrecisionRequirements(Double.parseDouble(sharedPreferences
                .getString(getString(R.string.req_precision_key)
                        , getString(R.string.reg_precision_default_value))));
        //complete: recuperar o valor booleano para opção de cálculo da TIR modificada
        IRR_METHOD = sharedPreferences.getBoolean(getString(R.string.calculate_tirm_key),
                getResources().getBoolean(R.bool.calculate_tirm));
        //Unidade correspondente a moeda
        CURRENCY = sharedPreferences.getString(getString(R.string.pref_currency_key)
                , getString(R.string.pref_currency_default_value));
        //Unidade de tempo
        PERIOD = sharedPreferences.getString(getString(R.string.pref_period_key),
                getString(R.string.periodo_value));

        //Registra o listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Método para inflar o menu
     *
     * @param menu Menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
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
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        //Ações para as demais opções
        switch (item.getItemId()) {
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
            for (CashFlow cashFlow : controller.recuperarListaDeFluxoDeCaixa()) {
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
                        if (dialogInterface != null) {
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
    private void clearCashFlowListView() {

        controller.excluirFluxoDeCaixa();
        cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo para exibir um alertDialog para inserir um novo fluxo de caixa
     */
    private void showAddCashFlowAlertDialog() {

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
                            } catch (NumberFormatException nfe) {
                                showToast(getString(R.string.invalid_type));
                            }
                        }
                    }
                });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Método para adicionar um fluxo de caixa a ArrayList e atualizar o adapter
     *
     * @param cash Double fluxo de caixa no período considerado
     */
    private void addChashFlow(Double cash) {

        controller.adicionarFluxoDeCaixa(cash);

        //Atualiza o Adapter
        cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo para exibir um alertDialog para editar um novo fluxo de caixa
     *
     * @param index int indice da lista
     */
    private void showEditCashFlowAlertDialog(final int index) {

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
                            editCashFlow(index, cash);
                        }
                    }
                });

        builder.setNegativeButton(R.string.project_dialog_negative_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (dialogInterface != null) {
                            dialogInterface.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Edita o valor do fluxo de caixa ao clicar em um indice da lista
     *
     * @param index int indice clicado da lista
     * @param cash  double valor do fluxo de caixa
     */
    private void editCashFlow(int index, double cash) {

        controller.atualizarValor(index, cash);
        //Atualiza a visualizacao da listView
        cashFlowAdapter.notifyDataSetChanged();
    }

    /**
     * Método para exibir um toast personalizado
     *
     * @param message
     */
    private void showToast(String message) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
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

        outState.putParcelable(PROJECT_STATE, controller);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.low_rate_key))) {
            //Taxa de retorno inicial para cálculo das iterações
            controller.setLowRate(Double.parseDouble(sharedPreferences
                    .getString(getString(R.string.low_rate_key)
                            , getString(R.string.low_rate_default_value))));
        } else if (key.equals(getString(R.string.high_rate_key))) {
            controller.setHighRate(Double.parseDouble(sharedPreferences
                    .getString(getString(R.string.high_rate_key)
                            , getString(R.string.high_rate_default_value))));
            //Log.i(LOG_TAG,key + HIGH_RATE);
        } else if (key.equals(getString(R.string.max_iterations_key))) {
            controller.setMaxIterations(Integer.parseInt(sharedPreferences
                    .getString(getString(R.string.max_iterations_key)
                            , getString(R.string.max_iterations_default_value))));
            //Log.i(LOG_TAG,key + MAX_ITERATION);
        } else if (key.equals(getString(R.string.req_precision_key))) {
            //Precisão máxima para o cálculo da TIR
            controller.setPrecisionRequirements(Double.parseDouble(sharedPreferences
                    .getString(getString(R.string.req_precision_key)
                            , getString(R.string.reg_precision_default_value))));
            //Log.i(LOG_TAG, key + PRECISION_REQ);
        } else if (key.equals(getString(R.string.calculate_tirm_key))) {
            IRR_METHOD = sharedPreferences.getBoolean(getString(R.string.calculate_tirm_key),
                    getResources().getBoolean(R.bool.calculate_tirm));
            Log.i(LOG_TAG, key + IRR_METHOD);
        } else if (key.equals(getString(R.string.pref_currency_key))) {
            CURRENCY = sharedPreferences.getString(getString(R.string.pref_currency_key),
                    getString(R.string.pref_currency_default_value));
            //Atualiza a TextView com o resultado do cálculo do VPL
            vplTextView.setText(getResources().getString(R.string.tv_vpl_result) + " " + CURRENCY +
                    " " + mVPLResult);
            //Informa o adapter da alteração
            cashFlowAdapter.notifyDataSetChanged();
        } else if (key.equals(getString(R.string.pref_period_key))) {
            PERIOD = sharedPreferences.getString(getString(R.string.pref_period_key),
                    getString(R.string.pref_period_month_value));
            //Atualiza a TextView com o resultado do cálculo do Payback
            setPaybackTextView();
        }
    }

    /**
     * Esse método exibe o valor da taxa de reinvestimento na rrateTextView
     *
     * @param rrate taxa de reinvestimento Double
     */
    private void setRRateTVString(double rrate) {
        //Cria a string rrateString com o valor definido nos atributos
        String rrateString = getString(R.string.tv_rrate_start) + " " + rrate * 100 +
                " " + getString(R.string.tv_rrate_end);
        rrateTextView.setText(rrateString);
    }

    /**
     * Esse método exibe o valor da taxa de financiamento na frateTextView
     *
     * @param frate taxa de financiamento Double
     */
    private void setFRateTVString(double frate) {
        //Cria a string frateString com o valor definido nos atributos
        String frateString = getString(R.string.tv_frate_start) + " " + frate * 100 +
                " " + getString(R.string.tv_frate_end);
        frateTextView.setText(frateString);
    }

    /**
     * Define a ação para cada item, dependendo a posicao
     *
     * @param postiton posição da ListView clicada
     */
    private void selectItem(int postiton) {
        switch (postiton) {
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
