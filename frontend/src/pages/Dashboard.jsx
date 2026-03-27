import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { getPautaSessionInfo, SESSION_STATUS } from '../services/sessionState';

const RESULTADO_LABEL = {
  APROVADA: 'Aprovada',
  REPROVADA: 'Reprovada',
  EMPATE: 'Empate',
  SEM_VOTOS: 'Sem votos',
};

const STATUS_BADGE_CLASS = {
  CRIADA: 'bg-surface-container-high text-on-surface-variant',
  ABERTA: 'bg-primary-container text-on-primary-fixed',
  ENCERRADA: 'bg-secondary-container text-on-secondary-fixed',
};

function Dashboard({ onlyClosed = false }) {
  const [pautas, setPautas] = useState([]);
  const [loading, setLoading] = useState(true);

  const mapStatusFromSessionInfo = (sessionInfo) => {
    if (sessionInfo?.status === SESSION_STATUS.ABERTA) {
      return { uiStatus: 'ABERTA', uiStatusLabel: 'Aberta' };
    }

    if (sessionInfo?.status === SESSION_STATUS.ENCERRADA) {
      return { uiStatus: 'ENCERRADA', uiStatusLabel: 'Encerrada' };
    }

    return { uiStatus: 'CRIADA', uiStatusLabel: 'Pauta criada' };
  };

  useEffect(() => {
    const carregarPautas = async () => {
      try {
        const response = await api.get('/pautas');

        const pautasOrdenadas = [...response.data].sort((a, b) => {
          const dataA = new Date(a.dataCriacao).getTime();
          const dataB = new Date(b.dataCriacao).getTime();
          return dataB - dataA;
        });

        const pautasComStatus = await Promise.all(
          pautasOrdenadas.map(async (pauta) => {
            const sessionInfo = getPautaSessionInfo(pauta.id);
            const statusInfo = mapStatusFromSessionInfo(sessionInfo);

            if (statusInfo.uiStatus === 'ENCERRADA') {
              try {
                const resultadoResponse = await api.get(`/pautas/${pauta.id}/resultado`);
                const resultado = resultadoResponse?.data?.status;
                return {
                  ...pauta,
                  ...statusInfo,
                  uiStatusLabel: resultado ? `Encerrada - ${RESULTADO_LABEL[resultado] || resultado}` : statusInfo.uiStatusLabel,
                };
              } catch {}
            }

            return { ...pauta, ...statusInfo };
          }),
        );

        setPautas(pautasComStatus);
      } catch (error) {
        console.error('Erro ao buscar pautas:', error);
      } finally {
        setLoading(false);
      }
    };

    carregarPautas();
  }, []);

  const pautasAtivas = pautas.filter((pauta) => pauta.uiStatus === 'ABERTA').length;
  const pautasFiltradas = onlyClosed ? pautas.filter((pauta) => pauta.uiStatus === 'ENCERRADA') : pautas;

  return (
      <div className="animate-in fade-in duration-500">
        <section className="mb-12">
          <h1 className="text-4xl md:text-5xl font-extrabold text-on-surface tracking-tight mb-4 leading-tight">
            Transparência e <br /><span className="text-primary">Decisão Coletiva.</span>
          </h1>
          <p className="text-on-surface-variant max-w-xl text-lg leading-relaxed">
            Bem-vindo ao centro deliberativo da nossa cooperativa. Sua voz molda o futuro de nossa comunidade. Participe ativamente das pautas em aberto.
          </p>
        </section>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-16">
          <div className="bg-surface-container-lowest p-8 rounded-2xl shadow-[0_20px_40px_rgba(43,52,55,0.04)] flex flex-col justify-between h-40">
            <span className="material-symbols-outlined text-primary text-3xl">ballot</span>
            <div>
              <span className="block text-3xl font-black text-on-surface">{pautasAtivas}</span>
              <span className="text-sm font-medium text-on-surface-variant">Pautas Ativas</span>
            </div>
          </div>
          <div className="bg-surface-container-low p-8 rounded-2xl flex flex-col justify-between h-40">
            <span className="material-symbols-outlined text-on-surface-variant text-3xl">group</span>
            <div>
              <span className="block text-3xl font-black text-on-surface">1.240</span>
              <span className="text-sm font-medium text-on-surface-variant">Cooperados Aptos</span>
            </div>
          </div>
          <div className="bg-primary p-8 rounded-2xl flex flex-col justify-between h-40 text-on-primary">
            <span className="material-symbols-outlined text-3xl">how_to_reg</span>
            <div>
              <span className="block text-3xl font-black">88%</span>
              <span className="text-sm font-medium opacity-80">Média de Engajamento</span>
            </div>
          </div>
        </div>

        <div className="flex items-center justify-between mb-8">
          <h2 className="text-2xl font-bold text-on-surface">{onlyClosed ? 'Pautas Encerradas' : 'Votações Recentes'}</h2>
        </div>

        {loading ? (
            <div className="flex justify-center p-12">
              <span className="material-symbols-outlined animate-spin-custom text-primary text-4xl">progress_activity</span>
            </div>
        ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
              {pautasFiltradas.map((pauta) => (
                  <div key={pauta.id} className="group bg-surface-container-lowest p-1 rounded-2xl hover:shadow-[0_20px_40px_rgba(43,52,55,0.06)] transition-all duration-300">
                    <div className="p-6">
                      <div className="flex justify-between items-start mb-6">
                        <span className={`${STATUS_BADGE_CLASS[pauta.uiStatus] || STATUS_BADGE_CLASS.ABERTA} text-[10px] font-bold uppercase tracking-widest px-3 py-1 rounded-full`}>
                          {pauta.uiStatusLabel || 'Aberta'}
                        </span>
                        <span className="text-xs text-on-surface-variant font-medium">
                    Criado em: {new Date(pauta.dataCriacao).toLocaleDateString('pt-BR')}
                  </span>
                      </div>

                      <h3 className="text-xl font-bold text-on-surface mb-3 group-hover:text-primary transition-colors">
                        Pauta de Deliberação
                      </h3>
                      <p className="text-on-surface-variant text-sm mb-6 leading-relaxed line-clamp-2">
                        {pauta.descricao}
                      </p>

                      <div className="flex items-center justify-between pt-6 border-t border-surface-container">
                        <div className="flex -space-x-2">
                          <div className="w-8 h-8 rounded-full border-2 border-white bg-slate-200 overflow-hidden">
                            <img className="w-full h-full object-cover" src="https://lh3.googleusercontent.com/aida-public/AB6AXuDwKugNQEE3Y-Wy6TLf4XSuyuZXX3n_rIvhVQkqlzBbtQYypcinfSZseEJUQMoBY3E0qPkRbsunti3tFcMZAps6vzBETyQl4-jsExNUAGMChgaFx4_ZYh5ZTNp5QwDj_xtazutvRjMwam1CeqkOCDNbtpVp2MraIoKRdaI1TFWUD-FJFHwSbEZWjx6Baiw-8iLKcupf224jxMFXcDBHmKh_ucszaDhzw9uiryLLiAesjZr8SU_hPNUK4nQAwL-lfjIVHP24iL4fhI0" alt="avatar" />
                          </div>
                          <div className="w-8 h-8 rounded-full border-2 border-white bg-primary-container flex items-center justify-center text-[10px] font-bold text-on-primary-fixed">
                            +12
                          </div>
                        </div>

                        <Link to={`/pautas/${pauta.id}`} className="text-primary font-bold text-sm flex items-center gap-1 hover:underline">
                          {pauta.uiStatus === 'ABERTA' ? 'Votar Agora' : pauta.uiStatus === 'CRIADA' ? 'Abrir Votação' : 'Ver Detalhes'} <span className="material-symbols-outlined text-sm">arrow_forward</span>
                        </Link>
                      </div>
                    </div>
                  </div>
              ))}

              {pautasFiltradas.length === 0 && (
                  <div className="col-span-1 md:col-span-2 text-center p-12 bg-surface-container-low rounded-2xl text-on-surface-variant">
                    {onlyClosed
                      ? 'Nenhuma pauta encerrada encontrada no momento.'
                      : 'Nenhuma pauta cadastrada no momento. Crie uma nova pauta!'}
                  </div>
              )}
            </div>
        )}
      </div>
  );
}

export default Dashboard;