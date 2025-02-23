document.getElementById('bookingForm').addEventListener('submit', function (e) {
  e.preventDefault();

  const name = document.getElementById('name').value;
  const phone = document.getElementById('phone').value;
  const date = document.getElementById('date').value;
  const time = document.getElementById('time').value;
  const doctor = document.getElementById('doctor').value;

  if (!name || !phone || !date || !time || !doctor) {
    alert('Please fill all fields');
    return;
  }

  const appointment = { id: Date.now(), name, phone, date, time, doctor, status };

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