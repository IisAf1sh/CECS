package com.experiment.cecsclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.experiment.util.EncryptUtils;
import com.experiment.util.SPCHS;

import org.json.JSONObject;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyPair;

import it.unisa.dia.gas.jpbc.Element;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText editText;
    JSONArray tuples;
    KeyPair keyPairO;
    KeyPair keyPairR;
    Socket socket;
    BufferedReader br;
    BufferedWriter bw;
    SPCHS spchs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText=(EditText) findViewById(R.id.edit_text);
        Button upload=(Button) findViewById(R.id.btn_upload);
        Button share=(Button) findViewById(R.id.btn_share);
        Button search=(Button) findViewById(R.id.btn_search);
        upload.setOnClickListener(this);
        share.setOnClickListener(this);
        search.setOnClickListener(this);
        try {
            keyPairO = EncryptUtils.setupRSA();
            keyPairR = EncryptUtils.setupRSA();
        } catch (Exception e){
            e.printStackTrace();
        }
        setup();
    }
    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_upload){
            uploadRequest();
        }
        else if(view.getId()==R.id.btn_share){
            shareRequest();
        }
        else if(view.getId()==R.id.btn_search){
            searchRequest();
        }
    }
    private void setup(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    spchs=new SPCHS();
                    spchs.setup();
                    socket = new Socket("202.114.6.201", 8080);
                    br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                    JSONObject jobject=new JSONObject();
                    jobject.put("g",Base64.encodeToString(spchs.getG().toBytes(),Base64.NO_WRAP));
                    jobject.put("P",Base64.encodeToString(spchs.getP().toBytes(),Base64.NO_WRAP));
                    jobject.put("u",Base64.encodeToString(spchs.getU().toBytes(),Base64.NO_WRAP));
                    jobject.put("Pub",Base64.encodeToString(spchs.getPub().toBytes(),Base64.NO_WRAP));
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
                    Log.d("CECSClient","setup");
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void uploadRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    JSONObject jobject=new JSONObject();
                    jobject.put("request","uploading request");
                    jobject.put("PKO",Base64.encodeToString(keyPairO.getPublic().getEncoded(),Base64.NO_WRAP));
                    jobject.put("PKR",Base64.encodeToString(keyPairR.getPublic().getEncoded(),Base64.NO_WRAP));
                    byte[] PKR=keyPairR.getPublic().getEncoded();
                    //String path=editText.getText().toString();
                    String path="/sdcard/CECSClientData";
                    jobject.put("tuples",getTuples(path));
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
                    br.readLine();
                    long endTime = System.currentTimeMillis();
                    Log.d("CECSClient:Uploading",(endTime-startTime)+"ms");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void shareRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    JSONObject jobject=new JSONObject();
                    jobject.put("request","sharing request");
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
                    String ck=br.readLine();
                    JSONArray CK=new JSONObject(ck).getJSONArray("CK");
                    JSONArray K=new JSONArray();
                    long start = System.currentTimeMillis();
                    for(int i=0;i<CK.length();i++) {
                        ck=Base64.encodeToString(EncryptUtils.decryptRSA(Base64.decode(CK.getString(i), Base64.NO_WRAP), keyPairR.getPrivate()), Base64.NO_WRAP);
                        K.put(ck);
                    }
                    long end = System.currentTimeMillis();
                    Log.d("CECSClient:DecryptKey",(end-start)+"ms");
                    JSONObject k=new JSONObject();
                    k.put("K",K);
                    bw.write(k.toString()+"\n");
                    bw.flush();
                    String tuples=br.readLine();
                    long endTime = System.currentTimeMillis();
                    Log.d("CECSClient:Sharing",(endTime-startTime)+"ms");
                    show(tuples);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void searchRequest(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    long startTime = System.currentTimeMillis();
                    JSONObject jobject=new JSONObject();
                    jobject.put("request","searching request");
                    long start = System.currentTimeMillis();
                    Element TW=spchs.trapdoor(spchs.getS(),"keyword");
                    long end = System.currentTimeMillis();
                    Log.d("CECSClient:Trapdoor",(end-start)+"ms");
                    jobject.put("TW",Base64.encodeToString(TW.toBytes(),Base64.NO_WRAP));
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
                    String ck=br.readLine();
                    JSONArray CK=new JSONObject(ck).getJSONArray("CK");
                    JSONArray K=new JSONArray();
                    for(int i=0;i<CK.length();i++) {
                        ck=Base64.encodeToString(EncryptUtils.decryptRSA(Base64.decode(CK.getString(i), Base64.NO_WRAP), keyPairR.getPrivate()), Base64.NO_WRAP);
                        K.put(ck);
                    }
                    JSONObject k=new JSONObject();
                    k.put("K",K);
                    bw.write(k.toString()+"\n");
                    bw.flush();
                    String tuples=br.readLine();
                    long endTime = System.currentTimeMillis();
                    Log.d("CECSClient:Searching",(endTime-startTime)+"ms");
                    show(tuples);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void show(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d("CECSClient",str);
                editText.setText(str);
            }
        });
    }

    private JSONArray getTuples(String path){
        File f = new File(path);
        if (!f.exists()) {
            return null;
        }
        File[] files = f.listFiles();
        if(files==null) {
            return null;
        }
        tuples=new JSONArray();
        long sumTime=0;
        for (File file : files) {
            if(file.isFile()){
                StringBuilder sb=new StringBuilder();
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedReader br=new BufferedReader(new InputStreamReader(fis));
                    String str;
                    while((str=br.readLine())!=null){
                        sb.append(str);
                    }
                    str=sb.toString();
                    JSONObject jobject=new JSONObject();
                    jobject.put("data",str);
                    jobject.put("keywords","keyword");
                    long startTime = System.currentTimeMillis();
                    jobject.put("Sig",Base64.encodeToString(EncryptUtils.signature(str.getBytes(),keyPairO.getPrivate()),Base64.NO_WRAP));
                    long endTime = System.currentTimeMillis();
                    sumTime=sumTime+endTime-startTime;
                    tuples.put(jobject);
                    fis.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(file.isDirectory()){
                getTuples(file.getAbsolutePath());
            }
        }
        Log.d("CECSClient:Sig:",sumTime+"ms");
        return tuples;
    }
}
