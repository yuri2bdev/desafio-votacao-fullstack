import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import NovaPauta from './pages/NovaPauta';
import DetalhePauta from './pages/DetalhePauta';

function App() {
  return (
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Dashboard />} />
            <Route path="pautas/encerradas" element={<Dashboard onlyClosed />} />
            <Route path="pautas/nova" element={<NovaPauta />} />
            <Route path="pautas/:id" element={<DetalhePauta />} />
          </Route>
        </Routes>
      </BrowserRouter>
  );
}

export default App;