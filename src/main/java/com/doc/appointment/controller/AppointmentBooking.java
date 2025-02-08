package com.doc.appointment.controller;

import com.doc.appointment.model.Appointment;
import com.doc.appointment.service.AppointmentService;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Say;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@RestController
public class AppointmentBooking {

    @Autowired
    private AppointmentService appointmentService;

    Appointment appointment;

    @PostMapping(value = "/", produces = "application/xml")
    public String handleMenu() {
        System.out.println("Call Accepted");
        appointment = new Appointment();

        VoiceResponse response = new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .inputs(Arrays.asList(Gather.Input.DTMF))
                        .numDigits(1)
                        .timeout(10)
                        .say(new Say.Builder("Welcome to Healer Clinic! " +
                                "Press one to book an appointment. " +
                                "Press two to cancel an appointment.")
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)
                                .build())
                        .action("/handleInput")
                        .build())
                // If no digits are captured, redirect back to the main menu.
                .redirect(new Redirect.Builder("/").build())
                .build();
        System.out.println("Response Accepted");
        return response.toXml();
    }

    @PostMapping(value = "/handleInput", produces = "application/xml")
    public String handleInput(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Menu selection: " + digits);
        if (digits == null) {
            // No input received; re-present the main menu.
            return handleMenu();
        }

        String message;
        if ("1".equals(digits)) {
            // First, collect the phone number.
            message = "You have chosen to book an appointment. Please enter your 10-digit mobile number.";
            return buildGatherPhoneNumberResponse(message, "/collectPhoneNumber");
        } else if ("2".equals(digits)) {
            message = "You have chosen to cancel an appointment. Goodbye.";
            return buildSayResponse(message);
        } else {
            message = "Invalid input. Please try again.";
            // Re-prompt the main menu if input is invalid.
            return handleMenu();
        }
    }

    @PostMapping(value = "/collectPhoneNumber", produces = "application/xml")
    public String collectPhoneNumber(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Collected phone number: " + digits);
        if (digits == null || digits.length() != 10 || !digits.matches("\\d{10}")) {
            // Re-prompt for a valid 10-digit phone number.
            return buildGatherPhoneNumberResponse("We did not receive a valid 10-digit phone number. Please enter your 10-digit mobile number.", "/collectPhoneNumber");
        }
        appointment.setPhone(digits);
        // Once a valid phone number is captured, ask for the name.
        String message = "Thank you for providing your phone number. Please say your full name.";
        return buildGatherSpeechResponse(message, "/collectName");
    }

    @PostMapping(value = "/collectName", produces = "application/xml")
    public String collectName(@RequestParam(value = "SpeechResult", required = false) String speechResult) {
        System.out.println("Collected name: " + speechResult);
        if (speechResult == null || speechResult.isEmpty()) {
            // Re-prompt for the caller's name.
            return buildGatherSpeechResponse("We did not receive a valid name. Please say your full name.", "/collectName");
        }
        appointment.setName(speechResult);
        String message = "Thank you " + speechResult + ". Please enter your appointment date in DDMM format, for example, 1231 for December 31.";
        return buildGatherDateResponse(message, "/collectDate");
    }

    @PostMapping(value = "/collectDate", produces = "application/xml")
    public String collectDate(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Collected appointment date: " + digits);
        if (digits == null || digits.length() != 4 || !digits.matches("\\d{4}") || !isValidDate(digits)) {
            // Re-prompt for a valid appointment date.
            return buildGatherDateResponse("We did not receive a valid appointment date. Please enter the date in DDMM format, for example, 1231 for December 31.", "/collectDate");
        }
        appointment.setDate(convertMMDDtoDDMMYYYY(digits));
        String message = "You have selected appointment date " + digits + ". Now, please enter your preferred appointment time in HHMM format, for example, 0930 for 9:30 AM.";
        return buildGatherResponse(message, "/collectTime");
    }

    @PostMapping(value = "/collectTime", produces = "application/xml")
    public String collectTime(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Collected appointment time: " + digits);
        if (digits == null || digits.isEmpty() || !isValidTime(digits)) {
            // Re-prompt for a valid time slot.
            return buildGatherResponse("We did not receive a valid time slot. Please enter your preferred time using 4 digits, like 0930 for 9:30 AM.", "/collectTime");
        }
        appointment.setTime(convertHHMMtoHHMMWithColon(digits));
        String message = "Your appointment is booked for time slot " + digits + ". Thank you for using our service.";
        appointment.setDoctor("Dr. Atul");
        appointmentService.save(appointment);
        System.out.println("Appointment booked!" + appointment);
        return buildSayResponse(message);
    }

    // Helper method for gathering time input via DTMF (expects 4 digits for HHMM)
    private String buildGatherResponse(String prompt, String action) {
        System.out.println("Prompt (Time): " + prompt);
        return new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .inputs(Gather.Input.DTMF)
                        .numDigits(4)
                        .say(new Say.Builder(prompt)
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)
                                .build())
                        .action(action)
                        .timeout(10)
                        .build())
                // If no input is received, redirect back to the same endpoint.
                .redirect(new Redirect.Builder(action).build())
                .build()
                .toXml();
    }

    // Helper method for gathering date input via DTMF (expects 4 digits for MMDD)
    private String buildGatherDateResponse(String prompt, String action) {
        System.out.println("Prompt (Date): " + prompt);
        return new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .inputs(Gather.Input.DTMF)
                        .numDigits(4)
                        .say(new Say.Builder(prompt)
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)
                                .build())
                        .action(action)
                        .timeout(10)
                        .build())
                .redirect(new Redirect.Builder(action).build())
                .build()
                .toXml();
    }

    // Helper method for gathering phone number input via DTMF (expects 10 digits)
    private String buildGatherPhoneNumberResponse(String prompt, String action) {
        System.out.println("Prompt (Phone Number): " + prompt);
        return new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .inputs(Gather.Input.DTMF)
                        .numDigits(10)
                        .say(new Say.Builder(prompt)
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)
                                .build())
                        .action(action)
                        .timeout(10)
                        .build())
                .redirect(new Redirect.Builder(action).build())
                .build()
                .toXml();
    }

    // Helper method for gathering speech input (for the name)
    private String buildGatherSpeechResponse(String prompt, String action) {
        System.out.println("Prompt (Speech): " + prompt);
        return new VoiceResponse.Builder()
                .gather(new Gather.Builder()
                        .inputs(Arrays.asList(Gather.Input.SPEECH))
                        .action(action)
                        .timeout(10)
                        .say(new Say.Builder(prompt)
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)
                                .build())
                        .build())
                .redirect(new Redirect.Builder(action).build())
                .build()
                .toXml();
    }

    // Simple helper method to generate a say-only response.
    private String buildSayResponse(String message) {
        return new VoiceResponse.Builder()
                .say(new Say.Builder(message)
                        .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                        .language(Say.Language.EN_IN)
                        .build())
                .build()
                .toXml();
    }

    // Validates a 4-digit time in HHMM format.
    private boolean isValidTime(String digits) {
        if (digits.length() != 4 || !digits.matches("\\d{4}")) {
            return false;
        }
        int hour = Integer.parseInt(digits.substring(0, 2));
        int minute = Integer.parseInt(digits.substring(2, 4));
        return (hour >= 0 && hour <= 23) && (minute >= 0 && minute <= 59);
    }

    // Validates a 4-digit date in MMDD format.
    private boolean isValidDate(String digits) {
        if (digits.length() != 4 || !digits.matches("\\d{4}")) {
            return false;
        }
        int month = Integer.parseInt(digits.substring(0, 2));
        int day = Integer.parseInt(digits.substring(2, 4));
        if (month < 1 || month > 12) {
            return false;
        }
        if (day < 1 || day > 31) {  // A simple check; you might want to add further validation.
            return false;
        }
        return true;
    }

    // Converts a date provided in MMDD format to a string in DD-MM-YYYY format.
    public static String convertMMDDtoDDMMYYYY(String mmdd) {
        if (mmdd == null || mmdd.length() != 4) {
            throw new IllegalArgumentException("Input must be in MMDD format (4 digits)");
        }

        String dayStr = mmdd.substring(0, 2);
        String monthStr = mmdd.substring(2, 4);
        int currentYear = Year.now().getValue();

        try {
            int month = Integer.parseInt(monthStr);
            int day = Integer.parseInt(dayStr);
            LocalDate date = LocalDate.of(currentYear, month, day);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return date.format(formatter);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid month or day format", e);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid date: " + mmdd, e);
        }
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
}
