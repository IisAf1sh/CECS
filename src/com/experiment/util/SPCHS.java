package com.experiment.util;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.ArrayList;
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
        pairing= PairingFactory.getPairing("a.properties");
        PairingFactory.getInstance().setUsePBCWhenPossible(true);
        G1=pairing.getG1();
        GT=pairing.getGT();
        Zr=pairing.getZr();
        g=G1.newRandomElement().getImmutable();
        s=G1.newRandomElement().getImmutable();
        P=g.powZn(s);
        u=Zr.newRandomElement().getImmutable();
        Pt=null;
        Pub=g.powZn(u);
    }
    public JSONObject encryption(String W,Element Pt){
        JSONObject jobject=new JSONObject();
        Element r=Zr.newRandomElement().getImmutable();
        if(Pt==null){
            Pt=G1.newRandomElement().getImmutable();
            try{
                byte[] bytes=EncryptUtils.hash(W.getBytes());
                Element HW=G1.newElement().setFromHash(bytes,0, bytes.length);
                Element C1=pairing.pairing(P,HW).powZn(u);
                Element C2=g.powZn(r);
                Element C3=pairing.pairing(P,HW).powZn(r).mul(Pt);
                jobject.put("C1",C1);
                jobject.put("C2",C2);
                jobject.put("C3",C3);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            Element R=G1.newRandomElement().getImmutable();
            try{
                byte[] bytes=EncryptUtils.hash(W.getBytes());
                Element HW=G1.newElement().setFromHash(bytes,0, bytes.length);
                Element C1=Pt;
                Element C2=g.powZn(r);
                Element C3=pairing.pairing(P,HW).powZn(r).mul(R);
                jobject.put("C1",C1);
                jobject.put("C2",C2);
                jobject.put("C3",C3);
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
            TW= G1.newElement().setFromHash(bytes,0,bytes.length);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return TW;
    }
    public ArrayList search(Element Pub,HashMap<String,String> CW,Element TW){
        ArrayList<Integer> id=new ArrayList<>();
        Element Pt2=pairing.pairing(Pub,TW);
        while(CW.containsKey(Pt2.toBytes().toString())){
            try {
                JSONObject jobject = new JSONObject(CW.get(Pt2.toBytes().toString()));
                id.add(jobject.getInt("id"));
                Element C2=G1.newElement();
                C2.setFromBytes(jobject.getString("C2").getBytes());
                Element C3=G1.newElement();
                C3.setFromBytes(jobject.getString("C3").getBytes());
                Pt2=pairing.pairing(C2,TW).pow(new BigInteger("-1")).mul(C3);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
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