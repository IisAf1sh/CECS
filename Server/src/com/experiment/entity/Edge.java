package com.experiment.entity;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;

import com.experiment.util.EncryptUtils;
import com.experiment.util.SPCHS;

import it.unisa.dia.gas.jpbc.Element;


public class Edge{
	public static final int PORT = 8080;
	public static SPCHS spchs;
	public static void main(String[] args) {
		Edge edge=new Edge();
		edge.init();
	}
	
	public void init() {  
        try {  
            ServerSocket serverSocket = new ServerSocket(PORT);  
            Socket c = serverSocket.accept();  
            BufferedReader cbr = new BufferedReader(new InputStreamReader(c.getInputStream())); 
        	String str=cbr.readLine();
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
        	Socket server=new Socket("localhost",8888);
            BufferedWriter sbw = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
            sbw.write(str+"\n");
            sbw.flush();
            server.close();
            while (true) {  
                Socket client = serverSocket.accept();  
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
            	str=cbr.readLine();
                JSONObject jobject = new JSONObject(str);
                System.out.println(jobject);
                String request = jobject.getString("request");
                System.out.println(request);
                switch(request){
                case "uploading request": 
                	String PKO=jobject.getString("PKO");
                	String PKR=jobject.getString("PKR");
                	JSONArray jarray1=jobject.getJSONArray("tuples");
                	for(int i=0;i<jarray1.length();i++) {
                		jobject=jarray1.getJSONObject(i);
                	}
                	Socket server1=new Socket("localhost",8888);
                	BufferedReader sbr1 = new BufferedReader(new InputStreamReader(server1.getInputStream())); 
                    BufferedWriter sbw1 = new BufferedWriter(new OutputStreamWriter(server1.getOutputStream()));
                    
                    sbw1.write(str+"\n");            
                    sbw1.flush();
                    str=sbr1.readLine();
                    cbw.write(str+"\n");
                    cbw.flush();
		            break;
                case "sharing request": 
                	Socket server2=new Socket("localhost",8888);
                	BufferedReader sbr2 = new BufferedReader(new InputStreamReader(server2.getInputStream())); 
                    BufferedWriter sbw2 = new BufferedWriter(new OutputStreamWriter(server2.getOutputStream()));
                    sbw2.write(str+"\n");            
                    sbw2.flush();
                    str=sbr2.readLine();
                    
                    cbw.write(str+"\n");
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
            } catch (Exception e) {  
            } finally {  
                if (client != null) {  
                    try {  
                        client.close();  
                    } catch (Exception e) {
                    	e.printStackTrace();
                    }  
                }  
            } 
        }  
    }
}
