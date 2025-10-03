FROM node:18-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:alpine

# Копируем продакшен конфиг nginx
COPY nginx-prod.conf /etc/nginx/conf.d/default.conf

# Копируем собранное приложение
COPY --from=builder /app/dist /usr/share/nginx/html

# Копируем страницу обслуживания
COPY public/maintenance.html /usr/share/nginx/html/maintenance.html

EXPOSE 80

# Устанавливаем права
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html

# Запускаем nginx
CMD ["nginx", "-g", "daemon off;"]