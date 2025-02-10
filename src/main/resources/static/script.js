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
        const doctor = document.querySelector('input[name="doctor"]:checked')?.value;

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

        // API Call
        fetch('http://localhost:8080/api/appointments', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(appointment)
        })
        .then(response => {
            if (!response.ok) throw new Error('Failed to book appointment');
            return response.json();
        })
        .then(data => {
            appointments.push(data);
            showConfirmation(data);
            updateAppointmentsList();
            this.reset();
            document.querySelectorAll('.doctor-card').forEach(c => c.classList.remove('selected'));
            if (document.getElementById('appointmentsSection').classList.contains('active')) {
                loadAppointments();
            }
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

    async function loadAppointments() {
        try {
            const response = await fetch('http://localhost:8080/api/appointments/getAll');
            const appointments = await response.json();
            const tbody = document.getElementById('appointmentsBody');
            const noAppointments = document.getElementById('noAppointments');

            tbody.innerHTML = '';
            if (!appointments || appointments.length === 0) {
                noAppointments.style.display = 'block';
            } else {
                noAppointments.style.display = 'none';
                appointments.forEach(app => {
                    const row = document.createElement('tr');
                    const appDateTime = new Date(`${app.date}T${app.time}:00`);
                    const now = new Date();
                    const status = appDateTime > now ? 'upcoming' : 'completed';
                    const formattedDate = appDateTime.toLocaleDateString('en-US', {
                        weekday: 'long',
                        year: 'numeric',
                        month: 'long',
                        day: 'numeric'
                    });

                    row.innerHTML = `
                        <td>${app.name}</td>
                        <td>${app.phone}</td>
                        <td>${formattedDate}</td>
                        <td>${app.time}</td>
                        <td><span class="status ${status}">${status.charAt(0).toUpperCase() + status.slice(1)}</span></td>
                    `;
                    tbody.appendChild(row);
                });
            }
        } catch (error) {
            console.error('Error loading appointments:', error);
            alert('Error loading appointments. Please try again.');
        }
    }

    function showSection(sectionId) {
        document.querySelectorAll('.section').forEach(section => {
            section.classList.remove('active');
        });
        const activeSection = document.getElementById(`${sectionId}Section`);
        activeSection.classList.add('active');

        if (sectionId === 'appointments') {
            loadAppointments();
        }
    }

    // Initial setup
    document.addEventListener("DOMContentLoaded", function() {
        const today = new Date().toISOString().split('T')[0];
        document.getElementById("date").setAttribute("min", today);
        updateAppointmentsList();
    });