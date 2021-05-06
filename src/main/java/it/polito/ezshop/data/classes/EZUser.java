package it.polito.ezshop.data.classes;

import it.polito.ezshop.data.User;

import java.util.Locale;

public class EZUser implements User{
    private Integer userId;
    private String username;
    private String password;
    private UserRole role;

    @Override
    public Integer getId() {
        return userId;
    }

    @Override
    public void setId(Integer id) {
        this.userId=id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
            this.username=username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password=password;

    }

    @Override
    public String getRole() {
        if(this.role==UserRole.MANAGER){
            return "MANAGER";
        }
        if(this.role==UserRole.ADMINISTRATOR){
            return "ADMINISTRATOR";
        }
        if(this.role==UserRole.CASHIER)
            return "CASHIER";
        return "";
    }

    @Override
    public void setRole(String role) {
        if((role).equalsIgnoreCase("MANAGER"))
            this.role=UserRole.MANAGER;
        if((role).equalsIgnoreCase("ADMINISTRATOR"))
            this.role=UserRole.ADMINISTRATOR;
        if((role).equalsIgnoreCase("CASHIER"))
            this.role=UserRole.CASHIER;

    }
}
