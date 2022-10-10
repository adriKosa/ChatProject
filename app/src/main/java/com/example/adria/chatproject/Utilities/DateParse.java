package com.example.adria.chatproject.Utilities;

import java.text.SimpleDateFormat;
import java.util.Date;


public class DateParse {
    private static SimpleDateFormat format = new SimpleDateFormat("kk:mm dd.MM");

    public static String convertDateToString(Date date){
        String strDate = "";
        strDate = format.format(date);
        return strDate;
    }
}
