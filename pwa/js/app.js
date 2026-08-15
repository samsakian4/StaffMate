import { ensureDefaultSettings, getSetting } from './db.js';
import { getSession, onAuthChange, signOut } from './auth.js';
import { route, startRouter, navigate } from './router.js';
import { showToast } from './utils.js';
import { renderDashboard } from './screens/dashboard.js';
import { renderEmployees } from './screens/employees.js';
import { renderEmployeeForm } from './screens/employeeForm.js';
import { renderEmployeeProfile } from './screens/employeeProfile.js';
import { renderQuickAdd } from './screens/quickAdd.js';
import { renderNoteForm } from './screens/noteForm.js';
import { renderReports } from './screens/reports.js';
import { renderSettings } from './screens/settings.js';
import { renderPin } from './screens/pin.js';
import { renderLogin } from './screens/login.js';

async function guarded(renderFn) {
  return async (container, params) => {
    try {
      const session = await getSession();
      if (!session) {
        navigate('#/login');
        return;
      }
      const pinEnabled = (await getSetting('pin_enabled', '0')) === '1';
      if (pinEnabled && sessionStorage.getItem('pin_verified') !== '1') {
        navigate('#/pin');
        return;
      }
      await renderFn(container, params);
    } catch (e) {
      // Most likely an expired/invalid session talking to Supabase — clear it
      // and send the user back to login instead of leaving a frozen screen.
      console.error('Route render failed:', e);
      showToast('نشست شما منقضی شده. دوباره وارد شوید.');
      await signOut();
      navigate('#/login');
    }
  };
}

async function init() {
  route('#/login', renderLogin);
  route('#/pin', renderPin);
  route('#/dashboard', await guarded(renderDashboard));
  route('#/employees', await guarded(renderEmployees));
  route('#/employee-form/:id', await guarded(renderEmployeeForm));
  route('#/employee/:id', await guarded(renderEmployeeProfile));
  route('#/quick-add', await guarded(renderQuickAdd));
  route('#/note-form/:type/:employeeId/:noteId', await guarded(renderNoteForm));
  route('#/reports', await guarded(renderReports));
  route('#/settings', await guarded(renderSettings));

  try {
    const session = await getSession();
    if (session) {
      await ensureDefaultSettings();
    }
  } catch (e) {
    console.error('Startup settings check failed:', e);
    try { await signOut(); } catch (e2) { /* ignore */ }
  }

  onAuthChange((session) => {
    if (session) ensureDefaultSettings().catch((e) => console.error('ensureDefaultSettings failed:', e));
  });

  // startRouter() must always run — even if the checks above failed —
  // otherwise the page stays stuck on the static "loading" placeholder forever.
  startRouter();

  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').catch(() => {});
  }
}

init().catch((e) => {
  console.error('Fatal init error:', e);
  document.getElementById('app').innerHTML =
    '<div class="center-loading">خطایی رخ داد. صفحه را دوباره بارگذاری کنید.</div>';
});
