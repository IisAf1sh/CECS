package com.experiment.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.experiment.util.EncryptUtils;
import com.experiment.util.SPCHS;

import it.unisa.dia.gas.jpbc.Element;

public class Edge{
	public static final int PORT = 8080;
	public static SPCHS spchs;
	public static void main(String[] args) {
		Security.setProperty("crypto.policy", "unlimited");
		Edge edge=new Edge();
		edge.init();
	}
	
	public void init() {  
        try {  
        	ServerSocket serverSocket = new ServerSocket(PORT);  
        	while (true) {            
	            Socket client = serverSocket.accept();  
	            BufferedReader cbr = new BufferedReader(new InputStreamReader(client.getInputStream())); 
	        	String str=cbr.readLine();
	        	JSONObject jobject=new JSONObject(str);
	        	System.out.println(jobject.toString());
	        	spchs=new SPCHS();
	        	spchs.setup();
	        	Element g=spchs.getG1().newElement();
	        	g.setFromBytes(Base64.getDecoder().decode(jobject.getString("g").getBytes()));
	        	spchs.setG(g);
	        	Element P=spchs.getG1().newElement();
	        	P.setFromBytes(Base64.getDecoder().decode(jobject.getString("P").getBytes()));
	        	spchs.setP(P);
	        	Element u=spchs.getG1().newElement();
	        	u.setFromBytes(Base64.getDecoder().decode(jobject.getString("u").getBytes()));
	        	spchs.setU(u);
	        	Element Pub=spchs.getG1().newElement();
	        	Pub.setFromBytes(Base64.getDecoder().decode(jobject.getString("Pub").getBytes()));
	        	spchs.setPub(Pub);
	        	Socket server=new Socket("localhost",8888);
	            BufferedWriter sbw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
	            sbw.write(str+"\n");
	            sbw.flush();
	            server.close();
                new HandlerThread(client);  
            } 
        } catch (Exception e) {
        	e.printStackTrace();
        } 
    }  

	private class HandlerThread implements Runnable {  
        private Socket client;  
        public HandlerThread(Socket client) {  
            this.client = client;  
            new Thread(this).start();  
        }  
  
        public void run() {
            try {
            	String str;         
            	BufferedReader cbr = new BufferedReader(new InputStreamReader(client.getInputStream())); 
            	BufferedWriter cbw = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
            	System.out.println("readline");
            	while(true) {
	            	str=cbr.readLine();
	                JSONObject jobject = new JSONObject(str);
	                System.out.println(jobject);
	                String request = jobject.getString("request");
	                System.out.println(request);
	                switch(request){
	                case "uploading request": 
	                	JSONArray jarray=encrypt(jobject);
	                	String PKO=jobject.getString("PKO");
	                	JSONObject jobject1=new JSONObject();
	                	jobject1.put("request", "uploading request");
	                	jobject1.put("PKO",PKO);
	                	jobject1.put("tuples",jarray);
	                	Socket server1=new Socket("localhost",8888);
	                	BufferedReader sbr1 = new BufferedReader(new InputStreamReader(server1.getInputStream())); 
	                    BufferedWriter sbw1 = new BufferedWriter(new OutputStreamWriter(server1.getOutputStream()));
	                    sbw1.write(jobject1.toString()+"\n");            
	                    sbw1.flush();
	                    System.out.println(jobject1.toString());
			            break;
	                case "sharing request": 
	                	Socket server2=new Socket("localhost",8888);
	                	BufferedReader sbr2 = new BufferedReader(new InputStreamReader(server2.getInputStream())); 
	                    BufferedWriter sbw2 = new BufferedWriter(new OutputStreamWriter(server2.getOutputStream()));
	                    sbw2.write(str+"\n");            
	                    sbw2.flush();
	                    str=sbr2.readLine();
	                    System.out.println(str);
	                    JSONObject jobject2=new JSONObject(str);
	                    JSONArray jarray2=decrypt(jobject2,cbw,cbr);
	                    jobject2=new JSONObject();
	                    jobject2.put("tuples", jarray2);
	                    cbw.write(jobject2.toString()+"\n");
	                    cbw.flush();
			            break;		              
	                case "searching request": 
	                	Socket server3=new Socket("localhost",8888);
	                	BufferedReader sbr3 = new BufferedReader(new InputStreamReader(server3.getInputStream())); 
	                    BufferedWriter sbw3 = new BufferedWriter(new OutputStreamWriter(server3.getOutputStream()));
	                    sbw3.write(str+"\n");            
	                    sbw3.flush();
	                    str=sbr3.readLine();
	                    
	                    cbw.write(str+"\n");
	                    cbw.flush();
			            break;		              	              
	                } 
            	}
            } catch (Exception e) {
            	e.printStackTrace();
            } 
        }
        private JSONArray encrypt(JSONObject jobject) throws NoSuchAlgorithmException, InvalidKeyException, JSONException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException {
        	String PKR=jobject.getString("PKR");
        	PublicKey publicKey=EncryptUtils.string2PublicKey(PKR);
        	JSONArray jarray1=new JSONArray();
        	JSONArray jarray=jobject.getJSONArray("tuples");
        	for(int i=0;i<jarray.length();i++) {
        		jobject=jarray.getJSONObject(i);
        		System.out.println(jobject.toString());
        		String data=jobject.getString("data");
        		String keyword=jobject.getString("keywords");
        		String Sig=jobject.getString("Sig");
        		JSONObject jobject1=new JSONObject();
        		JSONArray CW=new JSONArray();
        		JSONObject peks=new JSONObject();
        		SecretKey secretKey= EncryptUtils.setupAES();
        		System.out.println("K:"+Base64.getEncoder().encodeToString(secretKey.getEncoded()));
        		jobject1.put("C", Base64.getEncoder().encodeToString(EncryptUtils.encryptAES(data.getBytes(), secretKey)));
        		jobject1.put("CK", Base64.getEncoder().encodeToString(EncryptUtils.encryptRSA(secretKey.getEncoded(), publicKey)));
        		CW.put(spchs.encryption(spchs.getP(),keyword,spchs.getU()));
        		jobject1.put("CW",CW);
        		jobject1.put("Sig", Sig);
        		jarray1.put(jobject1);
        	}
        	return jarray1;
        }
        private JSONArray decrypt(JSONObject jobject,BufferedWriter bw,BufferedReader br) throws IOException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException, SignatureException {
        	JSONArray jarray=jobject.getJSONArray("tuples");
        	JSONArray jarray1=new JSONArray();
        	for(int i=0;i<jarray.length();i++) {
        		jobject=jarray.getJSONObject(i);
            	String PKO=jobject.getString("PKO");
        		String Sig=jobject.getString("Sig");
        		String CK=jobject.getString("CK");
        		bw.write(CK+"\n");
        		bw.flush();
        		String K=br.readLine();
        		System.out.println(K);
        		String C=jobject.getString("C");
        		byte[] data=EncryptUtils.decryptAES(Base64.getDecoder().decode(C.getBytes()), EncryptUtils.string2SecretKey(K));
        		if(EncryptUtils.verify(data, EncryptUtils.string2PublicKey(PKO), Sig.getBytes())) {
        			jarray1.put(new JSONObject().put("data", data));
        		}
        	}
        	return jarray1;
        }
    }
}
