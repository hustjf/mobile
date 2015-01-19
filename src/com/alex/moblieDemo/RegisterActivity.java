package com.alex.moblieDemo;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Alex on 14-9-18.
 */
public class RegisterActivity extends Activity{
    private EditText username;
    private EditText password;
    private Button confirmBTN;
    private Button backBTN;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        confirmBTN = (Button) findViewById(R.id.confirm);
        backBTN = (Button) findViewById(R.id.back);
        confirmBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable usernameText = username.getText();
                Editable passwordText = password.getText();
            }
        });
        backBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void connect() {

    }
}
