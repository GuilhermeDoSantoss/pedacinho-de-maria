/**
 * Validação client-side — existe só para feedback instantâneo (evitar que o
 * cliente preencha as 6 telas e descubra um erro só no fim). O backend
 * (PickupTimePolicy) é a única fonte de verdade; se as duas divergirem, a
 * resposta de erro da API prevalece e é exibida normalmente.
 */
let pickupTimePolicy = null;

export async function loadPickupTimePolicy() {
    const response = await fetch('http://localhost:8080/api/v1/orders/pickup-time-policy');
    pickupTimePolicy = await response.json();
}

function getPickupTimePolicy() {
    if (!pickupTimePolicy) {
        return { openingTime: '11:00', closingTime: '15:30' };
    }
    return pickupTimePolicy;
}

export function validateCustomerName(name) {
    const trimmed = name.trim();
    if (trimmed.length < 2 || trimmed.length > 60) {
        return 'Nome deve ter entre 2 e 60 caracteres';
    }
    return null;
}

export function validatePhoneNumber(phoneNumber, required = false) {
    const digitsOnly = (phoneNumber || '').replace(/\D/g, '');

    if (!required) {
        return null;
    }

    if (!digitsOnly) {
        return 'Informe um telefone para pedidos para viagem';
    }

    if (digitsOnly.length < 10 || digitsOnly.length > 11) {
        return 'Telefone inválido. Informe um número entre 10 e 11 dígitos.';
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

    const policy = getPickupTimePolicy();
    const [openingHours, openingMinutes] = policy.openingTime.split(':').map(Number);
    const [closingHours, closingMinutes] = policy.closingTime.split(':').map(Number);
    const openingMinutesValue = openingHours * 60 + openingMinutes;
    const closingMinutesValue = closingHours * 60 + closingMinutes;

    if (totalMinutes < nowMinutes) {
        return 'Horário de retirada não pode estar no passado';
    }
    if (totalMinutes < openingMinutesValue || totalMinutes > closingMinutesValue) {
        return `Horário de retirada deve ser entre ${policy.openingTime} e ${policy.closingTime}`;
    }
    return null;
}

export function validateObservation(text) {
    if (text && text.length > 140) {
        return 'Observação deve ter no máximo 140 caracteres';
    }
    return null;
}
