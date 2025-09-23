import { Navigate, Route, Routes, BrowserRouter } from "react-router-dom";
import { AdminPage } from "./pages/AdminPage";
import { PublicPage } from "./pages/PublicPage";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<PublicPage />} />
        <Route path="/admin" element={<AdminPage />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
