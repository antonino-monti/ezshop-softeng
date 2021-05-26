# Integration and API Test Documentation

Authors: Giulia D'Ascenzi, Francesco di Franco, Antonino Monti

Date: 26/05/2021

Version: 1.0

# Contents

- [Dependency graph](#dependency graph)

- [Integration approach](#integration)

- [Tests](#tests)

- [Scenarios](#scenarios)

- [Coverage of scenarios and FR](#scenario-coverage)
- [Coverage of non-functional requirements](#nfr-coverage)

# Dependency graph 

     <report the here the dependency graph of the classes in EzShop, using plantuml>

![dependency graph](TestingPNGs/dependencyGraph.png)

# Integration approach

    <Write here the integration sequence you adopted, in general terms (top down, bottom up, mixed) and as sequence
    (ex: step1: class A, step 2: class A+B, step 3: class A+B+C, etc)> 
    <Some steps may  correspond to unit testing (ex step1 in ex above), presented in other document UnitTestReport.md>
    <One step will  correspond to API testing>

We adopted a **bottom-up** approach for testing, in which we started from the classes and methods which did not have any significant dependencies (i.e. unit testing), then tested the DB class and finally the EZShop API methods. The testing sequence is described in the section below.

#  Tests

   <define below a table for each integration step. For each integration step report the group of classes under test, and the names of
     JUnit test cases applied to them> JUnit test classes should be here src/test/java/it/polito/ezshop

## Step 1
| Classes  | JUnit test cases |
|--|--|
|EZBalanceOperation|WB_UnitTesting.test_BalanceOperation|
|EZSaleTransaction|WB_UnitTesting.test_SaleTransaction|
|EZTicketEntry|WB_UnitTesting.test_TicketEntry|
|EZReturnTransaction|WB_UnitTesting.test_ReturnTransaction|
|EZOrder|WB_UnitTesting.test_OrderClassMethods|
|EZUser|WB_UnitTesting.test_UserClassMethods|
|EZCustomer, EZCustomerCard|WB_UnitTesting.test_CustomerClassMethod|
|EZProductType|WB_UnitTesting.test_ProductTypeMethod|
|EZFileReader|BB_UnitTesting.test_readFromFile, BB_UnitTesting.test_writeToFile|


## Step 2
| Classes  | JUnit test cases |
|--|--|
|EZDatabase + classes from step1|TestDBClass|


## Step 3

| Classes  | JUnit test cases |
|--|--|
|EZShop + class of step2 and step1|IntegrationTest_Accounting|
||IntegrationTest_Payments|
||IntegrationTest_ReturnTransaction|
||IntegrationTest_SaleTransaction|
||EZShopOrderMethodsTest|
||EZShopProductTypeTest|
||EZShopUserMethodsTest|
||...|
||...|


# Scenarios

No additional scenarios defined.

# Coverage of Scenarios and FR

<Report in the following table the coverage of  scenarios (from official requirements and from above) vs FR. 
Report also for each of the scenarios the (one or more) API JUnit tests that cover it. >


| Scenario ID | Functional Requirements covered | JUnit  Test(s) |
| ----------- | ------------------------------- | ----------- |
|  1.1      | FR3.1                 | EZShopProductTypeTest.Test_createProductType()            |
|  1.2      | FR3.1                         |         EZShopProductTypeTest.Test_getUpdatePosition()    |
| 1.3      |      FR3.1                           |         EZShopProductTypeTest.Test_updateProduct()    |
| 2.1      | FR1.1 | EZShopUserMethodsTest.createUserValid() |
| 2.2    | FR1.2, FR1.4 | EZShopUserMethodsTest.testDeleteUserValid() |
| 2.3      | FR1.5 | EZShopUserMethodsTest.testUpdateUserRightsValid() |
| 3.1 | FR4.4 | EZShopOrderMethodsTest.IssueOrderValid() |
| 3.2 | FR4.5 | EZShopOrderMethodsTest.PayOrderOrderValid() |
| 3.3 | FR4.6 | EZShopOrderMethodsTest.RecordOrderArrivalValid() |
| 4.1 | FR5.1 | EZShopCustomerMethodTest.Test_defineCustomer() |
| 4.2 | FR5.6 | EZShopCustomerMethodTest.test_attachCardToCustomer()|
| 4.3 | FR5.1| EZShopCustomerMethodTest.Test_ModifyCustomer() |    
| 4.4 | FR5.1| EZShopCustomerMethodTest.Test_ModifyCustomer()|
| 5.1 | FR1.4, FR1.5 | EZShopUserMethodsTest.testLoginValid() |
| 5.2 | " | EZShopUserMethodsTest.testLogoutLoggedUsers() |
| 6.1 | FR6, FR7, FR8 | IntegrationTest_SaleTransaction.test_SaleTransactions, <br/>IntegrationTest_Payments.test_paymentTest |
| 6.2 | " | " |
| 6.3 | " | " |
| 6.4 | FR5, FR6, FR7, FR8 | " |
| 6.5 | FR6 | " |
| 6.6 | FR8 | "                                                            |
| 7.1 | FR7 | IntegrationTest_Payments.test_paymentTest |
| 7.2 | FR7 | " |
| 7.3 | FR7 | " |
| 7.4 | FR7 | "                                                            |
| 8.1 | FR6, FR7, FR8 | IntegrationTest_ReturnTransaction.test_ReturnTransactions, <br/>IntegrationTest_Payments.test_paymentTest |
| 8.2 | " | " |
| 9.1 | FR8 | IntegrationTest_Accounting.test_AccountingMethods |
| 10.1 | FR7 | IntegrationTest_Payments.test_paymentTest |
| 10.2 | FR7 | " |

# Coverage of Non Functional Requirements

<Report in the following table the coverage of the Non Functional Requirements of the application - only those that can be tested with automated testing frameworks.>

| Non Functional Requirement | Test name                                                    |
| -------------------------- | ------------------------------------------------------------ |
| NFR4                       | BBUnitTesting.test_InvalidBarCode, BBUnitTesting.test_ValidBarCode |
| NFR5                       | BBUnitTesting.test_InvalidCreditCard, BBUnitTesting.test_ValidCreditCard |
| NFR6                       | BBUnitTesting.test_CustomerCardVal              |

