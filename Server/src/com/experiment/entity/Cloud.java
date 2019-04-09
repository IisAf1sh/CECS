package com.experiment.entity;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.experiment.util.SPCHS;

import it.unisa.dia.gas.jpbc.Element;

public class Cloud {
	private static final int PORT = 8888;
	private static HashMap<String, String> peks=new HashMap<>();
	private static ArrayList<String> tuples=new ArrayList<>();
	private static int id=0;
	private static SPCHS spchs;
	public static void main(String[] args) {
		Cloud cloud=new Cloud();
		cloud.init();
	}
	
	public void init() {  
        try {  
            ServerSocket serverSocket = new ServerSocket(PORT); 
            Socket s = serverSocket.accept();  
            BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream())); 
        	String str=br.readLine();
        	JSONObject jobject=new JSONObject(str);
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
            while (true) {  
                Socket socket = serverSocket.accept();  
                new HandlerThread(socket);  
            }  
        } catch (Exception e) {
        	e.printStackTrace();
        }  
    }  

	private class HandlerThread implements Runnable {  
        private Socket socket;  
        public HandlerThread(Socket client) {  
            socket = client;  
            new Thread(this).start();  
        }  
  
        public void run() {   
            try {  
            	String str;         
            	BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream())); 
            	BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            	str=br.readLine();
                JSONObject jobject = new JSONObject(str);
                String request = jobject.getString("request");
                switch(request){
                case "uploading request": 
                	String PKO=jobject.getString("PKO");
                	JSONArray jarray1=jobject.getJSONArray("tuples");
                	for(int i=0;i<jarray1.length();i++) {
                		jobject=jarray1.getJSONObject(i);
                		JSONArray CW=jobject.getJSONArray("CW");
                		JSONObject peksObject;
                		for(int j=0;j<CW.length();j++) {
                			peksObject=CW.getJSONObject(j);
                			peksObject.put("id", id);
                			peks.put(peksObject.getString("C1"), peksObject.toString());
                		}
                		jobject.put("PKO", PKO);
                		tuples.add(jobject.toString());
                		id++;
                		System.out.println(jobject.toString());
                	}
		            break;		
                case "sharing request": 
                	JSONArray jarray2=new JSONArray();
                	for(String s:tuples) {
                		jarray2.put(new JSONObject(s));
                	}
                	JSONObject jobject2=new JSONObject();
                	jobject2.put("tuples", jarray2);
                	bw.write(jobject2.toString()+"\n");
                	bw.flush();
                	System.out.println(jobject2.toString());
		            break;		              
                case "searching request": 
                	
		            break;		              
                } 
            } catch (Exception e) {  
            } finally {  
                if (socket != null) {  
                    try {  
                        socket.close();  
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }  
                }  
            } 
        }  
    }
}
