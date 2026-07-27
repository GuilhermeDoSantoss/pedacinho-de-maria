/**
 * Validação client-side — existe só para feedback instantâneo (evitar que o
 * cliente preencha as 6 telas e descubra um erro só no fim). O backend
 * (CreateOrderUseCase) é a única fonte de verdade; se as duas divergirem, a
 * resposta de erro da API prevalece e é exibida normalmente.
 *
 * As constantes de horário de funcionamento abaixo duplicam OPENING_TIME/
 * LAST_PICKUP_TIME do backend. É uma duplicação real e consciente — não há
 * endpoint de configuração pública para consultar isso ainda (faz sentido
 * criar um quando o Admin Panel existir e o horário deixar de ser fixo).
 * Até lá, se o horário de funcionamento mudar, precisa atualizar nos dois
 * lugares — documentado aqui para não ser esquecido.
 */
const OPENING_TIME_MINUTES = 11 * 60;
const LAST_PICKUP_TIME_MINUTES = 15 * 60 + 30;

export function validateCustomerName(name) {
    const trimmed = name.trim();
    if (trimmed.length < 2 || trimmed.length > 60) {
        return 'Nome deve ter entre 2 e 60 caracteres';
    }
    return null;
}

export function validatePickupTime(timeString) {
    if (!timeString) {
        return 'Escolha um horário de retirada';
    }

    const [hours, minutes] = timeString.split(':').map(Number);
    const totalMinutes = hours * 60 + minutes;

    const now = new Date();
    const nowMinutes = now.getHours() * 60 + now.getMinutes();

    if (totalMinutes < nowMinutes) {
        return 'Horário de retirada não pode estar no passado';
    }
    if (totalMinutes < OPENING_TIME_MINUTES || totalMinutes > LAST_PICKUP_TIME_MINUTES) {
        return 'Horário de retirada deve ser entre 11:00 e 15:30';
    }
    return null;
}

export function validateObservation(text) {
    if (text && text.length > 140) {
        return 'Observação deve ter no máximo 140 caracteres';
    }
    return null;
}
