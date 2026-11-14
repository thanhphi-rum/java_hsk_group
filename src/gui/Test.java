package gui;

import connectDB.ConnectDB;

public class Test {
    public static void main(String[] args) {
        new PhieuDatVe_GUI(ConnectDB.getConnection()).setVisible(true);
    }
}