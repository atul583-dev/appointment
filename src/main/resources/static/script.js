let appointments = [];

function selectDoctor(card) {
    document.querySelectorAll('.doctor-card').forEach(c => c.classList.remove('selected'));
    card.classList.add('selected');
    card.querySelector('input').checked = true;
}

document.getElementById('bookingForm').addEventListener('submit', function(e) {
    e.preventDefault();

    const name = document.getElementById('name').value;
    const phone = document.getElementById('phone').value;
    const date = document.getElementById('date').value;
    const time = document.getElementById('time').value;
    const doctor = document.querySelector('input[name="doctor"]:checked').value;

    if (!name || !phone || !date || !time || !doctor) {
        alert('Please fill all fields');
        return;
    }

    const appointment = {
        id: Date.now(),
        name,
        phone,
        date,
        time,
        doctor
    };

    appointments.push(appointment);
    showConfirmation(appointment);
    updateAppointmentsList();
    this.reset();
    document.querySelectorAll('.doctor-card').forEach(c => c.classList.remove('selected'));

    // Call Spring Boot API
    fetch('http://localhost:8080/api/appointments', { // Replace with your API URL
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(appointment)
    })
    .then(response => {
        if (!response.ok) {
            throw new Error('Failed to book appointment');
        }
        return response.json();
    })
    .then(data => {
        alert('Appointment booked successfully!');
        appointments.push(data); // Assuming API returns the created appointment with an ID
        showConfirmation(data);
        updateAppointmentsList();
        document.getElementById('bookingForm').reset();
        document.querySelectorAll('.doctor-card').forEach(c => c.classList.remove('selected'));
    })
    .catch(error => {
        console.error('Error:', error);
        alert('An error occurred while booking the appointment.');
    });
});

function showConfirmation(appointment) {
    const confirmation = document.getElementById('confirmation');
    const confirmationDetails = document.getElementById('confirmationDetails');

    confirmationDetails.innerHTML = `
        <strong>Name:</strong> ${appointment.name}<br>
        <strong>Phone:</strong> ${appointment.phone}<br>
        <strong>Date:</strong> ${new Date(appointment.date).toLocaleDateString()}<br>
        <strong>Time:</strong> ${appointment.time}<br>
        <strong>Doctor:</strong> ${appointment.doctor.replace('dr-', 'Dr. ').replace('-', ' ').toUpperCase()}
    `;

    confirmation.style.display = 'block';
    setTimeout(() => confirmation.style.display = 'none', 5000);
}

function updateAppointmentsList() {
    const appointmentsDiv = document.getElementById('appointments');
    appointmentsDiv.innerHTML = appointments.length === 0
        ? '<p>No appointments booked yet</p>'
        : appointments.map(app => `
            <div class="appointment-item" style="padding: 10px; margin: 10px 0; border-bottom: 1px solid #eee;">
                <p><strong>${app.name}</strong> with ${app.doctor.replace('dr-', 'Dr. ').replace('-', ' ').toUpperCase()}</p>
                <p>${new Date(app.date).toLocaleDateString()} at ${app.time}</p>
            </div>
        `).join('');
}

// Initial load
updateAppointmentsList();

document.addEventListener("DOMContentLoaded", function () {
    let today = new Date().toISOString().split('T')[0]; // Get today's date in YYYY-MM-DD format
    document.getElementById("date").setAttribute("min", today); // Set min attribute to today's date
});