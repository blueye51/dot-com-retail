const CM_TO_IN = 0.393701;
const G_TO_OZ = 0.035274;
const KG_TO_LB = 2.20462;

export function cmToIn(cm) {
    return +(cm * CM_TO_IN).toFixed(2);
}

export function inToCm(inches) {
    return +(inches / CM_TO_IN).toFixed(2);
}

export function gToOz(g) {
    return +(g * G_TO_OZ).toFixed(2);
}

export function ozToG(oz) {
    return +(oz / G_TO_OZ).toFixed(2);
}

export function kgToLb(kg) {
    return +(kg * KG_TO_LB).toFixed(2);
}

export function lbToKg(lb) {
    return +(lb / KG_TO_LB).toFixed(2);
}

export function formatDimension(value, imperial) {
    if (value == null) return null;
    return imperial ? `${cmToIn(value)} in` : `${value} cm`;
}

export function formatWeight(value, imperial) {
    if (value == null) return null;
    return imperial ? `${kgToLb(value)} lb` : `${value} kg`;
}
