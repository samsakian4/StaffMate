// IndexedDB wrapper for StaffMate PWA — fully offline, no network calls.
const DB_NAME = 'staffmate';
const DB_VERSION = 1;

let dbPromise = null;

function openDb() {
  if (dbPromise) return dbPromise;
  dbPromise = new Promise((resolve, reject) => {
    const req = indexedDB.open(DB_NAME, DB_VERSION);
    req.onupgradeneeded = (e) => {
      const db = e.target.result;
      if (!db.objectStoreNames.contains('employees')) {
        const s = db.createObjectStore('employees', { keyPath: 'id', autoIncrement: true });
        s.createIndex('personnelCode', 'personnelCode', { unique: true });
        s.createIndex('active', 'active', { unique: false });
      }
      if (!db.objectStoreNames.contains('positiveNotes')) {
        const s = db.createObjectStore('positiveNotes', { keyPath: 'id', autoIncrement: true });
        s.createIndex('employeeId', 'employeeId', { unique: false });
      }
      if (!db.objectStoreNames.contains('negativeNotes')) {
        const s = db.createObjectStore('negativeNotes', { keyPath: 'id', autoIncrement: true });
        s.createIndex('employeeId', 'employeeId', { unique: false });
      }
      if (!db.objectStoreNames.contains('personalityNotes')) {
        const s = db.createObjectStore('personalityNotes', { keyPath: 'id', autoIncrement: true });
        s.createIndex('employeeId', 'employeeId', { unique: false });
      }
      if (!db.objectStoreNames.contains('disciplinaryRecords')) {
        const s = db.createObjectStore('disciplinaryRecords', { keyPath: 'id', autoIncrement: true });
        s.createIndex('employeeId', 'employeeId', { unique: false });
      }
      if (!db.objectStoreNames.contains('settings')) {
        db.createObjectStore('settings', { keyPath: 'key' });
      }
    };
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
  return dbPromise;
}

function tx(storeName, mode) {
  return openDb().then(db => db.transaction(storeName, mode).objectStore(storeName));
}

function reqToPromise(req) {
  return new Promise((resolve, reject) => {
    req.onsuccess = () => resolve(req.result);
    req.onerror = () => reject(req.error);
  });
}

export const DB = {
  async add(store, value) {
    const s = await tx(store, 'readwrite');
    return reqToPromise(s.add(value));
  },
  async put(store, value) {
    const s = await tx(store, 'readwrite');
    return reqToPromise(s.put(value));
  },
  async get(store, id) {
    const s = await tx(store, 'readonly');
    return reqToPromise(s.get(id));
  },
  async delete(store, id) {
    const s = await tx(store, 'readwrite');
    return reqToPromise(s.delete(id));
  },
  async getAll(store) {
    const s = await tx(store, 'readonly');
    return reqToPromise(s.getAll());
  },
  async getAllByIndex(store, indexName, value) {
    const s = await tx(store, 'readonly');
    const idx = s.index(indexName);
    return reqToPromise(idx.getAll(value));
  },
  async count(store) {
    const s = await tx(store, 'readonly');
    return reqToPromise(s.count());
  },
  async clearAll() {
    const db = await openDb();
    const names = ['employees', 'positiveNotes', 'negativeNotes', 'personalityNotes', 'disciplinaryRecords', 'settings'];
    const t = db.transaction(names, 'readwrite');
    names.forEach(n => t.objectStore(n).clear());
    return new Promise((resolve, reject) => {
      t.oncomplete = () => resolve();
      t.onerror = () => reject(t.error);
    });
  },
  async exportAll() {
    const names = ['employees', 'positiveNotes', 'negativeNotes', 'personalityNotes', 'disciplinaryRecords', 'settings'];
    const out = {};
    for (const n of names) out[n] = await DB.getAll(n);
    out.__meta = { app: 'StaffMate', exportedAt: new Date().toISOString(), version: DB_VERSION };
    return out;
  },
  async importAll(data) {
    await DB.clearAll();
    const db = await openDb();
    const names = ['employees', 'positiveNotes', 'negativeNotes', 'personalityNotes', 'disciplinaryRecords', 'settings'];
    const t = db.transaction(names, 'readwrite');
    for (const n of names) {
      const rows = data[n] || [];
      const store = t.objectStore(n);
      rows.forEach(r => store.put(r));
    }
    return new Promise((resolve, reject) => {
      t.oncomplete = () => resolve();
      t.onerror = () => reject(t.error);
    });
  }
};

export const DEFAULT_SETTINGS = {
  weight_positive: '1',
  weight_negative: '-1',
  severity_low: '-1',
  severity_medium: '-3',
  severity_high: '-5',
  severity_very_high: '-8',
  shifts_list: 'صبح,عصر,شب',
  positions_list: 'اپراتور,سرشیفت,کارگر ساده,تکنسین',
  workplaces_list: 'خط تولید ۱,خط تولید ۲,انبار',
  violation_types_list: 'غیبت,تاخیر,عدم رعایت ایمنی,نافرمانی,سایر',
  importance_list: 'کم,متوسط,زیاد',
  pin_enabled: '0',
  pin_hash: '',
  last_backup_date: ''
};

export async function ensureDefaultSettings() {
  for (const [key, value] of Object.entries(DEFAULT_SETTINGS)) {
    const existing = await DB.get('settings', key);
    if (!existing) await DB.put('settings', { key, value });
  }
}

export async function getSetting(key, fallback = '') {
  const row = await DB.get('settings', key);
  return row ? row.value : fallback;
}

export async function setSetting(key, value) {
  return DB.put('settings', { key, value });
}
