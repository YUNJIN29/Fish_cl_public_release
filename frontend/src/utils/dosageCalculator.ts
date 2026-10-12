export type VolumeInput = {
  mode: 'dimensions' | 'direct'
  length?: number
  width?: number
  depth?: number
  directVolume?: number
}

export type DoseInput = {
  mode: 'product' | 'active'
  volumeM3: number
  dosePerM3?: number
  targetMgL?: number
  concentrationPercent?: number
}

export function calculateVolume(input: VolumeInput): number | null {
  const value = input.mode === 'direct'
    ? input.directVolume
    : Number(input.length) * Number(input.width) * Number(input.depth)
  return validPositive(value) ? value! : null
}

export function calculateProductAmount(input: DoseInput): number | null {
  if (!validPositive(input.volumeM3)) return null
  if (input.mode === 'product') {
    if (!validPositive(input.dosePerM3)) return null
    return input.volumeM3 * input.dosePerM3!
  }
  if (!validPositive(input.targetMgL) || !validPositive(input.concentrationPercent) || input.concentrationPercent! > 100) return null
  const activeIngredientGrams = input.targetMgL! * input.volumeM3
  return activeIngredientGrams / (input.concentrationPercent! / 100)
}

export function formatMass(grams: number) {
  if (grams >= 1000) return { value: trim(grams / 1000), unit: 'kg' }
  return { value: trim(grams), unit: 'g' }
}

export function formatVolume(volumeM3: number) {
  return `${trim(volumeM3)} m³（约 ${trim(volumeM3 * 1000)} L）`
}

function validPositive(value?: number): value is number {
  return Number.isFinite(value) && value! > 0
}

function trim(value: number) {
  return Number(value.toFixed(value >= 100 ? 1 : value >= 10 ? 2 : 3)).toLocaleString('zh-CN')
}
