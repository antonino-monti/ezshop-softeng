package it.polito.ezshop.blackboxTest;

import it.polito.ezshop.data.EZShop;
import org.junit.Test;

import static org.junit.Assert.*;

public class BB_UnitTesting {
    EZShop ez = new EZShop();

    /*
        Methods to test for unit testing:
        - EZShop:
            * checkBarCodeValidity
            * checkCreditCardValidity
     */

    // --- Test bar code validity --- //
    @Test
    public void test_InvalidShortBarCode() {
        assertFalse(ez.checkBarCodeValidity("425003"));
    }

    @Test
    public void test_InvalidBarCode() {
        assertFalse(ez.checkBarCodeValidity("6291041500218"));
    }

    @Test
    public void test_ValidBarCode() {
        assertTrue(ez.checkBarCodeValidity("6291041500213"));
    }

    @Test
    public void test_InvalidStringCreditCard() {
        assertFalse(ez.checkCreditCardValidity("banana"));
    }

    @Test
    public void test_InvalidCreditCard() {
        assertFalse(ez.checkCreditCardValidity("79927398718"));
    }

    @Test
    public void test_ValidCreditCard() {
        assertTrue(ez.checkCreditCardValidity("79927398713"));
    }
}