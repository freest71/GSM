package com.shb06145620.yyh.gsm;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Log.d("login_chk", "1");

        login_chk();
    }

    public void login_chk() {

        Log.d("login_chk", "2");

        //교재 p707~p713 Firebase 설정 적용
        String fireTokenText = "";
        try {
            fireTokenText = FirebaseInstanceId.getInstance().getToken();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Login_chk().execute("http://172.16.1.253:3000/login/device_token/"+fireTokenText);
    }

    class Login_chk extends AsyncTask<String,String,String> {

        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        @Override
        protected String doInBackground(String... params) {

            Log.d("login_chk", "3");
            StringBuilder output = new StringBuilder();
            try {
                URL url = new URL(params[0]);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("GET");
                    //conn.setDoInput(true); conn.setDoOutput(true);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) break;
                        output.append(line);
                    }
                    reader.close();
                    conn.disconnect();
                }
            } catch (Exception e) { e.printStackTrace(); }
            return output.toString();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Device Token 로그인 체크중...");
            dialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            try {
                Log.d("login_chk", "4");
                JSONObject json = new JSONObject(s);
                if (json.getBoolean("result") == true) {//로그인 성공

                    Log.d("login_chk", "device token 기등록");
                    //device_token이 기등록된 고객인경우 로그인없이 자동 리스트뷰로 이동
                    Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                    startActivity(intent);
                    finish();

                } else {//로그인 실패
                    Log.d("login_chk", "device token미존재... 로그인 필요");
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }






    public void login(View view) {
        EditText userIdText = (EditText)findViewById(R.id.jkw_no);
        EditText passwordText = (EditText)findViewById(R.id.password);

        //교재 p707~p713 Firebase 설정 적용
        //Registration ID
        String fireTokenText = "";
        try {
            fireTokenText = FirebaseInstanceId.getInstance().getToken();
            //Log.i("fireTokenText", fireTokenText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        new Login().execute(
                "http://172.16.1.253:3000/login",
                userIdText.getText().toString(),
                passwordText.getText().toString(),
                fireTokenText);
    }

    class Login extends AsyncTask<String,String,String> {
        ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        @Override
        protected String doInBackground(String... params) {
            StringBuilder output = new StringBuilder();
            try {
                URL url = new URL(params[0]);
                JSONObject postDataParams = new JSONObject();
                postDataParams.put("jkw_no", params[1]);
                postDataParams.put("password", params[2]);
                postDataParams.put("fire_token", params[3]);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                if (conn != null) {
                    conn.setConnectTimeout(10000);
                    conn.setRequestMethod("POST");
                    conn.setDoInput(true); conn.setDoOutput(true);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(
                            new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getPostDataString(postDataParams));
                    writer.flush();
                    writer.close();
                    os.close();
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                    String line = null;
                    while(true) {
                        line = reader.readLine();
                        if (line == null) break;
                        output.append(line);
                    }
                    reader.close();
                    conn.disconnect();
                }
            } catch (Exception e) { e.printStackTrace(); }
            return output.toString();
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("로그인 중...");
            dialog.show();
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dialog.dismiss();
            try {
                JSONObject json = new JSONObject(s);
                if (json.getBoolean("result") == true) {//로그인 성공
                    String token = json.getString("token");
                    SharedPreferences pref = getSharedPreferences("pref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("token", token);
                    editor.commit();
                    Intent intent = new Intent(LoginActivity.this, ListActivity.class);
                    startActivity(intent);
                    finish();
                } else {//로그인 실패
                    Toast.makeText(LoginActivity.this,
                            "아이디가 없거나 암호가 틀렸습니다.",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        return result.toString();
    }
}
