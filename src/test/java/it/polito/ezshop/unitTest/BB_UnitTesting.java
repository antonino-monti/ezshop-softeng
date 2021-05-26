package it.polito.ezshop.unitTest;

import it.polito.ezshop.data.BalanceOperation;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.SaleTransaction;
import it.polito.ezshop.data.TicketEntry;
import it.polito.ezshop.data.classes.*;
import it.polito.ezshop.exceptions.InvalidPasswordException;
import it.polito.ezshop.exceptions.InvalidRoleException;
import it.polito.ezshop.exceptions.InvalidUsernameException;
import it.polito.ezshop.exceptions.UnauthorizedException;
import org.junit.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BB_UnitTesting {
    EZShop ez = new EZShop();

    // --- Test bar code validity --- //
    @Test
    public void test_InvalidBarCode() {
        assertFalse(ez.checkBarCodeValidity("6291041500218"));
        assertFalse(ez.checkBarCodeValidity(null));
        assertFalse(ez.checkBarCodeValidity("42"));
        assertFalse(ez.checkBarCodeValidity("62910415002187326548"));
        assertFalse(ez.checkBarCodeValidity("1234a234b"));
    }
    @Test
    public void test_ValidBarCode() {
        assertTrue(ez.checkBarCodeValidity("6291041500213"));
    }
    // --- Test credit card validity --- //
    @Test
    public void test_InvalidCreditCard() {
        assertFalse(ez.checkCreditCardValidity("79927398718"));
        assertFalse(ez.checkCreditCardValidity(null));
        assertFalse(ez.checkCreditCardValidity("cane"));
    }
    @Test
    public void test_ValidCreditCard() {
        assertTrue(ez.checkCreditCardValidity("79927398713"));
    }
    // --- Test file reader --- //
    @Test
    public void test_readFromFile() {
        EZFileReader ez = new EZFileReader();

        Map<String, Double> map = ez.readCreditCards("testFiles/creditCardFile_unitTest.csv");

        assertTrue(map.containsKey("273564276542"));
    }
    @Test
    public void test_writeToFile() {
        EZFileReader ez = new EZFileReader();

        Map<String, Double> map = new HashMap<>();

        map.put("938728567833", 50.00);
        map.put("938728568433", 40.00);
        map.put("273564276542", 16.00);

        ez.setCreditCards(map, "testFiles/creditCardFile_unitTest.csv");

        Map<String, Double> map_res = ez.readCreditCards("testFiles/creditCardFile_unitTest.csv");

        assertTrue(map_res.containsKey("273564276542"));
    }
    @Test
    public void test_CustomerCardVal() {
        assertFalse(ez.checkCustomerCardValidity("Stringadilettere")); //Solo Numeri
        assertFalse(ez.checkCustomerCardValidity("000000")); //lenght<10
        assertFalse(ez.checkCustomerCardValidity(null));
        assertFalse(ez.checkCustomerCardValidity(""));
        assertTrue((ez.checkCustomerCardValidity("5000000001")));
    }
}
