// Offline-first service worker: cache-first for the app shell.
// No network calls are ever made — this only enables offline installability.
const CACHE_NAME = 'staffmate-v2';
const ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './css/style.css',
  './js/app.js',
  './js/db.js',
  './js/router.js',
  './js/layout.js',
  './js/utils.js',
  './js/scoring.js',
  './js/config.js',
  './js/supabaseClient.js',
  './js/vendor/supabase.js',
  './js/auth.js',
  './js/screens/login.js',
  './js/screens/dashboard.js',
  './js/screens/employees.js',
  './js/screens/employeeForm.js',
  './js/screens/employeeProfile.js',
  './js/screens/quickAdd.js',
  './js/screens/noteForm.js',
  './js/screens/reports.js',
  './js/screens/settings.js',
  './js/screens/pin.js',
  './icons/icon-192.png',
  './icons/icon-512.png',
  './icons/icon-maskable-512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS)).then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  if (event.request.method !== 'GET') return;
  event.respondWith(
    caches.match(event.request).then((cached) => {
      if (cached) return cached;
      return fetch(event.request)
        .then((response) => {
          const copy = response.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(event.request, copy));
          return response;
        })
        .catch(() => cached);
    })
  );
});
