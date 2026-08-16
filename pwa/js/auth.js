import { supabase } from './supabaseClient.js';

/** Wraps a promise so it never hangs forever — rejects after `ms` if unresolved. */
function withTimeout(promise, ms = 12000) {
  return Promise.race([
    promise,
    new Promise((_, reject) => setTimeout(() => reject(new Error('پاسخی از سرور دریافت نشد (Timeout). اتصال اینترنت را بررسی کنید.')), ms))
  ]);
}

export async function getSession() {
  const { data } = await withTimeout(supabase.auth.getSession());
  return data.session;
}

export async function signIn(email, password) {
  return withTimeout(supabase.auth.signInWithPassword({ email, password }));
}

export async function signUp(email, password) {
  return withTimeout(supabase.auth.signUp({ email, password }));
}

export async function signOut() {
  return supabase.auth.signOut();
}

export function onAuthChange(callback) {
  return supabase.auth.onAuthStateChange((_event, session) => callback(session));
}
