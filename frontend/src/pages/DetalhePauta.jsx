import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { api } from '../services/api';
import { getPautaSessionInfo, openPautaSession, setPautaSessionStatus, SESSION_STATUS } from '../services/sessionState';

function DetalhePauta() {
  const { id } = useParams();
  const [pauta, setPauta] = useState(null);
  const [loading, setLoading] = useState(true);

  const [cpf, setCpf] = useState('');
  const [sessionHours, setSessionHours] = useState('0');
  const [sessionMinutes, setSessionMinutes] = useState('2');
  const [sessionInfo, setSessionInfo] = useState({ status: SESSION_STATUS.CRIADA });
  const [resultadoVotacao, setResultadoVotacao] = useState(null);
  const [loadingResultado, setLoadingResultado] = useState(false);
  const [votingSim, setVotingSim] = useState(false);
  const [votingNao, setVotingNao] = useState(false);
  const [openingSession, setOpeningSession] = useState(false);

  const [modal, setModal] = useState({ isOpen: false, type: 'info', title: '', message: '' });

  const closeModal = () => setModal((prev) => ({ ...prev, isOpen: false }));

  const formatCpf = (value) => {
    const digits = value.replace(/\D/g, '').slice(0, 11);

    if (digits.length <= 3) return digits;
    if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`;
    if (digits.length <= 9) return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
    return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9)}`;
  };

  const getBackendErrorMessage = (error, fallbackMessage) => {
    const data = error?.response?.data;

    if (!data) return fallbackMessage;
    if (typeof data === 'string') return data;
    if (Array.isArray(data)) return data.join('; ');
    if (typeof data.message === 'string') return data.message;

    return fallbackMessage;
  };

  const formatDurationLabel = (hours, minutes) => {
    const partes = [];
    if (hours > 0) partes.push(`${hours} hora(s)`);
    if (minutes > 0) partes.push(`${minutes} minuto(s)`);
    return partes.length > 0 ? partes.join(' e ') : '0 minuto(s)';
  };

  const refreshSessionInfo = () => {
    setSessionInfo(getPautaSessionInfo(id));
  };

  const fetchResultadoVotacao = async () => {
    setLoadingResultado(true);
    try {
      const response = await api.get(`/pautas/${id}/resultado`);
      setResultadoVotacao(response?.data || null);
    } catch {
      setResultadoVotacao(null);
    } finally {
      setLoadingResultado(false);
    }
  };

  const syncSessionWithBackend = async () => {
    try {
      const response = await api.get(`/pautas/${id}/resultado`);
      const resultadoStatus = response?.data?.status;
      setResultadoVotacao(response?.data || null);

      if (resultadoStatus && resultadoStatus !== 'SEM_VOTOS') {
        setPautaSessionStatus(id, SESSION_STATUS.ENCERRADA);
        refreshSessionInfo();
      }
    } catch (error) {
      const message = String(error?.response?.data?.message || '').toLowerCase();

      const hasNoOpenSessionSignal =
        message.includes('nao existe sessao aberta') ||
        message.includes('não existe sessão aberta') ||
        message.includes('nenhuma sessao aberta') ||
        message.includes('nenhuma sessão aberta');

      const sessionOpen =
        (message.includes('ja existe sessao aberta') ||
          message.includes('já existe sessão aberta') ||
          message.includes('sessao aberta para esta pauta') ||
          message.includes('sessão aberta para esta pauta') ||
          message.includes('em andamento') ||
          message.includes('nao encerrada') ||
          message.includes('não encerrada')) &&
        !hasNoOpenSessionSignal;

      if (hasNoOpenSessionSignal) {
        setPautaSessionStatus(id, SESSION_STATUS.CRIADA);
        refreshSessionInfo();
        return;
      }

      if (sessionOpen) {
        setPautaSessionStatus(id, SESSION_STATUS.ABERTA);
        refreshSessionInfo();
      }
    }
  };

  useEffect(() => {
    api.get(`/pautas/${id}`)
      .then((response) => {
        setPauta(response.data);
        setResultadoVotacao(null);
        const localSession = getPautaSessionInfo(id);
        setSessionInfo(localSession);

        if (localSession.status === SESSION_STATUS.CRIADA) {
          syncSessionWithBackend();
        }
      })
      .catch((error) => {
        console.error('Erro ao buscar pauta:', error);
      })
      .finally(() => {
        setLoading(false);
      });
  }, [id]);

  useEffect(() => {
    if (sessionInfo.status === SESSION_STATUS.ENCERRADA) {
      fetchResultadoVotacao();
    }
  }, [id, sessionInfo.status]);

  useEffect(() => {
    if (sessionInfo.status !== SESSION_STATUS.ABERTA) return undefined;

    const intervalId = setInterval(() => {
      refreshSessionInfo();
    }, 1000);

    return () => clearInterval(intervalId);
  }, [sessionInfo.status]);

  const getSessionBadge = () => {
    if (sessionInfo.status === SESSION_STATUS.ABERTA) {
      return { label: 'Sessão aberta', classes: 'bg-primary-container text-on-primary-fixed' };
    }

    if (sessionInfo.status === SESSION_STATUS.ENCERRADA) {
      return { label: 'Sessão encerrada', classes: 'bg-secondary-container text-on-secondary-fixed' };
    }

    return { label: 'Pauta criada', classes: 'bg-surface-container-high text-on-surface-variant' };
  };

  const handleAbrirSessao = async () => {
    if (sessionInfo.status === SESSION_STATUS.ABERTA) {
      setModal({ isOpen: true, type: 'info', title: 'Sessão já está aberta', message: 'A sessão desta pauta já está em andamento.' });
      return;
    }

    if (sessionInfo.status === SESSION_STATUS.ENCERRADA) {
      setModal({ isOpen: true, type: 'error', title: 'Sessão encerrada', message: 'Esta pauta já foi encerrada e não pode ser reaberta por este fluxo.' });
      return;
    }

    const horas = Number(sessionHours || '0');
    const minutos = Number(sessionMinutes || '0');

    const horasValidas = Number.isInteger(horas) && horas >= 0;
    const minutosValidos = Number.isInteger(minutos) && minutos >= 0 && minutos <= 59;
    const totalMinutos = horas * 60 + minutos;

    if (!horasValidas || !minutosValidos || totalMinutos < 1) {
      setModal({ isOpen: true, type: 'error', title: 'Duração inválida', message: 'Informe um tempo válido em minutos (mínimo de 1 minuto).' });
      return;
    }

    setOpeningSession(true);
    try {
      const duracaoSegundos = totalMinutos * 60;
      await api.post(`/pautas/${id}/sessao`, { duracaoSegundos });

      openPautaSession(id, duracaoSegundos);
      refreshSessionInfo();

      setModal({
        isOpen: true,
        type: 'success',
        title: 'Sessão Aberta!',
        message: `A votação foi iniciada com duração de ${formatDurationLabel(horas, minutos)}.`,
      });
    } catch (error) {
      const isNetworkOrCorsError = !error?.response || error?.code === 'ERR_NETWORK';
      const backendMessageRaw = getBackendErrorMessage(error, 'Não foi possível abrir a votação.');
      const backendMessageNormalized = String(backendMessageRaw).toLowerCase();

      if (error?.response?.status === 409 && backendMessageNormalized.includes('ja existe sessao aberta')) {
        setPautaSessionStatus(id, SESSION_STATUS.ABERTA);
        refreshSessionInfo();

        setModal({
          isOpen: true,
          type: 'info',
          title: 'Sessão já estava aberta',
          message: 'A pauta já possuía uma sessão aberta. Sincronizamos o status para você seguir com a votação.',
        });
        return;
      }

      const backendMessage = isNetworkOrCorsError
        ? 'Não foi possível comunicar com a API. Verifique CORS/backend e tente novamente.'
        : backendMessageRaw;

      setModal({
        isOpen: true,
        type: 'error',
        title: 'Erro ao abrir sessão',
        message: `Não foi possível abrir a votação. Detalhes: ${backendMessage}`,
      });
    } finally {
      setOpeningSession(false);
    }
  };

  const handleVote = async (voto) => {
    if (sessionInfo.status !== SESSION_STATUS.ABERTA) {
      setModal({ isOpen: true, type: 'error', title: 'Votação indisponível', message: 'A votação só pode ser realizada após a sessão ser aberta.' });
      return;
    }

    const cpfSanitizado = cpf.replace(/\D/g, '');
    if (!cpfSanitizado) {
      setModal({ isOpen: true, type: 'error', title: 'Atenção', message: 'Por favor, insira o seu CPF para votar.' });
      return;
    }

    if (cpfSanitizado.length !== 11) {
      setModal({ isOpen: true, type: 'error', title: 'CPF inválido', message: 'Informe um CPF com 11 dígitos numéricos.' });
      return;
    }

    if (!['SIM', 'NAO'].includes(voto)) {
      setModal({ isOpen: true, type: 'error', title: 'Voto inválido', message: 'Escolha do voto é obrigatória.' });
      return;
    }

    if (voto === 'SIM') setVotingSim(true);
    else setVotingNao(true);

    try {
      const response = await api.post(`/pautas/${id}/votos`, {
        associadoId: cpfSanitizado,
        escolha: voto,
      });

      setModal({
        isOpen: true,
        type: 'success',
        title: 'Voto Computado',
        message: response?.data?.mensagem || 'Voto recebido e enviado para processamento.',
      });
      setCpf('');
    } catch (error) {
      const backendMessage = getBackendErrorMessage(error, 'Verifique se a sessão está aberta e se seus dados estão corretos.');
      setModal({
        isOpen: true,
        type: 'error',
        title: 'Voto não registrado',
        message: `Ocorreu um erro ao processar seu voto. Detalhes: ${backendMessage}`,
      });
    } finally {
      setVotingSim(false);
      setVotingNao(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <span className="material-symbols-outlined animate-spin-custom text-primary text-4xl">progress_activity</span>
      </div>
    );
  }

  if (!pauta) {
    return (
      <div className="text-center p-12 text-on-surface-variant">
        Pauta não encontrada.
        <br />
        <Link to="/" className="text-primary font-bold hover:underline mt-4 inline-block">Voltar ao início</Link>
      </div>
    );
  }

  const sessionBadge = getSessionBadge();
  const totalSim = Number(resultadoVotacao?.totalSim || 0);
  const totalNao = Number(resultadoVotacao?.totalNao || 0);
  const temVotos = totalSim + totalNao > 0;

  return (
    <div className="max-w-5xl mx-auto animate-in fade-in duration-700 relative">
      {modal.isOpen && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/40 backdrop-blur-sm transition-opacity">
          <div className="bg-surface-container-lowest w-full max-w-md rounded-2xl p-6 shadow-2xl animate-in zoom-in-95 duration-200">
            <div className="flex items-center gap-4 mb-4">
              <div className={`w-12 h-12 rounded-full flex items-center justify-center ${modal.type === 'success' ? 'bg-primary-container text-primary' : 'bg-error-container text-error'}`}>
                <span className="material-symbols-outlined text-2xl">{modal.type === 'success' ? 'check_circle' : 'warning'}</span>
              </div>
              <h3 className="text-xl font-bold font-headline text-on-surface">{modal.title}</h3>
            </div>
            <p className="text-on-surface-variant mb-8 leading-relaxed">{modal.message}</p>
            <div className="flex justify-end">
              <button onClick={closeModal} className="px-6 py-2.5 bg-surface-container-high hover:bg-surface-variant text-on-surface font-bold rounded-xl transition-colors">
                Fechar
              </button>
            </div>
          </div>
        </div>
      )}

      <div className="mb-4">
        <span className={`${sessionBadge.classes} text-[10px] font-bold uppercase tracking-widest px-3 py-1 rounded-full`}>
          {sessionBadge.label}
        </span>
      </div>

      <div className="flex items-center justify-between gap-2 mb-8">
        <span className="px-3 py-1 bg-surface-container-high text-on-surface text-[10px] font-bold uppercase tracking-widest rounded-full">
          ID: {pauta.id.split('-')[0]}
        </span>

        <div className="flex items-end gap-2">
          <div>
            <label htmlFor="session-hours" className="block text-[10px] font-bold uppercase tracking-widest text-on-surface-variant mb-1">
              Horas
            </label>
            <input
              id="session-hours"
              type="number"
              min="0"
              step="1"
              value={sessionHours}
              onChange={(e) => setSessionHours(e.target.value.replace(/\D/g, ''))}
              disabled={sessionInfo.status !== SESSION_STATUS.CRIADA}
              className="w-20 bg-surface-container-low border border-outline-variant/40 rounded-lg px-3 py-2 text-sm text-on-surface outline-none focus:border-primary disabled:opacity-60"
            />
          </div>
          <div>
            <label htmlFor="session-minutes" className="block text-[10px] font-bold uppercase tracking-widest text-on-surface-variant mb-1">
              Minutos
            </label>
            <input
              id="session-minutes"
              type="number"
              min="0"
              max="59"
              step="1"
              value={sessionMinutes}
              onChange={(e) => {
                const digits = e.target.value.replace(/\D/g, '').slice(0, 2);
                if (digits === '') {
                  setSessionMinutes('');
                  return;
                }

                const valor = Number(digits);
                setSessionMinutes(String(Math.min(valor, 59)));
              }}
              disabled={sessionInfo.status !== SESSION_STATUS.CRIADA}
              className="w-24 bg-surface-container-low border border-outline-variant/40 rounded-lg px-3 py-2 text-sm text-on-surface outline-none focus:border-primary disabled:opacity-60"
            />
          </div>
          <button
            onClick={handleAbrirSessao}
            disabled={openingSession || sessionInfo.status !== SESSION_STATUS.CRIADA}
            className="flex items-center gap-2 px-4 py-2 bg-secondary text-on-secondary rounded-lg font-bold text-sm hover:opacity-90 active:scale-95 transition-all disabled:opacity-50"
          >
            {openingSession ? (
              <span className="material-symbols-outlined animate-spin-custom text-sm">progress_activity</span>
            ) : (
              <span className="material-symbols-outlined text-sm">play_arrow</span>
            )}
            Iniciar Votação
          </button>
        </div>
      </div>

      <section className="mb-16">
        <h1 className="font-headline text-4xl md:text-5xl font-extrabold text-primary tracking-tighter mb-6 leading-tight">Pauta de Deliberação</h1>
        <p className="text-xl text-on-surface-variant leading-relaxed max-w-3xl font-light">{pauta.descricao}</p>
      </section>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 mb-16">
        <div className="lg:col-span-7 space-y-8">
          <div className="bg-surface-container-low p-8 rounded-xl">
            <h3 className="font-headline text-xl text-primary mb-4">Contexto do Projeto</h3>
            <p className="text-on-surface leading-loose text-sm">
              Esta é uma página de deliberação oficial. Leia com atenção a descrição da pauta acima. Seu voto é único e garantido pela nossa arquitetura de processamento assíncrono.
            </p>
          </div>
          <div className="relative overflow-hidden rounded-xl h-64 group">
            <img
              alt="Office architecture"
              className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
              src="https://lh3.googleusercontent.com/aida-public/AB6AXuCTgnttuYAmpniXoz2SEgNmEr_uisUogbBh2ieVte7STjuBZSY1vO-sJfeTPHmj5a59EbvLbq6yeUD-xJRCKA-Noop3gzzEBDrNPLDS4UgFPHQQAVhjSu5lHILb6JrnJpCIfO6bx_yUCmZMTnXOOLGyHTIsZQHhSDHU0X-lSngqA5JoVN4yYnZdModwch6HHCCW1sF6NvlD51iPsmfs4MOGUDVjGDp14q8yCT9KI9JPobZuPhXKreQrjM1jxUMepcT86AJX5N72WR8"
            />
            <div className="absolute inset-0 bg-gradient-to-t from-primary/40 to-transparent" />
          </div>
        </div>

        <div className="lg:col-span-5">
          <div className="bg-surface-container-lowest p-8 md:p-10 shadow-[0_20px_40px_rgba(43,52,55,0.06)] rounded-xl border border-outline-variant/10">
            {sessionInfo.status === SESSION_STATUS.ABERTA ? (
              <>
                <h2 className="font-headline text-2xl text-on-surface mb-8 tracking-tight">Registre seu Voto</h2>

                <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
                  <div>
                    <label className="block text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-3" htmlFor="cpf">
                      CPF do Cooperado
                    </label>
                    <input
                      id="cpf"
                      type="text"
                      value={cpf}
                      onChange={(e) => setCpf(formatCpf(e.target.value))}
                      placeholder="000.000.000-00"
                      inputMode="numeric"
                      maxLength={14}
                      className="w-full bg-surface-container-low border-0 border-b-2 border-transparent focus:border-primary focus:ring-0 transition-all px-4 py-4 text-on-surface placeholder:text-outline-variant rounded-t-lg outline-none"
                    />
                  </div>

                  <div className="pt-4 grid grid-cols-2 gap-4">
                    <button
                      type="button"
                      disabled={votingSim || votingNao}
                      onClick={() => handleVote('SIM')}
                      className="group relative flex flex-col items-center justify-center gap-2 bg-primary text-on-primary py-6 rounded-xl hover:bg-primary-dim active:scale-[0.98] transition-all disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-3xl mb-1">check_circle</span>
                      <span className="font-headline font-bold text-lg tracking-widest uppercase">Sim</span>
                      {votingSim && (
                        <div className="absolute top-2 right-2">
                          <span className="material-symbols-outlined animate-spin-custom text-white/50 text-sm">progress_activity</span>
                        </div>
                      )}
                    </button>

                    <button
                      type="button"
                      disabled={votingSim || votingNao}
                      onClick={() => handleVote('NAO')}
                      className="group relative flex flex-col items-center justify-center gap-2 bg-error text-on-error py-6 rounded-xl hover:bg-error-dim active:scale-[0.98] transition-all disabled:opacity-50"
                    >
                      <span className="material-symbols-outlined text-3xl mb-1">cancel</span>
                      <span className="font-headline font-bold text-lg tracking-widest uppercase">Não</span>
                      {votingNao && (
                        <div className="absolute top-2 right-2">
                          <span className="material-symbols-outlined animate-spin-custom text-white/50 text-sm">progress_activity</span>
                        </div>
                      )}
                    </button>
                  </div>
                </form>
              </>
            ) : (
              <div className="space-y-4">
                <h2 className="font-headline text-2xl text-on-surface tracking-tight">Votação indisponível</h2>
                <p className="text-sm text-on-surface-variant leading-relaxed">
                  {sessionInfo.status === SESSION_STATUS.CRIADA
                    ? 'A pauta foi criada, mas a sessão de votação ainda não foi aberta. Defina o tempo e clique em "Iniciar Votação".'
                    : 'A sessão desta pauta foi encerrada. O formulário de voto não está mais disponível.'}
                </p>

                {sessionInfo.status === SESSION_STATUS.ENCERRADA && (
                  <div className="pt-2">
                    {loadingResultado ? (
                      <p className="text-sm text-on-surface-variant">Carregando resultado da votação...</p>
                    ) : temVotos ? (
                      <div className="grid grid-cols-2 gap-3">
                        <div className="bg-surface-container-low rounded-lg p-4">
                          <p className="text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">Votos SIM</p>
                          <p className="text-2xl font-extrabold text-primary mt-1">{totalSim}</p>
                        </div>
                        <div className="bg-surface-container-low rounded-lg p-4">
                          <p className="text-[11px] font-bold uppercase tracking-wider text-on-surface-variant">Votos NAO</p>
                          <p className="text-2xl font-extrabold text-error mt-1">{totalNao}</p>
                        </div>
                      </div>
                    ) : (
                      <p className="text-sm text-on-surface-variant">Sessão encerrada sem votos registrados.</p>
                    )}
                  </div>
                )}

                <Link to="/" className="inline-flex items-center gap-1 text-primary font-bold text-sm hover:underline">
                  Voltar ao Dashboard <span className="material-symbols-outlined text-sm">arrow_forward</span>
                </Link>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

export default DetalhePauta;