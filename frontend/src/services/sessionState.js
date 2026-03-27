const STORAGE_KEY = 'votacao:pauta-session-state';

export const SESSION_STATUS = {
  CRIADA: 'CRIADA',
  ABERTA: 'ABERTA',
  ENCERRADA: 'ENCERRADA',
};

const getStorage = () => {
  try {
    const raw = localStorage.getItem(STORAGE_KEY);
    if (!raw) return {};

    const parsed = JSON.parse(raw);
    return parsed && typeof parsed === 'object' ? parsed : {};
  } catch {
    return {};
  }
};

const saveStorage = (data) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
};

const normalizeInfo = (info) => {
  if (!info || typeof info !== 'object') {
    return { status: SESSION_STATUS.CRIADA };
  }

  const closeAtMs = Number(info.closeAtMs);
  if (info.status === SESSION_STATUS.ABERTA && Number.isFinite(closeAtMs) && Date.now() >= closeAtMs) {
    return {
      ...info,
      status: SESSION_STATUS.ENCERRADA,
      closedAt: new Date(closeAtMs).toISOString(),
    };
  }

  return info;
};

export const markPautaCreated = (pautaId) => {
  if (!pautaId) return;

  const all = getStorage();
  all[pautaId] = {
    ...(all[pautaId] || {}),
    status: SESSION_STATUS.CRIADA,
    updatedAt: new Date().toISOString(),
  };
  saveStorage(all);
};

export const openPautaSession = (pautaId, durationSeconds) => {
  if (!pautaId) return;

  const seconds = Number(durationSeconds);
  const safeSeconds = Number.isFinite(seconds) && seconds > 0 ? seconds : 60;
  const openedAtMs = Date.now();
  const closeAtMs = openedAtMs + safeSeconds * 1000;

  const all = getStorage();
  all[pautaId] = {
    ...(all[pautaId] || {}),
    status: SESSION_STATUS.ABERTA,
    durationSeconds: safeSeconds,
    openedAt: new Date(openedAtMs).toISOString(),
    closeAt: new Date(closeAtMs).toISOString(),
    closeAtMs,
    updatedAt: new Date().toISOString(),
  };

  saveStorage(all);
};

export const setPautaSessionStatus = (pautaId, status) => {
  if (!pautaId || !status) return;

  const all = getStorage();
  const current = all[pautaId] || {};
  const next = {
    ...current,
    status,
    updatedAt: new Date().toISOString(),
  };

  if (status === SESSION_STATUS.CRIADA) {
    delete next.durationSeconds;
    delete next.openedAt;
    delete next.closeAt;
    delete next.closeAtMs;
    delete next.closedAt;
  }

  if (status === SESSION_STATUS.ABERTA) {
    delete next.closedAt;
  }

  if (status === SESSION_STATUS.ENCERRADA) {
    delete next.closeAtMs;
    if (!next.closedAt) {
      next.closedAt = new Date().toISOString();
    }
  }

  all[pautaId] = next;
  saveStorage(all);
};

export const getPautaSessionInfo = (pautaId) => {
  if (!pautaId) return { status: SESSION_STATUS.CRIADA };

  const all = getStorage();
  const normalized = normalizeInfo(all[pautaId]);

  if (normalized.status !== (all[pautaId] || {}).status) {
    all[pautaId] = normalized;
    saveStorage(all);
  }

  return normalized.status ? normalized : { status: SESSION_STATUS.CRIADA };
};

