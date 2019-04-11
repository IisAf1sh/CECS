package com.experiment.entity;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
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
        	g.setFromBytes(Base64.getDecoder().decode(jobject.getString("g").getBytes("UTF-8")));
        	spchs.setG(g);
        	Element P=spchs.getG1().newElement();
        	P.setFromBytes(Base64.getDecoder().decode(jobject.getString("P").getBytes("UTF-8")));
        	spchs.setP(P);
        	Element u=spchs.getZr().newElement();
        	u.setFromBytes(Base64.getDecoder().decode(jobject.getString("u").getBytes("UTF-8")));
        	spchs.setU(u);
        	Element Pub=spchs.getG1().newElement();
        	Pub.setFromBytes(Base64.getDecoder().decode(jobject.getString("Pub").getBytes("UTF-8")));
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
                System.out.println(request);
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
                	Element TW=spchs.getG1().newElement();
                	TW.setFromBytes(Base64.getDecoder().decode(jobject.getString("TW").getBytes("UTF-8")));
                	System.out.println("TW"+Arrays.toString(TW.toBytes()));
                	ArrayList<Integer> ID=spchs.search(spchs.getPub(), peks, TW);
                	System.out.println(ID.toString());
                	JSONArray jarray3=new JSONArray();
                	for(int id:ID) {
                		jarray3.put(new JSONObject(tuples.get(id)));
                	}
                	JSONObject jobject3=new JSONObject();
                	jobject3.put("tuples", jarray3);
                	bw.write(jobject3.toString()+"\n");
                	bw.flush();
                	System.out.println(jobject3.toString());
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
