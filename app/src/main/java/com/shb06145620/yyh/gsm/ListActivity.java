package com.shb06145620.yyh.gsm;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ListActivity extends AppCompatActivity {

  // ListView listView = null;
  private static final String BRLIST_URL = "http://172.16.1.253:3000/gsm/list";

  class Item {
    String branch_no;
    String branch_name;
    String webserver_1_status;
    String webserver_2_status;

    Item(String branch_no, String branch_name, String webserver_1_status, String webserver_2_status) {
      this.branch_no = branch_no;
      this.branch_name = branch_name;
      this.webserver_1_status = webserver_1_status;
      this.webserver_2_status = webserver_2_status;
    }
  }

  //조회 버튼 클릭시 refresh이벤트 처리
  public void refresh(View view) {
    new ListActivity.ConnList().execute(BRLIST_URL);
    //Intent intent = new Intent(ListActivity.this, ListActivity.class);
    //startActivity(intent);
  }


  //push발송 클릭시 이벤트 처리 함수
  public void push_send(View view) {

    //교재 p707~p713 Firebase 설정 적용
    String fireTokenText = "";
    try {
      fireTokenText = FirebaseInstanceId.getInstance().getToken();
    } catch (Exception e) {
      e.printStackTrace();
    }

    new Push().execute("http://172.16.1.253:3000/user/push/", fireTokenText);
  }

  class Push extends AsyncTask<String,String,String> {
    ProgressDialog dialog = new ProgressDialog(ListActivity.this);
    @Override
    protected String doInBackground(String... params) {
      StringBuilder output = new StringBuilder();
      try {
        URL url = new URL(params[0]);
        JSONObject postDataParams = new JSONObject();
        postDataParams.put("device_token", params[1]);

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
      dialog.setMessage("push 전송중...");
      dialog.show();
    }
    @Override
    protected void onPostExecute(String s) {
      super.onPostExecute(s);
      dialog.dismiss();
      try {
        JSONObject json = new JSONObject(s);
        if (json.getBoolean("result") == true) {//push성공
          //Toast.makeText(ListActivity.this,
          //        "push전송이 완료 되었습니다.",
          //        Toast.LENGTH_SHORT).show();

          Toast.makeText(ListActivity.this,
                  Html.fromHtml("<font color='white'><b>"+ "push전송이 완료 되었습니다."+"</b></font>"),
                  Toast.LENGTH_SHORT).show();

        } else {//실패
          Toast.makeText(ListActivity.this,
                  "push전송이 실패되었습니다.",
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

      ImageView imageView = (ImageView)convertView.findViewById(R.id.image);
      TextView text1View = (TextView)convertView.findViewById(R.id.branch_name);
      TextView text2View = (TextView)convertView.findViewById(R.id.webserver_1_status);
      TextView text3View = (TextView)convertView.findViewById(R.id.webserver_2_status);

      ListActivity.Item item = itemList.get(position);

      if (item.branch_no.equals("100")) {
        imageView.setImageResource(R.drawable.f100);
      }
      else if (item.branch_no.equals("111")) {
        imageView.setImageResource(R.drawable.f111);
      }
      else if (item.branch_no.equals("120")) {
        imageView.setImageResource(R.drawable.f120);
      }
      else if (item.branch_no.equals("130")) {
        imageView.setImageResource(R.drawable.f130);
      }
      else if (item.branch_no.equals("140")) {
        imageView.setImageResource(R.drawable.f140);
      }
      else if (item.branch_no.equals("161")) {
        imageView.setImageResource(R.drawable.f161);
      }
      else if (item.branch_no.equals("162")) {
        imageView.setImageResource(R.drawable.f162);
      }
      else if (item.branch_no.equals("164")) {
        imageView.setImageResource(R.drawable.f164);
      }
      else if (item.branch_no.equals("166")) {
        imageView.setImageResource(R.drawable.f166);
      }
      else if (item.branch_no.equals("171")) {
        imageView.setImageResource(R.drawable.f171);
      }
      else if (item.branch_no.equals("180")) {
        imageView.setImageResource(R.drawable.f180);
      }
      else if (item.branch_no.equals("400")) {
        imageView.setImageResource(R.drawable.f400);
      }
      else if (item.branch_no.equals("411")) {
        imageView.setImageResource(R.drawable.f411);
      }
      else if (item.branch_no.equals("500")) {
        imageView.setImageResource(R.drawable.f500);
      }
      else if (item.branch_no.equals("501")) {
        imageView.setImageResource(R.drawable.f501);
      }
      else {
        imageView.setImageResource(R.drawable.f511);
      }


      text1View.setText(item.branch_name);

      //오류이면 폰트컬러 빨간색
      if (item.webserver_1_status.equals("오류")) {
        text2View.setText(Html.fromHtml("<font color='red'><b>"+item.webserver_1_status+"</b></font>"));
      } else {
        text2View.setText(item.webserver_1_status);
      }

      if (item.webserver_2_status.equals("오류")) {
        text3View.setText(Html.fromHtml("<font color='red'><b>"+item.webserver_2_status+"</b></font>"));
      } else {
        text3View.setText(item.webserver_2_status);
      }

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

          itemList.clear();
          JSONArray ny_array = json.getJSONArray("ny");

          for (int ix = 0; ix < ny_array.length(); ix++)
          {
            JSONObject obj = ny_array.getJSONObject(ix);

            String branch_no = obj.getString("branch_no");
            String branch_name = obj.getString("branch_name");

            String webserver_1_status = obj.getString("webserver_1_status");
            String webserver_2_status = obj.getString("webserver_2_status");

            itemList.add(new ListActivity.Item(branch_no, branch_name, webserver_1_status, webserver_2_status));
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

