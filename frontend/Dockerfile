FROM node:20-alpine
WORKDIR /app

# Copia os arquivos de dependência
COPY package.json package-lock.json* ./

# Instala as dependências
RUN npm install

# Copia o resto do código
COPY . .

EXPOSE 5173

# O parâmetro --host é obrigatório para o Vite expor a porta para fora do container Docker
CMD ["npm", "run", "dev", "--", "--host"]