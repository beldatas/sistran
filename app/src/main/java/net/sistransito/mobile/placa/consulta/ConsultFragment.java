package net.sistransito.mobile.placa.consulta;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.BufferType;

import com.rey.material.widget.CheckBox;

import net.sistransito.mobile.autoinfracao.AutoActivity;
import net.sistransito.mobile.autoinfracao.DadosDoAuto;
import net.sistransito.mobile.database.DatabaseCreator;
import net.sistransito.mobile.fragment.AnyDiaglog;
import net.sistransito.mobile.fragment.FragmentCallBack;
import net.sistransito.mobile.network.NetworkConnection;
import net.sistransito.mobile.placa.dados.PlacaHttpResultAnysTask;
import net.sistransito.mobile.placa.dados.PlacaRawFormat;
import net.sistransito.mobile.placa.dados.PlacaViewFormat;
import net.sistrnsitomobile.R;

public class ConsultFragment extends Fragment implements
        OnClickListener {

    private View view;
    private LinearLayout ll_parent_rsult_view, ll_child_reult_view;
    private TextView tv_result_show;
    private Button btn_autuar, btn_consultar;
    private EditText et_letras_da_placa, et_numeros_da_placa;
    private CheckBox cb_pesquisa_offline;
    private String placa;
    private final int PLACA_CHARACTER_LENGTH = 3;
    private final int PLACA_NUMBER_LENGTH = 4;
    private InputMethodManager imm;
    private PlacaHttpResultAnysTask httpResultAnysTask;
    private EditTextCahnge et_charater, et_number;
    private Boolean text_change_statte = true;
    private PlacaRawFormat placaRawFormat_main;
 //   private GPSTracker gps;

    public static ConsultFragment newInstance() {
        return new ConsultFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.consulta_fragment, null, false);
        imm = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        initilizedView();
        return view;
    }

    private void initilizedView() {
        ll_parent_rsult_view = (LinearLayout) view
                .findViewById(R.id.ll_parent_rsult_view);
        ll_child_reult_view = (LinearLayout) view
                .findViewById(R.id.ll_child_reult_view);
        tv_result_show = (TextView) view.findViewById(R.id.tv_result_show);
        tv_result_show.setOnClickListener(this);
        btn_autuar = (Button) view.findViewById(R.id.btn_autuar);
        btn_autuar.setOnClickListener(this);
        btn_consultar = (Button) view.findViewById(R.id.btn_consultar);
        btn_consultar.setOnClickListener(this);
        et_letras_da_placa = (EditText) view
                .findViewById(R.id.et_letras_da_placa);
        et_numeros_da_placa = (EditText) view
                .findViewById(R.id.et_numeros_da_placa);
        cb_pesquisa_offline = (CheckBox) view
                .findViewById(R.id.cb_pesquisa_offline);
        reomoveResultView();
        et_charater = new EditTextCahnge(R.id.et_letras_da_placa);
        et_number = new EditTextCahnge(R.id.et_numeros_da_placa);
        et_letras_da_placa.addTextChangedListener(et_charater);
        et_numeros_da_placa.addTextChangedListener(et_number);

        //gps = new GPSTracker(getActivity());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_consultar:
                if (iSVehicalSearch()) {
                    getVehicaleResult();
             //       Log.d("Latitude", gps.getLocation().getLatitude() + " ");
                //    Log.d("Longitude", gps.getLocation().getLongitude() + " ");
                }
                break;

            case R.id.tv_result_show:
                reomoveResultView();
                enableSearch();
                break;

            case R.id.btn_autuar:
                if (placaRawFormat_main != null) {

                    if ((DatabaseCreator
                            .getDatabaseAdapterAutoInfracao(getActivity()))
                            .isSamePlacaExit(placaRawFormat_main.getPLATE())) {
                        openAutode(DatabaseCreator.getDatabaseAdapterAutoInfracao(getActivity()).getAutodeDataFromPlaca(placaRawFormat_main.getPLATE()));

                    } else {
                        DadosDoAuto data = new DadosDoAuto();
                        data.setPlate(placaRawFormat_main.getPLATE());
                        data.setModel(placaRawFormat_main.getMODEL());
                        data.setCor_do_veiculo(placaRawFormat_main.getCOLOR());
                        data.setStoreFullData(false);
                        openAutode(data);
                    }
                }
        }
    }

    private void getVehicaleResult() {
        httpResultAnysTask = new PlacaHttpResultAnysTask(
                new FragmentCallBack() {

                    @Override
                    public void CallBack(PlacaRawFormat placaRawFormat,
                                         boolean isOffline) {
                        Log.d("bbbbb", "1");
                        resultCallBack(placaRawFormat, isOffline);

                    }
                }, getActivity(), isOfflineSearch(), placa, null);//gps.getLocation());
        httpResultAnysTask.execute("");
    }

    private void addResultView() {
        if (ll_child_reult_view.getParent() == null) {
            ll_parent_rsult_view.addView(ll_child_reult_view);
        }
    }

    private void reomoveResultView() {
        if (ll_child_reult_view.getParent() != null) {
            ll_parent_rsult_view.removeView(ll_child_reult_view);
        }

    }

    private boolean iSVehicalSearch() {

        if (true){//gps.canGetLocation()) {

            if (checkInput()) {
                if (isOfflineSearch()) {
                    return true;
                } else {
                    if (isInternetConnected()) {
                        return true;
                    } else {
                        AnyDiaglog.DialogShow(
                                getResources().getString(R.string.sem_conexao),
                                getActivity(), "info");
                        return false;
                    }
                }
            } else {
                return false;
            }
        } else {

            return false;
        }
    }

    private boolean isOfflineSearch() {
        return cb_pesquisa_offline.isChecked() ? true : false;
    }

    private boolean isInternetConnected() {
        return NetworkConnection.isNetworkAvailable(getActivity());
    }

    public void resultCallBack(PlacaRawFormat placaRawFormat, boolean isOffline) {
        placaRawFormat_main = null;
   //     gps.stopUsingGPS();


        if (placaRawFormat != null) {

            this.placaRawFormat_main = placaRawFormat;
            PlacaViewFormat placaViewFormat = new PlacaViewFormat(
                    placaRawFormat, getActivity());
            addResultView();

            tv_result_show.setText(placaViewFormat.getResultViewData(),
                    BufferType.SPANNABLE);
            placaViewFormat.setWarning();
            addResultView();
            diableSearch();
            btn_autuar.setEnabled(true);

        } else {
            tv_result_show.setText(getResources().getString(
                    R.string.nehum_resultado_retornado));
            btn_autuar.setEnabled(false);
            addResultView();
            diableSearch();
        }
    }

    // after result enable all search helper view ;
    private void enableSearch() {
        btn_consultar.setEnabled(true);
        et_letras_da_placa.setEnabled(true);
        et_numeros_da_placa.setEnabled(true);
        cb_pesquisa_offline.setEnabled(true);
    }

    // after show search result disable all search helper view ;
    private void diableSearch() {
        btn_consultar.setEnabled(false);
        et_letras_da_placa.setEnabled(false);
        et_numeros_da_placa.setEnabled(false);
        cb_pesquisa_offline.setEnabled(false);
        text_change_statte = false;
        et_numeros_da_placa.setText("");
        et_letras_da_placa.setText("");
        text_change_statte = true;
    }

    // check input if the number length is 4 and character length is 3
    private boolean checkInput() {
        String chareter = et_letras_da_placa.getText().toString().trim();
        String number = et_numeros_da_placa.getText().toString().trim();
        placa = chareter + number;
        if (chareter.length() != PLACA_CHARACTER_LENGTH) {
            et_letras_da_placa.setError(getResources().getString(
                    R.string.letras_da_placa));
            et_letras_da_placa.requestFocus();
            return false;
        } else if (number.length() != PLACA_NUMBER_LENGTH) {
            et_numeros_da_placa.setError(getResources().getString(
                    R.string.numeros_da_placa));
            et_numeros_da_placa.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private class EditTextCahnge implements TextWatcher {
        private int id;

        public EditTextCahnge(int id) {
            this.id = id;
        }

        @Override
        public void afterTextChanged(Editable text) {
            if (text_change_statte) {

                switch (id) {
                    case R.id.et_letras_da_placa:

                        et_letras_da_placa.setError(null);
                        if (text.toString().length() == PLACA_CHARACTER_LENGTH) {
                            if (et_numeros_da_placa.getText().toString().length() != PLACA_NUMBER_LENGTH) {
                                et_numeros_da_placa.requestFocus();
                            } else {
                                imm.hideSoftInputFromWindow(
                                        btn_consultar.getWindowToken(), 0);
                                btn_consultar.setFocusableInTouchMode(true);
                                btn_consultar.requestFocus();
                            }
                        }
                        break;
                    case R.id.et_numeros_da_placa:

                        et_numeros_da_placa.setError(null);
                        if (text.toString().length() == PLACA_NUMBER_LENGTH) {
                            if (et_letras_da_placa.getText().toString().length() != PLACA_CHARACTER_LENGTH) {
                                et_letras_da_placa.requestFocus();
                            } else {
                                imm.hideSoftInputFromWindow(
                                        btn_consultar.getWindowToken(), 0);
                                btn_consultar.setFocusableInTouchMode(true);
                                btn_consultar.requestFocus();
                            }
                        }
                        break;
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {
        }
    }

    private void openAutode(DadosDoAuto data) {
        Intent intent = new Intent(getActivity(), AutoActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(DadosDoAuto.getAutoDeId(), data);
        intent.putExtras(bundle);
        startActivity(intent);
    }
}
