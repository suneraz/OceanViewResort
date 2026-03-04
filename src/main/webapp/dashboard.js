const API  = 'http://localhost:8080/OceanViewResort/api';
const RATES = { 'Standard': 3000, 'Deluxe': 8000, 'Suite': 13000, 'Ocean View': 20000 };
const CANCEL_FEE = 500;

window.addEventListener('DOMContentLoaded', function () {
  const name = sessionStorage.getItem('adminName');
  const role = sessionStorage.getItem('adminRole');
  if (!name) { window.location.href = 'login.html'; return; }

  document.getElementById('sidebarName').textContent = name;
  document.getElementById('sidebarRole').textContent = role;
  document.getElementById('welcomeName').textContent = name;

  // Hide Add Staff from nonadmins
  if (role !== 'admin') {
    document.getElementById('nav-addStaff').style.display = 'none';
  }

  document.getElementById('logoutBtn').addEventListener('click', doLogout);
  document.getElementById('addForm').addEventListener('submit', submitReservation);
  document.getElementById('clearBtn').addEventListener('click', clearForm);
  document.getElementById('refreshBtn').addEventListener('click', loadAll);
  document.getElementById('searchName').addEventListener('input', searchRes);
  document.getElementById('lookupBtn').addEventListener('click', lookupRes);
  document.getElementById('billBtn').addEventListener('click', generateBill);
  document.getElementById('addStaffBtn').addEventListener('click', addStaff);
  document.getElementById('checkInDate').addEventListener('change', function () {
    document.getElementById('checkOutDate').min = this.value;
    updateAddTotal();
  });
  document.getElementById('checkOutDate').addEventListener('change', updateAddTotal);

  const today = new Date().toISOString().split('T')[0];
  document.getElementById('checkInDate').min = today;
  document.getElementById('checkOutDate').min = today;

  loadDashboard();
});

function doLogout() {
  fetch(API + '/logout', { method: 'POST', credentials: 'include' }).catch(() => {});
  sessionStorage.clear();
  window.location.href = 'login.html';
}

function showPage(name) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-item').forEach(n => n.classList.remove('active'));
  document.getElementById('page-' + name).classList.add('active');
  document.getElementById('nav-' + name).classList.add('active');
  if (name === 'allReservations') loadAll();
  if (name === 'addStaff') loadStaff();
}

//  DASHBOARD
async function loadDashboard() {
  try {
    const res  = await fetch(API + '/reservations?action=all', { credentials: 'include' });
    const data = await res.json();
    if (!data.success) return;
    const all = data.data;
    document.getElementById('statTotal').textContent     = all.length;
    document.getElementById('statConfirmed').textContent = all.filter(r => r.status === 'Confirmed').length;
    document.getElementById('statCancelled').textContent = all.filter(r => r.status === 'Cancelled').length;
    const rev = all.filter(r => r.status !== 'Cancelled').reduce((s, r) => {
      const nights = Math.max(1, Math.round((new Date(r.checkOutDate) - new Date(r.checkInDate)) / 86400000));
      return s + nights * (RATES[r.roomType] || 0);
    }, 0);
    document.getElementById('statRevenue').textContent = rev.toLocaleString();
    document.getElementById('dashBody').innerHTML = all.slice(0, 8).map(r =>
      `<tr>
        <td><strong>${r.reservationNumber}</strong></td>
        <td>${r.guestName}</td>
        <td>${r.roomNumber || '-'}</td>
        <td>${r.roomType}</td>
        <td>${r.checkInDate}</td>
        <td>${r.checkOutDate}</td>
        <td>${badge(r.status)}</td>
      </tr>`
    ).join('') || '<tr><td colspan="7" class="empty">No reservations yet.</td></tr>';
  } catch (e) { console.error(e); }
}

// ADD RESERVATION
async function onAdminRoomTypeChange() {
  const type = document.getElementById('roomType').value;
  const grp  = document.getElementById('roomNumGroup');
  const sel  = document.getElementById('roomNumber');
  updateAddTotal();
  if (!type) { grp.style.display = 'none'; return; }
  try {
    const res  = await fetch(API + '/reservations?action=rooms&type=' + encodeURIComponent(type), { credentials: 'include' });
    const data = await res.json();
    if (data.success) {
      sel.innerHTML = '<option value="">- Select room -</option>' +
        (data.rooms.length ? data.rooms.map(r => `<option value="${r}">${r}</option>`).join('') : '<option disabled>No rooms available</option>');
      grp.style.display = 'block';
    }
  } catch (e) { grp.style.display = 'none'; }
}

function updateAddTotal() {
  const type    = document.getElementById('roomType').value;
  const checkIn = document.getElementById('checkInDate').value;
  const checkOut= document.getElementById('checkOutDate').value;
  const grp     = document.getElementById('addTotalGroup');
  if (!type || !checkIn || !checkOut) { grp.style.display = 'none'; return; }
  const nights = Math.round((new Date(checkOut) - new Date(checkIn)) / 86400000);
  if (nights <= 0) { grp.style.display = 'none'; return; }
  const rate = RATES[type] || 0;
  document.getElementById('addTotal').value = nights + ' night(s) × LKR ' + rate.toLocaleString() + ' = LKR ' + (nights * rate).toLocaleString();
  grp.style.display = 'block';
}

async function submitReservation(e) {
  e.preventDefault();
  const alertEl = document.getElementById('addAlert');
  alertEl.style.display = 'none';

  const fd = new URLSearchParams();
  fd.append('guestName',     document.getElementById('guestName').value);
  fd.append('address',       document.getElementById('address').value);
  fd.append('contactNumber', document.getElementById('contactNumber').value);
  fd.append('roomType',      document.getElementById('roomType').value);
  fd.append('roomNumber',    document.getElementById('roomNumber').value);
  fd.append('checkInDate',   document.getElementById('checkInDate').value);
  fd.append('checkOutDate',  document.getElementById('checkOutDate').value);

  try {
    const res  = await fetch(API + '/reservations?action=add', {
      method: 'POST', body: fd, credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    const data = await res.json();
    if (data.success) {
      toast('✅ Reservation ' + data.reservationNumber + ' confirmed! Room ' + data.roomNumber);
      clearForm();
      loadDashboard();
    } else {
      alertEl.textContent = data.message;
      alertEl.className = 'alert alert-error';
      alertEl.style.display = 'block';
    }
  } catch (e) {
    alertEl.textContent = 'Server error.';
    alertEl.className = 'alert alert-error';
    alertEl.style.display = 'block';
  }
}

function clearForm() {
  ['guestName','address','contactNumber','checkInDate','checkOutDate'].forEach(id => document.getElementById(id).value = '');
  document.getElementById('roomType').value = '';
  document.getElementById('roomNumber').value = '';
  document.getElementById('roomNumGroup').style.display = 'none';
  document.getElementById('addTotalGroup').style.display = 'none';
  document.getElementById('addAlert').style.display = 'none';
}

// ALL RESERVATIONS
async function loadAll() {
  const tbody = document.getElementById('allBody');
  tbody.innerHTML = '<tr><td colspan="9" class="empty">Loading...</td></tr>';
  try {
    const res  = await fetch(API + '/reservations?action=all', { credentials: 'include' });
    const data = await res.json();
    if (data.success) renderAll(data.data);
  } catch (e) { tbody.innerHTML = '<tr><td colspan="9" class="empty">Error loading.</td></tr>'; }
}

async function searchRes() {
  const name = document.getElementById('searchName').value.trim();
  if (!name) { loadAll(); return; }
  const res  = await fetch(API + '/reservations?action=search&name=' + encodeURIComponent(name), { credentials: 'include' });
  const data = await res.json();
  if (data.success) renderAll(data.data);
}

function renderAll(list) {
  document.getElementById('allBody').innerHTML = list.length
    ? list.map(r => `<tr>
        <td><strong>${r.reservationNumber}</strong></td>
        <td>${r.guestName}</td>
        <td>${r.contactNumber}</td>
        <td>${r.roomNumber || '-'}</td>
        <td>${r.roomType}</td>
        <td>${r.checkInDate}</td>
        <td>${r.checkOutDate}</td>
        <td>${badge(r.status)}</td>
        <td class="actions">
          <button class="btn btn-danger" onclick="cancelRes('${r.reservationNumber}')" ${r.status==='Cancelled'?'disabled':''}>Cancel</button>
          <button class="btn btn-del"    onclick="deleteRes('${r.reservationNumber}')">Delete</button>
        </td>
      </tr>`).join('')
    : '<tr><td colspan="9" class="empty">No reservations found.</td></tr>';
}

async function cancelRes(id) {
  if (!confirm('Are you sure you want to cancel reservation ' + id + '?')) return;
  const fd = new URLSearchParams();
  const res  = await fetch(API + '/reservations?action=cancel&id=' + encodeURIComponent(id), {
    method: 'POST', body: fd, credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  });
  const data = await res.json();
  if (data.success) { toast('Reservation ' + id + ' cancelled.'); loadAll(); loadDashboard(); }
  else alert(data.message);
}

async function deleteRes(id) {
  if (!confirm('Permanently DELETE reservation ' + id + '? This cannot be undone.')) return;
  const fd = new URLSearchParams();
  const res  = await fetch(API + '/reservations?action=delete&id=' + encodeURIComponent(id), {
    method: 'POST', body: fd, credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  });
  const data = await res.json();
  if (data.success) { toast('Reservation ' + id + ' deleted.'); loadAll(); loadDashboard(); }
  else alert(data.message);
}

// DETAILS 
async function lookupRes() {
  const id = document.getElementById('lookupId').value.trim();
  if (!id) { alert('Enter a reservation number.'); return; }
  const res  = await fetch(API + '/reservations?action=get&id=' + encodeURIComponent(id), { credentials: 'include' });
  const data = await res.json();
  const card = document.getElementById('detailsCard');
  card.style.display = 'block';
  if (data.success) {
    const r = data.data;
    document.getElementById('detailsContent').innerHTML = `
      <div class="detail-grid">
        ${dRow('Reservation No.', r.reservationNumber)}
        ${dRow('Status', badge(r.status))}
        ${dRow('Guest Name', r.guestName)}
        ${dRow('Contact', r.contactNumber)}
        ${dRow('Address', r.address)}
        ${dRow('Room Number', r.roomNumber || '—')}
        ${dRow('Room Type', r.roomType)}
        ${dRow('Check-In', r.checkInDate)}
        ${dRow('Check-Out', r.checkOutDate)}
      </div>`;
  } else {
    document.getElementById('detailsContent').innerHTML = '<p style="color:#c00;font-size:14px;">' + data.message + '</p>';
  }
}
function dRow(l, v) {
  return `<div class="detail-item"><div class="lbl">${l}</div><div class="val">${v}</div></div>`;
}

// BILLING
async function generateBill() {
  const id = document.getElementById('billId').value.trim();
  if (!id) { alert('Enter a reservation number.'); return; }
  const res  = await fetch(API + '/reservations?action=get&id=' + encodeURIComponent(id), { credentials: 'include' });
  const data = await res.json();
  const sec  = document.getElementById('billSection');
  sec.style.display = 'block';
  if (data.success) {
    const r = data.data;
    const nights   = Math.max(1, Math.round((new Date(r.checkOutDate) - new Date(r.checkInDate)) / 86400000));
    const rate     = RATES[r.roomType] || 0;
    const subtotal = nights * rate;
    const tax      = subtotal * 0;
    const total    = subtotal + tax;
    document.getElementById('billContent').innerHTML = `
      <div style="text-align:center;padding-bottom:16px;margin-bottom:16px;border-bottom:1px solid #eee;">
        <div style="font-size:24px;"></div>
        <h2 style="font-size:18px;margin:4px 0;">Ocean View Resort</h2>
        <p style="font-size:12px;color:#888;">Galle, Sri Lanka - Tax Invoice</p>
      </div>
      ${bLine('Reservation No.', r.reservationNumber)}
      ${bLine('Guest Name', r.guestName)}
      ${bLine('Room Number', r.roomNumber || '-')}
      ${bLine('Room Type', r.roomType)}
      ${bLine('Check-In', r.checkInDate)}
      ${bLine('Check-Out', r.checkOutDate)}
      ${bLine('Nights', nights)}
      ${bLine('Rate / Night', 'LKR ' + rate.toLocaleString())}
      ${bLine('Subtotal', 'LKR ' + subtotal.toLocaleString())}
      <div class="bill-total"><span>TOTAL</span><span>LKR ${total.toLocaleString()}</span></div>`;
  } else {
    document.getElementById('billContent').innerHTML = '<p style="color:#c00;">' + data.message + '</p>';
  }
}
function bLine(l, v) {
  return `<div class="bill-line"><span class="lbl">${l}</span><span>${v}</span></div>`;
}

// ADD STAFF
async function addStaff() {
  const alertEl  = document.getElementById('staffAlert');
  alertEl.style.display = 'none';
  const fullName = document.getElementById('staffName').value.trim();
  const username = document.getElementById('staffUsername').value.trim();
  const password = document.getElementById('staffPassword').value;
  const confirm  = document.getElementById('staffConfirm').value;

  if (!fullName) { staffAlert('Enter full name.'); return; }
  if (!username) { staffAlert('Enter username.'); return; }
  if (!password) { staffAlert('Enter password.'); return; }
  if (password !== confirm) { staffAlert('Passwords do not match.'); return; }

  const fd = new URLSearchParams();
  fd.append('fullName', fullName); fd.append('username', username); fd.append('password', password);

  try {
    const res  = await fetch(API + '/staff?action=add', {
      method: 'POST', body: fd, credentials: 'include',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    });
    const data = await res.json();
    if (data.success) {
      toast('✅ Staff account created for ' + fullName);
      ['staffName','staffUsername','staffPassword','staffConfirm'].forEach(id => document.getElementById(id).value = '');
      loadStaff();
    } else staffAlert(data.message);
  } catch (e) { staffAlert('Server error.'); }
}

async function loadStaff() {
  try {
    const res  = await fetch(API + '/staff?action=list', { credentials: 'include' });
    const data = await res.json();
    if (data.success) {
      document.getElementById('staffBody').innerHTML = data.data.length
        ? data.data.map(s => `<tr>
            <td>${s.fullName}</td>
            <td>${s.username}</td>
            <td><span class="badge ${s.role === 'admin' ? 'badge-confirmed' : 'badge-cancelled'}" style="background:${s.role==='admin'?'#111':'#f0eeea'};color:${s.role==='admin'?'#fff':'#666'};">${s.role}</span></td>
            <td>${s.role !== 'admin' ? `<button class="btn btn-del" onclick="deleteStaff('${s.username}')">Remove</button>` : '—'}</td>
          </tr>`).join('')
        : '<tr><td colspan="4" class="empty">No staff found.</td></tr>';
    }
  } catch (e) { console.error(e); }
}

async function deleteStaff(username) {
  if (!confirm('Remove staff account: ' + username + '?')) return;
  const fd = new URLSearchParams();
  fd.append('username', username);
  const res  = await fetch(API + '/staff?action=delete', {
    method: 'POST', body: fd, credentials: 'include',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
  });
  const data = await res.json();
  if (data.success) { toast('Staff removed.'); loadStaff(); }
  else alert(data.message);
}

function staffAlert(msg) {
  const el = document.getElementById('staffAlert');
  el.textContent = msg; el.className = 'alert alert-error'; el.style.display = 'block';
}

// HELP
function toggleHelp(el) {
  el.classList.toggle('open');
  el.nextElementSibling.classList.toggle('open');
}

//HELPERS 
function badge(status) {
  const cls = status === 'Confirmed' ? 'badge-confirmed' : 'badge-cancelled';
  return `<span class="badge ${cls}">${status}</span>`;
}
function toast(msg) {
  const t = document.getElementById('toast');
  t.textContent = msg; t.classList.add('show');
  setTimeout(() => t.classList.remove('show'), 4000);
}
