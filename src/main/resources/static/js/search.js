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

      const resultItem = document.createElement('div');
      resultItem.className = 'result-item';
      resultItem.innerHTML = `
        <h4>${appointment.name}</h4>
        <div class="details">
          <p>Doctor: ${(appointment.doctor)}</p>
          <p>Date: ${appointment.date}</p>
          <p>Time: ${appointment.time}</p>
          <p>Status: ${appointment.status}</p>
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