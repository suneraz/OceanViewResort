const API = 'http://localhost:8080/OceanViewResort/api';

window.addEventListener('DOMContentLoaded', function () {
  document.getElementById('loginBtn').addEventListener('click', doLogin);
  document.getElementById('password').addEventListener('keydown', function (e) {
    if (e.key === 'Enter') doLogin();
  });
});

async function doLogin() {
  const alertEl  = document.getElementById('loginAlert');
  alertEl.style.display = 'none';
  const username = document.getElementById('username').value.trim();
  const password = document.getElementById('password').value;
  if (!username) { show('Please enter username.'); return; }
  if (!password) { show('Please enter password.'); return; }

  const fd = new URLSearchParams();
  fd.append('username', username);
  fd.append('password', password);

  try {
    const res  = await fetch(API + '/login', {
      method: 'POST', body: fd,
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      credentials: 'include'
    });
    const data = await res.json();
    if (data.success) {
      sessionStorage.setItem('adminName', data.fullName);
      sessionStorage.setItem('adminRole', data.role);
      window.location.href = 'dashboard.html';
    } else {
      show(data.message);
    }
  } catch (e) { show('Cannot connect to server.'); }

  function show(msg) {
    alertEl.textContent = msg;
    alertEl.style.display = 'block';
  }
}
