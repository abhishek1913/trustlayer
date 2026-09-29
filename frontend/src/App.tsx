import { Navigate, Route, Routes } from "react-router-dom";
import Layout from "./components/Layout";
import { RequireAdmin, RequireAuth } from "./components/Guards";
import Register from "./pages/Register";
import Login from "./pages/Login";
import VerifyEmail from "./pages/VerifyEmail";
import ForgotPassword from "./pages/ForgotPassword";
import ResetPassword from "./pages/ResetPassword";
import Dashboard from "./pages/Dashboard";
import Profile from "./pages/Profile";
import PaymentResult from "./pages/PaymentResult";
import Admin from "./pages/Admin";
import AdminUser from "./pages/AdminUser";
import NotFound from "./pages/NotFound";

export default function App() {
  return (
    <Layout>
      <Routes>
        <Route path="/" element={<Navigate to="/dashboard" replace />} />
        <Route path="/register" element={<Register />} />
        <Route path="/login" element={<Login />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/dashboard" element={<RequireAuth><Dashboard /></RequireAuth>} />
        <Route path="/profile" element={<RequireAuth><Profile /></RequireAuth>} />
        <Route path="/payment/success" element={<RequireAuth><PaymentResult outcome="success" /></RequireAuth>} />
        <Route path="/payment/cancel" element={<RequireAuth><PaymentResult outcome="cancel" /></RequireAuth>} />
        <Route path="/admin" element={<RequireAdmin><Admin /></RequireAdmin>} />
        <Route path="/admin/users/:id" element={<RequireAdmin><AdminUser /></RequireAdmin>} />
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Layout>
  );
}
