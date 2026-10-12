import { describe, expect, it } from 'vitest'
import { calculateProductAmount, calculateVolume, formatMass } from './dosageCalculator'

describe('water dosage calculator', () => {
  it('calculates rectangular water volume', () => {
    expect(calculateVolume({ mode: 'dimensions', length: 20, width: 10, depth: 1.5 })).toBe(300)
  })

  it('calculates a label dose in grams per cubic metre', () => {
    expect(calculateProductAmount({ mode: 'product', volumeM3: 300, dosePerM3: 0.5 })).toBe(150)
  })

  it('converts active ingredient target and product concentration', () => {
    expect(calculateProductAmount({ mode: 'active', volumeM3: 100, targetMgL: 0.2, concentrationPercent: 20 })).toBeCloseTo(100)
  })

  it('rejects zero, negative and impossible concentration values', () => {
    expect(calculateVolume({ mode: 'direct', directVolume: 0 })).toBeNull()
    expect(calculateProductAmount({ mode: 'product', volumeM3: 100, dosePerM3: -1 })).toBeNull()
    expect(calculateProductAmount({ mode: 'active', volumeM3: 100, targetMgL: 1, concentrationPercent: 101 })).toBeNull()
  })

  it('uses kilograms for large results', () => {
    expect(formatMass(1250)).toEqual({ value: '1.25', unit: 'kg' })
  })
})
