/**
 * Configuração central de ambiente. Único lugar do frontend que sabe onde o
 * backend está — mas agora essa decisão é AUTOMÁTICA, baseada no hostname de
 * onde a própria página está sendo servida, em vez de um valor fixo editado
 * à mão toda vez que você alterna entre testar local e fazer deploy.
 *
 * Por quê: este projeto é HTML/CSS/JS puro, sem bundler, sem build step (ver
 * README, seção de decisões de arquitetura) — não existe um passo de build
 * que possa injetar variável de ambiente diferente por ambiente, como
 * aconteceria num projeto com Vite/Webpack. `window.location.hostname` é o
 * único sinal confiável, disponível em runtime, sem precisar de build step,
 * pra saber em qual ambiente o código está rodando.
 *
 * Mesmo conteúdo do config.js do Customer App — os dois frontends apontam
 * pro mesmo backend, só rodam em Static Sites separados.
 */
const isLocalEnvironment = ['localhost', '127.0.0.1'].includes(window.location.hostname);

export const CONFIG = {
    API_BASE_URL: isLocalEnvironment
        ? 'http://localhost:8080/api/v1'
        : 'https://pedacinho-de-maria.onrender.com/api/v1',
    WS_URL: isLocalEnvironment
        ? 'ws://localhost:8080/ws'
        : 'wss://pedacinho-de-maria.onrender.com/ws',
};
