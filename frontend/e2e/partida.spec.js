import { test, expect } from '@playwright/test'

async function iniciarPartida(page) {
  await page.goto('/')
  await page.getByTestId('nombre-0').fill('Ana')
  await page.getByTestId('nombre-1').fill('Beto')
  await page.getByTestId('btn-iniciar').click()
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Ana')
}

test('turno exitoso: sacar carta, terminar turno y guardar las cartas', async ({ page }) => {
  await iniciarPartida(page)
  await expect(page.getByTestId('cartas-restantes')).toHaveText(
    'Cartas en el montón de polvo: 10',
  )

  await page.getByTestId('btn-sacar').click()
  await expect(page.getByTestId('mensaje')).toContainText('Sacaste una carta')
  await expect(page.getByTestId('delante-Ana')).toContainText('Valor 5: 1 carta(s)')
  await expect(page.getByTestId('cartas-restantes')).toHaveText(
    'Cartas en el montón de polvo: 9',
  )

  await page.getByTestId('btn-terminar').click()
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Beto')
  await expect(page.getByTestId('delante-Ana')).toContainText('Valor 5: 1 carta(s)')

  await page.getByTestId('btn-sacar').click()
  await expect(page.getByTestId('delante-Beto')).toContainText('Valor 5: 1 carta(s)')
  await page.getByTestId('btn-terminar').click()
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Ana')

  await page.getByTestId('btn-guardar').click()
  await expect(page.getByTestId('mensaje')).toContainText('Cartas guardadas')
  await expect(page.getByTestId('delante-Ana')).toContainText('Sin cartas delante')
  await expect(page.getByTestId('monton-Ana')).toContainText('Puntos: 5')
})

test('turno perdido: repetir una carta con 3 cartas delante descarta todo y pasa el turno', async ({
  page,
}) => {
  await iniciarPartida(page)
  const delanteAna = page.getByTestId('delante-Ana')

  await page.getByTestId('btn-sacar').click()
  await expect(delanteAna).toContainText('Valor 5: 1 carta(s)')
  await page.getByTestId('btn-sacar').click()
  await expect(delanteAna).toContainText('Valor 5: 2 carta(s)')
  await page.getByTestId('btn-sacar').click()
  await expect(delanteAna).toContainText('Valor 2: 1 carta(s)')
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Ana')

  await page.getByTestId('btn-sacar').click()

  await expect(page.getByTestId('mensaje')).toContainText('Perdiste el turno')
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Beto')
  await expect(delanteAna).toContainText('Sin cartas delante')
  await expect(page.getByTestId('monton-Ana')).toContainText('Puntos: 0')
  await expect(page.getByTestId('delante-Beto')).toContainText('Sin cartas delante')
  await expect(page.getByTestId('cartas-restantes')).toHaveText(
    'Cartas en el montón de polvo: 6',
  )
})

test('robo: el jugador toma las cartas del mismo valor que tiene otro jugador', async ({
  page,
}) => {
  await iniciarPartida(page)
  await page.getByTestId('btn-sacar').click()
  await expect(page.getByTestId('delante-Ana')).toContainText('Valor 5: 1 carta(s)')
  await page.getByTestId('btn-terminar').click()
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Beto')

  await page.getByTestId('btn-sacar').click()
  await expect(page.getByTestId('delante-Beto')).toContainText('Valor 5: 1 carta(s)')
  await page.getByTestId('select-victima').selectOption('Ana')
  await page.getByTestId('btn-robar').click()

  await expect(page.getByTestId('mensaje')).toContainText('Le robaste cartas a Ana')
  await expect(page.getByTestId('delante-Beto')).toContainText('Valor 5: 2 carta(s)')
  await expect(page.getByTestId('delante-Ana')).toContainText('Sin cartas delante')
})

test('el backend rechaza terminar el turno sin haber sacado una carta', async ({ page }) => {
  await iniciarPartida(page)

  await page.getByTestId('btn-terminar').click()

  await expect(page.getByTestId('error')).toContainText('Debes sacar al menos una carta')
  await expect(page.getByTestId('turno-de')).toHaveText('Turno de: Ana')
})