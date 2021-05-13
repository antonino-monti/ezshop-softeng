package it.polito.ezshop.unitTest;

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
    public void test_ShortBarCode() {
        assertFalse(ez.checkBarCodeValidity("42"));
    }

    @Test
    public void test_LongBarCode() {
        assertFalse(ez.checkBarCodeValidity("422398752874689274234"));
    }

    @Test
    public void test_InvalidBarCode() {
        assertFalse(ez.checkBarCodeValidity("6291041500218"));
    }

    @Test
    public void test_ValidBarCode() {
        assertTrue(ez.checkBarCodeValidity("6291041500213"));
    }
    // --- Test credit card validity --- //
    @Test
    public void test_InvalidCreditCard() {
        assertFalse(ez.checkCreditCardValidity("79927398718"));
    }

    @Test
    public void test_ValidCreditCard() {
        assertTrue(ez.checkCreditCardValidity("79927398713"));
    }
}
