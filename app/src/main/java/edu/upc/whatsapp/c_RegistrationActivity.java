package edu.upc.whatsapp;

import edu.upc.whatsapp.comms.RPC;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.Serializable;

import edu.upc.whatsapp.service.PushService;
import entity.User;
import entity.UserInfo;

public class c_RegistrationActivity extends Activity implements View.OnClickListener {

    _GlobalState globalState;
    ProgressDialog progressDialog;
    User user;
    OperationPerformer operationPerformer;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            operationPerformer = null;
            progressDialog.dismiss();

            UserInfo userInfo = (UserInfo) msg.getData().getSerializable("userInfo");

            if (userInfo.getId() >= 0) {
                toastShow("Registration successful");

                globalState.my_user = userInfo;
                Intent myIntent = new Intent(globalState, d_UsersListActivity.class);
                startActivity(myIntent);

                finish();
            }
            else if (userInfo.getId() == -1) {
                toastShow("Registration unsuccessful,\nlogin already used by another user");
            }
            else if (userInfo.getId() == -2) {
                toastShow("Not registered, connection problem due to: " + userInfo.getName());
                System.out.println("--------------------------------------------------");
                System.out.println("error!!!");
                System.out.println(userInfo.getName());
                System.out.println("--------------------------------------------------");
            }
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        globalState = (_GlobalState)getApplication();
        setContentView(R.layout.c_registration);
        ((Button) findViewById(R.id.register_button)).setOnClickListener(this);
    }

    public void onClick(View arg0) {
        if (arg0 == findViewById(R.id.register_button)) {

            String login = ((EditText) findViewById(R.id.registration_login)).getText().toString();
            String password = ((EditText) findViewById(R.id.registration_password)).getText().toString();
            String name = ((EditText) findViewById(R.id.registration_name)).getText().toString();
            String surname = ((EditText) findViewById(R.id.registration_surname)).getText().toString();
            String email = ((EditText) findViewById(R.id.registration_email)).getText().toString();


            boolean ready = true;

            if (login.length() == 0) {
                ((EditText) findViewById(R.id.registration_login)).setError("This field must be filled");
                ready = false;
            }
            if (password.length() == 0) {
                ((EditText) findViewById(R.id.registration_password)).setError("This field must be filled");
                ready = false;
            }
            if (name.length() == 0) {
                ((EditText) findViewById(R.id.registration_name)).setError("This field must be filled");
                ready = false;
            }
            if (surname.length() == 0) {
                ((EditText) findViewById(R.id.registration_surname)).setError("This field must be filled");
                ready = false;
            }
            if (email.length() == 0) {
                ((EditText) findViewById(R.id.registration_email)).setError("This field must be filled");
                ready = false;
            }


            if (ready) {
                this.user = new User();
                UserInfo userInfo = new UserInfo();
                userInfo.setName(name);
                userInfo.setSurname(surname);
                user.setEmail(email);
                user.setLogin(login);
                user.setPassword(password);
                user.setUserInfo(userInfo);

                progressDialog = ProgressDialog.show(this, "RegistrationActivity", "Registering for service...");
                // if there's still a running thread doing something, we don't create a new one
                if (operationPerformer == null) {
                    operationPerformer = new OperationPerformer();
                    operationPerformer.start();
                }
            }
        }
    }

    private void toastShow(String text) {
        Toast toast = Toast.makeText(this, text, Toast.LENGTH_LONG);
        toast.setGravity(0, 0, 200);
        toast.show();
    }

    private class OperationPerformer extends Thread {

        @Override
        public void run() {
            Message msg = handler.obtainMessage();
            Bundle b = new Bundle();

            UserInfo userInfo = RPC.registration(user);
            Log.d("userInfo", userInfo.toString());
            b.putSerializable("userInfo", userInfo);

            msg.setData(b);
            handler.sendMessage(msg);
        }
    }
}
