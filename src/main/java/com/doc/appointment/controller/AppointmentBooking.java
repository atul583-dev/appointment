package com.doc.appointment.controller;

import com.doc.appointment.utils.AppointmentUtils;
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
            message = "You have chosen to cancel an appointment. Please enter your 10-digit mobile number.";
            return buildGatherPhoneNumberResponse(message, "/cancelCollectPhoneNumber");
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
        Appointment apt = appointmentService.findByPhone(digits);
        String message = "";
        if(apt != null) {
            String name = apt.getName();
            appointment.setName(name);
            message = "Thank you " + name + ". Please enter your appointment date in DDMM format, for example, 1231 for December 31.";
            return buildGatherDateResponse(message, "/collectDate");
        } else {
            message = "Thank you for providing your phone number. Please say your full name.";
        }
        return buildGatherSpeechResponse(message, "/collectName");
    }

    @PostMapping(value = "/collectName", produces = "application/xml")
    public String collectName(@RequestParam(value = "SpeechResult", required = false) String speechResult) {
        System.out.println("Collected name: " + speechResult);

        // Clean and validate the captured name.
        String cleanedName = (speechResult != null) ? speechResult.trim() : "";
        if (cleanedName.isEmpty() || !cleanedName.matches(".*[a-zA-Z]+.*")) {
            // Log the fact that we didn't receive valid speech input.
            System.out.println("No valid name received. Re-prompting...");
            return buildGatherSpeechResponse(
                    "We did not receive a valid name. Please say your full name, including your first and last name.",
                    "/collectName"
            );
        }

        appointment.setName(cleanedName);
        String message = "Thank you " + cleanedName + ". Please enter your appointment date in DDMM format, for example, 1231 for December 31.";
        return buildGatherDateResponse(message, "/collectDate");
    }

    @PostMapping(value = "/collectDate", produces = "application/xml")
    public String collectDate(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Collected appointment date: " + digits);
        if (digits == null || digits.length() != 4 || !digits.matches("\\d{4}") || !AppointmentUtils.isValidDate(digits)) {
            // Re-prompt for a valid appointment date.
            return buildGatherDateResponse("We did not receive a valid appointment date. Please enter the date in DDMM format, for example, 1231 for December 31.", "/collectDate");
        }
        System.out.println("Date: " + AppointmentUtils.convertDate(digits));
        appointment.setDate(AppointmentUtils.convertDate(digits));
        String message = "You have selected appointment date " + AppointmentUtils.convertDateToWords(AppointmentUtils.convertDate(digits)) + ". Now, please enter your preferred appointment time in HHMM format, for example, 0930 for 9:30 AM.";
        return buildGatherResponse(message, "/collectTime");
    }

    @PostMapping(value = "/collectTime", produces = "application/xml")
    public String collectTime(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Collected appointment time: " + digits);
        if (digits == null || digits.isEmpty() || !AppointmentUtils.isValidTime(digits)) {
            // Re-prompt for a valid time slot.
            return buildGatherResponse("We did not receive a valid time slot. Please enter your preferred time using 4 digits, like 0930 for 9:30 AM.", "/collectTime");
        }
        // Convert and set the appointment time
        appointment.setTime(AppointmentUtils.convertHHMMtoHHMMWithColon(digits));
        System.out.println("Checking if slot is available");

        // Check if the desired slot is available
        boolean isAvailable = appointmentService.isSlotAvailable(appointment.getDate(), appointment.getTime());
        System.out.println("Is slot available: " + isAvailable);

        // If the slot is not available, prompt the user to select another time slot.
        if (!isAvailable) {
            return buildGatherResponse("Slot is already booked, please select another time slot.", "/collectTime");
        }

        // Slot is available; proceed to book the appointment.
        String message = "Your appointment is booked for " + AppointmentUtils.convertDateToWords(appointment.getDate()) +
                ", at " + AppointmentUtils.convertTimeToWords(digits) + ". Thank you for using our service.";
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
                        .hints("John, Jane, Michael, Sarah, David, Emily")  // Hints can help the recognizer
                        // Optionally comment out enhanced and speechTimeout for troubleshooting:
                        // .enhanced(true)
                        // .speechTimeout("auto")
                        .action(action)
                        .timeout(10)
                        .say(new Say.Builder(prompt)
                                .voice(Say.Voice.GOOGLE_EN_IN_NEURAL2_C)
                                .language(Say.Language.EN_IN)  // Use "en-IN" (or try "en-US" if appropriate)
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

    @PostMapping(value = "/cancelCollectPhoneNumber", produces = "application/xml")
    public String cancelCollectPhoneNumber(@RequestParam(value = "Digits", required = false) String digits) {
        System.out.println("Cancel appointment phone number: " + digits);
        if (digits == null || digits.length() != 10 || !digits.matches("\\d{10}")) {
            return buildGatherPhoneNumberResponse("We did not receive a valid 10-digit phone number. Please enter your 10-digit mobile number.", "/cancelCollectPhoneNumber");
        }

        Appointment existingAppointment = appointmentService.findByPhone(digits);
        if (existingAppointment != null) {
            appointmentService.cancelAppointment(digits);
            String message = "Your appointment has been successfully canceled. Thank you.";
            return buildSayResponse(message);
        } else {
            String message = "No appointment found with the provided phone number. Goodbye.";
            return buildSayResponse(message);
        }
    }
}
