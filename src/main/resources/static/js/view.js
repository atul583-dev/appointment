function loadAppointments() {
  fetch('https://www.zaptobook.com/api/appointments/getAll')
    .then(response => response.json())
    .then(data => {
      console.log("DATA", data);
      const tbody = document.getElementById('appointmentsBody');
      const noAppointments = document.getElementById('noAppointments');

      if (!tbody || !noAppointments) {
        console.error('Required elements missing!');
        return;
      }

      tbody.innerHTML = '';
      if (!data || data.length === 0) {
        noAppointments.style.display = 'block';
      } else {
        noAppointments.style.display = 'none';
        data.forEach(appointment => {
          const row = document.createElement('tr');
          row.innerHTML = getAppointmentRowHTML(appointment);
          tbody.appendChild(row);
        });
      }
    })
    .catch(error => {
      console.error('Error loading appointments:', error);
    });
}

function getAppointmentRowHTML(appointment) {
  const appointmentDate = new Date(`${appointment.date} ${appointment.time}`);
  const now = new Date();
  const status = appointmentDate < now ? "Completed" : "Upcoming";

  return `
    <td>${appointment.name}</td>
    <td>${appointment.phone}</td>
    <td>${appointment.date}</td>
    <td>${appointment.time}</td>
    <td>${status}</td>
  `;
}

window.onload = loadAppointments;