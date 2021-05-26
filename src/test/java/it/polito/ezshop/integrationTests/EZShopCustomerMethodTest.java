package it.polito.ezshop.integrationTests;
import it.polito.ezshop.data.EZShop;
import it.polito.ezshop.data.classes.EZDatabase;
import it.polito.ezshop.data.classes.EZProductType;
import it.polito.ezshop.exceptions.*;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.*;



public class EZShopCustomerMethodTest {

    EZShop sp = new EZShop();

    @Before
    public void resetShop() {
        sp.reset();

    }

    @Test
    public void Test_defineCustomer(){
        assertThrows(InvalidCustomerNameException.class,()->{sp.defineCustomer(null);});
        assertThrows(InvalidCustomerNameException.class,()->{sp.defineCustomer("");});


        try {
            assertThrows(UnauthorizedException.class,()->{sp.defineCustomer("Frank");});
            sp.createUser("fridanco1","pass","Cashier");
            sp.login("fridanco1","pass");
            assertEquals(1,(int)sp.defineCustomer("Frank"));
            assertEquals(2,(int)sp.defineCustomer("Frank1"));
            assertEquals(3,(int)sp.defineCustomer("Frank2"));
            assertEquals("Frank",sp.getCustomer(1).getCustomerName());
            assertEquals("Frank1",sp.getCustomer(2).getCustomerName());
            assertEquals("Frank2",sp.getCustomer(3).getCustomerName());

        } catch (InvalidUsernameException | UnauthorizedException | InvalidPasswordException | InvalidCustomerNameException | InvalidRoleException | InvalidCustomerIdException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    @Test
    public void Test_ModifyCustomer(){
        assertThrows(InvalidCustomerNameException.class,()->{sp.defineCustomer(null);});
        assertThrows(InvalidCustomerNameException.class,()->{sp.defineCustomer("");});

        assertThrows(InvalidCustomerIdException.class,()->{sp.modifyCustomer(3,"FrankN","5000000001");});


        try {
            sp.createUser("fridanco1","pass","Cashier");
            sp.login("fridanco1","pass");
            String name = "Frank1";
            assertEquals((Integer) 1, sp.defineCustomer(name));
            String newCustomerCard = sp.createCard();
            sp.attachCardToCustomer(newCustomerCard,1);
            assertEquals( 2,(int)sp.defineCustomer("Frank2"));
            sp.attachCardToCustomer( sp.createCard(),2);
            sp.logout();
            assertThrows(UnauthorizedException.class,()->{sp.modifyCustomer(1,"FrankN","5000000001");});

            sp.login("fridanco1","pass");


            assertThrows(InvalidCustomerCardException.class,()->{sp.modifyCustomer(1,"FrankN","55000000001");});
            assertThrows(InvalidCustomerCardException.class,()->{sp.modifyCustomer(1,"FrankN","55000001");});
            assertThrows(InvalidCustomerCardException.class,()->{sp.modifyCustomer(1,"FrankN","Ciao");});


            assertFalse(sp.modifyCustomer(2,"Paolo","5000000001"));

            assertTrue(sp.modifyCustomer(2,"Paolo",""));
            assertEquals("",sp.getCustomer(2).getCustomerCard());
            assertEquals("Paolo",sp.getCustomer(2).getCustomerName());

            assertTrue(sp.modifyCustomer(2,"Giovanni","5000000003"));
            assertEquals("5000000003",sp.getCustomer(2).getCustomerCard());
            assertEquals("Giovanni",sp.getCustomer(2).getCustomerName());

            assertTrue(sp.modifyCustomer(2,"Federico",null));
            assertEquals("5000000003",sp.getCustomer(2).getCustomerCard());
            assertEquals("Federico",sp.getCustomer(2).getCustomerName());

        } catch (InvalidUsernameException | UnauthorizedException | InvalidPasswordException | InvalidCustomerNameException | InvalidRoleException | InvalidCustomerIdException | InvalidCustomerCardException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    @Test
    public void Test_deleteCustomer(){
        assertThrows(InvalidCustomerIdException.class,()->{sp.deleteCustomer(null);});
        assertThrows(InvalidCustomerIdException.class,()->{sp.deleteCustomer(0);});
        assertThrows(InvalidCustomerIdException.class,()->{sp.deleteCustomer(-3);});


        try {
            assertThrows(UnauthorizedException.class,()->{sp.deleteCustomer(1);});
            sp.createUser("fridanco1","pass","Cashier");
            sp.login("fridanco1","pass");
            assertFalse(sp.deleteCustomer(2));

            assertEquals(1,(int)sp.defineCustomer("Frank"));
            assertEquals(2,(int)sp.defineCustomer("Frank1"));
            assertEquals(3,(int)sp.defineCustomer("Frank2"));

            assertTrue(sp.deleteCustomer(2));

            assertEquals("Frank",sp.getCustomer(1).getCustomerName());
            assertEquals("Frank2",sp.getCustomer(3).getCustomerName());




        } catch (InvalidUsernameException | UnauthorizedException | InvalidPasswordException | InvalidCustomerNameException | InvalidRoleException | InvalidCustomerIdException e) {
            e.printStackTrace();
            System.out.println(e);
        }
    }
    @Test
    public void Test_getCustomer(){
        assertThrows(InvalidCustomerIdException.class,()->{sp.getCustomer(null);});
        assertThrows(InvalidCustomerIdException.class,()->{sp.getCustomer(0);});
        assertThrows(InvalidCustomerIdException.class,()->{sp.getCustomer(-3);});

        try {
            assertThrows(UnauthorizedException.class,()->{sp.getCustomer(1);});
            sp.createUser("fridanco1","pass","Cashier");
            sp.login("fridanco1","pass");

            assertNull(sp.getCustomer(1));
            assertNull(sp.getCustomer(2));

            assertEquals(1,(int)sp.defineCustomer("Frank"));
            assertEquals(2,(int)sp.defineCustomer("Frank1"));
            assertEquals(3,(int)sp.defineCustomer("Frank2"));



            assertEquals("Frank",sp.getCustomer(1).getCustomerName());
            assertEquals("Frank2",sp.getCustomer(3).getCustomerName());




        } catch (InvalidUsernameException | UnauthorizedException | InvalidPasswordException | InvalidCustomerNameException | InvalidRoleException | InvalidCustomerIdException e) {
            e.printStackTrace();
            System.out.println(e);
        }

    }
    @Test
    public void Test_getAllCustomer(){
        try {
            assertThrows(UnauthorizedException.class,()->{sp.getAllCustomers();});
            sp.createUser("fridanco1","pass","Cashier");
            sp.login("fridanco1","pass");

            assertNull(sp.getCustomer(1));
            assertNull(sp.getCustomer(2));

            assertEquals(1,(int)sp.defineCustomer("Frank"));
            assertEquals(2,(int)sp.defineCustomer("Frank1"));
            assertEquals(3,(int)sp.defineCustomer("Frank2"));



            assertEquals("Frank",sp.getCustomer(1).getCustomerName());
            assertEquals("Frank2",sp.getCustomer(3).getCustomerName());




        } catch (InvalidUsernameException | UnauthorizedException | InvalidPasswordException | InvalidCustomerNameException | InvalidRoleException | InvalidCustomerIdException e) {
            e.printStackTrace();
            System.out.println(e);
        }

    }
    @Test
    public void test_CreateCard()  {
        try {
            assertThrows(UnauthorizedException.class,()->{sp.createCard();});
            sp.createUser("fridanco","pass", "Cashier");
            sp.login("fridanco","pass");
            String CustomerCard = sp.createCard();
            assertTrue(CustomerCard.matches("[0-9]{10}"));
        } catch (InvalidUsernameException | InvalidPasswordException | InvalidRoleException | UnauthorizedException e) {
            e.printStackTrace();
        }

    }
    @Test
    public void test_attachCardToCustomer() {
        assertThrows(UnauthorizedException.class, () -> {
            sp.attachCardToCustomer("5000000003", 1);
        });
        try {
            sp.createUser("fridanco1", "pass", "Cashier");
            sp.login("fridanco1", "pass");


            assertThrows(InvalidCustomerIdException.class, () -> {
                sp.attachCardToCustomer("5000000003", null);
            });
            assertThrows(InvalidCustomerIdException.class, () -> {
                sp.attachCardToCustomer("5000000003", 0);
            });
            assertThrows(InvalidCustomerIdException.class, () -> {
                sp.attachCardToCustomer("5000000003", -3);
            });

            assertThrows(InvalidCustomerCardException.class, () -> {
                sp.attachCardToCustomer(null, 1);
            });
            assertThrows(InvalidCustomerCardException.class, () -> {
                sp.attachCardToCustomer("5000003", 1);
            });

            assertEquals(1, (int) sp.defineCustomer("Frank"));
            assertFalse(sp.attachCardToCustomer("5000000002", 2));
            assertEquals(2, (int) sp.defineCustomer("Frank1"));
            assertEquals(3, (int) sp.defineCustomer("Frank2"));
            assertTrue(sp.attachCardToCustomer("5000000001", 1));
            assertFalse(sp.attachCardToCustomer("5000000001", 2));
            assertTrue(sp.attachCardToCustomer("5000000003", 3));


        } catch (InvalidUsernameException | InvalidCustomerCardException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidCustomerNameException | InvalidCustomerIdException e) {
            e.printStackTrace(); }
    }

    @Test
    public void test_modifyPointsOnCard() {
        assertThrows(UnauthorizedException.class, () -> {
            sp.modifyPointsOnCard("5000000003", 1);
        });
        try {
            sp.createUser("fridanco1", "pass", "Cashier");
            sp.login("fridanco1", "pass");


            assertThrows(InvalidCustomerCardException.class, () -> {
                sp.modifyPointsOnCard(null, 1);
            });
            assertThrows(InvalidCustomerCardException.class, () -> {
                sp.modifyPointsOnCard("50000003", 4);
            });


            assertEquals(1, (int) sp.defineCustomer("Frank"));
            assertEquals(2, (int) sp.defineCustomer("Frank1"));
            assertEquals(3, (int) sp.defineCustomer("Frank2"));
            assertTrue(sp.attachCardToCustomer("5000000001", 1));
            assertTrue(sp.attachCardToCustomer("5000000002", 2));
            assertTrue(sp.attachCardToCustomer("5000000003", 3));

            assertFalse(sp.modifyPointsOnCard("5000000001",-10));
            assertTrue(sp.modifyPointsOnCard("5000000001",10));
            assertEquals(10,(int)sp.getCustomer(1).getPoints());

        } catch (InvalidUsernameException | InvalidCustomerCardException | InvalidPasswordException | InvalidRoleException | UnauthorizedException | InvalidCustomerNameException | InvalidCustomerIdException e) {
            e.printStackTrace(); }
    }



}

