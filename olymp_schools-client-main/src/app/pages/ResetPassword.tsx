// src/pages/ResetPassword.tsx
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { toast } from "react-toastify";
import axios from "axios";
import { getCustomToastStyle } from "../components/ui/toastStyles";
import { useThemeStore } from "../stores/themeStore";
import { Button } from "../components/ui/Button";
import Input from "../components/ui/Input";
import { useForm } from "react-hook-form";
import Navbar from "../components/layout/Navbar/Navbar";
import Footer from "../components/layout/Footer/Footer";
import { BackgroundBlobs } from "../components/ui/BackgroundBlobs/BackgroundBlobs";
import cn from "clsx";

interface ResetForm {
  newPassword: string;
  confirmPassword: string;
}

export default function ResetPassword() {
  const API_URL = import.meta.env.VITE_API_URL;
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");
  const navigate = useNavigate();
  const { isDarkMode } = useThemeStore();
  const [isValidToken, setIsValidToken] = useState<boolean | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<ResetForm>();

  const password = watch("newPassword");

  // Проверка токена при загрузке
  useEffect(() => {
    if (!token) {
      toast.error("Токен не указан", getCustomToastStyle(isDarkMode));
      navigate("/");
      return;
    }

    const validate = async () => {
      try {
        const res = await axios.get(`${API_URL}/auth/validate-reset-token/${token}`);
        if (res.data.success) {
          setIsValidToken(true);
        } else {
          setIsValidToken(false);
          toast.error("Неверный или просроченный токен", getCustomToastStyle(isDarkMode));
          navigate("/");
        }
      } catch (err) {
        setIsValidToken(false);
        toast.error("Неверный или просроченный токен", getCustomToastStyle(isDarkMode));
        navigate("/");
      }
    };

    validate();
  }, [token, API_URL, isDarkMode, navigate]);

  const onSubmit = async (data: ResetForm) => {
    if (!token) return;
    setIsSubmitting(true);
    try {
      await axios.post(`${API_URL}/auth/reset-password`, {
        token,
        newPassword: data.newPassword,
      });
      toast.success("Пароль успешно изменён!", getCustomToastStyle(isDarkMode));
      navigate("/login");
    } catch (err: any) {
      const msg = err.response?.data?.message || "Ошибка при сбросе пароля";
      toast.error(msg, getCustomToastStyle(isDarkMode));
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isValidToken === null) {
    return (
      <div className="flex min-h-screen items-center justify-center">Загрузка...</div>
    );
  }

  if (!isValidToken) {
    return null; // Редирект уже произошёл
  }

  return (
    <div
      className={cn("min-h-screen w-screen font-sans", {
        "bg-[#0b0f1a] text-white": isDarkMode,
        "bg-gray-50 text-gray-900": !isDarkMode,
      })}
    >
      <BackgroundBlobs />
      <Navbar />
      <div className="flex min-h-[80vh] items-center justify-center px-4">
        <div className="w-full max-w-md rounded-2xl bg-white/90 p-6 shadow-lg backdrop-blur-sm dark:bg-[#111827]/90">
          <h2 className="mb-6 text-center text-2xl font-bold">Сброс пароля</h2>
          <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
            <div>
              <Input
                type="password"
                placeholder="Новый пароль"
                {...register("newPassword", {
                  required: "Обязательное поле",
                  minLength: {
                    value: 6,
                    message: "Минимум 6 символов",
                  },
                })}
              />
              {errors.newPassword && (
                <p className="mt-1 text-sm text-red-500">{errors.newPassword.message}</p>
              )}
            </div>
            <div>
              <Input
                type="password"
                placeholder="Подтвердите пароль"
                {...register("confirmPassword", {
                  required: "Обязательное поле",
                  validate: (value) => value === password || "Пароли не совпадают",
                })}
              />
              {errors.confirmPassword && (
                <p className="mt-1 text-sm text-red-500">{errors.confirmPassword.message}</p>
              )}
            </div>
            <Button type="submit" disabled={isSubmitting} className="w-full">
              {isSubmitting ? "Сохранение..." : "Сохранить новый пароль"}
            </Button>
          </form>
        </div>
      </div>
      <Footer />
    </div>
  );
}