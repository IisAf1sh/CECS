package com.experiment.entity;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

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
        	System.out.println(str);
        	JSONObject jobject=new JSONObject(str);
        	spchs=new SPCHS();
        	spchs.setup();
        	Element g=spchs.getG1().newElement();
        	g.setFromBytes(jobject.getString("g").getBytes());
        	spchs.setG(g);
        	g.setFromBytes(jobject.getString("P").getBytes());
        	spchs.setP(g);
        	g.setFromBytes(jobject.getString("u").getBytes());
        	spchs.setU(g);
        	g.setFromBytes(jobject.getString("Pub").getBytes());
        	spchs.setPub(g);
        	g.setFromBytes(jobject.getString("Pt").getBytes());
        	spchs.setPt(g);
        	System.out.println(spchs.getG().toBytes().toString());
        	System.out.println(spchs.getPub().toBytes().toString());
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
                		tuples.add(jobject.toString());
                		id++;
                		System.out.println(jobject.toString());
                	}
		            break;		
                case "sharing request": 
                	JSONArray jarray2=new JSONArray();
                	jarray2.put(tuples);
                	JSONObject json=new JSONObject();
                	json.put("tuples", jarray2);
                	bw.write(json.toString()+"\n");
                	bw.flush();
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
