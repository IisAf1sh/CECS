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

import java.net.Socket;
import java.io.*;
import java.security.KeyPair;
import java.security.PublicKey;

import javax.crypto.SecretKey;


public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    EditText editText;
    JSONArray tuples =new JSONArray();
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
                    JSONObject jobject=new JSONObject();
                    jobject.put("request","uploading request");
                    jobject.put("PKO",Base64.encodeToString(keyPairO.getPublic().getEncoded(),Base64.NO_WRAP));
                    jobject.put("PKR",Base64.encodeToString(keyPairR.getPublic().getEncoded(),Base64.NO_WRAP));
                    //String path=editText.getText().toString();
                    String path="/sdcard/CECSClientData";
                    jobject.put("tuples",getTuples(path));
                    /*JSONObject data=new JSONObject();
                    data.put("data",path);
                    data.put("keywords","keyword");
                    Log.d("CECSClient",EncryptUtils.signature(path.getBytes(),keyPairO.getPrivate()).toString());
                    data.put("Sig",Base64.encodeToString(EncryptUtils.signature(path.getBytes(),keyPairO.getPrivate()),Base64.NO_WRAP));
                    tuples=new JSONArray();
                    tuples.put(data);
                    jobject.put("tuples",tuples);*/
                    Log.d("CECSClient",jobject.toString());
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
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
                    JSONObject jobject=new JSONObject();
                    jobject.put("request","sharing request");
                    bw.write(jobject.toString()+"\n");
                    bw.flush();
                    Log.d("CECSClient",jobject.toString());
                    String CK=br.readLine();
                    Log.d("CECSClient",CK);
                    byte[] k=EncryptUtils.decryptRSA(Base64.decode(CK.getBytes(),Base64.NO_WRAP),keyPairR.getPrivate());
                    String K=Base64.encodeToString(EncryptUtils.decryptRSA(Base64.decode(CK.getBytes(),Base64.NO_WRAP),keyPairR.getPrivate()),Base64.NO_WRAP);
                    bw.write(K+"\n");
                    bw.flush();
                    Log.d("CECSClient",K);
                    String tuples=br.readLine();
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

            }
        }).start();
    }
    private void show(final String str){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
                    jobject.put("Sig",Base64.encodeToString(EncryptUtils.signature(str.getBytes(),keyPairO.getPrivate()),Base64.NO_WRAP));
                    tuples.put(jobject);
                    fis.close();
                } catch (Exception e){
                    e.printStackTrace();
                }
            } else if(file.isDirectory()){
                getTuples(file.getAbsolutePath());
            }
        }
        return tuples;
    }
}
