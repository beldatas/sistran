package net.sistran.mobile.login;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.gc.materialdesign.widgets.ProgressDialog;
import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.rey.material.widget.TextView;

import net.sistran.mobile.appconstantes.AppConstants;
import net.sistran.mobile.appobjeto.AppObject;
import net.sistran.mobile.autoinfracao.Utility;
import net.sistran.mobile.fragment.AnyPageChangeListener;
import net.sistran.mobile.http.WebClient;
import net.sistran.mobile.location.GPSTracker;
import net.sistran.mobile.main.MainUserActivity;
import net.sistran.mobile.network.NetworkConnection;
import net.sistran.mobile.timeandime.TimeAndIme;
import net.sistran.mobile.util.DialogMaterial;
import net.sistran.mobile.util.Rotinas;
import net.sistran.mobile.util.User;
import net.sistran.R;

import cz.msebera.android.httpclient.Header;


public class FragmentLogin extends Fragment implements OnClickListener {

    private View view;
    private AnyPageChangeListener anyPageChangeListener;
    private TextView btnRegister;
    private Button btnLogin;
    private EditText etEmail, etPassword;
    private ProgressDialog pd;
    private TimeAndIme imei;
    private GPSTracker gps;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        anyPageChangeListener = (LoginActivity) activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_fragment, null, false);
        initializedView();
        return view;
    }

    private void initializedView() {
        imei = new TimeAndIme(getActivity());
        gps = new GPSTracker(getActivity());

        btnLogin = (Button) view.findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(this);
        etEmail = (EditText) view.findViewById(R.id.et_email);
        etPassword = (EditText) view.findViewById(R.id.et_password);
        pd = new ProgressDialog(getActivity(), getResources().getString(R.string.processing));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (checkInput()) {
                    if (NetworkConnection.isNetworkAvailable(getActivity())) {
                        //imm.hideSoftInputFromWindow(btnLogin.getWindowToken(), 0);
                        Rotinas.closeKeyboard(btnLogin,getActivity());
                        userLogin();
                    } else {
                        DialogMaterial.getBottomSheet(getResources().getString(R.string.sem_conexao), Color.RED, getActivity()).show();
                    }
                }
                break;
        }
    }

    private boolean checkInput() {
        if (etEmail.getText().toString().equals("")) {
            etEmail.setError(getResources().getString(
                    R.string.texto_inserir_email));
            etEmail.requestFocus();
            return false;
        } else if (etPassword.getText().toString().equals("")) {
            etPassword.setError(getResources().getString(
                    R.string.texto_inserir_senha));
            etPassword.requestFocus();
            return false;
        } else {
            return true;
        }
    }

    private void userLogin() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String imei = this.imei.getIMEI(getActivity());
        String mobile = this.imei.getPhoneNumber(getActivity());
        Location latitude = this.gps.getLatitude();
        Location longitude = this.gps.getLongitude();
        String uniqueID = this.imei.getDeviceUniqueID(getActivity());
        String url = WebClient.getLoginUrl(email, password, imei, mobile, latitude, longitude);

        Log.d("Imei: ", imei + " Telefone: " + mobile + " UniqueID: " + uniqueID + " Location: " + latitude + "," + longitude);

        WebClient.get(url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onStart() {
                super.onStart();
                pd.show();
            }

            @Override
            public void onFinish() {
                super.onFinish();
                pd.dismiss();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                User user = gson.fromJson(response, User.class);
                //Log.d("Sucesso", user.toString());
                if (user.getSuccess().compareTo("1") == 0) {
                    AppObject.getTinyDB(getActivity()).putBoolean(AppConstants.isLogin, true);
                    AppObject.getTinyDB(getActivity()).putObject(AppConstants.user, user);
                    getActivity().startActivity(new Intent(getActivity(), MainUserActivity.class));
                    getActivity().finish();
                    //Log.d("Imei", FragmentLogin.this.imei.getImei());
                } else {
                    showLoginFailMgs();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                showLoginFailMgs();
            }
        });
    }

    private void showLoginFailMgs() {
        DialogMaterial.getBottomSheet(getResources().getString(R.string.login_fail), Color.RED, getActivity()).show();
    }

}
