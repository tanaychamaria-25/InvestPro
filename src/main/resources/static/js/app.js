'use strict';

const state = { user: null, investments: [], dashboard: null, activeFilter: 'ALL', trade: null, authMode: 'login' };
const $ = (id) => document.getElementById(id);
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 2 }).format(Number(value || 0));
const number = (value) => new Intl.NumberFormat('en-IN', { maximumFractionDigits: 2 }).format(Number(value || 0));
const safe = (value) => String(value ?? '').replace(/[&<>"']/g, (char) => ({ '&':'&amp;', '<':'&lt;', '>':'&gt;', '"':'&quot;', "'":'&#39;' })[char]);

function toast(message, isError = false) {
  const item = $('toast'); item.textContent = message; item.classList.toggle('error', isError); item.classList.add('show');
  window.clearTimeout(toast.timer); toast.timer = window.setTimeout(() => item.classList.remove('show'), 3200);
}
async function api(url, options = {}) {
  const config = { credentials: 'same-origin', ...options, headers: { ...(options.body ? { 'Content-Type': 'application/json' } : {}), ...(options.headers || {}) } };
  const response = await fetch(url, config);
  const type = response.headers.get('content-type') || '';
  const payload = type.includes('application/json') ? await response.json() : await response.text();
  if (!response.ok) {
    if (response.status === 401) showAuth();
    throw new Error(payload && payload.error ? payload.error : 'The request could not be completed.');
  }
  return payload;
}
function showAuth() { $('authView').classList.remove('hidden'); $('appView').classList.add('hidden'); state.user = null; }
function showApp() { $('authView').classList.add('hidden'); $('appView').classList.remove('hidden'); }
function setAuthMode(mode) {
  state.authMode = mode;
  const registering = mode === 'register';
  $('loginTab').classList.toggle('active', !registering); $('registerTab').classList.toggle('active', registering);
  $('nameField').classList.toggle('hidden', !registering); $('fullName').required = registering;
  $('password').autocomplete = registering ? 'new-password' : 'current-password';
  $('authSubmit').innerHTML = registering ? 'Create your account <span aria-hidden="true">↗</span>' : 'Sign in to InvestPro <span aria-hidden="true">↗</span>';
  $('authError').classList.add('hidden'); $('authError').textContent = '';
}
$('loginTab').addEventListener('click', () => setAuthMode('login'));
$('registerTab').addEventListener('click', () => setAuthMode('register'));
$('authForm').addEventListener('submit', async (event) => {
  event.preventDefault(); $('authSubmit').disabled = true; $('authError').classList.add('hidden');
  const body = { email: $('email').value.trim(), password: $('password').value };
  if (state.authMode === 'register') body.fullName = $('fullName').value.trim();
  try {
    const result = await api(`/api/auth/${state.authMode}`, { method: 'POST', body: JSON.stringify(body) });
    state.user = result.user; toast(result.message || 'Welcome to InvestPro.'); await loadApp();
  } catch (error) { $('authError').textContent = error.message; $('authError').classList.remove('hidden'); }
  finally { $('authSubmit').disabled = false; }
});
$('logoutButton').addEventListener('click', async () => {
  try { await api('/api/auth/logout', { method: 'POST' }); } catch (_) { /* A stale session can still be cleared in the UI. */ }
  showAuth(); $('password').value = ''; setAuthMode('login'); toast('You have signed out.');
});

async function loadApp() {
  try {
    const [user, investments, dashboard, transactions] = await Promise.all([
      api('/api/auth/me'), api('/api/investments'), api('/api/dashboard'), api('/api/transactions')
    ]);
    state.user = user; state.investments = investments; state.dashboard = dashboard; state.transactions = transactions;
    showApp(); renderAll();
  } catch (error) { if (!state.user) showAuth(); else toast(error.message, true); }
}
function badgeClass(type) { return type === 'BOND' ? 'bond' : type === 'MUTUAL_FUND' ? 'fund' : ''; }
function badgeText(investment) { return investment.type === 'MUTUAL_FUND' ? 'MF' : investment.type === 'BOND' ? 'BD' : safe(investment.symbol.slice(0, 2)); }
function assetCell(investment, subtitle = '') {
  return `<div class="asset-cell"><span class="asset-badge ${badgeClass(investment.type)}">${badgeText(investment)}</span><span><strong>${safe(investment.name)}</strong><small>${safe(investment.symbol)}${subtitle ? ` · ${safe(subtitle)}` : ''}</small></span></div>`;
}
function colorValue(value) { const n = Number(value || 0); return `<span class="${n > 0 ? 'positive' : n < 0 ? 'negative' : ''}">${n > 0 ? '+' : ''}${money(n)}</span>`; }
function renderAll() {
  const d = state.dashboard || {};
  const fullName = state.user?.fullName || 'Investor'; const firstName = fullName.trim().split(/\s+/)[0] || 'Investor';
  $('profileName').textContent = fullName; $('profileEmail').textContent = state.user?.email || 'Personal account'; $('avatar').textContent = firstName.charAt(0).toUpperCase(); $('welcomeName').textContent = firstName;
  $('cashBalance').textContent = money(d.cashBalance); $('marketValue').textContent = money(d.marketValue); $('totalEquity').textContent = money(d.totalEquity);
  $('profitLoss').textContent = money(d.unrealizedProfitLoss); $('portfolioMarketValue').textContent = money(d.marketValue); $('portfolioProfitLoss').textContent = money(d.unrealizedProfitLoss);
  $('profitLoss').classList.toggle('positive', Number(d.unrealizedProfitLoss) > 0); $('profitLoss').classList.toggle('negative', Number(d.unrealizedProfitLoss) < 0);
  const holdings = d.holdings || []; $('positionCount').textContent = number(holdings.length);
  renderHoldings(holdings); renderFeatured(); renderMarketplace(); renderTransactions(state.transactions || []);
}
function investmentById(id) { return state.investments.find((item) => Number(item.id) === Number(id)); }
function holdingRows(holdings, full = false) {
  if (!holdings.length) return `<tr><td colspan="${full ? 7 : 6}" class="empty-cell">No investments yet. Visit the marketplace to make your first simulated trade.</td></tr>`;
  return holdings.map((h) => {
    const investment = investmentById(h.investmentId || h.investmentid || h.id) || { id: h.investmentId, symbol: h.symbol, name: h.name, type: h.type || 'STOCK' };
    const qty = Number(h.quantity || 0); const avg = Number(h.averageCost ?? h.average_cost ?? 0); const price = Number(h.currentPrice ?? h.currentprice ?? 0); const market = Number(h.marketValue ?? h.marketvalue ?? price * qty); const pnl = Number(h.profitLoss ?? h.profitloss ?? (price - avg) * qty);
    return `<tr><td>${assetCell(investment)}</td><td>${number(qty)}</td><td>${money(avg)}</td>${full ? `<td>${money(price)}</td>` : ''}<td>${money(market)}</td><td>${colorValue(pnl)}</td><td><button class="small-action" data-trade="SELL" data-id="${Number(investment.id)}">Sell</button></td></tr>`;
  }).join('');
}
function renderHoldings(holdings) {
  $('overviewHoldings').innerHTML = holdings.length ? holdingRows(holdings, false) : '<tr><td colspan="6" class="empty-cell">Your portfolio is empty for now. Start with a small simulated investment.</td></tr>';
  $('portfolioHoldings').innerHTML = holdings.length ? holdingRows(holdings, true) : '<tr><td colspan="7" class="empty-cell">No holdings yet. Buy a product from the marketplace to see it here.</td></tr>';
}
function investmentCard(item) {
  const typeName = item.type === 'MUTUAL_FUND' ? 'Mutual fund' : item.type === 'BOND' ? 'Bond' : 'Stock';
  return `<article class="investment-card"><div class="investment-card-top"><span class="asset-badge ${badgeClass(item.type)}">${badgeText(item)}</span><span class="type-tag">${typeName}</span></div><h4>${safe(item.name)}</h4><div class="symbol-line">${safe(item.symbol)} <span class="risk-tag">· ${safe(item.riskLevel || '')}</span></div><p class="description">${safe(item.description || item.investorNote || 'Simulated investment product.')} </p><div class="price-row"><div><strong>${money(item.price)}</strong><small>Per unit · sample price</small></div><button class="button button-primary" data-trade="BUY" data-id="${Number(item.id)}">Buy <span>↗</span></button></div></article>`;
}
function renderFeatured() { $('featuredInvestments').innerHTML = state.investments.slice(0, 3).map(investmentCard).join(''); }
function renderMarketplace() {
  const search = ($('investmentSearch')?.value || '').trim().toLowerCase();
  const list = state.investments.filter((item) => (state.activeFilter === 'ALL' || item.type === state.activeFilter) && `${item.name} ${item.symbol} ${item.type}`.toLowerCase().includes(search));
  $('allInvestments').innerHTML = list.length ? list.map(investmentCard).join('') : '<p class="empty-cell">No products match this search.</p>';
}
function dateFormat(value) { if (!value) return '—'; const parsed = new Date(value); return Number.isNaN(parsed.getTime()) ? String(value).replace('T', ' ').slice(0, 16) : parsed.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' }); }
function renderTransactions(transactions) {
  $('transactionRows').innerHTML = transactions.length ? transactions.map((t) => `<tr><td>${safe(dateFormat(t.createdAt || t.created_at))}</td><td><span class="${String(t.action || '').toUpperCase() === 'BUY' ? 'negative' : 'positive'}">${safe(t.action || t.transactionType || t.transaction_type)}</span></td><td>${assetCell({ name: t.name, symbol: t.symbol, type: t.type || 'STOCK' })}</td><td>${number(t.quantity)}</td><td>${money(t.price)}</td><td>${money(t.totalAmount ?? t.total_amount)}</td></tr>`).join('') : '<tr><td colspan="6" class="empty-cell">No transactions yet. Your trades will appear here.</td></tr>';
}

function navigate(target) {
  document.querySelectorAll('.page-section').forEach((el) => el.classList.toggle('hidden', el.id !== target));
  document.querySelectorAll('.nav-item').forEach((el) => el.classList.toggle('active', el.dataset.target === target));
  const names = { overview: 'Overview', marketplace: 'Marketplace', portfolio: 'My portfolio', activity: 'Transaction activity' };
  $('pageHeading').textContent = names[target] || 'Overview';
  window.scrollTo({ top: 0, behavior: 'smooth' });
  if (target === 'marketplace') renderMarketplace();
}
document.querySelectorAll('.nav-item').forEach((item) => item.addEventListener('click', () => navigate(item.dataset.target)));
document.querySelectorAll('[data-jump]').forEach((item) => item.addEventListener('click', () => navigate(item.dataset.jump)));
$('investmentSearch').addEventListener('input', renderMarketplace);
$('simulateMarketButton').addEventListener('click', async () => {
  const button = $('simulateMarketButton');
  button.disabled = true; button.textContent = 'Simulating market…';
  try {
    const result = await api('/api/investments/simulate', { method: 'POST' });
    await loadApp();
    toast(result.message || 'Illustrative prices updated. Portfolio values have been refreshed.');
    navigate('marketplace');
  } catch (error) { toast(error.message, true); }
  finally { button.disabled = false; button.textContent = '↗ Simulate market movement'; }
});
$('filterGroup').addEventListener('click', (event) => { const button = event.target.closest('[data-filter]'); if (!button) return; state.activeFilter = button.dataset.filter; document.querySelectorAll('.filter-pill').forEach((pill) => pill.classList.toggle('active', pill === button)); renderMarketplace(); });

document.body.addEventListener('click', (event) => {
  const button = event.target.closest('[data-trade]'); if (!button) return;
  const investment = investmentById(button.dataset.id); if (!investment) { toast('That investment could not be loaded. Refresh and try again.', true); return; }
  openTrade(investment, button.dataset.trade);
});
function openTrade(investment, action) {
  state.trade = { investment, action };
  $('tradeActionLabel').textContent = action === 'BUY' ? 'PLACE A SIMULATED ORDER' : 'SELL FROM YOUR PORTFOLIO'; $('tradeTitle').textContent = action === 'BUY' ? 'Buy investment' : 'Sell investment';
  $('tradeName').textContent = investment.name; $('tradeSymbol').textContent = investment.symbol; $('tradePrice').textContent = money(investment.price);
  $('tradeBadge').className = `asset-badge ${badgeClass(investment.type)}`; $('tradeBadge').textContent = badgeText(investment); $('tradeQuantity').value = '1';
  $('tradeHelp').textContent = action === 'BUY' ? `Available cash: ${money(state.dashboard?.cashBalance)}. The total must fit your simulated cash balance.` : 'You can sell only the units currently held in your portfolio.';
  $('confirmTrade').textContent = action === 'BUY' ? 'Confirm simulated buy' : 'Confirm simulated sale'; $('confirmTrade').classList.toggle('button-primary', true); $('tradeError').classList.add('hidden'); updateTradeTotal(); $('tradeDialog').showModal();
}
function updateTradeTotal() { const qty = Math.max(0, Number($('tradeQuantity').value || 0)); const price = Number(state.trade?.investment.price || 0); $('tradeTotal').textContent = money(qty * price); }
$('tradeQuantity').addEventListener('input', updateTradeTotal);
$('minusQuantity').addEventListener('click', () => { $('tradeQuantity').value = Math.max(1, Number($('tradeQuantity').value || 1) - 1); updateTradeTotal(); });
$('plusQuantity').addEventListener('click', () => { $('tradeQuantity').value = Math.min(1000000, Number($('tradeQuantity').value || 1) + 1); updateTradeTotal(); });
$('closeTrade').addEventListener('click', () => $('tradeDialog').close());
$('tradeForm').addEventListener('submit', async (event) => {
  event.preventDefault(); if (!state.trade) return; const quantity = Number($('tradeQuantity').value);
  if (!Number.isInteger(quantity) || quantity < 1 || quantity > 1000000) { $('tradeError').textContent = 'Enter a whole number of units from 1 to 1,000,000.'; $('tradeError').classList.remove('hidden'); return; }
  $('confirmTrade').disabled = true; $('tradeError').classList.add('hidden');
  try {
    const result = await api('/api/trades', { method: 'POST', body: JSON.stringify({ investmentId: state.trade.investment.id, action: state.trade.action, quantity }) });
    $('tradeDialog').close(); toast(result.message || 'Trade completed.'); await loadApp();
  } catch (error) { $('tradeError').textContent = error.message; $('tradeError').classList.remove('hidden'); }
  finally { $('confirmTrade').disabled = false; }
});

(async function start() {
  try { state.user = await api('/api/auth/me'); await loadApp(); }
  catch (_) { showAuth(); }
})();
