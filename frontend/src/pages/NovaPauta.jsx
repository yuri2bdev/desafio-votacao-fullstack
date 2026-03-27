import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api } from '../services/api';
import { markPautaCreated } from '../services/sessionState';

function NovaPauta() {
  const [descricao, setDescricao] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!descricao.trim()) return;

    setLoading(true);

    api.post('/pautas', { descricao })
        .then((response) => {
          const pautaId = response?.data?.id;
          if (pautaId) {
            markPautaCreated(pautaId);
            navigate(`/pautas/${pautaId}`);
            return;
          }

          navigate('/');
        })
        .catch((error) => {
          console.error("Erro ao criar pauta:", error);
          alert("Erro ao criar pauta. Verifique se o backend está rodando.");
        })
        .finally(() => {
          setLoading(false);
        });
  };

  return (
      <div className="animate-in slide-in-from-bottom-4 duration-500 max-w-2xl mx-auto">
        <Link to="/" className="group flex items-center gap-2 text-on-surface-variant hover:text-primary transition-all mb-8 font-medium">
          <span className="material-symbols-outlined text-[20px] group-hover:-translate-x-1 transition-transform">arrow_back</span>
          <span>Voltar ao Dashboard</span>
        </Link>

        <div className="grid grid-cols-1 md:grid-cols-12 gap-8 mb-12 items-end">
          <div className="md:col-span-8">
            <h1 className="text-4xl md:text-5xl font-extrabold font-headline tracking-tight text-on-surface mb-4">
              Nova Pauta
            </h1>
            <p className="text-on-surface-variant text-lg leading-relaxed max-w-md">
              Inicie uma nova deliberação para a cooperativa. Descreva os objetivos e o contexto para os membros.
            </p>
          </div>
        </div>

        <div className="bg-surface-container-lowest rounded-xl p-8 md:p-12 shadow-[0_20px_40px_rgba(43,52,55,0.06)]">
          <form onSubmit={handleSubmit} className="space-y-10">
            <div className="group">
              <div className="flex justify-between items-center mb-3">
                <label htmlFor="topic-description" className="block text-sm font-bold text-on-surface-variant uppercase tracking-widest">
                  Descrição da Pauta
                </label>
              </div>
              <textarea
                  id="topic-description"
                  rows="8"
                  required
                  value={descricao}
                  onChange={(e) => setDescricao(e.target.value)}
                  placeholder="Descreva detalhadamente a proposta, os benefícios esperados e o impacto para a cooperativa..."
                  className="w-full bg-surface-container-low border-none rounded-xl focus:ring-2 focus:ring-primary/20 focus:bg-white transition-all p-6 text-on-surface leading-relaxed placeholder:text-outline-variant/60 outline-none"
              ></textarea>
            </div>

            <div className="flex flex-col md:flex-row items-center justify-between gap-6 pt-6 border-t border-outline-variant/10">
              <div className="flex items-center gap-3 text-on-surface-variant">
                <span className="material-symbols-outlined text-[20px]">info</span>
                <span className="text-xs">Esta pauta será publicada imediatamente.</span>
              </div>

              <div className="flex gap-4 w-full md:w-auto">
                <Link to="/" className="flex-1 md:flex-none px-8 py-3 rounded-lg text-primary font-bold hover:bg-primary-container/30 transition-all text-center">
                  Cancelar
                </Link>
                <button
                    type="submit"
                    disabled={loading}
                    className="flex-1 md:flex-none px-12 py-3 rounded-lg bg-primary text-on-primary font-bold shadow-lg shadow-primary/20 hover:shadow-xl hover:translate-y-[-2px] transition-all flex items-center justify-center gap-2 disabled:opacity-70"
                >
                  {loading ? (
                      <span className="material-symbols-outlined animate-spin-custom">progress_activity</span>
                  ) : (
                      <span className="material-symbols-outlined text-[20px]">check_circle</span>
                  )}
                  Salvar Pauta
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
  );
}

export default NovaPauta;