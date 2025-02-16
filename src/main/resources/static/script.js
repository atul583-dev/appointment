let appointments = [];

// Helper functions
function formatDoctor(doctor) {
  return doctor.replace('dr-', 'Dr. ').replace('-', ' ').toUpperCase();
}

function clearDoctorSelection() {
  document.querySelectorAll('.doctor-card').forEach(card =>
    card.classList.remove('selected')
  );
}

function handleFetchResponse(response) {
  if (!response.ok) {
    return response.text().then(text => {
      throw new Error(
        `HTTP error! status: ${response.status}, body: ${text}`
      );
    });
  }
  if (response.status === 204) return null;
  const contentType = response.headers.get('content-type');
  if (!contentType?.includes('application/json')) {
    throw new TypeError('Expected JSON response');
  }
  return response.json();
}

function getAppointmentStatus(appointment) {
  const appDateTime = new Date(`${appointment.date}T${appointment.time}:00`);
  const now = new Date();
  const statusClass = appDateTime > now ? 'upcoming' : 'completed';
  const statusText = statusClass.charAt(0).toUpperCase() + statusClass.slice(1);
  return { statusClass, statusText };
}

function formatDate(dateStr, separator = '-') {
  const dateObj = new Date(dateStr);
  const day = String(dateObj.getDate()).padStart(2, '0');
  const month = String(dateObj.getMonth() + 1).padStart(2, '0');
  const year = dateObj.getFullYear();
  return [day, month, year].join(separator);
}

function getAppointmentRowHTML(app) {
  const { statusClass, statusText } = getAppointmentStatus(app);
  const formattedDate = formatDate(app.date, '-');
  return `
    <td>${app.name}</td>
    <td>${app.phone}</td>
    <td>${formattedDate}</td>
    <td>${app.time}</td>
    <td><span class="status ${statusClass}">${statusText}</span></td>
  `;
}

// UI Functions
function selectDoctor(card) {
  clearDoctorSelection();
  card.classList.add('selected');
  card.querySelector('input').checked = true;
}

function showConfirmation(appointment) {
  const confirmation = document.getElementById('confirmation');
  const confirmationDetails = document.getElementById('confirmationDetails');

  confirmationDetails.innerHTML = `
    <strong>Name:</strong> ${appointment.name}<br>
    <strong>Phone:</strong> ${appointment.phone}<br>
    <strong>Date:</strong> ${new Date(appointment.date).toLocaleDateString()}<br>
    <strong>Time:</strong> ${appointment.time}<br>
    <strong>Doctor:</strong> ${formatDoctor(appointment.doctor)}
  `;
  confirmation.style.display = 'block';
  setTimeout(() => (confirmation.style.display = 'none'), 5000);
}

function updateAppointmentsList() {
  const appointmentsDiv = document.getElementById('appointments');
  appointmentsDiv.innerHTML =
    appointments.length === 0
      ? '<p>No appointments booked yet</p>'
      : appointments
          .map(
            app => `
      <div class="appointment-item" style="padding: 10px; margin: 10px 0; border-bottom: 1px solid #eee;">
        <p><strong>${app.name}</strong> with ${formatDoctor(app.doctor)}</p>
        <p>${new Date(app.date).toLocaleDateString()} at ${app.time}</p>
      </div>
    `
          )
          .join('');
}

async function loadAppointments() {
  try {
    const response = await fetch('https://www.zaptobook.com/api/appointments/getAll');
    const data = await response.json();
    const tbody = document.getElementById('appointmentsBody');
    const noAppointments = document.getElementById('noAppointments');

    tbody.innerHTML = '';
    if (!data || data.length === 0) {
      noAppointments.style.display = 'block';
    } else {
      noAppointments.style.display = 'none';
      data.forEach(app => {
        const row = document.createElement('tr');
        row.innerHTML = getAppointmentRowHTML(app);
        tbody.appendChild(row);
      });
    }
  } catch (error) {
    console.error('Error loading appointments:', error);
  }
}

function showSection(sectionId) {
  // Update tab buttons
  document.querySelectorAll('.nav-button').forEach(btn =>
    btn.classList.remove('active')
  );
  document
    .querySelector(`[onclick="showSection('${sectionId}')"]`)
    .classList.add('active');

  // Update sections
  document.querySelectorAll('.section').forEach(section =>
    section.classList.remove('active')
  );
  document.getElementById(`${sectionId}Section`).classList.add('active');

  // Load content if needed
  if (sectionId === 'appointments') loadAppointments();
  if (sectionId === 'search') document.getElementById('searchPhone').focus();
}

// Event Listeners
document.getElementById('bookingForm').addEventListener('submit', function (e) {
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

  const appointment = { id: Date.now(), name, phone, date, time, doctor };

  fetch('https://www.zaptobook.com/api/appointments', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(appointment)
  })
    .then(handleFetchResponse)
    .then(data => {
      appointments.push(data);
      showConfirmation(data);
      updateAppointmentsList();
      this.reset();
      clearDoctorSelection();
      if (
        document
          .getElementById('appointmentsSection')
          .classList.contains('active')
      ) {
        loadAppointments();
      }
    })
    .catch(error => {
      console.error('Error:', error);
      alert(
        error.message ||
          'An error occurred while booking the appointment.'
      );
    });
});

function searchAppointments() {
  const phone = document.getElementById('searchPhone').value.trim();
  const resultsContainer = document.getElementById('searchResults');

  if (!phone) {
    alert('Please enter a phone number');
    return;
  }

  resultsContainer.innerHTML = '<div class="loading">Searching...</div>';

  fetch(
    `https://www.zaptobook.com/api/appointments/search?phone=${encodeURIComponent(
      phone
    )}`
  )
    .then(handleFetchResponse)
    .then(appointment => {
      resultsContainer.innerHTML = '';

      if (!appointment) {
        resultsContainer.innerHTML = `
          <div class="no-results">
            No appointment found for this phone number
          </div>
        `;
        return;
      }

      const { statusClass, statusText } = getAppointmentStatus(appointment);
      const formattedDate = formatDate(appointment.date, '/');

      const resultItem = document.createElement('div');
      resultItem.className = 'result-item';
      resultItem.innerHTML = `
        <h4>${appointment.name}</h4>
        <div class="details">
          <p>Doctor: ${formatDoctor(appointment.doctor)}</p>
          <p>Date: ${formattedDate}</p>
          <p>Time: ${appointment.time}</p>
          <p>Status: <span class="status ${statusClass}">${statusText}</span></p>
        </div>
      `;
      resultsContainer.appendChild(resultItem);
    })
    .catch(error => {
      console.error('Error:', error);
      resultsContainer.innerHTML = `
        <div class="error">
          Error searching appointment: ${error.message}
        </div>
      `;
    });
}

// Initial setup
document.addEventListener('DOMContentLoaded', function () {
  const today = new Date().toISOString().split('T')[0];
  document.getElementById('date').setAttribute('min', today);
  updateAppointmentsList();
});
