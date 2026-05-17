const API = 'http://localhost:8080/api';

const state = {
  token: localStorage.getItem('aai_token') || null,
  username: localStorage.getItem('aai_user') || null,
  role: localStorage.getItem('aai_role') || null,
  policies: [],
  currentClaimId: null,
};

async function api(method, path, body = null, auth = true) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth && state.token) headers['Authorization'] = `Bearer ${state.token}`;

  const res = await fetch(`${API}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : null,
  });

  const text = await res.text();
  let data;
  try { data = JSON.parse(text); } catch { data = text; }

  if (!res.ok) {
    const msg = data?.message || data || `HTTP ${res.status}`;
    throw new Error(msg);
  }
  return data;
}

function toast(msg, type = 'info') {
  const el = document.createElement('div');
  el.className = `toast toast-${type}`;
  el.textContent = msg;
  document.getElementById('toastContainer').appendChild(el);
  setTimeout(() => el.remove(), 3200);
}

function navigate(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));

  const page = document.getElementById(`page-${pageId}`);
  if (page) page.classList.add('active');

  document.querySelectorAll(`[data-page="${pageId}"]`).forEach(l => l.classList.add('active'));

  if (pageId === 'policies') loadPolicies();
  if (pageId === 'claims')   loadClaims();
  if (pageId === 'admin')    loadAdminClaims();
}

function initStars() {
  const container = document.getElementById('stars');
  for (let i = 0; i < 160; i++) {
    const s = document.createElement('div');
    const size = Math.random() * 2.5 + 0.5;
    s.className = 'star';
    s.style.cssText = `
      width:${size}px; height:${size}px;
      top:${Math.random()*100}%; left:${Math.random()*100}%;
      --dur:${2+Math.random()*4}s;
      --delay:${-Math.random()*5}s;
      --min-op:${0.05+Math.random()*0.15};
      --max-op:${0.5+Math.random()*0.5};
    `;
    container.appendChild(s);
  }
}

function updateAuthUI() {
  const loggedIn = !!state.token;
  const isAdmin  = state.role === 'ROLE_ADMIN';

  document.querySelectorAll('.auth-only').forEach(el => el.classList.toggle('hidden', !loggedIn));
  document.querySelectorAll('.admin-only').forEach(el => el.classList.toggle('hidden', !isAdmin));
  document.getElementById('authBtn').classList.toggle('hidden', loggedIn);
  document.getElementById('logoutBtn').classList.toggle('hidden', !loggedIn);

  const navUser = document.getElementById('navUser');
  if (loggedIn) {
    navUser.textContent = `◈ ${state.username}${isAdmin ? ' [Admin]' : ''}`;
    navUser.classList.remove('hidden');
  } else {
    navUser.classList.add('hidden');
  }

  const heroBtn = document.getElementById('heroLoginBtn');
  if (heroBtn) heroBtn.textContent = loggedIn ? 'My policies' : 'Sign in';
}

function logout() {
  state.token = state.username = state.role = null;
  localStorage.removeItem('aai_token');
  localStorage.removeItem('aai_user');
  localStorage.removeItem('aai_role');
  updateAuthUI();
  navigate('home');
  toast('You have been signed out.', 'info');
}

document.getElementById('tabLogin').addEventListener('click', () => {
  document.getElementById('tabLogin').classList.add('active');
  document.getElementById('tabRegister').classList.remove('active');
  document.getElementById('loginForm').classList.remove('hidden');
  document.getElementById('registerForm').classList.add('hidden');
});

document.getElementById('tabRegister').addEventListener('click', () => {
  document.getElementById('tabRegister').classList.add('active');
  document.getElementById('tabLogin').classList.remove('active');
  document.getElementById('registerForm').classList.remove('hidden');
  document.getElementById('loginForm').classList.add('hidden');
});

document.getElementById('loginForm').addEventListener('submit', async e => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Signing in...';
  try {
    const data = await api('POST', '/auth/login', {
      username: document.getElementById('loginUsername').value,
      password: document.getElementById('loginPassword').value,
    }, false);
    state.token    = data.token;
    state.username = data.username;
    state.role     = data.role;
    localStorage.setItem('aai_token', data.token);
    localStorage.setItem('aai_user',  data.username);
    localStorage.setItem('aai_role',  data.role);
    updateAuthUI();
    toast(`Welcome, ${data.username}!`, 'success');
    navigate(data.role === 'ROLE_ADMIN' ? 'admin' : 'policies');
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Sign in';
  }
});

document.getElementById('registerForm').addEventListener('submit', async e => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Creating...';
  try {
    const data = await api('POST', '/auth/register', {
      username: document.getElementById('regUsername').value,
      email:    document.getElementById('regEmail').value,
      password: document.getElementById('regPassword').value,
    }, false);
    state.token    = data.token;
    state.username = data.username;
    state.role     = data.role;
    localStorage.setItem('aai_token', data.token);
    localStorage.setItem('aai_user',  data.username);
    localStorage.setItem('aai_role',  data.role);
    updateAuthUI();
    toast('Account created! Welcome.', 'success');
    navigate('policies');
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Create account';
  }
});

document.getElementById('qNights').addEventListener('input', e => {
  document.getElementById('nightsVal').textContent = e.target.value;
});

document.getElementById('quoteForm').addEventListener('submit', async e => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Calculating...';
  try {
    const data = await api('POST', '/policies/quote', {
      residenceZone:        document.getElementById('qZone').value,
      nightsOutdoorPerWeek: +document.getElementById('qNights').value,
      coverageAmount:       +document.getElementById('qCoverage').value,
      hasReportedUfos:      document.getElementById('qUfos').checked,
      hasMicrochip:         document.getElementById('qChip').checked,
    }, false);

    const el = document.getElementById('quoteResult');
    el.style.display = 'block';

    const riskEl = document.getElementById('resultRisk');
    riskEl.textContent = data.riskLevel;
    riskEl.className   = `result-risk ${data.riskLevel}`;

    document.getElementById('resultMonthly').textContent = `$${Number(data.estimatedMonthlyPremium).toLocaleString()}`;
    document.getElementById('resultAnnual').textContent  = `$${Number(data.estimatedAnnualPremium).toLocaleString()}`;
    document.getElementById('resultBreakdown').textContent = data.breakdown;

    const buyBtn = document.getElementById('buyPolicyBtn');
    if (state.token) {
      buyBtn.style.display = 'block';
      buyBtn.textContent = 'Get this policy';
      buyBtn.onclick = () => { navigate('policies'); document.getElementById('newPolicyBtn').click(); };
    }

    toast('Quote calculated!', 'success');
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Calculate risk';
  }
});

async function loadPolicies() {
  if (!state.token) return;
  const grid = document.getElementById('policiesGrid');
  grid.innerHTML = '<div class="loader">Loading policies...</div>';
  try {
    const policies = await api('GET', '/policies');
    state.policies = policies;
    grid.innerHTML = '';

    if (!policies.length) {
      grid.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">📋</div>
          <p>You have no active policies</p>
          <button class="btn btn-outline" onclick="navigate('quote')">Get a quote</button>
        </div>`;
      return;
    }

    policies.forEach(p => grid.appendChild(makePolicyCard(p)));
  } catch(err) {
    toast(err.message, 'error');
  }
}

function makePolicyCard(p) {
  const card = document.createElement('div');
  card.className = 'card';
  card.innerHTML = `
    <div class="card-header">
      <span class="card-num">${p.policyNumber}</span>
      <div class="card-actions">
        <span class="badge badge-${p.status}">${p.status}</span>
        ${p.status === 'ACTIVE' ? `<button class="btn btn-danger btn-sm" onclick="cancelPolicy(${p.id})">Cancel</button>` : ''}
      </div>
    </div>
    <div class="card-body">
      <div class="card-row"><span class="card-key">Zone</span><span class="card-val">${p.residenceZone}</span></div>
      <div class="card-row"><span class="card-key">Coverage</span><span class="card-val">$${Number(p.coverageAmount).toLocaleString()}</span></div>
      <div class="card-row"><span class="card-key">Nights outdoors</span><span class="card-val">${p.nightsOutdoorPerWeek}/wk</span></div>
      <div class="card-row"><span class="card-key">Seen UFOs</span><span class="card-val">${p.hasReportedUfos ? '⚠️ Yes' : 'No'}</span></div>
      <div class="card-row"><span class="card-key">Microchip</span><span class="card-val">${p.hasMicrochip ? '🚨 Detected' : 'No'}</span></div>
      <div class="card-row"><span class="card-key">Valid</span><span class="card-val">${p.startDate} → ${p.endDate}</span></div>
      <div class="card-premium">$${Number(p.monthlyPremium).toLocaleString()} / mo</div>
    </div>`;
  return card;
}

async function cancelPolicy(id) {
  if (!confirm('Cancel this policy? This cannot be undone.')) return;
  try {
    await api('PATCH', `/policies/${id}/cancel`);
    toast('Policy cancelled.', 'info');
    loadPolicies();
  } catch(err) {
    toast(err.message, 'error');
  }
}

document.getElementById('newPolicyBtn').addEventListener('click', () => {
  const today = new Date().toISOString().split('T')[0];
  const next  = new Date(Date.now() + 365*86400000).toISOString().split('T')[0];
  document.getElementById('pStart').value = today;
  document.getElementById('pEnd').value   = next;
  document.getElementById('policyModal').classList.remove('hidden');
});

document.getElementById('closePolicyModal').addEventListener('click', () => {
  document.getElementById('policyModal').classList.add('hidden');
});

document.getElementById('policyForm').addEventListener('submit', async e => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Creating...';
  try {
    await api('POST', '/policies', {
      residenceZone:        document.getElementById('pZone').value,
      nightsOutdoorPerWeek: +document.getElementById('pNights').value,
      coverageAmount:       +document.getElementById('pCoverage').value,
      hasReportedUfos:      document.getElementById('pUfos').checked,
      hasMicrochip:         document.getElementById('pChip').checked,
      startDate:            document.getElementById('pStart').value,
      endDate:              document.getElementById('pEnd').value,
    });
    document.getElementById('policyModal').classList.add('hidden');
    toast('Policy created!', 'success');
    loadPolicies();
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Create policy';
  }
});

async function loadClaims() {
  if (!state.token) return;
  const grid = document.getElementById('claimsGrid');
  grid.innerHTML = '<div class="loader">Loading claims...</div>';
  try {
    const claims = await api('GET', '/claims');
    grid.innerHTML = '';
    if (!claims.length) {
      grid.innerHTML = `
        <div class="empty-state">
          <div class="empty-icon">🛸</div>
          <p>No abduction claims on file</p>
          <p class="empty-sub">We hope it stays that way.</p>
        </div>`;
      return;
    }
    claims.forEach(c => grid.appendChild(makeClaimCard(c)));
  } catch(err) {
    toast(err.message, 'error');
  }
}

function makeClaimCard(c) {
  const card = document.createElement('div');
  card.className = 'card';
  card.innerHTML = `
    <div class="card-header">
      <span class="card-num">${c.claimNumber}</span>
      <span class="badge badge-${c.status}">${c.status}</span>
    </div>
    <div class="card-body">
      <div class="card-row"><span class="card-key">Policy</span><span class="card-val">${c.policyNumber}</span></div>
      <div class="card-row"><span class="card-key">Location</span><span class="card-val">${c.abductionLocation}</span></div>
      <div class="card-row"><span class="card-key">Date</span><span class="card-val">${new Date(c.abductionDateTime).toLocaleString('en')}</span></div>
      <div class="card-row"><span class="card-key">Hours missing</span><span class="card-val">${c.hoursMissing} hrs</span></div>
      <div class="card-row"><span class="card-key">Witnesses</span><span class="card-val">${c.witnessesPresent ? '✅ Yes' : 'No'}</span></div>
      <div class="card-row"><span class="card-key">Medical evidence</span><span class="card-val">${c.probeEvidence ? '🔬 Yes' : 'No'}</span></div>
      <div class="card-row"><span class="card-key">Claimed</span><span class="card-val">$${Number(c.claimedAmount).toLocaleString()}</span></div>
      ${c.approvedAmount ? `<div class="card-row"><span class="card-key">Approved</span><span class="card-val" style="color:var(--green)">$${Number(c.approvedAmount).toLocaleString()}</span></div>` : ''}
      <div class="card-desc">${c.incidentDescription?.substring(0, 120)}${c.incidentDescription?.length > 120 ? '...' : ''}</div>
      ${c.reviewerNotes ? `<div class="card-desc" style="color:var(--cyan);border-color:var(--border-hi)">📝 ${c.reviewerNotes}</div>` : ''}
    </div>`;
  return card;
}

document.getElementById('newClaimBtn').addEventListener('click', async () => {
  const sel = document.getElementById('claimPolicyId');
  sel.innerHTML = '';
  const activePolicies = state.policies.filter(p => p.status === 'ACTIVE');
  if (!activePolicies.length) {
    toast('You have no active policies to submit a claim for.', 'error');
    return;
  }
  activePolicies.forEach(p => {
    const opt = document.createElement('option');
    opt.value = p.id;
    opt.textContent = `${p.policyNumber} — $${Number(p.coverageAmount).toLocaleString()}`;
    sel.appendChild(opt);
  });

  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  document.getElementById('claimDateTime').value = now.toISOString().slice(0,16);
  document.getElementById('claimModal').classList.remove('hidden');
});

document.getElementById('closeClaimModal').addEventListener('click', () => {
  document.getElementById('claimModal').classList.add('hidden');
});

document.getElementById('claimForm').addEventListener('submit', async e => {
  e.preventDefault();
  const desc = document.getElementById('claimDesc').value;
  if (desc.length < 50) {
    toast('Description must be at least 50 characters.', 'error');
    return;
  }
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Submitting...';
  try {
    await api('POST', '/claims', {
      policyId:             +document.getElementById('claimPolicyId').value,
      abductionDateTime:    document.getElementById('claimDateTime').value + ':00',
      abductionLocation:    document.getElementById('claimLocation').value,
      incidentDescription:  desc,
      hoursMissing:         +document.getElementById('claimHours').value,
      witnessesPresent:     document.getElementById('claimWitness').checked,
      probeEvidence:        document.getElementById('claimProbe').checked,
      claimedAmount:        +document.getElementById('claimAmount').value,
    });
    document.getElementById('claimModal').classList.add('hidden');
    toast('Claim submitted! We will be in touch.', 'success');
    loadClaims();
    e.target.reset();
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Submit claim';
  }
});

async function loadAdminClaims() {
  if (state.role !== 'ROLE_ADMIN') return;
  const grid = document.getElementById('adminClaimsGrid');
  grid.innerHTML = '<div class="loader">Loading...</div>';
  try {
    const claims = await api('GET', '/claims/admin/all');
    grid.innerHTML = '';
    if (!claims.length) {
      grid.innerHTML = '<div class="empty-state"><div class="empty-icon">📭</div><p>No claims found</p></div>';
      return;
    }
    claims.forEach(c => {
      const card = makeClaimCard(c);
      if (!['APPROVED','REJECTED'].includes(c.status)) {
        const btn = document.createElement('button');
        btn.className = 'btn btn-primary btn-full';
        btn.style.marginTop = '0.75rem';
        btn.textContent = 'Review claim';
        btn.onclick = () => openReviewModal(c);
        card.querySelector('.card-body').appendChild(btn);
      }
      grid.appendChild(card);
    });
  } catch(err) {
    toast(err.message, 'error');
  }
}

function openReviewModal(claim) {
  state.currentClaimId = claim.id;
  document.getElementById('reviewClaimInfo').innerHTML = `
    <strong style="color:var(--cyan)">${claim.claimNumber}</strong> · ${claim.policyNumber}<br>
    📍 ${claim.abductionLocation} · ⏱ ${claim.hoursMissing} hours missing<br>
    💰 Claimed: $${Number(claim.claimedAmount).toLocaleString()}<br>
    <em>${claim.incidentDescription?.substring(0,160)}...</em>
  `;
  document.getElementById('reviewAmount').value = claim.claimedAmount;
  document.getElementById('reviewModal').classList.remove('hidden');
}

document.getElementById('closeReviewModal').addEventListener('click', () => {
  document.getElementById('reviewModal').classList.add('hidden');
});

document.getElementById('reviewForm').addEventListener('submit', async e => {
  e.preventDefault();
  const btn = e.target.querySelector('button');
  btn.disabled = true; btn.textContent = 'Saving...';
  try {
    await api('PATCH', `/claims/${state.currentClaimId}/review`, {
      status:         document.getElementById('reviewStatus').value,
      approvedAmount: +document.getElementById('reviewAmount').value || null,
      reviewerNotes:  document.getElementById('reviewNotes').value,
    });
    document.getElementById('reviewModal').classList.add('hidden');
    toast('Decision saved.', 'success');
    loadAdminClaims();
    e.target.reset();
  } catch(err) {
    toast(err.message, 'error');
  } finally {
    btn.disabled = false; btn.textContent = 'Save decision';
  }
});

document.getElementById('refreshAdmin').addEventListener('click', loadAdminClaims);

document.addEventListener('click', e => {
  const target = e.target.closest('[data-page]');
  if (!target) return;

  const page = target.dataset.page;

  if (['policies','claims'].includes(page) && !state.token) {
    toast('Please sign in first.', 'error');
    navigate('login');
    return;
  }
  if (page === 'admin' && state.role !== 'ROLE_ADMIN') {
    toast('Access denied.', 'error');
    return;
  }

  if (target.id === 'heroLoginBtn' && state.token) {
    navigate('policies');
    return;
  }

  navigate(page);
});

document.getElementById('authBtn').addEventListener('click', () => navigate('login'));
document.getElementById('logoutBtn').addEventListener('click', logout);

initStars();
updateAuthUI();
navigate('home');