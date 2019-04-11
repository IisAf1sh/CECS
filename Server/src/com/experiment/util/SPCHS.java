package com.experiment.util;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

/**
 * Created by Administrator on 2019/4/8.
 */

public class SPCHS {
    private Pairing pairing;
    private Field G1,GT,Zr;
    private Element g,s,P,u,Pub,Pt;
    public void setup(){
        pairing= PairingFactory.getPairing("resource/a.properties");
        PairingFactory.getInstance().setUsePBCWhenPossible(true);
        G1=pairing.getG1();
        GT=pairing.getGT();
        Zr=pairing.getZr();
        g=G1.newRandomElement().getImmutable();
        s=Zr.newRandomElement().getImmutable();
        P=g.powZn(s).getImmutable();
        u=Zr.newRandomElement().getImmutable();
        Pt=null;
        Pub=g.powZn(u).getImmutable();
    }
    public JSONObject encryption(Element P,String W,Element u){
        JSONObject jobject=new JSONObject();
        Element r=Zr.newRandomElement().getImmutable();
        if(Pt==null){
            Pt=GT.newRandomElement().getImmutable();
            try{
                byte[] bytes=EncryptUtils.hash(W.getBytes());
                Element HW=G1.newElement().setFromHash(bytes,0, bytes.length);
                Element C1=pairing.pairing(P,HW).powZn(u);
                Element C2=g.powZn(r);
                System.out.println("Pt"+pairing.pairing(P,HW).powZn(r).toString());
                Element C3=pairing.pairing(P,HW).powZn(r).mul(Pt);
                jobject.put("C1",new String(Base64.getEncoder().encode(C1.toBytes()),"UTF-8"));
                jobject.put("C2",new String(Base64.getEncoder().encode(C2.toBytes()),"UTF-8"));
                jobject.put("C3",new String(Base64.getEncoder().encode(C3.toBytes()),"UTF-8"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            Element R=GT.newRandomElement().getImmutable();
            try{
                byte[] bytes=EncryptUtils.hash(W.getBytes());
                Element HW=G1.newElement().setFromHash(bytes,0, bytes.length);
                Element C1=Pt.duplicate();
                System.out.println("C1"+C1.toString());
                Element C2=g.powZn(r);
                System.out.println("Pt"+pairing.pairing(P,HW).powZn(r).toString());
                Element C3=pairing.pairing(P,HW).powZn(r).mul(R);
                Pt=R.duplicate();
                jobject.put("C1",new String(Base64.getEncoder().encode(C1.toBytes()),"UTF-8"));
                jobject.put("C2",new String(Base64.getEncoder().encode(C2.toBytes()),"UTF-8"));
                jobject.put("C3",new String(Base64.getEncoder().encode(C3.toBytes()),"UTF-8"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        return jobject;
    }
    public Element trapdoor(Element s,String W){
        Element TW=null;
        try {
            byte[] bytes = EncryptUtils.hash(W.getBytes());
            TW= G1.newElement().setFromHash(bytes,0,bytes.length).powZn(s).getImmutable();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return TW;
    }
    public ArrayList<Integer> search(Element Pub,HashMap<String,String> CW,Element TW) throws UnsupportedEncodingException{
        ArrayList<Integer> id=new ArrayList<>();
        Element Pt2=pairing.pairing(Pub,TW);
        String str;
        while(CW.containsKey((str=new String(Base64.getEncoder().encode(Pt2.toBytes()),"UTF-8")))){
            try {
                JSONObject jobject = new JSONObject(CW.get(str));
                id.add(jobject.getInt("id"));
                Element C2=G1.newElement();
                C2.setFromBytes(Base64.getDecoder().decode(jobject.getString("C2").getBytes("UTF-8")));
                Element C3=GT.newElement();
                C3.setFromBytes(Base64.getDecoder().decode(jobject.getString("C3").getBytes("UTF-8")));
                System.out.println("Pt"+pairing.pairing(C2,TW).toString());
                Pt2=C3.div(pairing.pairing(C2,TW));
                Element Pt=GT.newElement();
                Pt.setFromBytes(Base64.getDecoder().decode(str));
                System.out.println(Pt.toString());
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        Element Pt=GT.newElement();
        Pt.setFromBytes(Base64.getDecoder().decode(str));
        System.out.println(Pt.toString());
        return id;
    }

    public Pairing getPairing() {
        return pairing;
    }

    public void setPairing(Pairing pairing) {
        this.pairing = pairing;
    }

    public Field getG1() {
        return G1;
    }

    public void setG1(Field g1) {
        G1 = g1;
    }

    public Field getGT() {
        return GT;
    }

    public void setGT(Field GT) {
        this.GT = GT;
    }

    public Field getZr() {
        return Zr;
    }

    public void setZr(Field zr) {
        Zr = zr;
    }

    public Element getG() {
        return g;
    }

    public void setG(Element g) {
        this.g = g;
    }

    public Element getS() {
        return s;
    }

    public void setS(Element s) {
        this.s = s;
    }

    public Element getP() {
        return P;
    }

    public void setP(Element p) {
        P = p;
    }

    public Element getU() {
        return u;
    }

    public void setU(Element u) {
        this.u = u;
    }

    public Element getPub() {
        return Pub;
    }

    public void setPub(Element pub) {
        Pub = pub;
    }

    public Element getPt() {
        return Pt;
    }

    public void setPt(Element pt) {
        Pt = pt;
    }
}