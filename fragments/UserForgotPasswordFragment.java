package com.panaceasoft.restaurateur.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.panaceasoft.restaurateur.Config;
import com.panaceasoft.restaurateur.R;
import com.panaceasoft.restaurateur.activities.MainActivity;
import com.panaceasoft.restaurateur.activities.UserRegisterActivity;
import com.panaceasoft.restaurateur.utilities.Utils;
import com.panaceasoft.restaurateur.utilities.VolleySingleton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UserForgotPasswordFragment extends Fragment {

    /**------------------------------------------------------------------------------------------------
     * Start Block - Private Variables
     **------------------------------------------------------------------------------------------------*/

    private View view;
    private EditText txtEmail;
    private ProgressDialog prgDialog;
    private String jsonStatusSuccessString;
    private LinearLayout mainLayout;

    /*------------------------------------------------------------------------------------------------
     * End Block - Private Functions
     **------------------------------------------------------------------------------------------------*/


    /**------------------------------------------------------------------------------------------------
     * Start Block - Override Functions
     **------------------------------------------------------------------------------------------------*/

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user_forgot_password, container, false);

        initData();

        initUI();

        return view;
    }

    @Override
    public void onDestroy() {

        try {
            Utils.unbindDrawables(mainLayout);
            //GlobalData.shopdata = null;
            super.onDestroy();
        }catch (Exception e){
            super.onDestroy();
        }
    }

    /*------------------------------------------------------------------------------------------------
     * End Block - Private Functions
     **------------------------------------------------------------------------------------------------*/

    /**------------------------------------------------------------------------------------------------
     * Start Block - Init Data Functions
     **------------------------------------------------------------------------------------------------*/

    private void initData() {
        try {
            jsonStatusSuccessString = getResources().getString(R.string.json_status_success);

        }catch(Exception e){
            Utils.psErrorLogE("Error in init data.", e);
        }
    }

    /*------------------------------------------------------------------------------------------------
     * End Block - Init Data Functions
     **------------------------------------------------------------------------------------------------*/

    /**------------------------------------------------------------------------------------------------
     * Start Block - init UI Functions
     **------------------------------------------------------------------------------------------------*/

    private void initUI() {
        try {
            txtEmail = this.view.findViewById(R.id.input_email);
            txtEmail.setTypeface(Utils.getTypeFace(getContext(), Utils.Fonts.ROBOTO));
            Button btnRequest = this.view.findViewById(R.id.button_request);
            btnRequest.setTypeface(Utils.getTypeFace(getContext(), Utils.Fonts.ROBOTO));
            btnRequest.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    doRequest();
                }
            });
            Button btnCancel = this.view.findViewById(R.id.button_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    doCancel();
                }
            });
            prgDialog = new ProgressDialog(getActivity());
            prgDialog.setMessage("Please wait...");
            prgDialog.setCancelable(false);

            mainLayout = this.view.findViewById(R.id.nav_forgot);
            mainLayout.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.fade_in));

        }catch(Exception e){
            Utils.psErrorLogE("Error in Init UI.", e);
        }
    }

    /*------------------------------------------------------------------------------------------------
     * End Block - init UI Functions
     **------------------------------------------------------------------------------------------------*/

    /**------------------------------------------------------------------------------------------------
     * Start Block - Private Functions
     **------------------------------------------------------------------------------------------------*/

    private void doCancel() {
        if(getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).openFragment(R.id.nav_profile);
        }else if(getActivity() instanceof UserRegisterActivity) {
            getActivity().finish();
        }
    }

    private void doRequest() {
        if(inputValidation()) {
            final String URL = Config.APP_API_URL + Config.GET_FORGOT_PASSWORD;
            HashMap<String, String> params = new HashMap<>();
            params.put("email", txtEmail.getText().toString());

            doSubmit(URL,params);
        }else {
            Toast.makeText(getActivity(), "Fail input validation.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean inputValidation() {
        if(getContext() != null) {
            if (txtEmail.getText().toString().equals("")) {
                Toast.makeText(getContext(), R.string.email_validation_message,
                        Toast.LENGTH_LONG).show();
                return false;
            } else {
                if (!Utils.isEmailFormatValid(txtEmail.getText().toString())) {
                    Toast.makeText(getContext(), R.string.email_format_validation_message,
                            Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            return true;
        }else {
            return  false;
        }
    }

    public void doSubmit(String URL, final HashMap<String,String> params) {
        prgDialog.show();
        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            String success_status = response.getString("status");
                            Utils.psLog(response.toString());

                            prgDialog.cancel();
                            if (success_status.equals(jsonStatusSuccessString)) {
                                showSuccessPopup();
                            } else {
                                showFailPopup(response.getString("error"));
                            }

                        } catch (JSONException e) {
                            Utils.psLog(response.toString());
                            prgDialog.cancel();
                            e.printStackTrace();
                        } catch (Exception e){
                            Utils.psErrorLog("Error in loading.", e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    prgDialog.cancel();
                    VolleyLog.e("Error: ", error.getMessage());
                }catch (Exception e) {
                    Utils.psErrorLog("onErrorResponse", e);
                }
            }
        });

        req.setShouldCache(false);
        VolleySingleton.getInstance(getActivity()).addToRequestQueue(req);
    }

    public void showSuccessPopup() {
        if(getActivity() != null) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.forgot_password);
            builder.setMessage(R.string.forgot_success);
            builder.setPositiveButton(R.string.OK, null);
            builder.show();
        }
    }

    public void showFailPopup(String error) {
        if(getActivity() != null) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.forgot_password);
            builder.setMessage(error);
            builder.setPositiveButton(R.string.OK, null);
            builder.show();
        }
    }

    /*------------------------------------------------------------------------------------------------
     * End Block - Private Functions
     **------------------------------------------------------------------------------------------------*/
}
