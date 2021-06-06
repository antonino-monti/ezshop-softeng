package it.polito.ezshop.data.classes;

import it.polito.ezshop.data.ReturnTransaction;

import java.util.HashMap;
import java.util.Map;

// TODO: AGGIUNGERE LISTA DI PRODOTTI ASSOCIATI AL RIMBORSO
public class EZReturnTransaction implements ReturnTransaction{
    private int saleTransactionID;
    private int returnID;
    // entries: (productCode, amount)
    private Map<String, Integer> productMap;
    /*
        Values:
        * OPEN
        * CLOSED
        * PAID
     */
    private String status;
    private double money;

    public EZReturnTransaction(int stID, int retID) {
        this.saleTransactionID = stID;
        this.returnID = retID;
        this.productMap = new HashMap<>();
        this.status = "OPEN";
    }

    public EZReturnTransaction(int stID, int retID, String status) {
        this.saleTransactionID = stID;
        this.returnID = retID;
        this.productMap = new HashMap<>();
        this.status = status;
    }

    @Override
    public int getSaleTransactionID() {
        return this.saleTransactionID;
    }

    @Override
    public void setSaleTransactionID(int id) {
        this.saleTransactionID = id;
    }

    @Override
    public int getReturnID() {
        return this.returnID;
    }

    @Override
    public void setReturnID(int id) {
        this.returnID = id;
    }

    @Override
    public Map<String, Integer> getMapOfProducts() {
        return this.productMap;
    }

    @Override
    public void setMapOfProducts(Map<String, Integer> map) {
        this.productMap = map;
    }

    @Override
    public String getStatus() {
        return this.status;
    }

    @Override
    public void setStatus(String s) {
        if (!s.equalsIgnoreCase("OPEN")
            && !s.equalsIgnoreCase("CLOSED")
            && !s.equalsIgnoreCase("PAID")) {
            return;
        }

        this.status = s.toUpperCase();
    }

    @Override
    public double getMoneyReturned() {
        return this.money;
    }

    @Override
    public void setMoneyReturned(double m) {
        this.money = m;
    }
}
