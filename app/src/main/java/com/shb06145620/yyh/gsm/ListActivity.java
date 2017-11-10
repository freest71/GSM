package com.shb06145620.yyh.gsm;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity {

  // ListView listView = null;
  private static final String BRLIST_URL = "http://210.92.231.28:3000/gsm/list";

  class Item {
    String branch_name;
    String webserver_location;
    String webserver_1_status;
    String webserver_2_status;

    Item(String branch_name, String webserver_location, String webserver_1_status, String webserver_2_status) {
      this.branch_name = branch_name;
      this.webserver_location = webserver_location;
      this.webserver_1_status = webserver_1_status;
      this.webserver_2_status = webserver_2_status;
    }
  }

  //ArrayList 저장할 타입은 <ListActivity.Item>
  ArrayList<ListActivity.Item> itemList = new ArrayList<ListActivity.Item>();

  class ItemAdapter extends ArrayAdapter {
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
      if (convertView == null) {
        LayoutInflater layoutInflater =
          (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(R.layout.list_item, null);
      }

      TextView text1View = (TextView)convertView.findViewById(R.id.branch_name);
      TextView text2View = (TextView)convertView.findViewById(R.id.webserver_location);
      TextView text3View = (TextView)convertView.findViewById(R.id.webserver_1_status);
      TextView text4View = (TextView)convertView.findViewById(R.id.webserver_2_status);

      ListActivity.Item item = itemList.get(position);

      text1View.setText(item.branch_name);
      text2View.setText(item.webserver_location);
      text3View.setText(item.webserver_1_status);
      text4View.setText(item.webserver_2_status);

      return convertView;
    }

    public ItemAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull List objects) {
      super(context, resource, objects);
    }
  }

  ListActivity.ItemAdapter itemAdpater = null;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_list);

    ListView listView = (ListView)findViewById(R.id.listView);

    itemAdpater = new ListActivity.ItemAdapter(ListActivity.this, R.layout.list_item, itemList);
    listView.setAdapter(itemAdpater);

    // 통신하여 모니터링 내역 조회
    new ListActivity.ConnList().execute(BRLIST_URL);
  }

  // Async 통신 처리
  class ConnList extends AsyncTask<String,String,String> {
    ProgressDialog dialog = new ProgressDialog(ListActivity.this);
    @Override
    protected String doInBackground(String... params) {
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
      dialog.setMessage("국가별 서버 모니터링 결과 조회중..");
      dialog.show();
    }
    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      dialog.dismiss();
      try {
        JSONObject json = new JSONObject(s);

        if (json.getBoolean("result") == true) {//통신 성공

          Log.d("___debug___", "gsm 통신성공");

          JSONArray ny_array = json.getJSONArray("ny");

          for (int ix = 0; ix < ny_array.length(); ix++)
          {
            JSONObject obj = ny_array.getJSONObject(ix);

            String branch_name = obj.getString("branch_name");
            String webserver_location = obj.getString("webserver_location");
            String webserver_1_status = obj.getString("webserver_1_status");
            String webserver_2_status = obj.getString("webserver_2_status");

            itemList.add(new ListActivity.Item(branch_name, webserver_location, webserver_1_status, webserver_2_status));
          }

          Log.d("___debug___", "notifyDataSetChanged호출전");
          itemAdpater.notifyDataSetChanged();

        } else {//통신 실패

          Log.d("___debug___", "gsm 통신실패");
          Toast.makeText(ListActivity.this,
            json.getString("err"),
            Toast.LENGTH_SHORT).show();
        }
      } catch (Exception e) { e.printStackTrace(); }
    }
  }
}

