package com.doc.appointment;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class AppointmentUtils {

    public static String convertDateToWords(String date) {
        LocalDate localDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String dayOfWeek = localDate.getDayOfWeek().toString().toLowerCase();
        dayOfWeek = Character.toUpperCase(dayOfWeek.charAt(0)) + dayOfWeek.substring(1);

        String dayOfMonth = getDayWithSuffix(localDate.getDayOfMonth());
        String month = localDate.getMonth().toString().toLowerCase();
        month = Character.toUpperCase(month.charAt(0)) + month.substring(1);

        String yearInWords = convertYearToWords(localDate.getYear());

        return String.format("%s, %s %s %s", dayOfWeek, dayOfMonth, month, yearInWords);
    }

    private static String getDayWithSuffix(int day) {
        if (day >= 11 && day <= 13) return day + "th";
        switch (day % 10) {
            case 1:  return day + "st";
            case 2:  return day + "nd";
            case 3:  return day + "rd";
            default: return day + "th";
        }
    }

    private static String convertYearToWords(int year) {
        String[] ones = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};
        String[] teens = {"eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
        String[] tens = {"", "ten", "twenty", "thirty", "forty", "fifty", "sixty", "seventy", "eighty", "ninety"};

        int firstTwoDigits = year / 1000; // Extract thousands
        int secondTwoDigits = (year / 100) % 10; // Extract hundreds
        int lastTwoDigits = year % 100; // Extract last two digits

        StringBuilder yearInWords = new StringBuilder();

        if (firstTwoDigits > 0) {
            yearInWords.append(ones[firstTwoDigits]).append(" thousand");
        }

        if (secondTwoDigits > 0) {
            yearInWords.append(" ").append(ones[secondTwoDigits]).append(" hundred");
        }

        if (lastTwoDigits > 0) {
            if (lastTwoDigits > 10 && lastTwoDigits < 20) {
                yearInWords.append(" ").append(teens[lastTwoDigits - 11]);
            } else {
                yearInWords.append(" ").append(tens[lastTwoDigits / 10]);
                if (lastTwoDigits % 10 != 0) {
                    yearInWords.append(" ").append(ones[lastTwoDigits % 10]);
                }
            }
        }

        return yearInWords.toString().trim();
    }

    public static String convertDate(String ddMM) {
        int year = LocalDate.now().getYear(); // Assume current year
        String formattedDate = "";
        try {
            LocalDate date = LocalDate.parse(ddMM + year, DateTimeFormatter.ofPattern("ddMMyyyy"));
            formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            System.out.println("Converted Date: " + formattedDate);
        } catch (DateTimeParseException e) {
            System.out.println("Invalid Date Format");
        }
        return formattedDate;
    }

    // Validates a 4-digit time in HHMM format.
    public static boolean isValidTime(String digits) {
        if (digits.length() != 4 || !digits.matches("\\d{4}")) {
            return false;
        }
        int hour = Integer.parseInt(digits.substring(0, 2));
        int minute = Integer.parseInt(digits.substring(2, 4));
        return (hour >= 0 && hour <= 23) && (minute >= 0 && minute <= 59);
    }

    // Validates a 4-digit date in MMDD format.
    public static boolean isValidDate(String digits) {
        if (digits.length() != 4 || !digits.matches("\\d{4}")) {
            return false;
        }
        int day = Integer.parseInt(digits.substring(0, 2));
        int month = Integer.parseInt(digits.substring(2, 4));
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1 || day > 31) {  // A simple check; you might want to add further validation.
            return false;
        }
        return true;
    }

    public static String convertHHMMtoHHMMWithColon(String time) {
        if (time == null || time.length() != 4 || !time.matches("\\d{4}")) {
            throw new IllegalArgumentException("Time must be a 4-digit string in HHMM format.");
        }
        // Extract hours and minutes from the string
        String hours = time.substring(0, 2);
        String minutes = time.substring(2, 4);
        // Concatenate with a colon in between
        return hours + ":" + minutes;
    }

    public static String convertTimeToWords(String time) {
        // Ensure the input is exactly 4 characters (HHMM)
        if (time.length() != 4 || !time.matches("\\d{4}")) {
            throw new IllegalArgumentException("Invalid time format. Expected HHMM (e.g., 0930, 1545).");
        }

        // Extract hours and minutes
        int hour = Integer.parseInt(time.substring(0, 2));
        int minute = Integer.parseInt(time.substring(2, 4));

        // Convert hour to 12-hour format and determine AM/PM
        String period = (hour < 12) ? "AM" : "PM";
        hour = (hour % 12 == 0) ? 12 : hour % 12; // Convert 0 or 12 to 12 in 12-hour format

        // Convert numbers to words
        String hourWord = numberToWords(hour);
        String minuteWord = (minute == 0) ? "o'clock" : numberToWords(minute);

        return (minute == 0) ? hourWord + " " + minuteWord + " " + period : hourWord + " " + minuteWord + " " + period;
    }

    private static String numberToWords(int num) {
        String[] ones = {"", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen", "seventeen", "eighteen", "nineteen"};
        String[] tens = {"", "", "twenty", "thirty", "forty", "fifty"};

        if (num < 20) {
            return ones[num];
        } else {
            return tens[num / 10] + (num % 10 != 0 ? "-" + ones[num % 10] : "");
        }
    }
}
