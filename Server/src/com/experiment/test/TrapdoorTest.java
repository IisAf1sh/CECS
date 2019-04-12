package com.experiment.test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class TrapdoorTest {
	public static void main(String[] args) {
		for(int i=8011;i<8021;i++) {
			TrapdoorTest trapdoorTest=new TrapdoorTest();
			trapdoorTest.new HandlerThread(i);
		}
		SSEEdge edge=new SSEEdge();
		long startTime = System.currentTimeMillis();
		for(int i=8011;i<8021;i++) {
			try {
				Socket socket=new Socket("localhost",i);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				SecretKey k1=new SecretKeySpec(Base64.getDecoder().decode(br.readLine().getBytes("UTF-8")),"HmacSHA256");
				SecretKey k2=new SecretKeySpec(Base64.getDecoder().decode(br.readLine().getBytes("UTF-8")),"HmacSHA256");
				edge.trapdoor(k1, k2, "keyword");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("SSETrapdoor:"+(endTime-startTime)+"ms");
	}
	private class HandlerThread implements Runnable {
        private int port;
        public HandlerThread(int port) {
            this.port = port;
            new Thread(this).start();
        }
        public void run() {
        	SSEEdge edge=new SSEEdge();
        	edge.start(port);
        }
    }
}
