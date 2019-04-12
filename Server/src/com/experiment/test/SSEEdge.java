package com.experiment.test;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;

public class SSEEdge {
	public void start(int port) {
		try {
			KeyGenerator keyGenerator =KeyGenerator.getInstance("HmacSHA256");
			SecretKey secretKey1 =keyGenerator.generateKey();
			SecretKey secretKey2=keyGenerator.generateKey();
			ServerSocket serverSocket=new ServerSocket(port);
			Socket socket=serverSocket.accept();
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			bw.write(new String(Base64.getEncoder().encode(secretKey1.getEncoded()),"UTF-8")+"\n");
			bw.write(new String(Base64.getEncoder().encode(secretKey2.getEncoded()),"UTF-8")+"\n");
			bw.flush();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public byte[][] trapdoor(SecretKey k1,SecretKey k2,String keyword) throws NoSuchAlgorithmException, InvalidKeyException{
		byte[][] TW=new byte[2][];
		Mac hmac=Mac.getInstance("HmacSHA256");    
	    hmac.init(k1);       
	    TW[0] = hmac.doFinal(keyword.getBytes());   
	    hmac.init(k2);
	    TW[1]=hmac.doFinal(keyword.getBytes());
		return TW;
	}
}
