/**
 * Configuração central de ambiente. Único lugar do frontend que sabe onde o
 * backend está — trocar de localhost para produção é editar 2 linhas aqui,
 * não caçar URLs espalhadas pelos módulos.
 */
export const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api/v1',
    WS_URL: 'ws://localhost:8080/ws',
};
