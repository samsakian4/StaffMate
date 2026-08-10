import { ensureDefaultSettings, getSetting } from './db.js';
import { route, startRouter, navigate } from './router.js';
import { renderDashboard } from './screens/dashboard.js';
import { renderEmployees } from './screens/employees.js';
import { renderEmployeeForm } from './screens/employeeForm.js';
import { renderEmployeeProfile } from './screens/employeeProfile.js';
import { renderQuickAdd } from './screens/quickAdd.js';
import { renderNoteForm } from './screens/noteForm.js';
import { renderReports } from './screens/reports.js';
import { renderSettings } from './screens/settings.js';
import { renderPin } from './screens/pin.js';

async function guarded(renderFn) {
  return async (container, params) => {
    const pinEnabled = (await getSetting('pin_enabled', '0')) === '1';
    if (pinEnabled && sessionStorage.getItem('pin_verified') !== '1') {
      navigate('#/pin');
      return;
    }
    return renderFn(container, params);
  };
}

async function init() {
  await ensureDefaultSettings();

  route('#/pin', renderPin);
  route('#/dashboard', await guarded(renderDashboard));
  route('#/employees', await guarded(renderEmployees));
  route('#/employee-form/:id', await guarded(renderEmployeeForm));
  route('#/employee/:id', await guarded(renderEmployeeProfile));
  route('#/quick-add', await guarded(renderQuickAdd));
  route('#/note-form/:type/:employeeId/:noteId', await guarded(renderNoteForm));
  route('#/reports', await guarded(renderReports));
  route('#/settings', await guarded(renderSettings));

  startRouter();

  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.register('./sw.js').catch(() => {});
  }
}

init();
